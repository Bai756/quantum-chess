package main;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import javax.swing.JPanel;
import javax.swing.JButton;

import piece.Bishop;
import piece.King;
import piece.Knight;
import piece.Pawn;
import piece.Piece;
import piece.Queen;
import piece.Rook;

public class GamePanel extends JPanel implements Runnable {
    public static final int WIDTH = 1100;
    public static final int HEIGHT = 800;
    final int FPS = 60;
    ChatMain chatPanel = new ChatMain();
    Thread gameThread;
    Board board = new Board();
    Mouse mouse = new Mouse();
    public static ArrayList<Piece> pieces = new ArrayList<>();
    public static ArrayList<Piece> simPieces = new ArrayList<>();
    ArrayList<Piece> promotionP = new ArrayList<>();
    Piece activeP;
    public static Piece castlingP;
    public static final int WHITE = 0;
    public static final int BLACK = 1;
    int currentColor = WHITE;
    boolean canMove;
    boolean validSquare;
    boolean promotion;
    boolean gameOver;
    boolean stalemate;

    private final JPanel moveChoicePanel;
    private boolean awaitingMoveChoice = false;

    public GamePanel() {
        this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        this.setBackground(Color.black);
        this.addMouseMotionListener(this.mouse);
        this.addMouseListener(this.mouse);
        this.setPieces();
        this.copyPieces(pieces, simPieces);
        this.setLayout(null);
        chatPanel.setBounds(800, 400, 300, 400);
        this.add(chatPanel);

        moveChoicePanel = new JPanel();
        moveChoicePanel.setLayout(null);
        moveChoicePanel.setBounds(820, 600, 250, 80); // Position it as needed
        moveChoicePanel.setVisible(false);

        JButton regularButton = new JButton("Regular");
        regularButton.setBounds(0, 0, 120, 40);
        JButton splitButton = new JButton("Split");
        splitButton.setBounds(130, 0, 120, 40);

        moveChoicePanel.add(regularButton);
        moveChoicePanel.add(splitButton);
        this.add(moveChoicePanel);

        regularButton.addActionListener(e -> handleRegularMove());
        splitButton.addActionListener(e -> handleSplitMove());


    }

    public void launchGame() {
        this.gameThread = new Thread(this);
        this.gameThread.start();
    }

