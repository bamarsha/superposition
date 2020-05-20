package superposition.language

import com.badlogic.ashley.core.Entity
import superposition.component.{PrimaryBit, QuantumPosition}
import superposition.math.Gate.DivisibleGate.divisibleSyntax._
import superposition.math._

import scala.Function.{chain, const}

class Interpreter(objects: Map[Int, Entity]) {

  private val bitFunction = (id: Int) => objects(id).getComponent(classOf[PrimaryBit]).bit
  private val cellFunction = (id: Int) => objects(id).getComponent(classOf[QuantumPosition]).cell

  def iden2scala(name: String): Universe => _ = name match {
    case "bit" => const(bitFunction)
    case "cell" => const(cellFunction)
    case "value" => u => (stateId: StateId[_]) => u.state(stateId)
    case _ => throw new RuntimeException("Unknown literal: " + name)
  }

  def expr2scala(expr: Expression): Universe => _ = expr match {
    case Identifier(name) => iden2scala(name)
    case Number(n) => const(n)
    case Tuple(a) => u => a.map(expr2scala).map(_(u)).toList
    case Call(a, b) => u => expr2scala(a)(u).asInstanceOf[Any => _](expr2scala(b)(u))
    case Equals(a, b) => u => expr2scala(a)(u) == expr2scala(b)(u)
  }

  def trans2scala(trans: Transformer): Gate[Any] => Gate[Any] = trans match {
    case OnTransformer(expr) => _.controlled((b: Any) => u => {
      val expr2 = expr2scala(expr)(u)
      if (b == ()) expr2 else expr2scala(expr)(u).asInstanceOf[Any => Any](b)
    })
    case IfTransformer(expr) => _.filter2(expr2scala(expr).asInstanceOf[Universe => Boolean])
    case MultiTransformer => _.multi.asInstanceOf[Gate[Any]]
  }

  def gate2scala(name: String): Gate[_] = name match {
    case "X" => X contramap ((x: StateId[Boolean]) => { println("X on " + x.name); x })
    case "H" => H contramap ((x: StateId[Boolean]) => { println("H on " + x.name); x })
    case "Translate" => Translate contramap ((x: (StateId[Vector2[Int]], Vector2[Int])) => { println(x); x }) contramap[List[Any]] {
      case List(stateId: StateId[Vector2[Int]], List(x: Int, y: Int)) => (stateId, Vector2(x, y)) }
    case _ => throw new RuntimeException("Unknown gate: " + name)
  }

  def app2scala(app: Application): Gate[Unit] = {
    val gate = gate2scala(app.gate)
    val allTransformations = chain(app.transformers.map(trans2scala))
    allTransformations(gate.asInstanceOf[Gate[Any]]).asInstanceOf[Gate[Unit]]
  }

  def program2scala(program: Seq[Application]): Gate[Unit] = program.map(app2scala).reduce(_ andThen _)
}
