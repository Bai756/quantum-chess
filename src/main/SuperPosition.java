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

        GamePanel.pieces.add(newPiece);
        GamePanel.simPieces.add(newPiece);

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
        ArrayList<Piece> connectedGroup = new ArrayList<>(target.connectedPieces);
        connectedGroup.add(target);

        double targetProb = target.amplitude.absSquared();
        double totalProb = 0;
        for (Piece p : connectedGroup) {
            totalProb += p.amplitude.absSquared();
        }

        if (connectedGroup.size() == 1 || targetProb >= 0.9) {
            return;
        }

        double boostFactor = 1.3; // Boost by 30%
        Complex newAmp = target.amplitude.multiply(Math.sqrt(boostFactor));

        double remainingProb = totalProb - (targetProb * boostFactor);
        if (remainingProb < 0) remainingProb = 0;

        // Distribute remaining probability to other pieces
        for (Piece p : connectedGroup) {
            if (p != target) {
                double ratio = p.amplitude.absSquared() / (totalProb - targetProb);
                p.amplitude = p.amplitude.multiply(Math.sqrt(ratio * remainingProb / (totalProb - targetProb)));
            }
        }

        target.amplitude = newAmp;

        target.normalizeAmplitude();
    }
}