import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;


public class Simulation {
	public final static int minPriority = 1;
	public final static int maxPriority = 10;
	
	private EventWriter eventWriter = null;
	
	public Function eventValueFunction;
	public Function agentPeriod;
	public Function eventPeriod;
	
	public SimulationFrame graphFrame;
	public EventGraph graph;
	public Set<Agent> agents;
	
	public int eventsGenerated = 0;
	private int deadEventCount = 0;
	public int maxEventsGenerated;
	
	// Timers
	private VariableTimer randomEventTimer;
	private Set<VariableTimer> agentTimers;
	
	// TODO: number of dead events total

	public static void main(String[] args) {
		
		// Simulation file
		String userHomeFolder = System.getProperty("user.home");
		File file = new File(userHomeFolder, "Desktop/simulationData.csv");

		// Setup the EventWriter
		EventWriter eventWriter = null;
		if (file != null) {
			try {
				eventWriter = new EventWriter(file);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		// Agent period
		Function agentPeriod = new Function() {
			public long function(long x) {
				// TODO: priority value /= 10,20, and 50
	    		return 1000;
	    	}
		};
		
		// Event generation period
		Function eventPeriod = new Function() {
			public long function(long x) {
	    		return 500;
				
				// TODO: exponential distribution in MM1
//	    		return 2000/(x+1) + 100;
	    	}
		};

		// Value of event over time
		Function eventValue = new Function() {
			public long function(long x) {
	    		return 2000 * x; // Decrements by 1 value every 2 seconds
	    	}
		};

		new Simulation(eventWriter, eventValue, agentPeriod, 2, eventPeriod, 500);
//		new Simulation(eventWriter, null, agentPeriod, 2, eventPeriod, 500);
	}
	
	public Simulation(EventWriter eventWriter, Function eventValue, Function agentPeriod, int totalAgents, Function eventPeriod, int maxEventsGenerated) {
		super();
		this.eventWriter = eventWriter;
		this.eventValueFunction = eventValue;
		this.agentPeriod = agentPeriod;
		this.eventPeriod = eventPeriod;
		this.maxEventsGenerated = maxEventsGenerated;
		
		graph = new EventGraph(5,5);
		System.out.println(graph);

		graphFrame = new SimulationFrame(this);
		
		setupAgents(totalAgents);
		
		// Add an event every x seconds
		randomEventTimer = new VariableTimer();
		randomEventTimer.scheduleAtVariableRate(new RandomEventTask(graph), eventPeriod);
	}
	
	private void setupAgents(int totalAgents) {
		
		agents = new HashSet<Agent>();
		agentTimers = new HashSet<VariableTimer>();

		// TODO: currently only handles 1 or 2 agents
		if (totalAgents == 1) {
			// Create an agent in the top left of the graph
			Vertex topLeftVertex = graph.vertexArray[0][0];
			Agent agent = new Agent("A", graph, agentPeriod, topLeftVertex, graph.edges);
			agents.add(agent);

			// Move the agent every x seconds
			VariableTimer agentMovementTimer = new VariableTimer();
			agentTimers.add(agentMovementTimer);
			agent.move();
			agentMovementTimer.scheduleAtVariableRate(new AgentMoveTask(agent), agentPeriod);
		} else if (totalAgents == 2) {
			Set<EventEdge> topHalf = graph.getEdges(0,0,2,4);
			Set<EventEdge> bottomHalf = graph.getEdges(2,0,4,4);
			
			// Create an agent in the top left of the graph
			Vertex topLeftVertex = graph.vertexArray[0][0];
			Agent agentA = new Agent("A", graph, agentPeriod, topLeftVertex, topHalf);
			agents.add(agentA);

			// Create an agent in the bottom right of the graph
			Vertex bottomRightVertex = graph.vertexArray[4][4];
			Agent agentB = new Agent("B", graph, agentPeriod, bottomRightVertex, bottomHalf);
			agents.add(agentB);

			// Move the agent every x seconds
			VariableTimer agentMovementTimerA = new VariableTimer();
			agentTimers.add(agentMovementTimerA);
			agentA.move();
			agentMovementTimerA.scheduleAtVariableRate(new AgentMoveTask(agentA), agentPeriod);
			
			// Move the agent every x seconds
			VariableTimer agentMovementTimerB = new VariableTimer();
			agentTimers.add(agentMovementTimerB);
			agentB.move();
			agentMovementTimerB.scheduleAtVariableRate(new AgentMoveTask(agentB), agentPeriod);
		}
	}
	
	public double getAverageDelay() {
		double average = 0;
		for (Agent agent : agents) {
			average += agent.getAverageDelay();
		}
		average /= agents.size();
		return average;
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
//			for (Event deadEvent : deadEvents) {
//				deadEventCount++;
//				totalDelay += deadEvent.getLifeSpan();
//			}
		}
	}
	
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
    
    public void endSimulation() {
    	
    	// Cancel timers
    	randomEventTimer.cancel();
    	for (VariableTimer agentTimer : agentTimers) {
    		agentTimer.cancel();
    	}

    	// Collect remaining dead events
    	removeDeadEvents();
    	accumulateAgents();
    	
    	// Write the data
    	if (eventWriter != null) {
    		eventWriter.writeSimulation(this);
    		
        	// Close the file
    		try {
				eventWriter.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    }
}
