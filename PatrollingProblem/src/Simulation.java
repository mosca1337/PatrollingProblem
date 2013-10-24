import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;


public class Simulation {
	public final int maxPriority = 10;
	
	public SimulationFrame graphFrame;
	public EventGraph graph;
	public Agent agentA;

	public static void main(String[] args) {
		new Simulation();
	}
	
	public Simulation() {
		super();
		
		graph = new EventGraph(4,4);
		System.out.println(graph);
		
		// Create an agent in the top left of the graph
		Vertex topLeftVertex = graph.vertexArray[0][0];
		agentA = new Agent("A", graph, topLeftVertex);

		graphFrame = new SimulationFrame(this);

		// TODO: independent threads (generating events and movements), (add each events on a new thread?)
		// TODO: variable time that it takes for agent to move
		// TODO: lock all events on an edge when an agent is moving across it
		// TODO: assign edges to each agent
		
		// Move the agent every x seconds
		Timer agentMovementTimer = new Timer();
		agentMovementTimer.schedule(new AgentMoveTask(agentA), 0, 2000);
		
		// Add an event every x seconds
		Timer randomEventTimer = new Timer();
		randomEventTimer.schedule(new RandomEventTask(graph), 0, 500);
	}
	
    class AgentMoveTask extends TimerTask {
    	private Agent agent;
    	
    	public AgentMoveTask(Agent agent) {
    		this.agent = agent;
    	}
    	
        @Override
        public void run() {
        	agent.move();
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
