package it.polimi.middleware.kafka.Lab1.ES1;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;



/*
-----------------------------------------------------------
QUESTION ANSWER
-----------------------------------------------------------

1) C1 and C2 will receive the same msgs because they are in different consumer groups.
   Therefore, consumers are independent, when one crashes the other one keeps going without any problems.

2) When a Consumer reconnects he will receive msgs from the point he disconnected.
    By doing so, C1 crashes and when he reconnects he will start to process msgs from where he crashed.
    Since the producer is not idempotent some messages could be skipped or duplicated
 */
public class Producer {
    private static final String defaultTopic = "topicA";

    private static final int numMessages = 100000;
    private static final int waitBetweenMsgs = 1000;
    private static final boolean waitAck = true;

    private static final String serverAddr = "localhost:9092";

    public static void main(String[] args) {
        // If there are no arguments, publish to the default topic
        // Otherwise publish on the topics provided as argument
        List<String> topics = args.length < 1 ?
                Collections.singletonList(defaultTopic) :
                Arrays.asList(args);

        //Imposto i parametri per il KafkaProducer
        final Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, serverAddr);
        //Definisco di che classe sono i <key,value>
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        final KafkaProducer<String, String> producer = new KafkaProducer<>(props);
        final Random r = new Random();


        for (int i = 0; i < numMessages; i++) {
            final String topic = topics.get(r.nextInt(topics.size()));
            //msg con la stessa key andranno sicuro nella stessa partition

            String value = StringGenerator.generateRandomString(r.nextInt(20));
            final String key = "Key" + r.nextInt(1000);


            System.out.println(
                    "Topic: " + topic +
                            "\tKey: " + key +
                            "\tValue: " + value
            );

            //Aggiungo un nuovo ProducerRecord indicando topic, key e value
            //aggiungerà il msg per questo topic in una partition che vuole lui
            final ProducerRecord<String, String> record = new ProducerRecord<>(topic, key, value);
            final Future<RecordMetadata> future = producer.send(record);

            if (waitAck) {
                try {
                    //Aspetto ACK che mi indica che il msg è stato aggiunto in Kafka
                    RecordMetadata ack = future.get();
                    System.out.println("Ack for topic " + ack.topic() + ", partition " + ack.partition() + ", offset " + ack.offset());
                } catch (InterruptedException | ExecutionException e1) {
                    e1.printStackTrace();
                }
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