package it.polimi.middleware.kafka.Lab1.ES1;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Properties;
import java.util.stream.Collectors;

public class C2 {

    //Non voglio che sia nello stesso gruppo di C1 perchè voglio che entrambi C1 e C2 leggono tutti i msgs
    private static final String defaultConsumerGroupId = "groupC2";

    //Consumer per topicA
    private static final String defaultInputTopic = "topicA";

    //Producer per topicB
    private static final String defaultOutputTopic = "topicB";

    private static final String serverAddr = "localhost:9092";

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
        consumerProps.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed" );

        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerProps);
        consumer.subscribe(Collections.singletonList(inputTopic));

        // ------------
        // PRODUCER (per topicB)
        // ------------
        final Properties producerProps = new Properties();
        producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, serverAddr);
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        final KafkaProducer<String, String> producer = new KafkaProducer<>(producerProps);


        while (true) {
            final ConsumerRecords<String, String> records = consumer.poll(Duration.of(5, ChronoUnit.MINUTES));
            //Ho letto msgs dal topicA


            for (final ConsumerRecord<String, String> record : records) {

                //Processo il valore del msg rimuovendo tutti i Char Maiuscoli
                String processed = record.value();
                String output;

                /*for(int i=0;i<processed.length();i++){
                    if(!(processed.charAt(i)>='A' && processed.charAt(i)<='Z'))
                        output+=processed.charAt(i);
                }*/

                output = processed.chars()
                        .filter( x -> x == Character.toLowerCase(x))
                        .mapToObj(Character::toString)
                        .collect(Collectors.joining());


                //Scrivo su topicB quello che ho letto dal topicA
                producer.send(new ProducerRecord<>(outputTopic, record.key(), output));
                if(output.length() == 0){
                    System.out.println("String sent: none, String received: " + record.value());
                }else{
                    System.out.println("String sent:" + output + ", String received: " + record.value());
                }

            }

        }
    }
}
