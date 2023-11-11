package it.polimi.middleware.kafka.Lab1.ES1;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Properties;

public class C1 {

    private static final String defaultGroupId = "groupC1";
    private static final String defaultTopic = "topicA";

    private static final String serverAddr = "localhost:9092";
    private static final boolean autoCommit = true;
    private static final int autoCommitIntervalMs = 15000;

    // Default is "latest": try "earliest" instead
    private static final String offsetResetStrategy = "latest";

    public static void main(String[] args) {
        // If there are arguments, use the first as group and the second as topic.
        // Otherwise, use default group and topic.
        String groupId = args.length >= 1 ? args[0] : defaultGroupId;
        String topic = args.length >= 2 ? args[1] : defaultTopic;

        final Properties props = new Properties();

        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, serverAddr);
        //Indico il consumer group (consumer con lo stesso groupId saranno nello stesso consumer group)
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);

        //If true, periodically commit to Kafka the offsets of messages already returned by the consumer.
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, String.valueOf(autoCommit));
        //The frequency in milliseconds that the consumer offsets are committed to Kafka. Relevant if ENABLE_AUTO_COMMIT_CONFIG is turned on.
        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, String.valueOf(autoCommitIntervalMs));

        //Specifica cosa dovrebbe succedere se non c'è un offset valido (cioè un punto di lettura valido)
        //per un consumer all'interno di un topic.
        //earliest:
        //  Kafka inizierà a leggere dal primo messaggio disponibile nel topic.
        //latest:
        //  Kafka inizierà a leggere solo i nuovi messaggi che arrivano dopo il momento in cui il consumer si è iscritto al topic.
        //none:
        //  Kafka solleverà un'eccezione indicando che non ci sono offset validi disponibili.
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, offsetResetStrategy);

        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);

        //Indico a quali topic faccio il subscribe e quindi per quali topic voglio essere notificato
        consumer.subscribe(Collections.singletonList(topic));


        while (true) {
            //Operazione bloccante dove leggo dei msgs
            final ConsumerRecords<String, String> records = consumer.poll(Duration.of(5, ChronoUnit.MINUTES));

            //Ho letto qualche msgs
            for (final ConsumerRecord<String, String> record : records) {
                //Stampo tutti i messaggi letti
                System.out.println("\tKey: " + record.key() +
                        "\tValue: " + record.value() +
                        "\tOffset: " + record.offset()
                );
            }
        }

    }

}
