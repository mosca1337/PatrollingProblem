import java.util.Date;
import java.util.HashSet;
import java.util.Set;


public class EventEdge extends Edge {
	private Set<Event> events;

	public EventEdge(String name, Vertex vertex1, Vertex vertex2) {
		super(name, vertex1, vertex2);
		this.name = name;
		events = new HashSet<Event>();
	}
	
	public void addEvent(Event event) {
		events.add(event);
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
		events = new HashSet<Event>();
	}
	
	public int getPriority() {
		int totalPriority = 0;
		for (Event event : events) {
			totalPriority += event.getPriority();
		}
		return totalPriority;
	}
	
	public Set<Event> removeDeadEvents() {
		Set<Event> deadEvents = new HashSet<Event>();
		Set<Event> liveEvents = new HashSet<Event>();
		for (Event event : events) {
			if (event.getPriority() <= 0) {
				deadEvents.add(event);
			} else {
				liveEvents.add(event);
			}
		}
		events = liveEvents;
		return deadEvents;
	}
	
	@Override
	public String toString() {
		return "[Edge:"+(new Integer(getPriority()).toString()) + "]";
	}
}
