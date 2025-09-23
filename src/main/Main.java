package main;

import javax.swing.*;

public class Main {
    public static void main(String[] argc) {
        JFrame window = new JFrame("Chess");
        window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        window.setResizable(false);
        GamePanel gp = new GamePanel();
        window.add(gp);
        window.pack();
        window.setLocationRelativeTo(null);
        window.setVisible(true);

        gp.launchGame();
    }

    public static void restartApplication() {
        String javaBin = System.getProperty("java.home") + "/bin/java";
        String classPath = System.getProperty("java.class.path");
        String className = Main.class.getName();

        try {
            new ProcessBuilder(javaBin, "-cp", classPath, className).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.exit(0);
    }
}
