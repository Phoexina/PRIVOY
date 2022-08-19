//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//


import ch.ethz.infk.pps.shared.avro.Digest;
import ch.ethz.infk.pps.shared.avro.Token;
import ch.ethz.infk.pps.shared.avro.UniversePartitionUpdate;
import ch.ethz.infk.pps.zeph.server.worker.config.ConfigLoader;
import ch.ethz.infk.pps.zeph.server.worker.config.WorkerConfig;
import ch.ethz.infk.pps.zeph.server.worker.processor.CiphertextSumTransformer.CiphertextSumTransformerSupplier;
import ch.ethz.infk.pps.zeph.server.worker.processor.CiphertextTransformer.ValueTransformerSupplier;
import ch.ethz.infk.pps.zeph.server.worker.processor.TokenTransformer.TokenTransformerSupplier;
import ch.ethz.infk.pps.zeph.server.worker.util.DigestSerde;
import ch.ethz.infk.pps.zeph.server.worker.util.SerdeFactory;
import ch.ethz.infk.pps.zeph.shared.DigestOp;
import ch.ethz.infk.pps.zeph.shared.Names;
import com.google.common.collect.Sets;
import java.io.File;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import kstream.ciphersumtrans;
import kstream.ciphertrans;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.KafkaAdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.admin.TopicListing;
import org.apache.kafka.common.serialization.Serdes.LongSerde;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.KafkaStreams.State;
import org.apache.kafka.streams.KafkaStreams.StateListener;
import org.apache.kafka.streams.errors.DefaultProductionExceptionHandler;
import org.apache.kafka.streams.errors.LogAndContinueExceptionHandler;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.Stores;
import org.apache.kafka.streams.state.WindowBytesStoreSupplier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import kstream.ciphertrans.valuetransSupplier;
import kstream.ciphersumtrans.ciphersumtransSupplier;
import kstream.tokentrans.tokentransSupplier;

public class test {
    private static final Logger LOG = LogManager.getLogger();
    public static Marker GLOBAL_MARKER;

    public test() {
    }

