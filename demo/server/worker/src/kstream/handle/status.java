package kstream.handle;
import ch.ethz.infk.pps.shared.avro.Digest;
import ch.ethz.infk.pps.shared.avro.UniversePartitionUpdate;
import ch.ethz.infk.pps.shared.avro.Window;
import ch.ethz.infk.pps.zeph.server.worker.WorkerApp;
import ch.ethz.infk.pps.zeph.shared.Names;
import ch.ethz.infk.pps.zeph.shared.WindowUtil;
import ch.ethz.infk.pps.zeph.shared.pojo.MemberDiffType;
import ch.ethz.infk.pps.zeph.shared.pojo.WindowStatus;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.Windowed;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.ValueAndTimestamp;
import org.apache.kafka.streams.state.WindowStore;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.apache.logging.log4j.util.Supplier;

public class status {
    private static final Logger LOG = LogManager.getLogger();
    private Marker m;
    private final long MEMBERSHIP_STORE_WINDOW_KEY = -1L;
    private final String NO_RESULT_MARKER = "noresult";
    private final int MEMBER_STATUS_NEW;
    private final int MEMBER_STATUS_DROPPED;
    private final long universeId;
    private final int statusMerged;
    private ProcessorContext ctx;
    private String taskId;
    private WindowStore<Long, ValueAndTimestamp<Digest>> ciphertextSumStore;
    private KeyValueStore<Long, Digest> committingSumStore;
    private KeyValueStore<Long, Digest> transformingSumStore;
    private WindowStore<Long, Integer> commitBufferStore;
    private WindowStore<Long, Integer> membershipDeltaStore;
    private KeyValueStore<Long, Long> membershipStore;

    public status(ProcessorContext ctx, Names n) {
        this.MEMBER_STATUS_NEW = MemberDiffType.ADD.code();
        this.MEMBER_STATUS_DROPPED = MemberDiffType.DEL.code();
        this.statusMerged = WindowStatus.MERGED.code();
        this.ctx = ctx;
        this.universeId = n.UNIVERSE_ID;
        this.taskId = ctx.taskId().toString();
        this.ciphertextSumStore = (WindowStore)this.ctx.getStateStore(n.CIPHERTEXT_SUM_STORE);
        this.committingSumStore = (KeyValueStore)this.ctx.getStateStore(n.COMMITTING_SUM_STORE);
        this.transformingSumStore = (KeyValueStore)this.ctx.getStateStore(n.TRANSFORMING_SUM_STORE);
        this.commitBufferStore = (WindowStore)this.ctx.getStateStore(n.COMMIT_BUFFER_STORE);
        this.membershipDeltaStore = (WindowStore)this.ctx.getStateStore(n.MEMBER_DELTA_STORE);
        this.membershipStore = (KeyValueStore)this.ctx.getStateStore(n.MEMBER_STORE);
        this.m = MarkerManager.getMarker("taskId" + this.taskId);
        this.m.addParents(new Marker[]{WorkerApp.GLOBAL_MARKER});
    }

    public Iterable<KeyValue<Long, UniversePartitionUpdate>> handleWindowCommitted(Window window) {
        LOG.info(this.m, "{} - handle window committed", new Supplier[]{() -> {
            return WindowUtil.f(window);
        }});
        long windowStart = window.getStart();
        Boolean taskSendsResult = null;
        Digest committedSum = (Digest)this.committingSumStore.delete(windowStart);
        if (committedSum != null) {
            this.transformingSumStore.put(windowStart, committedSum);
            taskSendsResult = true;
        } else {
            LOG.warn(this.m, "{} - no producer committed", new Supplier[]{() -> {
                return WindowUtil.f(window);
            }});
            taskSendsResult = false;
        }

        this.handleUncommittedProducers(window);
        this.handleIncompleteValues(window);
        Map<String, Integer> memberDiffs = this.handleMembershipDiff(window);
        if (!taskSendsResult) {
            memberDiffs.put("noresult", 0);
        }

        UniversePartitionUpdate update = new UniversePartitionUpdate(this.taskId, window, this.statusMerged, memberDiffs, (Digest)null);
        return Collections.singleton(new KeyValue(this.universeId, update));
    }

