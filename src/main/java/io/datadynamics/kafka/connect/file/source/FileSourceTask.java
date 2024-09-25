package io.datadynamics.kafka.connect.file.source;

import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.source.SourceRecord;
import org.apache.kafka.connect.source.SourceTask;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FileSourceTask extends SourceTask {
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
	public List<SourceRecord> poll() throws InterruptedException {
		ArrayList<SourceRecord> records = new ArrayList<>();
		try {
			List<String> lines = Files.readAllLines(Paths.get(filename));
			lines.forEach(line -> {
				SourceRecord sourceRecord = new SourceRecord(null, null, "my_topic", Schema.STRING_SCHEMA, line);
				records.add(sourceRecord);
			});

		} catch (IOException e) {
			e.printStackTrace();
		}

		return records;
	}

	@Override
	public void stop() {
		// Handle cleanup
	}
}