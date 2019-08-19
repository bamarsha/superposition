package engine.util.math;

import org.joml.Vector4d;

public class Vec4d {

    public final double x, y, z, w;

    public Vec4d(double x, double y, double z, double w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    public Vec4d add(double a) {
        return new Vec4d(x + a, y + a, z + a, w + a);
    }

    public Vec4d add(Vec4d other) {
        return new Vec4d(x + other.x, y + other.y, z + other.z, w + other.w);
    }

    public Vec4d clamp(double lower, double upper) {
        return new Vec4d(MathUtils.clamp(x, lower, upper), MathUtils.clamp(y, lower, upper), MathUtils.clamp(z, lower, upper), MathUtils.clamp(w, lower, upper));
    }

    public Vec4d clamp(Vec4d lower, Vec4d upper) {
        return new Vec4d(MathUtils.clamp(x, lower.x, upper.x), MathUtils.clamp(y, lower.y, upper.y), MathUtils.clamp(z, lower.z, upper.z), MathUtils.clamp(w, lower.w, upper.w));
    }

    public Vec4d div(double a) {
        return new Vec4d(x / a, y / a, z / a, w / a);
    }

    public Vec4d div(Vec4d other) {
        return new Vec4d(x / other.x, y / other.y, z / other.z, w / other.w);
    }

    public double dot(Vec4d other) {
        return x * other.x + y * other.y + z * other.z + w * other.w;
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
        final Vec4d other = (Vec4d) obj;
        if (Double.doubleToLongBits(this.x) != Double.doubleToLongBits(other.x)) {
            return false;
        }
        if (Double.doubleToLongBits(this.y) != Double.doubleToLongBits(other.y)) {
            return false;
        }
        if (Double.doubleToLongBits(this.z) != Double.doubleToLongBits(other.z)) {
            return false;
        }
        if (Double.doubleToLongBits(this.w) != Double.doubleToLongBits(other.w)) {
            return false;
        }
        return true;
    }

    public Vec4d floor() {
        return new Vec4d(Math.floor(x), Math.floor(y), Math.floor(z), Math.floor(w));
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + (int) (Double.doubleToLongBits(this.x) ^ (Double.doubleToLongBits(this.x) >>> 32));
        hash = 89 * hash + (int) (Double.doubleToLongBits(this.y) ^ (Double.doubleToLongBits(this.y) >>> 32));
        hash = 89 * hash + (int) (Double.doubleToLongBits(this.z) ^ (Double.doubleToLongBits(this.z) >>> 32));
        hash = 89 * hash + (int) (Double.doubleToLongBits(this.w) ^ (Double.doubleToLongBits(this.w) >>> 32));
        return hash;
    }

    public double length() {
        return Math.sqrt(x * x + y * y + z * z + w * w);
    }

    public double lengthSquared() {
        return x * x + y * y + z * z + w * w;
    }

    public Vec4d lerp(Vec4d other, double amt) {
        return mul(1 - amt).add(other.mul(amt));
    }

    public Vec4d mul(double a) {
        return new Vec4d(x * a, y * a, z * a, w * a);
    }

    public Vec4d mul(Vec4d other) {
        return new Vec4d(x * other.x, y * other.y, z * other.z, w * other.w);
    }

    public Vec4d normalize() {
        return div(length());
    }

    public Vec4d setLength(double l) {
        if (l == 0) {
            return new Vec4d(0, 0, 0, 0);
        } else {
            return mul(l / length());
        }
    }

    public Vec4d setX(double x) {
        return new Vec4d(x, y, z, w);
    }

    public Vec4d setY(double y) {
        return new Vec4d(x, y, z, w);
    }

    public Vec4d setZ(double z) {
        return new Vec4d(x, y, z, w);
    }

    public Vec4d setW(double z) {
        return new Vec4d(x, y, z, w);
    }

    public Vec4d sub(double a) {
        return new Vec4d(x - a, y - a, z - a, w - a);
    }

    public Vec4d sub(Vec4d other) {
        return new Vec4d(x - other.x, y - other.y, z - other.z, w - other.w);
    }

    public Vector4d toJOML() {
        return new Vector4d(x, y, z, w);
    }

    @Override
    public String toString() {
        return "Vec4d{" + "x=" + x + ", y=" + y + ", z=" + z + ", w=" + w + '}';
    }
}
