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

    protected volatile boolean paused = false;
    protected volatile boolean cancelled = false;
    protected volatile boolean stopRequested = false;
    protected int currentProgress = 0;
    protected final Object lock = new Object();

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
        if (!cancelled) {
            new Thread(this, agentName).start();
        }
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
    public void pause() {
        paused = true;
        System.out.println("[TASK] " + name + " paused.");
    }
    
    public boolean isPaused() {
        return paused;
    }


    @Override
    public void resume() {
        synchronized (lock) {
            paused = false;
            lock.notify();
        }
    }

    @Override
    public void cancel() {
        cancelled = true;
        observer.markTaskCancelled(currentAgent, name);
        synchronized (lock) {
            lock.notify();
        }
        for (TaskObserver obs : observers) {
            obs.update(name, -1); // indicate cancelled
        }
        if (observer != null) {
            observer.notifyTaskComplete(currentAgent, name, shared, false);
        }
    }

    @Override
    public void run() {
        runWithNotify(currentAgent);
    }

    protected void runWithNotify(String agentName) {
        String threadName = Thread.currentThread().getName();
        int startTime = GameClock.getSecondsElapsed();

        if (cancelled) return;

        System.out.println("[T+" + startTime + "s] " + threadName + " started simple task: " + name);

        if (shared && observer.isSharedTaskRunning(name)) {
            RunningTaskInfo info = observer.joinSharedTask(name, agentName);
            System.out.println("[T+" + GameClock.getSecondsElapsed() + "s] " + agentName +
                    " joined shared task: " + name + " (elapsed: " + info.getElapsedSeconds() + ", participants: " + info.getParticipantCount() + ")");

            while (observer.isSharedTaskRunning(name) && !GameClock.isSessionOver()) {
                if (cancelled) return;
                try {
                    Thread.sleep(500);
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

        if (shared) {
            observer.registerRunningTask(name, Thread.currentThread(), duration, agentName);
        }

        while (currentProgress < duration && !GameClock.isSessionOver() && !stopRequested) {
            if (cancelled) return;

            synchronized (lock) {
                while (paused && !cancelled) {
                    System.out.println("[TASK] " + name + " is paused. Waiting...");
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        return;
                    }
                }
            }

            int participants = shared ? observer.getParticipantCount(name) : 1;
//            System.out.println("[T+" + GameClock.getSecondsElapsed() + "s] " + threadName +
//                    " doing " + name + ": progress " + currentProgress + " / " + duration +
//                    " (participants: " + participants + ")");

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                return;
            }

            currentProgress += 1 * participants;
            int remaining = Math.max(0, duration - currentProgress);

            for (TaskObserver obs : observers) {
                obs.update(name, remaining);
            }
        }

        if (cancelled) return;

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
        return Set.of();
    }

    protected boolean isGloballyCompleting() {
        return name.equals("Household") || name.equals("Cook") || name.equals("Do Shopping") || name.equals("Feed Dog");
    }
    
    public void requestStop() {
        stopRequested = true;
        synchronized (lock) {
            lock.notify(); // wake if paused
        }
    }
}