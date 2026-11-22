static String GREETING = "Hello";

static String createGreeting(String name){
  return GREETING + " " + name + "!";
}

static void main() {
  String name = IO.readln("Name: ");
  IO.println(createGreeting(name));
}