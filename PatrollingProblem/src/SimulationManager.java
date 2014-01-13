import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Arrays;

import au.com.bytecode.opencsv.CSVWriter;


public class SimulationManager {
	public final static int trials = 10;
	public final static int[] serviceRateConstants = {10,20,50};
	
	// Event generation period (Constant)
	public static Function constantEventPeriod = new Function() {
		public long function(long x) {
    		return Simulation.timeConstant / 2; // Two events are generated every tick
    	}
	};

	// Event generation period (Exponential)
	private static Exponential exponential = new Exponential(1); // Default as 1
	public static Function exponentialEventPeriod = new Function() {
		public long function(long x) {

			double exp = Simulation.timeConstant / exponential.nextExponential();
			exp = Math.ceil(exp); // Round up to prevent time of 0
			long longExp = (long) exp;
			
			// Display time in seconds
			if (Simulation.verbose) {
				System.out.print("Next event generated in ");
				System.out.printf("%.3f", (exp/1000));
				System.out.println(" seconds");
			}
			
			return longExp;
    	}
	};

	// Decreasing event value
	public static Function decreasingValue = new Function() {
		public long function(long x) {
    		return 2 * x; // Decrements by 1 value every 2 ticks
    	}
	};
	
	// Constant event value
	public static Function constantValue = new Function() {
		public long function(long x) {
    		return 0; // Decrements by 0
    	}
	};
	
	public static Function[] valueFunctions = {constantValue, decreasingValue};
	public static String[] valueFunctionStrings = {"Constant", "Decreasing"};

	public static void main(String[] args) throws IOException {
				
		// Progress bar
		int numberOfSimulations = 2 * valueFunctions.length * serviceRateConstants.length * 10 * trials;
		System.out.println("There will be " + numberOfSimulations + " simulations.");
		ProgressFrame progressFrame = new ProgressFrame(numberOfSimulations);
		
		// Writing data to CSV
		String userHomeFolder = System.getProperty("user.home");
		File file = new File(userHomeFolder, "Desktop/simulationData.csv");
		FileWriter fileWriter = new FileWriter(file);
		CSVWriter writer = new CSVWriter(new BufferedWriter(fileWriter), ',', CSVWriter.NO_QUOTE_CHARACTER);

		// Accelerate the time of a 'tick'
		Simulation.timeConstant = 10;
		
		// Formatting output
		DecimalFormat df = new DecimalFormat("#.###"); // 3 decimal places

		int simulationCount = 0;
		for (int agents = 1; agents <= 2; agents++) {
			
			for (int k = 0; k < valueFunctions.length; k++) {
				Function valueFunction = valueFunctions[k];
				
				String[] line = new String[]{"Agents:", String.valueOf(agents), "Event Value:", valueFunctionStrings[k]};
				writer.writeNext(line);
				writer.flushQuietly();

				for (int serviceRateConstant : serviceRateConstants) {
					
					line = new String[]{"f/" + String.valueOf(serviceRateConstant)};
					writer.writeNext(line);
					writer.flushQuietly();
					line = new String[]{"Mean", "TotalCollected", "Delay", "AverageDelay", "DeadEvents"};
					writer.writeNext(line);
					writer.flushQuietly();
					
					Fraction serviceRate = new Fraction(1, serviceRateConstant);

					for (float mean = 1.0f; mean < 6.0; mean += 0.5) {
						
						// TODO: 10 different constant event generation instead of mean
						
						// Set the exponential mean
						exponential.setMean(mean);

						// Average results
						double avgTotalCollected = 0;
						double avgDelay = 0;
						double avgAverageDelay = 0;
						double avgDeadEvents = 0;
					
						for (int i = 0; i < trials; i ++) {
							// Create a simulation
							Simulation simulation = new Simulation();
							simulation.isBlocking = true;
							simulation.totalAgents = agents;
							simulation.serviceRate = serviceRate;
//							simulation.totalEvents = 10000000; // 10,000,000
							simulation.totalEvents = 100;
							simulation.eventPeriod = exponentialEventPeriod;
							simulation.eventValueFunction = valueFunction;
							simulation.isVisible = false;
							simulation.simulate();
							
							// Gather statistics
							avgTotalCollected += simulation.getTotalPriorityCollected();
							avgDelay += simulation.getDelay();
							avgAverageDelay += simulation.getAverageDelay();
							avgDeadEvents += simulation.getDeadEventCount();
							
							// Close the visualization
							simulation.closeFrame();
							
							// Update the progress bar
							simulationCount ++;
							progressFrame.updateValue(simulationCount);
						}
						
						// Average statistics for all trials
						avgTotalCollected /= trials;
						avgDelay /= trials;
						avgAverageDelay /= trials;
						avgDeadEvents /= trials;
					
						// TODO: There is no mean if the eventGeneration is constant
						
						// Record results
						line = new String[]{df.format(mean), df.format(avgTotalCollected), df.format(avgDelay), df.format(avgAverageDelay), df.format(avgDeadEvents)};
						writer.writeNext(line);
						writer.flushQuietly();
						
						System.out.println("Simulation " + simulationCount + " ended.");
						System.out.println(Arrays.toString(line));
					}
					
					// Blank line
					line = new String[]{""};
					writer.writeNext(line);
					writer.flushQuietly();
				}
			}
		}
		
		writer.close();
		progressFrame.setVisible(false);
		progressFrame.dispose();
	}
}
