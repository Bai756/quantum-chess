package piece;

import main.GamePanel;
import main.Type;
import java.util.List;

public class King extends Piece {
    public King(int color, int col, int row) {
        super(color, col, row);
        this.type = Type.KING;
        if (color == 0) {
            this.image = this.getImage("/resources/piece/king");
        } else {
            this.image = this.getImage("/resources/piece/king1");
        }

    }

    public boolean canMove(int targetCol, int targetRow, List<Piece> board) {
        if (this.isWithinBoard(targetCol, targetRow) && (Math.abs(targetCol - this.preCol) + Math.abs(targetRow - this.preRow) == 1 || Math.abs(targetCol - this.preCol) * Math.abs(targetRow - this.preRow) == 1) && this.isValidSquare(targetCol, targetRow, board)) {
            return true;
        } else {
            if (!this.moved) {
                if (targetCol == this.preCol + 2 && targetRow == this.preRow && !this.isPieceOnStraightLine(targetCol, targetRow)) {
                    for(Piece piece : board) {
                        if (piece.col == this.preCol + 3 && piece.row == this.preRow && !piece.moved) {
                            GamePanel.castlingP = piece;
                            return true;
                        }
                    }
                }

                if (targetCol == this.preCol - 2 && targetRow == this.preRow && !this.isPieceOnStraightLine(targetCol, targetRow)) {
                    Piece[] p = new Piece[2];

                    for(Piece piece : board) {
                        if (piece.col == this.preCol - 3 && piece.row == this.preRow) {
                            p[0] = piece;
                        }

                        if (piece.col == this.preCol - 4 && piece.row == this.preRow) {
                            p[1] = piece;
                        }

                        if (p[0] == null && p[1] != null && !p[1].moved) {
                            GamePanel.castlingP = p[1];
                            return true;
                        }
                    }
                }
            }

            return false;
        }
    }
}
