import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;

import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import au.com.bytecode.opencsv.CSVWriter;


public class SimulationManager {
	public final static int trials = 10;
	public final static int totalAgents = 2;
	public final static int[] serviceRateConstants = {10,20,50};
	
	
	// TODO: even
	// TODO: even from 1.0 to 5.5 events per 'tick'
	// Event generation period (Even)
	private static float constantEventPeriodMean = 2; // Default as 2
	public static Function evenEventPeriod = new Function() {
		public long function(long x) {
    		return (long) Math.round(Simulation.timeConstant / constantEventPeriodMean); // Two events are generated every tick
    	}
	};

	// Event generation period (Exponential)
	private static Exponential exponential = new Exponential(1); // Default as 1
	public static Function exponentialEventPeriod = new Function() {
		public long function(long x) {

			double exponent = exponential.nextExponential();
			long period = (long) (Simulation.timeConstant * exponent);
			period = (long) Math.ceil(period); // Round up to prevent time of 0
			
			// Display time in 'ticks'
			if (Simulation.verbose) {
				System.out.print("Next event generated in ");
				System.out.printf("%.3f", exponent);
				System.out.print(" ticks (");
				System.out.println(period+" seconds)");
			}
			
			return period;
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
	// TODO: show decreasing function instead of 'Decreasing' f = f0 - 0.5t
	public static String[] valueFunctionStrings = {"Constant", "Decreasing"};
	
	public static Function[] periodFunctions = {evenEventPeriod, exponentialEventPeriod};

	public static void main(String[] args) throws IOException {
				
		// Progress bar
		int numberOfSimulations = periodFunctions.length * totalAgents * valueFunctions.length * serviceRateConstants.length * 10 * trials;
		System.out.println("There will be " + numberOfSimulations + " simulations.");
		ProgressFrame progressFrame = new ProgressFrame(numberOfSimulations);
		
		// Writing data to CSV
		String userHomeFolder = System.getProperty("user.home");
		File simulationsFolder = new File(userHomeFolder, "Desktop/Simulations");
		simulationsFolder.mkdirs();
		File simulationCSV = new File(simulationsFolder, "simulationData.csv");
		FileWriter fileWriter = new FileWriter(simulationCSV);
		CSVWriter writer = new CSVWriter(new BufferedWriter(fileWriter), ',', CSVWriter.NO_QUOTE_CHARACTER);

		// Writing PNG graphs
		File graphFolder = new File(simulationsFolder, "graphs");
		graphFolder.mkdirs();

		// Accelerate the time of a 'tick'
		Simulation.timeConstant = 100;
		
		// Formatting output
		DecimalFormat df = new DecimalFormat("#.###"); // 3 decimal places

		int simulationCount = 0;
		for (Function periodFunction : periodFunctions) {
			
			String periodFunctionString = "";
			if (periodFunction == evenEventPeriod) {
//				periodFunctionString = "f=" + new Double(constantEventPeriod.function(1) * Simulation.timeConstant).toString() + "t";
				periodFunctionString = "Even";
			} else if (periodFunction == exponentialEventPeriod) {
//				periodFunctionString = new Double(constantEventPeriod.function(1) * Simulation.timeConstant).toString();
				periodFunctionString = "Exponential";
			}
			
			for (int agents = 1; agents <= totalAgents; agents++) {
				
				for (int k = 0; k < valueFunctions.length; k++) {
					Function valueFunction = valueFunctions[k];
					
					// Write CSV table header
					String[] line = new String[]{};
					writer.writeNext(line);
					writer.flushQuietly();
					
					// Visual graph data set
					XYSeriesCollection eventsCollectedCollection = new XYSeriesCollection();
					XYSeriesCollection deadEventsCollection = new XYSeriesCollection();
					XYSeriesCollection averageDelayCollection = new XYSeriesCollection();
					XYSeriesCollection handledRateCollection = new XYSeriesCollection();

					for (int serviceRateConstant : serviceRateConstants) {
						
						line = new String[]{"Event Generation:", periodFunctionString, "Agents:", String.valueOf(agents), "Event Value:", valueFunctionStrings[k], "Service Rate:", "f/" + String.valueOf(serviceRateConstant)};
						writer.writeNext(line);
						writer.flushQuietly();
						line = new String[]{"Mean", "Events Collected", "Dead Events", "Weight Collected", "Delay", "AverageDelay", "Handled Rate"};
						writer.writeNext(line);
						writer.flushQuietly();
						
						// The service rate (Ex. 1/10 or 1/20)
						Fraction serviceRate = new Fraction(1, serviceRateConstant);
						
						String serviceRateString = "f/"+serviceRateConstant;
						
						// Data series for graphing
						XYSeries eventsCollectedSeries = new XYSeries(serviceRateString);
						XYSeries deadEventsSeries = new XYSeries(serviceRateString);
						XYSeries averageDelaySeries = new XYSeries(serviceRateString);
						XYSeries handledRateSeries = new XYSeries(serviceRateString);

						for (float mean = 1.0f; mean < 6.0; mean += 0.5) {
							
							// Set the exponential mean
							exponential.setMean(mean);
							
							// Set the constant mean
							constantEventPeriodMean = mean;

							// Average results
							int avgLiveEventsCollected = 0;
							double avgDeadEvents = 0;
							double avgWeightCollected = 0;
							double avgDelay = 0;
							double avgAverageDelay = 0;
							double avgHandledRate = 0;
						
							for (int i = 0; i < trials; i ++) {
								// Create a simulation
								Simulation simulation = new Simulation();
								simulation.totalAgents = agents;
								simulation.serviceRate = serviceRate;
//								simulation.totalEvents = 100000; // 100,000
//								simulation.totalEvents = 100;
								simulation.totalEvents = 10000;
								simulation.eventValueFunction = valueFunction;
								simulation.eventPeriod = periodFunction;
								simulation.isVisible = false;
								simulation.simulate();
								
								// Gather statistics
								avgLiveEventsCollected += simulation.getLiveEventsCollected();
								avgDeadEvents += simulation.getDeadEventCount();
								avgWeightCollected += simulation.getTotalPriorityCollected();
								avgDelay += simulation.getDelay();
								avgAverageDelay += simulation.getAverageDelay();
								avgHandledRate += simulation.getHandledRate();
								
								// Close the visualization
								simulation.closeFrame();
								
								// Update the progress bar
								simulationCount ++;
								progressFrame.updateValue(simulationCount);
							}
							
							// Average statistics for all trials
							avgLiveEventsCollected /= trials;
							avgDeadEvents /= trials;
							avgWeightCollected /= trials;
							avgDelay /= trials;
							avgAverageDelay /= trials;
							avgHandledRate /= trials;
						
							// Add data to series
							eventsCollectedSeries.add(mean, avgLiveEventsCollected);
							deadEventsSeries.add(mean, avgDeadEvents);
							averageDelaySeries.add(mean, avgDelay);
							handledRateSeries.add(mean, avgHandledRate);

							// Record a CSV line
							line = new String[]{df.format(mean), df.format(avgLiveEventsCollected), df.format(avgDeadEvents), df.format(avgWeightCollected), df.format(avgDelay), df.format(avgAverageDelay), df.format(avgHandledRate)};
							writer.writeNext(line);
							writer.flushQuietly();
							
							System.out.println("Simulation " + simulationCount + " ended.");
							System.out.println(Arrays.toString(line));
						}
						
						eventsCollectedCollection.addSeries(eventsCollectedSeries);
						deadEventsCollection.addSeries(deadEventsSeries);
						averageDelayCollection.addSeries(averageDelaySeries);
						handledRateCollection.addSeries(handledRateSeries);
						
						// Blank line
						line = new String[]{""};
						writer.writeNext(line);
						writer.flushQuietly();
					}

					LineChart eventsCollectedChart = new LineChart("Mean", "Events Collected", eventsCollectedCollection);
					LineChart deadEventsChart = new LineChart("Mean", "Dead Events", deadEventsCollection);
					LineChart averageDelayChart = new LineChart("Mean", "Average Delay", averageDelayCollection);
					LineChart handledRateChart = new LineChart("Mean", "Handled Rate", handledRateCollection);
					
					// Set tick size for handledRateChart
			        XYPlot xyPlot = (XYPlot) handledRateChart.chart.getPlot();
			        NumberAxis domain = (NumberAxis) xyPlot.getRangeAxis();
			        domain.setNumberFormatOverride(NumberFormat.getPercentInstance());
			        domain.setRange(0.00, 1.00);
			        domain.setTickUnit(new NumberTickUnit(0.05));
					
					File simulationFolder = new File(graphFolder, periodFunctionString+"/"+Integer.toString(agents)+"agents"+"/"+valueFunctionStrings[k]+"/");
					simulationFolder.mkdirs();
					
					File eventsCollectedFile = new File(simulationFolder, "eventsCollected.png");
					File deadEventsFile = new File(simulationFolder, "deadEvents.png");
					File averageDelayFile = new File(simulationFolder, "averageDelay.png");
					File handledRateFile = new File(simulationFolder, "handledRate.png");
					
					eventsCollectedChart.saveChartAsPNG(eventsCollectedFile);
					deadEventsChart.saveChartAsPNG(deadEventsFile);
					averageDelayChart.saveChartAsPNG(averageDelayFile);
					handledRateChart.saveChartAsPNG(handledRateFile);
				}
			}
		}
		
		writer.close();
		progressFrame.setVisible(false);
		progressFrame.dispose();
	}
}