    public static void main(String[] args) {

        //String[] plaintextArgs = (String[])Arrays.copyOfRange(args, 1, args.length);
        //PlaintextApp.main(plaintextArgs);
        String[] testargs = {"worker",
                "--universe-id", "5",
                "--window-size", "10000",
                "--grace-size", "10000",
                "--state-dir", "data",
                "--bootstrap-server", "localhost:9092",
                "--partitions", "1",
                "--retention-time", "1"
        };


        WorkerConfig config = ConfigLoader.loadConfig(testargs);
        System.out.println("config pass");

        long var10000 = config.getUniverseId();
        System.out.println("var1000:"+var10000);

        String instanceId = "worker-u" + var10000 + "-i" + System.currentTimeMillis();
        System.out.println("instanceId:"+instanceId);
        config.setInstanceId(instanceId);

        String instanceStateDir = Paths.get(config.getStateDir(), instanceId).toString();
        config.setStateDir(instanceStateDir);
        System.out.println("instanceStateDir:"+instanceStateDir);

        GLOBAL_MARKER = MarkerManager.getMarker("global-" + config.getInstanceId());
        System.out.println(GLOBAL_MARKER);

        createTopics(config);
        System.out.println("createTopics pass");

        SerdeFactory serde = new SerdeFactory();
        KafkaStreams streams = createStreams(config, serde);
        System.out.println("streams:"+streams);

        streams.setStateListener(new StateListener() {
            public void onChange(State newState, State oldState) {
                test.LOG.info("kafka streams state change: {} -> {}", oldState, newState);
            }
        });
        System.out.println("streams start");
        streams.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                LOG.info("Caught Shutdown Hook -> Closing Kafka Streams");
                streams.close();
                serde.close();
                if (config.deleteTopics()) {
                    deleteTopics(config);
                }
            } catch (Exception var4) {
                LOG.error("Failed to Close Kafka Streams", var4);
            }

        }));

    }

    private static KafkaStreams createStreams(WorkerConfig config, SerdeFactory serde) {
        Properties streamsConfig = buildStreamConfiguration(config);

        //获取参数
        Names n = new Names(config.getUniverseId());
        Duration windowSize = Duration.ofMillis(config.getWindowSizeMillis());
        Duration grace = Duration.ofMillis(config.getGraceSizeMillis());
        Duration retentionPeriod = Duration.ofHours(config.getRetentionPeriodHours());
        TimeWindows windows = TimeWindows.of(windowSize).grace(grace);
        //设置builder
        StreamsBuilder builder = new StreamsBuilder();
        WindowBytesStoreSupplier ciphertextSumStore = Stores.inMemoryWindowStore(n.CIPHERTEXT_SUM_STORE, retentionPeriod, windowSize, false);
        builder.addStateStore(Stores.keyValueStoreBuilder(Stores.inMemoryKeyValueStore(n.COMMITTING_SUM_STORE), serde.longSerde(), serde.digestSerde()));
        builder.addStateStore(Stores.windowStoreBuilder(Stores.inMemoryWindowStore(n.COMMIT_BUFFER_STORE, retentionPeriod, windowSize, false), serde.longSerde(), serde.intSerde()));
        builder.addStateStore(Stores.keyValueStoreBuilder(Stores.inMemoryKeyValueStore(n.TRANSFORMING_SUM_STORE), serde.longSerde(), serde.digestSerde()));
        builder.addStateStore(Stores.windowStoreBuilder(Stores.inMemoryWindowStore(n.EXPECTED_TRANSFORMATION_TOKEN_STORE, retentionPeriod, windowSize, false), serde.longSerde(), serde.intSerde()));
        builder.addStateStore(Stores.keyValueStoreBuilder(Stores.inMemoryKeyValueStore(n.MEMBER_STORE), serde.longSerde(), serde.longSerde()));
        builder.addStateStore(Stores.windowStoreBuilder(Stores.inMemoryWindowStore(n.MEMBER_DELTA_STORE, retentionPeriod, windowSize, false), serde.longSerde(), serde.intSerde()));

        KStream<Long, Digest> ciphertextInputStream = builder.stream(n.CIPHERTEXTS_TOPIC, Consumed.with(serde.longSerde(), serde.digestSerde()).withName("SOURCE.ciphertexts"));
        KStream<Long, Token> tokenInputStream = builder.stream(n.TOKEN_TOPIC, Consumed.with(serde.longSerde(), serde.tokenSerde()).withName("SOURCE.tokens"));

        //给Digest的header填写start 和end
        KStream<Long, Digest> ciphertext1=ciphertextInputStream.transformValues(new valuetransSupplier(), Named.as("TRANSFORM.heac-header"), new String[0]);
        //按producerId(key值)分组
        KGroupedStream<Long, Digest> ciphertext2=ciphertext1.groupByKey();
        TimeWindowedKStream<Long, Digest> ciphertext3=ciphertext2.windowedBy(windows);
        //相加
        KTable<Windowed<Long>, Digest> ciphertext4=ciphertext3.reduce((aggValue, newValue) -> {
            //System.out.println("reduce:"+aggValue+"+"+newValue);
            return DigestOp.add(aggValue, newValue);
            }, Named.as("REDUCE.ciphertexts"), Materialized.as(ciphertextSumStore));
        KStream<Windowed<Long>, Digest> ciphertext5=ciphertext4.toStream(Named.as("toStream"));
        //向主题TRANSFORM.ciphertext-sums发送
        KStream<Long, UniversePartitionUpdate> ciphertext6=ciphertext5.flatTransform(
                //new CiphertextSumTransformerSupplier(windows, n),
                new ciphersumtransSupplier(windows, n),
                Named.as("TRANSFORM.ciphertext-sums"),
                new String[]{n.CIPHERTEXT_SUM_STORE,
                             n.COMMIT_BUFFER_STORE,
                             n.COMMITTING_SUM_STORE,
                             n.TRANSFORMING_SUM_STORE,
                             n.EXPECTED_TRANSFORMATION_TOKEN_STORE,
                             n.MEMBER_STORE,
                              n.MEMBER_DELTA_STORE});
        //向主题universe-partition-updates发送
        ciphertext6.to("universe-partition-updates", Produced.with(serde.longSerde(), serde.updateSerde()));

        tokenInputStream.flatTransform(
                new tokentransSupplier(n),
                Named.as("TRANSFORM.tokens"),
                new String[]{n.CIPHERTEXT_SUM_STORE, n.COMMIT_BUFFER_STORE, n.COMMITTING_SUM_STORE, n.TRANSFORMING_SUM_STORE, n.EXPECTED_TRANSFORMATION_TOKEN_STORE, n.MEMBER_STORE, n.MEMBER_DELTA_STORE}).
                //向主题universe-partition-updates发送
                to("universe-partition-updates", Produced.with(serde.longSerde(), serde.updateSerde()));

        Topology topology = builder.build();
        System.out.println("TOPOLOGY: ");
        System.out.println(topology.describe());
        return new KafkaStreams(topology, streamsConfig);
    }

    private static Properties buildStreamConfiguration(WorkerConfig config) {
        long universeId = config.getUniverseId();
        Properties streamsConfig = new Properties();
        String identifier = "zeph-transformation-u" + universeId;
        streamsConfig.put("application.id", identifier);
        streamsConfig.put("client.id", identifier);
        streamsConfig.put("num.stream.threads", Math.min(config.getNumPartitions(), config.getStreamThreads()));
        streamsConfig.put("buffered.records.per.partition", 0);
        streamsConfig.put(StreamsConfig.producerPrefix("batch.size"), 0);
        streamsConfig.put(StreamsConfig.producerPrefix("linger.ms"), 0);
        streamsConfig.put("commit.interval.ms", 0);
        streamsConfig.put("bootstrap.servers", config.getKafkaBootstrapServers());
        streamsConfig.put("default.key.serde", LongSerde.class.getName());
        streamsConfig.put("default.value.serde", DigestSerde.class.getName());
        File file = new File(config.getStateDir());
        streamsConfig.put("state.dir", file.getPath());
        streamsConfig.put("default.deserialization.exception.handler", LogAndContinueExceptionHandler.class.getName());
        streamsConfig.put("default.production.exception.handler", DefaultProductionExceptionHandler.class.getName());
        return streamsConfig;
    }

    private static void createTopics(WorkerConfig config) {
        System.out.println("createTopics start");
        Names names = new Names(config.getUniverseId());
        System.out.println("names:"+config.getUniverseId());

        Properties props = new Properties();
        props.put("bootstrap.servers", config.getKafkaBootstrapServers());
        System.out.println("bootstrap.servers:"+config.getKafkaBootstrapServers());

        AdminClient admin = KafkaAdminClient.create(props);
        HashSet topics = Sets.newHashSet(new String[]{names.CIPHERTEXTS_TOPIC, names.TOKEN_TOPIC});

        try {
            Set<String> existingTopics = (Set)((Collection)admin.listTopics().listings().get()).stream().map((x) -> {
                System.out.println(((TopicListing)x).name());
                return ((TopicListing)x).name();
            }).collect(Collectors.toSet());
            int numPartitions = config.getNumPartitions();
            int replicationFactor = config.getReplication();
            List<NewTopic> createTopics = (List)topics.stream().filter((topic) -> {
                return !existingTopics.contains(topic);
            }).map((topic) -> {
                return new NewTopic((String)topic, numPartitions, (short)replicationFactor);
            }).collect(Collectors.toList());
            if (!existingTopics.contains(names.INFO_TOPIC)) {
                NewTopic infoTopic = new NewTopic(names.INFO_TOPIC, 1, (short)replicationFactor);
                createTopics.add(infoTopic);
            }

            admin.createTopics(createTopics).all().get();
        } catch (ExecutionException var14) {
            LOG.warn(GLOBAL_MARKER, "execution exception while creating topics: ", var14);
        } catch (InterruptedException var15) {
            throw new IllegalStateException("failed to create topics", var15);
        } finally {
            admin.close();
        }

    }

    private static void deleteTopics(WorkerConfig config) {
        Properties props = new Properties();
        props.put("bootstrap.servers", config.getKafkaBootstrapServers());
        AdminClient admin = KafkaAdminClient.create(props);
        HashSet topics = Sets.newHashSet(new String[]{Names.getInfosTopic(config.getUniverseId()), Names.getTokensTopic(config.getUniverseId())});

        try {
            admin.deleteTopics(topics).all().get();
            admin.close();
        } catch (InterruptedException | ExecutionException var5) {
            throw new IllegalStateException("failed to delete topics", var5);
        }
    }
}
