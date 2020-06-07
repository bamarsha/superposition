package superposition.component;

import com.badlogic.ashley.core.{Component, ComponentMapper}
import superposition.math.Unitary


/** The OracleUnitary component lets an oracle apply a unitary.
 *
 * @param unitary the unitary to apply
 */
final class OracleUnitary(val unitary: Unitary, val conjugate: Boolean) extends Component

/** Contains the component mapper for the OracleUnitary component. */
object OracleUnitary {
    /** The component mapper for the OracleUnitary component. */
    val mapper: ComponentMapper[OracleUnitary] = ComponentMapper.getFor(classOf[OracleUnitary])
}
