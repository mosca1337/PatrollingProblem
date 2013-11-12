import java.io.File;


public class SimulationManager {
	public final static int trials = 10;
	
	public static void main(String[] args) {

		for (int i = 0; i < trials; i ++) {
			for (int agentSpeed = 1000; agentSpeed < 3000; agentSpeed += 1000) {
				for (int eventSpeed = 500; eventSpeed < 1500; eventSpeed += 500) {
					
					String userHomeFolder = System.getProperty("user.home");
					File file = new File(userHomeFolder, "Desktop/simulationData.csv");
//					new Simulation(file, agentSpeed, eventSpeed);
				}
			}
		}
		// Part2 
		// TODO: variable time that it takes for agent to move

	}
}
