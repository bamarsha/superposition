package superposition.language

import com.badlogic.gdx.maps.tiled.TiledMap
import scalaz.Scalaz._
import superposition.component.Multiverse
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
  /** The built-in functions. */
  private val builtIns: BuiltIns = new BuiltIns(multiverse, map)

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
    case "activated" => builtIns.activated
    case "activeCell" => tuple2 andThen builtIns.activeCell
    case "and" => builtIns.and
    case "bitAt" => tuple2 andThen builtIns.bitAt
    case "cell" => tuple2 andThen builtIns.cell
    case "indices" => tuple2 andThen builtIns.indices[Any]
    case "int" => builtIns.int
    case "or" => builtIns.or
    case "qubit" => builtIns.qubit
    case "qubits" => builtIns.qubits
    case "qucell" => builtIns.qucell
    case "value" => builtIns.value
    case "vec2" => tuple2 andThen builtIns.vec2
    case _ => error(s"Unknown identifier: $name")
  }

  /** Converts an n-tuple into a 2-tuple. */
  private def tuple2[A, B]: QExpr[NTuple => (A, B)] = ((_: NTuple).toTuple2[A, B]).pure[QExpr]

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
    case Identifier(name) => name match {
      case "and" | "bitAt" | "cell" | "indices" | "int" | "or" | "qubit" | "qubits" | "qucell" | "vec2" => true
      case _ => false
    }
    case Number(_) => true
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
  private final case class NTuple(items: Any*) {
    /** Converts this n-tuple into a 2-tuple. */
    def toTuple2[A, B]: (A, B) = (items(0).asInstanceOf[A], items(1).asInstanceOf[B])
  }

}
