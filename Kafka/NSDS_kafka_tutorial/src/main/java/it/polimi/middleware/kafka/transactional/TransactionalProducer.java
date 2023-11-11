package it.polimi.middleware.kafka.transactional;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.*;

public class TransactionalProducer {
    private static final String defaultTopic = "topicA";

    private static final int numMessages = 100000;
    private static final int waitBetweenMsgs = 500;

    //Fondamentali se si vogliono utilizzare transaction per un producer
    private static final String transactionalId = "myTransactionalId";

    private static final String serverAddr = "localhost:9092";

    public static void main(String[] args) {
        // If there are no arguments, publish to the default topic
        // Otherwise publish on the topics provided as argument
        List<String> topics = args.length < 1 ?
                Collections.singletonList(defaultTopic) :
                Arrays.asList(args);

        final Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, serverAddr);

        //Specifico qui l'id della transaction
        props.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, transactionalId);


        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        final KafkaProducer<String, String> producer = new KafkaProducer<>(props);
        final Random r = new Random();

        // This must be called before any method that involves transactions
        producer.initTransactions();

        for (int i = 0; i < numMessages; i++) {
            final String topic = topics.get(r.nextInt(topics.size()));
            final String key = "Key" + r.nextInt(1000);
            final String value = "Val" + i;

            System.out.println(
                    "Topic: " + topic +
                    "\tKey: " + key +
                    "\tValue: " + value
            );

            final ProducerRecord<String, String> record = new ProducerRecord<>(topic, key, value);

            //Inizio la transaction
            producer.beginTransaction();

            producer.send(record);

            //Committa 1 messaggio su 2 => 1 msg committed e 1 msg aborted
            if (i % 2 == 0) {
                producer.commitTransaction(); //Chiudo la transaction facendo commit
            } else {
                // If not flushed, aborted messages are deleted from the outgoing buffer
                producer.flush();
                producer.abortTransaction(); //Chiudo la transaction facendo abort
            }

            try {
                Thread.sleep(waitBetweenMsgs);
            } catch (final InterruptedException e) {
                e.printStackTrace();
            }
        }

        producer.close();
    }
}