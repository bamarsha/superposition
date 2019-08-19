package engine.util.math;

import org.joml.Matrix3d;
import org.joml.Quaterniond;
import static engine.util.math.MathUtils.mod;

public class Quaternion {

    public static Quaternion IDENTITY = new Quaternion(1, 0, 0, 0);

    public final double a, b, c, d;

    private Quaternion(double a, double b, double c, double d) {
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
    }

    public double angle() {
        if (a < 0) {
            return negate().angle();
        }
        return 2 * Math.atan2(Math.sqrt(b * b + c * c + d * d), a);
    }

    public Vec3d applyTo(Vec3d pos) {
        Quaternion result = mul(new Quaternion(0, pos.x, pos.y, pos.z)).mul(inverse());
        return new Vec3d(result.b, result.c, result.d);
    }

    public Vec3d applyToForwards() {
        Quaternion result = mul(new Quaternion(0, 1, 0, 0)).mul(inverse());
        return new Vec3d(result.b, result.c, result.d);
    }

    public Vec3d axis() {
        if (a < 0) {
            return negate().axis();
        }
        if (new Vec3d(b, c, d).lengthSquared() < 1e-12) {
            return new Vec3d(1, 0, 0);
        }
        return new Vec3d(b, c, d).normalize();
    }

    public Quaternion div(Quaternion other) {
        return other.inverse().mul(this);
    }

    public static Quaternion fromAngleAxis(Vec3d axis) {
        if (axis.length() < 1e-12) {
            return IDENTITY;
        }
        return fromAngleAxis(axis.length(), axis);
    }

    public static Quaternion fromAngleAxis(double angle, Vec3d axis) {
        axis = axis.normalize();
        double sin = Math.sin(angle / 2), cos = Math.cos(angle / 2);
        return new Quaternion(cos, axis.x * sin, axis.y * sin, axis.z * sin);
    }

    public static Quaternion fromEulerAngles(double yaw, double pitch, double roll) {
        yaw = mod(yaw, Math.PI * 2);
        pitch = mod(pitch, Math.PI * 2);
        roll = mod(roll, Math.PI * 2);
        double sinYaw = Math.sin(yaw / 2), cosYaw = Math.cos(yaw / 2);
        double sinPitch = Math.sin(pitch / 2), cosPitch = Math.cos(pitch / 2);
        double sinRoll = Math.sin(roll / 2), cosRoll = Math.cos(roll / 2);
        return new Quaternion(
                sinYaw * sinPitch * sinRoll + cosYaw * cosPitch * cosRoll,
                -sinYaw * sinPitch * cosRoll + cosYaw * cosPitch * sinRoll,
                sinYaw * cosPitch * sinRoll + cosYaw * sinPitch * cosRoll,
                sinYaw * cosPitch * cosRoll - cosYaw * sinPitch * sinRoll);
    }

    public static Quaternion fromXYAxes(Vec3d x, Vec3d y) {
        Vec3d z = x.cross(y);
        y = z.cross(x);
        Matrix3d m = new Matrix3d(x.toJOML(), y.toJOML(), z.toJOML());
        Quaterniond q = m.getUnnormalizedRotation(new Quaterniond());
        return new Quaternion(q.w, q.x, q.y, q.z);
    }

    public double getPitch() {
        return Math.asin(2 * (a * c - b * d));
    }

    public double getRoll() {
        return Math.atan2(2 * (a * b + c * d), 1 - 2 * (b * b + c * c));
    }

    public double getYaw() {
        return Math.atan2(2 * (a * d + b * c), 1 - 2 * (c * c + d * d));
    }

    public Quaternion inverse() {
        return new Quaternion(a, -b, -c, -d);
    }

    public Quaternion lerp(Quaternion other, double amt) {
        if (a * other.a + b * other.b + c * other.c + d * other.d < 0) {
            other = other.negate();
        }
        return new Quaternion(a * (1 - amt) + other.a * amt,
                b * (1 - amt) + other.b * amt,
                c * (1 - amt) + other.c * amt,
                d * (1 - amt) + other.d * amt).normalize();
    }

    public Quaternion mul(Quaternion other) {
        return new Quaternion(
                a * other.a - b * other.b - c * other.c - d * other.d,
                a * other.b + b * other.a + c * other.d - d * other.c,
                a * other.c - b * other.d + c * other.a + d * other.b,
                a * other.d + b * other.c - c * other.b + d * other.a);
    }

    private Quaternion negate() {
        return new Quaternion(-a, -b, -c, -d);
    }

    public Quaternion normalize() {
        double length = Math.sqrt(a * a + b * b + c * c + d * d);
        return new Quaternion(a / length, b / length, c / length, d / length);
    }

    public Quaternion pow(double t) {
        return fromAngleAxis(t * angle(), axis());
    }

    public Vec3d toAngleAxis() {
        return axis().mul(angle());
    }

    public Quaterniond toJOML() {
        return new Quaterniond(b, c, d, a);
    }

    @Override
    public String toString() {
        return "Quaternion{" + "a=" + a + ", b=" + b + ", c=" + c + ", d=" + d + '}';
    }
}
