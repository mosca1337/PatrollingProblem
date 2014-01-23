
public abstract class EventTask {
	
	public Double executionTime;
	
	public EventTask(Double executionTime) {
		this.executionTime = executionTime;
	}
	
	public abstract void run();
}
