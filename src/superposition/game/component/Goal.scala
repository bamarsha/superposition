package superposition.game.component

import com.badlogic.ashley.core.{Component, ComponentMapper}
import superposition.math.Vector2i
import superposition.quantum.StateId

final class Goal(val multiverse: Multiverse, needsThunk: () => StateId[Vector2i]) extends Component {
  lazy val needs: StateId[Vector2i] = needsThunk()
}

object Goal {
  val Mapper: ComponentMapper[Goal] = ComponentMapper.getFor(classOf[Goal])
}
