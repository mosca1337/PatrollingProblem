import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;
import java.util.Timer;
import java.util.TimerTask;


public class Agent {
	
	public Simulation simulation;
	
	// Agent Properties
	public String name;
	public Vertex lastVertex;
	public EventEdge lastEdge = null;
	public EventEdge currentEdge = null;
	public Set<EventEdge> boundary;
	private Fraction serviceRate;
	private double serviceTime;
	
	// Movement logistics
	private Stack<EventEdge> lastEdges;
	public Queue<EventEdge> movementSequence;
	
	// Agent statistics
	public int totalPriorityCollected;
	public long totalDelay;
	public int deadEventsCollected;
	public int liveEventsCollected;
	
	// Animation properties
	public Vertex movingToVertex;
	public double startTime;
	public double endTime;
	
	// Agent's have a name
	// belong to a simulation
	// have a starting location
	// and can travel within a boundary
	public Agent(String name, Simulation simulation, Vertex initialLocation, Set<EventEdge> boundary) {
		this.name = name;
		this.simulation = simulation;
		this.lastVertex = initialLocation;
		this.movingToVertex = initialLocation;
		this.boundary = boundary; // null boundary allows the agent to move anywhere
		this.lastEdges = new Stack<EventEdge>();
		
		// Default service rate of 1
		this.setServiceRate(new Fraction(1, 1));
		this.movementSequence = new LinkedList<EventEdge>();
	}
	
	public Fraction getServiceRate() {
		return serviceRate;
	}
	
	public void setServiceRate(Fraction aServiceRate) {
		serviceRate = aServiceRate;
		
		// Find service time
		serviceTime = serviceRate.evaluate() * Simulation.timeConstant;
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
			currentEdge.agents.remove(this);
		}
		
		if (Simulation.verbose) {
	    	System.out.println(this);			
		}

		lastEdge = currentEdge;
    	lastVertex = movingToVertex;
    	lastEdges.add(lastEdge);

    	// If we are out of planned movements, find the next path
    	if (movementSequence.size() == 0) {
//    		Set<EventEdge> adjacentEdges = simulation.graph.getAdjacentEdges(lastVertex);
//    		Set<EventEdge> adjacentEdgesCopy = new HashSet<EventEdge>(simulation.graph.getAdjacentEdges(lastVertex));
//    		adjacentEdgesCopy.retainAll(boundary);
//    		if (adjacentEdgesCopy.size() == 0) {
//    			System.out.println("NO OPTIONS");
//
//    			System.out.println("boundary");
//    			Set<Vertex> boundaryVertices = new HashSet<Vertex>();
//    			for (EventEdge edge : boundary) {
//    				boundaryVertices.add(edge.vertex1);
//    				boundaryVertices.add(edge.vertex2);
////    				System.out.println(edge);
//    			}
//    			
//    			for (Vertex vertex : boundaryVertices) {
//    				System.out.println(vertex);
//    			}
//    			
//    			System.out.println("adjacents");
//    			for (EventEdge edge : adjacentEdges) {
//    				System.out.println(edge);
//    			}
//    			System.out.println("NO OPTIONS");
//
//    		}

    		// Movement logic
//    		basicFindMove(lastVertex);
        	findMovesWithTwoStepLookAhead(lastVertex);
    	}

    	// Get the next movement
    	currentEdge = movementSequence.poll();

		// We are now traversing this edge
		currentEdge.agents.add(this);

		// Find the destination vertex
		Vertex nextLocation = currentEdge.getOtherVertex(lastVertex);
		movingToVertex = nextLocation;

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
		double traversalTime = edgePriority * serviceTime;
		traversalTime = Math.max(serviceTime, traversalTime); // Idle time is equal to service time
		endTime = startTime + Math.ceil(traversalTime);
		
		return endTime;
	}
	
	private void basicFindMove(Vertex currentLocation) {

    	EventEdge nextEdge = null;
		Set<EventEdge> adjacentEdges = simulation.graph.getAdjacentEdges(currentLocation);

		// Only travel within the agent's boundary
    	if (boundary != null) {
    		adjacentEdges.retainAll(boundary);
    	}

		// Looks for edge with highest priority
		int highestPriority = -1;
		for (EventEdge edge : adjacentEdges) {
			int edgePriority = edge.getPriority(startTime);
			if (edgePriority > highestPriority) {
				highestPriority = edgePriority;
				nextEdge = edge;
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
				nextEdge = edge;
				break;
			}
		} else {
			// If the chosen edge has value, reset lastEdges
			lastEdges.removeAllElements();
		}
		
		movementSequence.add(nextEdge);
	}
	
	private void findMovesWithTwoStepLookAhead(Vertex currentLocation) {

		EventEdge bestFirstEdge = null;
		EventEdge bestSecondEdge = null;
		Set<EventEdge> adjacentEdges = simulation.graph.getAdjacentEdges(currentLocation);

		// Only travel within the agent's boundary
		if (boundary != null) {
    		adjacentEdges.retainAll(boundary);
    	}

		// Looks for edge with highest priority
		int highestPriority = -1;
		for (EventEdge edge : adjacentEdges) {
			int firstEdgePriority = edge.getPriority(startTime);
			
			double traversalTime = firstEdgePriority * serviceTime;
			traversalTime = Math.max(serviceTime, traversalTime); // Idle time is equal to service time

			// Get second step edges
			Vertex firstVertex = edge.getOtherVertex(lastVertex);
			Set<EventEdge> secondEdges = simulation.graph.getAdjacentEdges(firstVertex);
			
			// Do not consider the previous location
			secondEdges.remove(edge);
			
			// Only travel within the agent's boundary
			if (boundary != null) {
        		secondEdges.retainAll(boundary);
        	}
			
			// If there is more than one option, avoid edges that are being traversed
			if (secondEdges.size() > 1) {
				Set<EventEdge> nonTraversedEdges = new HashSet<EventEdge>();
				for (EventEdge secondEdge : secondEdges) {
					if (!secondEdge.isBeingTraversed()) {
						nonTraversedEdges.add(secondEdge);
					}
				}
				
				// Make sure that there is at least one option
				if (nonTraversedEdges.size() > 0) {
					secondEdges = nonTraversedEdges;
				} else {
					System.out.println("Agent " + this + " has no other options!");
				}
			}

			for (EventEdge secondEdge : secondEdges) {

				int secondEdgePriority = secondEdge.getPriority(traversalTime + startTime);
				int totalPriority = firstEdgePriority + secondEdgePriority;
				if (totalPriority > highestPriority) {
					highestPriority = firstEdgePriority;
					
					bestFirstEdge = edge;
					bestSecondEdge = secondEdge;
				}
			}
		}
		
		movementSequence.add(bestFirstEdge);
		movementSequence.add(bestSecondEdge);
	}
	
//	private int highestPathValue;
//	private Queue<EventEdge> reversedMovements;
//	private Queue<EventEdge> bestReversedMovements;
//	
//	private void findMovements(Vertex currentLocation, int movements) {
//		
//		highestPathValue = -1;
//		reversedMovements = new LinkedList<EventEdge>();
//		int pathValue = recursiveFindMovements(currentLocation, movements, 0);
//		
//		movementSequence.addAll(bestReversedMovements);
//	}
//	
//	private int recursiveFindMovements(Vertex currentLocation, int movements, int pathValue) {
//		
//		
//		recursiveFindMovements(currentLocation, movements--);
//		
//		return 0;
//	}

	@Override
	public String toString() {
		return name + " traversing (" + lastVertex.toString() + " to " + movingToVertex.toString() + ")";
	}
}
