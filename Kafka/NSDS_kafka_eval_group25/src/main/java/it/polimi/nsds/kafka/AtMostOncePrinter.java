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
import java.util.Properties;

public class AtMostOncePrinter {
    private static final String topic = "inputTopic";
    private static final String serverAddr = "localhost:9092";
    private static final int threshold = 500;
    private static final String offsetResetStrategy = "latest";


    public static void main(String[] args) {
        String groupId = args[0];

        final Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, serverAddr);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);

        // TODO: complete the implementation by adding code here
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, String.valueOf(false));

        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, offsetResetStrategy);

        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, IntegerDeserializer.class.getName());


        try (KafkaConsumer<String, Integer> consumer = new KafkaConsumer<>(props)) {

            //Indico a quali topic faccio il subscribe e quindi per quali topic voglio essere notificato
            consumer.subscribe(Collections.singletonList(topic));

            while (true) {
                //Operazione bloccante dove leggo dei msgs
                final ConsumerRecords<String, Integer> records = consumer.poll(Duration.of(5, ChronoUnit.MINUTES));

                //Dato che vuole al massimo una stampa per messaggio, committiamo gli offset subito dopo la poll così,
                // in caso di crash del sistema alla riconnessione leggeremo i messaggi con offset non ancora committato.
                consumer.commitSync();

                //Ho letto qualche msgs
                for (final ConsumerRecord<String, Integer> record : records) {

                    if (record.value() > threshold) {
                        //Stampo i messaggi letti con value > threshold
                        System.out.print("Consumer group: " + groupId + "\t");
                        System.out.println("Partition: " + record.partition() +
                                "\tOffset: " + record.offset() +
                                "\tKey: " + record.key() +
                                "\tValue: " + record.value()
                        );
                    }
                }
            }
        }

    }
}
