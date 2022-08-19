package drivers;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;

import ch.ethz.infk.pps.shared.avro.Digest;
import ch.ethz.infk.pps.zeph.client.facade.ILocalTransformationFacade;
import ch.ethz.infk.pps.zeph.client.facade.LocalTransformationFacade;
import ch.ethz.infk.pps.zeph.shared.serde.SpecificAvroSimpleSerializer;
import com.google.common.primitives.Longs;
import data.CiphertextData;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.apache.kafka.common.serialization.LongSerializer;

public class CiphertextDriver {

    protected Map<String,LocalTransformationFacade> facades;
    protected static CiphertextDriver ciphertextDriver;

    private CiphertextDriver(){
        facades=new HashMap<>();

    }

    public static CiphertextDriver getCiphertextDriver(){
        if(ciphertextDriver==null){
            synchronized (CiphertextDriver.class){
                if(ciphertextDriver==null){
                    ciphertextDriver=new CiphertextDriver();
                }
            }
        }
        return ciphertextDriver;
    }

    public void send(CiphertextData data) {
        String tag= data.getProduceId()+"-"+data.getUniverseId();
        LocalTransformationFacade facade=facades.get(tag);
        if(facade==null){
            facade=new LocalTransformationFacade("8.130.10.212:9092");
            facade.init(data.getProduceId(), data.getUniverseId());
            facades.put(tag,facade);
        }
        CompletableFuture<RecordMetadata> future=facade.sendCiphertext(data.getTimestamp(), CiphertextData.toDigest(data.getData()), data.getPrevTimestamp());
    }
}