    public void setPieces() {
        // Initialize white pawns
//        for (int col = 0; col < 8; col++) {
//            pieces.add(new Pawn(WHITE, col, 2));
//        }

        pieces.add(new Pawn(WHITE, 0, 3));
        pieces.add(new Pawn(WHITE, 1, 3));
        pieces.add(new Pawn(WHITE, 2, 3));
        pieces.add(new Pawn(WHITE, 3, 3));

        pieces.get(0).probability = 0.25;
        pieces.get(1).probability = 0.25;
        pieces.get(2).probability = 0.25;
        pieces.get(3).probability = 0.25;

        pieces.get(0).connectedPieces.add(pieces.get(1));
        pieces.get(0).connectedPieces.add(pieces.get(2));
        pieces.get(0).connectedPieces.add(pieces.get(3));

        pieces.get(1).connectedPieces.add(pieces.get(0));
        pieces.get(1).connectedPieces.add(pieces.get(2));
        pieces.get(1).connectedPieces.add(pieces.get(3));

        pieces.get(2).connectedPieces.add(pieces.get(0));
        pieces.get(2).connectedPieces.add(pieces.get(1));
        pieces.get(2).connectedPieces.add(pieces.get(3));

        pieces.get(3).connectedPieces.add(pieces.get(0));
        pieces.get(3).connectedPieces.add(pieces.get(1));
        pieces.get(3).connectedPieces.add(pieces.get(2));

        // Initialize black pawns in row 2
        pieces.add(new Pawn(BLACK, 0, 2));
        pieces.add(new Pawn(BLACK, 1, 2));
        pieces.add(new Pawn(BLACK, 2, 2));
        pieces.add(new Pawn(BLACK, 3, 2));

        pieces.get(4).probability = 0.25;
        pieces.get(5).probability = 0.25;
        pieces.get(6).probability = 0.25;
        pieces.get(7).probability = 0.25;

        pieces.get(4).connectedPieces.add(pieces.get(5));
        pieces.get(4).connectedPieces.add(pieces.get(6));
        pieces.get(4).connectedPieces.add(pieces.get(7));

        pieces.get(5).connectedPieces.add(pieces.get(4));
        pieces.get(5).connectedPieces.add(pieces.get(6));
        pieces.get(5).connectedPieces.add(pieces.get(7));

        pieces.get(6).connectedPieces.add(pieces.get(4));
        pieces.get(6).connectedPieces.add(pieces.get(5));
        pieces.get(6).connectedPieces.add(pieces.get(7));

        pieces.get(7).connectedPieces.add(pieces.get(4));
        pieces.get(7).connectedPieces.add(pieces.get(5));
        pieces.get(7).connectedPieces.add(pieces.get(6));

        // Initialize white major pieces
        pieces.add(new Rook(WHITE, 0, 7));
        pieces.add(new Knight(WHITE, 1, 7));
        pieces.add(new Bishop(WHITE, 2, 7));
        pieces.add(new Queen(WHITE, 3, 7));
        pieces.add(new King(WHITE, 4, 7));
        pieces.add(new Bishop(WHITE, 5, 7));
        pieces.add(new Knight(WHITE, 6, 7));
        pieces.add(new Rook(WHITE, 7, 7));

        // Initialize black pawns
        for (int col = 0; col < 8; col++) {
            pieces.add(new Pawn(BLACK, col, 1));
        }
        // Initialize black major pieces
        pieces.add(new Rook(BLACK, 0, 0));
        pieces.add(new Knight(BLACK, 1, 0));
        pieces.add(new Bishop(BLACK, 2, 0));
        pieces.add(new Queen(BLACK, 3, 0));
        pieces.add(new King(BLACK, 4, 0));
        pieces.add(new Bishop(BLACK, 5, 0));
        pieces.add(new Knight(BLACK, 6, 0));
        pieces.add(new Rook(BLACK, 7, 0));
    }

    private void copyPieces(ArrayList<Piece> source, ArrayList<Piece> target) {
        target.clear();
        target.addAll(source);
    }

    @Override
    public void run() {
        double drawInterval = 1_000_000_000.0 / FPS;
        double delta = 0;
        long lastTime = System.nanoTime();

        while (gameThread != null) {
            long currentTime = System.nanoTime();
            delta += (currentTime - lastTime) / drawInterval;
            lastTime = currentTime;
            if (delta >= 1) {
                update();
                repaint();
                delta--;
            }
        }
    }

    private void update() {
        boolean success = false;
        if (promotion) {
            promoting();
        }
        else if (!gameOver) {
            if (mouse.pressed) {
                if (activeP == null) {
                    for (Piece piece : simPieces) {
                        if (piece.color == currentColor && piece.col == mouse.x / 100 && piece.row == mouse.y / 100) {
                            activeP = piece;
                            break;
                        }
                    }
                } else {
                    simulate();
                }
            }

            if (!mouse.pressed && activeP != null) {
                if (validSquare && !awaitingMoveChoice) {
                    awaitingMoveChoice = true;
                    int x = activeP.x;
                    int y = activeP.y;
                    moveChoicePanel.setBounds(x, y, 250, 40);
                    moveChoicePanel.setVisible(true);
                } else if (validSquare) {
                  
                    if (activeP.hittingP != null) {
                        success = SuperPosition.resolveCapture(activeP, activeP.hittingP);
                    }

                    // If capture successful, copy the simulation else go back to original pieces
                    if (success) {
                        copyPieces(simPieces, pieces);
                    } else {
                        copyPieces(pieces, simPieces);
                    }

                    activeP.updatePosition();
                    if (castlingP != null) {
                        castlingP.updatePosition();

                    }
                    if (isKingCaptured()) {
                        gameOver = true;
                    } else if (canPromote()) {
                        promotion = true;
                        return;
                    } else if (isDrawByInsufficientMaterial()) {
                        stalemate = true;
                    } else {
                        changePlayer();
                    }
                } else {
                    copyPieces(pieces, simPieces);
                    activeP.resetPosition();
                }
                activeP = null;
            }

        }
    }

