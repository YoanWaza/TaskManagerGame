package tasks;

import core.GameClock;
import core.Observer;
import observers.TaskObserver;

import java.util.HashSet;
import java.util.Set;

public class ParentRoutine extends ComplexTask {
    private static final Observer observer = Observer.getInstance();
    private int totalDuration;
    private int currentProgress;
    private static int activeRoutines = 0;
    private static final Object routineLock = new Object();
    private final Set<String> completedBy = new HashSet<>();
    private static Task currentSubtask = null;
    private int startTime;

    public ParentRoutine() {
        super("ParentRoutine");
        // Add the required subtasks
        Task doShopping = new DoShoppingTask();
        Task cook = new CookTask();
        doShopping.markShared();
        cook.markShared();
        addSubtask(doShopping);
        addSubtask(cook);
        
        // Calculate total duration
        totalDuration = doShopping.getOriginalDuration() + cook.getOriginalDuration();
    }

    @Override
    public void addObserver(TaskObserver obs) {
        // Only add observer to ParentRoutine, not to subtasks
        observers.add(obs);
    }

    @Override
    public void start(String agentName) {
        synchronized (routineLock) {
            activeRoutines++;
        }
        startTime = GameClock.getSecondsElapsed();
        super.start(agentName);
    }

    @Override
    public void run() {
        this.currentAgent = extractAgentNameFromThread();
        int now = GameClock.getSecondsElapsed();
        System.out.println("[T+" + now + "s] " + currentAgent + " started parent routine");

        try {
            // Notify start of the complex task
            observer.markTaskStartManual(currentAgent, getName());

            // Execute subtasks sequentially
            for (Task sub : getSubtasks()) {
                if (GameClock.isSessionOver()) return;

                // Update current subtask
                synchronized (routineLock) {
                    currentSubtask = sub;
                }

                // Start the subtask without any observers
                sub.start(currentAgent);

                // Wait for completion while updating progress
                while (!sub.isCompletedBy(currentAgent)) {
                    if (GameClock.isSessionOver()) return;
                    
                    // Calculate total progress based on elapsed time and number of participants
                    int elapsed = GameClock.getSecondsElapsed() - startTime;
                    currentProgress = Math.min(totalDuration, elapsed * activeRoutines);
                    int remaining = Math.max(0, totalDuration - currentProgress);
                    
                    // Update progress for ParentRoutine only with custom format
                    for (TaskObserver obs : observers) {
                        obs.update("doing " + currentSubtask.getName() + " - " + remaining + "s", remaining);
                    }

                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        return;
                    }
                }
            }

            // Mark task as complete
            completedBy.add(currentAgent);
            observer.notifyTaskComplete(currentAgent, getName(), true, false);
            System.out.println("[T+" + GameClock.getSecondsElapsed() + "s] " + currentAgent + " completed parent routine");

        } finally {
            synchronized (routineLock) {
                activeRoutines--;
                if (activeRoutines == 0) {
                    currentSubtask = null;
                }
            }
        }

        // Notify agent to continue
        Object lock = observer.getAgentLock(currentAgent);
        if (lock != null) {
            synchronized (lock) {
                lock.notify();
            }
        }
    }

    @Override
    public int getOriginalDuration() {
        return totalDuration;
    }

    @Override
    public TaskType getTaskType() {
        return TaskType.PARENT;
    }

    @Override
    public boolean isCompletedBy(String agentName) {
        return completedBy.contains(agentName);
    }
} 