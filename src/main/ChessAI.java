package main;

import piece.*;

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

    private static int getPieceValue(Type type) {
        return switch (type) {
            case PAWN -> 100;
            case KNIGHT, BISHOP -> 300;
            case ROOK -> 500;
            case QUEEN -> 900;
            case KING -> 10000;
        };
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

    public void performAIMove() {
        System.out.println("AI is thinking...");
        List<Move> legal = getAllLegalMoves(pieces, BLACK);
        if (legal.isEmpty()) return;

        // Anmol's ai
        Move bestMove = null;
        int bestScore = Integer.MIN_VALUE;
        for (Move m : legal) {
            int s = evaluateMove(m, pieces);
            if (s > bestScore) {
                bestScore = s;
                bestMove = m;
            }
        }

        // Negamax
//        MinimaxResult result = negamax(pieces, 3, Integer.MIN_VALUE, Integer.MAX_VALUE, +1);
//        Move bestMove = result.move;
        if (bestMove == null) {
            return;
        }

        Piece mover = bestMove.piece;
        Piece captured = null;
        mover.hittingP = null;
        for (Piece p : pieces) {
            if (p != mover && p.col == bestMove.targetCol && p.row == bestMove.targetRow && p.color != mover.color) {
                captured = p;
                mover.hittingP = captured;
                break;
            }
        }
        char captureOutcome = ' ';
        if (captured != null) {
            captureOutcome = SuperPosition.resolveCapture(mover, captured);
        }

        mover.col = bestMove.targetCol;
        mover.row = bestMove.targetRow;
        mover.updatePosition();
        GamePanel.lastMoveWasHuman = false;

        gamePanel.copyPieces(pieces, simPieces);

        System.out.println(bestMove.piece + " moved to " + bestMove.targetCol + "," + bestMove.targetRow);
        System.out.println(mover.hittingP);
//        System.out.println("AI move score: " + result.score);

        String notation = gamePanel.generateNotation(mover, null, false, false, null, captureOutcome);
        gamePanel.moveTrackerPanel.logMove("Black: " + notation);

        if (GamePanel.isKingCaptured()) {
            gamePanel.gameOver = true;
        } else if (gamePanel.isDrawByInsufficientMaterial()) {
            gamePanel.stalemate = true;
        } else {
            gamePanel.changePlayer();
        }
    }

    private MinimaxResult negamax(List<Piece> board, int depth, int alpha, int beta, int color) {
        if (depth == 0 || isGameOver(board)) {
            return new MinimaxResult(color * evaluateBoard(board), null);
        }

        int maxScore = Integer.MIN_VALUE;
        Move bestMove = null;

        int sideToMove = (color == 1) ? BLACK : WHITE;
        for (Move m : getAllLegalMoves(board, sideToMove)) {
            List<Piece> next = simulateMove(board, m);
            MinimaxResult child = negamax(next, depth - 1, -beta, -alpha, -color);
            int score = -child.score;
            if (score > maxScore) {
                maxScore = score;
                bestMove = m;
            }
            alpha = Math.max(alpha, score);
            if (alpha >= beta) break;
        }

        return new MinimaxResult(maxScore, bestMove);
    }

    private record MinimaxResult(int score, Move move) {
    }

    private static List<Move> getAllLegalMoves(List<Piece> board, int color) {
        List<Move> moves = new ArrayList<>();
        for (Piece p : board) {
            if (p.color != color) continue;
            // Normal moves
            for (int c = 0; c < 8; c++)
                for (int r = 0; r < 8; r++)
                    if (p.canMove(c, r))
                        moves.add(new Move(p, c, r));

            // Castling
            if (p.type == Type.KING && !p.moved) {
                int row = p.row;
                if (canCastleKingside(p, board, color, row))
                    moves.add(new Move(p, 6, row));
                if (canCastleQueenside(p, board, color, row))
                    moves.add(new Move(p, 2, row));
            }
        }
        return moves;
    }

    private static boolean canCastleKingside(Piece king, List<Piece> b, int color, int row) {
        Piece rook = b.stream().filter(p ->
                p.type == Type.ROOK && p.color == color
                        && p.row == row && p.col == 7
                        && !p.moved).findFirst().orElse(null);
        if (rook == null) return false;
        // empty f,g
        if (b.stream().anyMatch(p -> p.row==row && (p.col==5||p.col==6))) return false;
        // no checks on e,f,g
        return !isSquareAttacked(b, 4, row, color)
                && !isSquareAttacked(b, 5, row, color)
                && !isSquareAttacked(b, 6, row, color);
    }

    private static boolean canCastleQueenside(Piece king, List<Piece> b, int color, int row) {
        Piece rook = b.stream().filter(p ->
                p.type == Type.ROOK && p.color == color
                        && p.row == row && p.col == 0
                        && !p.moved).findFirst().orElse(null);
        if (rook == null) return false;
        // empty b,c,d
        if (b.stream().anyMatch(p -> p.row==row && (p.col>=1&&p.col<=3))) return false;
        // no checks on e,d,c
        return !isSquareAttacked(b, 4, row, color)
                && !isSquareAttacked(b, 3, row, color)
                && !isSquareAttacked(b, 2, row, color);
    }

    private static boolean isSquareAttacked(List<Piece> b, int col, int row, int color) {
        int enemy = (color == WHITE) ? BLACK : WHITE;
        return b.stream().anyMatch(p -> p.color == enemy && p.canMove(col, row));
    }

    private List<Piece> cloneBoard(List<Piece> board) {
        List<Piece> c = new ArrayList<>();
        for (Piece p : board) {
            Piece q = switch(p.type) {
                case PAWN -> new Pawn(p.color, p.col, p.row);
                case KNIGHT -> new Knight(p.color, p.col, p.row);
                case BISHOP -> new Bishop(p.color, p.col, p.row);
                case ROOK -> new Rook(p.color, p.col, p.row);
                case QUEEN -> new Queen(p.color, p.col, p.row);
                case KING -> new King(p.color, p.col, p.row);
            };
            q.moved = p.moved;
            c.add(q);
        }
        return c;
    }

    private List<Piece> simulateMove(List<Piece> board, Move move) {
        List<Piece> newBoard = cloneBoard(board);
        Piece mover = findPiece(newBoard, move.piece);
        // Capture
        newBoard.removeIf(p -> p != mover && p.col == move.targetCol && p.row == move.targetRow);

        // Castling rook
        if (mover.type == Type.KING && Math.abs(move.targetCol - move.fromCol) == 2) {
            int row = mover.row;
            if (move.targetCol == 6) {
                Piece r = findPiece(newBoard, new Rook(mover.color, 7, row));
                if (r != null) {
                    r.col=5;
                    r.moved = true;
                }
            } else {
                Piece r = findPiece(newBoard, new Rook(mover.color,0,row));
                if (r != null) {
                    r.col = 3;
                    r.moved = true;
                }
            }
        }
        mover.col = move.targetCol;
        mover.row = move.targetRow;
        mover.moved = true;
        return newBoard;
    }

    private int evaluateBoard(List<Piece> board) {
        int materialScore = 0;
        int whiteCenter = 0, blackCenter = 0;
        int whiteMobility = 0, blackMobility = 0;
        boolean whiteCanCastle = false, blackCanCastle = false;
        int[][] centerSquares = {{3,3},{3,4},{4,3},{4,4}};

        for (Piece p : board) {
            int v = getPieceValue(p.type);
            if (p.color == BLACK) {
                materialScore += v;
            } else {
                materialScore -= v;
            }

            // Center control
            for (int[] sq : centerSquares) {
                if (p.col == sq[0] && p.row == sq[1]) {
                    if (p.color == BLACK) {
                        blackCenter++;
                    } else {
                        whiteCenter++;
                    }
                }
            }

            // Mobility: one pass
            for (int c = 0; c < 8; c++) {
                for (int r = 0; r < 8; r++) {
                    if (p.canMove(c, r)) {
                        if (p.color == BLACK) {
                            blackMobility++;
                        } else {
                            whiteMobility++;
                        }
                    }
                }
            }
        }

        int score = materialScore;
        score += 20 * (blackCenter - whiteCenter);
        score +=  2 * (blackMobility - whiteMobility);

        return score;
    }

    private static boolean isGameOver(List<Piece> b) {
        boolean white = false, black = false;
        for (Piece p : b) {
            if (p.type == Type.KING) {
                if (p.color == WHITE) white = true;
                else black = true;
            }
        }
        return !(white && black);
    }

    private static Piece findPiece(List<Piece> board, Piece reference) {
        for (Piece p : board) {
            if (p.type == reference.type && p.color == reference.color
                    && p.col == reference.col && p.row == reference.row)
                return p;
        }
        return null;
    }
}
