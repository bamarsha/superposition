package superposition.language

/** A gate application.
  *
  * @param gate the gate
  * @param transformers the transformers
  */
private final case class Application(gate: Expression, transformers: Seq[Transformer])
