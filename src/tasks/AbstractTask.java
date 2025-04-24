package tasks;

import core.GameClock;
import core.Observer;
import core.RunningTaskInfo;
import observers.TaskObserver;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class AbstractTask implements Task {
    protected String name;
    protected int duration;
    protected boolean shared = false;
    protected TaskType type;
    protected Set<String> completedBy = new HashSet<>();
    protected static Observer observer;
    protected String currentAgent;
    protected List<TaskObserver> observers = new ArrayList<>();

    public AbstractTask(String name, int duration, TaskType type) {
        this.name = name;
        this.duration = duration;
        this.type = type;
    }

    public static void setObserver(Observer obs) {
        observer = obs;
    }

    @Override
    public void start(String agentName) {
        this.currentAgent = agentName;
        new Thread(this, agentName).start();
    }

    @Override
    public void addObserver(TaskObserver obs) {
        observers.add(obs);
    }

    @Override
    public void markShared() {
        shared = true;
    }

    @Override
    public boolean isShared() {
        return shared;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public TaskType getTaskType() {
        return type;
    }

    @Override
    public int getOriginalDuration() {
        return duration;
    }

    @Override
    public boolean isCompletedBy(String agentName) {
        return completedBy.contains(agentName);
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void cancel() {}

    @Override
    public void run() {
        runWithNotify(currentAgent);
    }

    protected void runWithNotify(String agentName) {
        String threadName = Thread.currentThread().getName();
        int startTime = GameClock.getSecondsElapsed();
        System.out.println("[T+" + startTime + "s] " + threadName + " started simple task: " + name);

        if (shared && observer.isSharedTaskRunning(name)) {
            RunningTaskInfo info = observer.joinSharedTask(name, agentName);
            System.out.println("[T+" + GameClock.getSecondsElapsed() + "s] " + agentName +
                " joined shared task: " + name + " (elapsed: " + info.getElapsedSeconds() + ", participants: " + info.getParticipantCount() + ")");

            // Wait until the shared thread completes
            while (observer.isSharedTaskRunning(name) && !GameClock.isSessionOver()) {
                try {
                    Thread.sleep(500); // check every half second
                } catch (InterruptedException e) {
                    return;
                }
            }

            completedBy.add(agentName);
            System.out.println("[T+" + GameClock.getSecondsElapsed() + "s] " + agentName + " finished shared task: " + name);
            if (observer != null) {
                observer.notifyTaskComplete(agentName, name, true, isGloballyCompleting());
            }
            return;
        }


        // This is the agent that owns the actual thread execution
        if (shared) {
            observer.registerRunningTask(name, Thread.currentThread(), duration, agentName);
        }

        double progress = 0.0;

        while (progress < duration && !GameClock.isSessionOver()) {
            int currentParticipants = shared ? observer.getParticipantCount(name) : 1;

            System.out.println("[T+" + GameClock.getSecondsElapsed() + "s] " + threadName +
                " doing " + name + ": progress " + String.format("%.2f", progress) +
                " / " + duration + " (participants: " + currentParticipants + ")");

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                return;
            }

            progress += 1.0 * currentParticipants;

            for (TaskObserver obs : observers) {
                obs.update(name, (int) Math.ceil(duration - progress));
            }
        }

        completedBy.add(agentName);
        System.out.println("[T+" + GameClock.getSecondsElapsed() + "s] " + threadName + " finished task: " + name);

        if (shared && observer.isSharedTaskRunning(name)) {
            observer.removeSharedTask(name);
        }

        if (observer != null) {
            observer.notifyTaskComplete(agentName, name, shared, isGloballyCompleting());
        }
    }

    public Set<String> getParticipants() {
        if (observer != null && isShared()) {
            return observer.getParticipants(name);
        }
        return Set.of(); // or just empty if not shared
    }




    protected boolean isGloballyCompleting() {
        return name.equals("Household") || name.equals("Cook") || name.equals("Do Shopping") || name.equals("Feed Dog");
    }
}
