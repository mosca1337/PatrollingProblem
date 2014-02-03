import java.util.Date;
import java.util.HashSet;
import java.util.Set;


public class EventEdge extends Edge {
	private Set<Event> events;
	public boolean isBeingTraversed = false;

	public EventEdge(String name, Vertex vertex1, Vertex vertex2) {
		super(name, vertex1, vertex2);
		this.name = name;
		events = new HashSet<Event>();
	}
	
	public void addEvent(Event event) {
		events.add(event);
	}
	
	public Set<Event> collectEvents(double time) {
		
		// Set time collected for each event
		for (Event event : events) {
			event.timeCollected = time;
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
	
	public int getPriority(double time) {
		int totalPriority = 0;
		for (Event event : events) {
			totalPriority += event.getPriority(time);
		}
		return totalPriority;
	}
	
	public Set<Event> removeDeadEvents(double time) {
		Set<Event> deadEvents = new HashSet<Event>();
		Set<Event> liveEvents = new HashSet<Event>();
		synchronized (events) {
			for (Event event : events) {
				if (event.getPriority(time) <= 0) {
					deadEvents.add(event);
				} else {
					liveEvents.add(event);
				}
			}
		}
		events = liveEvents;
		return deadEvents;
	}
	
//	@Override
//	public String toString() {
//		return "[Edge:"+(new Integer(getPriority()).toString()) + "]";
//	}
}