    private void simulate() {
        canMove = false;
        validSquare = false;
        copyPieces(pieces, simPieces);

        if (castlingP != null) {
            castlingP.col = castlingP.preCol;
            castlingP.x = castlingP.getX(castlingP.col);
            castlingP = null;
        }

        activeP.x = mouse.x - 50;
        activeP.y = mouse.y - 50;
        activeP.col = activeP.getCol(activeP.x);
        activeP.row = activeP.getRow(activeP.y);

        if (activeP.canMove(activeP.col, activeP.row)) {
            canMove = true;
            if (activeP.hittingP != null) {
                simPieces.remove(activeP.hittingP.getIndex());
            }
            checkCastling();
            validSquare = true;
        }
    }

    private boolean isKingCaptured() {
        boolean whiteKing = false;
        boolean blackKing = false;
        for (Piece piece : simPieces) {
            if (piece.type == Type.KING) {
                if (piece.color == WHITE) whiteKing = true;
                if (piece.color == BLACK) blackKing = true;
            }
        }
        return !(whiteKing && blackKing);
    }

    private boolean isDrawByInsufficientMaterial() {
        int whiteBishops = 0, blackBishops = 0;
        int whiteKnights = 0, blackKnights = 0;
        int whiteOthers = 0, blackOthers = 0;

        for (Piece piece : simPieces) {
            if (piece.type == Type.KING) continue;
            if (piece.color == WHITE) {
                if (piece.type == Type.BISHOP) {
                    whiteBishops++;
                } else if (piece.type == Type.KNIGHT) {
                    whiteKnights++;
                } else {
                    whiteOthers++;
                }
            } else {
                if (piece.type == Type.BISHOP) {
                    blackBishops++;
                } else if (piece.type == Type.KNIGHT) {
                    blackKnights++;
                } else {
                    blackOthers++;
                }
            }
        }

        // King vs King
        if (whiteBishops == 0 && whiteKnights == 0 && whiteOthers == 0 &&
                blackBishops == 0 && blackKnights == 0 && blackOthers == 0) {
            return true;
        }
        // King vs King + Bishop/Knights (1 or 2)
        if (whiteOthers == 0 && blackOthers == 0) {
            if ((whiteBishops == 1 && whiteKnights == 0 && blackBishops == 0 && blackKnights == 0) ||
                    (whiteBishops == 0 && whiteKnights == 1 && blackBishops == 0 && blackKnights == 0) ||
                    (whiteBishops == 0 && whiteKnights == 2 && blackBishops == 0 && blackKnights == 0) ||
                    (whiteBishops == 0 && whiteKnights == 0 && blackBishops == 1 && blackKnights == 0) ||
                    (whiteBishops == 0 && whiteKnights == 0 && blackBishops == 0 && blackKnights == 1) ||
                    (whiteBishops == 0 && whiteKnights == 0 && blackBishops == 0 && blackKnights == 2)) {
                return true;
            }
            // King + Bishop vs King + Bishop
            if (whiteBishops == 1 && whiteKnights == 0 && blackBishops == 1 && blackKnights == 0) {
                return true;
            }
            // King + Knight vs King + Knight
            if (whiteBishops == 0 && whiteKnights == 1 && blackBishops == 0 && blackKnights == 1) {
                return true;
            }
            // King + Bishop vs King + Knight
            if (whiteBishops == 1 && whiteKnights == 0 && blackBishops == 0 && blackKnights == 1) {
                return true;
            }
            if (whiteBishops == 0 && whiteKnights == 1 && blackBishops == 1 && blackKnights == 0) {
                return true;
            }
        }
        return false;
    }

    private void checkCastling() {
        if (castlingP != null) {
            if (castlingP.col == 0) {
                castlingP.col += 3;
            } else if (castlingP.col == 7) {
                castlingP.col -= 2;
            }
            castlingP.x = castlingP.getX(castlingP.col);
        }
    }

