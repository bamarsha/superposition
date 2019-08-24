package engine.graphics

import engine.core.Settings
import engine.util.Vec2d
import engine.util.Vec3d
import org.joml.FrustumIntersection
import org.joml.Matrix4d
import org.joml.Matrix4f
import org.joml.Vector3d

abstract class Camera {

    abstract fun projectionMatrix(): Matrix4d

    abstract fun viewMatrix(): Matrix4d

    class Camera2d : Camera() {

        var lowerLeft = Vec2d(0.0, 0.0)
        var upperRight = Vec2d(1.0, 1.0)

        val center: Vec2d
            get() = lowerLeft.lerp(upperRight, .5)

        override fun projectionMatrix(): Matrix4d {
            val projectionMatrix = Matrix4d()
            projectionMatrix.setOrtho2D(lowerLeft.x, upperRight.x, lowerLeft.y, upperRight.y)
            return projectionMatrix
        }

        fun setCenterSize(center: Vec2d, size: Vec2d) {
            lowerLeft = center - size * 0.5
            upperRight = center + size * 0.5
        }

        fun transform(screenPos: Vec2d): Vec2d {
            return lowerLeft.lerp(upperRight, screenPos)
        }

        override fun viewMatrix(): Matrix4d {
            return Matrix4d()
        }
    }

    class Camera3d : Camera() {

        var position = Vec3d(0.0, 0.0, 0.0)
        var horAngle: Double = 0.toDouble()
        var vertAngle: Double = 0.toDouble()
        var up = Vec3d(0.0, 0.0, 1.0)

        var fov = 90.0
        var zNear = .2
        var zFar = 2000.0

        val viewFrustum: FrustumIntersection
            get() {
                val viewFrustum = FrustumIntersection()
                viewFrustum.set(Matrix4f(projectionMatrix().mul(viewMatrix())))
                return viewFrustum
            }

        fun facing(): Vec3d {
            return Vec3d(Math.cos(vertAngle) * Math.cos(horAngle), Math.cos(vertAngle) * Math.sin(horAngle), -Math.sin(vertAngle))
        }

        override fun projectionMatrix(): Matrix4d {
            val aspectRatio = Settings.WINDOW_WIDTH.toDouble() / Settings.WINDOW_HEIGHT
            val projectionMatrix = Matrix4d()
            projectionMatrix.perspective(fov * Math.PI / 180, aspectRatio, zNear, zFar)
            return projectionMatrix
        }

        override fun viewMatrix(): Matrix4d {
            return Matrix4d()
                    .rotate(vertAngle - Math.PI / 2, Vector3d(1.0, 0.0, 0.0))
                    .rotate(Math.PI / 2 - horAngle, Vector3d(0.0, 0.0, 1.0))
                    .translate(position.toJoml().mul(-1.0, Vector3d()))
            // Why am I adding/subtracting doubles from the angles? Idk, but it works.
        }
    }

    companion object {

        val camera2d = Camera2d()
        val camera3d = Camera3d()

        var current: Camera = camera2d
    }
}
