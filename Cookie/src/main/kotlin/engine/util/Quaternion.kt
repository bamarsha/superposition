package engine.util

import engine.util.MathUtils.mod
import org.joml.Matrix3d
import org.joml.Quaterniond

class Quaternion private constructor(val a: Double, val b: Double, val c: Double, val d: Double) {

    val pitch: Double
        get() = Math.asin(2 * (a * c - b * d))

    val roll: Double
        get() = Math.atan2(2 * (a * b + c * d), 1 - 2 * (b * b + c * c))

    val yaw: Double
        get() = Math.atan2(2 * (a * d + b * c), 1 - 2 * (c * c + d * d))

    fun angle(): Double {
        return if (a < 0) {
            negate().angle()
        } else 2 * Math.atan2(Math.sqrt(b * b + c * c + d * d), a)
    }

    fun applyTo(pos: Vec3d): Vec3d {
        val result = mul(Quaternion(0.0, pos.x, pos.y, pos.z)).mul(inverse())
        return Vec3d(result.b, result.c, result.d)
    }

    fun applyToForwards(): Vec3d {
        val result = mul(Quaternion(0.0, 1.0, 0.0, 0.0)).mul(inverse())
        return Vec3d(result.b, result.c, result.d)
    }

    fun axis(): Vec3d {
        if (a < 0) {
            return negate().axis()
        }
        return if (Vec3d(b, c, d).length() < 1e-12) {
            Vec3d(1.0, 0.0, 0.0)
        } else Vec3d(b, c, d).normalize()
    }

    operator fun div(other: Quaternion): Quaternion {
        return other.inverse().mul(this)
    }

    fun inverse(): Quaternion {
        return Quaternion(a, -b, -c, -d)
    }

    fun lerp(other: Quaternion, amt: Double): Quaternion {
        var other = other
        if (a * other.a + b * other.b + c * other.c + d * other.d < 0) {
            other = other.negate()
        }
        return Quaternion(a * (1 - amt) + other.a * amt,
                b * (1 - amt) + other.b * amt,
                c * (1 - amt) + other.c * amt,
                d * (1 - amt) + other.d * amt).normalize()
    }

    fun mul(other: Quaternion): Quaternion {
        return Quaternion(
                a * other.a - b * other.b - c * other.c - d * other.d,
                a * other.b + b * other.a + c * other.d - d * other.c,
                a * other.c - b * other.d + c * other.a + d * other.b,
                a * other.d + b * other.c - c * other.b + d * other.a)
    }

    private fun negate(): Quaternion {
        return Quaternion(-a, -b, -c, -d)
    }

    fun normalize(): Quaternion {
        val length = Math.sqrt(a * a + b * b + c * c + d * d)
        return Quaternion(a / length, b / length, c / length, d / length)
    }

    fun pow(t: Double): Quaternion {
        return fromAngleAxis(t * angle(), axis())
    }

    fun toAngleAxis(): Vec3d {
        return axis() * angle()
    }

    fun toJoml(): Quaterniond {
        return Quaterniond(b, c, d, a)
    }

    override fun toString(): String {
        return "Quaternion{a=$a, b=$b, c=$c, d=$d}"
    }

    companion object {

        var IDENTITY = Quaternion(1.0, 0.0, 0.0, 0.0)

        fun fromAngleAxis(axis: Vec3d): Quaternion {
            return if (axis.length() < 1e-12) {
                IDENTITY
            } else fromAngleAxis(axis.length(), axis)
        }

        fun fromAngleAxis(angle: Double, axis: Vec3d): Quaternion {
            var axis = axis
            axis = axis.normalize()
            val sin = Math.sin(angle / 2)
            val cos = Math.cos(angle / 2)
            return Quaternion(cos, axis.x * sin, axis.y * sin, axis.z * sin)
        }

        fun fromEulerAngles(yaw: Double, pitch: Double, roll: Double): Quaternion {
            var yaw = yaw
            var pitch = pitch
            var roll = roll
            yaw = mod(yaw, Math.PI * 2.0)
            pitch = mod(pitch, Math.PI * 2.0)
            roll = mod(roll, Math.PI * 2.0)
            val sinYaw = Math.sin(yaw / 2)
            val cosYaw = Math.cos(yaw / 2)
            val sinPitch = Math.sin(pitch / 2)
            val cosPitch = Math.cos(pitch / 2)
            val sinRoll = Math.sin(roll / 2)
            val cosRoll = Math.cos(roll / 2)
            return Quaternion(
                    sinYaw * sinPitch * sinRoll + cosYaw * cosPitch * cosRoll,
                    -sinYaw * sinPitch * cosRoll + cosYaw * cosPitch * sinRoll,
                    sinYaw * cosPitch * sinRoll + cosYaw * sinPitch * cosRoll,
                    sinYaw * cosPitch * cosRoll - cosYaw * sinPitch * sinRoll)
        }

        fun fromXYAxes(x: Vec3d, y: Vec3d): Quaternion {
            var y = y
            val z = x.cross(y)
            y = z.cross(x)
            val m = Matrix3d(x.toJoml(), y.toJoml(), z.toJoml())
            val q = m.getUnnormalizedRotation(Quaterniond())
            return Quaternion(q.w, q.x, q.y, q.z)
        }
    }
}
