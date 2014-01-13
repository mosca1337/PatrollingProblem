import java.util.Set;
import java.util.Stack;
import java.util.Timer;
import java.util.TimerTask;


public class Agent {
	
	public Simulation simulation;
	
	// Agent Properties
	public String name;
	public Vertex lastLocation;
	public EventEdge lastEdge = null;
	public EventEdge currentEdge = null;
	private Timer timer;
	public Set<EventEdge> boundary;
	public Fraction serviceRate;
	
	// Movement logistics
	private Stack<EventEdge> lastEdges;
	
	// Agent statistics
	public int totalPriorityCollected;
	public long totalDelay;
	public int deadEventsCollected;
	public int liveEventsCollected;
	
	// Animation properties
	public Vertex movingToLocation;
	public long startTime;
	public long endTime;
	private boolean stopped = true;
	
	// Agent's have a name
	// belong to a simulation
	// have a starting location
	// and can travel within a boundary
	public Agent(String name, Simulation simulation, Vertex initialLocation, Set<EventEdge> boundary) {
		this.name = name;
		this.simulation = simulation;
		this.lastLocation = initialLocation;
		this.movingToLocation = initialLocation;
		this.boundary = boundary; // null boundary allows the agent to move anywhere
		this.timer = new Timer();
		this.lastEdges = new Stack<EventEdge>();
		
		// Default service rate of 1
		this.serviceRate = new Fraction(1, 1);
	}
	
	// This task is used to by a Timer to move the agent
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
		// Don't divide by zero. The universe may implode...
		if (liveEventsCollected == 0) {
			return 0;
		}
		return (double)totalDelay / (double)liveEventsCollected / Simulation.timeConstant;
	}
	
	private void move() {
		
		// Don't move if the agent has been stopped.
		if (stopped) {
			return;
		}
		
		if (Simulation.verbose) {
	    	System.out.println(this);			
		}

		lastEdge = currentEdge;
    	lastLocation = movingToLocation;
    	lastEdges.add(lastEdge);

		// Find next movement choices within the agent's boundary
    	// Agent can move anywhere if there is no boundary
		Set<EventEdge> adjacentEdges = simulation.graph.getAdjacentEdges(lastLocation);
    	if (boundary != null) {
    		adjacentEdges.retainAll(boundary);
    		
    	}
		
		// Looks for edge with highest priority
		int highestPriority = -1;
		for (EventEdge edge : adjacentEdges) {
			int edgePriority = edge.getPriority();
			if (edgePriority > highestPriority) {
				highestPriority = edgePriority;
				currentEdge = edge;
			}
		}
		
		// If all adjacent edges have no events, pick a new edge
		if (highestPriority == 0) {
			
			// Avoid recently traversed edges
			for (EventEdge edge : lastEdges) {
				adjacentEdges.remove(edge);
			}

			// Pick a new edge
			for (EventEdge edge : adjacentEdges) {
				currentEdge = edge;
				break;
			}
		} else {
			// If the chosen edge has value, reset lastEdges
			lastEdges.removeAllElements();
		}

		// Choose next location
		Vertex nextLocation = currentEdge.getOtherVertex(lastLocation);
		movingToLocation = nextLocation;

		int edgePriority = 0;
		Set<Event> collectedEvents = currentEdge.collectEvents();
		for (Event event : collectedEvents) {
			
			// Collect the total priority and delay from each event on the edge
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
		
		// Traversal time for the visualization
		startTime = endTime;
		double serviceTime = serviceRate.evaluate() * Simulation.timeConstant;
		double traversalTime = edgePriority * serviceTime;
		traversalTime = Math.max(serviceTime, traversalTime); // Idle time is equal to service time
		endTime = (long) (startTime + Math.ceil(traversalTime));
		
		// Schedule next movement
		if (!stopped) {
			timer.schedule(new AgentMoveTask(this), (long) Math.ceil(traversalTime));
		}
	}
	
	public void start() {
		stopped = false;
		startTime = System.currentTimeMillis();
		endTime = startTime;
		move();
	}
	
	public void stop() {
		stopped = true;
		timer.cancel();
	}

	@Override
	public String toString() {
		return name + " traversing (" + lastLocation.toString() + " to " + movingToLocation.toString() + ")";
	}
}
