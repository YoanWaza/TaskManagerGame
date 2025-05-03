package ui;

import javax.swing.*;
import java.awt.*;

public class InstructionFrame extends JFrame {
    private static final int WINDOW_WIDTH = 800;
    private static final int WINDOW_HEIGHT = 600;

    public InstructionFrame() {
        setTitle("Task Manager Game - Instructions");
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JTabbedPane tabbedPane = new JTabbedPane();
        
        // User Guide Tab
        JPanel userGuidePanel = createUserGuidePanel();
        tabbedPane.addTab("📖 How to Play", new ImageIcon(), userGuidePanel);
        
        // Developer Notes Tab
        JPanel devNotesPanel = createDevNotesPanel();
        tabbedPane.addTab("👨‍💻 Developer Notes", new ImageIcon(), devNotesPanel);

        add(tabbedPane);
    }

    private JPanel createUserGuidePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Title
        JLabel titleLabel = new JLabel("How to Play");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(20));

        // Basic Rules Section
        panel.add(createSectionTitle("📜 Basic Rules"));
        panel.add(createBulletPoint("• Each agent (Dad, Mom, Brother, Sister, Dog) has several tasks to complete in one day (60s)"));
        panel.add(createBulletPoint("• An agent can only do one task at a time (except BeHappy, which can run anytime)"));
        panel.add(createBulletPoint("• Eat/DogEat can not be started if nobody finished Cook/FeedDog first"));
        panel.add(createBulletPoint("• ParentRoutine = DoShopping + Cook: parents must do both in one go (Complex Task)"));
        panel.add(createBulletPoint("• FeedDog can not be runned by 2 (or more) agents at once"));
        panel.add(createBulletPoint("• WalkingDog can only be started by the Dog."));
        panel.add(createBulletPoint("• When Dog starts WalkingDog, it triggers to all agents (who has the task too) to join him"));
        panel.add(Box.createVerticalStrut(20));


        // Controls Section
        panel.add(createSectionTitle("🎮 Controls"));
        panel.add(createBulletPoint("• Start: Begin a task"));
        panel.add(createBulletPoint("• Pause: Temporarily stop a task"));
        panel.add(createBulletPoint("• Cancel: Stop a task completely"));
        panel.add(Box.createVerticalStrut(20));

        
        return panel;
    }

    private JPanel createDevNotesPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Title
        JLabel titleLabel = new JLabel("Developer Overview");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(20));

        // Design Patterns Section
        panel.add(createSectionTitle("🔄 Design Patterns"));
        panel.add(createBulletPoint("• Observer Pattern: Notifies agents of task completion and shared task changes"));
        panel.add(createBulletPoint("• Factory Pattern: TaskFactory generates consistent task sets for agents"));
        panel.add(Box.createVerticalStrut(20));

        // Multithreading Section
        panel.add(createSectionTitle("⚡ Multithreading Management"));
        panel.add(createBulletPoint("• Each agent runs as a dedicated thread"));
        panel.add(createBulletPoint("• Tasks (especially shared) are also threads"));
        panel.add(createBulletPoint("• Synchronized methods + shared status flags prevent race conditions"));
        panel.add(Box.createVerticalStrut(20));

        // Core Components Section
        panel.add(createSectionTitle("🔧 Core Components"));
        panel.add(createBulletPoint("• GameClock: Manages global timer"));
        panel.add(createBulletPoint("• TaskBoxPanel: Observes tasks and updates UI"));
        panel.add(createBulletPoint("• Observer: Central coordination point for all agents and tasks"));
        panel.add(Box.createVerticalStrut(20));

        // Task Management Section
        panel.add(createSectionTitle("📋 Task Management"));
        panel.add(createBulletPoint("• Complex tasks use state machines for progression"));
        panel.add(createBulletPoint("• Shared tasks use participant counting and time synchronization"));
        panel.add(createBulletPoint("• Task cancellation and pausing are handled through the Observer"));

        return panel;
    }

    private JLabel createSectionTitle(String title) {
        JLabel label = new JLabel(title);
        label.setFont(new Font("Arial", Font.BOLD, 18));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private JLabel createBulletPoint(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.PLAIN, 14));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 0));
        return label;
    }
} 