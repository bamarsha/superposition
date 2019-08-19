package superposition;

public class Complex {

    public final double real, im;

    public Complex(double real, double imaginary) {
        this.real = real;
        this.im = imaginary;
    }

    public Complex(double real) {
        this.real = real;
        this.im = 0;
    }

    public Complex add(Complex o) {
        return new Complex(real + o.real, im + o.im);
    }

    public Complex div(Complex o) {
        var m = o.magnitudeSquared();
        return new Complex((real * o.real + im * o.im) / m, (im * o.real - real * o.im) / m);
    }

    public double magnitude() {
        return Math.sqrt(magnitudeSquared());
    }

    public double magnitudeSquared() {
        return real * real + im * im;
    }

    public Complex mul(Complex o) {
        return new Complex(real * o.real - im * o.im, real * o.im + im * o.real);
    }

    public double phase() {
        return Math.atan2(im, real);
    }

    public static Complex polar(double r, double theta) {
        return new Complex(r * Math.cos(theta), r * Math.sin(theta));
    }

    public Complex sub(Complex o) {
        return new Complex(real - o.real, im - o.im);
    }
}
