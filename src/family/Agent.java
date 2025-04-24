package family;

import core.GameClock;
import core.Observer;
import tasks.Task;

import java.util.List;

public class Agent extends Thread {
    private final String name;
    private final List<Task> tasks;
    private final Observer observer;
    private final Object lock = new Object(); // agent's private wait/notify lock

    public Agent(String name, List<Task> tasks, Observer observer) {
        super("Agent-" + name);
        this.name = name;
        this.tasks = tasks;
        this.observer = observer;

        observer.registerAgent(name, lock); // register lock with Observer
    }

    @Override
    public void run() {
        System.out.println("Agent " + name + " started thread " + Thread.currentThread().getId());

        while (GameClock.isRunning()) {
            boolean didSomething = false;

            for (Task task : tasks) {
                if (GameClock.isSessionOver()) break;
                if (task.isCompletedBy(name)) continue;
                if (observer.isMemberBusy(name)) continue;

                boolean canStart = observer.requestStart(name, task.getName(), task.isShared(), false);
                if (canStart) {
                    task.start(name);
                    didSomething = true;
                    break;
                }
            }

            // If nothing to do, wait to be notified
            if (!didSomething && GameClock.isRunning()) {
                synchronized (lock) {
                    try {
                        System.out.println("Agent " + name + " is idle, waiting for a new task...");
                        lock.wait(); // wait until observer wakes you up
                        System.out.println("Agent " + name + " lock was released, re-entering task loop.");
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            }
        }

        System.out.println("Agent " + name + " finished session.");
    }

    public Object getLock() {
        return lock;
    }
}
