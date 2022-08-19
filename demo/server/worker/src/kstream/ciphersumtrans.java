package kstream;

import ch.ethz.infk.pps.shared.avro.Digest;
import ch.ethz.infk.pps.shared.avro.HeacHeader;
import ch.ethz.infk.pps.shared.avro.UniversePartitionUpdate;
import ch.ethz.infk.pps.shared.avro.Window;
import ch.ethz.infk.pps.zeph.shared.Names;
import ch.ethz.infk.pps.zeph.shared.WindowUtil;
import ch.ethz.infk.pps.zeph.shared.pojo.WindowStatus;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import kstream.handle.commit;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.apache.kafka.streams.kstream.Transformer;
import org.apache.kafka.streams.kstream.TransformerSupplier;
import org.apache.kafka.streams.kstream.Windowed;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.TimestampedWindowStore;
import org.apache.kafka.streams.state.ValueAndTimestamp;
import org.apache.kafka.streams.state.WindowStore;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.apache.logging.log4j.util.Supplier;

public class ciphersumtrans implements Transformer<Windowed<Long>, Digest, Iterable<KeyValue<Long, UniversePartitionUpdate>>> {
    private static final Logger LOG = LogManager.getLogger();
    private Marker m;
    private final long universeId;
    private final Names n;
    private final long windowSizeMs;
    private final long expirationMs;
    private final int statusOpen;
    private final int statusStaged;
    private ProcessorContext ctx;
    private String taskId;
    private PriorityQueue<Long> openWindows;
    private PriorityQueue<Long> stagedWindows;
    private long observedStreamTime;
    private long observedWindowStart;
    private commit commitHandler;
    private WindowStore<Long, Integer> commitBufferStore;

    public ciphersumtrans(TimeWindows windows, Names n) {
        this.statusOpen = WindowStatus.OPEN.code();
        this.statusStaged = WindowStatus.STAGED.code();
        this.openWindows = new PriorityQueue();
        this.stagedWindows = new PriorityQueue();
        this.observedStreamTime = -1L;
        this.observedWindowStart = -1L;
        this.universeId = n.UNIVERSE_ID;
        this.n = n;
        this.windowSizeMs = windows.size();
        this.expirationMs = this.windowSizeMs + windows.gracePeriodMs();
    }

    public void init(ProcessorContext context) {
        this.ctx = context;
        this.taskId = this.ctx.taskId().toString();
        this.m = MarkerManager.getMarker("taskId" + this.taskId);
        this.commitHandler = new commit(this.ctx, this.n);
        this.commitBufferStore = (WindowStore)context.getStateStore(this.n.COMMIT_BUFFER_STORE);
        this.loadOpenWindows();
    }

