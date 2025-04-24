package main;

import javax.swing.*;
import java.awt.*;

public class WelcomePanel extends JPanel {
    public WelcomePanel(MainFrame frame) {
        setLayout(new BorderLayout());

        JLabel title = new JLabel("Welcome to Family Task Manager", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 28));
        add(title, BorderLayout.CENTER);

        JButton startBtn = new JButton("Start Game");
        startBtn.setFont(new Font("SansSerif", Font.BOLD, 20));
        add(startBtn, BorderLayout.SOUTH);

        startBtn.addActionListener(e -> frame.setScreen(new GameViewPanel(frame)));
    }
}