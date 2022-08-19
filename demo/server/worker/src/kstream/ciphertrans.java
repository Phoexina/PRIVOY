package kstream;


import ch.ethz.infk.pps.shared.avro.Digest;
import ch.ethz.infk.pps.shared.avro.HeacHeader;
import com.google.common.primitives.Longs;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.streams.kstream.ValueTransformerWithKey;
import org.apache.kafka.streams.kstream.ValueTransformerWithKeySupplier;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

public class ciphertrans implements ValueTransformerWithKey<Long, Digest, Digest> {
    private static final Logger LOG = LogManager.getLogger();
    private Marker m;
    private ProcessorContext ctx;

    public ciphertrans() {
    }

    public void init(ProcessorContext context) {
        this.ctx = context;
        this.m = MarkerManager.getMarker("taskId" + context.taskId().toString());
    }

    public Digest transform(Long producerId, Digest value) {
        //System.out.println(producerId+" ciphertrans in:"+value);
        if (value == null) {
            return null;
        } else {
            long timestamp = this.ctx.timestamp();
            LOG.trace(this.m, "T={} P={}  - Record={}", timestamp, producerId, value);
            Header header = this.ctx.headers().lastHeader("heac");
            byte[] bytes = header.value();
            long prevTimestamp = Longs.fromByteArray(bytes);
            value.setHeader(new HeacHeader(prevTimestamp, timestamp));
            //System.out.println(producerId+" ciphertrans out:"+value);
            return value;
        }
    }

    public void close() {
    }

    public static class valuetransSupplier implements ValueTransformerWithKeySupplier<Long, Digest, Digest> {
        public valuetransSupplier() {
        }

        public ValueTransformerWithKey<Long, Digest, Digest> get() {
            return new ciphertrans();
        }
    }
}


