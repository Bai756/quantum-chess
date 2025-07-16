package main;

import piece.Piece;
import java.util.ArrayList;

public class SuperPosition {
    public static void resolveCapture(Piece attacker, Piece defender) {
        boolean aHere = Math.random() < attacker.probability;
        boolean dHere = Math.random() < defender.probability;

        if (aHere && dHere) {
            removePiece(defender);
            collapseTo(attacker);

        } else if (aHere) {
            collapseTo(attacker);
            removePiece(defender);

        } else if (dHere) {
            collapseTo(defender);
            removePiece(attacker);

        } else {
            removePiece(attacker);
            removePiece(defender);
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
}
