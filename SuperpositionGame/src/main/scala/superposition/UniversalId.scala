package superposition

/**
 * Represents the ID of a universe object.
 *
 * A universal ID is unique within a universe but not within the multiverse; copies of an entity in other universes will
 * have the same ID.
 *
 * @param value the ID
 */
private final case class UniversalId(value: Int) extends AnyVal
