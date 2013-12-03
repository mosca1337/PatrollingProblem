import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import au.com.bytecode.opencsv.CSVWriter;


public class SimulationManager {
	public final static int eventsGenerated = 10000000; // 10,000,000
	public final static int trials = 10;
	public final static int[] agentConstants = {10,20,50};
	
	// Event generation period
	public static Function eventPeriod = new Function() {
		public long function(long x) {
			// TODO: exponential distribution in MM1
    		return 2000/(x+1) + 100;
    	}
	};
	
	// Constant event period
	public static Function eventPeriodConstant = new Function() {
		public long function(long x) {
    		return 500;
    	}
	};

	// Decreasing event value
	public static Function eventValueDecreasing = new Function() {
		public long function(long x) {
    		return 2000 * x; // Decrements by 1 value every 2 seconds
    	}
	};
	
	// Constant event value
	public static Function eventValueConstant = null;

	public static void main(String[] args) throws IOException {
				
		String userHomeFolder = System.getProperty("user.home");
		File file = new File(userHomeFolder, "Desktop/simulationData.csv");
		FileWriter fileWriter = new FileWriter(file);
		CSVWriter writer = new CSVWriter(new BufferedWriter(fileWriter), ',', CSVWriter.NO_QUOTE_CHARACTER);

		for (int agents = 1; agents <= 2; agents++) {
			String[] line = new String[]{"Agents:", String.valueOf(agents), "Priority", "Constant"};
			writer.writeNext(line);
			writer.flushQuietly();

			for (int agentConstant : agentConstants) {
				line = new String[]{"f/" + String.valueOf(agentConstant)};
				writer.writeNext(line);
				writer.flushQuietly();
				line = new String[]{"Mean", "TotalCollected", "Delay", "AverageDelay", "DeadEvents"};
				writer.writeNext(line);
				writer.flushQuietly();

				for (float mean = 1.0f; mean < 6.0; mean += 0.5) {

					// Average results
					int avgTotalCollected = 0;
					double avgDelay = 0;
					double avgAverageDelay = 0;
					double avgDeadEvents = 0;
				
					for (int i = 0; i < trials; i ++) {
						Simulation simulation = new Simulation(eventPeriodConstant, agentConstant, 2, eventPeriod, 500);
						simulation.simulate();
						
						avgTotalCollected += simulation.getTotalPriorityCollected();
						avgDelay += simulation.getDelay();
						avgAverageDelay += simulation.getAverageDelay();
						avgDeadEvents += simulation.getDeadEventCount();
						
						simulation.closeFrame();
					}
					avgTotalCollected /= trials;
					avgDelay /= trials;
					avgAverageDelay /= trials;
					avgDeadEvents /= trials;
				
					// Record results
					line = new String[]{String.valueOf(mean), String.valueOf(avgTotalCollected), String.valueOf(avgDelay), String.valueOf(avgAverageDelay),  String.valueOf(avgDeadEvents)};
					writer.writeNext(line);
					writer.flushQuietly();
				}
				
				// Blank line
				line = new String[]{""};
				writer.writeNext(line);
				writer.flushQuietly();
			}
		}
		
		writer.close();
	}
}
