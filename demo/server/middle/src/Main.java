import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import ch.ethz.infk.pps.shared.avro.Digest;
import ch.ethz.infk.pps.shared.avro.Universe;
import ch.ethz.infk.pps.shared.avro.Window;
import ch.ethz.infk.pps.zeph.shared.WindowUtil;
import data.CiphertextData;
import drivers.CiphertextDriver;
import drivers.NettyDriver;

public class Main {
    public static void main(String[] args) {
        NettyDriver nettyDriver=new NettyDriver();
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        executorService.submit(nettyDriver);

//        long windowSizeMillis=10000;
//        long earliestPossibleStart = System.currentTimeMillis();
//        long start = WindowUtil.getWindowStart(earliestPossibleStart, windowSizeMillis);
//        Window firstWindow = new Window(start + windowSizeMillis, start + 2L * windowSizeMillis);
//
//        List<Long> ciphertext= new ArrayList<>();
//        ciphertext.add(3L);
//        ciphertext.add(2L);
//        ciphertext.add(1L);
//        CiphertextData data=new CiphertextData();
//        data.setData(ciphertext);
//        data.setPrevTimestamp(firstWindow.getStart()-1L);
//        data.setProduceId(1L);
//        data.setUniverseId(5L);
//
//        CiphertextDriver driver=CiphertextDriver.getCiphertextDriver();
//        try {
//            Thread.sleep(Math.max(0L, firstWindow.getStart() - earliestPossibleStart));
//        } catch (InterruptedException var17) {
//            System.out.println("Producer interrupted.");
//        }
//
//        while(true) {
//            long timestamp = System.currentTimeMillis();
//            data.setTimestamp(timestamp);
//            driver.send(data);
//            try{
//                Thread.sleep(100);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            data.setPrevTimestamp(timestamp);
//        }




    }

}
