package io.datadynamics.kafka.connect.file.source;

import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.connect.source.SourceConnector;
import org.apache.kafka.connect.source.SourceTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FileSourceConnector extends SourceConnector {

	private String filename;

	@Override
	public String version() {
		return "1.0.0";
	}

	@Override
	public void start(Map<String, String> props) {
		filename = props.get("file");
	}

	@Override
	public Class<? extends SourceTask> taskClass() {
		return FileSourceTask.class;
	}

	@Override
	public List<Map<String, String>> taskConfigs(int maxTasks) {
		List<Map<String, String>> configs = new ArrayList<>();
		configs.add(Map.of("file", filename));
		return configs;
	}

	@Override
	public void stop() {
		// Handle cleanup
	}

	@Override
	public ConfigDef config() {
		return new ConfigDef().define(
				"file",
				ConfigDef.Type.STRING,
				ConfigDef.Importance.HIGH,
				"Source file"
		);
	}
}