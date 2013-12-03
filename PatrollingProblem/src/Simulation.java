import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;


public class Simulation {
	public final static int minPriority = 1;
	public final static int maxPriority = 10;
	
	public Function eventValueFunction;
	public int agentConstant;
	public Function eventPeriod;
	
	public SimulationFrame graphFrame;
	public EventGraph graph;
	public Set<Agent> agents;
	public int totalAgents;
	
	public int eventsGenerated = 0;
	private int deadEventCount = 0;
	public int maxEventsGenerated;
	
	// Timers
	private VariableTimer randomEventTimer;
	
	// Blocking
	CountDownLatch latch;

	public static void main(String[] args) {

		// Event generation period
		Function eventPeriod = new Function() {
			public long function(long x) {
//	    		return 500;
				
				// TODO: exponential distribution in MM1
	    		return 2000/(x+1) + 100;
	    	}
		};

		// Value of event over time
		Function eventValue = new Function() {
			public long function(long x) {
	    		return 2000 * x; // Decrements by 1 value every 2 seconds
	    	}
		};

		Simulation simulation = new Simulation(eventValue, 10, 2, eventPeriod, 500);
//		Simulation simulation = new Simulation(null, agentPeriod, 2, eventPeriod, 500);
		simulation.simulate();
	}
	
	public Simulation(Function eventValue,int agentConstant, int totalAgents, Function eventPeriod, int maxEventsGenerated) {
		super();
		this.eventValueFunction = eventValue;
		this.agentConstant = agentConstant;
		this.totalAgents = totalAgents;
		this.eventPeriod = eventPeriod;
		this.maxEventsGenerated = maxEventsGenerated;
		
		this.latch = new CountDownLatch(1);
	}
	
	public void simulate() {
		
		graph = new EventGraph(5,5);
//		System.out.println(graph);

		graphFrame = new SimulationFrame(this);
		
		setupAgents(totalAgents);
		
		// Add an event every x seconds
		randomEventTimer = new VariableTimer();
		randomEventTimer.scheduleAtVariableRate(new RandomEventTask(graph), eventPeriod);
		

		// block this function until the simulation is over
		try {
			latch.await();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
			Agent agent = new Agent("A", graph, agentConstant, topLeftVertex, graph.edges);
			agents.add(agent);
			agent.start();
		} else if (totalAgents == 2) {
			Set<EventEdge> topHalf = graph.getEdges(0,0,2,4);
			Set<EventEdge> bottomHalf = graph.getEdges(2,0,4,4);
			
			// Create an agent in the top left of the graph
			Vertex topLeftVertex = graph.vertexArray[0][0];
			Agent agentA = new Agent("A", graph, agentConstant, topLeftVertex, topHalf);
			agents.add(agentA);
			agentA.start();

			// Create an agent in the bottom right of the graph
			Vertex bottomRightVertex = graph.vertexArray[4][4];
			Agent agentB = new Agent("B", graph, agentConstant, bottomRightVertex, bottomHalf);
			agents.add(agentB);
			agentB.start();
		}
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
	
	public int getDeadEventCount() {
		removeDeadEvents();
		accumulateAgents();
		return deadEventCount;
	}
	
	private void accumulateAgents() {
		for (Agent agent : agents) {
			deadEventCount += agent.deadEventsCollected;
			agent.deadEventsCollected = 0;
		}
	}
	
	private void removeDeadEvents() {
		for (EventEdge edge : graph.edges) {
			Set<Event> deadEvents = edge.removeDeadEvents();
			deadEventCount += deadEvents.size();
		}
	}
	
    class RandomEventTask extends TimerTask {
    	private EventGraph graph;
    	
    	public RandomEventTask(EventGraph graph) {
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
            Event event = new Event(randomPriority, eventValueFunction);
            edge.addEvent(event);
            eventsGenerated++;
            
            // End simulation after x amount of events generated
            if (eventsGenerated >= maxEventsGenerated) {
            	endSimulation();
            }
        }
    }
    
    private void endSimulation() {
    	
    	// Cancel timers
    	randomEventTimer.cancel();
    	for (Agent agent : agents) {
    		agent.stop();
    	}
    	
    	// Collect remaining dead events
    	removeDeadEvents();
    	accumulateAgents();
        
        // Finish simulation
        latch.countDown();
    }
}
