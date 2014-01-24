import java.util.Date;


public class Event {
	private int priority;
	public double timeGenerated;
	public double timeCollected;
	public double timeOfDeath;
	private Function function = null;
	private long lifeSpan = 0;
	
	public Event(int priority) {
		this.priority = priority;
	}
	
	public Event(int priority, double timeGenerated, Function lifeSpanFunction) {
		this.priority = priority;
		this.timeGenerated = timeGenerated;
		this.function = lifeSpanFunction;
		
		// Decreasing priority
		if (function != null) {
			lifeSpan = Simulation.timeConstant * function.function(1) * priority;
			timeOfDeath = lifeSpan + timeGenerated;
		}
	}
	
	public int getPriority(double time) {
		
		// Decreasing priority
		if (function != null && (int) function.function(1) != 0) {
			double currentTime = time - timeGenerated;
			double percentOfPriority = currentTime / lifeSpan;
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
