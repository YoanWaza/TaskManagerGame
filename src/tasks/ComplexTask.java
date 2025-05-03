package tasks;

import core.GameClock;
import core.Observer;
import observers.TaskObserver;

import java.util.ArrayList;
import java.util.List;

public class ComplexTask implements Task {
    private final String name;
    private final List<Task> subtasks = new ArrayList<>();
    protected final List<TaskObserver> observers = new ArrayList<>();
    private boolean started = false;
    private boolean shared = true;
    protected static final Observer observer = core.Observer.getInstance();
    protected String currentAgent;

    public ComplexTask(String name) {
        this.name = name;
    }

    public void addSubtask(Task t) {
        subtasks.add(t);
    }

    public List<Task> getSubtasks() {
        return subtasks;
    }

    @Override
    public void addObserver(TaskObserver obs) {
        observers.add(obs);
    }

    @Override
    public void start(String agentName) {
        if (!started) {
            started = true;
            Thread t = new Thread(this);
            t.setName("Agent-" + agentName + "::" + name);
            t.start();
        }
    }

    @Override
    public void run() {
        this.currentAgent = extractAgentNameFromThread();

        int now = GameClock.getSecondsElapsed();
        System.out.println("[T+" + now + "s] " + currentAgent + " (" + Thread.currentThread().getName() + ") started complex task: " + name + " containing " + subtasks.size() + " tasks");

        for (Task sub : subtasks) {
            sub.markShared();

            //  Manually tell observer this subtask is being executed
            observer.markTaskStartManual(currentAgent, sub.getName());

            sub.start(currentAgent);

            while (!sub.isCompletedBy(currentAgent)) {
                if (GameClock.isSessionOver()) return;
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    return;
                }
            }

            observer.markTaskEndManual(currentAgent);
        }

        observer.notifyTaskComplete(currentAgent, name, true, false);
        System.out.println("[T+" + GameClock.getSecondsElapsed() + "s] " + currentAgent + " completed complex task: " + name);

        List<Task> remaining = observer.getRemainingTasks(currentAgent);
        System.out.println("[DEBUG] " + currentAgent + " remaining tasks: " +
            (remaining.isEmpty() ? "none" : remaining.stream().map(Task::getName).toList()));

        Object lock = observer.getAgentLock(currentAgent);
        if (lock != null) {
            synchronized (lock) {
                lock.notify();  // let agent continue
            }
        }
    }

    protected String extractAgentNameFromThread() {
        String threadName = Thread.currentThread().getName();
        return threadName.contains("::") ? threadName.split("::")[0].replace("Agent-", "") : threadName;
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void cancel() {}
    @Override public boolean isCompletedBy(String agentName) { return false; }

    @Override public String getName() { return name; }
    @Override public TaskType getTaskType() { return null; }

    @Override public int getOriginalDuration() {
        return subtasks.stream().mapToInt(Task::getOriginalDuration).sum();
    }

    @Override public boolean isShared() { return shared; }
    @Override public void markShared() { this.shared = true; }
}
