//package tasks;
//
//import core.GameClock;
//
//import java.util.HashSet;
//import java.util.Set;
//
//public class SimpleTask implements Task {
//    private String name;
//    private int duration;
//    private int originalDuration;
//    private boolean paused;
//    private boolean cancelled;
//    private int timeLeft;
//    private boolean started;
//
//    private boolean inUse = false;
//    private boolean shared = false;
//    private int activeCount = 0;
//
//    private final Set<String> completedBy = new HashSet<>();
//
//    public SimpleTask(String name, int duration) {
//        this.name = name;
//        this.duration = duration;
//        this.originalDuration = duration;
//        this.paused = false;
//        this.cancelled = false;
//        this.timeLeft = duration;
//        this.started = false;
//    }
//
//    public SimpleTask(String name) {
//        this(name, 10);
//    }
//
//    public synchronized void start(String agentName) {
//        if (completedBy.contains(agentName) || cancelled) {
//            log(agentName + " tried to start " + name + " but it is already completed or cancelled.");
//            return;
//        }
//
//        if (!shared) {
//            if (inUse) {
//                log(agentName + " tried to start " + name + " but it's already in use.");
//                return;
//            }
//            inUse = true;
//            log(agentName + " started simple task " + name);
//        } else {
//            log(agentName + " joined shared task " + name);
//        }
//
//        activeCount++;
//
//        new Thread(() -> {
//            int remaining = timeLeft;
//            while (remaining > 0 && !cancelled) {
//                if (!paused) {
//                    try {
//                        Thread.sleep(1000);
//                        int decrement = shared && activeCount > 0 ? Math.max(1, 1 / activeCount) : 1;
//                        remaining -= decrement;
//                    } catch (InterruptedException e) {
//                        return;
//                    }
//                } else {
//                    try {
//                        Thread.sleep(200);
//                    } catch (InterruptedException e) {
//                        return;
//                    }
//                }
//            }
//            if (!cancelled) {
//                complete(agentName);
//            }
//            reset();
//            log(agentName + " finished task " + name);
//        }).start();
//    }
//
//    private synchronized void complete(String agentName) {
//        if (!completedBy.contains(agentName)) {
//            completedBy.add(agentName);
//        }
//    }
//
//    private synchronized void reset() {
//        inUse = false;
//        shared = false;
//        activeCount = 0;
//    }
//
//    @Override
//    public void start() {
//        start("Unknown");
//    }
//
//    @Override
//    public void complete() {
//        // unused since we're now agent-specific
//    }
//
//    @Override
//    public void cancel() {
//        cancelled = true;
//    }
//
//    @Override
//    public boolean isCompleted() {
//        return false; // use per-agent logic
//    }
//
//    public boolean isCompletedBy(String agentName) {
//        return completedBy.contains(agentName);
//    }
//
//    public void pause() {
//        paused = true;
//    }
//
//    public void resume() {
//        paused = false;
//    }
//
//    public int getTimeLeft() {
//        return timeLeft;
//    }
//
//    public int getOriginalDuration() {
//        return originalDuration;
//    }
//
//    public String getName() {
//        return name;
//    }
//
//    public synchronized boolean isAvailable() {
//        return !inUse && !cancelled && !shared;
//    }
//
//    public synchronized void markShared() {
//        shared = true;
//    }
//
//    public synchronized boolean isShared() {
//        return shared;
//    }
//
//    private void log(String message) {
//        System.out.println("[T+" + GameClock.getSecondsElapsed() + "s] " + message);
//    }
//}