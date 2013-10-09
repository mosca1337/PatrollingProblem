import java.util.HashSet;
import java.util.Set;


public class EventGraph {
	private Set<Vertex> vertices;
	private Set<EventEdge> edges;
	private Vertex[][] vertexArray;
	
	public EventGraph() {
		super();
	}
	
	public EventGraph(int width, int height) {
		this();
		vertices = new HashSet<Vertex>(width * height);
		edges = new HashSet<EventEdge>();
		vertexArray = new Vertex[width][height];
		
		// Create a grid of vertices
		int vertexCount = 0;
		for (int i=0; i < width; i++) {
			for (int j=0; j < height; j++) {
				Vertex vertex = new Vertex(new Integer(vertexCount).toString());
				this.addVertex(vertex);
				vertexArray[i][j] = vertex;
				vertexCount++;
			}
		}
		
		// Attach edges to the vertices
		int edgeCount = 0;
		for (int i=0; i < width; i++) {
			for (int j=0; j < height; j++) {
				Vertex thisVertex = vertexArray[i][j];
				// Up,down,left,right edges
				for (int horizontal=(i-1); horizontal < (i+2); horizontal++) {
					for (int vertical=(j-1); vertical < (j+2); vertical++) {
						
						if (horizontal > -1 && horizontal < width &&
							vertical > -1 && vertical < height) {
							Vertex adjacentVertex = vertexArray[horizontal][vertical];
							if (!adjacentVertex.equals(thisVertex)) {
								String edgeName = new Integer(edgeCount).toString();
								EventEdge edge = new EventEdge(edgeName, thisVertex, adjacentVertex);
								this.addEdge(edge);
								edgeCount++;
							}
						}
					}
				}
			}
		}
	}
	
	public void addVertex(Vertex vertex) {
		vertices.add(vertex);
	}
	
	public void addEdge(EventEdge edge) {
		// Are the vertices of the edge in the graph?
		boolean validEdge = true;
		for (Vertex vertex : edge.getVertices()) {
			if (!vertices.contains(vertex)) {
				validEdge = true;
			}
		}
		if (validEdge) {
			edges.add(edge);
		}
	}
	
	public boolean isAdjacent(Vertex vertex1, Vertex vertex2) {
		// TODO: Find a faster implementation
		for (Edge edge : edges) {
			// Does any edge in the graph contain these two vertices?
			if (edge.getVertices().contains(vertex1) &&
				edge.getVertices().contains(vertex2)) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public String toString() {
		String string = "Vertices:\n";
		for (Vertex vertex : vertices) {
			string += vertex.toString() + "\n";
		}
		string += "Edges:\n";
		for (EventEdge edge : edges) {
			string += edge.toString() + "\n";
		}
		return string;
	}
}
