package superposition.language

import com.badlogic.gdx.maps.tiled.TiledMap
import superposition.component.Multiverse
import superposition.math.{Gate, Universe}

import scala.util.parsing.combinator.RegexParsers

/** Parses gate programs. */
object Parser extends RegexParsers {
  override val skipWhitespace: Boolean = false

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
  private val identifier: Parser[String] = """[^\d\W]\w*""".r - keyword

  /** A number. */
  private val number: Parser[Int] = """-?\d+""".r ^^ (_.toInt)

  /** A literal is an identifier or a number. */
  private val literal: Parser[Expression] = identifier ^^ Identifier | number  ^^ Number

  /** A parenthetical expression. */
  private val parenthetical: Parser[Expression] = "(" ~> whiteSpace.? ~> expression <~ whiteSpace.? <~ ")"

  /** A tuple. */
  private val tuple: Parser[Expression] =
    (("(" ~> whiteSpace.? ~> expression <~ whiteSpace.?)
      ~ ("," ~> whiteSpace.? ~> expression <~ whiteSpace.?).+ <~ ")"
      ^^ mkList ^^ Tuple)

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
    (("Apply" ~> whiteSpace).? ~> identifier ~ (whiteSpace ~> transformer).* <~ whiteSpace.? <~ "."
      ^^ makeTuple ^^ Application.tupled)

  /** A gate program. */
  private val program: Parser[Seq[Application]] = whiteSpace.? ~> phrase((application <~ whiteSpace.?).*)

  def parseExpression[A](multiverse: Multiverse, map: TiledMap, text: String): ParseResult[Universe => A] =
    parse(expression ^^ new Interpreter(multiverse, map).expr2scala ^^ (_.asInstanceOf[Universe => A]), text)

  def parseProgram(multiverse: Multiverse, map: TiledMap, text: String): ParseResult[Gate[Unit]] =
    parse(program ^^ new Interpreter(multiverse, map).program2scala, text)

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
