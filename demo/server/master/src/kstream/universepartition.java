package kstream;

import ch.ethz.infk.pps.shared.avro.UniversePartitionUpdate;
import ch.ethz.infk.pps.shared.avro.Window;
import ch.ethz.infk.pps.zeph.server.master.processor.StatusChangeHandler;
import ch.ethz.infk.pps.zeph.shared.pojo.WindowStatus;
import java.time.Duration;
import org.apache.kafka.streams.processor.Processor;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.processor.ProcessorSupplier;

public class universepartition implements Processor<Long, UniversePartitionUpdate> {
    private final Duration timeForCommit;
    private statushandler statusHandler;

    public universepartition(Duration timeForCommit) {
        this.timeForCommit = timeForCommit;
    }

    public void init(ProcessorContext context) {
        //与分区有关，分区多少就init多少
        this.statusHandler = new statushandler(context, this.timeForCommit);
    }

    public void process(Long universeId, UniversePartitionUpdate update) {
        System.out.println(universeId+" "+update);
        WindowStatus status = WindowStatus.of(update.getStatus());
        Window window = update.getWindow();
        String taskId = update.getTaskId();
        switch (status) {
            case OPEN:
                this.statusHandler.handleStatusOpen(universeId, taskId, window);
                break;
            case STAGED:
                this.statusHandler.handleStatusStaged(universeId, taskId, window);
                break;
            case COMMITTED:
                this.statusHandler.handleStatusCommitted(universeId, taskId, window);
                break;
            case MERGED:
                this.statusHandler.handleStatusMerged(universeId, taskId, window, update.getMemberDiff());
                break;
            case CLOSED:
                this.statusHandler.handleStatusClosed(universeId, taskId, window, update.getResult());
                break;
            default:
                throw new IllegalStateException("not possible");
        }

    }

    public void close() {
    }

    public static class universepartitionSupplier implements ProcessorSupplier<Long, UniversePartitionUpdate> {
        private Duration timeForCommit;

        public universepartitionSupplier(Duration timeForCommit) {
            this.timeForCommit = timeForCommit;
        }

        public Processor<Long, UniversePartitionUpdate> get() {
            return new universepartition(this.timeForCommit);
        }
    }
}
