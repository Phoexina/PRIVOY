import ch.ethz.infk.pps.shared.avro.TransformationResult;
import ch.ethz.infk.pps.shared.avro.Window;
import ch.ethz.infk.pps.zeph.shared.serde.SpecificAvroSimpleDeserializer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Properties;

public class ResultConsumer {
    public static void main(String[] args) {

        //0 配置
        Properties properties = new Properties();
        //连接集群
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,"8.130.10.212:9092");

        //配置消费者组id 必须！！！
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, "result");
        properties.put(ConsumerConfig.CLIENT_ID_CONFIG, "result");
        properties.put("auto.offset.reset", "earliest");

        //1 创建一个消费者
        //KafkaConsumer<Long, Digest > kafkaConsumer = buildCiphertextConsumer("192.168.10.103:9092", 1L);
        KafkaConsumer<Long, TransformationResult> kafkaConsumer = new KafkaConsumer<Long, TransformationResult>(properties,new LongDeserializer(),new SpecificAvroSimpleDeserializer(TransformationResult.getClassSchema()));
        //new SpecificAvroSimpleDeserializer(TransformationResult.getClassSchema())

        //2 定义主题first
        kafkaConsumer.subscribe(Collections.singleton("universe-results"));
        KafkaProducer<String, String> producer=buildProducer();

        //3 消费数据
        while(true){
            ConsumerRecords<Long, TransformationResult> consumerRecords = kafkaConsumer.poll(Duration.ofSeconds(30000));
            for(ConsumerRecord<Long, TransformationResult> consumerRecord : consumerRecords){
                //System.out.println(consumerRecord);
                System.out.println("内容："+consumerRecord.value());
                Long id=consumerRecord.key();
                Window window=consumerRecord.value().getWindow();
                Long number=consumerRecord.value().getCount();
                double value=consumerRecord.value().getSum();
                if(value<0 || value >10000) continue;
                String res="属性"+id+"在"+window+"内计数"+number+"次，平均速度为"+value/1000+"m/s";
                System.out.println(res);
                producer.send(new ProducerRecord<>("string-results",res));
            }
        }
    }
    public static KafkaProducer<String, String> buildProducer(){
        Properties properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,"8.130.10.212:9092");
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, "results");
        properties.put(ConsumerConfig.CLIENT_ID_CONFIG, "stringresult1");
        return new KafkaProducer(properties);
    }
}

