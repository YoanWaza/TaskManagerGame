package ui;

import core.AgentRegistry;
import core.Observer;
import family.Agent;
import observers.TaskObserver;
import tasks.Task;
import tasks.AbstractTask;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class TaskBoxPanel extends JPanel implements TaskObserver {
    private final String memberName;
    private final Task task;
    private final JProgressBar progress;
    private final JLabel label;
    private final JPanel buttons;

    private final JButton startBtn;
    private final JButton pauseBtn;
    private final JButton cancelBtn;

    public TaskBoxPanel(String memberName, Task task) {
        this.memberName = memberName;
        this.task = task;

        setLayout(new BorderLayout());

        label = new JLabel();
        updateLabelText();
        add(label, BorderLayout.NORTH);

        progress = new JProgressBar(0, task.getOriginalDuration());
        progress.setStringPainted(true);
        add(progress, BorderLayout.CENTER);

        buttons = new JPanel();

        startBtn = new JButton("Start");
        pauseBtn = new JButton("Pause");
        cancelBtn = new JButton("Cancel");

        pauseBtn.setEnabled(false); // Can't pause until running

        buttons.add(startBtn);
        buttons.add(pauseBtn);
        buttons.add(cancelBtn);
        add(buttons, BorderLayout.SOUTH);

        startBtn.addActionListener(e -> {
            if (core.GameClock.isSessionOver()) {
                JOptionPane.showMessageDialog(this, "Session is over!");
                return;
            }

            Agent agent = AgentRegistry.get(memberName);

            // Special handling for WalkingDogTask
            if (task.getName().equals("Walk Dog")) {
                if (memberName.equals("Dog")) {
                    // Dog can always start WalkingDogTask
                    if (task instanceof AbstractTask absTask) {
                        if (absTask.isPaused()) {
                            absTask.resume();
                        } else {
                            // Notify Observer that Dog is starting to walk
                            Observer.getInstance().requestStart(memberName, task.getName(), task.isShared(), true);
                            task.start(memberName);
                        }
                    } else {
                        // Notify Observer that Dog is starting to walk
                        Observer.getInstance().requestStart(memberName, task.getName(), task.isShared(), true);
                        task.start(memberName);
                    }
                    startBtn.setEnabled(false);
                    pauseBtn.setEnabled(true);
                    agent.switchToAutomaticMode();
                    synchronized (agent) {
                        agent.notify();
                    }
                    return;
                } else {
                    JOptionPane.showMessageDialog(this, "Only Dog can start WalkingDogTask first!");
                    return;
                }
            }

            // Check if we can start the task through Observer
            // Use bypassBusyCheck=true if agent is in manual mode or if task is paused
            boolean bypassBusyCheck = !agent.isAutomatic() || (task instanceof AbstractTask absTask && absTask.isPaused());
            boolean canStart = Observer.getInstance().requestStart(memberName, task.getName(), task.isShared(), bypassBusyCheck);
            
            if (!canStart) {
                if (task.getName().equals("Eat")) {
                    JOptionPane.showMessageDialog(this, "Cannot start Eat task before Cook is completed!");
                } else if (task.getName().equals("Dog Eat")) {
                    JOptionPane.showMessageDialog(this, "Cannot start Dog Eat task before Feed Dog is completed!");
                } else {
                    JOptionPane.showMessageDialog(this, "Cannot start this task at the moment!");
                }
                return;
            }

            if (task instanceof AbstractTask absTask) {
                if (absTask.isPaused()) {
                    absTask.resume();
                } else {
                    absTask.start(memberName);
                }
            } else {
                task.start(memberName);
            }

            startBtn.setEnabled(false);
            pauseBtn.setEnabled(true);

            agent.switchToAutomaticMode();
            synchronized (agent) {
                agent.notify();
            }
        });

        pauseBtn.addActionListener(e -> {
            if (task.getName().equals("Walk Dog")) {
                JOptionPane.showMessageDialog(this, "Cannot pause WalkingDogTask!");
                return;
            }
            task.pause();
            pauseBtn.setEnabled(false);
            startBtn.setEnabled(true);
            AgentRegistry.get(memberName).switchToManualMode();
        });

        cancelBtn.addActionListener(e -> {
            if (task.getName().equals("Walk Dog")) {
                JOptionPane.showMessageDialog(this, "Cannot cancel WalkingDogTask!");
                return;
            }
            task.cancel();
            progress.setForeground(Color.RED);
            progress.setString(task.getName() + " – canceled");
            Observer.getInstance().markTaskCancelled(memberName, task.getName());
            disableButtons(buttons);
        });

        if (task.getName().equals("Walk Dog")) {
            pauseBtn.setEnabled(false);
            cancelBtn.setEnabled(false);
        }

        task.addObserver(this);
    }

    private void updateLabelText() {
        String base = task.getName();

        if (!task.isShared()) {
            label.setText(base + " (can't be shared)");
        } else {
            Set<String> participants = Observer.getInstance().getParticipants(task.getName());
            List<String> others = participants.stream()
                    .filter(name -> !name.equals(memberName))
                    .collect(Collectors.toList());

            if (others.isEmpty()) {
                label.setText(base + " (shared with nobody)");
            } else {
                label.setText(base + " (shared with " + String.join(", ", others) + ")");
            }
        }
    }

    private void disableButtons(JPanel buttonsPanel) {
        for (Component comp : buttonsPanel.getComponents()) {
            if (comp instanceof JButton button) {
                button.setEnabled(false);
            }
        }
    }

    @Override
    public void update(String taskName, int timeLeft) {
        SwingUtilities.invokeLater(() -> {
            if (Observer.getInstance().isTaskCancelled(memberName, task.getName())) {
                progress.setValue(0);
                progress.setForeground(Color.RED);
                progress.setString(task.getName() + " – canceled");
                disableButtons(buttons);
                return;
            }

            int remaining = task.isShared()
                    ? Observer.getInstance().getSharedRemainingTime(task.getName())
                    : Math.max(0, timeLeft);

            progress.setMaximum(task.getOriginalDuration());

            if (timeLeft == -1) {
                progress.setValue(0);
                progress.setForeground(Color.RED);
                progress.setString(taskName + " – canceled");
                disableButtons(buttons);
            } else if (remaining == 0) {
                progress.setValue(task.getOriginalDuration());
                progress.setForeground(Color.GREEN);
                progress.setString(taskName + " – completed");
                disableButtons(buttons);
            } else {
                progress.setValue(task.getOriginalDuration() - remaining);
                progress.setForeground(UIManager.getColor("ProgressBar.foreground"));
                progress.setString(taskName + ": " + remaining + "s left");

                // Lock/unlock Start/Pause buttons based on task state
                if (task instanceof AbstractTask absTask) {
                    if (absTask.isPaused()) {
                        startBtn.setEnabled(true);
                        pauseBtn.setEnabled(false);
                    } else {
                        startBtn.setEnabled(false);
                        pauseBtn.setEnabled(true);
                    }
                }
            }

            updateLabelText();
        });
    }
}
