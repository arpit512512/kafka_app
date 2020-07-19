package com.github.arpit512.kafka.app1;

import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.concurrent.ExecutionException;

public class ProducerWithCallback {

  private static Logger logger = LoggerFactory.getLogger(ProducerWithCallback.class);

  public static void main(String[] arg) throws ExecutionException, InterruptedException {
    System.out.println("Hello welcome to kafka app version 1!");
    app();
  }

  private static void app() throws ExecutionException, InterruptedException {

    String bootstrapServers = "127.0.0.1:9092";

    // Create producer properties.
    Properties properties = new Properties();
    properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

    // Create the producer.
    KafkaProducer<String, String> producer = new KafkaProducer<String, String>(properties);

    for (int i=0; i<10; i++) {
      // Create a producer record.
      String topic = "first_topic";
      String key = "id_" + Integer.toString(i);
      String value = "Hi from intellij " + Integer.toString(i);
      ProducerRecord<String, String> record =
      new ProducerRecord<String, String>(topic, key, value);

      logger.info("Key: " + key);
      // id_0 --> par 1
      // id_1 --> par 0
      // id_2 --> par 2
      // id_3 --> par 0
      // id_4 --> par 2
      // id_5 --> par 2

      // Send data - async.
      producer.send(record, new Callback() {
        @Override
        public void onCompletion(RecordMetadata recordMetadata, Exception e) {
          if (e == null) {
            // the record was successfully sent.
            logger.info("Received new metadata");
            logger.info("Received new metadata" + "\n" +
            "Topic: " + recordMetadata.topic() + "\n" +
            "Partition: " + recordMetadata.partition() + "\n" +
            "Offset" + recordMetadata.offset() + "\n" +
            "Timestamp" + recordMetadata.timestamp());

          } else {
            logger.error("Error while producing", e);
          }
        }
      }).get();
    }
    producer.flush();
    producer.close();
  }

}
