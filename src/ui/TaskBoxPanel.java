package ui;

import observers.TaskObserver;
import tasks.Task;

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

    public TaskBoxPanel(String memberName, Task task) {
        this.memberName = memberName;
        this.task = task;

        setLayout(new BorderLayout());

        label = new JLabel();
        updateLabelText(); // Sets the initial label text
        add(label, BorderLayout.NORTH);

        progress = new JProgressBar(0, task.getOriginalDuration());
        progress.setStringPainted(true);
        add(progress, BorderLayout.CENTER);

        JPanel buttons = new JPanel();
        JButton startBtn = new JButton("Start");
        JButton pauseBtn = new JButton("Pause");
        JButton cancelBtn = new JButton("Cancel");
        buttons.add(startBtn);
        buttons.add(pauseBtn);
        buttons.add(cancelBtn);
        add(buttons, BorderLayout.SOUTH);

        startBtn.addActionListener(e -> {
            if (!core.GameClock.isSessionOver()) task.start(memberName);
            else JOptionPane.showMessageDialog(this, "Session is over!");
        });

        pauseBtn.addActionListener(e -> task.pause());
        cancelBtn.addActionListener(e -> task.cancel());

        task.addObserver(this);
    }

    private void updateLabelText() {
        String base = task.getName();

        if (!task.isShared()) {
            label.setText(base + " (can't be shared)");
        } else {
            Set<String> participants = core.Observer.getInstance().getParticipants(task.getName());
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

    @Override
    public void update(String taskName, int timeLeft) {
        SwingUtilities.invokeLater(() -> {
            int remaining = Math.max(0, timeLeft);
            progress.setMaximum(task.getOriginalDuration());
            progress.setValue(task.getOriginalDuration() - remaining);
            progress.setString(taskName + ": " + remaining + "s left");
            updateLabelText(); // Refresh shared info next to name
        });
    }
}
