package io.datadynamics.kafka.connect.kafka.sink;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.source.SourceRecord;
import org.apache.kafka.connect.source.SourceTask;

import java.time.Duration;
import java.util.*;

public class KafkaSourceTask extends SourceTask {

    private KafkaConsumer<String, String> consumer;
    private String sourceTopic;
    private String targetTopic;
    private Integer pollIntervalMs;

    @Override
    public String version() {
        return new KafkaSourceConnector().version();
    }

    @Override
    public void start(Map<String, String> props) {
        String bootstrapServers = props.get("bootstrap.servers");
        String groupId = props.get("group.id");
        sourceTopic = props.get("source.topic");
        targetTopic = props.get("target.topic");
        pollIntervalMs = Integer.parseInt(props.get("poll.interval.ms"));

        Properties properties = new Properties();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");

        consumer = new KafkaConsumer<>(properties);
        consumer.subscribe(Collections.singletonList(sourceTopic));
    }

    @Override
    public List<SourceRecord> poll() throws InterruptedException {
        List<SourceRecord> records = new ArrayList<>();
        ConsumerRecords<String, String> consumerRecords = consumer.poll(Duration.ofMillis(pollIntervalMs));
        for (ConsumerRecord<String, String> record : consumerRecords) {
            SourceRecord sourceRecord = new SourceRecord(
                    Collections.singletonMap("source.topic", sourceTopic),  // Source 정보
                    Collections.singletonMap("offset", record.offset()),    // Offset
                    targetTopic,                                            // 전송할 Topic
                    null,                                                   // 파티션 번호
                    Schema.STRING_SCHEMA,                                   // 파티셔닝을 위한 키 스키마 (생략 가능)
                    record.key(),                                           // 파티셔닝을 위한 키 (생략 가능)
                    Schema.STRING_SCHEMA,                                   // 값의 스키마 (생략 가능)
                    record.value(),                                         // 원본 메시지의 실제 데이터
                    null,                                                   // Timestamp
                    (Iterable) getHeaders(record));                         // 원본 메시지의 헤더
            records.add(sourceRecord);
        }
        return records;
    }

    /**
     * Kafka Topic에서 수신한 메시지를 재전송하기 위해서 org.apache.kafka.connect.source.SourceRecord의 Header로 변경한다.
     */
    private static List<Header> getHeaders(ConsumerRecord<String, String> record) {
        List<Header> headers = new ArrayList<>();
        record.headers().forEach(header -> {
            headers.add(new RecordHeader(header.key(), header.value()));
        });
        return headers;
    }

    @Override
    public void stop() {
        consumer.close();
    }
}