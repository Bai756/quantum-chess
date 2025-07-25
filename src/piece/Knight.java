package piece;

import main.Type;
import java.util.List;

public class Knight extends Piece {
    public Knight(int color, int col, int row) {
        super(color, col, row);
        this.type = Type.KNIGHT;
        if (color == 0) {
            this.image = this.getImage("/resources/piece/knight");
        } else {
            this.image = this.getImage("/resources/piece/knight1");
        }

    }

    public boolean canMove(int targetCol, int targetRow, List<Piece> board) {
        return this.isWithinBoard(targetCol, targetRow) && Math.abs(targetCol - this.preCol) * Math.abs(targetRow - this.preRow) == 2 && this.isValidSquare(targetCol, targetRow, board) && !this.isSameSquare(targetCol, targetRow);
    }
}