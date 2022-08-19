package drivers;

import netty.NettyStart;

public class NettyDriver implements Runnable{
    private boolean shutdownRequested = false;

    @Override
    public void run() {
        try {
            new NettyStart(9009);
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
