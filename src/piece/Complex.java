package piece;

public record Complex(double re, double im) {
    public static final Complex ZERO = new Complex(0, 0);

    public double absSquared() {
        return re * re + im * im;
    }

    public Complex add(Complex other) {
        return new Complex(re + other.re, im + other.im);
    }

    public Complex multiply(double scalar) {
        return new Complex(re * scalar, im * scalar);
    }

    public Complex divide(double scalar) {
        return new Complex(re / scalar, im / scalar);
    }
}