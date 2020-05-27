package superposition.math

/** A universe represents a basis state, its corresponding probability amplitude, and additional arbitrary metadata.
  *
  * @param amplitude the probability amplitude of the universe
  * @param state the classical values of each qudit in the universe
  * @param meta arbitrary metadata for the universe
  */
final case class Universe(
    amplitude: Complex = Complex(1),
    state: DependentMap[StateId[_]] = DependentMap.empty,
    meta: MutableDependentMap[MetaId[_]] = MutableDependentMap.empty) {
  /** Increases the probability amplitude by `c`.
    *
    * @param c the number to add to the probability amplitude
    * @return the universe with probability amplitude increased by `c`
    */
  def +(c: Complex): Universe = copy(amplitude = amplitude + c, meta = meta.clone)

  /** Decreases the probability amplitude by `c`.
    *
    * @param c the number to subtract from the probability amplitude
    * @return the universe with probability amplitude decreased by `c`
    */
  def -(c: Complex): Universe = copy(amplitude = amplitude - c, meta = meta.clone)

  /** Multiplies the probability amplitude by `c`.
    *
    * @param c the number to multiply the probability amplitude by
    * @return the universe with probability amplitude multiplied by `c`
    */
  def *(c: Complex): Universe = copy(amplitude = amplitude * c, meta = meta.clone)

  /** Divides the probability amplitude by `c`.
    *
    * @param c the number to divide the probability amplitude by
    * @return the universe with probability amplitude divided by `c`
    */
  def /(c: Complex): Universe = copy(amplitude = amplitude / c, meta = meta.clone)

  /** Adds or replaces the value of a qudit.
    *
    * @param id the qudit to update
    * @param value the new value of the qudit
    * @return the updated universe
    */
  def updatedState(id: StateId[_])(value: id.Value): Universe =
    copy(state = state.updated(id)(value), meta = meta.clone)

  /** Maps the value of a qudit if the qudit exists.
    *
    * @param id the qudit to update
    * @param updater a function that maps the value of the qudit to the new value
    * @return the updated universe
    */
  def updatedStateWith(id: StateId[_])(updater: id.Value => id.Value): Universe =
    copy(state = state.updatedWith(id)(updater), meta = meta.clone)
}

/** Factories for universes. */
object Universe {
  /** The empty universe. */
  val empty: Universe = Universe()
}
