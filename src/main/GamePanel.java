package main;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JPanel;

public class GamePanel extends JPanel implements Runnable {
    public static final int WIDTH = 1100;
    public static final int HEIGHT = 800;
    final int FPS = 60;
    Thread gameThread;
    Board board = new Board();
    Mouse mouse = new Mouse();

    public GamePanel() {
        setPreferredSize(new Dimension(1100, 800));

    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D)g;
        this.board.draw(g2);
    }

    public void run() {

    }
}
