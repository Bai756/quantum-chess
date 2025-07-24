package main;

import piece.*;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;


import static main.GamePanel.pieces;
import static main.GamePanel.simPieces;

public class ChessAI {
    private static final int WHITE = 0;
    private static final int BLACK = 1;
    private final GamePanel gamePanel;
    public ChessAI(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
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
            // Castling logic for king
            if (piece.type == Type.KING && !piece.moved) {
                int row = (color == WHITE) ? 7 : 0;
                // Kingside castling
                Piece kingsideRook = null;
                for (Piece p : board) {
                    if (p.type == Type.ROOK && p.color == color && p.col == 7 && p.row == row && !p.moved) {
                        kingsideRook = p;
                        break;
                    }
                }
                if (kingsideRook != null &&
                        board.stream().noneMatch(p -> p.col > 4 && p.col < 7 && p.row == row) && // squares between king and rook are empty
                        !isSquareAttacked(board, 4, row, color) &&
                        !isSquareAttacked(board, 5, row, color) &&
                        !isSquareAttacked(board, 6, row, color)) {
                    legalMoves.add(new Move(piece, 6, row));
                }
                // Queenside castling
                Piece queensideRook = null;
                for (Piece p : board) {
                    if (p.type == Type.ROOK && p.color == color && p.col == 0 && p.row == row && !p.moved) {
                        queensideRook = p;
                        break;
                    }
                }
                if (queensideRook != null &&
                        board.stream().noneMatch(p -> p.col > 0 && p.col < 4 && p.row == row) && // squares between king and rook are empty
                        !isSquareAttacked(board, 4, row, color) &&
                        !isSquareAttacked(board, 3, row, color) &&
                        !isSquareAttacked(board, 2, row, color)) {
                    legalMoves.add(new Move(piece, 2, row));
                }
            }
        }
        return legalMoves;
    }

    private static boolean isSquareAttacked(List<Piece> board, int col, int row, int color) {
        int opponent = (color == WHITE) ? BLACK : WHITE;
        for (Piece p : board) {
            if (p.color == opponent && p.canMove(col, row)) {
                return true;
            }
        }
        return false;
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
        int depth = 4;

        // Anmol's homemade chess AI
//        for (Move move : legalMoves) {
//            int score = evaluateMove(move, pieces);
//            if (score > bestScore) {
//                bestScore = score;
//                bestMove = move;
//            }
//        }

        // Negamax
        MinimaxResult result = negamax(pieces, depth, Integer.MIN_VALUE, Integer.MAX_VALUE, +1);
        bestMove = result.move;

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
                char captureResult = ' ';
                if (captured != null) {
                    captureResult = SuperPosition.resolveCapture(targetPiece, captured);
                }

                targetPiece.col = bestMove.targetCol;
                targetPiece.row = bestMove.targetRow;

                GamePanel.lastMoveWasHuman = false;

                gamePanel.copyPieces(pieces, simPieces);
                gamePanel.repaint();

                String notation = gamePanel.generateNotation(
                        targetPiece,
                        null,
                        false,
                        false,
                        null,
                        captureResult
                );
                gamePanel.moveTrackerPanel.logMove("Black: " + notation);

                targetPiece.updatePosition();

                if (GamePanel.isKingCaptured()) {
                    gamePanel.gameOver = true;
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

    private static class MinimaxResult {
        int score;
        Move move;
        MinimaxResult(int score, Move move) {
            this.score = score;
            this.move = move;
        }
    }

    private boolean isGameOver(List<Piece> board) {
        boolean whiteKing = false;
        boolean blackKing = false;

        for (Piece piece : board) {
            if (piece.type == Type.KING) {
                if (piece.color == WHITE) whiteKing = true;
                if (piece.color == BLACK) blackKing = true;
            }
        }

        return !(whiteKing && blackKing);
    }

    private int evaluateBoard(List<Piece> board) {
        int score = 0;
        int[] kingCol = new int[2];
        int[] kingRow = new int[2];
        int[] mobility = new int[2];
        boolean[] kingInCheck = new boolean[2];

        int totalMaterial = 0;
        for (Piece p : board) {
            if (p.type != Type.KING) totalMaterial += getPieceValue(p.type);
        }
        boolean isEndgame = totalMaterial < 2000;

        // Find king positions
        for (Piece p : board) {
            if (p.type == Type.KING) {
                kingCol[p.color] = p.col;
                kingRow[p.color] = p.row;
            }
        }

        for (int color = 0; color <= 1; color++) {
            int sign = (color == BLACK) ? 1 : -1;
            int kingStartCol = 4;
            int kingStartRow = (color == WHITE) ? 7 : 0;
            if (!isEndgame) {
                // If king moved from the starting square and not castled, penalize
                boolean kingMoved = !(kingCol[color] == kingStartCol && kingRow[color] == kingStartRow);
                boolean castled = (kingCol[color] == 6 || kingCol[color] == 2);
                if (kingMoved && !castled) {
                    score -= sign * 100; // adjust penalty as needed
                }
            }
        }

        // Check if king is in check
        for (int color = 0; color <= 1; color++) {
            int oppColor = 1 - color;
            for (Piece opp : board) {
                if (opp.color == oppColor && opp.canMove(kingCol[color], kingRow[color])) {
                    kingInCheck[color] = true;
                    break;
                }
            }
        }

        for (Piece p : board) {
            int pieceValue = getPieceValue(p.type);
            int colorSign = (p.color == BLACK) ? 1 : -1;
            score += colorSign * pieceValue;

            // Center control
            int centerBonus = 3 - Math.abs(3 - p.col) + 3 - Math.abs(3 - p.row);
            score += colorSign * centerBonus * 25;

            // Forward pawns and connected pawns
            if (p.type == Type.PAWN) {
                int forwardBonus = (p.color == BLACK) ? p.row : (7 - p.row);
                score += colorSign * forwardBonus * 5;

                // Connected pawns
                for (Piece other : board) {
                    if (other != p && other.type == Type.PAWN && other.color == p.color) {
                        if (Math.abs(other.col - p.col) == 1 && Math.abs(other.row - p.row) == 1) {
                            score += colorSign * 5;
                        }
                    }
                }

                // Penalize isolated pawns
                boolean isIsolated = true;
                for (Piece other : board) {
                    if (other.type == Type.PAWN && other.color == p.color && Math.abs(other.col - p.col) <= 1 && other != p) {
                        isIsolated = false;
                        break;
                    }
                }
                if (isIsolated) {
                    score -= colorSign * 10;
                }
            }

            // Mobility
            int moves = 0;
            for (int c = 0; c < 8; c++) {
                for (int r = 0; r < 8; r++) {
                    if (p.canMove(c, r)) moves++;
                }
            }
            mobility[p.color] += moves;

            // Penalize undeveloped pieces
            if ((p.type == Type.BISHOP || p.type == Type.KNIGHT) &&
                    (p.row == (p.color == WHITE ? 7 : 0))) {
                score -= colorSign * 25;
            }
        }

        // King safety: check, defenders, open file
        for (int color = 0; color <= 1; color++) {
            int sign = (color == BLACK) ? 1 : -1;
            int kingColCastledShort = 6, kingColCastledLong = 2;
            int kingRowCastle = (color == WHITE) ? 7 : 0;

            boolean shortCastle = false;
            boolean longCastle = false;
            for (Piece p : board) {
                if (p.type == Type.KING && p.color == color && p.col == kingColCastledShort && p.row == kingRowCastle) {
                    // Look for rook on h1/h8
                    for (Piece r : board) {
                        if (r.type == Type.ROOK && r.color == color && r.col == 7 && r.row == kingRowCastle) {
                            shortCastle = true;
                            break;
                        }
                    }
                }
                // Long castle (queenside)
                if (p.type == Type.KING && p.color == color && p.col == kingColCastledLong && p.row == kingRowCastle) {
                    for (Piece r : board) {
                        if (r.type == Type.ROOK && r.color == color && r.col == 0 && r.row == kingRowCastle) {
                            longCastle = true;
                            break;
                        }
                    }
                }
            }
            if (shortCastle) score += sign * 75;
            if (longCastle) score += sign * 75;

            // Penalize king in check
            if (kingInCheck[color]) {
                score -= sign * 500;
            }

            // Defenders
            int defenders = 0;
            for (Piece p : board) {
                if (p.color == color && p.type != Type.KING) {
                    if (Math.abs(p.col - kingCol[color]) <= 1 && Math.abs(p.row - kingRow[color]) <= 1) {
                        defenders++;
                    }
                }
            }
            if (defenders < 2) {
                score -= sign * 30 * (2 - defenders);
            }

            // Penalize king on open file
            boolean pawnOnFile = false;
            for (Piece p : board) {
                if (p.type == Type.PAWN && p.color == color && p.col == kingCol[color]) {
                    pawnOnFile = true;
                    break;
                }
            }
            if (!pawnOnFile) {
                score -= sign * 10;
            }
        }

        // Mobility difference
        score += (mobility[BLACK] - mobility[WHITE]) * 2;

        return score;
    }

    private Piece applyMove(List<Piece> board, Move move) {
        Piece mover = findPiece(board, move.piece);
        Piece captured = null;
        // Handle normal capture
        for (Piece p : board) {
            if (p != mover && p.col == move.targetCol && p.row == move.targetRow && p.color != mover.color) {
                captured = p;
                break;
            }
        }
        if (captured != null) {
            board.remove(captured);
        }
        // Castling: king moves two squares
        if (mover.type == Type.KING && Math.abs(move.targetCol - move.fromCol) == 2) {
            int row = mover.row;
            if (move.targetCol == 6) { // Kingside
                Piece rook = null;
                for (Piece p : board) {
                    if (p.type == Type.ROOK && p.color == mover.color && p.col == 7 && p.row == row) {
                        rook = p;
                        break;
                    }
                }
                if (rook != null) {
                    rook.col = 5;
                }
            } else if (move.targetCol == 2) { // Queenside
                Piece rook = null;
                for (Piece p : board) {
                    if (p.type == Type.ROOK && p.color == mover.color && p.col == 0 && p.row == row) {
                        rook = p;
                        break;
                    }
                }
                if (rook != null) {
                    rook.col = 3;
                }
            }
        }
        mover.col = move.targetCol;
        mover.row = move.targetRow;
        return captured;
    }

    private void undoMove(List<Piece> board, Move move, Piece captured) {
        Piece mover = findPiece(board, move.piece);
        // Undo castling rook move
        if (mover.type == Type.KING && Math.abs(move.targetCol - move.fromCol) == 2) {
            int row = mover.row;
            if (move.targetCol == 6) { // Kingside
                Piece rook = null;
                for (Piece p : board) {
                    if (p.type == Type.ROOK && p.color == mover.color && p.col == 5 && p.row == row) {
                        rook = p;
                        break;
                    }
                }
                if (rook != null) {
                    rook.col = 7;
                }
            } else if (move.targetCol == 2) { // Queenside
                Piece rook = null;
                for (Piece p : board) {
                    if (p.type == Type.ROOK && p.color == mover.color && p.col == 3 && p.row == row) {
                        rook = p;
                        break;
                    }
                }
                if (rook != null) {
                    rook.col = 0;
                }
            }
        }
        mover.col = move.fromCol;
        mover.row = move.fromRow;
        if (captured != null) {
            board.add(captured);
        }
    }

    private MinimaxResult negamax(List<Piece> board, int depth, int alpha, int beta, int color) {
        if (depth == 0 || isGameOver(board)) {
            int raw = evaluateBoard(board);
            return new MinimaxResult(color * raw, null);
        }

        List<Move> moves = getAllLegalMoves(board, color == 1 ? BLACK : WHITE);
        if (moves.isEmpty()) {
            return new MinimaxResult(color * evaluateBoard(board), null);
        }

        Move best = null;
        int max = Integer.MIN_VALUE;
        for (Move m : moves) {
            Piece captured = applyMove(board, m);
            MinimaxResult child = negamax(board, depth - 1, -beta, -alpha, -color);
            int score = -child.score;
            undoMove(board, m, captured);

            if (score > max) {
                max = score;
                best = m;
            }
            alpha = Math.max(alpha, score);
            if (alpha >= beta) break;
        }
        return new MinimaxResult(max, best);
    }
}
