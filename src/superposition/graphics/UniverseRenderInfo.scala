package superposition.graphics

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Color.CLEAR

/** The information for rendering an entity in a particular universe.
  *
  * @param color the color of the universe
  */
final case class UniverseRenderInfo(color: Color) extends AnyVal

/** Contains presets for universe rendering information. */
object UniverseRenderInfo {
  /** The default rendering information. */
  val default: UniverseRenderInfo = UniverseRenderInfo(CLEAR)
}
