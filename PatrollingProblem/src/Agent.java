import java.util.HashSet;
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
	public double startTime;
	public double endTime;
	
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
		this.lastEdges = new Stack<EventEdge>();
		
		// Default service rate of 1
		this.serviceRate = new Fraction(1, 1);
	}
    
	public double getAverageDelay() {
		// Don't divide by zero. The universe may implode...
		if (liveEventsCollected == 0) {
			return 0;
		}
		return (double)totalDelay / (double)liveEventsCollected / Simulation.timeConstant;
	}
	
	public double move(double startTime) {
		this.startTime = startTime;
		this.endTime = startTime;
		
		// We are done traversing the edge
		if (currentEdge != null) {
			currentEdge.isBeingTraversed = false;
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
    	
    	// Do not traverse an edge with another agent
		Set<EventEdge> traversedEdges = new HashSet<EventEdge>();
		for (EventEdge edge : adjacentEdges) {
			if (edge.isBeingTraversed) {
				traversedEdges.add(edge);
			}
		}
		adjacentEdges.removeAll(traversedEdges);
		adjacentEdges.remove(lastEdge);
		
		// Looks for edge with highest priority
		int highestPriority = -1;
		for (EventEdge edge : adjacentEdges) {
			int edgePriority = edge.getPriority(startTime);
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
		
		// We are now traversing this edge
		if (currentEdge != null) {
			currentEdge.isBeingTraversed = true;
		}

		// Choose next location
		Vertex nextLocation = currentEdge.getOtherVertex(lastLocation);
		movingToLocation = nextLocation;

		int edgePriority = 0;
		Set<Event> collectedEvents = currentEdge.collectEvents(startTime);
		for (Event event : collectedEvents) {
			
			// Collect the total priority and delay from each event on the edge
			totalPriorityCollected += event.getPriority(startTime);
			edgePriority += event.getPriority(startTime);
			double delay = event.timeCollected - event.timeGenerated;
			totalDelay += delay;
			
			// Count live and dead events
			if (event.getPriority(startTime) <= 0) {
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
		endTime = startTime + Math.ceil(traversalTime);
		
		return endTime;
	}

	@Override
	public String toString() {
		return name + " traversing (" + lastLocation.toString() + " to " + movingToLocation.toString() + ")";
	}
}
