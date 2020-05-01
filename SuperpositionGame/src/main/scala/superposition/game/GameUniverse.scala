package superposition.game

import superposition.math.Vector2i
import superposition.quantum.{StateId, Universe}

private object GameUniverse {

  final implicit class Ops(val universe: Universe) extends AnyVal {
//    def allInCell(cell: Vector2i): Iterable[UniverseComponent] =
//      UniverseComponent.All filter (_.position map (universe.state(_)) contains cell)

//    def primaryBits(cell: Vector2i): Iterable[StateId[Boolean]] =
//      allInCell(cell) flatMap (_.primaryBit.toList)

//    def isBlocked(cell: Vector2i): Boolean =
//      UniverseComponent.All exists (_.blockingCells(universe) contains cell)

//    def allOn(controls: Iterable[Vector2i]): Boolean =
//      controls forall { control =>
//        Quball.All exists { quball =>
//          universe.state(quball.cell) == control && universe.state(quball.onOff)
//        }
//      }

//    def isValid: Boolean = Player.All forall (player => !isBlocked(universe.state(player.cell)))
  }

}
