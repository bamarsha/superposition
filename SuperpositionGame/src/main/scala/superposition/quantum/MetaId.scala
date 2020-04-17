package superposition.quantum

import superposition.math.DependentKey

final class MetaId[A] extends DependentKey {
  type Value = A
}
