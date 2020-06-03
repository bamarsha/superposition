package superposition.system

import com.badlogic.ashley.core.{Entity, Family}
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.Gdx.input
import superposition.component.{ClassicalPosition, OracleUnitary}
import superposition.entity.Level

/** The system for activating lasers based on player input.
  *
  * @param level a function that returns the current level
  */
final class OracleInputSystem(level: () => Option[Level])
  extends IteratingSystem(Family.all(classOf[OracleUnitary], classOf[ClassicalPosition]).get) {
  override def processEntity(entity: Entity, deltaTime: Float): Unit = {
    val cell = ClassicalPosition.mapper.get(entity).cells.head
    val multiverse = level().get.multiverse
    val multiverseView = level().get.multiverseView

    // Apply the unitary when the oracle is clicked.
    if (input.isButtonJustPressed(0) && multiverseView.isSelected(cell)) {
      val oracleUnitary = OracleUnitary.mapper.get(entity)
      multiverse.applyUnitary(oracleUnitary.unitary, oracleUnitary.conjugate)
    }
  }
}
