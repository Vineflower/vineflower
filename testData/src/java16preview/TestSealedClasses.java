package java16preview;

sealed class TestSealedClasses {

}

sealed class PermittedSubClassA extends TestSealedClasses {

}

non-sealed class PermittedSubClassB extends PermittedSubClassA {

}