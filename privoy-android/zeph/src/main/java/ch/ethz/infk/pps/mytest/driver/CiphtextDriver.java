package ch.ethz.infk.pps.mytest.driver;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ch.ethz.infk.pps.zeph.client.data.MyIdentity;
import ch.ethz.infk.pps.shared.avro.Input;
import ch.ethz.infk.pps.shared.avro.Universe;
import ch.ethz.infk.pps.shared.avro.Window;
import ch.ethz.infk.pps.zeph.client.transformation.CiphertextTransformation;
import ch.ethz.infk.pps.zeph.client.transformation.CiphertextTransformationFacade;
import ch.ethz.infk.pps.zeph.shared.WindowUtil;

public class CiphtextDriver implements Runnable{
    private final CiphertextTransformation transformation= CiphertextTransformation.getInstance();
    private boolean shutdownRequested = false;

    public CiphtextDriver() throws InterruptedException {
    }

    @Override
    public void run() {
        try {
            Window firstWindow =transformation.startsubmit(5L);
            long timestamp = System.currentTimeMillis();
            Thread.sleep(Math.max(0L, firstWindow.getStart() - timestamp));
            while(!shutdownRequested){
                Input input =new Input(10L,1L);
                long prevTimestamp = timestamp;
                timestamp = System.currentTimeMillis();
                if (timestamp <= prevTimestamp) {
                    timestamp = prevTimestamp + 1L;
                }
                transformation.submit(5L,input, timestamp);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
            transformation.closesubmit();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void requestShutdown() {
        this.shutdownRequested = true;
    }
    protected boolean isShutdownRequested() {
        return this.shutdownRequested;
    }


}
