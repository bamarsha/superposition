package superposition.language

import com.badlogic.gdx.maps.tiled.TiledMap
import scalaz.syntax.contravariant._
import superposition.component.{Multiverse, PrimaryBit, QuantumPosition}
import superposition.language.Interpreter.NTuple
import superposition.language.Parser.{expression, parse, program}
import superposition.math._

import scala.Function.{chain, const}
import scala.sys.error

/** An interpreter for gate programs.
  *
  * @param multiverse the multiverse
  * @param map the tile map
  */
final class Interpreter(multiverse: Multiverse, map: TiledMap) {
  /** The height of the tile map. */
  private val height: Int = Option(map.getProperties.get("height", classOf[Int])).get

  /** Evaluates a program string.
    *
    * @param string the program string
    * @return the evaluated program
    */
  def evalProgram(string: String): Gate[Unit] = evalProgram(parse(program, string).get)

  /** Evaluates an expression string.
    *
    * @param string the expression string
    * @tparam A the type of the expression
    * @return the evaluated expression
    */
  def evalExpression[A](string: String): Universe => A =
    evalExpression(parse(expression, string).get).asInstanceOf[Universe => A]

  /** Evaluates a program.
    *
    * @param program the program sequence
    * @return the evaluated program
    */
  private def evalProgram(program: Seq[Application]): Gate[Unit] = program.view map evalApplication reduce (_ andThen _)

  /** Evaluates an expression.
    *
    * @param expression the expression
    * @return the evaluated expression
    */
  private def evalExpression(expression: Expression): Universe => Any = expression match {
    case Identifier(name) => evalIdentifier(name)
    case Number(value) => const(value)
    case Tuple(items) =>
      val xs = items map evalExpression
      universe => NTuple(xs map (_ (universe)): _*)
    case List(items) =>
      val xs = items map evalExpression
      universe => xs map (_ (universe))
    case Call(function, argument) =>
      val func = evalExpression(function)
      val arg = evalExpression(argument)
      universe => func(universe).asInstanceOf[Any => Any](arg(universe))
    case Equals(lhs, rhs) =>
      val l = evalExpression(lhs)
      val r = evalExpression(rhs)
      universe => l(universe) == r(universe)
  }

  /** Evaluates a transformer.
    *
    * @param transformer the transformer.
    * @return the evaluated transformer
    */
  private def evalTransformer(transformer: Transformer): Gate[Any] => Gate[Any] = transformer match {
    case OnTransformer(argument) =>
      val arg = evalExpression(argument)
      _.controlledMap(value => universe => {
        if (value == ()) arg(universe)
        else arg(universe).asInstanceOf[Any => Any](value)
      })
    case IfTransformer(expression) =>
      val expr = evalExpression(expression).asInstanceOf[Universe => Boolean]
      _.controlled(expr)
    case MultiTransformer => _.multi.asInstanceOf[Gate[Any]]
  }

  /** Evaluates an application.
    *
    * @param application the application
    * @return the evaluated application
    */
  private def evalApplication(application: Application): Gate[Unit] = {
    val gate = makeGate(application.gate)
    val allTransformations = chain(application.transformers map evalTransformer)
    allTransformations(gate.asInstanceOf[Gate[Any]]).asInstanceOf[Gate[Unit]]
  }

  /** Evaluates an identifier name.
    *
    * @param name the name of the identifier
    * @return the evaluated identifier name
    */
  private def evalIdentifier(name: String): Universe => Any = name match {
    case "allOn" => universe => multiverse.allOn(universe, _)
    case "bit" => const(multiverse.entityById(_: Int).get.getComponent(classOf[PrimaryBit]).bit)
    case "cell" => const(multiverse.entityById(_: Int).get.getComponent(classOf[QuantumPosition]).cell)
    case "value" => universe => (id: StateId[_]) => universe.state(id)
    case "vec2" => const(makeVector2 _)
    case _ => error(s"Unknown identifier: $name")
  }

  /** Makes a gate corresponding to the name.
    *
    * @param name the gate name
    * @return the gate
    */
  private def makeGate(name: String): Gate[_] = name match {
    case "X" => X
    case "H" => H
    case "Translate" => Translate contramap[NTuple] {
      // TODO: Require the second argument to be a Vector2.
      case NTuple(id: StateId[Vector2[Int]], NTuple(x: Int, y: Int)) => (id, Vector2(x, y))
    }
    case _ => error(s"Unknown gate: $name")
  }

  /** Makes a vector from its components.
    *
    * @param components the vector components
    * @return the vector
    */
  private def makeVector2(components: NTuple): Vector2[Int] = components match {
    case NTuple(x: Int, y: Int) => Vector2(x, height - y - 1)
  }
}

/** Data types for the interpreter. */
private object Interpreter {

  /** An n-ary tuple.
    *
    * @param items the items in the tuple.
    */
  private final case class NTuple(items: Any*)

}
