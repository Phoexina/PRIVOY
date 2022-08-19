package kstream;
import kstream.handle.commit;
import kstream.handle.status;
import kstream.handle.token;

import ch.ethz.infk.pps.shared.avro.Digest;
import ch.ethz.infk.pps.shared.avro.HeacHeader;
import ch.ethz.infk.pps.shared.avro.Token;
import ch.ethz.infk.pps.shared.avro.UniversePartitionUpdate;
import ch.ethz.infk.pps.shared.avro.Window;
import ch.ethz.infk.pps.zeph.shared.Names;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.Transformer;
import org.apache.kafka.streams.kstream.TransformerSupplier;
import org.apache.kafka.streams.processor.ProcessorContext;

public class tokentrans implements Transformer<Long, Token, Iterable<KeyValue<Long, UniversePartitionUpdate>>> {
    private ProcessorContext ctx;
    private Names n;
    private commit commitHandler;
    private status statusHandler;
    private token transformationHandler;

    public tokentrans(Names n) {
        this.n = n;
    }

    public void init(ProcessorContext context) {
        this.ctx = context;
        this.commitHandler = new commit(this.ctx, this.n);
        this.statusHandler = new status(this.ctx, this.n);
        this.transformationHandler = new token(this.ctx, this.n);
    }

    public Iterable<KeyValue<Long, UniversePartitionUpdate>> transform(Long producerId, Token token) {
        //System.out.println(producerId+" token in:"+token);
        Window window = token.getWindow();
        int count = 0;
        HeacHeader commitInfo = token.getCommit();
        count += commitInfo == null ? 0 : 1;
        Integer status = token.getStatus();
        count += status == null ? 0 : 1;
        Digest transformationToken = token.getTransformation();
        count += transformationToken == null ? 0 : 1;
        if (count != 1) {
            System.out.println(producerId+" token out null");
            return null;
        } else if (commitInfo != null) {
            System.out.println(producerId+" token:"+window);
            return this.commitHandler.handleCommit(producerId, window);
        } else {
            System.out.println(producerId+" token:"+window+transformationToken);
            return transformationToken != null ? this.transformationHandler.handleTransformationToken(producerId, window, transformationToken) : this.statusHandler.handleWindowCommitted(window);
        }
    }

    public void close() {
    }

    public static class tokentransSupplier implements TransformerSupplier<Long, Token, Iterable<KeyValue<Long, UniversePartitionUpdate>>> {
        private Names names;

        public tokentransSupplier(Names names) {
            this.names = names;
        }

        public Transformer<Long, Token, Iterable<KeyValue<Long, UniversePartitionUpdate>>> get() {
            return new tokentrans(this.names);
        }
    }
}
