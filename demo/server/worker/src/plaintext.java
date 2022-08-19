
import org.apache.kafka.clients.admin.TopicListing;

import ch.ethz.infk.pps.shared.avro.ApplicationAdapter;
import ch.ethz.infk.pps.shared.avro.Input;
import ch.ethz.infk.pps.shared.avro.TransformationResult;
import ch.ethz.infk.pps.shared.avro.Window;
import ch.ethz.infk.pps.zeph.server.worker.config.ConfigLoader;
import ch.ethz.infk.pps.zeph.server.worker.config.WorkerConfig;
import ch.ethz.infk.pps.zeph.server.worker.util.InputSerde;
import ch.ethz.infk.pps.zeph.shared.TransformationResultOp;
import ch.ethz.infk.pps.zeph.shared.pojo.WindowedUniverseId;
import ch.ethz.infk.pps.zeph.shared.serde.SpecificAvroSimpleSerde;
import ch.ethz.infk.pps.zeph.shared.serde.WindowedUniverseIdSerde;
import com.google.common.collect.Sets;
import java.io.File;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.KafkaAdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.errors.DefaultProductionExceptionHandler;
import org.apache.kafka.streams.errors.LogAndContinueExceptionHandler;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.kstream.Suppressed;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.apache.kafka.streams.kstream.Transformer;
import org.apache.kafka.streams.kstream.TransformerSupplier;
import org.apache.kafka.streams.kstream.Suppressed.BufferConfig;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.processor.To;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.Stores;
import org.apache.kafka.streams.state.WindowBytesStoreSupplier;

public class plaintext {
    public plaintext () {
    }

    public static String getPlaintextTopic(long universeId) {
        return "u" + universeId + "-plaintexts";
    }

    public static String getPlaintextSumStore(long universeId) {
        return "u" + universeId + "-plaintext-sum-store";
    }

    public static String getPlaintextProducerSumTopic(long universeId) {
        return "u" + universeId + "-plaintext-psums";
    }

