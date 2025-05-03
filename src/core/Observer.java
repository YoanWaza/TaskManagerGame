package core;

import tasks.Task;
import tasks.AbstractTask;
import java.util.*;
import family.Agent;

public class Observer {

    private final Map<String, String> memberToTask = new HashMap<>();
    private final Map<String, Set<String>> sharedTaskParticipants = new HashMap<>();
    private final Set<String> completedGlobalTasks = new HashSet<>();
    private final Set<String> busyMembers = new HashSet<>();

    private final Map<String, List<Task>> taskQueue = new HashMap<>();
    private final Map<String, Integer> taskPointers = new HashMap<>();
    private final Map<String, Object> agentLocks = new HashMap<>();
    private final Map<String, RunningTaskInfo> runningSharedTasks = new HashMap<>();
    private final Map<String, Set<String>> cancelledTasksPerAgent = new HashMap<>();

    private boolean dogEating = false;
    private boolean feedDogLocked = false;
    private boolean isDogWalking = false;
    private final Set<String> agentsWithWalkingDogTask = new HashSet<>();

    private static final Observer instance = new Observer();
    public static Observer getInstance() {
        return instance;
    }
    private Observer() {}

    public void registerTasks(String agent, List<Task> tasks) {
        taskQueue.put(agent, tasks);
        taskPointers.put(agent, 0);
        if (tasks.stream().anyMatch(t -> t.getName().equals("Walk Dog"))) {
            agentsWithWalkingDogTask.add(agent);
        }
    }

    public void startFirstTask(String agent) {
        startNextTask(agent);
    }

    public void startNextTask(String agent) {
        if (!taskQueue.containsKey(agent)) return;
        int idx = taskPointers.getOrDefault(agent, 0);
        List<Task> list = taskQueue.get(agent);

        while (idx < list.size()) {
            Task task = list.get(idx);
            if (!isTaskCancelled(agent, task.getName()) && !task.isCompletedBy(agent)) {
                task.start(agent);
                taskPointers.put(agent, idx + 1);
                return;
            }
            idx++;
        }
    }

    public Object getAgentLock(String agentName) {
        return agentLocks.get(agentName);
    }

    public synchronized boolean requestStart(String member, String taskName, boolean isShared, boolean bypassBusyCheck) {
        if (core.GameClock.isSessionOver()) return false;
        
        // Allow task start if bypassBusyCheck is true (for manual mode) or if member is not busy
        if (!bypassBusyCheck && busyMembers.contains(member)) {
            // Special case: allow BeHappy to run simultaneously
            if (!taskName.equals("Be Happy")) {
                return false;
            }
        }

        if (taskName.equals("Walk Dog")) {
            if (member.equals("Dog") && !isDogWalking) {
                isDogWalking = true;
                notifyAgentsToStartWalkingDog();
            }
            else if (!member.equals("Dog") && !isDogWalking) {
                System.out.println("[OBSERVER] Only Dog can start WalkingDogTask first");
                return false;
            }
            else if (member.equals("Dog") && isDogWalking) {
                System.out.println("[OBSERVER] Dog already started WalkingDogTask");
                return false;
            }
        }

        if (taskName.equals("Feed Dog") && (feedDogLocked || dogEating)) return false;
        if (taskName.equals("Dog Eat") && !completedGlobalTasks.contains("Feed Dog")) return false;
        if (taskName.equals("Eat") && !completedGlobalTasks.contains("Cook")) return false;

        if (taskName.equals("Feed Dog")) feedDogLocked = true;
        if (taskName.equals("Dog Eat")) dogEating = true;

        memberToTask.put(member, taskName);
        busyMembers.add(member);

        if (isShared) {
            sharedTaskParticipants.computeIfAbsent(taskName, k -> new HashSet<>()).add(member);
        }

        return true;
    }

    public synchronized void notifyTaskComplete(String member, String taskName, boolean isShared, boolean isGloballyCompleting) {
        System.out.println("[OBSERVER] Task '" + taskName + "' completed by " + member);

        if (taskName.equals("Walk Dog")) {
            isDogWalking = false;
            memberToTask.remove(member);
            busyMembers.remove(member);
            System.out.println("[OBSERVER] WalkingDogTask completed by " + member + ", marking as not busy");
        }

        memberToTask.remove(member);
        busyMembers.remove(member);

        if (isShared && sharedTaskParticipants.containsKey(taskName)) {
            sharedTaskParticipants.get(taskName).remove(member);
            if (sharedTaskParticipants.get(taskName).isEmpty()) {
                sharedTaskParticipants.remove(taskName);
            }
        }

        if (isGloballyCompleting) {
            System.out.println("[OBSERVER] Task '" + taskName + "' is globally completing. Marking as complete.");
            completedGlobalTasks.add(taskName);
            notifyAllIdleAgents();
        }

        if (taskName.equals("Feed Dog")) {
            System.out.println("[OBSERVER] Unlocking 'Feed Dog'");
            feedDogLocked = false;
            if (!busyMembers.contains("Dog")) {
                Object dogLock = agentLocks.get("Dog");
                if (dogLock != null) {
                    System.out.println("[OBSERVER] Notifying Dog that FeedDog is complete.");
                    synchronized (dogLock) {
                        dogLock.notify();
                    }
                }
            }
        }

        if (taskName.equals("Dog Eat")) {
            System.out.println("[OBSERVER] Marking Dog is no longer eating");
            dogEating = false;
        }

        Object lock = agentLocks.get(member);
        if (lock != null) {
            System.out.println("[OBSERVER] Notifying agent " + member + " on lock: " + lock);
            synchronized (lock) {
                lock.notify();
            }
        }

        if (!taskQueue.containsKey(member)) return;

        int pointer = taskPointers.getOrDefault(member, 0);
        List<Task> tasks = taskQueue.get(member);

        if (pointer < tasks.size() && tasks.get(pointer).getName().equals(taskName)) {
            taskPointers.put(member, pointer + 1);
        }
    }

