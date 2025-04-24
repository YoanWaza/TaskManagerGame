package tasks;

import core.GameClock;
import core.Observer;
import observers.TaskObserver;

import java.util.ArrayList;
import java.util.List;

public class ComplexTask implements Task {
    private final String name;
    private final List<Task> subtasks = new ArrayList<>();
    private final List<TaskObserver> observers = new ArrayList<>();
    private boolean started = false;
    private boolean shared = true;
    private static Observer observer = core.Observer.getInstance();
    private String currentAgent;

    public ComplexTask(String name) {
        this.name = name;
    }

    public void addSubtask(Task t) {
        subtasks.add(t);
        for (TaskObserver obs : observers) {
            t.addObserver(obs);
        }
    }

    public List<Task> getSubtasks() {
        return subtasks;
    }

    @Override
    public void addObserver(TaskObserver obs) {
        observers.add(obs);
        for (Task sub : subtasks) {
            sub.addObserver(obs);
        }
    }

    @Override
    public void run() {
        String threadName = Thread.currentThread().getName();
        String agentName = threadName.contains("::") ? threadName.split("::")[0] : threadName;
        this.currentAgent = agentName;

        int now = GameClock.getSecondsElapsed();
//        boolean access = observer.requestStart(agentName, name, true, name.equals("Be Happy"));
//        if (!access) {
//            System.out.println("[T+" + now + "s] " + agentName + " (" + Thread.currentThread().getName() + ") tried to start complex task: " + name + " but was blocked.");
//            return;
//        }

        System.out.println("[T+" + now + "s] " + agentName + " (" + Thread.currentThread().getName() + ") started complex task: " + name + " containing " + subtasks.size() + " tasks");

        for (Task sub : subtasks) {
            sub.markShared();
            sub.start(agentName);

            while (!sub.isCompletedBy(agentName)) {
                if (GameClock.isSessionOver()) return;
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    return;
                }
            }
        }

        observer.notifyTaskComplete(agentName, name, true, false);
        System.out.println("[T+" + GameClock.getSecondsElapsed() + "s] " + agentName + " completed complex task: " + name);
    }

    @Override
    public void start(String agentName) {
        if (!started) {
            started = true;
            Thread t = new Thread(this);
            t.setName(agentName + "::" + name);
            t.start();
        }
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
