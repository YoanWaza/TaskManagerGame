package family;

import core.GameClock;
import core.Observer;
import tasks.Task;
import tasks.ComplexTask;

import java.util.List;

public class Agent extends Thread {
    private final String name;
    private final List<Task> tasks;
    private final Observer observer;

    private boolean automaticRun = true; // default mode
    private boolean stop = false;

    public Agent(String name, List<Task> tasks, Observer observer) {
        super("Agent-" + name);
        this.name = name;
        this.tasks = tasks;
        this.observer = observer;
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

    @Override
    public void run() {
        System.out.println("Agent " + name + " started thread " + Thread.currentThread().getId());

        while (GameClock.isRunning() && !stop) {
            boolean didSomething = false;

            if (!automaticRun) {
                try {
                    synchronized (this) {
                        System.out.println("Agent " + name + " is in manual mode. Waiting...");
                        wait(); // manual mode → wait until GUI tells us to resume
                    }
                } catch (InterruptedException e) {
                    return;
                }
                continue;
            }

            for (Task task : tasks) {
                if (GameClock.isSessionOver()) break;
                if (task.isCompletedBy(name) || observer.isTaskCancelled(name, task.getName())) continue;
                if (observer.isMemberBusy(name)) break;

                boolean canStart = observer.requestStart(name, task.getName(), task.isShared(), false);
                if (canStart) {
                	task.start(name); // ✅ let the task start itself, like simple tasks

                    didSomething = true;
                    break;
                }
            }

            if (!didSomething) {
                try {
                    Thread.sleep(500); // idle wait
                } catch (InterruptedException e) {
                    break;
                }
            }
        }

        System.out.println("Agent " + name + " finished session.");
    }
}
