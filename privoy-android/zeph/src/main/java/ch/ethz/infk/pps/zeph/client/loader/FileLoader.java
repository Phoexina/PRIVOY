package ch.ethz.infk.pps.zeph.client.loader;

import android.util.Log;

import java.io.File;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.crypto.SecretKey;

import ch.ethz.infk.pps.zeph.client.data.MyIdentity;
import ch.ethz.infk.pps.shared.avro.util.CertificateFileStore;
import ch.ethz.infk.pps.shared.avro.util.ProducerKeyFileStore;
import ch.ethz.infk.pps.shared.avro.util.SharedKeyFileStore;
import ch.ethz.infk.pps.zeph.client.loader.util.PathUtil;
import ch.ethz.infk.pps.zeph.crypto.DiffieHellmanKeyExchange;
import ch.ethz.infk.pps.zeph.shared.pojo.ProducerIdentity;

public class FileLoader {

    //ToDo:安卓不支持对称秘钥,目前存储producer身份的文件未使用KeyStore保存密钥
    public static int loadIdentity(Long producerId,String basePath){
        PathUtil.BASE=basePath;
        MyIdentity myIdentity=MyIdentity.getInstance();
        myIdentity.setId(producerId);
        Map<Long, ProducerIdentity> producers = new ConcurrentHashMap();
        File keyFile= PathUtil.getKeysFile();
        if (keyFile.exists()) {
            Log.i("LoaderUtil.loadIdentity","producerFile exist");
            producers.putAll(ProducerKeyFileStore.load(keyFile));
            if(producers.size()==1){
                producers.forEach((pId, pInfo) -> {
                    Log.i("LoaderUtil.loadIdentity.setproducer", pId + "");
                    MyIdentity.getInstance().setId(pId);
                    MyIdentity.getInstance().setIdentity(pInfo);
                });
                return 1;
            }
        }
        producers = new ConcurrentHashMap();
        Log.i("LoaderUtil.loadIdentity","producerFile have not producer"+producerId);
        ProducerIdentity identity = ProducerIdentity.generate(producerId);
        producers.put(producerId,identity);
        ProducerKeyFileStore.store(keyFile, producers);
        Log.i("LoaderUtil.loadIdentity","producerFile add producer"+producerId+" success");
        MyIdentity.getInstance().setIdentity(producers.get(producerId));
        Log.i("LoaderUtil.loadIdentity","ProducerIdentity load success");
        return 0;
    }

    public static int uploadKeyFile(){
        File keyFile= PathUtil.getKeysFile();
        if(!keyFile.exists()) return -1;
        try {
            FileUpload fileUpload=new FileUpload(keyFile);
            return 0;
        } catch (Exception e) {
            Log.e("LoaderUtil.uploadKeyFile","upload error"+keyFile.getPath());
            e.printStackTrace();
            return 1;
        }

    }

    public static void flushSharedKeys(){
        if(!checkIdentity()) return;
        //ToDo:此处需要存在最新的Certificate文件,应接收最新Certificate
        File certFile = PathUtil.getCertificatesFile();
        ConcurrentHashMap<Long, Certificate> certificates = new ConcurrentHashMap();
        certificates.putAll(CertificateFileStore.load(certFile));
        Log.i("LoaderUtil.flushSharedKeys","Load lastest certificates "+certificates.size());

        Map<Long, SecretKey> sharedKeys=MyIdentity.getInstance().getSaveSharedKeys();
        PrivateKey privateKey= MyIdentity.getInstance().getIdentity().getPrivateKey();
        DiffieHellmanKeyExchange keyExchange = new DiffieHellmanKeyExchange();
        certificates.forEach((otherProducerId, certificate) -> {
            if (!sharedKeys.containsKey(otherProducerId)) {
                SecretKey sharedKey = keyExchange.generateSharedKey(privateKey, certificate);
                sharedKeys.put(otherProducerId, sharedKey);
                //Log.i("LoaderUtil.flushSharedKeys","Generate Shared Key from "+otherProducerId+" in "+sharedKeys.size());
            }
        });
        MyIdentity.getInstance().setSaveSharedKeys(sharedKeys);
        MyIdentity.getInstance().flushSharedKeys();
        Log.i("LoaderUtil.flushSharedKeys","Flush SharedKey Map<ImmutableUnorderedPair<Long>, SecretKey>");

        Long pId=MyIdentity.getInstance().getId();
        ProducerIdentity pInfo=MyIdentity.getInstance().getIdentity();
        File sharedKeysFile =PathUtil.getSharedKeysFile(pId);
        SecretKey sharedKeysFileKey=pInfo.getSharedKeysFileKey();
        SharedKeyFileStore.store(sharedKeysFile, "sk" + pId, sharedKeysFileKey, sharedKeys);
        Log.i("LoaderUtil.flushSharedKeys","Save SharedKey "+sharedKeys.size());
    }

    public static void loadSharedKeys(){
        if(!checkIdentity()) return;
        Long pId=MyIdentity.getInstance().getId();
        ProducerIdentity pInfo=MyIdentity.getInstance().getIdentity();
        SecretKey sharedKeysFileKey=pInfo.getSharedKeysFileKey();
        File sharedKeysFile =PathUtil.getSharedKeysFile(pId);
        if (sharedKeysFile.exists()) {
            Map<Long, SecretKey> existingSharedKeys = SharedKeyFileStore.load(sharedKeysFile, "sk" + pId, sharedKeysFileKey);
            MyIdentity.getInstance().setSaveSharedKeys(existingSharedKeys);
            Log.i("LoaderUtil.loadSharedKeys","Load Shared Keys: "+existingSharedKeys.size());
        }else{
            Log.i("LoaderUtil.loadSharedKeys","No Shared Keys File");
        }
        flushSharedKeys();
    }

    public static boolean checkIdentity(){
        if(MyIdentity.getInstance().getId()==-1L){
            Log.e("LoaderUtil.loadIdentity","myIdentity need id");
            return false;
        }
        if(MyIdentity.getInstance().getIdentity()==null){
            Log.e("LoaderUtil.loadSharedKeys","myidentity need load ProducerIdentity");
            return false;
        }
        return true;

    }


}