    public synchronized boolean isMemberBusy(String name) {
        return busyMembers.contains(name);
    }

    public void registerAgent(String agentName, Object lock) {
        agentLocks.put(agentName, lock);
    }

    private void notifyAllIdleAgents() {
        for (String agentName : agentLocks.keySet()) {
            if (!busyMembers.contains(agentName)) {
                Object lock = agentLocks.get(agentName);
                if (lock != null) {
                    System.out.println("[OBSERVER] Notifying idle agent " + agentName + " due to global task unlock.");
                    synchronized (lock) {
                        lock.notify();
                    }
                }
            }
        }
    }

    public synchronized void registerRunningTask(String taskName, Thread thread, int duration, String agent) {
        RunningTaskInfo info = new RunningTaskInfo(thread, duration, agent);
        runningSharedTasks.put(taskName, info);
    }

    public synchronized boolean isSharedTaskRunning(String taskName) {
        return runningSharedTasks.containsKey(taskName);
    }

    public synchronized RunningTaskInfo joinSharedTask(String taskName, String agent) {
        RunningTaskInfo info = runningSharedTasks.get(taskName);
        if (info != null) {
            info.addParticipant(agent);
        }
        return info;
    }

    public synchronized void removeSharedTask(String taskName) {
        runningSharedTasks.remove(taskName);
    }

    public synchronized int getParticipantCount(String taskName) {
        RunningTaskInfo info = runningSharedTasks.get(taskName);
        return (info != null) ? info.getParticipantCount() : 1;
    }

    public synchronized Set<String> getParticipants(String taskName) {
        RunningTaskInfo info = runningSharedTasks.get(taskName);
        if (info != null) {
            return new HashSet<>(info.participants);
        }
        return Set.of();
    }

    public synchronized int getSharedRemainingTime(String taskName) {
        RunningTaskInfo info = runningSharedTasks.get(taskName);
        return (info != null) ? info.getRemainingSeconds() : 0;
    }

    public synchronized boolean isTaskCancelled(String agentName, String taskName) {
        return cancelledTasksPerAgent.getOrDefault(agentName, Set.of()).contains(taskName);
    }

    public synchronized void markTaskCancelled(String agentName, String taskName) {
        cancelledTasksPerAgent.computeIfAbsent(agentName, k -> new HashSet<>()).add(taskName);
        Object lock = agentLocks.get(agentName);
        if (lock != null) {
            synchronized (lock) {
                lock.notify();
            }
        }
    }

    public synchronized List<Task> getRemainingTasks(String agentName) {
        List<Task> all = taskQueue.getOrDefault(agentName, List.of());
        int pointer = taskPointers.getOrDefault(agentName, 0);
        return pointer < all.size() ? all.subList(pointer, all.size()) : List.of();
    }

    public synchronized void markTaskStartManual(String agent, String taskName) {
        memberToTask.put(agent, taskName);
        busyMembers.add(agent);
    }

    public synchronized void markTaskEndManual(String agent) {
        memberToTask.remove(agent);
        busyMembers.remove(agent);
    }

    public synchronized void requestShutdownAllTasks() {
        for (List<Task> taskList : taskQueue.values()) {
            for (Task task : taskList) {
                if (task instanceof AbstractTask at) {
                    at.requestStop();
                }
            }
        }
        busyMembers.clear();
        memberToTask.clear();
        sharedTaskParticipants.clear();
        runningSharedTasks.clear();
        System.out.println("[OBSERVER] All tasks shutdown for new day.");
    }

    public synchronized void resetState() {
        completedGlobalTasks.clear();
        feedDogLocked = false;
        dogEating = false;
        isDogWalking = false;
        busyMembers.clear();
        memberToTask.clear();
        sharedTaskParticipants.clear();
        runningSharedTasks.clear();
        cancelledTasksPerAgent.clear();
        taskQueue.clear();
        taskPointers.clear();
        System.out.println("[OBSERVER] All session state cleared.");
    }

    public synchronized void cancelAllTasks() {
        for (List<Task> taskList : taskQueue.values()) {
            for (Task task : taskList) {
                if (task instanceof AbstractTask at) {
                    at.cancel();
                }
            }
        }
        System.out.println("[OBSERVER] All tasks cancelled (via cancel()).");
    }

    private void notifyAgentsToStartWalkingDog() {
        System.out.println("[OBSERVER] Dog started walking, notifying other agents");
        for (String agent : agentsWithWalkingDogTask) {
            if (!agent.equals("Dog")) {
                Object lock = agentLocks.get(agent);
                if (lock != null) {
                    System.out.println("[OBSERVER] Notifying agent " + agent + " to start WalkingDogTask");
                    synchronized (lock) {
                        lock.notify();
                    }
                }
            }
        }
    }

    public synchronized boolean isDogWalking() {
        return isDogWalking;
    }
}
