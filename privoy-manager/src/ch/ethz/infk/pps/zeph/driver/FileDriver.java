package ch.ethz.infk.pps.zeph.driver;

import ch.ethz.infk.pps.zeph.client.loader.FileUploadServer;

public class FileDriver implements Runnable{
    public static int PORT=8080;
    private boolean shutdownRequested = false;

    @Override
    public void run() {
        try {
            new FileUploadServer().bind(PORT);
        } catch (Exception e) {
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
