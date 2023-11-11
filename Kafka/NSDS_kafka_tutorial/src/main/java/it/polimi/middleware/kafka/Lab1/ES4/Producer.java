package it.polimi.middleware.kafka.Lab1.ES4;

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
1) In caso di fallimento la singola istanza di C2 perderà tutti i conteggi delle keys che ha effettuato
   fino a quel momento. Questo perchè l'oggetto utilizzato per memorizzare le coppie <Key,Counter> è salvato in locale
   sul consumer C2 e non memorizzato su qualche topic speciale (store topic) di kafka.
   Il conteggio ripartirà da 0.

2) L'implementazione non garantisce EOS per il conteggio e l'inserimento dei valori <key,counter> sul topicB.
   Questo significa che 1 msg viene letto esattamente 1 volta e di conseguenza il contatore per la sua key viene
   aggiornato solo una 1.
   Rimane però il problema dell'utilizzo della classe KeyCounter che al restart riparte con i contatori tutti a 0 in quanto
   non recupera lo stato salvato sul topicB.

3) La risposta (1) non cambia, dato che kafka garantisce che i messaggi con una data key saranno sempre memorizzati
   alla stessa partizione, quindi alla stessa istanza di kafka.
   La risposta (2) non cambia.
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
        // Idempotence = exactly once semantics between the producer and the partition
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, String.valueOf(true));

        //Definisco di che classe sono i <key,value>
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        final KafkaProducer<String, String> producer = new KafkaProducer<>(props);
        final Random r = new Random();


        for (int i = 0; i < numMessages; i++) {
            final String topic = topics.get(r.nextInt(topics.size()));
            //msg con la stessa key andranno sicuro nella stessa partition

            String value = StringGenerator.generateRandomString(r.nextInt(20));
            final String key = "Key" + r.nextInt(10);


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