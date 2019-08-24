package engine.util

import org.joml.Vector3d
import kotlin.math.sqrt

data class Vec3d(val x: Double, val y: Double, val z: Double) {

    operator fun unaryMinus() = Vec3d(-x, -y, -z)

    operator fun plus(b: Vec3d) = Vec3d(x + b.x, y + b.y, z + b.z)

    operator fun minus(b: Vec3d) = Vec3d(x - b.x, y - b.y, z - b.z)

    operator fun times(b: Double) = Vec3d(x * b, y * b, z * b)

    operator fun Double.times(b: Vec3d) = b * this

    operator fun times(b: Vec3d) = Vec3d(x * b.x, y * b.y, z * b.z)

    operator fun div(b: Double) = Vec3d(x / b, y / b, z / b)

    fun cross(other: Vec3d) = Vec3d(y * other.z - z * other.y, z * other.x - x * other.z, x * other.y - y * other.x)

    fun length() = sqrt(x * x + y * y + z * z)

    fun lerp(b: Vec3d, amt: Double) = (1 - amt) * this + amt * b

    fun lerp(b: Vec3d, amt: Vec3d) = (Vec3d(1.0, 1.0, 1.0) - amt) * this + amt * b

    fun normalize() = this / length()

    fun setLength(d: Double) = if (d == 0.0) Vec3d(0.0, 0.0, 0.0) else normalize() * d

    fun toJoml() = Vector3d(x, y, z)
}
