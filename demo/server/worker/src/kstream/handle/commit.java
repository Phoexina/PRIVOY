package kstream.handle;

import ch.ethz.infk.pps.shared.avro.Digest;
import ch.ethz.infk.pps.shared.avro.HeacHeader;
import ch.ethz.infk.pps.shared.avro.UniversePartitionUpdate;
import ch.ethz.infk.pps.shared.avro.Window;
import ch.ethz.infk.pps.zeph.server.worker.WorkerApp;
import ch.ethz.infk.pps.zeph.shared.DigestOp;
import ch.ethz.infk.pps.zeph.shared.Names;
import ch.ethz.infk.pps.zeph.shared.WindowUtil;
import ch.ethz.infk.pps.zeph.shared.pojo.MemberDiffType;
import ch.ethz.infk.pps.zeph.shared.pojo.WindowStatus;
import java.util.Collections;
import java.util.Map;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.Windowed;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.TimestampedWindowStore;
import org.apache.kafka.streams.state.ValueAndTimestamp;
import org.apache.kafka.streams.state.WindowStore;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.apache.logging.log4j.util.Supplier;

public class commit {
    private static final Logger LOG = LogManager.getLogger();
    private Marker m;
    private final int MEMBER_STATUS_NEW;
    private final int MEMBER_STATUS_DROPPED;
    private final int STATUS_COMMITTED;
    private ProcessorContext ctx;
    private String taskId;
    private long universeId;
    private WindowStore<Long, ValueAndTimestamp<Digest>> ciphertextSumStore;
    private KeyValueStore<Long, Digest> committingSumStore;
    private WindowStore<Long, Integer> commitBufferStore;
    private WindowStore<Long, Integer> expectedTransformationTokenStore;
    private WindowStore<Long, Integer> memberDeltaStore;
    public static final Long GRACE_OVER_MARKER = -1L;

    public commit(ProcessorContext context, Names n) {
        this.MEMBER_STATUS_NEW = MemberDiffType.ADD.code();
        this.MEMBER_STATUS_DROPPED = MemberDiffType.DEL.code();
        this.STATUS_COMMITTED = WindowStatus.COMMITTED.code();
        this.ctx = context;
        this.taskId = this.ctx.taskId().toString();
        this.universeId = n.UNIVERSE_ID;
        this.ciphertextSumStore = (TimestampedWindowStore)this.ctx.getStateStore(n.CIPHERTEXT_SUM_STORE);
        this.committingSumStore = (KeyValueStore)this.ctx.getStateStore(n.COMMITTING_SUM_STORE);
        this.commitBufferStore = (WindowStore)context.getStateStore(n.COMMIT_BUFFER_STORE);
        this.expectedTransformationTokenStore = (WindowStore)context.getStateStore(n.EXPECTED_TRANSFORMATION_TOKEN_STORE);
        this.memberDeltaStore = (WindowStore)context.getStateStore(n.MEMBER_DELTA_STORE);
        this.m = MarkerManager.getMarker("taskId" + context.taskId().toString());
        this.m.addParents(new Marker[]{WorkerApp.GLOBAL_MARKER});
    }

    public Iterable<KeyValue<Long, UniversePartitionUpdate>> commitWindowedAggregate(long producerId, Digest windowedAggregate, Window window) {
        long windowStart = window.getStart();
        //通过密文传输的header提取密文
        this.ciphertextSumStore.put(producerId, (ValueAndTimestamp<Digest>) null, windowStart);
        Digest aggregator = (Digest)this.committingSumStore.get(windowStart);
        if (aggregator == null) {
            aggregator = DigestOp.empty();
        }
        //密文+窗口
        aggregator = DigestOp.add(aggregator, windowedAggregate);
        //存储进committingSumStore
        this.committingSumStore.put(windowStart, aggregator);
        //等待该窗口pid的token
        this.expectedTransformationTokenStore.put(producerId, 1, windowStart);
        this.handleDeltaMembershipStore(producerId, windowStart);
        LOG.debug(this.m, "{} - commit from producer={}", new Supplier[]{() -> {
            return WindowUtil.f(window);
        }, () -> {
            return producerId;
        }});
        KeyValue<Long, UniversePartitionUpdate> update = this.checkTaskStatusCommitted(window, (Boolean)null);
        if (update != null) {
            LOG.info(this.m, "{} - last commit of window", new Supplier[]{() -> {
                return WindowUtil.f(window);
            }});
            return Collections.singleton(update);
        } else {
            return null;
        }
    }

    public KeyValue<Long, UniversePartitionUpdate> checkTaskStatusCommitted(Window window, Boolean isGraceOver) {
        long windowStart = window.getStart();
        if (isGraceOver == null) {
            isGraceOver = this.commitBufferStore.fetch(GRACE_OVER_MARKER, windowStart) != null;
        }

        if (isGraceOver) {
            KeyValueIterator<Windowed<Long>, ValueAndTimestamp<Digest>> iter = this.ciphertextSumStore.fetchAll(windowStart, windowStart);
            boolean hasUncommittedValue = iter.hasNext();
            iter.close();
            if (!hasUncommittedValue) {
                return new KeyValue(this.universeId, new UniversePartitionUpdate(this.taskId, window, this.STATUS_COMMITTED, (Map)null, (Digest)null));
            }
        }

        return null;
    }

    public Iterable<KeyValue<Long, UniversePartitionUpdate>> handleCommit(long producerId, Window window) {
        long windowStart = window.getStart();
        ValueAndTimestamp<Digest> record = (ValueAndTimestamp)this.ciphertextSumStore.fetch(producerId, windowStart);
        if (record == null) {
            System.out.println("handleCommit:"+window+" neednot commit "+producerId);
            /*LOG.warn(this.m, "{} - ignored commit from producer={} (no uncommitted value in store)", new Supplier[]{() -> {
                return WindowUtil.f(window);
            }, () -> {
                return producerId;
            }});*/
            return null;
        }else {
            Digest windowedAggregate = (Digest) record.value();
            HeacHeader header = windowedAggregate.getHeader();
            if (header.getStart() == window.getStart() - 1L && header.getEnd() == window.getEnd() - 1L) {
                return this.commitWindowedAggregate(producerId, windowedAggregate, window);
            } else {
                this.commitBufferStore.put(producerId, 1, windowStart);
                LOG.debug(this.m, "{} - commit placed in buffer producer={} header={}", new Supplier[]{() -> {
                    return WindowUtil.f(window);
                }, () -> {
                    return producerId;
                }, () -> {
                    return header;
                }});
                return null;
            }
        }

    }

    private void handleDeltaMembershipStore(Long producerId, long window) {
        Integer membershipStatus = (Integer)this.memberDeltaStore.fetch(producerId, window);
        if (membershipStatus == null) {
            this.memberDeltaStore.put(producerId, this.MEMBER_STATUS_NEW, window);
        } else {
            if (membershipStatus != this.MEMBER_STATUS_DROPPED) {
                LOG.error(this.m, "{} - IllegalState in Membership Delta Store: producerId={} membershipStatus={}", window, producerId, membershipStatus);
                throw new IllegalStateException("Membership Delta Store");
            }

            this.memberDeltaStore.put(producerId, (Integer) null, window);
        }

    }
}
