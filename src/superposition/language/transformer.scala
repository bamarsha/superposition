package superposition.language

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
