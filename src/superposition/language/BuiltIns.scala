package superposition.language

import cats.Monad
import cats.syntax.applicative.catsSyntaxApplicativeId
import cats.syntax.flatMap.toFlatMapOps
import cats.syntax.functor.toFunctorOps
import com.badlogic.gdx.maps.tiled.TiledMap
import superposition.component.{Multiverse, PrimaryBit, QuantumPosition}
import superposition.math.QExpr.QExpr
import superposition.math.{BitSeq, StateId, Vector2}

/** The built-in functions.
  *
  * @param multiverse the multiverse
  * @param map the tile map
  */
private final class BuiltIns(multiverse: Multiverse, map: TiledMap) {
  /** The height of the tile map. */
  private val height: Int = Option(map.getProperties.get("height", classOf[Int])).get

  /** Returns true in index `i` if all cells have at least one |1⟩ activator in index `i`. */
  val activated: QExpr[Iterable[Vector2[Int]] => BitSeq] = QExpr.prepare(multiverse.allActivated)

  /** Returns true if all bits are true. */
  val and: QExpr[Iterable[Boolean] => Boolean] = ((_: Iterable[Boolean]) forall identity).pure[QExpr]

  /** Returns the bit at the index in the bit sequence. */
  val bitAt: QExpr[((BitSeq, Int)) => Boolean] = ((_: BitSeq) (_: Int)).tupled.pure[QExpr]

  /** Returns the cell in game coordinates corresponding to the (x, y) position in tile map coordinates. */
  val cell: QExpr[((Int, Int)) => Vector2[Int]] = Monad[QExpr].pure { case (x, y) => Vector2(x, height - y - 1) }

  /** Returns true if the cell has an activator that is on in index 0. */
  val activeCell: QExpr[((Int, Int)) => Boolean] =
    for {
      cellValue <- cell
      activatedValue <- activated
      bitAtValue <- bitAt
    } yield cellValue andThen (Seq(_)) andThen activatedValue andThen (bitAtValue(_, 0))

  /** Filters the sequence to include only the given indices. */
  def indices[A]: QExpr[((Seq[A], Seq[Int])) => Seq[A]] =
    Monad[QExpr].pure { case (items, indices) => indices map (items(_)) }

  /** Converts a bit sequence to an integer. */
  val int: QExpr[BitSeq => Int] = ((_: BitSeq).toInt).pure[QExpr]

  /** Returns true if any bit is true. */
  val or: QExpr[Iterable[Boolean] => Boolean] = ((_: Iterable[Boolean]) exists identity).pure[QExpr]

  /** Returns the primary qubits for the entity with the ID. */
  val qubits: QExpr[Int => Seq[StateId[Boolean]]] =
    (multiverse.entityById(_: Int).get.getComponent(classOf[PrimaryBit]).bits).pure[QExpr]

  /** Returns the first primary qubit for the entity with the ID. */
  val qubit: QExpr[Int => StateId[Boolean]] = qubits map (_ andThen (_.head))

  /** Returns the cell qudit for the entity with the ID. */
  val qucell: QExpr[Int => StateId[Vector2[Int]]] =
    (multiverse.entityById(_: Int).get.getComponent(classOf[QuantumPosition]).cell).pure[QExpr]

  /** Returns the value of the qudit. */
  def value[A]: QExpr[StateId[A] => A] = QExpr.prepare(_.value)

  /** Converts a 2-tuple into a 2-vector. */
  val vec2: QExpr[((Int, Int)) => Vector2[Int]] = (Vector2(_: Int, _: Int)).tupled.pure[QExpr]
}