    public static void main(String[] args) {
        String[] testargs = {
                "--universe-id", "5",
                "--window-size", "1000",
                "--grace-size", "1000",
                "--state-dir", "data",
                "--bootstrap-server", "192.168.10.103:9092",
                "--partitions", "4",
                "--retention-time","24",
                "--universe-size","10000"

        };
        WorkerConfig config = ConfigLoader.loadConfig(testargs);
        System.out.println("config pass");

        long var10000 = config.getUniverseId();
        String instanceId = "plaintext-worker-u" + var10000 + "-i" + System.currentTimeMillis();
        config.setInstanceId(instanceId);
        String instanceStateDir = Paths.get(config.getStateDir(), instanceId).toString();
        config.setStateDir(instanceStateDir);

        System.out.println("createTopics start");
        createTopics(config);
        System.out.println("createTopics pass");

        KafkaStreams streams = createStreams(config);
        System.out.println("streams:"+streams);
        streams.start();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                System.out.println("Thread");
                streams.close();
                if (config.deleteTopics()) {
                    deleteTopics(config);
                }
            } catch (Exception var3) {
            }

        }));
    }

    private static KafkaStreams createStreams(WorkerConfig config) {
        long universeId = config.getUniverseId();
        final long universeSize = config.getUniverseSize();
        if (universeSize < 0L) {
            throw new IllegalArgumentException("plaintext app requires that universe size is set");
        } else {
            Properties streamsConfig = buildStreamConfiguration(config);
            StreamsBuilder builder = new StreamsBuilder();
            Serdes.LongSerde longSerde = new Serdes.LongSerde();
            SpecificAvroSimpleSerde<Input> inputSerde = new SpecificAvroSimpleSerde(Input.getClassSchema());
            SpecificAvroSimpleSerde<TransformationResult> resultSerde = new SpecificAvroSimpleSerde(TransformationResult.getClassSchema());
            WindowedUniverseIdSerde windowedUniverseIdSerde = new WindowedUniverseIdSerde();
            final String dataStoreName = getPlaintextSumStore(universeId);
            String producerIntermediateSumTopic = getPlaintextProducerSumTopic(universeId);
            TimeWindows windows = TimeWindows.of(Duration.ofMillis(config.getWindowSizeMillis())).grace(Duration.ofMillis(config.getGraceSizeMillis()));
            builder.addStateStore(Stores.keyValueStoreBuilder(Stores.inMemoryKeyValueStore(dataStoreName), windowedUniverseIdSerde, inputSerde));
            WindowBytesStoreSupplier store = Stores.inMemoryWindowStore("u" + universeId + "-xy", Duration.ofHours(config.getRetentionPeriodHours()), Duration.ofMillis(config.getWindowSizeMillis()), false);
            builder.stream(getPlaintextTopic(universeId), Consumed.with(longSerde, inputSerde)).groupByKey(Grouped.with(longSerde, inputSerde)).windowedBy(windows).reduce((a, b) -> {
                return ApplicationAdapter.add(a, b);
            }, Materialized.as(store)).suppress(Suppressed.untilWindowCloses(BufferConfig.unbounded())).toStream().selectKey((k, v) -> {
                return new WindowedUniverseId(universeId, new Window(k.window().start(), k.window().end()));
            }).through(producerIntermediateSumTopic, Produced.with(windowedUniverseIdSerde, inputSerde)).flatTransform(new TransformerSupplier<WindowedUniverseId, Input, Iterable<KeyValue<Long, TransformationResult>>>() {
                public Transformer<WindowedUniverseId, Input, Iterable<KeyValue<Long, TransformationResult>>> get() {
                    return new Transformer<WindowedUniverseId, Input, Iterable<KeyValue<Long, TransformationResult>>>() {
                        private ProcessorContext ctx;
                        private KeyValueStore<WindowedUniverseId, Input> dataStore;

                        public void init(ProcessorContext context) {
                            this.ctx = context;
                            this.dataStore = (KeyValueStore)this.ctx.getStateStore(dataStoreName);
                        }

                        public Iterable<KeyValue<Long, TransformationResult>> transform(WindowedUniverseId key, Input value) {
                            Input aggregator = (Input)this.dataStore.get(key);
                            if (aggregator == null) {
                                aggregator = ApplicationAdapter.empty();
                            }

                            value.setCount(1L);
                            Input updatedAggregator = ApplicationAdapter.add(aggregator, value);
                            long counter = updatedAggregator.getCount();
                            if (counter < universeSize) {
                                this.dataStore.put(key, updatedAggregator);
                                return Collections.emptySet();
                            } else {
                                this.dataStore.delete(key);
                                updatedAggregator.setCount(0L);
                                TransformationResult result = TransformationResultOp.of(updatedAggregator, key.getWindow());
                                return Collections.singleton(new KeyValue(key.getUniverseId(), result));
                            }
                        }

                        public void close() {
                        }
                    };
                }
            }, new String[]{dataStoreName}).transform(new TransformerSupplier<Long, TransformationResult, KeyValue<Long, TransformationResult>>() {
                public Transformer<Long, TransformationResult, KeyValue<Long, TransformationResult>> get() {
                    return new Transformer<Long, TransformationResult, KeyValue<Long, TransformationResult>>() {
                        private ProcessorContext ctx;

                        public void init(ProcessorContext context) {
                            this.ctx = context;
                        }

                        public KeyValue<Long, TransformationResult> transform(Long key, TransformationResult value) {
                            long timestamp = System.currentTimeMillis();
                            this.ctx.forward(key, value, To.all().withTimestamp(timestamp));
                            return null;
                        }

                        public void close() {
                        }
                    };
                }
            }, new String[0]).to("universe-results", Produced.with(longSerde, resultSerde));
            Topology topology = builder.build();
            System.out.println("TOPOLOGY: ");
            System.out.println(topology.describe());
            return new KafkaStreams(topology, streamsConfig);
        }
    }

    private static Properties buildStreamConfiguration(WorkerConfig config) {
        Properties streamsConfig = new Properties();
        String identifier = "plaintext-transformation-u" + config.getUniverseId();
        streamsConfig.put("application.id", identifier);
        streamsConfig.put("client.id", identifier);
        streamsConfig.put("bootstrap.servers", config.getKafkaBootstrapServers());
        streamsConfig.put("num.stream.threads", Math.min(config.getNumPartitions(), config.getStreamThreads()));
        streamsConfig.put(StreamsConfig.producerPrefix("batch.size"), 0);
        streamsConfig.put(StreamsConfig.producerPrefix("linger.ms"), 0);
        streamsConfig.put("commit.interval.ms", 0);
        streamsConfig.put("default.key.serde", Serdes.LongSerde.class.getName());
        streamsConfig.put("default.value.serde", InputSerde.class.getName());
        File file = new File(config.getStateDir());
        streamsConfig.put("state.dir", file.getPath());
        streamsConfig.put("default.deserialization.exception.handler", LogAndContinueExceptionHandler.class.getName());
        streamsConfig.put("default.production.exception.handler", DefaultProductionExceptionHandler.class.getName());
        return streamsConfig;
    }

    private static void createTopics(WorkerConfig config) {
        Properties props = new Properties();
        props.put("bootstrap.servers", config.getKafkaBootstrapServers());
        AdminClient admin = KafkaAdminClient.create(props);
        Set<String> topics = Sets.newHashSet(new String[]{getPlaintextProducerSumTopic(config.getUniverseId()), getPlaintextTopic(config.getUniverseId()), "universe-results"});

        try {
            Set<String> existingTopics = (Set)((Collection)admin.listTopics().listings().get()).stream().map((x) -> {
                //return x.name();
                System.out.println(((TopicListing)x).name());
                return ((TopicListing)x).name();
            }).collect(Collectors.toSet());
            int numPartitions = config.getNumPartitions();
            int replicationFactor = config.getReplication();
            List<NewTopic> createTopics = (List)topics.stream().filter((topic) -> {
                return !existingTopics.contains(topic);
            }).map((topic) -> {
                System.out.println("new "+topic);
                return new NewTopic(topic, numPartitions, (short)replicationFactor);
            }).collect(Collectors.toList());
            admin.createTopics(createTopics).all().get();
            admin.close();
        } catch (InterruptedException | ExecutionException var8) {
            throw new IllegalStateException("failed to create topics", var8);
        }
    }

    private static void deleteTopics(WorkerConfig config) {
        Properties props = new Properties();
        props.put("bootstrap.servers", config.getKafkaBootstrapServers());
        AdminClient admin = KafkaAdminClient.create(props);
        Set<String> topics = Sets.newHashSet(new String[]{getPlaintextProducerSumTopic(config.getUniverseId()), getPlaintextTopic(config.getUniverseId()), "universe-results"});

        try {
            admin.deleteTopics(topics).all().get();
            admin.close();
        } catch (InterruptedException | ExecutionException var5) {
            throw new IllegalStateException("failed to delete topics", var5);
        }
    }
}
