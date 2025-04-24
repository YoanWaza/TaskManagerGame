package main;

import javax.swing.*;

public class MainFrame extends JFrame {
    public MainFrame() {
        setTitle("Family Task Simulator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setResizable(false);

        setContentPane(new WelcomePanel(this));
    }

    public void setScreen(JPanel panel) {
        setContentPane(panel);
        revalidate();
        repaint();
    }
}