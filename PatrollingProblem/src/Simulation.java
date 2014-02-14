import java.util.Comparator;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Set;

public class Simulation {

	public final static boolean verbose = false;
	
	// Simulation constants
	public final static int minPriority = 1;
	public final static int maxPriority = 10;
	// Used to show the simulation is real time
	// The range of agent traversal time is equal to the range of (minPriority*timeConstant) to (maxPriority*timeConstant)
	public static int timeConstant = 1000; // The default time of a 'tick' is one second
	
	// Time
	public double startTime = 0;
	private double lastExecutionTime = 0;
	private PriorityQueue<EventTask> eventQueue;
	
	// Visualization
	public boolean isVisible;
	public SimulationFrame graphFrame;
	public EventGraph graph;

	// Agents
	public Set<Agent> agents;
	public int totalAgents;
	public Fraction serviceRate;
	
	// Simulation statistics
	public int eventsGenerated = 0;
	private int deadEventCount = 0;
	public int totalEvents;
	
	// Timers and functions
	public Function eventValueFunction;
	public Function eventPeriod;
	
	// Default event generation function
	private Fraction constantPeriodFraction = new Fraction(1,2);
	private Function constantEventPeriod = new Function() {
		public long function(long x) {
			 // Two events are generated every 'tick'
    		return (long) (Simulation.timeConstant * constantPeriodFraction.evaluate());
    	}
	};

	public static void main(String[] args) {

		// Event generation period (Exponential)
		final Exponential exponential = new Exponential(5.0); // Default as 1
		Function exponentialEventPeriod = new Function() {
			public long function(long x) {

				double exponent = exponential.nextExponential();
				long period = (long) (Simulation.timeConstant * exponent);
				period = (long) Math.ceil(period); // Round up to prevent time of 0
				
				// Display time in 'ticks'
				if (Simulation.verbose) {
					System.out.print("Next event generated in ");
					System.out.printf("%.3f", exponent);
					System.out.println(" ticks");
				}
				
				return period;
	    	}
		};

		// Value of event over time
		Function decreasingValue = new Function() {
			public long function(long x) {
	    		return 2 * x; // Decrements by 1 value every 2 ticks
	    	}
		};
		
		// Constant event value
		Function constantValue = new Function() {
			public long function(long x) {
	    		return 0; // Decrements by 0
	    	}
		};

		Simulation simulation = new Simulation();
		simulation.isVisible = true;
//		simulation.eventValueFunction = constantValue;
		simulation.eventValueFunction = decreasingValue;
//		simulation.eventPeriod = exponentialEventPeriod;
		simulation.totalAgents = 4;
		
		simulation.totalEvents = 100000;
//		simulation.serviceRate = new Fraction(1,10);
		simulation.simulate();
		
		simulation.getAverageDelay();
		simulation.getDeadEventCount();
	}
	
	public Simulation() {
		super();
		
		isVisible = true;
		totalAgents = 1;
		totalEvents = 500; // Default to 500 for display purposes.
		eventPeriod = constantEventPeriod; // Default to a constant period
		eventValueFunction = null; // Default: event value does not change
	}
	
	public void simulate() {
		
		graph = new EventGraph(5,5);
		setupAgents(totalAgents);
		
		// Event Queue
		eventQueue = new PriorityQueue<EventTask>(totalEvents, new Comparator<EventTask>() {
			public int compare(EventTask eventTask1, EventTask eventTask2) {
				   return Double.compare(eventTask1.executionTime, eventTask2.executionTime);
				}
	    });

		if (isVisible) {
			graphFrame = new SimulationFrame(this);
		}
		
		// Generate all events
		double eventTime = 0;
		RandomEventTask lastEventTask = null;
		for (int i = 0; i < totalEvents; i++) {
			eventTime += eventPeriod.function(i);
			lastEventTask = new RandomEventTask(eventTime, graph);
			eventQueue.add(lastEventTask);
		}

		// Set agents in motion
		for (Agent agent : agents) {
			AgentMoveTask agentMove = new AgentMoveTask(0.0, agent, eventQueue);
			eventQueue.add(agentMove);
		}
		
		startTime = System.currentTimeMillis();
		
		// Simulate all events
		lastExecutionTime = 0;
		EventTask task = null;
		while (task != lastEventTask) {
			
			// Current time
			long now = 0; // Imaginary time
			if (isVisible) {
				now = System.currentTimeMillis(); // Real time
			}
			
			// Has the event just passed?
			if (!isVisible || now > (lastExecutionTime + startTime)) {
				task = eventQueue.remove();
				lastExecutionTime = task.executionTime;
				task.run();				
			}
		}
		
		// End of simulation
    	removeDeadEvents(lastExecutionTime);
    	accumulateAgents();
	}
	
	public void closeFrame() {
		if (graphFrame != null) {
			graphFrame.setVisible(false);
		}
	}
	
