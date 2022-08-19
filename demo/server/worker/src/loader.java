//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//


import ch.ethz.infk.pps.zeph.server.worker.config.WorkerConfig;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

public class loader {
    public loader() {
    }
    public static void main(String[] args) {

        //String[] plaintextArgs = (String[])Arrays.copyOfRange(args, 1, args.length);
        //PlaintextApp.main(plaintextArgs);
        String[] testargs = {"worker",
                "--universe-id", "1",
                "--window-size", "1000",
                "--grace-size", "1000",
                "--state-dir", "data",
                "--bootstrap-server", "127.0.0.1:3000",
                "--partitions", "4",
                "--retention-time","24"

        };
        WorkerConfig config = loadConfig(testargs);
        System.out.println("config pass");
    }
    public static WorkerConfig loadConfig(String[] args) {
        CommandLineParser parser = new DefaultParser();
        Options options = buildOptions();

        try {
            CommandLine cmd = parser.parse(options, args);
            WorkerConfig config = new WorkerConfig();
            config.setKafkaBootstrapServers(cmd.getOptionValue("bootstrap-server"));
            System.out.println("bootstrap-server pass");
            config.setUniverseId(Long.parseLong(cmd.getOptionValue("universe-id")));
            System.out.println("universe-id pass");
            config.setUniverseSize(Long.parseLong(cmd.getOptionValue("universe-size", "-1")));
            System.out.println("universe-size pass");
            config.setWindowSizeMillis(Long.parseLong(cmd.getOptionValue("window-size")));
            System.out.println("window-size pass");
            config.setGraceSizeMillis(Long.parseLong(cmd.getOptionValue("grace-size")));
            System.out.println("grace-size pass");
            config.setRetentionPeriodHours(Long.parseLong(cmd.getOptionValue("retention-time")));
            System.out.println("retention-time pass");
            config.setStreamThreads(Integer.parseInt(cmd.getOptionValue("stream-threads", "1")));
            System.out.println("stream-threads pass");
            config.setNumPartitions(Integer.parseInt(cmd.getOptionValue("partitions", "3")));
            System.out.println("partitions pass");
            config.setReplication(Integer.parseInt(cmd.getOptionValue("replications", "1")));
            System.out.println("replications pass");
            config.setStateDir(cmd.getOptionValue("state-dir"));
            System.out.println("state-dir pass");
            config.setDeleteTopics(cmd.hasOption("delete-topics"));
            System.out.println("delete-topics pass");
            return config;
        } catch (Exception var5) {
            System.out.println(var5);
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("zeph-server-worker", options);
            System.exit(0);
            return null;
        }
    }

    private static Options buildOptions() {
        Options options = new Options();
        options.addOption(Option.builder().longOpt("universe-id").argName("NUMBER").desc("universe id").hasArg().required().build());
        options.addOption(Option.builder().longOpt("universe-size").argName("NUMBER").desc("universe size").hasArg().build());
        options.addOption(Option.builder().longOpt("window-size").argName("MILLIS").desc("window size in milliseconds").hasArg().required().build());
        options.addOption(Option.builder().longOpt("grace-size").argName("MILLIS").desc("grace size in milliseconds (i.e. time late records are accepted)").hasArg().required().build());
        options.addOption(Option.builder().longOpt("retention-time").argName("HOURS").desc("retention period for streams state").hasArg().build());
        options.addOption(Option.builder().longOpt("state-dir").argName("PATH").desc("kafka streams state directory").hasArg().required().build());
        options.addOption(Option.builder().longOpt("bootstrap-server").argName("HOST:PORT").desc("kafka bootstrap server host:port").hasArg().required().build());
        options.addOption(Option.builder().longOpt("partitions").argName("NUMBER").desc("number of partitions of token and value topics").hasArg().required().build());
        options.addOption(Option.builder().longOpt("replications").argName("NUMBER").desc("number of replications of token and value topic").hasArg().build());
        options.addOption(Option.builder().longOpt("stream-threads").argName("NUMBER").desc("number of threads in kafka streams").hasArg().build());
        options.addOption(Option.builder().longOpt("delete-topics").hasArg(false).desc("delete the created topics when shutting down").build());
        return options;
    }
}
