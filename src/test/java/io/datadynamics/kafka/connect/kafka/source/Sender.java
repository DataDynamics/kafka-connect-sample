package io.datadynamics.kafka.connect.kafka.source;

import io.datadynamics.kafka.connect.util.IOUtils;
import io.datadynamics.kafka.connect.util.resource.DefaultResourceLoader;
import io.datadynamics.kafka.connect.util.resource.Resource;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Sender {

    public static void main(String[] args) throws IOException {
        Properties kafkaProps = new Properties();
        kafkaProps.put("bootstrap.servers", "10.0.1.67:9092,10.0.1.68:9092,10.0.1.69:9092");
        kafkaProps.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        kafkaProps.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        kafkaProps.put("acks", "all");
        kafkaProps.put("enable.idempotence", "false");
        kafkaProps.put("delivery.timeout.ms", "600000");

        KafkaProducer<String, String> producer = new KafkaProducer<>(kafkaProps);

        DefaultResourceLoader loader = new DefaultResourceLoader();
        Resource resource = loader.getResource("classpath:test-message.json");
        InputStream is = resource.getInputStream();
        String json = new String(IOUtils.toByteArray(is));
        IOUtils.closeQuietly(is);

        System.out.println(json);

        ProducerRecord<String, String> record = new ProducerRecord<>("all-messages", json);

        int messages = 100;

        try {
            for (int i = 0; i < messages; i++) {
                producer.send(record, new Callback() {
                    @Override
                    public void onCompletion(RecordMetadata metadata, Exception exception) {
                        if (exception != null) {
                            System.out.println("onCompletion, exception is not null");
                            System.err.println("Failed to send message: " + exception.getMessage());
                            exception.printStackTrace();
                        } else {
                            System.out.println("Message sent to topic " + metadata.topic() + " partition " + metadata.partition() + " offset " + metadata.offset());
                        }
                    }
                });

                Thread.sleep(1000);
            }
        } catch (Exception e) {
            System.out.println("Exception occurred.");
            System.err.println("Error while sending message: " + e.getMessage());
            e.printStackTrace();
        } finally {
            producer.close();
        }
    }
}