    private void handleUncommittedProducers(Window window) {
        long windowStart = window.getStart();
        KeyValueIterator<Windowed<Long>, ValueAndTimestamp<Digest>> iter = this.ciphertextSumStore.fetchAll(windowStart, windowStart);
        Set<Long> uncommittedProducers = new HashSet();
        iter.forEachRemaining((kv) -> {
            uncommittedProducers.add((Long)((Windowed)kv.key).key());
        });
        iter.close();
        String wStr = WindowUtil.f(window);
        uncommittedProducers.forEach((pId) -> {
            LOG.debug(this.m, "{} - producer {} did not commit (-> is ignored)", wStr, pId);
            this.ciphertextSumStore.put(pId, (ValueAndTimestamp<Digest>) null, windowStart);
        });
    }

    private void handleIncompleteValues(Window window) {
        long windowStart = window.getStart();
        this.commitBufferStore.put(commit.GRACE_OVER_MARKER, (Integer) null, windowStart);
        KeyValueIterator<Windowed<Long>, Integer> iter = this.commitBufferStore.fetchAll(windowStart, windowStart);
        Set<Long> producerIds = new HashSet();
        iter.forEachRemaining((kv) -> {
            producerIds.add((Long)((Windowed)kv.key).key());
        });
        iter.close();
        String wStr = WindowUtil.f(window);
        producerIds.forEach((pId) -> {
            LOG.debug(this.m, "{} - producer {} did have complete value (-> commit is ignored)", wStr, pId);
            this.commitBufferStore.put(pId, (Integer) null, windowStart);
        });
    }

    private Map<String, Integer> handleMembershipDiff(Window window) {
        long windowStart = window.getStart();
        Map<String, Integer> diff = new HashMap();
        KeyValueIterator<Windowed<Long>, Integer> iter = this.membershipDeltaStore.fetchAll(windowStart, windowStart);
        iter.forEachRemaining((kv) -> {
            Long producerId = (Long)((Windowed)kv.key).key();
            Integer status = (Integer)kv.value;
            Long oldValue;
            if (status == this.MEMBER_STATUS_DROPPED) {
                oldValue = (Long)this.membershipStore.delete(producerId);
                if (oldValue == null) {
                    LOG.debug(this.m, "{}membership error: -",window);
                    System.out.println("membership error: -");
                    //throw new IllegalStateException("membership error: -");
                }
            } else {
                if (status != this.MEMBER_STATUS_NEW) {
                    throw new IllegalStateException("membership error: unknown member status");
                }

                oldValue = (Long)this.membershipStore.putIfAbsent(producerId, windowStart);
                System.out.println(" handleMembershipDiff:"+producerId+window+" "+oldValue);
                if (oldValue != null) {
                    LOG.debug(this.m, "{}membership error: +",window);
                    System.out.println("membership error: +");
                    //throw new IllegalStateException("membership error: +");
                }
            }

            diff.put("" + producerId, status);
        });
        iter.close();
        Long prevWindowStart = (Long)this.membershipStore.get(-1L);
        this.membershipStore.put(-1L, windowStart);
        LOG.trace(this.m, "{} - update membership store window (prev start={})", new Supplier[]{() -> {
            return WindowUtil.f(window);
        }, () -> {
            return prevWindowStart;
        }});
        if (prevWindowStart != null && windowStart <= prevWindowStart) {
            //throw new IllegalStateException("membership error: window prev=" + prevWindowStart + "    windowStart=" + windowStart);
            System.out.println("membership error: window prev=" + prevWindowStart + "    windowStart=" + windowStart);
            LOG.debug(this.m, "{} - membership error", new Supplier[]{() -> {
                return WindowUtil.f(new Window(prevWindowStart,windowStart));
            }});
            return diff;
        } else {
            long nextWindowStart = window.getEnd();
            KeyValueIterator<Long, Long> mIter = this.membershipStore.all();
            mIter.forEachRemaining((kv) -> {
                if ((Long)kv.key != -1L) {
                    long producerId = (Long)kv.key;
                    Integer status = (Integer)this.membershipDeltaStore.fetch(producerId, nextWindowStart);
                    if (status == null) {
                        this.membershipDeltaStore.put(producerId, this.MEMBER_STATUS_DROPPED, nextWindowStart);
                    } else {
                        if (status != this.MEMBER_STATUS_NEW) {
                            LOG.error(this.m, "{} - Illegal MembershipStore State", new Supplier[]{() -> {
                                return WindowUtil.f(window);
                            }});
                            throw new IllegalStateException("membership error: next window");
                        }

                        this.membershipDeltaStore.put(producerId, (Integer) null, nextWindowStart);
                    }
                }

            });
            mIter.close();
            return diff;
        }
    }
}
