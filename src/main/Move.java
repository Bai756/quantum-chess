package main;

import piece.Piece;

public class Move {
    public Piece piece;
    public int targetCol;
    public int targetRow;

    public Move(Piece piece, int targetCol, int targetRow) {
        this.piece = piece;
        this.targetCol = targetCol;
        this.targetRow = targetRow;
    }
}