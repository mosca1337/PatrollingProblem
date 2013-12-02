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
		
		// Writer header
		// TODO: 'NumberOfAgents', 'eventChange(constant decreasing)' ' Mean' , 'totalCollected', 'delay', 'averageDelay', 'serviceTime (f/10)', deadEvents 
		String[] line = new String[]{"meanCollected", "totalEventsCollected", "totalDelay"};
		writer.writeNext(line);
		writer.flushQuietly();
	}
	
	public void writeSimulation(Simulation simulation) {
		
		long meanCollected = 0;
		long totalEventsCollected = 0;
		long totalDelay = 0;
		for (Agent agent : simulation.agents) {
			meanCollected += agent.totalPriorityCollected;
			totalEventsCollected += agent.liveEventsCollected;
			totalDelay += agent.totalDelay;
		}
		
		String[] line = new String[]{String.valueOf(meanCollected), String.valueOf(totalEventsCollected), String.valueOf(totalDelay)};
		writer.writeNext(line);
		writer.flushQuietly();
	}
	
	public void close() throws IOException {
		writer.close();
	}
}
