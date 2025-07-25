package main;

import piece.Piece;
import piece.Complex;
import java.util.ArrayList;

public class SuperPosition {
    public static char resolveCapture(Piece attacker, Piece defender) {
        double aProb = attacker.amplitude.absSquared();
        double dProb = defender.amplitude.absSquared();

        boolean aHere = Math.random() < aProb;
        boolean dHere = Math.random() < dProb;

        if (aHere && dHere) {
            collapseTo(defender);
            removePiece(defender);
            collapseTo(attacker);
            System.out.println("Both a and d");
            return 'b';

        } else if (aHere) {
            collapseTo(attacker);
            removePiece(defender);
            System.out.println("Only a");
            return 'a';

        } else if (dHere) {
            collapseTo(defender);
            removePiece(attacker);
            System.out.println("Only d");
            return 'd';

        } else {
            removePiece(attacker);
            removePiece(defender);
            System.out.println("Neither a nor d");
            return 'n';
        }
    }

    public static void collapseTo(Piece survivor) {
        survivor.amplitude = new Complex(1, 0);
        for (Piece sibling : new ArrayList<>(survivor.connectedPieces)) {
            removePiece(sibling);
        }
        survivor.connectedPieces.clear();
    }

    private static void removePiece(Piece p) {
        // Distribute amplitude to connected pieces
        if (!p.connectedPieces.isEmpty()) {
            double count = p.connectedPieces.size() + 1;
            Complex share = p.amplitude.divide(count);
            ArrayList<Piece> connected = new ArrayList<>(p.connectedPieces);
            for (Piece sib : connected) {
                if (sib != p) {
                    sib.amplitude = sib.amplitude.add(share);
                    sib.connectedPieces.remove(p);
                }
            }
            Piece sib = p.connectedPieces.getFirst();
            sib.normalizeAmplitude();
        }

        GamePanel.pieces.remove(p);
        GamePanel.simPieces.remove(p);
    }

    public static Piece handleSplit(Piece piece) {
        Piece newPiece = piece.copy();
        Complex splitAmp = piece.amplitude.multiply(1.0 / Math.sqrt(2));
        piece.amplitude = splitAmp;
        newPiece.amplitude = splitAmp;

        for (Piece connected: piece.connectedPieces) {
            if (connected != piece) {
                connected.connectedPieces.add(newPiece);
            }
        }
        piece.connectedPieces.add(newPiece);
        newPiece.connectedPieces.add(piece);

        piece.normalizeAmplitude();

        return newPiece;
    }

    public static boolean checkPromotion(Piece piece) {
        double prob = piece.amplitude.absSquared();
        boolean here = Math.random() < prob;
        if (here) {
            collapseTo(piece);
            return true;
        } else {
            removePiece(piece);
            return false;
        }
    }

    public static void amplifyPiece(Piece target) {
        // What amplification does is it takes the target piece and all its connected pieces,
        // phase shifts the target piece (makes it negative) then
        // it inverts their amplitudes around the mean amplitude of the group.

        ArrayList<Piece> connectedGroup = new ArrayList<>(target.connectedPieces);
        connectedGroup.add(target);

        target.amplitude = target.amplitude.multiply(-1.0);

        Complex meanAmp = Complex.ZERO;
        for (Piece p : connectedGroup) {
            meanAmp = meanAmp.add(p.amplitude);
        }
        meanAmp = meanAmp.divide(connectedGroup.size());

        for (Piece p : connectedGroup) {
            p.amplitude = meanAmp.multiply(2).subtract(p.amplitude);
        }
        for (Piece p : connectedGroup) {
            if (p.amplitude.absSquared() < 0.01) {
                removePiece(p);
            }
        }

        target.normalizeAmplitude();
    }
}