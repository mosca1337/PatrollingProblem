import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;


public class Agent {
	public final static int idleTime = 100;
	
	public EventGraph graph;
	public String name;
	public Vertex lastLocation;
	public Set<EventEdge> boundary;
	public EventEdge currentEdge = null;
	private Timer timer;
	private int speedConstant;
	
	public int totalPriorityCollected;
	public long totalDelay;
	public int deadEventsCollected;
	public int liveEventsCollected;
	
	// Animation properties
	public Vertex movingToLocation;
	public long startTime;
	public long endTime;
	
	public Agent(String name, EventGraph graph, int speedConstant, Vertex location, Set<EventEdge> boundary) {
		this.name = name;
		this.speedConstant = speedConstant;
		this.graph = graph;
		this.lastLocation = location;
		this.movingToLocation = location;
		this.boundary = boundary;
		this.timer = new Timer();
	}
	
    class AgentMoveTask extends TimerTask {
    	private Agent agent;
    	
    	public AgentMoveTask(Agent agent) {
    		this.agent = agent;
    	}
    	
        @Override
        public void run() {
        	// Move the agent
        	agent.move();
        }
    }

	public double getAverageDelay() {
		if (liveEventsCollected == 0) {
			return 0;
		}
		return (double)totalDelay / (double)liveEventsCollected;
	}
	
	private void move() {
		
		lastLocation = movingToLocation;

		// Find next movement choices within the agent's boundary
		Set<EventEdge> adjacentEdges = graph.getAdjacentEdges(lastLocation);
		adjacentEdges.retainAll(boundary);
		
		// Looks for edge with highest priority
		int highestPriority = -1;
		for (EventEdge edge : adjacentEdges) {
			int edgePriority = edge.getPriority();
			if (edgePriority > highestPriority) {
				highestPriority = edgePriority;
				currentEdge = edge;
			}
		}

		// Choose next location
		Vertex nextLocation = currentEdge.getOtherVertex(lastLocation);
		movingToLocation = nextLocation;

		int edgePriority = 0;
		Set<Event> collectedEvents = currentEdge.collectEvents();
		for (Event event : collectedEvents) {
			
			// Collect the total priority and delay from each edge
			totalPriorityCollected += event.getPriority();
			edgePriority += event.getPriority();
			long delay = event.timeCollected.getTime() - event.timeGenerated.getTime();
			totalDelay += delay;
			
			// Count live and dead events
			if (event.getPriority() <= 0) {
				deadEventsCollected++;
			} else {
				liveEventsCollected++;
			}
		}
		
		startTime = endTime;
		int traversalTime = (edgePriority * 1000) / speedConstant;
		traversalTime = Math.max(idleTime, traversalTime);
		endTime = startTime + traversalTime;
		
		// Schedule next movement
		timer.schedule(new AgentMoveTask(this), traversalTime);
	}
	
	public void start() {
		startTime = System.currentTimeMillis();
		endTime = startTime;
		move();
	}
	
	public void stop() {
		timer.cancel();
	}

	@Override
	public String toString() {
		return name + " at " + lastLocation.toString();
	}
}
