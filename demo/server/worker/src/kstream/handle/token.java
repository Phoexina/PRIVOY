package kstream.handle;


import ch.ethz.infk.pps.shared.avro.Digest;
import ch.ethz.infk.pps.shared.avro.UniversePartitionUpdate;
import ch.ethz.infk.pps.shared.avro.Window;
import ch.ethz.infk.pps.zeph.server.worker.WorkerApp;
import ch.ethz.infk.pps.zeph.shared.DigestOp;
import ch.ethz.infk.pps.zeph.shared.Names;
import ch.ethz.infk.pps.zeph.shared.WindowUtil;
import ch.ethz.infk.pps.zeph.shared.pojo.WindowStatus;
import java.util.Collections;
import java.util.Map;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.Windowed;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.WindowStore;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.apache.logging.log4j.util.Supplier;

public class token{
    private static final Logger LOG = LogManager.getLogger();
    private Marker m;
    private String taskId;
    private final long universeId;
    private final int statusClosed;
    private WindowStore<Long, Integer> expectedTransformationTokenStore;
    private KeyValueStore<Long, Digest> transformingSumStore;

    public token(ProcessorContext ctx, Names n) {
        this.statusClosed = WindowStatus.CLOSED.code();
        this.universeId = n.UNIVERSE_ID;
        this.taskId = ctx.taskId().toString();
        this.expectedTransformationTokenStore = (WindowStore)ctx.getStateStore(n.EXPECTED_TRANSFORMATION_TOKEN_STORE);
        this.transformingSumStore = (KeyValueStore)ctx.getStateStore(n.TRANSFORMING_SUM_STORE);
        this.m = MarkerManager.getMarker("taskId" + this.taskId);
        this.m.addParents(new Marker[]{WorkerApp.GLOBAL_MARKER});
    }

    public Iterable<KeyValue<Long, UniversePartitionUpdate>> handleTransformationToken(long producerId, Window window, Digest transformationToken) {
        long windowStart = window.getStart();
        Integer record = (Integer)this.expectedTransformationTokenStore.fetch(producerId, windowStart);
        Digest aggregator = (Digest)this.transformingSumStore.get(windowStart);
        if (record != null && aggregator != null) {
            this.expectedTransformationTokenStore.put(producerId, (Integer) null, windowStart);
            aggregator = DigestOp.add(aggregator, transformationToken);
            LOG.debug(this.m, "{} - transformation token from producer={} (token = {})", new Supplier[]{() -> {
                return WindowUtil.f(window);
            }, () -> {
                return producerId;
            }, () -> {
                return transformationToken;
            }});
            KeyValueIterator<Windowed<Long>, Integer> iter = this.expectedTransformationTokenStore.fetchAll(windowStart, windowStart);
            boolean isComplete = !iter.hasNext();
            iter.close();
            if (isComplete) {
                this.transformingSumStore.delete(windowStart);
                LOG.info(this.m, "{} - closed", WindowUtil.f(window));
                LOG.debug(this.m, "{} - local task result = {}", WindowUtil.f(window), aggregator);
                return Collections.singleton(new KeyValue(this.universeId, new UniversePartitionUpdate(this.taskId, window, this.statusClosed, (Map)null, aggregator)));
            } else {
                this.transformingSumStore.put(windowStart, aggregator);
                return null;
            }
        } else {
            LOG.warn(this.m, "{} - ignored transformation token: producerId={} token={} (expected={} aggregator={})", WindowUtil.f(window), producerId, transformationToken, record, aggregator);
            return null;
        }
    }
}
