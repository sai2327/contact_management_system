# JAVA2 - Advanced OOPs Concepts

## Table of Contents
1. [Inheritance](#1-inheritance)
2. [Polymorphism](#2-polymorphism)
3. [Encapsulation](#3-encapsulation)
4. [Abstraction](#4-abstraction)
5. [Interfaces](#5-interfaces)
6. [Abstract Classes](#6-abstract-classes)
7. [Access Modifiers](#7-access-modifiers)
8. [Static Keyword](#8-static-keyword)
9. [Final Keyword](#9-final-keyword)
10. [super Keyword](#10-super-keyword)

---

## 1. Inheritance

### What is Inheritance?
Inheritance is a mechanism where a new class (child/subclass) acquires properties and behaviors of an existing class (parent/superclass).

### Syntax
```java
class Parent {
    // parent members
}

class Child extends Parent {
    // child inherits parent members + own members
}
```

### Types of Inheritance

#### 1. Single Inheritance
```java
// One parent, one child
class Animal {
    void eat() {
        System.out.println("Eating...");
    }
}

class Dog extends Animal {
    void bark() {
        System.out.println("Barking...");
    }
}

public class SingleInheritance {
    public static void main(String[] args) {
        Dog d = new Dog();
        d.eat();   // inherited from Animal
        d.bark();  // own method
    }
}
```

#### 2. Multilevel Inheritance
```java
// Grandparent -> Parent -> Child
class Animal {
    void eat() {
        System.out.println("Eating...");
    }
}

class Mammal extends Animal {
    void walk() {
        System.out.println("Walking...");
    }
}

class Dog extends Mammal {
    void bark() {
        System.out.println("Barking...");
    }
}

public class MultilevelInheritance {
    public static void main(String[] args) {
        Dog d = new Dog();
        d.eat();   // from Animal
        d.walk();  // from Mammal
        d.bark();  // from Dog
    }
}
```

#### 3. Hierarchical Inheritance
```java
// One parent, multiple children
class Animal {
    void eat() {
        System.out.println("Eating...");
    }
}

class Dog extends Animal {
    void bark() {
        System.out.println("Barking...");
    }
}

class Cat extends Animal {
    void meow() {
        System.out.println("Meowing...");
    }
}

public class HierarchicalInheritance {
    public static void main(String[] args) {
        Dog d = new Dog();
        d.eat();
        d.bark();
        
        Cat c = new Cat();
        c.eat();
        c.meow();
    }
}
```

#### 4. Multiple Inheritance (Using Interfaces)
```java
// Java doesn't support multiple inheritance through classes
// But supports through interfaces

interface Father {
    void property1();
}

interface Mother {
    void property2();
}

class Child implements Father, Mother {
    public void property1() {
        System.out.println("Father's property");
    }
    
    public void property2() {
        System.out.println("Mother's property");
    }
}

public class MultipleInheritance {
    public static void main(String[] args) {
        Child c = new Child();
        c.property1();
        c.property2();
    }
}
```

### IS-A Relationship
```java
class Vehicle {
    int wheels;
}

class Car extends Vehicle {
    String brand;
}

public class ISA {
    public static void main(String[] args) {
        Car c = new Car();
        // Car IS-A Vehicle (inheritance relationship)
        // Car has wheels (inherited) + brand (own)
        c.wheels = 4;
        c.brand = "Toyota";
    }
}
```

### HAS-A Relationship (Composition)
```java
class Engine {
    String type = "Petrol";
}

class Car {
    Engine e = new Engine();  // Car HAS-A Engine
    String brand = "Toyota";
}

public class HASA {
    public static void main(String[] args) {
        Car c = new Car();
        System.out.println("Brand: " + c.brand);
        System.out.println("Engine: " + c.e.type);
    }
}
```

### Viva Questions

**Q1: What is inheritance?**
A: Mechanism where a class acquires properties and behaviors of another class.

**Q2: What are the types of inheritance?**
A: Single, Multilevel, Hierarchical. Java doesn't support Multiple and Hybrid through classes (diamond problem).

**Q3: Why doesn't Java support multiple inheritance?**
A: To avoid diamond problem (ambiguity when two parents have same method).

**Q4: What is the keyword used for inheritance?**
A: extends (for class to class), implements (for interface to class)

**Q5: Can we inherit private members?**
A: No, private members are not inherited.

**Q6: What is IS-A relationship?**
A: Inheritance relationship (Dog IS-A Animal).

**Q7: What is HAS-A relationship?**
A: Composition relationship (Car HAS-A Engine).

**Q8: Can a class extend multiple classes?**
A: No, a class can extend only one class in Java.

**Q9: Does constructor get inherited?**
A: No, constructors are not inherited.

**Q10: What is the root class of all classes in Java?**
A: Object class (java.lang.Object)

---

## 2. Polymorphism

### What is Polymorphism?
Polymorphism means "many forms". One entity can behave differently in different situations.

### Types of Polymorphism

```
Polymorphism
├── Compile-time (Static Binding)
│   └── Method Overloading
└── Runtime (Dynamic Binding)
    └── Method Overriding
```

### A. Method Overloading (Covered in Java1.md)

### B. Method Overriding

```java
class Parent {
    void show() {
        System.out.println("Parent's show()");
    }
    
    void display() {
        System.out.println("Parent's display()");
    }
}

class Child extends Parent {
    // Method overriding
    @Override
    void show() {
        System.out.println("Child's show()");
    }
    
    // Not overriding display()
}

public class MethodOverriding {
    public static void main(String[] args) {
        Child c = new Child();
        c.show();      // Child's show() (overridden)
        c.display();   // Parent's display() (inherited)
        
        // Runtime polymorphism
        Parent p = new Child();  // parent reference, child object
        p.show();      // Child's show() (decided at runtime)
    }
}
```

### Rules for Method Overriding

```java
class Parent {
    // Original method
    protected Object display(int a) throws IOException {
        System.out.println("Parent");
        return null;
    }
}

class Child extends Parent {
    // Overriding rules:
    // 1. Same method signature (name, parameters)
    // 2. Same or covariant return type
    // 3. Cannot have more restrictive access modifier
    // 4. Cannot throw broader checked exception
    
    @Override
    public String display(int a) {  // public >= protected (OK)
        System.out.println("Child");
        return "Child";  // String is subtype of Object (covariant)
    }
}
```

### Dynamic Method Dispatch (Runtime Polymorphism)

```java
class Shape {
    void draw() {
        System.out.println("Drawing Shape");
    }
}

class Circle extends Shape {
    @Override
    void draw() {
        System.out.println("Drawing Circle");
    }
}

class Rectangle extends Shape {
    @Override
    void draw() {
        System.out.println("Drawing Rectangle");
    }
}

public class DynamicDispatch {
    public static void main(String[] args) {
        Shape s;  // parent reference
        
        s = new Circle();
        s.draw();  // Drawing Circle (runtime decision)
        
        s = new Rectangle();
        s.draw();  // Drawing Rectangle (runtime decision)
        
        // Array of parent type
        Shape[] shapes = {new Circle(), new Rectangle(), new Shape()};
        for (Shape shape : shapes) {
            shape.draw();  // polymorphic behavior
        }
    }
}
```

### Overloading vs Overriding

| Feature | Overloading | Overriding |
|---------|-------------|------------|
| Definition | Same name, different parameters | Same signature in parent and child |
| Class | Same class | Different classes (inheritance) |
| Binding | Compile-time (static) | Runtime (dynamic) |
| Return type | Can be different | Must be same or covariant |
| Access modifier | Can be different | Cannot be more restrictive |
| Example | add(int, int), add(double, double) | Parent's show(), Child's show() |

### Viva Questions

**Q1: What is polymorphism?**
A: Ability of an entity to take multiple forms. One method behaving differently based on object.

**Q2: What are the types of polymorphism?**
A: Compile-time (method overloading) and Runtime (method overriding).

**Q3: What is method overriding?**
A: When child class provides specific implementation of method already defined in parent class.

**Q4: What is the difference between overloading and overriding?**
A: Overloading is same name, different parameters in same class. Overriding is same signature in parent and child.

**Q5: Can we override static methods?**
A: No, static methods belong to class and cannot be overridden (method hiding occurs).

**Q6: Can we override private methods?**
A: No, private methods are not visible to child class.

**Q7: Can we override final methods?**
A: No, final methods cannot be overridden.

**Q8: What is dynamic method dispatch?**
A: Runtime polymorphism where method call is resolved at runtime based on object type.

**Q9: What is covariant return type?**
A: Overriding method can return subtype of the return type declared in parent method.

**Q10: Why do we use @Override annotation?**
A: To tell compiler that we're overriding. Helps catch errors if signature doesn't match.

---

## 3. Encapsulation

### What is Encapsulation?
Encapsulation is wrapping of data (variables) and code (methods) together as a single unit and restricting access to some components.

### Implementing Encapsulation

```java
class Student {
    // Private variables (data hiding)
    private int id;
    private String name;
    private int age;
    
    // Public getter methods
    public int getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public int getAge() {
        return age;
    }
    
    // Public setter methods (with validation)
    public void setId(int id) {
        if (id > 0) {
            this.id = id;
        } else {
            System.out.println("Invalid ID");
        }
    }
    
    public void setName(String name) {
        if (name != null && !name.isEmpty()) {
            this.name = name;
        } else {
            System.out.println("Invalid name");
        }
    }
    
    public void setAge(int age) {
        if (age > 0 && age < 150) {
            this.age = age;
        } else {
            System.out.println("Invalid age");
        }
    }
}

public class Encapsulation {
    public static void main(String[] args) {
        Student s = new Student();
        
        // Cannot access directly: s.id = 1; (compilation error)
        
        // Must use setter methods
        s.setId(101);
        s.setName("Sai");
        s.setAge(20);
        
        // Access through getter methods
        System.out.println("ID: " + s.getId());
        System.out.println("Name: " + s.getName());
        System.out.println("Age: " + s.getAge());
        
        // Validation in action
        s.setAge(-5);  // Invalid age
        s.setId(-10);  // Invalid ID
    }
}
```

### Read-Only Class

```java
class ImmutableStudent {
    private final int id;
    private final String name;
    
    // Constructor to initialize
    public ImmutableStudent(int id, String name) {
        this.id = id;
        this.name = name;
    }
    
    // Only getters, no setters (read-only)
    public int getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
}

public class ReadOnly {
    public static void main(String[] args) {
        ImmutableStudent s = new ImmutableStudent(1, "Sai");
        System.out.println("ID: " + s.getId());
        System.out.println("Name: " + s.getName());
        // Cannot modify: no setters available
    }
}
```

### Write-Only Class

```java
class WriteOnlyStudent {
    private int id;
    private String name;
    
    // Only setters, no getters (write-only)
    public void setId(int id) {
        this.id = id;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    void displayInternal() {
        System.out.println("ID: " + id + ", Name: " + name);
    }
}

public class WriteOnly {
    public static void main(String[] args) {
        WriteOnlyStudent s = new WriteOnlyStudent();
        s.setId(1);
        s.setName("Sai");
        // Cannot read: no getters available
        s.displayInternal();
    }
}
```

### Advantages of Encapsulation

1. **Data Hiding**: Private data cannot be accessed directly
2. **Flexibility**: Change implementation without affecting outside code
3. **Validation**: Control setter logic for data validation
4. **Read-Only/Write-Only**: Can make class read-only or write-only
5. **Easy to test**: Encapsulated code is easier to unit test

### Viva Questions

**Q1: What is encapsulation?**
A: Wrapping data and code together and restricting direct access to some components.

**Q2: How to achieve encapsulation?**
A: Make variables private and provide public getter/setter methods.

**Q3: What is data hiding?**
A: Making data members private to prevent direct access from outside.

**Q4: What are getter and setter methods?**
A: Getter returns value, setter sets/modifies value with validation.

**Q5: What is the difference between encapsulation and abstraction?**
A: Encapsulation is data hiding (how), abstraction is showing only essential features (what).

**Q6: Can we have encapsulation without inheritance?**
A: Yes, encapsulation is independent of inheritance.

**Q7: What is the advantage of encapsulation?**
A: Data hiding, flexibility, validation, maintainability, security.

**Q8: How to make a class read-only?**
A: Provide only getters (no setters) and make variables final.

**Q9: Is encapsulation same as data hiding?**
A: No, data hiding is part of encapsulation. Encapsulation includes controlled access.

**Q10: What is tightly encapsulated class?**
A: A class where all variables are private.

---

## 4. Abstraction

### What is Abstraction?
Abstraction is showing only essential features and hiding implementation details.

### Ways to Achieve Abstraction
1. Abstract Classes (0-100% abstraction)
2. Interfaces (100% abstraction)

### Abstract Class

```java
abstract class Shape {
    String color;
    
    // Abstract method (no body)
    abstract double area();
    abstract void draw();
    
    // Concrete method (has body)
    void setColor(String color) {
        this.color = color;
    }
    
    void displayColor() {
        System.out.println("Color: " + color);
    }
}

class Circle extends Shape {
    double radius;
    
    Circle(double radius) {
        this.radius = radius;
    }
    
    // Must implement abstract methods
    @Override
    double area() {
        return 3.14 * radius * radius;
    }
    
    @Override
    void draw() {
        System.out.println("Drawing Circle");
    }
}

class Rectangle extends Shape {
    double length, width;
    
    Rectangle(double length, double width) {
        this.length = length;
        this.width = width;
    }
    
    @Override
    double area() {
        return length * width;
    }
    
    @Override
    void draw() {
        System.out.println("Drawing Rectangle");
    }
}

public class AbstractionEx {
    public static void main(String[] args) {
        // Cannot create object of abstract class
        // Shape s = new Shape();  // Error
        
        Shape s1 = new Circle(5);
        s1.setColor("Red");
        s1.displayColor();
        s1.draw();
        System.out.println("Area: " + s1.area());
        
        System.out.println();
        
        Shape s2 = new Rectangle(4, 6);
        s2.setColor("Blue");
        s2.displayColor();
        s2.draw();
        System.out.println("Area: " + s2.area());
    }
}
```

### Real-World Example

```java
abstract class Bank {
    abstract int getRateOfInterest();
    
    void displayBankInfo() {
        System.out.println("This is a bank");
    }
}

class SBI extends Bank {
    int getRateOfInterest() {
        return 7;
    }
}

class HDFC extends Bank {
    int getRateOfInterest() {
        return 8;
    }
}

class ICICI extends Bank {
    int getRateOfInterest() {
        return 9;
    }
}

public class BankExample {
    public static void main(String[] args) {
        Bank b;
        
        b = new SBI();
        System.out.println("SBI Rate: " + b.getRateOfInterest() + "%");
        
        b = new HDFC();
        System.out.println("HDFC Rate: " + b.getRateOfInterest() + "%");
        
        b = new ICICI();
        System.out.println("ICICI Rate: " + b.getRateOfInterest() + "%");
    }
}
```

### Viva Questions

**Q1: What is abstraction?**
A: Hiding implementation details and showing only essential features.

**Q2: How to achieve abstraction?**
A: Using abstract classes and interfaces.

**Q3: What is an abstract class?**
A: Class declared with abstract keyword that cannot be instantiated and may have abstract methods.

**Q4: Can abstract class have constructor?**
A: Yes, abstract classes can have constructors.

**Q5: Can abstract class have concrete methods?**
A: Yes, abstract classes can have both abstract and concrete methods.

**Q6: What is abstract method?**
A: Method declared without body, must be implemented by subclass.

**Q7: Can we create object of abstract class?**
A: No, abstract classes cannot be instantiated.

**Q8: What is the difference between abstraction and encapsulation?**
A: Abstraction hides complexity (what to do), encapsulation hides data (how to do).

**Q9: Can abstract class have static methods?**
A: Yes, abstract classes can have static methods.

**Q10: Is it mandatory to have abstract methods in abstract class?**
A: No, abstract class can have zero or more abstract methods.

---

## 5. Interfaces

### What is an Interface?
Interface is a blueprint of a class containing only abstract methods (before Java 8) and constants.

### Basic Interface

```java
interface Animal {
    // All methods are public and abstract by default
    void eat();
    void sleep();
    
    // All variables are public static final by default
    int LEGS = 4;
}

class Dog implements Animal {
    // Must implement all interface methods
    public void eat() {
        System.out.println("Dog is eating");
    }
    
    public void sleep() {
        System.out.println("Dog is sleeping");
    }
}

public class InterfaceEx {
    public static void main(String[] args) {
        Dog d = new Dog();
        d.eat();
        d.sleep();
        System.out.println("Legs: " + Animal.LEGS);
        
        // Interface reference
        Animal a = new Dog();
        a.eat();
        a.sleep();
    }
}
```

### Multiple Inheritance through Interfaces

```java
interface Printable {
    void print();
}

interface Showable {
    void show();
}

class Document implements Printable, Showable {
    public void print() {
        System.out.println("Printing document");
    }
    
    public void show() {
        System.out.println("Showing document");
    }
}

public class MultipleInheritanceInterface {
    public static void main(String[] args) {
        Document doc = new Document();
        doc.print();
        doc.show();
    }
}
```

### Interface Inheritance

```java
interface A {
    void methodA();
}

interface B extends A {
    void methodB();
}

class C implements B {
    public void methodA() {
        System.out.println("Method A");
    }
    
    public void methodB() {
        System.out.println("Method B");
    }
}

public class InterfaceInheritance {
    public static void main(String[] args) {
        C obj = new C();
        obj.methodA();
        obj.methodB();
    }
}
```

### Java 8 Interface Features

```java
interface MyInterface {
    // Abstract method
    void abstractMethod();
    
    // Default method (has body)
    default void defaultMethod() {
        System.out.println("Default method");
    }
    
    // Static method
    static void staticMethod() {
        System.out.println("Static method");
    }
}

class MyClass implements MyInterface {
    public void abstractMethod() {
        System.out.println("Abstract method implementation");
    }
    
    // Can override default method (optional)
    @Override
    public void defaultMethod() {
        System.out.println("Overridden default method");
    }
}

public class Java8Interface {
    public static void main(String[] args) {
        MyClass obj = new MyClass();
        obj.abstractMethod();
        obj.defaultMethod();
        
        // Static method called using interface name
        MyInterface.staticMethod();
    }
}
```

### Functional Interface (Java 8)

```java
// Interface with single abstract method
@FunctionalInterface
interface Calculator {
    int calculate(int a, int b);
    
    // Can have default and static methods
    default void display() {
        System.out.println("Calculator");
    }
}

public class FunctionalInterfaceEx {
    public static void main(String[] args) {
        // Lambda expression
        Calculator add = (a, b) -> a + b;
        Calculator multiply = (a, b) -> a * b;
        
        System.out.println("Addition: " + add.calculate(10, 20));
        System.out.println("Multiplication: " + multiply.calculate(10, 20));
    }
}
```

### Marker Interface

```java
// Interface with no methods (marker/tag interface)
interface Serializable {
    // No methods - just marks the class
}

class Student implements Serializable {
    int id;
    String name;
}

public class MarkerInterface {
    public static void main(String[] args) {
        Student s = new Student();
        // Serializable tells JVM this object can be serialized
    }
}
```

### Viva Questions

**Q1: What is an interface?**
A: Blueprint of a class containing abstract methods and constants.

**Q2: Why do we use interfaces?**
A: To achieve abstraction, multiple inheritance, and loose coupling.

**Q3: Can we create object of interface?**
A: No, interfaces cannot be instantiated.

**Q4: What is the default access modifier for interface methods?**
A: public abstract (for methods before Java 8)

**Q5: Can interface have variables?**
A: Yes, but they are public static final (constants) by default.

**Q6: What is the difference between abstract class and interface?**
A: Abstract class can have both abstract and concrete methods. Interface can have only abstract methods (before Java 8).

**Q7: Can a class implement multiple interfaces?**
A: Yes, a class can implement multiple interfaces.

**Q8: Can an interface extend another interface?**
A: Yes, interfaces can extend other interfaces.

**Q9: What is marker interface?**
A: Interface with no methods, used to mark a class (e.g., Serializable, Cloneable).

**Q10: What is functional interface?**
A: Interface with exactly one abstract method, used with lambda expressions.

**Q11: Can interface have constructor?**
A: No, interfaces cannot have constructors.

**Q12: What is default method in interface?**
A: Method with body in interface (Java 8+), provides default implementation.

---

## 6. Abstract Classes

### Detailed Abstract Class Examples

```java
abstract class Employee {
    String name;
    int id;
    
    // Constructor
    Employee(String name, int id) {
        this.name = name;
        this.id = id;
    }
    
    // Abstract methods
    abstract double calculateSalary();
    abstract void displayDetails();
    
    // Concrete method
    void commonMethod() {
        System.out.println("Common method for all employees");
    }
}

class FullTimeEmployee extends Employee {
    double monthlySalary;
    
    FullTimeEmployee(String name, int id, double monthlySalary) {
        super(name, id);
        this.monthlySalary = monthlySalary;
    }
    
    @Override
    double calculateSalary() {
        return monthlySalary  * 12;
    }
    
    @Override
    void displayDetails() {
        System.out.println("Full Time Employee");
        System.out.println("Name: " + name);
        System.out.println("ID: " + id);
        System.out.println("Annual Salary: " + calculateSalary());
    }
}

class PartTimeEmployee extends Employee {
    double hourlyRate;
    int hoursWorked;
    
    PartTimeEmployee(String name, int id, double hourlyRate, int hoursWorked) {
        super(name, id);
        this.hourlyRate = hourlyRate;
        this.hoursWorked = hoursWorked;
    }
    
    @Override
    double calculateSalary() {
        return hourlyRate * hoursWorked;
    }
    
    @Override
    void displayDetails() {
        System.out.println("Part Time Employee");
        System.out.println("Name: " + name);
        System.out.println("ID: " + id);
        System.out.println("Salary: " + calculateSalary());
    }
}

public class AbstractClassEx {
    public static void main(String[] args) {
        Employee e1 = new FullTimeEmployee("Sai", 101, 50000);
        e1.displayDetails();
        e1.commonMethod();
        
        System.out.println();
        
        Employee e2 = new PartTimeEmployee("Teja", 102, 500, 160);
        e2.displayDetails();
        e2.commonMethod();
    }
}
```

### Abstract Class vs Interface

| Feature | Abstract Class | Interface |
|---------|----------------|-----------|
| Methods | Both abstract and concrete | Only abstract (before Java 8) |
| Variables | Can have any type | Only public static final |
| Constructor | Can have | Cannot have |
| Multiple Inheritance | Single inheritance | Multiple inheritance |
| Access Modifiers | All modifiers | public only (methods) |
| When to use | When classes have common behavior | When unrelated classes share behavior |

### When to Use What?

```java
// Use Abstract Class: When classes have common behavior
abstract class Vehicle {
    abstract void start();
    
    void commonService() {  // Common for all vehicles
        System.out.println("Servicing vehicle");
    }
}

// Use Interface: When unrelated classes share behavior
interface Flyable {
    void fly();
}

class Airplane extends Vehicle implements Flyable {
    void start() {
        System.out.println("Airplane starting");
    }
    
    public void fly() {
        System.out.println("Airplane flying");
    }
}

class Bird implements Flyable {  // Bird is not a Vehicle
    public void fly() {
        System.out.println("Bird flying");
    }
}
```

### Viva Questions

**Q1: What is the difference between abstract class and interface?**
A: Abstract class can have both abstract and concrete methods, constructor, any type of variables. Interface can have only abstract methods (before Java 8), no constructor, only constants.

**Q2: When to use abstract class vs interface?**
A: Abstract class when classes have common behavior. Interface when unrelated classes share behavior.

**Q3: Can abstract class implement interface?**
A: Yes, abstract class can implement interface without implementing its methods.

**Q4: Can interface extend abstract class?**
A: No, interface cannot extend class (only other interfaces).

**Q5: Can we have abstract class without abstract methods?**
A: Yes, but no point unless preventing instantiation.

---

## 7. Access Modifiers

### Types of Access Modifiers

```
Access Modifiers
├── private (same class only)
├── default (same package)
├── protected (same package + subclass)
└── public (everywhere)
```

### Access Modifier Examples

```java
class AccessModifiers {
    // Private: accessible only within same class
    private int privateVar = 10;
    
    // Default: accessible within same package
    int defaultVar = 20;
    
    // Protected: accessible within same package and subclasses
    protected int protectedVar = 30;
    
    // Public: accessible everywhere
    public int publicVar = 40;
    
    private void privateMethod() {
        System.out.println("Private method");
    }
    
    void defaultMethod() {
        System.out.println("Default method");
    }
    
    protected void protectedMethod() {
        System.out.println("Protected method");
    }
    
    public void publicMethod() {
        System.out.println("Public method");
    }
    
    void testAccess() {
        // All accessible within same class
        System.out.println(privateVar);
        System.out.println(defaultVar);
        System.out.println(protectedVar);
        System.out.println(publicVar);
        
        privateMethod();
        defaultMethod();
        protectedMethod();
        publicMethod();
    }
}

public class AccessModifierTest {
    public static void main(String[] args) {
        AccessModifiers obj = new AccessModifiers();
        
        // System.out.println(obj.privateVar);  // Error
        System.out.println(obj.defaultVar);     // OK (same package)
        System.out.println(obj.protectedVar);   // OK (same package)
        System.out.println(obj.publicVar);      // OK
        
        // obj.privateMethod();  // Error
        obj.defaultMethod();     // OK
        obj.protectedMethod();   // OK
        obj.publicMethod();      // OK
    }
}
```

### Access Modifier Table

| Modifier | Same Class | Same Package | Subclass (different package) | Everywhere |
|----------|-----------|--------------|-----------------------------|-----------| 
| private | ✓ | ✗ | ✗ | ✗ |
| default | ✓ | ✓ | ✗ | ✗ |
| protected | ✓ | ✓ | ✓ | ✗ |
| public | ✓ | ✓ | ✓ | ✓ |

### Viva Questions

**Q1: What are access modifiers?**
A: Keywords that set accessibility of classes, methods, variables.

**Q2: What are the types of access modifiers?**
A: private, default, protected, public

**Q3: What is the most restrictive access modifier?**
A: private

**Q4: What is the difference between protected and default?**
A: Protected is accessible in subclass (different package), default is not.

**Q5: Can we apply access modifiers to local variables?**
A: No, access modifiers cannot be applied to local variables.

---

## 8. Static Keyword

### Static Variable

```java
class Student {
    int id;           // instance variable
    String name;      // instance variable
    static String college = "ABC College";  // static variable
    static int count = 0;  // static counter
    
    Student(int id, String name) {
        this.id = id;
        this.name = name;
        count++;  // increment for each object
    }
    
    void display() {
        System.out.println(id + " " + name + " " + college);
    }
}

public class StaticVariable {
    public static void main(String[] args) {
        Student s1 = new Student(1, "Sai");
        Student s2 = new Student(2, "Teja");
        Student s3 = new Student(3, "Ram");
        
        s1.display();
        s2.display();
        s3.display();
        
        System.out.println("Total students: " + Student.count);
        
        // Changing static variable
        Student.college = "XYZ College";
        s1.display();  // Shows XYZ College
        s2.display();  // Shows XYZ College
    }
}
```

### Static Method

```java
class MathUtils {
    // Static method
    static int add(int a, int b) {
        return a + b;
    }
    
    static int multiply(int a, int b) {
        return a * b;
    }
    
    // Static method cannot access instance members directly
    int instanceVar = 10;
    
    static void staticMethod() {
        // System.out.println(instanceVar);  // Error
        // this.instanceVar;  // Error: cannot use 'this'
        System.out.println("Static method");
    }
}

public class StaticMethod {
    public static void main(String[] args) {
        // Called using class name (no object needed)
        System.out.println("Sum: " + MathUtils.add(10, 20));
        System.out.println("Product: " + MathUtils.multiply(10, 20));
        MathUtils.staticMethod();
    }
}
```

### Static Block

```java
class StaticBlockExample {
    static int a;
    static int b;
    
    // Static block - executed when class is loaded
    static {
        System.out.println("Static block executed");
        a = 10;
        b = 20;
    }
    
    static void display() {
        System.out.println("a = " + a + ", b = " + b);
    }
}

public class StaticBlock {
    public static void main(String[] args) {
        System.out.println("Main method");
        StaticBlockExample.display();
    }
}
// Output:
// Static block executed
// Main method
// a = 10, b = 20
```

### Static Nested Class

```java
class Outer {
    static int outerStatic = 10;
    int outerInstance = 20;
    
    // Static nested class
    static class StaticNested {
        void display() {
            System.out.println("OuterStatic: " + outerStatic);
            // System.out.println(outerInstance);  // Error: cannot access instance
        }
    }
}

public class StaticNestedClass {
    public static void main(String[] args) {
        // Creating object of static nested class
        Outer.StaticNested nested = new Outer.StaticNested();
        nested.display();
    }
}
```

### Viva Questions

**Q1: What is static keyword?**
A: Static members belong to class rather than objects, shared by all instances.

**Q2: Can we override static methods?**
A: No, static methods cannot be overridden (method hiding occurs).

**Q3: Can we use 'this' keyword in static method?**
A: No, 'this' refers to instance, static belongs to class.

**Q4: When is static block executed?**
A: When class is loaded (before main method, before any object creation).

**Q5: Can we have static class in Java?**
A: Only nested classes can be static, not top-level classes.

---

## 9. Final Keyword

### Final Variable

```java
public class FinalVariable {
    final int CONSTANT = 100;  // must be initialized
    
    final int blank;  // blank final variable
    
    // Blank final must be initialized in constructor
    FinalVariable() {
        blank = 50;
    }
    
    void modify() {
        // CONSTANT = 200;  // Error: cannot change final variable
        // blank = 100;     // Error: cannot change
    }
    
    public static void main(String[] args) {
        FinalVariable obj = new FinalVariable();
        System.out.println("Constant: " + obj.CONSTANT);
        System.out.println("Blank: " + obj.blank);
    }
}
```

### Final Method

```java
class Parent {
    // Final method cannot be overridden
    final void display() {
        System.out.println("Parent's final method");
    }
    
    void show() {
        System.out.println("Parent's show method");
    }
}

class Child extends Parent {
    // Cannot override final method
    // void display() { }  // Error
    
    // Can override non-final method
    @Override
    void show() {
        System.out.println("Child's show method");
    }
}

public class FinalMethod {
    public static void main(String[] args) {
        Child c = new Child();
        c.display();
        c.show();
    }
}
```

### Final Class

```java
// Final class cannot be inherited
final class FinalClass {
    void display() {
        System.out.println("Final class method");
    }
}

// Cannot extend final class
// class Child extends FinalClass { }  // Error

public class FinalClassEx {
    public static void main(String[] args) {
        FinalClass obj = new FinalClass();
        obj.display();
    }
}
```

### Final Parameter

```java
public class FinalParameter {
    void display(final int num) {
        System.out.println("Number: " + num);
        // num = 20;  // Error: cannot change final parameter
    }
    
    public static void main(String[] args) {
        FinalParameter obj = new FinalParameter();
        obj.display(10);
    }
}
```

### Viva Questions

**Q1: What is the use of final keyword?**
A: To declare constants (final variable), prevent overriding (final method), prevent inheritance (final class).

**Q2: Can we change the value of final variable?**
A: No, final variables cannot be reassigned.

**Q3: What is blank final variable?**
A: Final variable declared but not initialized, must be initialized in constructor.

**Q4: Can we inherit final class?**
A: No, final classes cannot be extended.

**Q5: Can we override final method?**
A: No, final methods cannot be overridden.

---

## 10. super Keyword

### super for Variables

```java
class Parent {
    int num = 10;
}

class Child extends Parent {
    int num = 20;
    
    void display() {
        System.out.println("Child num: " + num);           // 20
        System.out.println("Parent num: " + super.num);    // 10
    }
}

public class SuperVariable {
    public static void main(String[] args) {
        Child c = new Child();
        c.display();
    }
}
```

### super for Methods

```java
class Parent {
    void display() {
        System.out.println("Parent's display method");
    }
}

class Child extends Parent {
    @Override
    void display() {
        super.display();  // calling parent's method
        System.out.println("Child's display method");
    }
}

public class SuperMethod {
    public static void main(String[] args) {
        Child c = new Child();
        c.display();
        // Output:
        // Parent's display method
        // Child's display method
    }
}
```

### super for Constructor

```java
class Parent {
    Parent() {
        System.out.println("Parent's constructor");
    }
    
    Parent(int num) {
        System.out.println("Parent's parameterized constructor: " + num);
    }
}

class Child extends Parent {
    Child() {
        super();  // calling parent's default constructor
        System.out.println("Child's constructor");
    }
    
    Child(int num) {
        super(num);  // calling parent's parameterized constructor
        System.out.println("Child's parameterized constructor");
    }
}

public class SuperConstructor {
    public static void main(String[] args) {
        System.out.println("Creating Child object:");
        Child c1 = new Child();
        
        System.out.println("\nCreating Child object with parameter:");
        Child c2 = new Child(10);
    }
}
```

### Viva Questions

**Q1: What is super keyword?**
A: super refers to immediate parent class object.

**Q2: What are the uses of super keyword?**
A: To access parent's variable, method, and constructor.

**Q3: What is the difference between this and super?**
A: this refers to current object, super refers to parent class object.

**Q4: Can we use super in static method?**
A: No, super refers to instance, static belongs to class.

**Q5: Is super() mandatory in constructor?**
A: If not written explicitly, compiler automatically adds super() as first statement.

---

## Summary

### OOPs Concepts Covered:
✅ Inheritance (Single, Multilevel, Hierarchical, Multiple through interfaces)
✅ Polymorphism (Overloading, Overriding, Dynamic dispatch)
✅ Encapsulation (Data hiding, getters/setters)
✅ Abstraction (Abstract classes, interfaces)
✅ Access Modifiers (private, default, protected, public)
✅ Static Keyword (variables, methods, blocks, nested class)
✅ Final Keyword (variables, methods, classes)
✅ super Keyword (variables, methods, constructors)

### Key Points:
- Inheritance: Code reusability using 'extends'
- Polymorphism: One entity, many forms
- Encapsulation: Data + methods together, controlled access
- Abstraction: Hide complexity, show only essentials
- Interface: 100% abstraction, multiple inheritance
- Static: Belongs to class, shared by all objects
- Final: Constant, prevent override/inheritance
- super: Access parent class members

**Next**: Study Java3.md for Exception Handling and Packages!

---
**End of Java2.md**
