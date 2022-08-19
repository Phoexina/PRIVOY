package ch.ethz.infk.pps.zeph;

import ch.ethz.infk.pps.zeph.client.util.LoaderUtil;
import ch.ethz.infk.pps.zeph.driver.FileDriver;
import ch.ethz.infk.pps.zeph.driver.TokenDriver;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {
    public static TokenDriver tokenDriver;
    public static void main(String[] args) {
        LoaderUtil.init("password");
        List<Long> universes=new ArrayList<>();
        universes.add(5L);
        tokenDriver=new TokenDriver(universes);
        FileDriver fileDriver=new FileDriver();
        ExecutorService executorService = Executors.newFixedThreadPool(2);

        System.out.println("drivers start");
        //CountDownLatch latch=new CountDownLatch(1);
        executorService.submit(tokenDriver);
        executorService.submit(fileDriver);



    }

}
