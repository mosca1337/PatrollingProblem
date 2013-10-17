import java.util.HashSet;
import java.util.Set;


public class EventGraph {
	public Set<Vertex> vertices;
	public Set<EventEdge> edges;
	public Vertex[][] vertexArray;
	public int width;
	public int height;
	
	public EventGraph(int width, int height) {
		super();
		this.width = width;
		this.height = height;
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
				
				// Horizontal edges
				if (i != (width - 1)) {
					Vertex adjacentVertex = vertexArray[i+1][j];
					String edgeName = new Integer(edgeCount).toString();
					EventEdge edge = new EventEdge(edgeName, thisVertex, adjacentVertex);
					this.addEdge(edge);
					edgeCount++;
				}
				// Vertical edges
				if (j != (height - 1)) {
					Vertex adjacentVertex = vertexArray[i][j+1];
					String edgeName = new Integer(edgeCount).toString();
					EventEdge edge = new EventEdge(edgeName, thisVertex, adjacentVertex);
					this.addEdge(edge);
					edgeCount++;
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
	
	public Set<EventEdge> getAdjacentEdges(Vertex vertex) {
		Set<EventEdge> adjacentEdges = new HashSet<EventEdge>();
		for (EventEdge edge : edges) {
			if (edge.getVertices().contains(vertex)) {
				adjacentEdges.add(edge);
			}
		}
		
		return adjacentEdges;
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
