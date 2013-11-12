import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;


public class Simulation {
	public final int maxPriority = 10;
	
	private EventWriter eventWriter = null;
	
	public Function agentPeriod;
	public Function eventPeriod;
	
	public SimulationFrame graphFrame;
	public EventGraph graph;
	public Set<Agent> agents;

	public static void main(String[] args) {
		
		// Simulation file
		String userHomeFolder = System.getProperty("user.home");
		File file = new File(userHomeFolder, "Desktop/simulationData.csv");
		
		// Agent period
		Function agentPeriod = new Function() {
			public long function(long x) {
//	    		return 3000;
	    		return 5000/(x+1) + 500;
	    	}
		};
		
		// Event generation period
		Function eventPeriod = new Function() {
			public long function(long x) {
//	    		return 500;
	    		return 2000/(x+1) + 100;
	    	}
		};
			
		new Simulation(file, agentPeriod, eventPeriod);
	}
	
	public Simulation(File dataFile, Function agentPeriod, Function eventPeriod) {
		super();
		this.agentPeriod = agentPeriod;
		this.eventPeriod = eventPeriod;
		
		graph = new EventGraph(5,5);
		System.out.println(graph);
		
		Set<EventEdge> topHalf = graph.getEdges(0,0,2,4);
		Set<EventEdge> bottomHalf = graph.getEdges(2,0,4,4);
		
		agents = new HashSet<Agent>();
		
		// Create an agent in the top left of the graph
		Vertex topLeftVertex = graph.vertexArray[0][0];
		Agent agentA = new Agent("A", graph, agentPeriod, topLeftVertex, topHalf);
		agents.add(agentA);

		// Create an agent in the bottom right of the graph
		Vertex bottomRightVertex = graph.vertexArray[4][4];
		Agent agentB = new Agent("B", graph, agentPeriod, bottomRightVertex, bottomHalf);
		agents.add(agentB);

		graphFrame = new SimulationFrame(this);

		// Setup the EventWriter
		if (dataFile != null) {
			try {
				eventWriter = new EventWriter(dataFile);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		// Move the agent every x seconds
		VariableTimer agentMovementTimerA = new VariableTimer();
		agentA.move();
		agentMovementTimerA.scheduleAtVariableRate(new AgentMoveTask(agentA), agentPeriod);
		
		// Move the agent every x seconds
		VariableTimer agentMovementTimerB = new VariableTimer();
		agentB.move();
		agentMovementTimerB.scheduleAtVariableRate(new AgentMoveTask(agentB), agentPeriod);
		
		// Add an event every x seconds
		VariableTimer randomEventTimer = new VariableTimer();
		randomEventTimer.scheduleAtVariableRate(new RandomEventTask(graph), eventPeriod);
	}
	
    class AgentMoveTask extends TimerTask {
    	private Agent agent;
    	
    	public AgentMoveTask(Agent agent) {
    		this.agent = agent;
    	}
    	
        @Override
        public void run() {
        	// Move the agent
        	Set<Event> collectedEvents = agent.move();
        	
        	// Write the event
        	if (eventWriter != null) {
            	eventWriter.writeEvents(agent, collectedEvents);
        	}
        }
    }
	
    class RandomEventTask extends TimerTask {
    	private EventGraph graph;
    	
    	public RandomEventTask(EventGraph graph) {
    		this.graph = graph;
    	}
    	
        @Override
        public void run() {
    		// Select random edge
            Random rand = new Random();
            int randomInt = rand.nextInt(this.graph.edges.size());
            EventEdge edge = (EventEdge) this.graph.edges.toArray()[randomInt];
            
            // Create random priority
            int randomPriority = rand.nextInt(maxPriority);
            Event event = new Event(randomPriority);
            edge.addEvent(event);
        }
    }
}
