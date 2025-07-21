package piece;

public class QuantumGates {
    public void hadamard(Piece piece) {
        piece.amplitude = piece.amplitude.multiply(1.0 / Math.sqrt(2));
        piece.normalizeAmplitude();
    }

    public void x(Piece piece) {
        piece.amplitude = piece.amplitude.multiply(-1);
        piece.normalizeAmplitude();
    }

    public void y(Piece piece) {
        piece.amplitude = new Complex(-piece.amplitude.im(), piece.amplitude.re());
        piece.normalizeAmplitude();
    }
}