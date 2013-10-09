import java.util.Set;
import java.util.HashSet;


public class Edge {
	public String name;
	private Vertex vertex1;
	private Vertex vertex2;
	private Set<Vertex> vertices = null;
	
	public Edge(String name, Vertex vertex1, Vertex vertex2) {
		this.name = name;
		this.vertex1 = vertex1;
		this.vertex2 = vertex2;
	}
	
	public Set<Vertex> getVertices() {
		if (vertices == null) {
			HashSet<Vertex> vertices = new HashSet<Vertex>(2);
			vertices.add(vertex1);
			vertices.add(vertex2);
			this.vertices = vertices;
		}
		return vertices;
	}
	
	@Override
	public String toString() {
		return "Edge("+vertex1.toString()+","+vertex2.toString()+")";
	}
}
