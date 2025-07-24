package piece;

import main.Type;
import java.util.List;

public class Rook extends Piece {
    public Rook(int color, int col, int row) {
        super(color, col, row);
        this.type = Type.ROOK;
        if (color == 0) {
            this.image = this.getImage("/resources/piece/rook");
        } else {
            this.image = this.getImage("/resources/piece/rook1");
        }

    }

    public boolean canMove(int targetCol, int targetRow, List<Piece> board) {
        return this.isWithinBoard(targetCol, targetRow) && !this.isSameSquare(targetCol, targetRow) && (targetCol == this.preCol || targetRow == this.preRow) && this.isValidSquare(targetCol, targetRow, board) && !this.isPieceOnStraightLine(targetCol, targetRow);
    }
}