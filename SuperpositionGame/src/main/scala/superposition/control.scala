package superposition

/**
 * A condition that a universe must satisfy for a quantum gate to be applied.
 */
private sealed trait Control

/**
 * Controls a gate based on a bit in an object's bit map.
 *
 * @param id    the object's ID
 * @param entry the entry that must be contained in the bit map
 */
private final case class BitControl(id: ObjectId, entry: (String, Boolean)) extends Control

/**
 * Controls a gate based on an object's position.
 *
 * @param id   the object's ID
 * @param cell the necessary position of the object
 */
private final case class PositionControl(id: ObjectId, cell: Cell) extends Control
