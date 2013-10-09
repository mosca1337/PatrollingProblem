
public class Event {
	public int priority;
	
	public Event(int priority) {
		this.priority = priority;
	}

	@Override
	public String toString() {
		return new Integer(priority).toString();
	}
}
