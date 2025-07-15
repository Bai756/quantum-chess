package main;

import java.awt.Component;
import javax.swing.JFrame;

public class Main {
    public static void main(String[] argc) {
        JFrame window = new JFrame("Chess");
        window.setDefaultCloseOperation(3);
        window.setResizable(false);
        GamePanel gp = new GamePanel();
        window.add(gp);
        window.pack();
        window.setLocationRelativeTo((Component)null);
        window.setVisible(true);
    }
}


