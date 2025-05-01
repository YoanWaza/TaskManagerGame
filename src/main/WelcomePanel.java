package main;

import javax.swing.*;
import java.awt.*;

public class WelcomePanel extends JPanel {
    public WelcomePanel(MainFrame frame) {
        setLayout(new GridBagLayout()); // Centers everything

        // Panel to hold the content vertically
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false); // Let background show

        // Title label
        JLabel title = new JLabel("Welcome to Family Task Manager", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 28));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setForeground(new Color(30, 30, 30)); // Dark gray text

        // Space between elements
        content.add(Box.createRigidArea(new Dimension(0, 40)));
        content.add(title);
        content.add(Box.createRigidArea(new Dimension(0, 40)));

        // Start button
        JButton startBtn = new JButton("Start Game");
        startBtn.setFont(new Font("SansSerif", Font.BOLD, 20));
        startBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        startBtn.setFocusPainted(false);
        startBtn.setBackground(new Color(70, 130, 180));
        startBtn.setForeground(Color.WHITE);
        startBtn.setPreferredSize(new Dimension(200, 50));

        // Add hover effect
        startBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                startBtn.setBackground(new Color(100, 149, 237));
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                startBtn.setBackground(new Color(70, 130, 180));
            }
        });

        content.add(startBtn);
        content.add(Box.createRigidArea(new Dimension(0, 40)));

        add(content); // Add to center of main layout

        // Action listener
        startBtn.addActionListener(e -> frame.setScreen(new GameViewPanel(frame)));

        // Optional: Set background color or image
        setBackground(new Color(245, 245, 245)); // Light gray
    }
}
//package main;
//
//import javax.swing.*;
//import java.awt.*;
//
//public class WelcomePanel extends JPanel {
//    private Image backgroundImage;
//
//    public WelcomePanel(MainFrame frame) {
//        // Load image
////        backgroundImage = new ImageIcon(getClass().getResource("/images/welcome.png")).getImage();
//    	backgroundImage = new ImageIcon(getClass().getResource("/resources/images/welcome.png")).getImage();
//
//
//
//        // Transparent layout for layering components
//        setLayout(new GridBagLayout());
//
//        // Button
//        JButton startBtn = new JButton("Start Game");
//        startBtn.setFont(new Font("SansSerif", Font.BOLD, 20));
//        startBtn.setFocusPainted(false);
//        startBtn.setBackground(new Color(70, 130, 180));
//        startBtn.setForeground(Color.WHITE);
//        startBtn.setPreferredSize(new Dimension(200, 50));
//
//        startBtn.addActionListener(e -> frame.setScreen(new GameViewPanel(frame)));
//
//        // Add button to center
//        add(startBtn);
//        setOpaque(false);
//    }
//
//    @Override
//    protected void paintComponent(Graphics g) {
//        super.paintComponent(g);
//        // Draw image to fill the panel
//        g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
//    }
//}
