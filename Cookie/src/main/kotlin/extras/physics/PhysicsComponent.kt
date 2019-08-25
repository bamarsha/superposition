package extras.physics

import engine.core.Component
import engine.core.Game
import engine.core.Game.dt
import engine.util.Vec2d
import extras.physics.Rectangle.Companion.fromCenterSize
import kotlin.math.pow

class PhysicsComponent : Component() {

    companion object {
        init {
            Game.declareSystem(PhysicsComponent::class.java) { obj -> obj.step() }
        }

        fun wallCollider(hitboxSize: Vec2d, walls: List<Rectangle>) = { pos: Vec2d ->
            walls.any { it.intersects(fromCenterSize(pos, hitboxSize)) }
        }
    }

    var position: Vec2d = Vec2d(0.0, 0.0)
    var velocity: Vec2d = Vec2d(0.0, 0.0)
    var collider: (Vec2d) -> Boolean = { false }
    var hitWall: Boolean = false

    private fun moveToWall(dir: Vec2d) {
        for (i in 1..10) {
            val newPos = position + dir * 0.5.pow(i)
            if (!collider(newPos)) {
                position = newPos
            }
        }
    }

    fun step() {
        val dir = velocity * dt()
        if (collider(position + dir)) {
            moveToWall(Vec2d(dir.x, 0.0))
            moveToWall(Vec2d(0.0, dir.y))
        } else {
            position += dir
        }
    }
}
