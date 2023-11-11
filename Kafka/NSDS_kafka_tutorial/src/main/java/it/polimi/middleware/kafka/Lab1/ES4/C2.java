package it.polimi.middleware.kafka.Lab1.ES4;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.IntegerSerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class C2 {

    //Non voglio che sia nello stesso gruppo di C1 perchè voglio che entrambi C1 e C2 leggono tutti i msgs
    private static final String defaultConsumerGroupId = "groupRW";

    //Consumer per topicA
    private static final String defaultInputTopic = "topicA";

    //Producer per topicB
    private static final String defaultOutputTopic = "topicB";

    private static final String serverAddr = "localhost:9092";

    //Deve usare la transactions
    private static final String producerTransactionalId = "forwarderTransactionalId";


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

        // Vogliamo essere noi a dire quando committare l'offset
        // The consumer does not commit automatically, but within the producer transaction
        consumerProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, String.valueOf(false));


        KafkaConsumer<String, Integer> consumer = new KafkaConsumer<>(consumerProps);
        consumer.subscribe(Collections.singletonList(inputTopic));

        // ------------
        // PRODUCER (per topicB)
        // ------------
        final Properties producerProps = new Properties();
        producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, serverAddr);
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, IntegerSerializer.class.getName());

        producerProps.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, producerTransactionalId); //Per fare transaction
        producerProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, String.valueOf(true)); //Voglio essere EOS

        final KafkaProducer<String, Integer> producer = new KafkaProducer<>(producerProps);

        producer.initTransactions();

        while (true) {
            final ConsumerRecords<String, Integer> records = consumer.poll(Duration.of(5, ChronoUnit.MINUTES));
            //Ho letto msgs dal topicA

            //Faccio partire una transaction sul producer
            producer.beginTransaction();

            //Oggetto per memorizzare il conteggio delle key
            KeyCounter kc = new KeyCounter();


            for (final ConsumerRecord<String, Integer> record : records) {
                //Incremento il contatore per questa key
                kc.incrementKeyCount(record.key());


                //Scrivo su topicB quello che ho letto dal topicA
                producer.send(new ProducerRecord<>(outputTopic, record.key(), kc.getKeyCount(record.key())));
                System.out.println("msgs received with key:" + record.key() + ", " + kc.getKeyCount(record.key()) );

                // The producer manually commits the offsets for the consumer within the transaction
                final Map<TopicPartition, OffsetAndMetadata> map = new HashMap<>();

                //Per ogni partitions del topicA che ho letto
                for (final TopicPartition partition : records.partitions()) {
                    final List<ConsumerRecord<String, Integer>> partitionRecords = records.records(partition);
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
}
