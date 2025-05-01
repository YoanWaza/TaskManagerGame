package observers;

public interface TaskObserver {
	void update(String taskName, int secondsRemaining);
}