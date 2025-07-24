package main;
import piece.*;
import javax.swing.*; import java.util.ArrayList; import java.util.Iterator; import java.util.List;
import static main.GamePanel.pieces; import static main.GamePanel.simPieces;
public class ChessAI {
    private static final int WHITE = 0;
    private static final int BLACK = 1;
    private boolean gameOver = false;
    private GamePanel gamePanel;

    public ChessAI(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
    }

    public static void setGameOver(boolean gameOver) {
        gameOver = gameOver;
    }


    private static List<Move> getAllLegalMoves(List<Piece> board, int color) {
        List<Move> legalMoves = new ArrayList<>();
        for (Piece piece : board) {
            if (piece.color != color) continue;
            for (int col = 0; col < 8; col++) {
                for (int row = 0; row < 8; row++) {
                    if (piece.canMove(col, row)) {
                        legalMoves.add(new Move(piece, col, row));
                    }
                }
            }
        }
        return legalMoves;
    }


    public static Piece findPiece(List<Piece> board, Piece reference) {
        for (Piece p : board) {
            if (p.type == reference.type &&
                    p.color == reference.color &&
                    p.col == reference.col &&
                    p.row == reference.row) {
                return p;
            }
        }
        return null;
    }
    private int getPieceValue(Type type) {
        return switch (type) {
            case PAWN -> 100;
            case KNIGHT, BISHOP -> 300;
            case ROOK -> 500;
            case QUEEN -> 900;
            case KING -> 10000;
        };
    }
    private int evaluateBoard(List<Piece> board, int color) {
        int score = 0;
        for (Piece p : board) {
            int value = switch (p.type) {
                case PAWN -> 100;
                case KNIGHT, BISHOP -> 300;
                case ROOK -> 500;
                case QUEEN -> 900;
                case KING -> 10000;
            };
            score += (p.color == color) ? value : -value;
        }
        return score;
    }
    private List<Piece> simulateMove(List<Piece> board, Move move) {
        List<Piece> cloned = cloneBoard(board);
        Piece mover = findPiece(cloned, move.piece);

        if (mover == null) return cloned;

        // Remove captured piece, if any
        cloned.removeIf(p -> p != mover &&
                p.col == move.targetCol && p.row == move.targetRow &&
                p.color != mover.color);

        mover.col = move.targetCol;
        mover.row = move.targetRow;
        return cloned;
    }
    private List<Piece> cloneBoard(List<Piece> board) {
        List<Piece> clone = new ArrayList<>();
        for (Piece p : board) {
            Piece copy = switch (p.type) {
                case PAWN -> new Pawn(p.color, p.col, p.row);
                case KNIGHT -> new Knight(p.color, p.col, p.row);
                case BISHOP -> new Bishop(p.color, p.col, p.row);
                case ROOK -> new Rook(p.color, p.col, p.row);
                case QUEEN -> new Queen(p.color, p.col, p.row);
                case KING -> new King(p.color, p.col, p.row);
            };
            copy.moved = p.moved;
            clone.add(copy);
        }
        return clone;
    }
    // ChessAI.java
    public void performAIMove() {
        List<Move> legalMoves = getAllLegalMoves(pieces, BLACK);
        if (legalMoves.isEmpty()) {
            System.out.println("AI has no legal moves.");
            return;
        }

        Move bestMove = null;
        int bestScore = Integer.MIN_VALUE;

        for (Move move : legalMoves) {
            System.out.println("Evaluating move: " + move.piece.type + " to (" + move.targetCol + ", " + move.targetRow + ")");
            List<Piece> simulated = simulateMove(pieces, move);

            //if (isKingInCheck(simulated, BLACK)) continue;
            if (isRiskyTrade(move, simulated)) continue;

            int score = evaluateBoard(simulated, BLACK);

            Piece mover = move.piece;

            // Minor piece development
            if (mover.type == Type.KNIGHT || mover.type == Type.BISHOP) score += 100;

            // Center control
            if (isCentral(move.targetCol, move.targetRow)) score += 50;

            // Pawn spam penalty
            if (mover.type == Type.PAWN) score -= 30;

            // Capture bonus
            Piece victim = getVictim(move, simulated);
            if (victim != null) {
                int gain = getPieceValue(victim.type) - getPieceValue(mover.type);
                if (gain > 0) score += gain * 100;
            }

            if (score > bestScore || bestMove == null) {
                bestScore = score;
                bestMove = move;
            }
        }

        if (bestMove == null) {
            System.out.println("No safe move found. Playing fallback.");
            bestMove = legalMoves.get(0);
        }

        applyMove(bestMove);
    }
    private boolean isCentral(int col, int row) {
        return col >= 2 && col <= 5 && row >= 2 && row <= 5; // 4x4 grid at center
    }

