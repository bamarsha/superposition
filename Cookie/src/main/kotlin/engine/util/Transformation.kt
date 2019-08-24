package engine.util

import org.joml.Matrix3d
import org.joml.Matrix4d
import org.joml.Vector4d

class Transformation(private val m: Matrix4d) {

    fun apply(v: Vec2d): Vec2d {
        val v4 = m.transform(Vector4d(v.x, v.y, 0.0, 1.0))
        return Vec2d(v4.x, v4.y)
    }

    fun apply(v: Vec3d): Vec3d {
        val v4 = m.transform(Vector4d(v.x, v.y, v.z, 1.0))
        return Vec3d(v4.x, v4.y, v4.z)
    }

    fun applyRotation(v: Vec3d): Vec3d {
        val v4 = m.transform(Vector4d(v.x, v.y, v.z, 0.0))
        return Vec3d(v4.x, v4.y, v4.z)
    }

    fun matrix(): Matrix4d {
        return Matrix4d(m)
    }

    fun mul(t: Transformation): Transformation {
        return Transformation(m.mul(t.m, Matrix4d()))
    }

    fun position(): Vec3d {
        val v = Vector4d(0.0, 0.0, 0.0, 1.0).mul(m)
        return Vec3d(v.x, v.y, v.z)
    }

    fun scale(s: Double): Transformation {
        return Transformation(m.scale(s, Matrix4d()))
    }

    override fun toString(): String {
        return "Transformation{m=$m}"
    }

    fun translate(offset: Vec3d): Transformation {
        return Transformation(m.translate(offset.toJoml(), Matrix4d()))
    }

    companion object {

        val IDENTITY = Transformation(Matrix4d())

        fun create(position: Vec2d, rotation: Double, scale: Double): Transformation {
            return create(position, rotation, Vec2d(scale, scale))
        }

        fun create(position: Vec2d, rotation: Double, scale: Vec2d): Transformation {
            return create(Vec3d(position.x, position.y, 0.0), Quaternion.fromAngleAxis(rotation, Vec3d(0.0, 0.0, 1.0)), Vec3d(scale.x, scale.y, 1.0))
        }

        fun create(position: Vec3d, rotation: Quaternion, scale: Double): Transformation {
            return create(position, rotation, Vec3d(scale, scale, scale))
        }

        fun create(position: Vec3d, rotation: Quaternion, scale: Vec3d): Transformation {
            return Transformation(Matrix4d().translationRotateScale(position.toJoml(), rotation.toJoml(), scale.toJoml()))
        }

        fun create(position: Vec2d, xAxis: Vec2d, yAxis: Vec2d): Transformation {
            return create(Vec3d(position.x, position.y, 0.0), Vec3d(xAxis.x, xAxis.y, 0.0), Vec3d(yAxis.x, yAxis.y, 0.0), Vec3d(0.0, 0.0, 1.0))
        }

        fun create(position: Vec3d, xAxis: Vec3d, yAxis: Vec3d, zAxis: Vec3d): Transformation {
            val linearPart = Matrix3d().setColumn(0, xAxis.toJoml()).setColumn(1, yAxis.toJoml()).setColumn(2, zAxis.toJoml())
            return Transformation(Matrix4d().translate(position.toJoml()).set3x3(linearPart))
        }
    }
}
