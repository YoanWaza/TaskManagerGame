package main;

import core.AgentRegistry; // ✅ Add this import
import core.GameClock;
import core.GameTimer;
import core.Observer;
import family.Agent;
import tasks.*;
import ui.TaskBoxPanel;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.List;
import java.util.*;

public class GameViewPanel extends JPanel {
    private final Map<String, TaskType> familyMap = Map.of(
        "Dad", TaskType.PARENT,
        "Mom", TaskType.PARENT,
        "Brother", TaskType.CHILD,
        "Sister", TaskType.CHILD,
        "Dog", TaskType.DOG
    );

    private final Map<String, List<Task>> assignedTasks = new HashMap<>();
    private final Map<String, JPanel> memberPanels = new HashMap<>();
    private final JLabel timerLabel = new JLabel("Time Left: 60s", SwingConstants.CENTER);
    private javax.swing.Timer swingTimer;
    private boolean sessionStarted = false;
    private JButton startDayBtn, nextDayBtn;
    private JPanel grid;
    private final Observer observer = core.Observer.getInstance();
    private final List<Agent> currentAgents = new ArrayList<>();
    


    public GameViewPanel(MainFrame frame) {
        AbstractTask.setObserver(observer); // ✅ Needed for task logic
        setLayout(new BorderLayout());
        grid = new JPanel(new GridLayout(1, 5));

        for (String member : familyMap.keySet()) {
            List<Task> tasks = TaskFactory.getRandomTasksForMember(member);
            assignedTasks.put(member, tasks);
            JPanel memberPanel = createMemberPanel(member, tasks);
            grid.add(memberPanel);
            memberPanels.put(member, memberPanel);
        }

        add(grid, BorderLayout.CENTER);
        add(timerLabel, BorderLayout.NORTH);
        add(controlPanel(), BorderLayout.SOUTH);
    }

    private JPanel createMemberPanel(String memberName, List<Task> tasks) {
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY), memberName, TitledBorder.CENTER, TitledBorder.TOP));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        for (Task task : tasks) {
            panel.add(new TaskBoxPanel(memberName, task));
        }
        return panel;
    }

    private JPanel controlPanel() {
        JPanel controls = new JPanel();
        startDayBtn = new JButton("Start the day");
        nextDayBtn = new JButton("Next day");

        startDayBtn.addActionListener(e -> {
            if (!sessionStarted) {
                sessionStarted = true;
                startDayBtn.setEnabled(false);
                GameClock.start();
                new GameTimer(60).startTimer();
                startTimerDisplay();

                for (Map.Entry<String, List<Task>> entry : assignedTasks.entrySet()) {
                    String member = entry.getKey();
                    List<Task> taskList = entry.getValue();

                    Agent agent = new Agent(member, taskList, observer);
                    AgentRegistry.register(member, agent); // Register for TaskBoxPanel to access
                    agent.start(); // Start agent thread
                    currentAgents.add(agent); // Track the agent
                }
            }
        });

        nextDayBtn.addActionListener(e -> resetDay());

        controls.add(startDayBtn);
        controls.add(nextDayBtn);
        return controls;
    }

    private void startTimerDisplay() {
        swingTimer = new javax.swing.Timer(1000, e -> {
            int left = 60 - GameClock.getSecondsElapsed();
            if (left <= 0) {
                timerLabel.setText("Time Left: 0s – Session Over");
                ((javax.swing.Timer) e.getSource()).stop();
            } else {
                timerLabel.setText("Time Left: " + left + "s");
            }
        });
        swingTimer.start();
    }

    private void resetDay() {
    	observer.resetState();
        // Stop all running agents
        for (Agent agent : currentAgents) {
            agent.stopAgent();  // flag stop
            agent.interrupt();  // force wake from wait/sleep
        }
        currentAgents.clear();

        // Stop task threads
//        observer.requestShutdownAllTasks();  // we'll implement this below
        observer.cancelAllTasks();  // 👈 clean, real shutdown


        // Reset GUI and state
        assignedTasks.clear();
        grid.removeAll();
        AgentRegistry.clear();
        
        for (String member : familyMap.keySet()) {
            List<Task> tasks = TaskFactory.getRandomTasksForMember(member);
            assignedTasks.put(member, tasks);
            JPanel panel = createMemberPanel(member, tasks);
            grid.add(panel);
            memberPanels.put(member, panel);
        }

        grid.revalidate();
        grid.repaint();
        sessionStarted = false;
        startDayBtn.setEnabled(true);
        timerLabel.setText("Time Left: 60s");
    }

}
