package superposition.component

import com.badlogic.ashley.core.{Component, ComponentMapper}
import superposition.math.StateId

/** The Fourier qubit is used by the QFT.
  *
  * @param bit the fourier bit
  */
final class FourierBit(val bit: StateId[Boolean]) extends Component

/** Contains the component mapper for the Fourier bit component. */
object FourierBit {

  /** The component mapper for the Fourier bit component. */
  val mapper: ComponentMapper[FourierBit] = ComponentMapper.getFor(classOf[FourierBit])
}
