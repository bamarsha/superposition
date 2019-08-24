package engine.util;

import java.util.Random;

public abstract class MathUtils {

    public static int ceil(double x) {
        return (int) Math.ceil(x);
    }

    public static double clamp(double x, double lower, double upper) {
        return Math.max(lower, Math.min(x, upper));
    }

    public static int clamp(int x, int lower, int upper) {
        return Math.max(lower, Math.min(x, upper));
    }

    public static double cubicSpline(double t, double p1, double v1, double p2, double v2) {
        double a = v1 - (p2 - p1);
        double b = -v2 + (p2 - p1);
        return lerp(p1, p2, t) + lerp(a, b, t) * t * (1 - t);
    }

    public static int floor(double x) {
        return (int) Math.floor(x);
    }

    public static double lerp(double x, double y, double amt) {
        return x * (1 - amt) + y * amt;
    }

    public static double max(double... a) {
        double r = a[0];
        for (int i = 1; i < a.length; i++) {
            r = Math.max(r, a[i]);
        }
        return r;
    }

    public static int max(int... a) {
        int r = a[0];
        for (int i = 1; i < a.length; i++) {
            r = Math.max(r, a[i]);
        }
        return r;
    }

    public static double min(double... a) {
        double r = a[0];
        for (int i = 1; i < a.length; i++) {
            r = Math.min(r, a[i]);
        }
        return r;
    }

    public static int min(int... a) {
        int r = a[0];
        for (int i = 1; i < a.length; i++) {
            r = Math.min(r, a[i]);
        }
        return r;
    }

    public static double mod(double x, double m) {
        return (x % m + m) % m;
    }

    public static int mod(int x, int m) {
        return (x % m + m) % m;
    }

    public static int poissonSample(Random random, double expected) {
        double t = 0;
        int k = 0;
        while (true) {
            t += -Math.log(random.nextDouble()) / expected;
            if (t > 1) {
                return k;
            }
            k++;
        }
    }

    public static int round(double x) {
        return (int) Math.round(x);
    }

    public static double round(double x, double m) {
        return Math.round(x / m) * m;
    }

    public static double[] round(double[] x, double m) {
        double[] r = new double[x.length];
        for (int i = 0; i < x.length; i++) {
            r[i] = round(x[i], m);
        }
        return r;
    }
}
