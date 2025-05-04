package tasks;

import java.util.*;

public class TaskFactory {
    private static final Random random = new Random();

    public static List<Task> getRandomTasksForMember(String memberName) {
        List<Task> pool = getEligibleTasksFor(memberName);
        List<Task> selected = new ArrayList<>();

//        if (memberName.equals("Mom") || memberName.equals("Dad")) {
//            // Add complex task first for parents
//        	Task doShop = new DoShoppingTask();
//            Task cook = new CookTask();
//            doShop.markShared();
//            cook.markShared();
//            selected.add(createComplexTask("ParentRoutine", List.of(
//                doShop, cook
//            )));
//        }

        // Special handling for Dog - ensure WalkingDogTask is included but in random position
        if (memberName.equals("Dog")) {
            // Remove WalkingDogTask from pool since we'll add it in a random position
            pool.removeIf(task -> task.getName().equals("Walk Dog"));
            
            // Shuffle remaining tasks
            Collections.shuffle(pool);
            
            // Randomly choose position for WalkingDogTask (0 to 3)
            int walkingDogPosition = random.nextInt(4);
            
            // Add tasks up to the random position
            for (int i = 0; i < walkingDogPosition; i++) {
                if (i < pool.size()) {
                    selected.add(pool.get(i));
                }
            }
            
            // Add WalkingDogTask at the chosen position
            selected.add(new WalkingDogTask());
            
            // Add remaining tasks
            for (int i = walkingDogPosition; i < pool.size() && selected.size() < 4; i++) {
                selected.add(pool.get(i));
            }
        } else {
            // Normal handling for other agents
            Collections.shuffle(pool);
            for (Task task : pool) {
                if (selected.size() >= 4) break;
                selected.add(task);
            }
        }

        return selected;
    }

    private static List<Task> getEligibleTasksFor(String name) {
        List<Task> list = new ArrayList<>();

        // Group B (Mom, Dad)
        if (name.equals("Mom") || name.equals("Dad")) {
            list.add(new WalkingDogTask());
            list.add(new BeHappyTask());
            list.add(new RestTimeTask());
            list.add(new EatTask());
            list.add(new FeedDogTask());
            list.add(new HouseholdTask());
            list.add(new ParentRoutine());
        }

        // Group A (Brother, Sister)
        else if (name.equals("Brother") || name.equals("Sister")) {
            list.add(new WalkingDogTask());
            list.add(new BeHappyTask());
            list.add(new RestTimeTask());
            list.add(new EatTask());
            list.add(new FeedDogTask());
            list.add(new HouseholdTask());
            list.add(new HomeworkTask());
        }

        // Dog
        else if (name.equals("Dog")) {
            list.add(new WalkingDogTask());
            list.add(new BeHappyTask());
            list.add(new RestTimeTask());
            list.add(new NapTask());
            list.add(new DogEatTask());
        }

        // Fallback (shouldn't happen)
        else {
            list.add(new BeHappyTask());
            list.add(new RestTimeTask());
        }

        return list;
    }

    public static ComplexTask createComplexTask(String name, List<Task> subtasks) {
        ComplexTask complex = new ComplexTask(name);
        for (Task t : subtasks) complex.addSubtask(t);
        return complex;
    }
}
