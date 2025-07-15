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
import piece.King;
//import piece.Knight;
//import piece.Pawn;
import piece.Piece;
//import piece.Queen;
import piece.Rook;

public class GamePanel extends JPanel implements Runnable {
    public static final int WIDTH = 1100;
    public static final int HEIGHT = 800;
    final int FPS = 60;
    Thread gameThread;
    Board board = new Board();
    Mouse mouse = new Mouse();
    public static ArrayList<Piece> pieces = new ArrayList();
    public static ArrayList<Piece> simPieces = new ArrayList();
    ArrayList<Piece> promotionP = new ArrayList();
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
        this.setPreferredSize(new Dimension(1100, 800));
        this.setBackground(Color.black);
        this.addMouseMotionListener(this.mouse);
        this.addMouseListener(this.mouse);
        this.setPieces();
        this.copyPieces(pieces, simPieces);
    }

    public void launchGame() {
        this.gameThread = new Thread(this);
        this.gameThread.start();
    }

    public void setPieces() {
//        pieces.add(new Pawn(0, 0, 6));
//        pieces.add(new Pawn(0, 1, 6));
//        pieces.add(new Pawn(0, 2, 6));
//        pieces.add(new Pawn(0, 3, 6));
//        pieces.add(new Pawn(0, 4, 6));
//        pieces.add(new Pawn(0, 5, 6));
//        pieces.add(new Pawn(0, 6, 6));
//        pieces.add(new Pawn(0, 7, 6));
        pieces.add(new Rook(0, 0, 7));
//        pieces.add(new Knight(0, 1, 7));
//        pieces.add(new Bishop(0, 2, 7));
//        pieces.add(new Queen(0, 3, 7));
        pieces.add(new King(0, 4, 7));
//        pieces.add(new Knight(0, 6, 7));
//        pieces.add(new Bishop(0, 5, 7));
        pieces.add(new Rook(0, 7, 7));
//        pieces.add(new Pawn(1, 0, 1));
//        pieces.add(new Pawn(1, 1, 1));
//        pieces.add(new Pawn(1, 2, 1));
//        pieces.add(new Pawn(1, 3, 1));
//        pieces.add(new Pawn(1, 4, 1));
//        pieces.add(new Pawn(1, 5, 1));
//        pieces.add(new Pawn(1, 6, 1));
//        pieces.add(new Pawn(1, 7, 1));
        pieces.add(new Rook(1, 0, 0));
//        pieces.add(new Knight(1, 1, 0));
//        pieces.add(new Bishop(1, 2, 0));
//        pieces.add(new Queen(1, 3, 0));
        pieces.add(new King(1, 4, 0));
//        pieces.add(new Knight(1, 6, 0));
//        pieces.add(new Bishop(1, 5, 0));
        pieces.add(new Rook(1, 7, 0));
    }

    private void copyPieces(ArrayList<Piece> source, ArrayList<Piece> target) {
        target.clear();

        for(int i = 0; i < source.size(); ++i) {
            target.add((Piece)source.get(i));
        }

    }

    public void run() {
        double drawInterval = (double)1.6666666E7F;
        double delta = (double)0.0F;
        long lastTime = System.nanoTime();

        while(this.gameThread != null) {
            long currentTime = System.nanoTime();
            delta += (double)(currentTime - lastTime) / drawInterval;
            lastTime = currentTime;
            if (delta >= (double)1.0F) {
                this.update();
                this.repaint();
                --delta;
            }
        }

    }

    private void update() {
        if (this.promotion) {
            this.promoting();
        } else if (!this.gameover && !this.stalemate) {
            if (this.mouse.pressed) {
                if (this.activeP == null) {
                    for(Piece piece : simPieces) {
                        if (piece.color == this.currentColor && piece.col == this.mouse.x / 100 && piece.row == this.mouse.y / 100) {
                            this.activeP = piece;
                        }
                    }
                } else {
                    this.simulate();
                }
            }

            if (!this.mouse.pressed && this.activeP != null) {
                if (this.validSquare) {
                    this.copyPieces(simPieces, pieces);
                    this.activeP.updatePosition();
                    if (castlingP != null) {
                        castlingP.updatePosition();
                    }

                    if (this.kingInCheck() && this.isCheckmate()) {
                        this.gameover = true;
                    } else if (this.isStalemate() && !this.kingInCheck()) {
                        this.stalemate = true;
                    } else if (this.canPromote()) {
                        this.promotion = true;
                    } else {
                        this.changePlayer();
                    }
                } else {
                    this.copyPieces(pieces, simPieces);
                    this.activeP.resetPosition();
                    this.activeP = null;
                }
            }
        }

    }

    private void simulate() {
        this.canMove = false;
        this.validSquare = false;
        this.copyPieces(pieces, simPieces);
        if (castlingP != null) {
            castlingP.col = castlingP.preCol;
            castlingP.x = castlingP.getX(castlingP.col);
            castlingP = null;
        }

        this.activeP.x = this.mouse.x - 50;
        this.activeP.y = this.mouse.y - 50;
        this.activeP.col = this.activeP.getCol(this.activeP.x);
        this.activeP.row = this.activeP.getRow(this.activeP.y);
        if (this.activeP.canMove(this.activeP.col, this.activeP.row)) {
            this.canMove = true;
            if (this.activeP.hittingP != null) {
                simPieces.remove(this.activeP.hittingP.getIndex());
            }

            this.checkCastling();
            if (!this.isIllegal(this.activeP) && !this.canOpponentCaptureKing()) {
                this.validSquare = true;
            }
        }

    }

    private boolean isIllegal(Piece king) {
        if (king.type == Type.KING) {
            for(Piece piece : simPieces) {
                if (piece != king && piece.color != king.color && piece.canMove(king.col, king.row)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean canOpponentCaptureKing() {
        Piece king = this.getKing(false);

        for(Piece piece : simPieces) {
            if (piece.color != king.color && piece.canMove(king.col, king.row)) {
                return true;
            }
        }

        return false;
    }

    private boolean kingInCheck() {
        Piece king = this.getKing(true);

        for(Piece piece : simPieces) {
            if (piece.color == this.currentColor && piece.canMove(king.col, king.row)) {
                this.checkingP = this.activeP;
                return true;
            }

            this.checkingP = null;
        }

        return false;
    }

    private Piece getKing(boolean opponent) {
        Piece king = null;

        for(Piece piece : simPieces) {
            if (opponent) {
                if (piece.type == Type.KING && piece.color != this.currentColor) {
                    king = piece;
                }
            } else if (piece.type == Type.KING && piece.color == this.currentColor) {
                king = piece;
            }
        }

        return king;
    }

    private boolean isCheckmate() {
        Piece king = this.getKing(true);
        if (this.kingCanMove(king)) {
            return false;
        } else {
            int colDiff = Math.abs(king.col - this.checkingP.col);
            int rowDiff = Math.abs(king.row - this.checkingP.row);
            if (colDiff == 0) {
                if (this.checkingP.row < king.row) {
                    for(int row = this.checkingP.row; row < king.row; ++row) {
                        for(Piece piece : simPieces) {
                            if (piece != king && piece.color != this.currentColor && piece.canMove(this.checkingP.col, row)) {
                                return false;
                            }
                        }
                    }
                }

                if (this.checkingP.row > king.row) {
                    for(int row = this.checkingP.row; row > king.row; --row) {
                        for(Piece piece : simPieces) {
                            if (piece != king && piece.color != this.currentColor && piece.canMove(this.checkingP.col, row)) {
                                return false;
                            }
                        }
                    }
                }
            } else if (rowDiff == 0) {
                if (this.checkingP.col < king.col) {
                    for(int col = this.checkingP.col; col < king.col; ++col) {
                        for(Piece piece : simPieces) {
                            if (piece != king && piece.color != this.currentColor && piece.canMove(col, this.checkingP.row)) {
                                return false;
                            }
                        }
                    }
                }

                if (this.checkingP.col > king.col) {
                    for(int col = this.checkingP.col; col > king.col; --col) {
                        for(Piece piece : simPieces) {
                            if (piece != king && piece.color != this.currentColor && piece.canMove(col, this.checkingP.row)) {
                                return false;
                            }
                        }
                    }
                }
            } else if (colDiff == rowDiff) {
                if (this.checkingP.row < king.row) {
                    if (this.checkingP.col < king.col) {
                        int col = this.checkingP.col;

                        for(int row = this.checkingP.row; col < king.col; ++row) {
                            for(Piece piece : simPieces) {
                                if (piece != king && piece.color != this.currentColor && piece.canMove(col, row)) {
                                    return false;
                                }
                            }

                            ++col;
                        }
                    }

                    if (this.checkingP.col > king.col) {
                        int col = this.checkingP.col;

                        for(int row = this.checkingP.row; col > king.col; ++row) {
                            for(Piece piece : simPieces) {
                                if (piece != king && piece.color != this.currentColor && piece.canMove(col, row)) {
                                    return false;
                                }
                            }

                            --col;
                        }
                    }
                }

                if (this.checkingP.row > king.row) {
                    if (this.checkingP.col < king.col) {
                        int col = this.checkingP.col;

                        for(int row = this.checkingP.row; col < king.col; --row) {
                            for(Piece piece : simPieces) {
                                if (piece != king && piece.color != this.currentColor && piece.canMove(col, row)) {
                                    return false;
                                }
                            }

                            ++col;
                        }
                    }

                    if (this.checkingP.col > king.col) {
                        int col = this.checkingP.col;

                        for(int row = this.checkingP.row; col > king.col; --row) {
                            for(Piece piece : simPieces) {
                                if (piece != king && piece.color != this.currentColor && piece.canMove(col, row)) {
                                    return false;
                                }
                            }

                            --col;
                        }
                    }
                }
            }

            return true;
        }
    }

    private boolean kingCanMove(Piece king) {
        if (this.isValidMove(king, -1, -1)) {
            return true;
        } else if (this.isValidMove(king, -1, 0)) {
            return true;
        } else if (this.isValidMove(king, -1, 1)) {
            return true;
        } else if (this.isValidMove(king, 0, -1)) {
            return true;
        } else if (this.isValidMove(king, 0, 1)) {
            return true;
        } else if (this.isValidMove(king, 1, -1)) {
            return true;
        } else if (this.isValidMove(king, 1, 0)) {
            return true;
        } else {
            return this.isValidMove(king, 1, 1);
        }
    }

    private boolean isValidMove(Piece king, int colPlus, int rowPlus) {
        boolean isValidMove = false;
        king.col += colPlus;
        king.row += rowPlus;
        if (king.canMove(king.col, king.row)) {
            if (king.hittingP != null) {
                simPieces.remove(king.hittingP.getIndex());
            }

            if (!this.isIllegal(king)) {
                isValidMove = true;
            }
        }

        king.resetPosition();
        this.copyPieces(pieces, simPieces);
        return isValidMove;
    }

    private boolean isStalemate() {
        Piece king = null;
        Piece preCheckingP = null;

        for(Piece piece : simPieces) {
            if (piece.type == Type.KING && piece.color != this.currentColor) {
                king = piece;
            }
        }

        if (this.kingCanMove(king)) {
            return false;
        } else {
            for(Piece piece : simPieces) {
                if (piece.color != this.currentColor && piece.type != Type.KING) {
                    for(int col = 0; col < 8; ++col) {
                        for(int row = 0; row < 8; ++row) {
                            if (piece.canMove(col, row)) {
                                preCheckingP = this.checkingP;
                                int oldCol = piece.col;
                                int oldRow = piece.row;
                                piece.col = col;
                                piece.row = row;
                                if (!this.kingInCheck()) {
                                    this.checkingP = preCheckingP;
                                    piece.col = oldCol;
                                    piece.row = oldRow;
                                    return false;
                                }

                                this.checkingP = preCheckingP;
                                piece.col = oldCol;
                                piece.row = oldRow;
                            }
                        }
                    }
                }
            }

            return true;
        }
    }

    private void checkCastling() {
        if (castlingP != null) {
            if (castlingP.col == 0) {
                Piece var10000 = castlingP;
                var10000.col += 3;
            } else if (castlingP.col == 7) {
                Piece var1 = castlingP;
                var1.col -= 2;
            }

            castlingP.x = castlingP.getX(castlingP.col);
        }

    }

    private void changePlayer() {
        if (this.currentColor == 0) {
            this.currentColor = 1;

            for(Piece piece : simPieces) {
                if (piece.color == 1) {
                    piece.twoMoved = false;
                }
            }
        } else {
            this.currentColor = 0;

            for(Piece piece : simPieces) {
                if (piece.color == 0) {
                    piece.twoMoved = false;
                }
            }
        }

        this.activeP = null;
    }

    private boolean canPromote() {
        if (this.activeP.type != Type.PAWN || (this.currentColor != 0 || this.activeP.row != 0) && (this.currentColor != 1 || this.activeP.row != 7)) {
            return false;
        } else {
            this.promotionP.clear();
//            this.promotionP.add(new Queen(this.currentColor, 9, 2));
            this.promotionP.add(new Rook(this.currentColor, 9, 3));
//            this.promotionP.add(new Bishop(this.currentColor, 9, 4));
//            this.promotionP.add(new Knight(this.currentColor, 9, 5));
            return true;
        }
    }

    private void promoting() {
        if (this.mouse.pressed) {
            for(Piece piece : this.promotionP) {
                if (piece.col == this.mouse.x / 100 && piece.row == this.mouse.y / 100) {
                    switch (piece.type) {
//                        case KNIGHT -> simPieces.add(new Knight(this.currentColor, this.activeP.col, this.activeP.row));
//                        case BISHOP -> simPieces.add(new Bishop(this.currentColor, this.activeP.col, this.activeP.row));
                        case ROOK -> simPieces.add(new Rook(this.currentColor, this.activeP.col, this.activeP.row));
//                        case QUEEN -> simPieces.add(new Queen(this.currentColor, this.activeP.col, this.activeP.row));
                    }

                    simPieces.remove(this.activeP.getIndex());
                    this.copyPieces(simPieces, pieces);
                    this.activeP = null;
                    this.promotion = false;
                    this.changePlayer();
                }
            }
        }

    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D)g;
        this.board.draw(g2);

        for(Piece p : simPieces) {
            p.draw(g2);
        }

        if (this.activeP != null) {
            if (this.canMove) {
                if (!this.isIllegal(this.activeP) && !this.canOpponentCaptureKing()) {
                    g2.setColor(Color.white);
                    g2.setComposite(AlphaComposite.getInstance(3, 0.7F));
                    g2.fillRect(this.activeP.col * 100, this.activeP.row * 100, 100, 100);
                    g2.setComposite(AlphaComposite.getInstance(3, 1.0F));
                } else {
                    g2.setColor(Color.gray);
                    g2.setComposite(AlphaComposite.getInstance(3, 0.7F));
                    g2.fillRect(this.activeP.col * 100, this.activeP.row * 100, 100, 100);
                    g2.setComposite(AlphaComposite.getInstance(3, 1.0F));
                }
            }

            this.activeP.draw(g2);
        }

        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setFont(new Font("Book Antiqua", 0, 35));
        g2.setColor(Color.white);
        if (this.promotion) {
            g2.drawString("Promote to:", 840, 150);

            for(Piece piece : this.promotionP) {
                g2.drawImage(piece.image, piece.getX(piece.col), piece.getY(piece.row), 100, 100, (ImageObserver)null);
            }
        } else if (this.currentColor == 0) {
            g2.drawString("White's turn", 850, 550);
            if (this.checkingP != null && this.checkingP.color == 1) {
                g2.setColor(Color.red);
                g2.drawString("The King", 875, 650);
                g2.drawString("is in check", 860, 700);
            }
        } else {
            g2.drawString("Black's turn", 850, 250);
            if (this.checkingP != null && this.checkingP.color == 0) {
                g2.setColor(Color.red);
                g2.drawString("The King", 875, 100);
                g2.drawString("is in check", 860, 150);
            }
        }

        if (this.gameover) {
            String s = "";
            if (this.currentColor == 0) {
                s = "White Wins";
            } else {
                s = "Black Wins";
            }

            g2.setFont(new Font("Ariel", 0, 90));
            g2.setColor(Color.green);
            g2.drawString(s, 200, 420);
        }

        if (this.stalemate) {
            g2.setFont(new Font("Ariel", 0, 90));
            g2.setColor(Color.gray);
            g2.drawString("Stalemate", 200, 420);
        }

    }
}
