//// ✅ FINALIZED: File - src/ui/GameUI.java
//// Fixes live progress update without recreating bars every second
//package ui;
//
//import core.TaskManager;
//import tasks.SimpleTask;
//import tasks.Task;
//
//import javax.swing.*;
//import java.awt.*;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//public class GameUI extends JFrame implements UIObserver {
//    private static final long serialVersionUID = 1L;
//
//    private TaskManager taskManager;
//    private JButton startButton, cancelButton, pauseButton, resumeButton;
//    private JPanel taskPanel;
//    private Map<String, JProgressBar> taskBars;
//
//    public GameUI(TaskManager taskManager) {
//        this.taskManager = taskManager;
//        this.taskManager.addObserver(this);
//        this.taskBars = new HashMap<>();
//
//        setTitle("Task Manager Game");
//        setSize(600, 400);
//        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        setLayout(new BorderLayout());
//
//        taskPanel = new JPanel();
//        taskPanel.setLayout(new BoxLayout(taskPanel, BoxLayout.Y_AXIS));
//        JScrollPane scrollPane = new JScrollPane(taskPanel);
//        add(scrollPane, BorderLayout.CENTER);
//
//        JPanel buttonPanel = new JPanel();
//        startButton = new JButton("Start Tasks");
//        cancelButton = new JButton("Cancel All");
//        pauseButton = new JButton("Pause All");
//        resumeButton = new JButton("Resume All");
//
//        buttonPanel.add(startButton);
//        buttonPanel.add(pauseButton);
//        buttonPanel.add(resumeButton);
//        buttonPanel.add(cancelButton);
//        add(buttonPanel, BorderLayout.SOUTH);
//
//        startButton.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                taskManager.executeTasks();
//            }
//        });
//
//        pauseButton.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                taskManager.pauseAll();
//            }
//        });
//
//        resumeButton.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                taskManager.resumeAll();
//            }
//        });
//
//        cancelButton.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                taskManager.shutdown();
//            }
//        });
//
//        Timer refreshTimer = new Timer(1000, e -> update());
//        refreshTimer.start();
//    }
//
//    @Override
//    public void update() {
//        SwingUtilities.invokeLater(() -> {
//            List<Task> taskList = taskManager.getTasks();
//
//            for (Task task : taskList) {
//                if (task instanceof SimpleTask simple) {
//                    int total = simple.getOriginalDuration();
//                    int remaining = simple.getTimeLeft();
//                    int elapsed = total - remaining;
//                    if (elapsed < 0) elapsed = 0;
//                    int percent = (int) (((double) elapsed / total) * 100);
//
//                    JProgressBar bar = taskBars.get(simple.getName());
//                    if (bar == null) {
//                        bar = new JProgressBar(0, 100);
//                        bar.setStringPainted(true);
//                        taskBars.put(simple.getName(), bar);
//                        taskPanel.add(bar);
//                    }
//
//                    bar.setValue(percent);
//                    bar.setString(simple.getName() + " (" + percent + "%)");
//                }
//            }
//
//            taskPanel.revalidate();
//            taskPanel.repaint();
//        });
//    }
//}