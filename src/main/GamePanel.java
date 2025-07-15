package main;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.ImageObserver;
import java.util.ArrayList;
import javax.swing.JPanel;
//import piece.Bishop;
//import piece.King;
//import piece.Knight;
//import piece.Pawn;
//import piece.Piece;
//import piece.Queen;
//import piece.Rook;

public class GamePanel extends JPanel implements Runnable {
    public static final int WIDTH = 1100;
    public static final int HEIGHT = 800;
    final int FPS = 60;
    Thread gameThread;
    Board board = new Board();
    Mouse mouse = new Mouse();
    public static ArrayList<Piece> pieces = new ArrayList<>();
    public static ArrayList<Piece> simPieces = new ArrayList<>();
    ArrayList<Piece> promotionP = new ArrayList<>();
    Piece activeP;
    Piece checkingP;
    public static Piece castlingP;
    public static final int WHITE = 0;
    public static final int BLACK = 1;
    int currentColor = 0;
    boolean canMove;
    boolean validSquare;
    boolean promotion;
    boolean gameover;
    boolean stalemate;

    public GamePanel() {
        setPreferredSize(new Dimension(1100, 800));
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(java.awt.Color.WHITE);
        g.fillRect(0, 0, getWidth(), getHeight());
    }

    public void run() {
        pass;
    }
}