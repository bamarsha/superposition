package superposition.language

/** A gate application.
  *
  * @param gate the gate
  * @param transformers the transformers
  */
private final case class Application(gate: String, transformers: Seq[Transformer])
