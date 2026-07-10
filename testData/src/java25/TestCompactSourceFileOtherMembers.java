String GREETING = "Hello";

String createGreeting(String name){
  return GREETING + " " + name + "!";
}

void main() {
  IO.println(this.hashCode());
  String name = IO.readln("Name: ");
  IO.println(createGreeting(name));
}