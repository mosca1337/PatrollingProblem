import java.util.HashSet;
import java.util.Set;


public class EventEdge extends Edge {
	private Set<Event> events;
	private int totalPriority = 0;

	public EventEdge(String name, Vertex vertex1, Vertex vertex2) {
		super(name, vertex1, vertex2);
		this.name = name;
		events = new HashSet<Event>();
	}
	
	public void addEvent(Event event) {
		events.add(event);
		totalPriority += event.priority;
	}
	
	public Set<Event> getEvents() {
		return events;
	}
	
	public int clearEvents() {
		int priority = totalPriority;
		totalPriority = 0;
		events = new HashSet<Event>();
		return priority;
	}
	
	public int getPriority() {
		return totalPriority;
	}
	
	@Override
	public String toString() {
		return "[Priority:"+(new Integer(totalPriority).toString()) + "]";
	}
}
