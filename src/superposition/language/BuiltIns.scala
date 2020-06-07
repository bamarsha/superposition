package superposition.language

import cats.Monad
import cats.syntax.applicative.catsSyntaxApplicativeId
import cats.syntax.flatMap.toFlatMapOps
import cats.syntax.functor.toFunctorOps
import com.badlogic.ashley.core.Component
import com.badlogic.gdx.maps.tiled.TiledMap
import superposition.component.{FourierBit, LockCode, Multiverse, PrimaryBit, QuantumPosition}
import superposition.math.QExpr.QExpr
import superposition.math.{BitSeq, Gate, StateId, Vector2}

/** The built-in functions.
  *
  * @param multiverse the multiverse
  * @param map the tile map
  */
private final class BuiltIns(multiverse: Multiverse, map: TiledMap) {
  /** The height of the tile map. */
  private val height: Int = Option(map.getProperties.get("height", classOf[Int])).get

  /** Returns true in index `i` if all cells have at least one |1⟩ activator in index `i`. */
  val activated: QExpr[Iterable[Vector2[Int]] => BitSeq] = QExpr.prepare(multiverse.allActivated(true))

  /** Returns true in index `i` if all cells have at least one |1⟩ activator in index `i`. */
  val activatedNF: QExpr[Iterable[Vector2[Int]] => BitSeq] = QExpr.prepare(multiverse.allActivated(false))

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

  private def component[A <: Component](c: Class[A])(id: Int): A =
    multiverse.entityById(id).getOrElse(
      throw new RuntimeException("Entity with id " + id + " does not exist")
      ).getComponent(c)

  val Fourier: Gate[Vector2[Int]] = Gate { cell =>
    val flipFourier = Gate.X.multi.controlledMap(fourierAt)(cell)
    val qftNonFourier = Gate.QFT.multi.onQExpr(multiverse.allInCell(cell).map {
      _
        .filter(!FourierBit.mapper.has(_))
        .filter(PrimaryBit.mapper.has)
        .map(PrimaryBit.mapper.get(_).bits)
    })
    flipFourier * qftNonFourier
  }

  /** Gets all the fourier bits in the given cell. */
  val fourierAt: QExpr[Vector2[Int] => Iterable[StateId[Boolean]]] = QExpr.prepare(multiverse.fourierBits)

  /** Filters the sequence to include only the given indices. */
  def indices[A]: QExpr[((Seq[A], Seq[Int])) => Seq[A]] =
    Monad[QExpr].pure { case (items, indices) => indices map (items(_)) }

  /** Converts a bit sequence to an integer. */
  val int: QExpr[BitSeq => Int] = ((_: BitSeq).toInt).pure[QExpr]

  /** Returns true if any bit is true. */
  val or: QExpr[Iterable[Boolean] => Boolean] = ((_: Iterable[Boolean]) exists identity).pure[QExpr]

  /** Gets all the primary bits in the given cell. */
  val primaryAt: QExpr[Vector2[Int] => Iterable[Seq[StateId[Boolean]]]] = QExpr.prepare(multiverse.primaryBits)

  /** Returns the primary qubits for the entity with the ID. */
  val qubits: QExpr[Int => Seq[StateId[Boolean]]] =
    (component(classOf[PrimaryBit]) andThen (_.bits)).pure[QExpr]

  /** Returns the first primary qubit for the entity with the ID. */
  val qubit: QExpr[Int => StateId[Boolean]] = qubits map (_ andThen (_.head))

  /** Returns the cell qudit for the entity with the ID. */
  val qucell: QExpr[Int => StateId[Vector2[Int]]] =
    (component(classOf[QuantumPosition]) andThen (_.cell)).pure[QExpr]

  /** Returns whether all the given locks are open. */
  val unlocked: QExpr[Seq[Int] => Boolean] = for {
    lockOpen <- QExpr.prepare(component(classOf[LockCode])(_: Int).isOpen)
  } yield (_: Seq[Int]).forall(lockOpen)

  /** Returns the value of the qudit. */
  def value[A]: QExpr[StateId[A] => A] = QExpr.prepare(_.value)

  /** Converts a 2-tuple into a 2-vector. */
  val vec2: QExpr[((Int, Int)) => Vector2[Int]] = (Vector2(_: Int, _: Int)).tupled.pure[QExpr]
}
