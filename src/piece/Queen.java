package piece;

import main.Type;

public class Queen extends Piece {
    public Queen(int color, int col, int row) {
        super(color, col, row);
        this.type = Type.QUEEN;
        if (color == 0) {
            this.image = this.getImage("/resources/piece/queen");
        } else {
            this.image = this.getImage("/resources/piece/queen1");
        }

    }

    public boolean canMove(int targetCol, int targetRow) {
        if (this.isWithinBoard(targetCol, targetRow) && !this.isSameSquare(targetCol, targetRow)) {
            if (Math.abs(targetCol - this.preCol) == Math.abs(targetRow - this.preRow) && this.isValidSquare(targetCol, targetRow) && !this.isPieceOnDiagonal(targetCol, targetRow)) {
                return true;
            }

            return (targetCol == this.preCol || targetRow == this.preRow) && this.isValidSquare(targetCol, targetRow) && !this.isPieceOnStraightLine(targetCol, targetRow);
        }

        return false;
    }
}
