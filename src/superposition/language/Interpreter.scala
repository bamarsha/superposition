package superposition.language

import com.badlogic.gdx.maps.tiled.TiledMap
import scalaz.Scalaz._
import superposition.component.{Multiverse, PrimaryBit, QuantumPosition}
import superposition.language.Interpreter.NTuple
import superposition.language.Parser.{NoSuccess, Success, expressionProgram, gateProgram, parse}
import superposition.math._

import scala.Function.chain
import scala.sys.error

/** An interpreter for gate programs.
  *
  * @param multiverse the multiverse
  * @param map the tile map
  */
final class Interpreter(multiverse: Multiverse, map: TiledMap) {
  /** The height of the tile map. */
  private val height: Int = Option(map.getProperties.get("height", classOf[Int])).get

  /** Evaluates a gate program string.
    *
    * @param string the program string
    * @throws RuntimeException if parsing fails
    * @return the evaluated program
    */
  def evalGate(string: String): Gate[Unit] = parse(gateProgram, string) match {
    case Success(program, _) => evalProgram(program)
    case NoSuccess(message, _) => error(s"Syntax error in gate program ($message): $string")
  }

  /** Evaluates an expression program string.
    *
    * @param string the expression string
    * @tparam A the type of the expression
    * @throws RuntimeException if parsing fails
    * @return the evaluated expression
    */
  def evalExpression[A](string: String): QExpr[A] = parse(expressionProgram, string) match {
    case Success(expression, _) => evalExpression(expression).asInstanceOf[QExpr[A]]
    case NoSuccess(message, _) => error(s"Syntax error in expression program ($message): $string")
  }

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
  private def evalExpression(expression: Expression): QExpr[Any] = {
    val expr = expression match {
      case Identifier(name) => evalIdentifier(name)
      case Number(value) => value.pure[QExpr]
      case Tuple(items) => (items map evalExpression).toList.sequence map (NTuple(_: _*))
      case List(items) => (items map evalExpression).toList.sequence
      case Call(function, argument) =>
        for {
          func <- evalExpression(function)
          arg <- evalExpression(argument)
        } yield func.asInstanceOf[Any => Any](arg)
      case Equals(lhs, rhs) =>
        for {
          l <- evalExpression(lhs)
          r <- evalExpression(rhs)
        } yield l == r
    }
    if (isConstant(expression)) expr(Universe.empty).pure[QExpr]
    else expr
  }

  /** Evaluates a transformer.
    *
    * @param transformer the transformer.
    * @return the evaluated transformer
    */
  private def evalTransformer(transformer: Transformer): Gate[Any] => Gate[Any] = transformer match {
    case OnTransformer(argument) =>
      _.controlledMap(evalExpression(argument) map (arg => {
        case () => arg
        case value => arg.asInstanceOf[Any => Any](value)
      }))
    case IfTransformer(expression) =>
      val expr = evalExpression(expression).asInstanceOf[QExpr[Boolean]]
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
  private def evalIdentifier(name: String): QExpr[Any] = name match {
    case "activated" => ??? // universe => (l: Iterable[Vector2[Int]]) => multiverse.allActivated(universe, l)
    case "activeCell" => ??? // universe => (nt: NTuple) => multiverse.allActivated(universe, Seq(makeCell(nt)))(0)
    case "bitAt" => ({ case NTuple(bits: BitSeq, index: Int) => bits(index) }: NTuple => Boolean).pure[QExpr]
    case "indices" =>
      ({ case NTuple(items: Seq[_], indices: Seq[Int]) => indices map (items(_)) }: NTuple => Any).pure[QExpr]
    case "int" => ((_: BitSeq).toInt).pure[QExpr]
    case "and" => ((_: NTuple).items.forall(_.asInstanceOf[Boolean])).pure[QExpr]
    case "or" => ((_: NTuple).items.exists(_.asInstanceOf[Boolean])).pure[QExpr]
    case "qubit" => (multiverse.entityById(_: Int).get.getComponent(classOf[PrimaryBit]).bits.head).pure[QExpr]
    case "qubits" => (multiverse.entityById(_: Int).get.getComponent(classOf[PrimaryBit]).bits).pure[QExpr]
    case "qucell" => (multiverse.entityById(_: Int).get.getComponent(classOf[QuantumPosition]).cell).pure[QExpr]
    case "value" => QExpr.liftBind((_: StateId[_]).value)
    case "vec2" => ({ case NTuple(x: Int, y: Int) => Vector2(x, y) }: NTuple => Vector2[Int]).pure[QExpr]
    case "cell" => makeCell.pure[QExpr]
    case _ => error(s"Unknown identifier: $name")
  }

  /** Maps a TiledMap cell to a Vector2, accounting for the different coordinate systems
    *
    * @return The function that maps the TiledMap cell to the Vector2
    */
  private def makeCell: NTuple => Vector2[Int] = { case NTuple(x: Int, y: Int) => Vector2(x, height - y - 1) }

  /** Makes a gate corresponding to the name.
    *
    * @param name the gate name
    * @return the gate
    */
  private def makeGate(name: String): Gate[_] = name match {
    case "X" => X
    case "H" => H
    case "Translate" => Translate contramap[NTuple] {
      case NTuple(id: StateId[Vector2[Int]], delta: Vector2[Int]) => (id, delta)
    }
    case _ => error(s"Unknown gate: $name")
  }

  /** Returns true if the expression does not depend on the universe.
    *
    * @param expression the expression
    * @return true if the expression does not depend on the universe
    */
  private def isConstant(expression: Expression): Boolean = expression match {
    case Identifier("qubit") |
         Identifier("qucell") |
         Identifier("vec2") |
         Identifier("cell") |
         Number(_) => true
    case Tuple(items) => items forall isConstant
    case List(items) => items forall isConstant
    case Call(function, argument) => isConstant(function) && isConstant(argument)
    case Equals(lhs, rhs) => isConstant(lhs) && isConstant(rhs)
    case _ => false
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
