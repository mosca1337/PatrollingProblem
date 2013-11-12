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
	public int totalCollected;
	
	// Animation properties
	public int nodeCount = 0;
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
		this.totalCollected = 0;
		this.isMoving = false;

		updateTimes();
	}
	
	public Set<Event> move() {
		
		// Do not collect events on the first move
		Set<Event> collectedEvents = new HashSet<Event>();
		if (lastEdge != null) {
			
			// Collect the total priority from the chosen edge
			totalCollected += lastEdge.getPriority();

			// Get all the events on the last edge
			collectedEvents = lastEdge.collectEvents();
			
			// Update locations
			lastLocation = movingToLocation;
		}

		Set<EventEdge> adjacentEdges = graph.getAdjacentEdges(lastLocation);
		
		// Stay within the boundary
		adjacentEdges.retainAll(boundary);
		System.out.println(name + " has choices " + adjacentEdges);
		
		// Looks for edge with highest priority
		int highestPriority = -1;
		for (EventEdge edge : adjacentEdges) {
			int edgePriority = edge.getPriority();
			if (edgePriority > highestPriority) {
				highestPriority = edgePriority;
				lastEdge = edge;
			}
		}

		Vertex nextLocation = lastEdge.getOtherVertex(lastLocation);
		movingToLocation = nextLocation;
		updateTimes();
		
		System.out.println(name + " now has " + totalCollected);

		nodeCount++;
		
		return collectedEvents;
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
