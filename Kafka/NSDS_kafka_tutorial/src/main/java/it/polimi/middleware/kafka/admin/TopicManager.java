package it.polimi.middleware.kafka.admin;

import org.apache.kafka.clients.admin.*;

import java.util.Collections;
import java.util.Properties;
import java.util.Set;

public class TopicManager  {
    private static final String defaultTopicName = "topicA";
    private static final int defaultTopicPartitions = 1;
    private static final short defaultReplicationFactor = 1;

    private static final String serverAddr = "localhost:9092";

    public static void main(String[] args) throws Exception {
        final String topicName = args.length >= 1 ? args[0] : defaultTopicName;
        final int topicPartitions = args.length >= 2 ? Integer.parseInt(args[1]) : defaultTopicPartitions;
        final short replicationFactor = args.length >= 3 ? Short.parseShort(args[2]) : defaultReplicationFactor;

        Properties props = new Properties();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, serverAddr);

        //Creo un AdmicClient per impostare valori di un topic su Kafka
        AdminClient adminClient = AdminClient.create(props);

        //Prendo la lista dei topics
        ListTopicsResult listResult = adminClient.listTopics();
        //Prendo la lista di String dei topics
        Set<String> topicsNames = listResult.names().get();

        System.out.println("Available topics: " + topicsNames);

        if (topicsNames.contains(topicName)) {
            System.out.println("Deleting topic " + topicName);

            //Cancello il topicA
            DeleteTopicsResult delResult = adminClient.deleteTopics(Collections.singletonList(topicName));

            //Aspetto che cancello tutti i topic indicati (in questo caso sto cancellando solo topicA)
            delResult.all().get();
            System.out.println("Done!");
            // Wait for the deletion
            Thread.sleep(5000);
        }

        System.out.println("Adding topic " + topicName + " with " + topicPartitions + " partitions");

        //Creo un topic e indico: il nome, il #partitions e il fattore di replicazione di ogni partition
        NewTopic newTopic = new NewTopic(topicName, topicPartitions, replicationFactor);

        //Aggiungo il topic con le caratteristiche che ho indicato su Kafka
        CreateTopicsResult createResult = adminClient.createTopics(Collections.singletonList(newTopic));
        createResult.all().get();

        System.out.println("Done!");
    }
}