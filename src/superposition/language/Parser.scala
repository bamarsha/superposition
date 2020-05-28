package superposition.language

import scala.util.parsing.combinator.RegexParsers

/** Parses gate programs. */
private object Parser extends RegexParsers {
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
  private val keyword: Parser[String] = "Apply" | "on" | "if" | "multi" | "adjoint"

  /** An identifier. */
  private val identifier: Parser[String] = """[^\d\W]\w*""".r - keyword

  /** A number. */
  private val number: Parser[Int] = """-?\d+""".r ^^ (_.toInt)

  /** A literal is an identifier or a number. */
  private val literal: Parser[Expression] = identifier ^^ Identifier | number ^^ Number

  /** A parenthetical expression. */
  private val parenthetical: Parser[Expression] = "(" ~> whiteSpace.? ~> expression <~ whiteSpace.? <~ ")"

  /** A tuple. */
  private val tuple: Parser[Expression] =
    (("(" ~> whiteSpace.? ~> expression <~ whiteSpace.?)
      ~ ("," ~> whiteSpace.? ~> expression <~ whiteSpace.?).+ <~ ")"
      ^^ mkList ^^ Tuple)

  /** A list. */
  private val list: Parser[Expression] =
    (("[" ~> whiteSpace.? ~> expression <~ whiteSpace.?)
      ~ ("," ~> whiteSpace.? ~> expression <~ whiteSpace.?).* <~ "]"
      ^^ mkList ^^ List)

  /** A term is a list, parenthetical expression, tuple or literal. */
  private val term: Parser[Expression] = list | parenthetical | tuple | literal

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

  /** The multi-transformer. */
  private val adjointTransformer: Parser[Transformer] = "adjoint" ^^^ AdjointTransformer

  /** A transformer. */
  private val transformer: Parser[Transformer] = onTransformer | ifTransformer | multiTransformer | adjointTransformer

  /** A gate application. */
  private val application: Parser[Application] =
    (("Apply" ~> whiteSpace).? ~> identifier ~ (whiteSpace ~> transformer).* <~ whiteSpace.? <~ "."
      ^^ makeTuple ^^ Application.tupled)

  /** A gate program. */
  val gateProgram: Parser[Seq[Application]] = whiteSpace.? ~> phrase((application <~ whiteSpace.?).*)

  /** An expression program. */
  val expressionProgram: Parser[Expression] = whiteSpace.? ~> phrase(expression)
}
