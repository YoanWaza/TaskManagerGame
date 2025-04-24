package core;

import tasks.Task;

import java.util.*;

public class Observer {

    private final Map<String, String> memberToTask = new HashMap<>();
    private final Map<String, Set<String>> sharedTaskParticipants = new HashMap<>();
    private final Set<String> completedGlobalTasks = new HashSet<>();
    private final Set<String> busyMembers = new HashSet<>();

    private final Map<String, List<Task>> taskQueue = new HashMap<>();
    private final Map<String, Integer> taskPointers = new HashMap<>();
    private final Map<String, Object> agentLocks = new HashMap<>();
    private final Map<String, RunningTaskInfo> runningSharedTasks = new HashMap<>();


    private boolean dogEating = false;
    private boolean feedDogLocked = false;
    
    //SINGLETON
    private static final Observer instance = new Observer();

    public static Observer getInstance() {
        return instance;
    }
    private Observer() {
        // prevent external instantiation
    }

    public void registerTasks(String agent, List<Task> tasks) {
        taskQueue.put(agent, tasks);
        taskPointers.put(agent, 0);
    }

    public void startFirstTask(String agent) {
        startNextTask(agent);
    }

    public void startNextTask(String agent) {
        if (!taskQueue.containsKey(agent)) return;
        int idx = taskPointers.getOrDefault(agent, 0);
        List<Task> list = taskQueue.get(agent);

        if (idx < list.size()) {
            Task task = list.get(idx);
            taskPointers.put(agent, idx + 1);
            task.start(agent);
        }
    }

    public synchronized boolean requestStart(String member, String taskName, boolean isShared, boolean bypassBusyCheck) {
        if (core.GameClock.isSessionOver()) return false;

        if (!bypassBusyCheck && busyMembers.contains(member)) return false;

        if (taskName.equals("Feed Dog") && (feedDogLocked || dogEating)) return false;
        if (taskName.equals("Dog Eat") && !completedGlobalTasks.contains("Feed Dog")) return false;
        if (taskName.equals("Eat") && !completedGlobalTasks.contains("Cook")) return false;

        if (taskName.equals("Walk Dog")) {
            if (member.equals("Dog")) {
                boolean humanWalking = sharedTaskParticipants.getOrDefault("Walk Dog", Set.of()).stream()
                        .anyMatch(name -> !name.equals("Dog"));
                if (!humanWalking) return false;
            } else {
                boolean dogAlreadyWalking = sharedTaskParticipants.getOrDefault("Walk Dog", Set.of()).contains("Dog");
                if (!dogAlreadyWalking && !memberToTask.containsKey("Dog")) return false;
            }
        }

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
            notifyAllIdleAgents(); // Wake up all idle agents to recheck logic

        }

        if (taskName.equals("Feed Dog")) {
            System.out.println("[OBSERVER] Unlocking 'Feed Dog'");
            feedDogLocked = false;
            
         // 🔔 Notify the dog ONLY (if idle)
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

        // 🔔 Notify the corresponding agent to wake up and recheck tasks
        Object lock = agentLocks.get(member);
        if (lock != null) {
            System.out.println("[OBSERVER] Notifying agent " + member + " on lock: " + lock);
            synchronized (lock) {
                lock.notify();
            }
        } else {
            System.out.println("[OBSERVER] No lock found for agent " + member + ". Cannot notify.");
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



}