	private void setupAgents(int totalAgents) {
		
		agents = new HashSet<Agent>();

		// TODO: currently only handles 1 or 2 agents
		if (totalAgents == 1) {
			// Create an agent in the top left of the graph
			Vertex topLeftVertex = graph.vertexArray[0][0];
			Agent agent = new Agent("A", this, topLeftVertex, null);
			agents.add(agent);
		} else if (totalAgents == 2) {
			Set<EventEdge> topHalf = graph.getEdges(0,0,2,4);
			Set<EventEdge> bottomHalf = graph.getEdges(2,0,4,4);
			
			// Create an agent in the top left of the graph
			Vertex topLeftVertex = graph.vertexArray[0][0];
			Agent agentA = new Agent("A", this, topLeftVertex, topHalf);
			agents.add(agentA);

			// Create an agent in the bottom right of the graph
			Vertex bottomRightVertex = graph.vertexArray[4][4];
			Agent agentB = new Agent("B", this, bottomRightVertex, bottomHalf);
			agents.add(agentB);
		} else if (totalAgents == 3) {
			Set<EventEdge> topHalf = graph.getEdges(0,0,1,4);
			Set<EventEdge> middleHalf = graph.getEdges(1,0,3,4);
			Set<EventEdge> bottomHalf = graph.getEdges(3,0,4,4);
			
			// Create an agent in the top left of the graph
			Vertex topLeftVertex = graph.vertexArray[0][0];
			Agent agentA = new Agent("A", this, topLeftVertex, topHalf);
			agents.add(agentA);

			// Create an agent in the bottom right of the graph
			Vertex middleVertex = graph.vertexArray[2][2];
			Agent agentB = new Agent("B", this, middleVertex, middleHalf);
			agents.add(agentB);

			// Create an agent in the bottom right of the graph
			Vertex bottomRightVertex = graph.vertexArray[4][4];
			Agent agentC = new Agent("C", this, bottomRightVertex, bottomHalf);
			agents.add(agentC);
		} else if (totalAgents == 4) {
			Set<EventEdge> topLeft = graph.getEdges(0,0,2,2);
			Set<EventEdge> topRight = graph.getEdges(0,2,2,4);
			Set<EventEdge> bottomLeft = graph.getEdges(2,0,4,2);
			Set<EventEdge> bottomRight = graph.getEdges(2,2,4,4);

			// Create an agent in the top left of the graph
			Vertex topLeftVertex = graph.vertexArray[0][0];
			Agent agentA = new Agent("A", this, topLeftVertex, topLeft);
			agents.add(agentA);

			// Create an agent in the bottom right of the graph
			Vertex topRightVertex = graph.vertexArray[0][4];
			Agent agentB = new Agent("B", this, topRightVertex, topRight);
			agents.add(agentB);

			// Create an agent in the bottom right of the graph
			Vertex bottomLeftVertex = graph.vertexArray[4][0];
			Agent agentC = new Agent("C", this, bottomLeftVertex, bottomLeft);
			agents.add(agentC);

			// Create an agent in the bottom right of the graph
			Vertex bottomRightVertex = graph.vertexArray[4][4];
			Agent agentD = new Agent("D", this, bottomRightVertex, bottomRight);
			agents.add(agentD);
		}
		
		// Apply the service rate to all agents
		for (Agent agent : agents) {
			if (serviceRate != null) {
				agent.setServiceRate(this.serviceRate);
			}
		}
	}
	
	public double getHandledRate() {
		int liveEventsCollected = 0;
		for (Agent agent : agents) {
			liveEventsCollected += agent.liveEventsCollected;
		}

		double handledRate = (double) liveEventsCollected / (double) eventsGenerated;
		
		return handledRate;
	}
	
	public int getLiveEventsCollected() {
		int liveEventsCollected = 0;
		for (Agent agent : agents) {
			liveEventsCollected += agent.liveEventsCollected;
		}
		return liveEventsCollected;
	}
	
	public int getTotalPriorityCollected() {
		int total = 0;
		for (Agent agent : agents) {
			total += agent.totalPriorityCollected;
		}
		return total;
	}
	
	public double getAverageDelay() {
		double average = 0;
		for (Agent agent : agents) {
			average += agent.getAverageDelay();
		}
		average /= agents.size();
		return average;
	}
	
	public double getDelay() {
		double delay = 0;
		for (Agent agent : agents) {
			delay += agent.totalDelay;
		}
		return delay;
	}
	
//	public int getDeadEventCount(double time) {
//		removeDeadEvents(time);
//		accumulateAgents();
//		return deadEventCount;
//	}
	
	public int getDeadEventCount() {
		removeDeadEvents(lastExecutionTime);
		accumulateAgents();
		return deadEventCount;
	}
	
	private void accumulateAgents() {
		for (Agent agent : agents) {
			deadEventCount += agent.deadEventsCollected;
			agent.deadEventsCollected = 0;
		}
	}
	
	private void removeDeadEvents(double time) {
		for (EventEdge edge : graph.edges) {
			Set<Event> deadEvents = edge.removeDeadEvents(time);
			deadEventCount += deadEvents.size();
		}
	}
	
    class RandomEventTask extends EventTask {
    	private EventGraph graph;
    	
    	public RandomEventTask(Double executionTime, EventGraph graph) {
    		super(executionTime);
    		this.graph = graph;
    	}

		@Override
        public void run() {
    		// Select random edge
            Random rand = new Random();
            int randomInt = rand.nextInt(this.graph.edges.size());
            EventEdge edge = (EventEdge) this.graph.edges.toArray()[randomInt];

            // Create random priority
            int randomPriority = minPriority + (int)(Math.random() * ((maxPriority - minPriority) + 1));
            Event event = new Event(randomPriority, executionTime, eventValueFunction);
            edge.addEvent(event);
            eventsGenerated++;
       }
    }
	
    class AgentMoveTask extends EventTask {
    	private Agent agent;
    	private PriorityQueue<EventTask> queue;
    	
    	public AgentMoveTask(Double executionTime, Agent agent, PriorityQueue<EventTask> queue) {
    		super(executionTime);
    		this.agent = agent;
    		this.queue = queue;
    	}

		@Override
        public void run() {
    		double movementTime = agent.move(executionTime);
    		AgentMoveTask nextMovementTask = new AgentMoveTask(movementTime, agent, queue);
    		queue.add(nextMovementTask);
        }
    }
}
