package ch.ethz.infk.pps.mytest;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.security.Provider;
import java.security.Security;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ch.ethz.infk.pps.zeph.client.data.CiphertextData;
import ch.ethz.infk.pps.zeph.client.data.MyIdentity;
import ch.ethz.infk.pps.mytest.driver.CiphtextDriver;
import ch.ethz.infk.pps.zeph.client.loader.FileLoader;
import ch.ethz.infk.pps.zeph.client.transformation.CiphertextTransformationFacade;
import ch.ethz.infk.pps.zeph.client.loader.util.PathUtil;
import ch.ethz.infk.pps.shared.avro.ApplicationAdapter;
import ch.ethz.infk.pps.shared.avro.Digest;
import ch.ethz.infk.pps.zeph.R;


public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //设置默认路径
        FileLoader.loadIdentity(3L,getApplicationContext().getFilesDir().getAbsolutePath());
        //setContentView(R.layout.activity_main);
        //findViewById(R.id.bt1).setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        Log.i("click","success");
        if(FileLoader.uploadKeyFile()!=0) return;
        Log.i("upload","success");
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        try {
            CiphtextDriver ciphtextDriver=new CiphtextDriver();
            executorService.submit(ciphtextDriver);
            Thread.sleep(100000L);
            Log.i("test","wake up");
            ciphtextDriver.requestShutdown();
            executorService.shutdown();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Log.i("test", String.valueOf(executorService.isShutdown()));

    }

}