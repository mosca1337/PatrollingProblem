import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;

import au.com.bytecode.opencsv.CSVWriter;


public class EventWriter {
	
	private CSVWriter writer;

	public EventWriter(File fileName) throws IOException {
		FileWriter fileWriter = new FileWriter(fileName);
		writer = new CSVWriter(new BufferedWriter(fileWriter), ',', CSVWriter.NO_QUOTE_CHARACTER);
	}
	
	public void writeEvents(Agent agent, Set<Event> events) {
		
		for (Event event : events) {
			String timeCollected = new Long(event.timeCollected.getTime()).toString();
			String eventPriority = new Integer(event.priority).toString();
			String totalCollected = new Integer(agent.totalCollected).toString();
			
			String[] line = new String[]{timeCollected, agent.name, eventPriority, totalCollected};
			writer.writeNext(line);	
		}
		
		writer.flushQuietly();
	}
	
	public void close() throws IOException {
		writer.close();
	}
}
