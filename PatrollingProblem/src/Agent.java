import java.util.Date;
import java.util.HashSet;
import java.util.Set;


public class Agent {
	public EventGraph graph;
	public String name;
	public Function speed;
	public Vertex lastLocation;
	public Set<EventEdge> boundary;
	public EventEdge lastEdge = null;
	
	public int totalPriorityCollected;
	public long totalDelay;
	public int deadEventsCollected;
	public int liveEventsCollected;
	
	// Animation properties
	public int nodeCount;
	public boolean isMoving;
	public Vertex movingToLocation;
	public long startTime;
	public long endTime;
	
	public Agent(String name, EventGraph graph, Function speed, Vertex location, Set<EventEdge> boundary) {
		this.name = name;
		this.speed = speed;
		this.graph = graph;
		this.lastLocation = location;
		this.movingToLocation = location;
		this.boundary = boundary;
		this.isMoving = false;

		updateTimes();
	}
	
	public double getAverageDelay() {
		if (liveEventsCollected == 0) {
			return 0;
		}
		return (double)totalDelay / (double)liveEventsCollected;
	}
	
	public void move() {
		
		// Do not collect events on the first move
		if (lastEdge != null) {
			
			// Get all the events on the last edge
			Set<Event> collectedEvents = lastEdge.collectEvents();
			for (Event event : collectedEvents) {
				
				// Collect the total priority and delay from each edge
				totalPriorityCollected += event.getPriority();
				long delay = event.timeCollected.getTime() - event.timeGenerated.getTime();
				totalDelay += delay;
				
				// Count live and dead events
				if (event.getPriority() <= 0) {
					deadEventsCollected++;
				} else {
					liveEventsCollected++;
				}
			}
			
			// Update locations
			lastLocation = movingToLocation;
		}

		// Find next movement choices within the agent's boundary
		Set<EventEdge> adjacentEdges = graph.getAdjacentEdges(lastLocation);
		adjacentEdges.retainAll(boundary);
		
		// Looks for edge with highest priority
		int highestPriority = -1;
		for (EventEdge edge : adjacentEdges) {
			int edgePriority = edge.getPriority();
			if (edgePriority > highestPriority) {
				highestPriority = edgePriority;
				lastEdge = edge;
			}
		}

		// Choose next location
		Vertex nextLocation = lastEdge.getOtherVertex(lastLocation);
		movingToLocation = nextLocation;
		updateTimes();
		
		nodeCount++;
	}
	
	private void updateTimes() {
		
		startTime = System.currentTimeMillis();
		long movementTime = speed.function(nodeCount);
		endTime = startTime + movementTime;
	}

	@Override
	public String toString() {
		return name + " at " + lastLocation.toString();
	}
}
