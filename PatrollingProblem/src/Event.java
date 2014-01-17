import java.util.Date;


public class Event {
	private int priority;
	public Date timeGenerated = new Date();
	public Date timeCollected = null;
	public Date timeOfDeath = null;
	private Function function = null;
	private long lifeSpan = 0;
	
	public Event(int priority) {
		this.priority = priority;
	}
	
	public Event(int priority, Function lifeSpanFunction) {
		this.priority = priority;
		this.function = lifeSpanFunction;
		
		// Decreasing priority
		if (function != null) {
			lifeSpan = Simulation.timeConstant * function.function(1) * priority;
			timeOfDeath = new Date(lifeSpan + timeGenerated.getTime());
		}
	}
	
	public int getPriority() {
		
		// Decreasing priority
		if (function != null && (int) function.function(1) != 0) {
			long currentTime = new Date().getTime() - timeGenerated.getTime();
			double percentOfPriority = (double)currentTime / (double)lifeSpan;
			percentOfPriority = Math.max(0, percentOfPriority);
			percentOfPriority = Math.min(1, percentOfPriority);
			percentOfPriority = 1 - percentOfPriority;
			return (int)Math.ceil(priority * percentOfPriority);
		}
		
		// Constant priority
		return priority;
	}
	
	@Override
	public String toString() {
		return new Integer(priority).toString();
	}
}
