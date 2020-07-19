package com.github.arpit512.app2;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;

public class ElasticSearchConsumer {

  public static RestHighLevelClient createClient() {
    String hostname = "nice-1-3373077473.us-east-1.bonsaisearch.net";
    String username = "fh515z64z6";
    String password = "vkthk64gxe";

    final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
    credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(
      username, password));

    RestClientBuilder builder = RestClient.builder(new HttpHost(hostname, 443, "https"))
      .setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
        @Override
        public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpAsyncClientBuilder) {
          return httpAsyncClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
        }});

    RestHighLevelClient client = new RestHighLevelClient(builder);
    return client;
  }

  public static KafkaConsumer<String, String> createConsumer(String topic) {
    String bootstrapServers = "127.0.0.1:9092";
    String groupId = "kafka-elastic-search-group";

    // Create consumer properties.
    Properties properties = new Properties();
    properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
    properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
    properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId);
    properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

    // Create the consumer.
    KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(properties);

    // subscribe to our topics.
    consumer.subscribe(Arrays.asList(topic));
    return consumer;
  }

  public static void main(String[] args) throws IOException, InterruptedException {
    Logger logger = LoggerFactory.getLogger(ElasticSearchConsumer.class.getName());
    RestHighLevelClient client = createClient();

    String topic = "twitter_tweets";
    KafkaConsumer<String, String> consumer = createConsumer(topic);

    // poll for new data.
    while (true) {
      logger.info("Starting polling for records.");
      Thread.sleep(2000);
      ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
      for (ConsumerRecord<String, String> record: records) {
        // Insert data into elastic search.
        logger.info("Key: " + record.key() + ", Value: " + record.value());
        String jsonString = "{\"message: \": \"" + record.value() + "\"}";
        logger.info("Json: " + jsonString);
        IndexRequest indexRequest = new IndexRequest(
          "twitter", "tweets").source(jsonString, XContentType.JSON);
        IndexResponse indexResponse = client.index(indexRequest, RequestOptions.DEFAULT);
        String id = indexResponse.getId();
        logger.info("index: " + id);
        Thread.sleep(1000);
      }
    }

    //client.close();
  }

}