import java.util.Date;
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
	
	public Set<Event> collectEvents() {
		
		// Set time collected for each event
		Date currentTime = new Date();
		for (Event event : events) {
			event.timeCollected = currentTime;
		}
		
		Set<Event> collectedEvents = new HashSet<Event>(events);
		clearEvents();

		return collectedEvents;
	}
	
	public Set<Event> getEvents() {
		return events;
	}
	
	public void clearEvents() {
		totalPriority = 0;
		events = new HashSet<Event>();
	}
	
	public int getPriority() {
		return totalPriority;
	}
	
	@Override
	public String toString() {
		return "[Edge:"+(new Integer(totalPriority).toString()) + "]";
	}
}
