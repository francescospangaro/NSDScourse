package it.polimi.nsds.kafka;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class PopularTopicConsumer {
    private static final String topic = "inputTopic";
    private static final String serverAddr = "localhost:9092";
    private static final boolean autoCommit = true;
    private static final int autoCommitIntervalMs = 15000;
    private static final String offsetResetStrategy = "earliest";


    public static void main(String[] args) {
        String groupId = args[0];

        final Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, serverAddr);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);

        // TODO: complete the implementation by adding code here
        //If true, periodically commit to Kafka the offsets of messages already returned by the consumer.
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, String.valueOf(autoCommit));
        //The frequency in milliseconds that the consumer offsets are committed to Kafka. Relevant if ENABLE_AUTO_COMMIT_CONFIG is turned on.
        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, String.valueOf(autoCommitIntervalMs));
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, offsetResetStrategy);

        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, IntegerDeserializer.class.getName());


        try (KafkaConsumer<String, Integer> consumer = new KafkaConsumer<>(props)) {

            //Indico a quali topic faccio il subscribe e quindi per quali topic voglio essere notificato
            consumer.subscribe(Collections.singletonList(topic));


            final Map<String, Integer> countMap = new HashMap<>();
            while (true) {
                //Operazione bloccante dove leggo dei msgs
                final ConsumerRecords<String, Integer> records = consumer.poll(Duration.of(5, ChronoUnit.MINUTES));

                //Ho letto qualche msgs
                for (final ConsumerRecord<String, Integer> record : records) {

                    countMap.compute(record.key(), (k, v) -> v == null ? 1 : v + 1);

                    int max = countMap.values().stream().max(Integer::compareTo).orElseThrow();
                    System.out.println("Occurrences: " + max);
                    for (Map.Entry<String, Integer> m : countMap.entrySet()) {
                        if (m.getValue() == max)
                            System.out.println("Key: " + m.getKey());
                    }
                }
            }
        }
    }
}
