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

        // Shuffle and select 3 additional tasks (excluding complex one if added)
        Collections.shuffle(pool);
        for (Task task : pool) {
            if (selected.size() >= 4) break;
            selected.add(task);
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
            
            Task doShop = new DoShoppingTask();
            Task cook = new CookTask();
            doShop.markShared();
            cook.markShared();
            list.add(createComplexTask("ParentRoutine", List.of(doShop, cook)));
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
