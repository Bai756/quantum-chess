package piece;

import main.GamePanel;
import main.Type;
import java.util.List;

public class Pawn extends Piece {
    public Pawn(int color, int col, int row) {
        super(color, col, row);
        this.type = Type.PAWN;
        if (color == 0) {
            this.image = this.getImage("/resources/piece/pawn");
        } else {
            this.image = this.getImage("/resources/piece/pawn1");
        }

    }

    public boolean canMove(int targetCol, int targetRow, List<Piece> board) {
        if (this.isWithinBoard(targetCol, targetRow) && !this.isSameSquare(targetCol, targetRow)) {
            int moveValue;
            if (this.color == 0) {
                moveValue = -1;
            } else {
                moveValue = 1;
            }

            this.hittingP = this.getHittingP(targetCol, targetRow, board);
            if (targetCol == this.preCol && targetRow == this.preRow + moveValue && this.hittingP == null) {
                return true;
            }

            if (targetCol == this.preCol && targetRow == this.preRow + moveValue * 2 && this.hittingP == null && !this.moved && !this.isPieceOnStraightLine(targetCol, targetRow)) {
                return true;
            }

            if (Math.abs(targetCol - this.preCol) == 1 && targetRow == this.preRow + moveValue && this.hittingP != null && this.hittingP.color != this.color) {
                return true;
            }

            if (Math.abs(targetCol - this.preCol) == 1 && targetRow == this.preRow + moveValue) {
                for(Piece piece : board) {
                    if (piece.col == targetCol && piece.row == this.preRow && piece.twoMoved) {
                        this.hittingP = piece;
                        return true;
                    }
                }
            }
        }

        return false;
    }
}
