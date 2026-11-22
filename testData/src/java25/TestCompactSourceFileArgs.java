void main(String... args) {
  String name;
  if (args.length == 0) {
    name = IO.readln("What is your name? ");
  } else {
    name = args[0];
  }
  IO.println("Hello " + name + "!");
}