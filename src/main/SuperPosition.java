package main;

import piece.Piece;
import java.util.ArrayList;

public class SuperPosition {
    public static boolean resolveCapture(Piece attacker, Piece defender) {
        boolean aHere = Math.random() < attacker.probability;
        boolean dHere = Math.random() < defender.probability;

        if (aHere && dHere) {
            collapseTo(defender);
            removePiece(defender);
            collapseTo(attacker);
            System.out.println("Both A and D");
            return true;

        } else if (aHere) {
            collapseTo(attacker);
            removePiece(defender);
            System.out.println("Only A");
            return true;

        } else if (dHere) {
            collapseTo(defender);
            removePiece(attacker);
            System.out.println("Only D");
            return false;

        } else {
            System.out.println("Neither A nor D");
            removePiece(attacker);
            removePiece(defender);
            return false;
        }
    }

    private static void collapseTo(Piece survivor) {
        for (Piece sibling : new ArrayList<>(survivor.connectedPieces)) {
            removePiece(sibling);
        }

        survivor.connectedPieces.clear();
        survivor.probability = 1.0;
    }

    private static void removePiece(Piece p) {
        // Distribute probability to connected pieces
        if (!p.connectedPieces.isEmpty()) {
            int count = p.connectedPieces.size();
            double share = p.probability / count;
            for (Piece sib : p.connectedPieces) {
                if (sib != p) {
                    sib.probability += share;
                    sib.connectedPieces.remove(p);
                }
            }
        }

        GamePanel.pieces.remove(p);
        GamePanel.simPieces.remove(p);
    }

    public static Piece handleSplit(Piece piece) {
        Piece newPiece = piece.copy();
        newPiece.probability = piece.probability / 2.0;
        piece.probability /= 2.0;

        piece.connectedPieces.add(newPiece);
        newPiece.connectedPieces.add(piece);
        GamePanel.pieces.add(newPiece);
        GamePanel.simPieces.add(newPiece);

        return newPiece;
    }
}
