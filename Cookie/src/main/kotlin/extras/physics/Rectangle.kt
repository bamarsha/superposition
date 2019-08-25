package extras.physics

import engine.util.Vec2d

data class Rectangle(val lowerLeft: Vec2d, val upperRight: Vec2d) {

    companion object {
        fun fromCenterSize(center: Vec2d, size: Vec2d) =
                Rectangle(center - size / 2.0, center + size / 2.0)
    }

    fun intersects(other: Rectangle) =
            lowerLeft.x <= other.upperRight.x && lowerLeft.y <= other.upperRight.y
                    && upperRight.x >= other.lowerLeft.x && upperRight.y >= other.lowerLeft.y
}
