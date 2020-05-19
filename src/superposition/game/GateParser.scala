package superposition.game

import scala.util.parsing.combinator.RegexParsers

/** Parses gate programs. */
private object GateParser extends RegexParsers {

  /** An expression. */
  private sealed trait Expression

  /** An identifier.
    *
    * @param name the name of the identifier
    */
  private final case class Identifier(name: String) extends Expression

  /** An integral number.
    *
    * @param value the value of the number
    */
  private final case class Number(value: Int) extends Expression

  /** A tuple.
    *
    * @param items the items in the tuple
    */
  private final case class Tuple(items: Seq[Expression]) extends Expression

  /** A function call.
    *
    * @param function the function
    * @param argument the argument to the function
    */
  private final case class Call(function: Expression, argument: Expression) extends Expression

  /** An equals condition.
    *
    * @param lhs the left-hand side
    * @param rhs the right-hand side
    */
  private final case class Equals(lhs: Expression, rhs: Expression) extends Expression

  /** A gate transformer. */
  private sealed trait Transformer

  /** The on-transformer pre-applies an argument to a gate.
    *
    * @param argument the argument to the gate
    */
  private final case class OnTransformer(argument: Expression) extends Transformer

  /** The if-transformer conditions the application of a gate.
    *
    * @param condition the condition
    */
  private final case class IfTransformer(condition: Expression) extends Transformer

  /** The multi-transformer converts a gate into one that takes a sequence of arguments. */
  private case object MultiTransformer extends Transformer

  /** A gate application.
    *
    * @param gate the gate
    * @param transformers the transformers
    */
  private final case class Application(gate: Expression, transformers: Seq[Transformer])

  override def skipWhitespace: Boolean = false

  /** Makes a tuple from a sequenced parser result.
    *
    * @tparam A the type of the first result
    * @tparam B the type of the second result
    */
  private def makeTuple[A, B]: A ~ B => (A, B) = {
    case x ~ y => (x, y)
  }

  /** The reserved keywords. */
  private val keyword: Parser[String] = "Apply" | "on" | "if" | "multi"

  /** An identifier. */
  private val identifier: Parser[Expression] = """[^\d\W]\w*""".r - keyword ^^ Identifier

  /** A number. */
  private val number: Parser[Expression] = """\d+""".r ^^ (_.toInt) ^^ Number

  /** A literal is an identifier or a number. */
  private val literal: Parser[Expression] = identifier | number

  /** A parenthetical expression. */
  private val parenthetical: Parser[Expression] = "(" ~> whiteSpace.? ~> expression <~ whiteSpace.? <~ ")"

  /** A tuple. */
  private val tuple: Parser[Expression] =
    (("(" ~> whiteSpace.? ~> expression <~ whiteSpace.?)
      ~ ("," ~> whiteSpace.? ~> expression <~ whiteSpace.?).+ <~ ")"
      ^^ mkList
      ^^ Tuple)

  /** A term is a parenthetical expression, tuple or literal. */
  private val term: Parser[Expression] = parenthetical | tuple | literal

  /** A function call. */
  private val call: Parser[Expression] = term ~ (whiteSpace ~> term) ^^ makeTuple ^^ Call.tupled

  /** A function call or term. */
  private val callOrTerm: Parser[Expression] = call | term

  /** An equals condition. */
  private lazy val equals: Parser[Expression] =
    callOrTerm ~ (whiteSpace.? ~> "=" ~> whiteSpace.? ~> expression) ^^ makeTuple ^^ Equals.tupled

  /** An expression. */
  private lazy val expression: Parser[Expression] = equals | callOrTerm

  /** The on-transformer. */
  private val onTransformer: Parser[Transformer] = "on" ~> whiteSpace ~> expression ^^ OnTransformer

  /** The if-transformer. */
  private val ifTransformer: Parser[Transformer] = "if" ~> whiteSpace ~> expression ^^ IfTransformer

  /** The multi-transformer. */
  private val multiTransformer: Parser[Transformer] = "multi" ^^^ MultiTransformer

  /** A transformer. */
  private val transformer: Parser[Transformer] = onTransformer | ifTransformer | multiTransformer

  /** A gate application. */
  private val application: Parser[Application] =
    (("Apply" ~> whiteSpace).? ~> identifier ~ (whiteSpace ~> transformer).* <~ whiteSpace.? <~ "." <~ whiteSpace.?
      ^^ makeTuple
      ^^ Application.tupled)

  /** A gate program. */
  private val program: Parser[Seq[Application]] = whiteSpace.? ~> phrase(application.*)

  /** Runs the gate parser on a couple of examples.
    *
    * @param args the command-line arguments
    */
  def main(args: Array[String]): Unit = {
    println(parse(
      program,
      """Apply H on bit 12.
        |
        |Apply Translate
        |on (cell 12, (6, 0))
        |if value (bit 12).
        |
        |Apply X on bit 12
        |if value (cell 12) = (6, 2).""".stripMargin))
    println(parse(
      program,
      """H on bit 12.
        |
        |Translate
        |on (cell 12, (6, 0))
        |if value (bit 12).
        |
        |X on bit 12
        |if value (cell 12) = (6, 2).""".stripMargin))
  }
}
