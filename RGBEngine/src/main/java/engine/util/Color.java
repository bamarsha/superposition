package engine.util;

import static engine.util.math.MathUtils.lerp;

public class Color {

    public static final Color AMBER = new Color(1, .75, 0);
    public static final Color BLACK = new Color(0, 0, 0);
    public static final Color BLUE = new Color(0, 0, 1);
    public static final Color CLEAR = new Color(0, 0, 0, 0);
    public static final Color CYAN = new Color(0, 1, 1);
    public static final Color GREEN = new Color(0, 1, 0);
    public static final Color GRAY = new Color(.5, .5, .5);
    public static final Color LIME = new Color(.75, 1, 0);
    public static final Color MAGENTA = new Color(1, 0, 1);
    public static final Color ORANGE = new Color(1, .5, 0);
    public static final Color PURPLE = new Color(.75, 0, 1);
    public static final Color RED = new Color(1, 0, 0);
    public static final Color VERMILION = new Color(1, .25, 0);
    public static final Color VIOLET = new Color(.375, 0, 1);
    public static final Color WHITE = new Color(1, 1, 1);
    public static final Color YELLOW = new Color(1, 1, 0);

    public final double r, g, b, a;

    /**
     * Constructs a color from rgb, with no transparency.
     *
     * @param r Red.
     * @param g Green.
     * @param b Blue.
     */
    public Color(double r, double g, double b) {
        this(r, g, b, 1);
    }

    /**
     * Constructs a color from rgba.
     *
     * @param r Red.
     * @param g Green.
     * @param b Blue.
     * @param a Alpha.
     */
    public Color(double r, double g, double b, double a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }

    /**
     * Replaces the old Alpha value with the new one.
     *
     * @param a The new value.
     * @return A new color which the value changed.
     */
    public Color setA(double a) {
        return new Color(r, g, b, a);
    }

    /**
     * Replaces the old Red value with the new one.
     *
     * @param r The new value.
     * @return A new color which the value changed.
     */
    public Color setR(double r) {
        return new Color(r, g, b, a);
    }

    /**
     * Replaces the old Green value with the new one.
     *
     * @param g The new value.
     * @return A new color which the value changed.
     */
    public Color setG(double g) {
        return new Color(r, g, b, a);
    }

    /**
     * Replaces the old Blue value with the new one.
     *
     * @param b The new value.
     * @return A new color which the value changed.
     */
    public Color setB(double b) {
        return new Color(r, g, b, a);
    }

    /**
     * Creates a gray based off of the value given.
     *
     * @param value The based value.
     * @return The gray color based on the value.
     */
    public static Color gray(double value) {
        return new Color(value, value, value);
    }

    /**
     * Returns the grayscale version of the color.
     *
     * @return The grayscale version of the color.
     */
    public Color grayscale() {
        double average = (r + g + b) / 3;
        return new Color(average, average, average, a);
    }

    /**
     * Linearly interpolates between two colors.
     *
     * @param c The second color to mix with.
     * @param amt The amount of the second color.
     * @return The interpolated color.
     */
    public Color mix(Color c, double amt) {
        return new Color(lerp(r, c.r, amt), lerp(g, c.g, amt), lerp(b, c.b, amt), lerp(a, c.a, amt));
    }

    /**
     * Mixes color with alpha mixing.
     *
     * @param top The color on top.
     * @return The new color.
     */
    public Color alphaMix(Color top) {
        if (top.a >= 1) {
            return top;
        }
        if (top.a <= 0) {
            return this;
        }
        double newAlpha = (1.0 - top.a) * a + top.a;
        return new Color(((1 - top.a) * a * r + top.a * top.r) / newAlpha,
                ((1 - top.a) * a * g + top.a * top.g) / newAlpha,
                ((1 - top.a) * a * b + top.a * top.b) / newAlpha, newAlpha);
    }

    /**
     * Multiplies the color's rgb by a scalar.
     *
     * @param scalar The scalar to multiply by.
     * @return The new color.
     */
    public Color multRGB(double scalar) {
        return new Color(r * scalar, g * scalar, b * scalar, a);
    }

    @Override
    public String toString() {
        return "Color{" + "r=" + r + ", g=" + g + ", b=" + b + ", a=" + a + '}';
    }
}
