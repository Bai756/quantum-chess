package main;

import piece.Piece;

public class Move {
    public final Piece piece;
    public final int fromCol, fromRow;
    public final int targetCol, targetRow;

    public Move(Piece piece, int toC, int toR) {
        this.piece = piece;
        this.fromCol = piece.col;
        this.fromRow = piece.row;
        this.targetCol = toC;
        this.targetRow = toR;
    }
}