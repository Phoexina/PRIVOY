package kstream;

import ch.ethz.infk.pps.shared.avro.Window;
import ch.ethz.infk.pps.zeph.server.master.processor.StatusChangeHandler;
import ch.ethz.infk.pps.zeph.shared.WindowUtil;
import java.util.Map;
import org.apache.kafka.streams.processor.Cancellable;
import org.apache.kafka.streams.processor.Punctuator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.util.Supplier;

public class committingphase implements Punctuator {
    private static final Logger LOG = LogManager.getLogger();
    private Marker m;
    private final Map<Window, Cancellable> cancellableMap;
    private final statushandler handler;
    private final Window window;
    private final long universeId;

    public committingphase(statushandler handler, Map<Window, Cancellable> cancellableMap, Window window, long universeId, Marker marker) {
        this.handler = handler;
        this.cancellableMap = cancellableMap;
        this.window = new Window(window.getStart(), window.getEnd());
        this.universeId = universeId;
        this.m = marker;
    }

    public void punctuate(long timestamp) {
        LOG.debug(this.m, "{} - universe={} punctuate", new Supplier[]{() -> {
            return WindowUtil.f(this.window);
        }, () -> {
            return this.universeId;
        }});
        Cancellable c = (Cancellable)this.cancellableMap.remove(this.window);
        if (c != null) {
            c.cancel();
            this.handler.handleUniverseStatusCommitted(this.universeId, this.window);
        } else {
            LOG.warn(this.m, "{} - universe={} cancellable for window not found (cancellables={})", WindowUtil.f(this.window), this.universeId, this.cancellableMap);
        }

    }
}
