package superposition.math

import cats.ContravariantMonoidal
import cats.data.NonEmptyList
import cats.syntax.contravariant.toContravariantOps
import cats.syntax.functor.toFunctorOps
import superposition.math.QExpr.QExpr

import scala.Function.const
import scala.math.sqrt

/** A quantum gate.
  *
  * @tparam A the type of the gate's argument
  */
final class Gate[-A] private(val f: A => Unitary) {
  /** Applies the gate to a value.
    *
    * @param value the value of the argument to the gate
    * @return the unitary produced by the gate
    */
  def apply(value: A): Unitary = f(value)
}

/** Gate operations and type classes. */
object Gate {

  /** Makes a quantum gate.
    *
    * @param f a function that evaluates the expression
    * @tparam A the expression type
    * @return the quantum gate
    */
  private def apply[A](f: A => Unitary): Gate[A] = new Gate(f)

  /** An instance of the contravariant monoidal type class for gates. */
  implicit object GateCM extends ContravariantMonoidal[Gate] {
    override def contramap[A, B](gate: Gate[A])(f: B => A): Gate[B] = Gate(gate.apply compose f)

    override def product[A, B](gate1: Gate[A], gate2: Gate[B]): Gate[(A, B)] = Gate {
      case (a, b) => gate1(a) * gate2(b)
    }

    override val unit: Gate[Unit] = Gate(const(Unitary.identity))
  }

  /** Operations on gates.
    *
    * @param gate the gate to apply the operations to
    */
  implicit final class Ops[A](val gate: Gate[A]) extends AnyVal {

    // ---------- Core ops ----------

    /** Returns the adjoint of this gate. */
    def adjoint: Gate[A] = Gate(gate.apply andThen (_.adjoint))

    /** A new gate that applies this gate to each argument in the sequence in order. */
    def multi: Gate[Seq[A]] = Gate {
      case Nil => Unitary.identity
      case x :: xs => gate(x) * gate.multi(xs)
    }

    /** Returns a new gate that applies this gate on a QExpr.
      *
      * To preserve unitarity, the new gate must not change the value of the QExpr in any universe.
      *
      * @throws AssertionError if the mapping function violates unitarity
      * @return the new gate
      */
    def onQExpr: Gate[QExpr[A]] = Gate(
      value => new Unitary {
        override def apply(universe: Universe): NonEmptyList[Universe] = {
          val newUniverses = gate(value(universe))(universe)
          assert(newUniverses forall (value(_) == value(universe)))
          newUniverses
        }
        override def adjoint: Unitary = gate.adjoint.onQExpr(value)
      })

    // ---------- Helper ops ----------

    /** Returns a new gate that applies this gate and then the other gate.
      *
      * @param other the gate to apply after this gate
      * @return the sequential composition of the two gates
      */
    def andThen(other: Gate[A]): Gate[A] =
      ContravariantMonoidal[Gate].product(gate, other) contramap (value => (value, value))

    /** Returns a new gate that controls the argument to the original gate by mapping it based on its value and the
      * state of the universe.
      *
      * To preserve unitarity, the new gate must not change the state of any qudits used by the mapping function to map
      * the argument.
      *
      * @param f a mapping function that receives the gate argument and the universe
      * @tparam B the type of the new argument
      * @throws AssertionError if the mapping function violates unitarity
      * @return the controlled gate
      */
    def controlledMap[B](f: QExpr[B => A]): Gate[B] = gate.onQExpr.contramap(b => f.map(_(b)))

    /** Returns a new gate that applies the original gate if the universe satisfies the predicate, and otherwise applies
      * the identity gate instead.
      *
      * @param predicate the predicate that must be satisfied to apply the original gate
      * @return the controlled gate
      */
    def controlled(predicate: QExpr[A => Boolean]): Gate[A] = /*_*/
      multi.onQExpr.contramap(a => predicate.map(_(a)).map(if (_) Seq(a) else Seq())) /*_*/

    /** Returns a new gate that maps its argument to a sequence and applies the original gate with each value in the
      * sequence.
      *
      * @param f the mapping function that returns a sequence
      * @tparam B the type of the new argument
      * @return the mapped and flattened gate
      */
    def flatMap[B](f: B => Seq[A]): Gate[B] = multi contramap f

    /** Returns a new gate that applies the original gate if the argument satisfies the predicate, and otherwise applies
      * the identity gate instead.
      *
      * @param predicate the predicate that must be satisfied to apply the original gate
      * @return the filtered gate
      */
    def filter(predicate: A => Boolean): Gate[A] = flatMap(List(_) filter predicate)
  }

  val X: Gate[StateId[Boolean]] = Gate(id => new Unitary {
    override def apply(universe: Universe): NonEmptyList[Universe] =
      NonEmptyList.of(universe.updatedStateWith(id)(!_))
    override def adjoint: Unitary = X(id)
  })

  val H: Gate[StateId[Boolean]] = Gate(id => new Unitary {
    override def apply(universe: Universe): NonEmptyList[Universe] =
      NonEmptyList.of(
        universe / Complex((if (universe.state(id)) -1 else 1) * sqrt(2)),
        universe.updatedStateWith(id)(!_) / Complex(sqrt(2)))
    override def adjoint: Unitary = H(id)
  })

  val Translate: Gate[(StateId[Vector2[Int]], Vector2[Int])] = Gate {
    case (id, delta) => new Unitary {
      override def apply(universe: Universe): NonEmptyList[Universe] =
        NonEmptyList.of(universe.updatedStateWith(id)(_ + delta))
      override def adjoint: Unitary = Translate(id, -delta)
    }
  }

  val Phase: Gate[Complex] = Gate(z => Unitary(u => NonEmptyList.of(u * z), Phase(z.conjugate)))

  val Rz: Gate[(StateId[Boolean], Double)] = Phase
    .contramap[(StateId[Boolean], Double)] { case (_, theta) => Complex.polar(1, theta) }
    .controlled(QExpr.prepare { case (id, _) => id.value })

  val QFT: Gate[Seq[StateId[Boolean]]] = Gate { ids =>
    var u = Unitary.identity
    val len = ids.length
    for (i <- Seq.range(0, len)) {
      u = H(ids(i)) * u
      for (j <- Seq.range(i + 1, len)) {
        u = Rz.controlled(ids(j).value.map(const))(ids(i), 2 * math.Pi / math.pow(2, j-i+1)) * u
      }
    }
    u
  }
}
