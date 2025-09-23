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
            case KNIGHT -> 300;
            case BISHOP -> 320;
            case ROOK -> 500;
            case QUEEN -> 900;
            case KING -> 10000;
        };
    }

//    private int evaluateMove(Move move, List<Piece> board) {
//        int score = 0;
//        Piece movingPiece = move.piece;
//        int targetCol = move.targetCol;
//        int targetRow = move.targetRow;
//
//        // Value if this move results in a capture
//        Piece target = null;
//        for (Piece p : board) {
//            if (p.col == targetCol && p.row == targetRow && p.color != movingPiece.color) {
//                target = p;
//                break;
//            }
//        }
//
//        if (target != null) {
//            boolean isDefended = isSquareDefended(targetCol, targetRow, board, target.color);
//            int targetValue = getPieceValue(target.type);
//            int attackerValue = getPieceValue(movingPiece.type);
//
//            if (targetValue > attackerValue && !isDefended) {
//                score += targetValue - attackerValue; // good trade
//            } else if (!isDefended) {
//                score += targetValue; // minor capture, safe
//            } else {
//                score -= attackerValue; // don't lose valuable piece to a defended target
//            }
//        }
//
//        // Penalize risky moves that expose the king or queen
//        if (movingPiece.type == Type.KING || movingPiece.type == Type.QUEEN) {
//            boolean movesIntoDanger = isSquareDefended(targetCol, targetRow, board, (movingPiece.color == WHITE) ? BLACK : WHITE);
//            if (movesIntoDanger) {
//                score -= getPieceValue(movingPiece.type); // heavily penalize exposing king or queen
//            }
//        }
//
//        // Positional encouragement: center control and forward movement
//        int centerBonus = 3 - Math.abs(3 - targetCol) + 3 - Math.abs(3 - targetRow);
//        score += centerBonus * 10;
//
//        int forwardBonus = (movingPiece.color == BLACK) ? targetRow : (7 - targetRow);
//        score += forwardBonus * 2;
//
//        return score;
//    }

    private boolean isSquareDefended(int col, int row, List<Piece> board, int defendingColor) {
        for (Piece piece : board) {
            if (piece.color == defendingColor && piece.canMove(col, row, board)) {
                return true;
            }
        }
        return false;
    }

    public void performAIMove() {
        System.out.println("AI is thinking...");
        GamePanel.lastMoveWasHuman = false;
        List<Move> legal = getAllLegalMoves(pieces, BLACK);
        if (legal.isEmpty()) return;

        // Anmol's ai
//        Move bestMove = null;
//        int bestScore = Integer.MIN_VALUE;
//        for (Move m : legal) {
//            int s = evaluateMove(m, pieces);
//            if (s > bestScore) {
//                bestScore = s;
//                bestMove = m;
//            }
//        }

        // Minimax
        MinimaxResult result = minimax(pieces, 3, Integer.MIN_VALUE, Integer.MAX_VALUE, true);
        Move bestMove = result.move;

        GamePanel.castlingP = null;

        if (result.score == -100000) {
            List<Move> kingMoves = new ArrayList<>();
            for (Move m : legal) {
                boolean occupied = false;
                for (Piece p : pieces) {
                    if (p.col == m.targetCol && p.row == m.targetRow && p.color != m.piece.color) {
                        occupied = true;
                        break;
                    }
                }
                if (!occupied) {
                    kingMoves.add(m);
                }
            }
            if (!kingMoves.isEmpty()) {
                Move kingMove = kingMoves.getFirst();
                Piece king = kingMove.piece;
                king.col = kingMove.targetCol;
                king.row = kingMove.targetRow;
                king.hittingP = null;
                gamePanel.handleAISplitMove(king);
                gamePanel.copyPieces(pieces, simPieces);
                System.out.println("AI split king to avoid mate");
                return;
            }
        }

        if (bestMove == null) {
            return;
        }

        Piece mover = bestMove.piece;

        mover.hittingP = null;
        for (Piece p : pieces) {
            if (p != mover && p.col == bestMove.targetCol && p.row == bestMove.targetRow && p.color != mover.color) {
                mover.hittingP = p;
                break;
            }
        }

        if (mover.type == Type.KING && Math.abs(bestMove.targetCol - bestMove.fromCol) == 2) {
            Piece castlingRook = null;
            if (bestMove.targetCol == 6) {
                castlingRook = findPiece(pieces, new Rook(mover.color, 7, mover.row));
            } else {
                castlingRook = findPiece(pieces, new Rook(mover.color, 0, mover.row));
            }
            GamePanel.castlingP = castlingRook;
        }

        // Decide move type and delegate
        if (mover.hittingP != null ||
                (mover.type == Type.PAWN && ((mover.color == BLACK && bestMove.targetRow == 7) || (mover.color == WHITE && bestMove.targetRow == 0)))) {
            mover.col = bestMove.targetCol;
            mover.row = bestMove.targetRow;
            gamePanel.handleAIMove(mover);
        } else if (Math.random() < 0.4) {
            System.out.println("AI chose split move");
            mover.col = bestMove.targetCol;
            mover.row = bestMove.targetRow;
            mover.hittingP = null;
            gamePanel.handleAISplitMove(mover);
        } else {
            mover.col = bestMove.targetCol;
            mover.row = bestMove.targetRow;
            gamePanel.handleAIMove(mover);
        }

        gamePanel.copyPieces(pieces, simPieces);

        System.out.println(bestMove.piece + " moved to " + bestMove.targetCol + "," + bestMove.targetRow);
        System.out.println(mover.hittingP);
        System.out.println("AI move score: " + result.score);
    }

    private MinimaxResult minimax(List<Piece> board, int depth, int alpha, int beta, boolean isMaximizing) {
        if (depth == 0 || isGameOver(board)) {
            int eval = evaluateBoard(board);
            return new MinimaxResult(eval, null);
        }

        int color = isMaximizing ? BLACK : WHITE;
        List<Move> moves = getAllLegalMoves(board, color);

        if (moves.isEmpty()) {
            int eval = evaluateBoard(board);
            return new MinimaxResult(eval, null);
        }

        Move bestMove = null;
        int bestScore = isMaximizing ? Integer.MIN_VALUE : Integer.MAX_VALUE;

        for (Move m : moves) {
            List<Piece> next = simulateMove(board, m, false);
            MinimaxResult child = minimax(next, depth - 1, alpha, beta, !isMaximizing);
            int score = child.score;

            if (isMaximizing) {
                if (score > bestScore) {
                    bestScore = score;
                    bestMove = m;
                }
                alpha = Math.max(alpha, score);
            } else {
                if (score < bestScore) {
                    bestScore = score;
                    bestMove = m;
                }
                beta = Math.min(beta, score);
            }
            if (beta <= alpha) break;
        }

        return new MinimaxResult(bestScore, bestMove);
    }

    private record MinimaxResult(int score, Move move) {
    }

    private static List<Move> getAllLegalMoves(List<Piece> board, int color) {
        List<Move> moves = new ArrayList<>();
        for (Piece p : board) {
            if (p.color != color) continue;
            // Normal moves
            for (int c = 0; c < 8; c++) {
                for (int r = 0; r < 8; r++) {
                    if (p.col == c && p.row == r) continue;

                    if (p.canMove(c, r, board) && p.isValidSquare(c, r, board)) {
                        moves.add(new Move(p, c, r));
                    }
                }
            }

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

        for (int c = 5; c <= 6; c++) {
            int finalC = c;
            if (b.stream().anyMatch(p -> p.row == row && p.col == finalC)) return false;
        }

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

        for (int c = 1; c <= 3; c++) {
            int finalC = c;
            if (b.stream().anyMatch(p -> p.row == row && p.col == finalC)) return false;
        }

        return !isSquareAttacked(b, 4, row, color)
                && !isSquareAttacked(b, 3, row, color)
                && !isSquareAttacked(b, 2, row, color);
    }

    private static boolean isSquareAttacked(List<Piece> board, int col, int row, int color) {
        int enemy = (color == WHITE) ? BLACK : WHITE;
        return board.stream().anyMatch(p -> p.color == enemy && p.canMove(col, row, board));
    }

    private List<Piece> cloneBoard(List<Piece> board) {
        List<Piece> c = new ArrayList<>();
        for (Piece p : board) {
            Piece q = p.clone();
            c.add(q);
        }
        return c;
    }

    private List<Piece> simulateMove(List<Piece> board, Move move, boolean isSplit) {
        List<Piece> newBoard = cloneBoard(board);
        Piece movingPiece = findPiece(newBoard, move.piece);

        if (isSplit) {
            Piece newPiece = SuperPosition.handleSplit(movingPiece);

            newPiece.col = move.targetCol;
            newPiece.row = move.targetRow;

            newPiece.updatePosition();

            newBoard.add(newPiece);
        } else {
            // Regular move
            movingPiece.col = move.targetCol;
            movingPiece.row = move.targetRow;
            movingPiece.updatePosition();
        }

        if (movingPiece.type == Type.PAWN && (movingPiece.row == 0 || movingPiece.row == 7)) {
            // Promote to queen
            Piece promoted = new Queen(movingPiece.color, movingPiece.col, movingPiece.row);
            promoted.moved = true;
            newBoard.remove(movingPiece);
            newBoard.add(promoted);
            movingPiece = promoted;
        }

        // Capture
        Piece finalMovingPiece = movingPiece;
        newBoard.removeIf(p -> p != finalMovingPiece && p.col == move.targetCol && p.row == move.targetRow);

        // Castling rook
        if (movingPiece.type == Type.KING && Math.abs(move.targetCol - move.fromCol) == 2) {
            int row = movingPiece.row;
            if (move.targetCol == 6) {
                Piece r = findPiece(newBoard, new Rook(movingPiece.color, 7, row));
                if (r != null) {
                    r.col=5;
                    r.moved = true;
                }
            } else {
                Piece r = findPiece(newBoard, new Rook(movingPiece.color,0,row));
                if (r != null) {
                    r.col = 3;
                    r.moved = true;
                }
            }
        }
        movingPiece.col = move.targetCol;
        movingPiece.row = move.targetRow;
        movingPiece.moved = true;

        return newBoard;
    }

    private int evaluateBoard(List<Piece> board) {
        boolean whiteAlive = false, blackAlive = false;
        for (Piece p : board) {
            if (p.type == Type.KING) {
                if (p.color == WHITE) whiteAlive = true;
                if (p.color == BLACK) blackAlive = true;
            }
        }
        // Game over bonus
        if (!whiteAlive) return 100000;  // Black wins
        if (!blackAlive) return -100000; // White wins

        int materialScore = 0;
        int whiteCenter = 0, blackCenter = 0;
        int whiteMobility = 0, blackMobility = 0;
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
                    if (p.canMove(c, r, board)) {
                        if (p.color == BLACK) {
                            blackMobility++;
                        } else {
                            whiteMobility++;
                        }
                    }
                }
            }

            if (p.type == Type.KING) {
                // Penalize early king moves (not castling)
                if (p.moved && p.col != 6 && p.col != 2) {
                    if (p.color == BLACK) materialScore -= 50;
                    else materialScore += 50;
                }
                // Reward castling
                if (p.moved && (p.col == 6 || p.col == 2)) {
                    if (p.color == BLACK) materialScore += 100;
                    else materialScore -= 100;
                }
            }

            if (p.type == Type.PAWN) {
                if (p.color == BLACK && p.row == 7) {
                    materialScore += 500;
                } else if (p.color == WHITE && p.row == 0) {
                    materialScore -= 500;
                }
            }

            if (p.type == Type.PAWN) {
                if (p.color == BLACK) {
                    materialScore += p.row * 10;
                } else {
                    materialScore -= (7 - p.row) * 10;
                }
            }
        }

        int score = materialScore;
        score += 30 * (blackCenter - whiteCenter);
        score += 10 * (blackMobility - whiteMobility);

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

    private void printBoard(List<Piece> board) {
        char[][] grid = new char[8][8];
        for (int r = 0; r < 8; r++)
            for (int c = 0; c < 8; c++)
                grid[r][c] = '.';

        for (Piece p : board) {
            char symbol = switch (p.type) {
                case PAWN -> 'P';
                case KNIGHT -> 'N';
                case BISHOP -> 'B';
                case ROOK -> 'R';
                case QUEEN -> 'Q';
                case KING -> 'K';
            };
            if (p.color == 1) symbol = Character.toLowerCase(symbol);
            grid[p.row][p.col] = symbol;
        }

        for (int r = 7; r >= 0; r--) {
            System.out.print((r + 1) + " ");
            for (int c = 0; c < 8; c++) {
                System.out.print(grid[r][c] + " ");
            }
            System.out.println();
        }
        System.out.println("  a b c d e f g h");
    }
}
