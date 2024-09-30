package io.datadynamics.kafka.connect.kafka.source;

import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.connect.connector.Task;
import org.apache.kafka.connect.source.SourceConnector;

import java.util.Map;
import java.util.List;

public class KafkaSourceConnector extends SourceConnector {

    private String bootstrapServers;
    private String groupId;
    private String sourceTopic;
    private String targetTopic;
    private String pollIntervalMs;

    @Override
    public String version() {
        return "1.0";
    }

    @Override
    public void start(Map<String, String> props) {
        bootstrapServers = props.get("bootstrap.servers");
        groupId = props.get("group.id");
        sourceTopic = props.get("source.topic");
        targetTopic = props.get("target.topic");
        pollIntervalMs = props.get("poll.interval.ms");
    }

    @Override
    public Class<? extends Task> taskClass() {
        return KafkaSourceTask.class;
    }

    @Override
    public List<Map<String, String>> taskConfigs(int maxTasks) {
        Map<String, String> config = Map.of(
                "bootstrap.servers", bootstrapServers,
                "group.id", groupId,
                "source.topic", sourceTopic,
                "target.topic", targetTopic,
                "poll.interval.ms", pollIntervalMs
        );
        return List.of(config);
    }

    @Override
    public void stop() {
        // Clean up resources here
    }

    @Override
    public ConfigDef config() {
        return new ConfigDef()
                .define("bootstrap.servers", ConfigDef.Type.STRING, ConfigDef.Importance.HIGH, "Kafka Bootstrap Servers")
                .define("group.id", ConfigDef.Type.STRING, ConfigDef.Importance.HIGH, "Kafka Consumer Group ID")
                .define("source.topic", ConfigDef.Type.STRING, ConfigDef.Importance.HIGH, "Source Kafka Topic")
                .define("target.topic", ConfigDef.Type.STRING, ConfigDef.Importance.HIGH, "Target Kafka Topic")
                .define("poll.interval.ms", ConfigDef.Type.LONG, 1000, ConfigDef.Importance.MEDIUM, "The interval in milliseconds between poll calls.");
    }
}
