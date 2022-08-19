
import kstream.universepartition.universepartitionSupplier;

import ch.ethz.infk.pps.shared.avro.DeltaUniverseState;
import ch.ethz.infk.pps.shared.avro.Token;
import ch.ethz.infk.pps.shared.avro.UniversePartitionUpdate;
import ch.ethz.infk.pps.zeph.server.master.config.ConfigLoader;
import ch.ethz.infk.pps.zeph.server.master.config.MasterConfig;
import ch.ethz.infk.pps.zeph.server.master.util.SerdeFactory;
import ch.ethz.infk.pps.zeph.shared.Names;
import ch.ethz.infk.pps.zeph.shared.pojo.WindowedUniverseId;
import com.google.common.collect.Sets;
import java.io.File;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.KafkaAdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.admin.TopicListing;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.errors.DefaultProductionExceptionHandler;
import org.apache.kafka.streams.errors.LogAndContinueExceptionHandler;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Named;
import org.apache.kafka.streams.processor.RecordContext;
import org.apache.kafka.streams.processor.StreamPartitioner;
import org.apache.kafka.streams.processor.TopicNameExtractor;
import org.apache.kafka.streams.state.Stores;
public class test {
    public test() {
    }

    public static void main(String[] args) {
        String[] testargs={
                "--bootstrap-server","172.22.84.175:9092",
                "--interactive-queries-server","172.22.84.175:9093",
                "--state-dir","data",
                "--time-to-commit","30000",
                "--universe-partitions","1"
                //"--stream-threads","1",

        };
        MasterConfig config = ConfigLoader.getConfig(testargs);
        String instanceId = "master-i" + System.currentTimeMillis();
        config.setInstanceId(instanceId);
        String instanceStateDir = Paths.get(config.getStateDir(), instanceId).toString();
        config.setStateDir(instanceStateDir);
        System.out.println(config.getStateDir());

        createTopics(config);

        SerdeFactory serde = new SerdeFactory();
        KafkaStreams streams = createStreams(config, serde);
        streams.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                System.out.println("SHUTDOWN HOOK");
                streams.close();
                serde.close();
                if (config.deleteTopics()) {
                    deleteTopics(config);
                }
            } catch (Exception var4) {
            }

        }));
    }

    private static KafkaStreams createStreams(MasterConfig config, SerdeFactory serde) {
        Properties streamsConfig = buildStreamConfiguration(config);
        Duration timeForCommit = Duration.ofMillis(config.getTimeToCommitMillis());
        StreamsBuilder builder = new StreamsBuilder();
        builder.addStateStore(Stores.keyValueStoreBuilder(Stores.inMemoryKeyValueStore("task-status-store"), serde.windowedUniverseIdSerde(), serde.universePartitionStatusSerde()));
        builder.addStateStore(Stores.keyValueStoreBuilder(Stores.inMemoryKeyValueStore("status-store"), serde.windowedUniverseIdSerde(), serde.intSerde()));
        builder.addStateStore(Stores.keyValueStoreBuilder(Stores.inMemoryKeyValueStore("membership-store"), serde.windowedUniverseIdSerde(), serde.membershipSerde()));
        builder.addStateStore(Stores.keyValueStoreBuilder(Stores.inMemoryKeyValueStore("result-store"), serde.windowedUniverseIdSerde(), serde.digestSerde()));

        KStream<Long, UniversePartitionUpdate> updateInputStream = builder.stream("universe-partition-updates", Consumed.with(serde.longSerde(), serde.updateSerde()).withName("SOURCE.universe-partition-updates"));
        updateInputStream.process(new universepartitionSupplier(timeForCommit), Named.as("TRANSFORM.universe-partition-update"), new String[]{"status-store", "task-status-store", "membership-store", "result-store"});

        Topology topology = builder.build();
        topology.addSink("SINK.results", "universe-results", serde.longSerde().serializer(), serde.resultSerde().serializer(), new String[]{"TRANSFORM.universe-partition-update"});
        topology.addSink("SINK.u-tokens", new test.UniverseTopicNameExtractorFromToken(), serde.longSerde().serializer(), serde.tokenSerde().serializer(), new test.DirectPartitioner(), new String[]{"TRANSFORM.universe-partition-update"});
        topology.addSink("SINK.u-info", new test.UniverseTopicNameExtractor(), serde.windowedUniverseIdSerde().serializer(), serde.universeWindowStateSerde().serializer(), new String[]{"TRANSFORM.universe-partition-update"});
        System.out.println("TOPOLOGY: ");
        System.out.println(topology.describe());
        return new KafkaStreams(topology, streamsConfig);
    }

    private static Properties buildStreamConfiguration(MasterConfig config) {
        Properties streamsConfig = new Properties();
        String identifier = "zeph-server";
        streamsConfig.put("application.id", identifier);
        streamsConfig.put("client.id", identifier);
        streamsConfig.put("bootstrap.servers", config.getKafkaBootstrapServers());
        streamsConfig.put("buffered.records.per.partition", 0);
        streamsConfig.put(StreamsConfig.producerPrefix("batch.size"), 0);
        streamsConfig.put(StreamsConfig.producerPrefix("linger.ms"), 0);
        streamsConfig.put("commit.interval.ms", 0);
        File file = new File(config.getStateDir());
        streamsConfig.put("state.dir", file.getPath());
        streamsConfig.put("default.deserialization.exception.handler", LogAndContinueExceptionHandler.class.getName());
        streamsConfig.put("default.production.exception.handler", DefaultProductionExceptionHandler.class.getName());
        return streamsConfig;
    }

    private static void createTopics(MasterConfig config) {
        Properties props = new Properties();
        String bootstrapServers = config.getKafkaBootstrapServers();
        props.put("bootstrap.servers", bootstrapServers);
        AdminClient admin = KafkaAdminClient.create(props);
        HashSet topics = Sets.newHashSet(new String[]{"universe-partition-updates", "universe-results"});

        try {
            Set<String> existingTopics = (Set)((Collection)admin.listTopics().listings().get()).stream().map((x) -> {
                return ((TopicListing)x).name();
            }).collect(Collectors.toSet());
            int numPartitions = config.getNumPartitions();
            int replicationFactor = config.getReplication();
            List<NewTopic> createTopics = (List)topics.stream().filter((topic) -> {
                return !existingTopics.contains(topic);
            }).map((topic) -> {
                return new NewTopic((String)topic, numPartitions, (short)replicationFactor);
            }).collect(Collectors.toList());
            admin.createTopics(createTopics).all().get();
            admin.close();
        } catch (InterruptedException | ExecutionException var9) {
            throw new IllegalStateException("failed to create topics", var9);
        }
    }

    private static void deleteTopics(MasterConfig config) {
        Properties props = new Properties();
        String bootstrapServers = config.getKafkaBootstrapServers();
        props.put("bootstrap.servers", bootstrapServers);
        AdminClient admin = KafkaAdminClient.create(props);
        HashSet topics = Sets.newHashSet(new String[]{"universe-partition-updates", "universe-results"});

        try {
            admin.deleteTopics(topics).all().get();
            admin.close();
        } catch (InterruptedException | ExecutionException var6) {
            throw new IllegalStateException("failed to delete topics", var6);
        }
    }

    private static class DirectPartitioner<V> implements StreamPartitioner<Long, V> {
        private DirectPartitioner() {
        }

        public Integer partition(String topic, Long key, V value, int numPartitions) {
            return (int)(key % (long)numPartitions);
        }
    }

    private static class UniverseTopicNameExtractorFromToken implements TopicNameExtractor<Long, Token> {
        private UniverseTopicNameExtractorFromToken() {
        }

        public String extract(Long key, Token token, RecordContext ctx) {
            long universeId = Long.parseLong(token.getMac());
            String topic = Names.getTokensTopic(universeId);
            return topic;
        }
    }

    private static class UniverseTopicNameExtractor implements TopicNameExtractor<WindowedUniverseId, DeltaUniverseState> {
        private UniverseTopicNameExtractor() {
        }

        public String extract(WindowedUniverseId key, DeltaUniverseState value, RecordContext recordContext) {
            String topic = Names.getInfosTopic(key.getUniverseId());
            return topic;
        }
    }
}
