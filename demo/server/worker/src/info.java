import org.apache.kafka.clients.admin.*;
import org.apache.kafka.common.KafkaFuture;

import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

public class info {

    public static void main(String[] args) {
        Properties props = new Properties();
        props.put("bootstrap.servers", "8.130.10.212:9092");
        deleteAllTopic(props);
        printAll(props);

    }

    public static void printAll(Properties props){
        AdminClient admin = KafkaAdminClient.create(props);
        ListTopicsResult topics = admin.listTopics();
        ListConsumerGroupsResult groups = admin.listConsumerGroups();
        try {
            System.out.println("TOPIC:");
            topics.listings().get().forEach((x)->{
                System.out.println(x.name());
            });
            System.out.println("GROUP:");
            groups.all().get().forEach((x)->{
                System.out.println(x);
            });
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
        admin.close();
    }

    public static void deleteAllTopic(Properties props){
        AdminClient admin = KafkaAdminClient.create(props);
        try {
            Collection<TopicListing> topics = admin.listTopics().listings().get();
            for(TopicListing topic:topics){
                admin.deleteTopics(Arrays.asList(topic.name()));
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
        admin.close();


    }
}
