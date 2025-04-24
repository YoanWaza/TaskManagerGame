//package core;
//
//import tasks.Task;
//import tasks.SimpleTask;
//import tasks.TaskException;
//import java.util.concurrent.*;
//import java.util.*;
//import ui.UIObserver;
//
//public class TaskManager {
//    private List<Task> tasks;
//    private ExecutorService executor;
//    private List<UIObserver> observers;
//
//    public TaskManager() {
//        this.tasks = new ArrayList<>();
//        this.executor = Executors.newCachedThreadPool();
//        this.observers = new ArrayList<>();
//    }
//
//    public void addTask(Task task) {
//        tasks.add(task);
//        notifyObservers();
//    }
//
//    public void executeTasks() {
//        for (Task task : tasks) {
//            executor.submit(() -> {
//                try {
//                    task.start();
//                } catch (RuntimeException e) {
//                    if (e.getCause() instanceof TaskException) {
//                        System.err.println("[Task Error] " + e.getCause().getMessage());
//                    } else {
//                        System.err.println("[Unexpected Error] " + e.getMessage());
//                    }
//                }
//                notifyObservers();
//            });
//        }
//    }
//
//    public void shutdown() {
//        executor.shutdownNow();
//        for (Task task : tasks) {
//            task.cancel();
//        }
//    }
//
//    public void pauseAll() {
//        for (Task task : tasks) {
//            if (task instanceof SimpleTask) {
//                try {
//                    ((SimpleTask) task).pause();
//                } catch (RuntimeException e) {
//                    if (e.getCause() instanceof TaskException) {
//                        System.err.println("[Pause Error] " + e.getCause().getMessage());
//                    }
//                }
//            }
//        }
//    }
//
//    public void resumeAll() {
//        for (Task task : tasks) {
//            if (task instanceof SimpleTask) {
//                try {
//                    ((SimpleTask) task).resume();
//                } catch (RuntimeException e) {
//                    if (e.getCause() instanceof TaskException) {
//                        System.err.println("[Resume Error] " + e.getCause().getMessage());
//                    }
//                }
//            }
//        }
//    }
//
//    public List<Task> getTasks() {
//        return tasks;
//    }
//
//    public void addObserver(UIObserver observer) {
//        observers.add(observer);
//    }
//
//    private void notifyObservers() {
//        for (UIObserver observer : observers) {
//            observer.update();
//        }
//    }
//}