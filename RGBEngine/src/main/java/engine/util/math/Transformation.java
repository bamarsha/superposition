package engine.util.math;

import org.joml.Matrix3d;
import org.joml.Matrix4d;
import org.joml.Vector4d;

public class Transformation {

    public static final Transformation IDENTITY = new Transformation(new Matrix4d());

    private final Matrix4d m;

    public Transformation(Matrix4d m) {
        this.m = m;
    }

    public Vec2d apply(Vec2d v) {
        Vector4d v4 = m.transform(new Vector4d(v.x, v.y, 0, 1));
        return new Vec2d(v4.x, v4.y);
    }

    public Vec3d apply(Vec3d v) {
        Vector4d v4 = m.transform(new Vector4d(v.x, v.y, v.z, 1));
        return new Vec3d(v4.x, v4.y, v4.z);
    }

    public Vec3d applyRotation(Vec3d v) {
        Vector4d v4 = m.transform(new Vector4d(v.x, v.y, v.z, 0));
        return new Vec3d(v4.x, v4.y, v4.z);
    }

    public static Transformation create(Vec2d position, double rotation, double scale) {
        return create(position, rotation, new Vec2d(scale, scale));
    }

    public static Transformation create(Vec2d position, double rotation, Vec2d scale) {
        return create(new Vec3d(position.x, position.y, 0), Quaternion.fromAngleAxis(rotation, new Vec3d(0, 0, 1)), new Vec3d(scale.x, scale.y, 1));
    }

    public static Transformation create(Vec3d position, Quaternion rotation, double scale) {
        return create(position, rotation, new Vec3d(scale, scale, scale));
    }

    public static Transformation create(Vec3d position, Quaternion rotation, Vec3d scale) {
        return new Transformation(new Matrix4d().translationRotateScale(position.toJOML(), rotation.toJOML(), scale.toJOML()));
    }

    public static Transformation create(Vec2d position, Vec2d xAxis, Vec2d yAxis) {
        return create(new Vec3d(position.x, position.y, 0), new Vec3d(xAxis.x, xAxis.y, 0), new Vec3d(yAxis.x, yAxis.y, 0), new Vec3d(0, 0, 1));
    }

    public static Transformation create(Vec3d position, Vec3d xAxis, Vec3d yAxis, Vec3d zAxis) {
        Matrix3d linearPart = new Matrix3d().setColumn(0, xAxis.toJOML()).setColumn(1, yAxis.toJOML()).setColumn(2, zAxis.toJOML());
        return new Transformation(new Matrix4d().translate(position.toJOML()).set3x3(linearPart));
    }

    public Matrix4d matrix() {
        return new Matrix4d(m);
    }

    public Transformation mul(Transformation t) {
        return new Transformation(m.mul(t.m, new Matrix4d()));
    }

    public Vec3d position() {
        Vector4d v = new Vector4d(0, 0, 0, 1).mul(m);
        return new Vec3d(v.x, v.y, v.z);
    }

    public Transformation scale(double s) {
        return new Transformation(m.scale(s, new Matrix4d()));
    }

    @Override
    public String toString() {
        return "Transformation{" + "m=" + m + '}';
    }

    public Transformation translate(Vec3d offset) {
        return new Transformation(m.translate(offset.toJOML(), new Matrix4d()));
    }
}
