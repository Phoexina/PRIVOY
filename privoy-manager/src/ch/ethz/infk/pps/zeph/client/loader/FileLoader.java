package ch.ethz.infk.pps.zeph.client.loader;

import ch.ethz.infk.pps.shared.avro.Universe;
import ch.ethz.infk.pps.shared.avro.tuple.AESKey;
import ch.ethz.infk.pps.shared.avro.tuple.ProducerKeyTuple;
import ch.ethz.infk.pps.shared.avro.util.AESGCMCodec;
import ch.ethz.infk.pps.shared.avro.util.ProducerKeyFileStore;
import ch.ethz.infk.pps.shared.avro.util.SharedKeyFileStore;
import ch.ethz.infk.pps.shared.avro.util.X509CertificateConverter;
import ch.ethz.infk.pps.zeph.Main;
import ch.ethz.infk.pps.zeph.client.util.ClientConfig;
import ch.ethz.infk.pps.zeph.client.util.LoaderUtil;
import ch.ethz.infk.pps.zeph.client.util.PathUtil;
import ch.ethz.infk.pps.zeph.client.util.SQLUtil;
import ch.ethz.infk.pps.zeph.crypto.DiffieHellmanKeyExchange;
import ch.ethz.infk.pps.zeph.shared.pojo.ProducerIdentity;
import org.apache.avro.file.CodecFactory;
import org.apache.avro.file.DataFileReader;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apaches.commons.codec.digest.DigestUtils;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FileLoader {
    private Map<Long, ProducerIdentity> producers;
    private ConcurrentHashMap<Long, Certificate> globalCertificates;
    private static String PASSWORD="password";

    private FileLoader(){

        producers=new HashMap<>();
        globalCertificates =new ConcurrentHashMap<>();
    }

    public volatile static FileLoader fileLoader= null;
    public static FileLoader getInstance() {
        if (fileLoader==null){
            synchronized (FileLoader.class) {
                if (fileLoader==null){
                    fileLoader=new FileLoader();
                }
            }
        }
        return fileLoader;
    }

    public void setPassword(String password){
        PASSWORD=password;
    }

    public void setProducers(Map<Long, ProducerIdentity> producers) {
        this.producers = producers;
    }
    public void setGlobalCertificates(ConcurrentHashMap<Long, Certificate> globalCertificates) {
        this.globalCertificates = globalCertificates;
    }

    public List<ClientConfig> toClientConfigs(Long unverseId){
        List<ClientConfig> clientConfigs=new ArrayList<>();
        producers.forEach((pid,pinfo)->{
            if(pid!=0L) {
                ClientConfig config = new ClientConfig();
                config.setProducerId(pid);
                config.setUniverseId(unverseId);
                config.setHeacKey(pinfo.getHeacKey());
                config.setPrivateKey(pinfo.getPrivateKey());
                config.setSharedKeys(LoaderUtil.getProducerSharedKeys(pid, pinfo));
                clientConfigs.add(config);
            }
        });
        return clientConfigs;
    }

    public List<Long> toLongs(){
        List<Long> producerIds=new ArrayList<>();
        producers.forEach((pid,pinfo)->{
            if(pid!=0L) producerIds.add(pid);
        });
        return producerIds;
    }

    public void loadIdentity(File file){
        Map<Long, ProducerIdentity> allproducers = LoaderUtil.getAllProducerIdentity(PASSWORD);
        if(allproducers.size()>producers.size()){
            System.out.println("class producers"+producers.size()+" not lastest, load "+allproducers.size());
            producers.putAll(allproducers);
        }
        Map<Long, ProducerIdentity> newproducer=load(file);
        producers.putAll(newproducer);
        LoaderUtil.loadAllProducerIdentity(PASSWORD,producers);
        if(newproducer.size() != 1) return;
        try {
            String md5 = DigestUtils.md5Hex(new FileInputStream(file));
            System.out.println("MD5:"+DigestUtils.md5Hex(new FileInputStream(file)));
            Long pId = null;
            ProducerIdentity pInfo = null;
            for (Long key : newproducer.keySet()) {
                pId = key;
                pInfo = producers.get(pId);
            }
            SQLUtil.statement.executeUpdate("replace into producer values(" +pId+", '"+md5+"')");
            if(pInfo != null && !globalCertificates.containsKey(pId)) {
                globalCertificates.put(pId, pInfo.getCertificate());
                PrivateKey privateKey = pInfo.getPrivateKey();
                SecretKey sharedKeysFileKey = pInfo.getSharedKeysFileKey();
                ConcurrentHashMap<Long, SecretKey> sharedKeys = new ConcurrentHashMap();
                File sharedKeysFile = PathUtil.getSharedKeysFile(pId);
                DiffieHellmanKeyExchange keyExchange = new DiffieHellmanKeyExchange();
                globalCertificates.forEach((otherProducerId, certificate) -> {
                    if (!sharedKeys.containsKey(otherProducerId)) {
                        SecretKey sharedKey = keyExchange.generateSharedKey(privateKey, certificate);
                        sharedKeys.put(otherProducerId, sharedKey);
                    }

                });
                SharedKeyFileStore.store(sharedKeysFile, "sk" + pId, sharedKeysFileKey, sharedKeys);
                System.out.println("Generated " + sharedKeys.size() + " shared keys");
                Main.tokenDriver.addProducerConfig(pId,pInfo);
            }
        }catch (SQLException | FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Load new producer"+file.getPath());

    }

    private static String md5HashCode(InputStream fis) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");

            byte[] buffer = new byte[1024];
            int length = -1;
            while ((length = fis.read(buffer, 0, 1024)) != -1) {
                md.update(buffer, 0, length);
            }
            fis.close();
            //转换并返回包含16个元素字节数组,返回数值范围为-128到127
            byte[] md5Bytes  = md.digest();
            BigInteger bigInt = new BigInteger(1, md5Bytes);//1代表绝对值
            return bigInt.toString(16);//转换为16进制
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private static Map<Long, ProducerIdentity> load(File file) {
        Map<Long, ProducerIdentity> result = new HashMap();

        try {
            DatumReader<ProducerKeyTuple> datumReader = new SpecificDatumReader(ProducerKeyTuple.class);
            DataFileReader<ProducerKeyTuple> dataFileReader = new DataFileReader(file, datumReader);
            ProducerKeyTuple tuple = null;
            KeyFactory kf = KeyFactory.getInstance("EC");

            while(dataFileReader.hasNext()) {
                tuple = (ProducerKeyTuple)dataFileReader.next(tuple);
                long pId = tuple.getProducerId();
                Certificate certificate = X509CertificateConverter.fromBytes(tuple.getCertificate());
                PrivateKey privateKey = kf.generatePrivate(new PKCS8EncodedKeySpec(tuple.getPrivateKey().array()));
                SecretKeySpec tokensFileKey = new SecretKeySpec(tuple.getDummyFileKey().bytes(), "AES");
                SecretKeySpec sharedKeysFileKey = new SecretKeySpec(tuple.getSharedFileKey().bytes(), "AES");
                SecretKeySpec heacKey = new SecretKeySpec(tuple.getHeacKey().bytes(), "AES");
                ProducerIdentity info = (new ProducerIdentity()).withCertificate(certificate).withHeacKey(heacKey).withPrivateKey(privateKey).withSharedKeysFileKey(sharedKeysFileKey).withTokensFileKey(tokensFileKey);
                result.put(pId, info);
            }

            dataFileReader.close();
            return result;
        } catch (InvalidKeySpecException | NoSuchAlgorithmException | IOException var15) {
            throw new IllegalStateException("failed to load producer key tuple", var15);
        }
    }

    private static void store(File file, Map<Long, ProducerIdentity> producerInfos) {
        DatumWriter<ProducerKeyTuple> datumWriter = new SpecificDatumWriter(ProducerKeyTuple.class);
        DataFileWriter<ProducerKeyTuple> dataFileWriter = new DataFileWriter(datumWriter);

        try {
            dataFileWriter.create(ProducerKeyTuple.getClassSchema(), file);
            producerInfos.forEach((pId, info) -> {
                try {
                    ProducerKeyTuple tuple = new ProducerKeyTuple(pId, ByteBuffer.wrap(info.getCertificate().getEncoded()), ByteBuffer.wrap(info.getPrivateKey().getEncoded()), new AESKey(info.getSharedKeysFileKey().getEncoded()), new AESKey(info.getTokensFileKey().getEncoded()), new AESKey(info.getHeacKey().getEncoded()));
                    dataFileWriter.append(tuple);
                } catch (CertificateEncodingException | IOException var4) {
                    throw new IllegalStateException("failed to write producers to file", var4);
                }
            });
            dataFileWriter.close();
        } catch (IOException var6) {
            throw new IllegalStateException("failed to write producers to file", var6);
        }
    }
}