    public Iterable<KeyValue<Long, UniversePartitionUpdate>> transform(Windowed<Long> windowedProducerId, Digest value) {

        long producerId = (Long)windowedProducerId.key();
        long timestamp = this.ctx.timestamp();
        long windowStart = windowedProducerId.window().start();
        LOG.debug(this.m, "{} - task={}  ciphertext sum transform [t={}] [observed-stream-time={}]", new Supplier[]{() -> {
            return WindowUtil.f(windowStart, this.windowSizeMs);
        }, () -> {
            return this.taskId;
        }, () -> {
            return timestamp;
        }, () -> {
            return this.observedStreamTime;
        }});
        Integer commit = (Integer)this.commitBufferStore.fetch(producerId, windowStart);
        Window window;
        if (commit != null) {
            System.out.println("commit!=null"+windowedProducerId+value);
            long windowEnd = windowedProducerId.window().end();
            HeacHeader header = value.getHeader();
            if (header.getStart() == windowStart - 1L && header.getEnd() == windowEnd - 1L) {
                this.commitBufferStore.put(producerId, (Integer) null, windowStart);
                window = new Window(windowStart, windowEnd);
                //符合要求就直接确认
                this.commitHandler.commitWindowedAggregate((Long)windowedProducerId.key(), value, window);
            } else {
                LOG.warn(this.m, "{} - incomplete aggregate value producer={} header={}", new Supplier[]{() -> {
                    return WindowUtil.f(windowStart, this.windowSizeMs);
                }, () -> {
                    return producerId;
                }, () -> {
                    return header;
                }});
            }
        }

        List<KeyValue<Long, UniversePartitionUpdate>> out = new ArrayList();
        long t;
        if (windowStart > this.observedWindowStart) {
            if (this.observedWindowStart == -1L) {
                LOG.info(this.m, "{} - task={} first window", WindowUtil.f(windowStart, this.windowSizeMs), this.taskId);
                this.observedWindowStart = windowStart - this.windowSizeMs;
            }

            LOG.info(this.m, "task={} new windows observedWindowStart={} windowStart={}", this.taskId, this.observedWindowStart, windowStart);
            System.out.println(windowedProducerId+" ciphersumtrans:"+value);
            for(t = this.observedWindowStart + this.windowSizeMs; t <= windowStart; t += this.windowSizeMs) {
                window = new Window(t, t + this.windowSizeMs);
                UniversePartitionUpdate update = new UniversePartitionUpdate(this.taskId, window, this.statusOpen, (Map)null, (Digest)null);
                out.add(new KeyValue(this.universeId, update));
                System.out.println("new window add:"+update);
                this.openWindows.add(t);
                Window finalWindow = window;
                LOG.info(this.m, "{} - task={} new window", new Supplier[]{() -> {
                    return WindowUtil.f(finalWindow);
                }, () -> {
                    return this.taskId;
                }});
            }

            this.observedWindowStart = windowStart;
        }

        this.observedStreamTime = Math.max(this.observedStreamTime, timestamp);
        t = this.observedStreamTime - this.windowSizeMs;

        long stagedExpiryTime;
        while(this.openWindows.peek() != null && (Long)this.openWindows.peek() <= t) {
            stagedExpiryTime = (Long)this.openWindows.poll();
            this.stagedWindows.add(stagedExpiryTime);
            window = new Window(stagedExpiryTime, stagedExpiryTime + this.windowSizeMs);
            UniversePartitionUpdate update = new UniversePartitionUpdate(this.taskId, window, this.statusStaged, (Map)null, (Digest)null);
            out.add(new KeyValue(this.universeId, update));
            System.out.println("send staged add:"+update);
            LOG.info(this.m, "{} - send staged", WindowUtil.f(window));
        }

        stagedExpiryTime = this.observedStreamTime - this.expirationMs;

        while(this.stagedWindows.peek() != null && (Long)this.stagedWindows.peek() <= stagedExpiryTime) {
            long stagedWindowStart = (Long)this.stagedWindows.poll();
            Window stagedWindow = new Window(stagedWindowStart, stagedWindowStart + this.windowSizeMs);
            LOG.info(this.m, "{} - mark as grace over", new Supplier[]{() -> {
                return WindowUtil.f(stagedWindow);
            }});
            KeyValue<Long, UniversePartitionUpdate> kv = this.commitHandler.checkTaskStatusCommitted(stagedWindow, true);
            if (kv != null) {
                out.add(kv);
                System.out.println(stagedWindowStart+"out add:"+kv.key+kv.value);
            } else {
                this.commitBufferStore.put(commitHandler.GRACE_OVER_MARKER, 0, stagedWindowStart);
                System.out.println(stagedWindowStart+"commitBufferStore put");
            }
        }

        if (out.isEmpty()) {
            out = null;
            //System.out.println(windowedProducerId+" ciphersumtrans out null");
        }

        return out;
    }

    private void loadOpenWindows() {
        WindowStore<Long, ValueAndTimestamp<Digest>> store = (TimestampedWindowStore)this.ctx.getStateStore(this.n.CIPHERTEXT_SUM_STORE);
        Set<Long> windows = new HashSet();
        KeyValueIterator<Windowed<Long>, ValueAndTimestamp<Digest>> iter = store.all();

        while(iter.hasNext()) {
            long windowStart = ((Windowed)((KeyValue)iter.next()).key).window().start();
            windows.add(windowStart);
        }

        iter.close();
        this.openWindows.addAll(windows);
    }

    public void close() {
        this.openWindows.clear();
    }

    public static class ciphersumtransSupplier implements TransformerSupplier<Windowed<Long>, Digest, Iterable<KeyValue<Long, UniversePartitionUpdate>>> {
        private TimeWindows timeWindows;
        private Names names;

        public ciphersumtransSupplier(TimeWindows timeWindows, Names names) {
            this.timeWindows = timeWindows;
            this.names = names;
        }

        public Transformer<Windowed<Long>, Digest, Iterable<KeyValue<Long, UniversePartitionUpdate>>> get() {
            return new ciphersumtrans(this.timeWindows, this.names);
        }
    }
}
