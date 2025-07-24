package main;

import java.awt.AlphaComposite;
import static java.awt.AlphaComposite.getInstance;
import java.awt.*;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import javax.swing.*;

import piece.*;

public class GamePanel extends JPanel implements Runnable, KeyListener {
    public static final int WIDTH = 1100;
    public static final int HEIGHT = 800;
    final int FPS = 60;
    public ChatMain chatPanel = new ChatMain();
    Thread gameThread;
    Board board = new Board();
    Mouse mouse = new Mouse();
    public static ArrayList<Piece> pieces = new ArrayList<>();
    public static ArrayList<Piece> simPieces = new ArrayList<>();
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
    boolean isAITurnPending;
    boolean hasAmplified = false;
    String amplifiedLocation = null;
    
    private final JPanel moveChoicePanel;
    private final JPanel promotionPanel;
    final MoveTrackerPanel moveTrackerPanel = new MoveTrackerPanel();
    private final MouseAdapter hoverEffect;
    private final RoundedButton amplifyButton;
    private boolean awaitingMoveChoice = false;
    private boolean justSplit = false;
    private char captureOutcome = ' ';
    private boolean debugAmp = false;
    private boolean debugProb = false;
    private boolean tabHeld = false;
    private Timer tabHoldTimer;
    public GameMode gameMode = GameMode.HUMAN_VS_AI;
    private ChessAI chessAI;
    public static boolean lastMoveWasHuman = true;


