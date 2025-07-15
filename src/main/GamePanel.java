package main;

import javax.swing.JPanel;
import java.awt.Dimension;
import java.awt.Graphics;

public class GamePanel extends JPanel {
    public GamePanel() {
        setPreferredSize(new Dimension(800, 800));
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(java.awt.Color.WHITE);
        g.fillRect(0, 0, getWidth(), getHeight());
    }
}