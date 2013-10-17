import java.util.Random;


public class Simulation {
	public final int maxPriority = 10;
	
	public EventGraph graph;
	public Agent agentA;

	public static void main(String[] args) {
		Simulation simulation = new Simulation();
		new GraphFrame(simulation);
	}
	
	public Simulation() {
		super();
		
		graph = new EventGraph(4,4);
		System.out.println(graph);
		
		Vertex topLeftVertex = graph.vertexArray[0][0];
		agentA = new Agent("A", graph, topLeftVertex);
		agentA.move();

		createRandomEvent(graph);
	}
	
	public void createRandomEvent(EventGraph graph) {
		// Select random edge
        Random rand = new Random();
        int randomInt = rand.nextInt(graph.edges.size());
        EventEdge edge = (EventEdge) graph.edges.toArray()[randomInt];
        
        // Create random priority
        int randomPriority = rand.nextInt(maxPriority);
        Event event = new Event(randomPriority);
        edge.addEvent(event);
	}
}
