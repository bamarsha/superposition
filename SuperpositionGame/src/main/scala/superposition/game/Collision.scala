package superposition.game

import com.badlogic.ashley.core.Component
import superposition.math.Vector2i
import superposition.quantum.Universe

import scala.Function.const

private final class Collision(val cells: Universe => Set[Vector2i] = const(Set.empty)) extends Component
