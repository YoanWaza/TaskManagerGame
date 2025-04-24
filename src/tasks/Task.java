package tasks;

import observers.TaskObserver;

public interface Task extends Runnable {
    void start(String agentName);
    void pause();
    void resume();
    void cancel();
    boolean isCompletedBy(String agentName);
    String getName();
    TaskType getTaskType();
    int getOriginalDuration();
    boolean isShared();
    void markShared();
    void addObserver(TaskObserver observer);

}