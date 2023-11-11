package it.polimi.middleware.kafka.atomic_forward;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class AtomicForwarder {
    private static final String defaultConsumerGroupId = "groupA";

    //Consumer per topicA
    private static final String defaultInputTopic = "topicA";

    //Producer per topicB
    private static final String defaultOutputTopic = "topicB";

    private static final String serverAddr = "localhost:9092";

    //Deve usare la transactions
    private static final String producerTransactionalId = "forwarderTransactionalId";


    //Voglio essere un consumer per il topicA e essere un producer per il topicB
    //Leggo da topicA e aggiungo quello che leggo al topicB
    //se crash non voglio che in topicB ci siano doppi msgs o msgs mancanti
    //  => dico io quando e come aggiornare gli offset e lo faccio in transaction
    //      (in transaction ci sarà il publish del msg in topicB e il commit dell'offsets)
    public static void main(String[] args) {
        // If there are arguments, use the first as group, the second as input topic, the third as output topic.
        // Otherwise, use default group and topics.
        String consumerGroupId = args.length >= 1 ? args[0] : defaultConsumerGroupId;
        String inputTopic = args.length >= 2 ? args[1] : defaultInputTopic;
        String outputTopic = args.length >= 3 ? args[2] : defaultOutputTopic;

        //  ------------
        // CONSUMER (per topicA)
        // ------------
        final Properties consumerProps = new Properties();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, serverAddr);
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroupId);

        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerProps.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed");

        // Vogliamo essere noi a dire quando committare l'offset
        // The consumer does not commit automatically, but within the producer transaction
        consumerProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, String.valueOf(false));

        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerProps);
        consumer.subscribe(Collections.singletonList(inputTopic));

        // ------------
        // PRODUCER (per topicB)
        // ------------
        final Properties producerProps = new Properties();
        producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, serverAddr);
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        producerProps.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, producerTransactionalId); //Per fare transaction
        producerProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, String.valueOf(true)); //Voglio essere EOS

        final KafkaProducer<String, String> producer = new KafkaProducer<>(producerProps);

        producer.initTransactions();

        while (true) {
            final ConsumerRecords<String, String> records = consumer.poll(Duration.of(5, ChronoUnit.MINUTES));
            //Ho letto dal topicA

            //Faccio partire una transaction sul producer
            producer.beginTransaction();

            for (final ConsumerRecord<String, String> record : records) {
                System.out.println("Partition: " + record.partition() +
                        "\tOffset: " + record.offset() +
                        "\tKey: " + record.key() +
                        "\tValue: " + record.value()
                );
                //Scrivo su topicB quello che ho letto dal topicA
                producer.send(new ProducerRecord<>(outputTopic, record.key(), record.value()));
            }
            //Ho scritto tutto su topicB

            //Faccio l'update manuale dei msgs che ho letto su topicA e che ho scritto su topicB
            //=> Creo una map in qui dico per ogni partition (del topicA) quel è l'offset

            // The producer manually commits the offsets for the consumer within the transaction
            final Map<TopicPartition, OffsetAndMetadata> map = new HashMap<>();

            //Per ogni partitions del topicA che ho letto
            for (final TopicPartition partition : records.partitions()) {
                final List<ConsumerRecord<String, String>> partitionRecords = records.records(partition);
                final long lastOffset = partitionRecords.get(partitionRecords.size() - 1).offset();
                map.put(partition, new OffsetAndMetadata(lastOffset + 1));
            }

            //Commit gli offsets per il topicA
            producer.sendOffsetsToTransaction(map, consumer.groupMetadata());

            //Committo la transaction garantendo END TO END Semantic
            producer.commitTransaction();
        }
    }
}