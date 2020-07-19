package com.github.arpit512.kafka.app1;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

public class Producer {

  public static void main(String[] arg) {
    System.out.println("Hello welcome to kafka app version 1!");
    app();
  }

  private static void app(){

    String bootstrapServers = "127.0.0.1:9092";

    // Create producer properties.
    Properties properties = new Properties();
    properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

    // Create the producer.
    KafkaProducer<String, String> producer = new KafkaProducer<String, String>(properties);

    // Create a producer record.
    ProducerRecord<String, String> record =
      new ProducerRecord<String, String>("first_topic", "Hi from intellij");

    // Send data - async.
    producer.send(record);
    producer.flush();
    producer.close();
  }
}
