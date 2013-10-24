import java.util.Set;


public class Agent {
	public final int movementTime = 3000;
	
	public EventGraph graph;
	public String name;
	public Vertex currentLocation;
	public int totalCollected;
	
	// Animation properties
	public boolean isMoving;
	public Vertex movingToLocation;
	public long startTime;
	public long endTime;
	
	public Agent(String name, EventGraph graph, Vertex location) {
		this.name = name;
		this.graph = graph;
		this.currentLocation = location;
		this.totalCollected = 0;
		this.isMoving = false;
	}
	
	public void move() {
		Set<EventEdge> adjacentEdges = graph.getAdjacentEdges(currentLocation);
		System.out.println(name + " has choices " + adjacentEdges);
		
		// Looks for edge with highest priority
		int highestPriority = -1;
		EventEdge bestChoice = null;
		for (EventEdge edge : adjacentEdges) {
			int edgePriority = edge.getPriority();
			if (edgePriority > highestPriority) {
				highestPriority = edgePriority;
				bestChoice = edge;
			}
		}
		
		System.out.println("On edge: " + bestChoice);

		Vertex nextLocation = bestChoice.getOtherVertex(currentLocation);
		
		// Collect the total priority from the chosen edge
		totalCollected += bestChoice.clearEvents();
		
		// Wait x amount of seconds for the Agent to traverse the edge
		System.out.println(name + " will move to " + nextLocation);
		isMoving = true;
		movingToLocation = nextLocation;
		startTime = System.currentTimeMillis();
		endTime = startTime + movementTime;
		try {
			Thread.sleep(movementTime);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		isMoving = false;
		
		currentLocation = nextLocation;
		System.out.println(name + " moved to " + nextLocation);
		System.out.println(name + " now has " + totalCollected);
	}

	@Override
	public String toString() {
		return name + " at " + currentLocation.toString();
	}
}
