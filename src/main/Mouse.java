package main;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class Mouse extends MouseAdapter {
    public int x;
    public int y;
    public boolean pressed;

    public void mousePressed(MouseEvent e) {
        this.pressed = true;
    }

    public void mouseReleased(MouseEvent e) {
        this.pressed = false;
    }

    public void mouseDragged(MouseEvent e) {
        this.x = e.getX();
        this.y = e.getY();
    }

    public void mouseMoved(MouseEvent e) {
        this.x = e.getX();
        this.y = e.getY();
    }
}