package com.example.my_lbs;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
//import org.apache.*;
//import org.apaches.commons.codec.binary.Hex;
import org.apaches.commons.codec.digest.DigestUtils;

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

//添加

public class MainActivity extends AppCompatActivity {

    LocationClient mLocationClient;  //定位客户端
    MapView mapView;  //Android Widget地图控件
    BaiduMap baiduMap;
    boolean isFirstLocate = true;
    int time=1;
    TextView tv_Lat;  //纬度
    TextView tv_Lon;  //经度
    TextView tv_Add;  //地址
    TextView tv_dis;
    TextView tv_hash;
    BDLocation start;
    BDLocation end;
    String hash;



    double sum=0;
    double sum1=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        File keyFile = PathUtil.getKeysFile();
        hash = DigestUtils.md5Hex(keyFile.toString());
        initLocation();
        mLocationClient.start();
    }




    private void initLocation() {  //初始化
        //init();
        MyLocationListener myLocationListener = new MyLocationListener();
        mLocationClient = new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener(myLocationListener);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);

        mapView = findViewById(R.id.bmapView);
        baiduMap = mapView.getMap();
        baiduMap.setMyLocationEnabled(true);//开启地图的定位图层
        tv_Lat = findViewById(R.id.tv_Lat);
        tv_Lon = findViewById(R.id.tv_Lon);
        tv_Add = findViewById(R.id.tv_Add);
        tv_dis=findViewById(R.id.tv_dis);
        tv_hash=findViewById(R.id.tv_hash);
        //System.out.println();
        LocationClientOption option = new LocationClientOption();
        //设置扫描时间间隔
        option.setScanSpan(1000);
        //设置定位模式，三选一
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        //设置需要地址信息
        option.setIsNeedAddress(true);
        //保存定位参数
        mLocationClient.setLocOption(option);




    }

    //内部类，百度位置监听器
    private class MyLocationListener extends BDAbstractLocationListener {
        @Override
        public void onReceiveLocation(BDLocation bdLocation) {

            sum=0;

            if(time==1) {
                  start = bdLocation;
            }

            end=bdLocation;


            double lat1 = (Math.PI/180)*start.getLatitude();

            double lat2 = (Math.PI/180)*end.getLatitude();

            double lon1 = (Math.PI/180)*start.getLongitude();

            double lon2 = (Math.PI/180)*end.getLongitude();


            double R = 6378.137;
            double d = Math.acos(Math.sin(lat1)*Math.sin(lat2)+Math.cos(lat1)*Math.cos(lat2)*Math.cos(lon2-lon1))*R;
            sum+= (Double)(d*1000000); //mm
            sum1+=(Double)(d*1000);
            start=end;


            //Log.i("infor", "移动距离（米）" +sum);
            tv_Lat.setText(bdLocation.getLatitude() + "");
            tv_Lon.setText(bdLocation.getLongitude() + "");
            tv_Add.setText(bdLocation.getAddrStr());
            tv_dis.setText(sum+"");
            tv_hash.setText(hash);
            Log.i("infor","经度等于" + tv_Lat.getText());
            Log.i("infor","纬度等于" + tv_Lon.getText());


            if (bdLocation.getLocType() == BDLocation.TypeGpsLocation || bdLocation.getLocType() == BDLocation.TypeNetWorkLocation) {
                navigateTo(bdLocation);
            }

           Input input = new Input((long) sum,  1L);
            CiphertextTransformation.getInstance().submit(5L,input,System.currentTimeMillis());

        }
    }






        private void navigateTo(BDLocation bdLocation) {
            if (isFirstLocate) {
                LatLng ll = new LatLng(bdLocation.getLatitude(), bdLocation.getLongitude());
                MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(ll);
                MyLocationData locationData = new MyLocationData.Builder().latitude(ll.latitude).longitude(ll.longitude).build();
                baiduMap.animateMapStatus(update);
                baiduMap.setMyLocationData(locationData);
                isFirstLocate = false;
            }
        }

        @Override
        protected void onResume() {
            super.onResume();
            mapView.onResume();
        }

        @Override
        protected void onPause() {
            super.onPause();
            mapView.onResume();
        }

        @Override
        protected void onDestroy() {
            super.onDestroy();
            mLocationClient.stop();
            mapView.onDestroy();
        }
    }
