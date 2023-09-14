package pkg

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@OptIn(ExperimentalContracts::class)
class TestContracts {
  fun testSimpleContract(x: Int?): Int {
    contract {
      returns() implies (x != null)
    }
    if (x == null) error("x is null")
    return x
  }

  fun testBooleanContract(a: Boolean, b: Boolean): Boolean? {
    contract {
      returns(true) implies (!a && !b)
      returns(null) implies (a && b)
      returns(false) implies ((a && !b) || (!a && b))
    }

    return if (a && b) null else a || b
  }

  fun testTypeContract(x: Any?): Int {
    contract {
      returns() implies (x is Int)
    }
    if (x !is Int) error("x is not Int")
    return x
  }

  fun testFunctionalContract(f: () -> Int): Int {
    contract {
      callsInPlace(f, InvocationKind.EXACTLY_ONCE)
    }
    return f()
  }

  fun testFunctionalContract2(f: () -> Int, b: Boolean): Int {
    contract {
      callsInPlace(f, InvocationKind.AT_MOST_ONCE)
    }
    return if (b) f() else 0
  }

  fun testFunctionalContract3(f: () -> Int, i: Int): Int {
    contract {
      callsInPlace(f)
    }
    return (0..i).sumOf { f() }
  }
}
