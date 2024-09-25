package io.datadynamics.kafka.connect.file.sink;

import org.apache.kafka.connect.sink.SinkRecord;
import org.apache.kafka.connect.sink.SinkTask;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;

public class FileSinkTask extends SinkTask {

	private String filename;
	private FileWriter writer;

	@Override
	public String version() {
		return "1.0.0";
	}

	@Override
	public void start(Map<String, String> props) {
		filename = props.get("file");
		try {
			writer = new FileWriter(filename, true);
		} catch (IOException e) {
			throw new RuntimeException("Error opening file for writing", e);
		}
	}

	@Override
	public void put(Collection<SinkRecord> records) {
		for (SinkRecord record : records) {
			try {
				writer.write(record.value().toString() + "\n");
			} catch (IOException e) {
				throw new RuntimeException("Error writing to file", e);
			}
		}
		try {
			writer.flush();
		} catch (IOException e) {
			throw new RuntimeException("Error flushing file writer", e);
		}
	}

	@Override
	public void stop() {
		try {
			writer.close();
		} catch (IOException e) {
			throw new RuntimeException("Error closing file writer", e);
		}
	}

}