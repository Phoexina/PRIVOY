package com.example.my_lbs;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
//import android.widget.Toast;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

import ch.ethz.infk.pps.shared.avro.Input;
import ch.ethz.infk.pps.shared.avro.Universe;
import ch.ethz.infk.pps.shared.avro.Window;
import ch.ethz.infk.pps.zeph.client.data.MyIdentity;
import ch.ethz.infk.pps.zeph.client.loader.FileLoader;
import ch.ethz.infk.pps.zeph.client.loader.FileUpload;
import ch.ethz.infk.pps.zeph.client.loader.util.PathUtil;
import ch.ethz.infk.pps.zeph.client.transformation.CiphertextTransformation;
import ch.ethz.infk.pps.zeph.client.transformation.CiphertextTransformationFacade;
import ch.ethz.infk.pps.zeph.shared.WindowUtil;


public class first extends AppCompatActivity {
    Button button1;
    TextView tv_res;

    public void init() {
        // Log.i("click","end1");
        Window firstWindow;
        Universe universe;
        CiphertextTransformation transformation = CiphertextTransformation.getInstance();
        long windowSizeMillis = 10000L;
        long earliestPossibleStart = System.currentTimeMillis();
        long start = WindowUtil.getWindowStart(earliestPossibleStart, windowSizeMillis);
        firstWindow = new Window(start + windowSizeMillis, start + 2L * windowSizeMillis);
        universe = new Universe(5L, firstWindow, MyIdentity.getInstance().getMembers(), 8, 0.5D, 1.0E-5D);
        transformation.addUniverse(universe);
        long timestamp = System.currentTimeMillis();
        Log.i("init", "success");
        try {
            Thread.sleep(Math.max(0L, universe.getFirstWindow().getStart() - timestamp));
        } catch (InterruptedException var17) {
            var17.printStackTrace();
        }
    }

    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

        setContentView(R.layout.first);
        button1=(Button)this.findViewById(R.id.button1);
        tv_res=findViewById(R.id.tv_res);
        tv_res.setText("已申请权限");
        int flag=FileLoader.loadIdentity(System.currentTimeMillis(),getApplicationContext().getFilesDir().getAbsolutePath());
        if(flag==0) tv_res.setText("已生成主秘密，用户ID"+MyIdentity.getInstance().getId());
        else tv_res.setText("已存在主秘密，用户ID"+MyIdentity.getInstance().getId());

        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent1= new Intent();

                int res=FileLoader.uploadKeyFile();
                if(res==0) {
                    try {
                        Window firstWindow=CiphertextTransformation.getInstance().startsubmit(5L);
                        tv_res.setText("密文发送初始化中请等待，用户ID"+MyIdentity.getInstance().getId());
                        Thread.sleep(Math.max(0L, firstWindow.getStart() - System.currentTimeMillis()));

                        intent1.setClass(first.this, MainActivity.class);
                        startActivity(intent1);

                    } catch (InterruptedException e) {
                        tv_res.setText("密文初始化失败");
                        e.printStackTrace();
                    }
                }
                else if(res==-1){
                    tv_res.setText("文件不存在");
                }
                else{
                    tv_res.setText("连接失败");
                }

            }
        });
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "没有定位权限！", Toast.LENGTH_LONG).show();
                    finish();
                }
        }
    }



}
