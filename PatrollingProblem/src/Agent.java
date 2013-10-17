import java.util.Set;


public class Agent {
	public String name;
	public EventGraph graph;
	public Vertex currentLocation;
	
	public Agent(String name, EventGraph graph, Vertex location) {
		this.name = name;
		this.graph = graph;
		this.currentLocation = location;
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
		
		Vertex nextLocation = bestChoice.getOtherVertex(currentLocation);
		currentLocation = nextLocation;
		System.out.println(name + " moved to " + nextLocation);
	}

	@Override
	public String toString() {
		return name + " at " + currentLocation.toString();
	}
}
