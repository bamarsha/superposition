package superposition.quantum

import superposition.math.DependentKey

final class StateId[A] extends DependentKey {
  type Value = A
}
