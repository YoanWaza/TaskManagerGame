package family;

import core.GameClock;
import core.Observer;
import tasks.Task;
import tasks.AbstractTask;

import java.util.List;

public class Agent extends Thread {
    private final String name;
    private final List<Task> tasks;
    private final Observer observer;
    private Task currentTask;
    private Task pausedTask;  // New field to store paused task

    private boolean automaticRun = true;
    private boolean stop = false;

    public Agent(String name, List<Task> tasks, Observer observer) {
        super("Agent-" + name);
        this.name = name;
        this.tasks = tasks;
        this.observer = observer;
        observer.registerAgent(name, this); // register for notify()
    }

    public void switchToManualMode() {
        automaticRun = false;
    }

    public void switchToAutomaticMode() {
        automaticRun = true;
    }

    public boolean isAutomatic() {
        return automaticRun;
    }

    public void stopAgent() {
        stop = true;
    }

    public Task getCurrentTask() {
        return currentTask;
    }

    public void setCurrentTask(Task t) {
        currentTask = t;
    }

    @Override
    public void run() {
        System.out.println("Agent " + name + " started thread " + Thread.currentThread().getId());

        while (GameClock.isRunning() && !stop) {
            boolean didSomething = false;

            if (!automaticRun) {
                try {
                    synchronized (this) {
                        System.out.println("Agent " + name + " is in manual mode. Waiting...");
                        wait();
                    }
                } catch (InterruptedException e) {
                    return;
                }
                continue;
            }

            // Check if dog started walking and we have WalkingDogTask
            if (observer.isDogWalking() && hasWalkingDogTask() && currentTask != null && !currentTask.getName().equals("Walk Dog")) {
                handleDogStartedWalking();
                didSomething = true;
                continue;
            }

            // If we have a paused task and dog is not walking, resume it
            if (pausedTask != null && !observer.isDogWalking()) {
                System.out.println("[AGENT] " + name + " resuming paused task: " + pausedTask.getName());
                currentTask = pausedTask;
                pausedTask = null;
                if (currentTask instanceof AbstractTask) {
                    ((AbstractTask) currentTask).resume();
                } else {
                    currentTask.start(name);
                }
                didSomething = true;
                continue;
            }

            for (Task task : tasks) {
                if (GameClock.isSessionOver()) break;
                if (task.isCompletedBy(name) || observer.isTaskCancelled(name, task.getName())) continue;
                if (observer.isMemberBusy(name)) break;

                boolean canStart = observer.requestStart(name, task.getName(), task.isShared(), false);
                if (canStart) {
                    currentTask = task;
                    task.start(name);
                    didSomething = true;
                    break;
                }
            }

            if (!didSomething) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    break;
                }
            }
        }

        System.out.println("Agent " + name + " finished session.");
    }

    private boolean hasWalkingDogTask() {
        return tasks.stream().anyMatch(t -> t.getName().equals("Walk Dog"));
    }

    private void handleDogStartedWalking() {
        if (currentTask != null && !currentTask.getName().equals("Walk Dog")) {
            System.out.println("[AGENT] " + name + " pausing current task to walk dog");
            pausedTask = currentTask;
            if (currentTask instanceof AbstractTask) {
                ((AbstractTask) currentTask).pause();
            }
        }

        // Find and start WalkingDogTask
        for (Task task : tasks) {
            if (task.getName().equals("Walk Dog") && !task.isCompletedBy(name)) {
                currentTask = task;
                task.start(name);
                // Ensure automatic mode is maintained
                switchToAutomaticMode();
                break;
            }
        }
    }
}
