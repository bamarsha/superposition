package engine.util

import org.joml.Vector2d
import java.lang.Math.sqrt

data class Vec2d(val x: Double, val y: Double) {

    operator fun unaryMinus() = Vec2d(-x, -y)

    operator fun plus(b: Vec2d) = Vec2d(x + b.x, y + b.y)

    operator fun minus(b: Vec2d) = Vec2d(x - b.x, y - b.y)

    operator fun times(b: Double) = Vec2d(x * b, y * b)

    operator fun Double.times(b: Vec2d) = b * this

    operator fun times(b: Vec2d) = Vec2d(x * b.x, y * b.y)

    operator fun div(b: Double) = Vec2d(x / b, y / b)

    fun length() = sqrt(x * x + y * y)

    fun lerp(b: Vec2d, amt: Double) = (1 - amt) * this + amt * b

    fun lerp(b: Vec2d, amt: Vec2d) = (Vec2d(1.0, 1.0) - amt) * this + amt * b

    fun normalize() = this / length()

    fun setLength(d: Double) = if (d == 0.0) Vec2d(0.0, 0.0) else normalize() * d

    fun toJoml() = Vector2d(x, y)
}
