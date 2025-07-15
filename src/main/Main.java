package main;

import java.awt.Component;
import javax.swing.*;

public class Main {
    public static void main(String[] argc) {
        JFrame window = new JFrame("Chess");
        window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        window.setResizable(false);
        GamePanel gp = new GamePanel();
        window.add(gp);
        window.pack();
        window.setLocationRelativeTo((Component)null);
        window.setVisible(true);

        gp.launchGame();
    }
}