    private boolean hasMovedBefore(Piece piece) {
        return piece.hasMoved; // assume you track this
    }
    private Piece getVictim(Move move, List<Piece> board) {
        for (Piece p : board) {
            if (p.col == move.targetCol && p.row == move.targetRow && p.color != move.piece.color) {
                return p;
            }
        }
        return null;
    }
    private void applyMove(Move move) {
        Piece targetPiece = findPiece(pieces, move.piece);
        if (targetPiece == null) return;

        // Remove captured piece
        pieces.removeIf(p -> p != targetPiece &&
                p.col == move.targetCol && p.row == move.targetRow &&
                p.color != targetPiece.color);

        targetPiece.col = move.targetCol;
        targetPiece.row = move.targetRow;
        targetPiece.updatePosition();
        GamePanel.lastMoveWasHuman = false;

        gamePanel.copyPieces(pieces, simPieces);
        gamePanel.repaint();

        String notation = gamePanel.generateMoveNotation(targetPiece);
        gamePanel.moveTrackerPanel.logMove("Black: " + notation);
        gamePanel.chatPanel.automsg("Black (AI): " + notation);

        if (GamePanel.isKingCaptured()) {
            gameOver = true;
        } else if (gamePanel.isDrawByInsufficientMaterial()) {
            gamePanel.stalemate = true;
        } else {
            gamePanel.changePlayer();
        }
    }
    private boolean isSquareDefended(int col, int row, List<Piece> board, int defendingColor) {
        for (Piece piece : board) {
            if (piece.color == defendingColor && piece.canMove(col, row)) {
                return true;
            }
        }

        return false;
    }
    private boolean isKingInCheck(List<Piece> board, int color) {
        int enemyColor = (color == WHITE) ? BLACK : WHITE;
        Piece king = null;

        for (Piece p : board) {
            if (p.type == Type.KING && p.color == color) {
                king = p;
                break;
            }
        }

        if (king == null) return true; // king missing = disaster

        for (Piece enemy : board) {
            if (enemy.color != enemyColor) continue;

            // Use actual attack logic, not just mobility
            if (enemy.canAttack(king.col, king.row, board)) {
                return true;
            }
        }

        return false;
    }
    private boolean isRiskyTrade(Move move, List<Piece> board) {
        Piece attacker = move.piece;
        int targetCol = move.targetCol;
        int targetRow = move.targetRow;

        Piece victim = null;
        for (Piece p : board) {
            if (p.col == targetCol && p.row == targetRow && p.color != attacker.color) {
                victim = p;
                break;
            }
        }

        if (victim == null) return false;

        int attackerValue = getPieceValue(attacker.type);
        int victimValue = getPieceValue(victim.type);

        boolean targetDefended = isSquareDefended(targetCol, targetRow, board, victim.color);
        boolean attackerDefended = isSquareDefended(attacker.col, attacker.row, board, attacker.color);
        System.out.println("Target defended: " + isSquareDefended(targetCol, targetRow, board, victim.color));
        System.out.println("Attacker defended: " + isSquareDefended(attacker.col, attacker.row, board, attacker.color));

        boolean isFavorableTrade = attackerValue < victimValue;
        boolean isProtectedAttack = attackerDefended && !targetDefended;

        return !(isFavorableTrade || isProtectedAttack);
    }
}