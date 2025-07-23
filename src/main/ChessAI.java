package main;

import piece.*;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


import static main.GamePanel.pieces;
import static main.GamePanel.simPieces;

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
    /*private int evaluateMove(Move move, List<Piece> board) {
        int score = 0;
        Piece movingPiece = move.piece;
        int targetCol = move.targetCol;
        int targetRow = move.targetRow;

        Piece target = null;
        for (Piece p : board) {
            if (p.col == targetCol && p.row == targetRow && p.color != movingPiece.color) {
                target = p;
                break;
            }
        }

        int attackerValue = getPieceValue(movingPiece.type);

        // === 1. Evaluate capture safety ===
        if (target != null) {
            boolean isDefended = isSquareDefended(targetCol, targetRow, board, target.color);
            int targetValue = getPieceValue(target.type);

            if (!isDefended && targetValue >= attackerValue) {
                score += targetValue - attackerValue; // favorable, safe trade
            } else if (!isDefended) {
                score += targetValue; // small, safe gain
            } else {
                score -= attackerValue; // risky capture
            }
        }

        // === 2. Simulate move and see if it exposes the piece ===
        List<Piece> simulatedBoard = deepCopy(board);
        Piece simPiece = findPiece(simulatedBoard, movingPiece);
        boolean becomesVulnerable = false;
        if (simPiece != null) {
            simPiece.col = targetCol;
            simPiece.row = targetRow;
            simPiece.updatePosition();

            becomesVulnerable = isSquareDefended(targetCol, targetRow, simulatedBoard, (movingPiece.color == WHITE) ? BLACK : WHITE);
            if (becomesVulnerable) {
                score -= attackerValue; // harsh penalty for leaving piece hanging
            }
        }

        // === 3. Protect king/queen from danger ===
        if (((movingPiece.type == Type.KING) || (movingPiece.type == Type.QUEEN)) && becomesVulnerable) {
            score -= attackerValue * 2; // extra penalty
        }

        // === 4. Positioning bonuses ===
        int centerBonus = 3 - Math.abs(3 - targetCol) + 3 - Math.abs(3 - targetRow);
        score += centerBonus * 5;

        return score;
    }*/
    private static List<Piece> deepCopy(List<Piece> originalPieces) {
        List<Piece> copiedPieces = new ArrayList<>();
        for (Piece p : originalPieces) {
            Piece copy;

            switch (p.type) {
                case PAWN -> copy = new Pawn(p.color, p.col, p.row);
                case KNIGHT -> copy = new Knight(p.color, p.col, p.row);
                case BISHOP -> copy = new Bishop(p.color, p.col, p.row);
                case ROOK   -> copy = new Rook(p.color, p.col, p.row);
                case QUEEN  -> copy = new Queen(p.color, p.col, p.row);
                case KING   -> copy = new King(p.color, p.col, p.row);
                default -> {
                    continue; // Skip invalid types
                }
            }

            // Preserve state flags (like movement history)
            copy.twoMoved = p.twoMoved;
            copiedPieces.add(copy);
        }
        return copiedPieces;
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
    private int evaluateMove(Move move, List<Piece> board) {
        int score = 0;
        Piece movingPiece = move.piece;
        int targetCol = move.targetCol;
        int targetRow = move.targetRow;

        // Value if this move results in a capture
        Piece target = null;
        for (Piece p : board) {
            if (p.col == targetCol && p.row == targetRow && p.color != movingPiece.color) {
                target = p;
                break;
            }
        }

        if (target != null) {
            boolean isDefended = isSquareDefended(targetCol, targetRow, board, target.color);
            int targetValue = getPieceValue(target.type);
            int attackerValue = getPieceValue(movingPiece.type);

            if (targetValue > attackerValue && !isDefended) {
                score += targetValue - attackerValue; // good trade
            } else if (!isDefended) {
                score += targetValue; // minor capture, safe
            } else {
                score -= attackerValue; // don't lose valuable piece to a defended target
            }
        }

        // Penalize risky moves that expose the king or queen
        if (movingPiece.type == Type.KING || movingPiece.type == Type.QUEEN) {
            boolean movesIntoDanger = isSquareDefended(targetCol, targetRow, board, (movingPiece.color == WHITE) ? BLACK : WHITE);
            if (movesIntoDanger) {
                score -= getPieceValue(movingPiece.type); // heavily penalize exposing king or queen
            }
        }

        // Positional encouragement: center control and forward movement
        int centerBonus = 3 - Math.abs(3 - targetCol) + 3 - Math.abs(3 - targetRow);
        score += centerBonus * 10;

        int forwardBonus = (movingPiece.color == BLACK) ? targetRow : (7 - targetRow);
        score += forwardBonus * 2;

        return score;
    }

    private boolean isSquareDefended(int col, int row, List<Piece> board, int defendingColor) {
        for (Piece piece : board) {
            if (piece.color == defendingColor && piece.canMove(col, row)) {
                return true;
            }
        }
        return false;
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

    // ChessAI.java
    public void performAIMove() {
        System.out.println("AI is evaluating moves...");

        List<Move> legalMoves = getAllLegalMoves(pieces, BLACK);
        if (legalMoves.isEmpty()) {
            System.out.println("AI has no legal moves.");
            return;
        }

        Move bestMove = null;
        int bestScore = Integer.MIN_VALUE;

        for (Move move : legalMoves) {
            int score = evaluateMove(move, pieces);
            if (score > bestScore) {
                bestScore = score;
                bestMove = move;
            }
        }

        if (bestMove != null) {
            Piece targetPiece = findPiece(pieces, bestMove.piece);
            if (targetPiece != null) {
                Piece captured = null;
                for (Piece p : pieces) {
                    if (p != targetPiece && p.col == bestMove.targetCol && p.row == bestMove.targetRow && p.color != targetPiece.color) {
                        captured = p;
                        break;
                    }
                }
                if (captured != null) {
                    pieces.remove(captured);
                }

                targetPiece.col = bestMove.targetCol;
                targetPiece.row = bestMove.targetRow;
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
        }
    }



    public static void showGameModeDialog() {
        String[] options = { "Play Against Human", "Play Against AI" };
        int choice = JOptionPane.showOptionDialog(
                null,
                "Choose Game Mode",
                "Chess Setup",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]
        );
        GameMode gameMode = (choice == 1) ? GameMode.HUMAN_VS_AI : GameMode.HUMAN_VS_HUMAN;
    }
}
