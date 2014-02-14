import java.util.Set;
import java.util.HashSet;


public class Edge {
	public String name;
	public Vertex vertex1;
	public Vertex vertex2;
	private Set<Vertex> vertices = null;
	
	public Edge(String name, Vertex vertex1, Vertex vertex2) {
		this.name = name;
		this.vertex1 = vertex1;
		this.vertex2 = vertex2;
	}
	
	public Vertex getOtherVertex(Vertex vertex) {
		if (vertex == vertex1) {
			return vertex2;
		} else if (vertex == vertex2) {
			return vertex1;
		}
		return null;
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
	
//	@Override
//    public boolean equals(Object obj) {
//        if (obj == null)
//            return false;
//        if (obj == this)
//            return true;
//        if (!(obj instanceof Edge))
//            return false;
//
//        Edge rhs = (Edge) obj;
//        return (this.vertex1 == rhs.vertex1 && this.vertex2 == rhs.vertex2 || this.vertex1 == rhs.vertex2 && this.vertex2 == rhs.vertex1);
//    }

	@Override
	public String toString() {
		return " Edge("+vertex1.toString()+","+vertex2.toString()+")" + name;
	}
}
