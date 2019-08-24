package engine.util

data class Color(val r: Double, val g: Double, val b: Double, val a: Double = 0.0) {

    companion object {
        val AMBER = Color(1.0, .75, 0.0)
        val BLACK = Color(0.0, 0.0, 0.0)
        val BLUE = Color(0.0, 0.0, 1.0)
        val CLEAR = Color(0.0, 0.0, 0.0, 0.0)
        val CYAN = Color(0.0, 1.0, 1.0)
        val GREEN = Color(0.0, 1.0, 0.0)
        val GRAY = Color(.5, .5, .5)
        val LIME = Color(.75, 1.0, 0.0)
        val MAGENTA = Color(1.0, 0.0, 1.0)
        val ORANGE = Color(1.0, .5, 0.0)
        val PURPLE = Color(.75, 0.0, 1.0)
        val RED = Color(1.0, 0.0, 0.0)
        val VERMILION = Color(1.0, .25, 0.0)
        val VIOLET = Color(.375, 0.0, 1.0)
        val WHITE = Color(1.0, 1.0, 1.0)
        val YELLOW = Color(1.0, 1.0, 0.0)
    }
}