    private void changePlayer() {
        currentColor = (currentColor == WHITE) ? BLACK : WHITE;
        for (Piece piece : simPieces) {
            if (piece.color == currentColor) {
                piece.twoMoved = false;
            }
        }
        this.activeP = null;
        String itsurturn = (this.currentColor == 0) ? "Game: White to Move\n" : "Game: Black to Move\n";
        chatPanel.automsg(itsurturn);
    }

    private boolean canPromote() {
        if (activeP.type != Type.PAWN) return false;

        if ((currentColor == WHITE && activeP.row == 0) || (currentColor == BLACK && activeP.row == 7)) {
            promotionP.clear();
            promotionP.add(new Queen(currentColor, 9, 2));
            promotionP.add(new Rook(currentColor, 9, 3));
            promotionP.add(new Bishop(currentColor, 9, 4));
            promotionP.add(new Knight(currentColor, 9, 5));
            return true;
        }
        return false;
    }

    private void promoting() {
        if (mouse.pressed) {
            for (Piece piece : promotionP) {
                if (piece.col == mouse.x / 100 && piece.row == mouse.y / 100) {
                    switch (piece.type) {
                        case KNIGHT -> simPieces.add(new Knight(currentColor, activeP.col, activeP.row));
                        case BISHOP -> simPieces.add(new Bishop(currentColor, activeP.col, activeP.row));
                        case ROOK -> simPieces.add(new Rook(currentColor, activeP.col, activeP.row));
                        case QUEEN -> simPieces.add(new Queen(currentColor, activeP.col, activeP.row));
                    }
                    simPieces.remove(activeP.getIndex());
                    copyPieces(simPieces, pieces);
                    promotion = false;
                    changePlayer();
                    break;
                }
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        board.draw(g2);

        // Make pieces semi-transparent based on their probability
        AlphaComposite original = (AlphaComposite) g2.getComposite();
        for (Piece piece : simPieces) {
            float alpha = (float) piece.probability;
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            piece.draw(g2);

            piece.draw(g2);
        }
        g2.setComposite(original);

        if (activeP != null ) {
            if (canMove) {
                g2.setColor(Color.white);
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
                g2.fillRect(activeP.col * 100, activeP.row * 100, 100, 100);
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
            } else {
                g2.setColor(Color.gray);
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7F));
                g2.fillRect(activeP.col * 100, activeP.row * 100, 100, 100);
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0F));
            }
            activeP.draw(g2);
        }


        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setFont(new Font("Book Antiqua", Font.PLAIN, 35));
        g2.setColor(Color.white);

        if (promotion) {
            g2.drawString("Promote to:", 840, 150);
            for (Piece piece : promotionP) {
                g2.drawImage(piece.image, piece.getX(piece.col), piece.getY(piece.row), 100, 100, null);
            }
        }

        if (gameOver) {
            String s;
            if (isKingPresent(WHITE)) {
                s = "White Wins";
            } else if (isKingPresent(BLACK)) {
                s = "Black Wins";
            } else s = "Game Over";

            g2.setFont(new Font("Ariel", Font.PLAIN, 90));
            g2.setColor(Color.green);
            g2.drawString(s, 200, 420);
        } else if (stalemate) {
            g2.setFont(new Font("Ariel", Font.PLAIN, 90));
            g2.setColor(Color.gray);
            g2.drawString("Draw", 200, 420);
        }
    }

    private boolean isKingPresent(int color) {
        for (Piece piece : simPieces) {
            if (piece.type == Type.KING && piece.color == color) return true;
        }
        return false;
    }

    private void handleRegularMove() {
        moveChoicePanel.setVisible(false);
        awaitingMoveChoice = false;
        copyPieces(simPieces, pieces);
        activeP.updatePosition();
        if (castlingP != null) {
            castlingP.updatePosition();
        }
        if (isKingCaptured()) {
            gameOver = true;
        } else if (canPromote()) {
            promotion = true;
        } else if (isDrawByInsufficientMaterial()) {
            stalemate = true;
        } else {
            changePlayer();

        }
        activeP = null;
    }

    private void handleSplitMove() {
        moveChoicePanel.setVisible(false);
        awaitingMoveChoice = false;
        System.out.println("Split move chosen.");
//        SuperPosition.resolveCapture();

    }

}
