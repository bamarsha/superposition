package engine.util.math;

import org.joml.Vector3d;

public class Vec3d {

    public final double x, y, z;

    public Vec3d(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vec3d add(double a) {
        return new Vec3d(x + a, y + a, z + a);
    }

    public Vec3d add(Vec3d other) {
        return new Vec3d(x + other.x, y + other.y, z + other.z);
    }

    public Vec3d clamp(double lower, double upper) {
        return new Vec3d(MathUtils.clamp(x, lower, upper), MathUtils.clamp(y, lower, upper), MathUtils.clamp(z, lower, upper));
    }

    public Vec3d clamp(Vec3d lower, Vec3d upper) {
        return new Vec3d(MathUtils.clamp(x, lower.x, upper.x), MathUtils.clamp(y, lower.y, upper.y), MathUtils.clamp(z, lower.z, upper.z));
    }

    public Vec3d cross(Vec3d other) {
        return new Vec3d(y * other.z - z * other.y, z * other.x - x * other.z, x * other.y - y * other.x);
    }

    public Vec3d div(double a) {
        return new Vec3d(x / a, y / a, z / a);
    }

    public Vec3d div(Vec3d other) {
        return new Vec3d(x / other.x, y / other.y, z / other.z);
    }

    public double dot(Vec3d other) {
        return x * other.x + y * other.y + z * other.z;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Vec3d other = (Vec3d) obj;
        if (Double.doubleToLongBits(this.x) != Double.doubleToLongBits(other.x)) {
            return false;
        }
        if (Double.doubleToLongBits(this.y) != Double.doubleToLongBits(other.y)) {
            return false;
        }
        if (Double.doubleToLongBits(this.z) != Double.doubleToLongBits(other.z)) {
            return false;
        }
        return true;
    }

    public Vec3d floor() {
        return new Vec3d(Math.floor(x), Math.floor(y), Math.floor(z));
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + (int) (Double.doubleToLongBits(this.x) ^ (Double.doubleToLongBits(this.x) >>> 32));
        hash = 67 * hash + (int) (Double.doubleToLongBits(this.y) ^ (Double.doubleToLongBits(this.y) >>> 32));
        hash = 67 * hash + (int) (Double.doubleToLongBits(this.z) ^ (Double.doubleToLongBits(this.z) >>> 32));
        return hash;
    }

    public double length() {
        return Math.sqrt(x * x + y * y + z * z);
    }

    public double lengthSquared() {
        return x * x + y * y + z * z;
    }

    public Vec3d lerp(Vec3d other, double amt) {
        return mul(1 - amt).add(other.mul(amt));
    }

    public Vec3d lerp(Vec3d other, Vec3d amt) {
        return mul(new Vec3d(1, 1, 1).sub(amt)).add(other.mul(amt));
    }

    public Vec3d mul(double a) {
        return new Vec3d(x * a, y * a, z * a);
    }

    public Vec3d mul(Vec3d other) {
        return new Vec3d(x * other.x, y * other.y, z * other.z);
    }

    public Vec3d normalize() {
        return div(length());
    }

    public Vec3d projectAgainst(Vec3d v) {
        return sub(projectOnto(v));
    }

    public Vec3d projectOnto(Vec3d v) {
        return v.mul(dot(v) / v.lengthSquared());
    }

    public Vec3d setLength(double l) {
        if (l == 0) {
            return new Vec3d(0, 0, 0);
        } else {
            return mul(l / length());
        }
    }

    public Vec3d setX(double x) {
        return new Vec3d(x, y, z);
    }

    public Vec3d setY(double y) {
        return new Vec3d(x, y, z);
    }

    public Vec3d setZ(double z) {
        return new Vec3d(x, y, z);
    }

    public Vec3d sub(double a) {
        return new Vec3d(x - a, y - a, z - a);
    }

    public Vec3d sub(Vec3d other) {
        return new Vec3d(x - other.x, y - other.y, z - other.z);
    }

    public Vector3d toJOML() {
        return new Vector3d(x, y, z);
    }

    @Override
    public String toString() {
        return "Vec3d{" + "x=" + x + ", y=" + y + ", z=" + z + '}';
    }
}
