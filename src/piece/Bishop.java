package piece;

import main.Type;
import java.util.List;

public class Bishop extends Piece {
    public Bishop(int color, int col, int row) {
        super(color, col, row);
        this.type = Type.BISHOP;
        if (color == 0) {
            this.image = this.getImage("/resources/piece/bishop");
        } else {
            this.image = this.getImage("/resources/piece/bishop1");
        }

    }

    public boolean canMove(int targetCol, int targetRow, List<Piece> board) {
        return this.isWithinBoard(targetCol, targetRow) && !this.isSameSquare(targetCol, targetRow) && Math.abs(targetCol - this.preCol) == Math.abs(targetRow - this.preRow) && this.isValidSquare(targetCol, targetRow, board) && !this.isPieceOnDiagonal(targetCol, targetRow);
    }
}