    public GamePanel() {
        if (gameMode == GameMode.HUMAN_VS_AI) {
            chessAI = new ChessAI(GamePanel.this);
        }
        this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        this.setBackground(Color.black);
        this.addMouseMotionListener(this.mouse);
        this.addMouseListener(this.mouse);
        this.setPieces();
        this.copyPieces(pieces, simPieces);
        this.setLayout(null);
        chatPanel.setBounds(800, 400, 300, 400);
        this.add(chatPanel);
        moveTrackerPanel.setBounds(800, 0, 350, 410); // Top-right corner
        this.add(moveTrackerPanel);
        moveTrackerPanel.setBackground(new Color(0,0,0)); // matching theme
        moveTrackerPanel.setVisible(true);

        moveChoicePanel = new JPanel();
        moveChoicePanel.setLayout(null);
        moveChoicePanel.setBounds(820, 600, 150, 120); // Position it as needed
        moveChoicePanel.setVisible(false);
        moveChoicePanel.setOpaque(false);
        moveChoicePanel.setBackground(new Color(0, 0, 0, 0)); // Fully transparent

        promotionPanel = new JPanel();
        promotionPanel.setLayout(null);
        promotionPanel.setVisible(false);
        promotionPanel.setBounds(400, 300, 120, 250); // Position it near the center
        promotionPanel.setOpaque(false);
        promotionPanel.setBackground(new Color(0, 0, 0, 0));

        RoundedButton queenButton = new RoundedButton("Queen");
        queenButton.setBounds(0, 0, 120, 40);
        buttonFormat(queenButton);
        queenButton.addActionListener(_ -> handlePromotion(Type.QUEEN));

        RoundedButton rookButton = new RoundedButton("Rook");
        rookButton.setBounds(0, 50, 120, 40);
        buttonFormat(rookButton);
        rookButton.addActionListener(_ -> handlePromotion(Type.ROOK));

        RoundedButton bishopButton = new RoundedButton("Bishop");
        bishopButton.setBounds(0, 100, 120, 40);
        buttonFormat(bishopButton);
        bishopButton.addActionListener(_ -> handlePromotion(Type.BISHOP));

        RoundedButton knightButton = new RoundedButton("Knight");
        knightButton.setBounds(0, 150, 120, 40);
        buttonFormat(knightButton);
        knightButton.addActionListener(_ -> handlePromotion(Type.KNIGHT));

        promotionPanel.add(queenButton);
        promotionPanel.add(rookButton);
        promotionPanel.add(bishopButton);
        promotionPanel.add(knightButton);
        moveChoicePanel.setOpaque(false);
        moveChoicePanel.setBackground(new Color(0, 0, 0, 0));
        this.add(promotionPanel);

        RoundedButton regularButton = new RoundedButton("Regular");
        regularButton.setBounds(0, 0, 120, 40);
        buttonFormat(regularButton);
        regularButton.addActionListener(_ -> handleMove());

        RoundedButton splitButton = new RoundedButton("Split");
        splitButton.setBounds(130, 0, 120, 40);
        buttonFormat(splitButton);
        splitButton.addActionListener(_ -> handleSplitMove());

        RoundedButton cancelMoveButton = new RoundedButton("Cancel");
        cancelMoveButton.setBounds(130, 50, 120, 40);
        buttonFormat(cancelMoveButton);
        cancelMoveButton.addActionListener(_ -> {
            if (activeP != null) {
                activeP.resetPosition();
                activeP = null;
            }
            if (castlingP != null) {
                castlingP.resetPosition();
                castlingP = null;
            }
            moveChoicePanel.setVisible(false);
            awaitingMoveChoice = false;
        });

        moveChoicePanel.add(regularButton);
        moveChoicePanel.add(splitButton);
        moveChoicePanel.add(cancelMoveButton);
        this.add(moveChoicePanel);

        amplifyButton = new RoundedButton("Amplify");
        amplifyButton.setBounds(0, 50, 90, 40);
        buttonFormat(amplifyButton);
        amplifyButton.addActionListener(_ -> {
            if (activeP != null) {
                SuperPosition.amplifyPiece(activeP);
                amplifyButton.setVisible(false);
                amplifiedLocation = String.format("%c%d", 'a' + activeP.col, 8 - activeP.row);
                awaitingMoveChoice = false;
                hasAmplified = true;
                chatPanel.displaySystemMessage("Amplified at " + amplifiedLocation);
            }
        });
        this.add(amplifyButton);
        amplifyButton.setVisible(false);

        this.hoverEffect = new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                JButton source = (JButton) e.getSource();
                source.setBackground(new Color(0,0,0)); // Slightly lighter tone
                source.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2)); // Pop with a soft outline
            }

            @Override
            public void mouseExited(MouseEvent e) {
                JButton source = (JButton) e.getSource();
                source.setBackground(new Color(0,0,0)); // Original color
                source.setBorder(null); // Return to flat look
            }
        };

        setFocusable(true);
        setFocusTraversalKeysEnabled(false);
        this.setFocusable(true);
        this.requestFocusInWindow();
        addKeyListener(this);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_TAB && !tabHeld) {
            tabHeld = true;
            tabHoldTimer = new Timer(100, _ -> {
//                debugAmp = true;
                debugProb = true;
            });
            tabHoldTimer.start();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_TAB) {
            tabHeld = false;
            if (tabHoldTimer != null) {
                tabHoldTimer.stop();
                tabHoldTimer = null;
            }
            debugAmp = false;
            debugProb = false;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    private void buttonFormat(RoundedButton button){
        button.setFont(new Font("SansSerif",Font.PLAIN,20));
        button.setBackground(new Color(0,0,0));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.addMouseListener(this.hoverEffect);
    }

    public void launchGame() {
        this.gameThread = new Thread(this);
        this.gameThread.start();
    }

    public void setPieces() {
//        pieces.add(new Pawn(WHITE, 0, 2));
//        pieces.add(new Pawn(WHITE, 1, 2));
//        pieces.add(new Pawn(WHITE, 2, 2));
//        pieces.add(new Pawn(WHITE, 3, 2));
//
//        pieces.get(0).amplitude = Complex.ONE;
//        pieces.get(1).amplitude = Complex.ONE;
//        pieces.get(2).amplitude = Complex.ONE;
//        pieces.get(3).amplitude = Complex.ONE;
//
//        pieces.get(0).connectedPieces.add(pieces.get(1));
//        pieces.get(0).connectedPieces.add(pieces.get(2));
//        pieces.get(0).connectedPieces.add(pieces.get(3));
//
//        pieces.get(1).connectedPieces.add(pieces.get(0));
//        pieces.get(1).connectedPieces.add(pieces.get(2));
//        pieces.get(1).connectedPieces.add(pieces.get(3));
//
//        pieces.get(2).connectedPieces.add(pieces.get(0));
//        pieces.get(2).connectedPieces.add(pieces.get(1));
//        pieces.get(2).connectedPieces.add(pieces.get(3));
//
//        pieces.get(3).connectedPieces.add(pieces.get(0));
//        pieces.get(3).connectedPieces.add(pieces.get(1));
//        pieces.get(3).connectedPieces.add(pieces.get(2));
//        pieces.get(0).normalizeAmplitude();
//
//        // Initialize black pawns in row 2
//        pieces.add(new Pawn(BLACK, 4, 6));
//        pieces.add(new Pawn(BLACK, 5, 6));
//        pieces.add(new Pawn(BLACK, 6, 6));
//        pieces.add(new Pawn(BLACK, 7, 6));
//
//        pieces.get(4).amplitude = Complex.ONE;
//        pieces.get(5).amplitude = Complex.ONE;
//        pieces.get(6).amplitude = Complex.ONE;
//        pieces.get(7).amplitude = Complex.ONE;
//
//        pieces.get(4).connectedPieces.add(pieces.get(5));
//        pieces.get(4).connectedPieces.add(pieces.get(6));
//        pieces.get(4).connectedPieces.add(pieces.get(7));
//
//        pieces.get(5).connectedPieces.add(pieces.get(4));
//        pieces.get(5).connectedPieces.add(pieces.get(6));
//        pieces.get(5).connectedPieces.add(pieces.get(7));
//
//        pieces.get(6).connectedPieces.add(pieces.get(4));
//        pieces.get(6).connectedPieces.add(pieces.get(5));
//        pieces.get(6).connectedPieces.add(pieces.get(7));
//
//        pieces.get(7).connectedPieces.add(pieces.get(4));
//        pieces.get(7).connectedPieces.add(pieces.get(5));
//        pieces.get(7).connectedPieces.add(pieces.get(6));
//        pieces.get(4).normalizeAmplitude();

        // Initialize white pawns
        for (int col = 0; col < 8; col++) {
            pieces.add(new Pawn(WHITE, col, 6));
        }

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

    public void copyPieces(ArrayList<Piece> source, ArrayList<Piece> target) {
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
//        System.out.println("Update running, color: " + currentColor + ", aiTurnPending: " + isAITurnPending);
        if (promotion) {
            promotionPanel.setVisible(true);
        } else if (!gameOver) {
            if (mouse.pressed) {
                if (activeP == null) {
                    boolean found = false;
                    for (Piece piece : simPieces) {
                        if (piece.color == currentColor && piece.col == mouse.x / 100 && piece.row == mouse.y / 100) {
                            activeP = piece;
                            boolean eligible = piece.connectedPieces.size() >= 2 && !hasAmplified;
                            amplifyButton.setVisible(eligible);
                            if (eligible) {
                                amplifyButton.setBounds(piece.x - 10, piece.y + 30, 120, 40);
                                moveChoicePanel.setVisible(false);
                            }
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        amplifyButton.setVisible(false);
                    }
                } else if (!awaitingMoveChoice) {
                    simulate();
                } else {
                    amplifyButton.setVisible(false);
                }
            }

            if (!mouse.pressed && activeP != null) {
                if (validSquare && !awaitingMoveChoice) {
                    if (activeP.hittingP != null ||
                            (activeP.type == Type.PAWN && ((currentColor == WHITE && activeP.row == 0) || (currentColor == BLACK && activeP.row == 7)))) { // Capture or promotion

                        handleMove();
                        return;
                    }

                    awaitingMoveChoice = true;
                    amplifyButton.setVisible(false);

                    int x = activeP.x;
                    int y = activeP.y;

                    if (x <= 550 && x >= 10 && y >= 50 && y <= 710) { // Normal move
                        moveChoicePanel.setBounds(x, y, 250, 90);
                    } else if (x > 550) { // If it's on the right
                        int dx = x - 540;
                        moveChoicePanel.setBounds(x - dx, y, 250, 90);
                    } else if (x < 10) { // If it's on the left
                        int dx = 10 - x;
                        moveChoicePanel.setBounds(x + dx, y, 250, 90);
                    } else if (y < 60) { // If it's too high
                        int dy = 60 - y;
                        moveChoicePanel.setBounds(x, y + dy, 250, 90);
                    } else { // If it's too low
                        int dy = y - 700;
                        moveChoicePanel.setBounds(x, y - dy, 250, 90);
                    }
                    moveChoicePanel.setVisible(true);

                } else if (!awaitingMoveChoice) {
                    Point mousePoint = new Point(mouse.x, mouse.y);
                    Rectangle ampBounds = amplifyButton.getBounds();
                    boolean overAmplify = ampBounds.contains(mousePoint);

                    Rectangle pieceSquare = new Rectangle(activeP.x, activeP.y, 100, 100);
                    boolean overPieceSquare = pieceSquare.contains(mousePoint);

                    if (activeP != null) activeP.resetPosition();

                    if (!overAmplify && !overPieceSquare) {
                        copyPieces(pieces, simPieces);
                        activeP = null;
                        amplifyButton.setVisible(false);
                    }
                }
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

    public static boolean isKingCaptured() {
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

    public boolean isDrawByInsufficientMaterial() {
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

    public void changePlayer() {
        if (isKingCaptured()) {
            gameOver = true;
        } else if (isDrawByInsufficientMaterial()) {
            stalemate = true;
        }

        currentColor = (currentColor == WHITE) ? BLACK : WHITE;
        for (Piece piece : simPieces) {
            if (piece.color == currentColor) {
                piece.twoMoved = false;
            }
        }

        activeP = null;
        moveChoicePanel.setVisible(false);
        amplifyButton.setVisible(false);
        awaitingMoveChoice = false;
        hasAmplified = false;
        justSplit = false;

        String itsurturn = (this.currentColor == 0) ? "Game: White to Move\n" : "Game: Black to Move\n";
        chatPanel.setCurrentColor(currentColor);
        chatPanel.automsg(itsurturn);

        if (gameMode == GameMode.HUMAN_VS_AI && isAITurnPending && lastMoveWasHuman) {
            chessAI.performAIMove();
            isAITurnPending = false;
        }
        else {
            lastMoveWasHuman = true;
        }
        System.out.println("Current color: " + currentColor + ", AI turn pending: " + isAITurnPending);
    }

    private boolean canPromote() {
        if (activeP.type != Type.PAWN) return false;

        if ((currentColor == WHITE && activeP.row == 0) || (currentColor == BLACK && activeP.row == 7)) {
            return SuperPosition.checkPromotion(activeP);
        }
        return false;
    }

    private void handlePromotion(Type pieceType) {
        String notation = generateNotation(
                activeP,
                pieceType,
                false,
                hasAmplified,
                amplifiedLocation,
                captureOutcome
        );

        if (!justSplit) {
            moveTrackerPanel.logMove((currentColor == WHITE ? "White: " : "Black: ") + notation);
        }

        if (pieceType == Type.QUEEN) {
            simPieces.add(new Queen(currentColor, activeP.col, activeP.row));
            chatPanel.displaySystemMessage((currentColor == WHITE ? "White" : "Black") + " promoted to Queen.");
        } else if (pieceType == Type.ROOK) {
            simPieces.add(new Rook(currentColor, activeP.col, activeP.row));
            chatPanel.displaySystemMessage((currentColor == WHITE ? "White" : "Black") + " promoted to Rook.");
        } else if (pieceType == Type.BISHOP) {
            simPieces.add(new Bishop(currentColor, activeP.col, activeP.row));
            chatPanel.displaySystemMessage((currentColor == WHITE ? "White" : "Black") + " promoted to Bishop.");
        } else if (pieceType == Type.KNIGHT) {
            simPieces.add(new Knight(currentColor, activeP.col, activeP.row));
            chatPanel.displaySystemMessage((currentColor == WHITE ? "White" : "Black") + " promoted to Knight.");
        }

        simPieces.remove(activeP.getIndex());
        copyPieces(simPieces, pieces);
        promotionPanel.setVisible(false);
        promotion = false;
        captureOutcome = ' ';

        changePlayer();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        board.draw(g2);

        // Make pieces semi-transparent based on their probability
        AlphaComposite original = (AlphaComposite) g2.getComposite();
        ArrayList<Piece> piecesCopy = new ArrayList<>(simPieces);
        for (Piece piece : piecesCopy) {
            if (piece.amplitude.absSquared() != 0.0) {
                int x = piece.x;
                int y = piece.y;

                float alpha = (float) piece.amplitude.absSquared();
                g2.setComposite(getInstance(AlphaComposite.SRC_OVER, alpha));
                g2.drawImage(piece.image, x + 5, y + 5, 90, 90, null);

                g2.setFont(new Font("TimesNewRoman", Font.PLAIN, 14));
                g2.setColor(Color.YELLOW);
                g2.setComposite(original);
                if (debugAmp) {
                    String ampText = String.format("%.2f + %.2fi", piece.amplitude.re(), piece.amplitude.im());
                    g2.drawString(ampText, x + 10, y + 30);
                }
                if (debugProb) {
                    String probText = String.format("%.2f", piece.amplitude.absSquared());
                    g2.drawString(probText, x + 35, y + 90);
                }
            }
        }
        g2.setComposite(original);

        if (activeP != null ) {
            if (canMove) {
                g2.setColor(Color.white);
                g2.setComposite(getInstance(AlphaComposite.SRC_OVER, 0.7f));
                g2.fillRect(activeP.col * 100, activeP.row * 100, 100, 100);
            }
            g2.setComposite(getInstance(AlphaComposite.SRC_OVER, 1f));
            activeP.draw(g2);
        }

        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setFont(new Font("Book Antiqua", Font.PLAIN, 35));
        g2.setColor(Color.white);

        if (gameOver) {
            String s;
            if (isKingPresent(WHITE)) {
                s = "White cooked";
            } else if (isKingPresent(BLACK)) {
                s = "Black cooked";
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

    private void handleSplitMove() {
        justSplit = true;
        Piece newPiece = SuperPosition.handleSplit(activeP);
        newPiece.row = activeP.preRow;
        newPiece.col = activeP.preCol;
        newPiece.updatePosition();

        String moveNotation = generateNotation(
                activeP,
                null,
                true,
                hasAmplified,
                amplifiedLocation,
                captureOutcome
        );

        moveTrackerPanel.logMove((currentColor == WHITE ? "White: " : "Black: ") + moveNotation);

        if (castlingP != null) {
            Piece newRook = SuperPosition.handleSplit(castlingP);
            newRook.row = castlingP.preRow;
            newRook.col = castlingP.preCol;
            if (castlingP.col == 0) {
                newRook.col += 3;
            } else if (castlingP.col == 7) {
                newRook.col -= 2;
            }
            newRook.updatePosition();
        }

        awaitingMoveChoice = false;
        handleMove();
        justSplit = false;
    }

    private void handleMove() {
        char captureResult = ' ';

        if (activeP.hittingP != null) {
            captureResult = SuperPosition.resolveCapture(activeP, activeP.hittingP);
        } else {
            if (canPromote()) {
                promotion = true;
                return;
            }
        }

        if (captureResult == 'b' || captureResult == 'a') {
            copyPieces(simPieces, pieces);
            if (activeP.type == Type.PAWN &&
                    ((currentColor == WHITE && activeP.row == 0) || (currentColor == BLACK && activeP.row == 7))) {
                promotion = true;

                if (gameMode == GameMode.HUMAN_VS_AI && currentColor == WHITE) {
                    isAITurnPending = true;
                }

                captureOutcome = captureResult;
                return;
            }
        } else {
            copyPieces(pieces, simPieces);
        }

        if (!justSplit) {
            String moveNotation = generateNotation(
                    activeP,
                    null,
                    false,
                    hasAmplified,
                    amplifiedLocation,
                    captureResult
            );
            moveTrackerPanel.logMove((currentColor == WHITE ? "White: " : "Black: ") + moveNotation);
        }

        if (castlingP != null) {
            castlingP.updatePosition();
        }
        activeP.updatePosition();

        if (gameMode == GameMode.HUMAN_VS_AI && currentColor == WHITE) {
            isAITurnPending = true;
        }

        changePlayer();
    }

    public String generateNotation(Piece piece, Type promotedType, boolean isSplit, boolean hasAmplified, String amplifiedLocation, char captureOutcome) {
        StringBuilder notation = new StringBuilder();

        // Amplification prefix
        if (hasAmplified && amplifiedLocation != null) {
            notation.append(amplifiedLocation).append("@");
        }

        // Split move
        if (isSplit) {
            notation.append("|");
        }

        // Castling
        if (piece.type == Type.KING && Math.abs(piece.col - piece.preCol) == 2) {
            if (piece.col == 6) notation.append("O-O");
            else if (piece.col == 2) notation.append("O-O-O");
        } else if (promotedType != null) {
            // Promotion
            if (piece.hittingP != null) {
                char originFile = (char) ('a' + piece.preCol);
                notation.append(originFile).append("x");
            }
            char file = (char) ('a' + piece.col);
            int rank = 8 - piece.row;
            notation.append(file).append(rank);
            notation.append("=").append(switch (promotedType) {
                case QUEEN -> "Q";
                case ROOK -> "R";
                case BISHOP -> "B";
                case KNIGHT -> "N";
                default -> "?";
            });
            if (piece.hittingP != null && captureOutcome != ' ') {
                notation.append(captureOutcome);
            }
            int opponentColor = (piece.color == WHITE) ? BLACK : WHITE;
            if (!isKingPresent(opponentColor)) {
                notation.append("#");
            }
        } else {
            // Regular move
            String pieceChar = switch (piece.type) {
                case Type.KNIGHT -> "N";
                case Type.BISHOP -> "B";
                case Type.ROOK -> "R";
                case Type.QUEEN -> "Q";
                case Type.KING -> "K";
                default -> "";
            };
            char file = (char) ('a' + piece.col);
            int rank = 8 - piece.row;
            boolean isCapture = piece.hittingP != null;
            if (piece.type == Type.PAWN && isCapture) {
                char originFile = (char) ('a' + piece.preCol);
                notation.append(originFile).append("x");
            } else {
                notation.append(pieceChar);
                if (isCapture) notation.append("x");
            }
            notation.append(file).append(rank);
            if (piece.type == Type.PAWN && (piece.row == 0 || piece.row == 7) && piece.hittingP == null) {
                notation.append("n");
            }
            if (isCapture && captureOutcome != ' ') {
                notation.append(captureOutcome);
            }
            int opponentColor = (piece.color == WHITE) ? BLACK : WHITE;
            if (!isKingPresent(opponentColor)) {
                notation.append("#");
            }
        }

        // Split move suffix
        if (isSplit) {
            notation.append("|");
        }

        return notation.toString();
    }
}
