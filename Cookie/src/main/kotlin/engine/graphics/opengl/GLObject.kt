package engine.graphics.opengl

abstract class GLObject(val id: Int) {

    abstract fun bind()

    abstract fun destroy()

    companion object {

        fun bindAll(vararg a: GLObject) {
            for (o in a) {
                o.bind()
            }
        }
    }
}
