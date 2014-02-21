import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;


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

		// Update locations
		lastEdge = currentEdge;
    	lastVertex = movingToVertex;

    	// If we are out of planned movements, find the next path
    	if (movementSequence.size() == 0) {

    		// Movement logic
    		basicFindMove(lastVertex);
//        	findMovesWithTwoStepLookAhead(lastVertex);
//        	findMovements(lastVertex, 3);
    	}

    	// Get the next movement
    	currentEdge = movementSequence.poll();

		// We are now traversing this edge
		currentEdge.agents.add(this);

		// Find the destination vertex
		Vertex nextLocation = currentEdge.getOtherVertex(lastVertex);
		movingToVertex = nextLocation;

		// Traversal time for the visualization
		startTime = endTime;
		double traversalTime = getTraversalTime(currentEdge, startTime);
		endTime = startTime + traversalTime;

		// Collect all events
		Set<Event> collectedEvents = currentEdge.collectEvents(startTime);
		for (Event event : collectedEvents) {
			
			// Collect the total priority and delay from each event on the edge
			totalPriorityCollected += event.getPriority(startTime);
			double delay = event.timeCollected - event.timeGenerated;
			totalDelay += delay;
			
			// Count live and dead events
			if (event.getPriority(startTime) <= 0) {
				deadEventsCollected++;
			} else {
				liveEventsCollected++;
			}
		}
		
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
		

    	lastEdges.add(nextEdge);
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
			double traversalTime = getTraversalTime(edge, startTime);

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
	
	private Set<EventEdge> possibleMovements;
	private Stack<EventEdge> reversedMovements;
	
	private void findMovements(Vertex currentLocation, int movements) {
		
		reversedMovements = new Stack<EventEdge>();
		possibleMovements = new HashSet<EventEdge>();
		int pathValue = recursiveFindMovements(currentLocation, movements, startTime, 0);
		System.out.println("Best option has value of " + pathValue);
		
//		for (EventEdge edge : reversedMovements) {
//			System.out.println(edge);
//			movementSequence.add(edge);
//		}
		
		movementSequence.addAll(reversedMovements);
	}
	
	private int recursiveFindMovements(Vertex currentLocation, int movements, double time, int pathValue) {
		
		// Base case
		if (movements == 0) {
			return 0;
		}
		movements--;
		
		Set<EventEdge> adjacentEdges = simulation.graph.getAdjacentEdges(currentLocation);
		
		// Ignore all edges that we already considered
		adjacentEdges.removeAll(possibleMovements);
		
		int highestPathValue = -1;
		EventEdge bestMove = null;
		
		for (EventEdge edge : adjacentEdges) {
			int edgePriority = edge.getPriority(time);
			double traversalTime = getTraversalTime(edge, time);

			possibleMovements.add(edge);
			int nextPathValue = recursiveFindMovements(currentLocation, movements, (time + traversalTime), (pathValue + edgePriority));
			possibleMovements.remove(edge);
			
			nextPathValue += edgePriority;
			if (nextPathValue > highestPathValue) {
				highestPathValue = nextPathValue;
				bestMove = edge;
			}
		}
		
		reversedMovements.add(bestMove);
		
		return highestPathValue;
	}
	
	private double getTraversalTime(EventEdge edge, double time) {
		int edgePriority = edge.getPriority(time);
		double traversalTime = edgePriority * serviceTime;
		traversalTime = Math.max(serviceTime, traversalTime); // Idle time is equal to service time

		return traversalTime;
	}

	@Override
	public String toString() {
		return name + " traversing (" + lastVertex.toString() + " to " + movingToVertex.toString() + ")";
	}
}
