import java.io.File;


public class SimulationManager {
	public final static int trials = 10;
	
	public static void main(String[] args) {
		// Decreasing event value
		Function eventValue = new Function() {
			public long function(long x) {
	    		return 2000 * x; // Decrements by 1 value every 2 seconds
	    	}
		};
		
		for (int i = 0; i < trials; i ++) {
			for (int agentSpeed = 1000; agentSpeed < 3000; agentSpeed += 1000) {
				for (int eventSpeed = 500; eventSpeed < 1500; eventSpeed += 500) {
					
					// TODO: simulation for 1 agent, simulation for 2 agents
					// TODO: constant priority vs decreasing priority
					// TODO: agent speed is priority/10 priority/20 priority/50
					
					// TODO: 10,000,000 events generated
					int maxPriority = Simulation.maxPriority;
					
					String userHomeFolder = System.getProperty("user.home");
					File file = new File(userHomeFolder, "Desktop/simulationData.csv");
//					new Simulation(file, agentSpeed, eventSpeed, 1000);
				}
			}
		}
		// Part2 
		// TODO: variable time that it takes for agent to move

	}
}
