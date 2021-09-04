package pkg;

sealed interface TestSealedInterfaces {

}

sealed class PermittedSubClassC implements TestSealedInterfaces {

}

non-sealed class PermittedSubClassD extends PermittedSubClassC {

}

non-sealed class PermittedSubClassE implements TestSealedInterfaces {

}

sealed interface PermittedSubInterfaceA extends TestSealedInterfaces {

}

non-sealed interface PermittedSubInterfaceB extends PermittedSubInterfaceA {

}