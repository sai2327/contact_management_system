# JAVA3 - Exception Handling and Packages

## Table of Contents
1. [Exception Handling](#1-exception-handling)
2. [Types of Exceptions](#2-types-of-exceptions)
3. [Try-Catch-Finally](#3-try-catch-finally)
4. [Throw and Throws](#4-throw-and-throws)
5. [Custom Exceptions](#5-custom-exceptions)
6. [Exception Propagation](#6-exception-propagation)
7. [Exception Handling Best Practices](#7-exception-handling-best-practices)
8. [Packages](#8-packages)
9. [Access Control with Packages](#9-access-control-with-packages)
10. [Built-in Packages](#10-built-in-packages)

---

## 1. Exception Handling

### What is an Exception?
An exception is an unwanted or unexpected event that disrupts the normal flow of a program during execution.

### Exception Hierarchy

```
                    Object
                      |
                  Throwable
                   /     \
              Error      Exception
              /  \        /      \
         ...  ...  RuntimeException  IOException
                    /    |    \         |
                   ...  ...  ...       ...
```

### Why Exception Handling?
1. **Maintain normal program flow**
2. **Provide meaningful error messages**
3. **Separate error handling code from normal code**
4. **Graceful degradation**

### Basic Exception Handling

```java
public class BasicException {
    public static void main(String[] args) {
        System.out.println("Program starts");
        
        try {
            int result = 10 / 0;  // ArithmeticException
            System.out.println("Result: " + result);
        } catch (ArithmeticException e) {
            System.out.println("Cannot divide by zero");
        }
        
        System.out.println("Program continues...");
        System.out.println("Program ends");
    }
}
```

### Without Exception Handling

```java
public class WithoutExceptionHandling {
    public static void main(String[] args) {
        System.out.println("Program starts");
        
        int result = 10 / 0;  // ArithmeticException
        // Program terminates here
        
        System.out.println("This will not execute");
        System.out.println("Program ends");
    }
}
```

### Viva Questions

**Q1: What is an exception?**
A: An unwanted event that disrupts the normal flow of program execution.

**Q2: What is exception handling?**
A: A mechanism to handle runtime errors and maintain normal program flow.

**Q3: What is the parent class of all exceptions?**
A: Throwable class

**Q4: What is the difference between Error and Exception?**
A: Error is serious problem that application should not try to handle (OutOfMemoryError). Exception can be handled by application.

**Q5: What happens if an exception is not handled?**
A: Program terminates abnormally and displays error message.

---

## 2. Types of Exceptions

### Checked vs Unchecked Exceptions

```
Throwable
├── Error (Unchecked)
│   ├── OutOfMemoryError
│   ├── StackOverflowError
│   └── VirtualMachineError
└── Exception
    ├── RuntimeException (Unchecked)
    │   ├── ArithmeticException
    │   ├── NullPointerException
    │   ├── ArrayIndexOutOfBoundsException
    │   ├── NumberFormatException
    │   └── ClassCastException
    └── Checked Exceptions
        ├── IOException
        ├── SQLException
        ├── FileNotFoundException
        └── ClassNotFoundException
```

### Checked Exceptions

```java
import java.io.*;

public class CheckedException {
    public static void main(String[] args) {
        
        // Must handle checked exceptions
        try {
            FileReader file = new FileReader("test.txt");
            BufferedReader br = new BufferedReader(file);
            String line = br.readLine();
            System.out.println(line);
            br.close();
        } catch (FileNotFoundException e) {
            System.out.println("File not found: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("IO Error: " + e.getMessage());
        }
    }
}
```

### Unchecked Exceptions

```java
public class UncheckedException {
    public static void main(String[] args) {
        
        // 1. ArithmeticException
        try {
            int result = 10 / 0;
        } catch (ArithmeticException e) {
            System.out.println("Arithmetic Exception: " + e.getMessage());
        }
        
        // 2. NullPointerException
        try {
            String str = null;
            System.out.println(str.length());
        } catch (NullPointerException e) {
            System.out.println("Null Pointer Exception: " + e.getMessage());
        }
        
        // 3. ArrayIndexOutOfBoundsException
        try {
            int[] arr = {1, 2, 3};
            System.out.println(arr[5]);
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Array Index Out of Bounds: " + e.getMessage());
        }
        
        // 4. NumberFormatException
        try {
            int num = Integer.parseInt("abc");
        } catch (NumberFormatException e) {
            System.out.println("Number Format Exception: " + e.getMessage());
        }
        
        // 5. ClassCastException
        try {
            Object obj = new Integer(10);
            String str = (String) obj;
        } catch (ClassCastException e) {
            System.out.println("Class Cast Exception: " + e.getMessage());
        }
    }
}
```

### Common Built-in Exceptions

```java
public class CommonExceptions {
    public static void main(String[] args) {
        
        // 1. StringIndexOutOfBoundsException
        try {
            String str = "Hello";
            char ch = str.charAt(10);
        } catch (StringIndexOutOfBoundsException e) {
            System.out.println("1. " + e.getClass().getSimpleName());
        }
        
        // 2. IllegalArgumentException
        try {
            Thread.sleep(-1000);
        } catch (IllegalArgumentException e) {
            System.out.println("2. " + e.getClass().getSimpleName());
        } catch (InterruptedException e) {
            System.out.println("Interrupted");
        }
        
        // 3. IllegalStateException
        try {
            // Some operation that causes illegal state
        } catch (IllegalStateException e) {
            System.out.println("3. " + e.getClass().getSimpleName());
        }
    }
}
```

### Checked vs Unchecked Comparison

| Feature | Checked Exception | Unchecked Exception |
|---------|-------------------|---------------------|
| Checked at | Compile-time | Runtime |
| Must handle? | Yes (compile error if not) | No (optional) |
| Parent class | Exception | RuntimeException |
| Examples | IOException, SQLException | NullPointerException, ArithmeticException |
| Use case | Recoverable conditions | Programming errors |

### Viva Questions

**Q1: What is the difference between checked and unchecked exceptions?**
A: Checked exceptions are checked at compile-time (must handle), unchecked exceptions are checked at runtime (optional to handle).

**Q2: What are examples of checked exceptions?**
A: IOException, SQLException, FileNotFoundException, ClassNotFoundException

**Q3: What are examples of unchecked exceptions?**
A: ArithmeticException, NullPointerException, ArrayIndexOutOfBoundsException, NumberFormatException

**Q4: Which is the parent class of all unchecked exceptions?**
A: RuntimeException

**Q5: What is Error in Java?**
A: Serious problems that application should not try to handle (OutOfMemoryError, StackOverflowError).

---

## 3. Try-Catch-Finally

### Basic Try-Catch

```java
public class TryCatch {
    public static void main(String[] args) {
        
        try {
            // Code that may throw exception
            int result = 10 / 0;
            System.out.println("Result: " + result);
        } catch (ArithmeticException e) {
            // Exception handling code
            System.out.println("Exception caught: " + e.getMessage());
        }
        
        System.out.println("Program continues...");
    }
}
```

### Multiple Catch Blocks

```java
public class MultipleCatch {
    public static void main(String[] args) {
        
        try {
            int[] arr = {1, 2, 3};
            System.out.println(arr[5]);  // ArrayIndexOutOfBoundsException
            
            int result = 10 / 0;  // Never reached
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Array Index Exception: " + e.getMessage());
        } catch (ArithmeticException e) {
            System.out.println("Arithmetic Exception: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Generic Exception: " + e.getMessage());
        }
    }
}
```

### Catch Block Order

```java
public class CatchOrder {
    public static void main(String[] args) {
        
        try {
            int result = 10 / 0;
        } 
        // Specific exception first
        catch (ArithmeticException e) {
            System.out.println("Arithmetic Exception");
        }
        // Generic exception later
        catch (Exception e) {
            System.out.println("Exception");
        }
        
        // Wrong order - compile error
        /*
        catch (Exception e) {
            System.out.println("Exception");
        }
        catch (ArithmeticException e) {  // Unreachable - compile error
            System.out.println("Arithmetic Exception");
        }
        */
    }
}
```

### Multi-Catch (Java 7+)

```java
public class MultiCatch {
    public static void main(String[] args) {
        
        // Multiple exceptions in single catch
        try {
            int result = 10 / 0;
            int[] arr = {1, 2, 3};
            System.out.println(arr[5]);
        } catch (ArithmeticException | ArrayIndexOutOfBoundsException e) {
            System.out.println("Exception occurred: " + e.getMessage());
        }
    }
}
```

### Finally Block

```java
public class FinallyBlock {
    public static void main(String[] args) {
        
        try {
            System.out.println("Inside try block");
            int result = 10 / 0;
            System.out.println("Result: " + result);
        } catch (ArithmeticException e) {
            System.out.println("Inside catch block");
        } finally {
            // Always executes (even if exception occurs or not)
            System.out.println("Inside finally block");
        }
        
        System.out.println("After try-catch-finally");
    }
}
```

### Finally with Return

```java
public class FinallyReturn {
    
    static int test1() {
        try {
            return 10;
        } finally {
            System.out.println("Finally executes before return");
        }
    }
    
    static int test2() {
        try {
            return 10;
        } finally {
            return 20;  // Finally return overrides try return
        }
    }
    
    public static void main(String[] args) {
        System.out.println("test1 returns: " + test1());
        System.out.println("test2 returns: " + test2());
    }
}
```

### Try-with-Resources (Java 7+)

```java
import java.io.*;

public class TryWithResources {
    public static void main(String[] args) {
        
        // Automatic resource management (no need for finally)
        try (BufferedReader br = new BufferedReader(new FileReader("test.txt"))) {
            String line = br.readLine();
            System.out.println(line);
            // br.close() automatically called
        } catch (IOException e) {
            System.out.println("IO Exception: " + e.getMessage());
        }
        
        // Multiple resources
        try (
            FileReader fr = new FileReader("input.txt");
            BufferedReader br = new BufferedReader(fr);
            FileWriter fw = new FileWriter("output.txt")
        ) {
            String line;
            while ((line = br.readLine()) != null) {
                fw.write(line);
            }
        } catch (IOException e) {
            System.out.println("IO Exception: " + e.getMessage());
        }
    }
}
```

### Nested Try-Catch

```java
public class NestedTryCatch {
    public static void main(String[] args) {
        
        try {
            System.out.println("Outer try block");
            
            try {
                System.out.println("Inner try block");
                int result = 10 / 0;
            } catch (ArithmeticException e) {
                System.out.println("Inner catch block");
            }
            
            int[] arr = {1, 2, 3};
            System.out.println(arr[5]);
            
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Outer catch block");
        }
    }
}
```

### Exception Methods

```java
public class ExceptionMethods {
    public static void main(String[] args) {
        
        try {
            int result = 10 / 0;
        } catch (ArithmeticException e) {
            // Common exception methods
            
            System.out.println("1. getMessage(): " + e.getMessage());
            
            System.out.println("\n2. toString(): " + e.toString());
            
            System.out.println("\n3. printStackTrace():");
            e.printStackTrace();
            
            System.out.println("\n4. getClass(): " + e.getClass());
            
            System.out.println("\n5. getCause(): " + e.getCause());
            
            System.out.println("\n6. getStackTrace():");
            StackTraceElement[] elements = e.getStackTrace();
            for (StackTraceElement element : elements) {
                System.out.println(element);
            }
        }
    }
}
```

### Viva Questions

**Q1: What is try-catch-finally?**
A: try contains code that may throw exception, catch handles exception, finally always executes.

**Q2: Can we have try without catch?**
A: Yes, we can have try with finally (without catch).

**Q3: Can we have multiple catch blocks?**
A: Yes, but specific exceptions must come before generic ones.

**Q4: When does finally block execute?**
A: Always executes (whether exception occurs or not, even if return statement in try/catch).

**Q5: When finally block doesn't execute?**
A: When System.exit() is called or JVM crashes.

**Q6: What is try-with-resources?**
A: Automatic resource management (Java 7+), resources are closed automatically.

**Q7: Can we have finally without catch?**
A: Yes, we can have try-finally without catch.

**Q8: What is multi-catch?**
A: Handling multiple exceptions in single catch block using pipe (|) operator.

---

## 4. Throw and Throws

### throw Keyword

```java
public class ThrowExample {
    
    // Method that throws exception
    static void checkAge(int age) {
        if (age < 18) {
            throw new ArithmeticException("Not eligible to vote");
        } else {
            System.out.println("Eligible to vote");
        }
    }
    
    static void validateMarks(int marks) {
        if (marks < 0 || marks > 100) {
            throw new IllegalArgumentException("Invalid marks: " + marks);
        }
        System.out.println("Valid marks: " + marks);
    }
    
    public static void main(String[] args) {
        
        // Example 1
        try {
            checkAge(15);
        } catch (ArithmeticException e) {
            System.out.println("Exception: " + e.getMessage());
        }
        
        // Example 2
        try {
            validateMarks(150);
        } catch (IllegalArgumentException e) {
            System.out.println("Exception: " + e.getMessage());
        }
        
        // Example 3: Throwing exception without handling
        // checkAge(10);  // Program will terminate
    }
}
```

### throws Keyword

```java
import java.io.*;

public class ThrowsExample {
    
    // Method declares that it may throw IOException
    static void readFile() throws IOException {
        FileReader file = new FileReader("test.txt");
        BufferedReader br = new BufferedReader(file);
        System.out.println(br.readLine());
        br.close();
    }
    
    // Method declares multiple exceptions
    static void multipleExceptions() throws IOException, SQLException {
        // Code that may throw IOException or SQLException
    }
    
    // Caller must handle or declare throws
    static void caller() throws IOException {
        readFile();  // propagating exception
    }
    
    public static void main(String[] args) {
        
        // Method 1: Handle using try-catch
        try {
            readFile();
        } catch (IOException e) {
            System.out.println("File not found: " + e.getMessage());
        }
        
        // Method 2: Declare throws (propagate to JVM)
        // public static void main(String[] args) throws IOException
    }
}
```

### throw vs throws

```java
import java.io.*;

public class ThrowVsThrows {
    
    // throw: used to explicitly throw an exception
    static void method1(int age) {
        if (age < 18) {
            throw new ArithmeticException("Not eligible");  // throw keyword
        }
    }
    
    // throws: used to declare exceptions
    static void method2() throws IOException {  // throws keyword
        FileReader file = new FileReader("test.txt");
    }
    
    public static void main(String[] args) {
        
        // Handling throw
        try {
            method1(15);
        } catch (ArithmeticException e) {
            System.out.println(e.getMessage());
        }
        
        // Handling throws
        try {
            method2();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
```

### throw vs throws Comparison

| Feature | throw | throws |
|---------|-------|--------|
| Purpose | Explicitly throw exception | Declare exceptions |
| Location | Inside method body | Method signature |
| Exception type | throw single exception | throws multiple exceptions |
| Syntax | throw new Exception() | void method() throws Exception |
| Followed by | Exception object | Exception class names |

### Rethrowing Exception

```java
public class RethrowException {
    
    static void method1() throws Exception {
        try {
            int result = 10 / 0;
        } catch (ArithmeticException e) {
            System.out.println("Exception in method1");
            throw e;  // Rethrowing exception
        }
    }
    
    static void method2() {
        try {
            method1();
        } catch (Exception e) {
            System.out.println("Exception caught in method2");
        }
    }
    
    public static void main(String[] args) {
        method2();
    }
}
```

### Viva Questions

**Q1: What is throw keyword?**
A: Used to explicitly throw an exception from method or code block.

**Q2: What is throws keyword?**
A: Used to declare that a method may throw exceptions.

**Q3: What is the difference between throw and throws?**
A: throw is used to throw exception, throws is used to declare exceptions in method signature.

**Q4: Can we throw multiple exceptions using throw?**
A: No, throw can throw only one exception at a time.

**Q5: Can we declare multiple exceptions using throws?**
A: Yes, multiple exceptions can be declared separated by comma.

**Q6: Can we throw checked exception without throws?**
A: No, checked exceptions must be handled or declared using throws.

**Q7: Can we rethrow an exception?**
A: Yes, we can catch and rethrow exception using throw.

---

## 5. Custom Exceptions

### Creating Custom Exception

```java
// Custom unchecked exception
class InvalidAgeException extends RuntimeException {
    InvalidAgeException(String message) {
        super(message);
    }
}

// Custom checked exception
class InsufficientBalanceException extends Exception {
    InsufficientBalanceException(String message) {
        super(message);
    }
}

public class CustomException {
    
    static void validateAge(int age) {
        if (age < 18) {
            throw new InvalidAgeException("Age must be 18 or above");
        }
        System.out.println("Valid age: " + age);
    }
    
    static void withdraw(double balance, double amount) throws InsufficientBalanceException {
        if (amount > balance) {
            throw new InsufficientBalanceException("Insufficient balance");
        }
        System.out.println("Withdrawal successful");
    }
    
    public static void main(String[] args) {
        
        // Example 1: Unchecked custom exception
        try {
            validateAge(15);
        } catch (InvalidAgeException e) {
            System.out.println("Exception: " + e.getMessage());
        }
        
        // Example 2: Checked custom exception
        try {
            withdraw(1000, 1500);
        } catch (InsufficientBalanceException e) {
            System.out.println("Exception: " + e.getMessage());
        }
    }
}
```

### Custom Exception with Additional Fields

```java
class StudentException extends Exception {
    private int studentId;
    private String studentName;
    
    StudentException(String message, int studentId, String studentName) {
        super(message);
        this.studentId = studentId;
        this.studentName = studentName;
    }
    
    public int getStudentId() {
        return studentId;
    }
    
    public String getStudentName() {
        return studentName;
    }
    
    @Override
    public String toString() {
        return "StudentException: " + getMessage() + 
               " [ID: " + studentId + ", Name: " + studentName + "]";
    }
}

public class CustomExceptionWithFields {
    
    static void validateMarks(int studentId, String name, int marks) throws StudentException {
        if (marks < 0 || marks > 100) {
            throw new StudentException("Invalid marks", studentId, name);
        }
        System.out.println("Valid marks for " + name);
    }
    
    public static void main(String[] args) {
        try {
            validateMarks(101, "Sai", 150);
        } catch (StudentException e) {
            System.out.println(e);
            System.out.println("Student ID: " + e.getStudentId());
            System.out.println("Student Name: " + e.getStudentName());
        }
    }
}
```

### Real-World Custom Exception Example

```java
// Banking system custom exceptions
class BankException extends Exception {
    BankException(String message) {
        super(message);
    }
}

class InsufficientFundsException extends BankException {
    private double shortage;
    
    InsufficientFundsException(double shortage) {
        super("Insufficient funds");
        this.shortage = shortage;
    }
    
    public double getShortage() {
        return shortage;
    }
}

class InvalidAccountException extends BankException {
    InvalidAccountException(String message) {
        super(message);
    }
}

class BankAccount {
    private int accountNumber;
    private double balance;
    
    BankAccount(int accountNumber, double balance) {
        this.accountNumber = accountNumber;
        this.balance = balance;
    }
    
    void deposit(double amount) {
        balance += amount;
        System.out.println("Deposited: " + amount);
    }
    
    void withdraw(double amount) throws InsufficientFundsException {
        if (amount > balance) {
            double shortage = amount - balance;
            throw new InsufficientFundsException(shortage);
        }
        balance -= amount;
        System.out.println("Withdrawn: " + amount);
    }
    
    void displayBalance() {
        System.out.println("Balance: " + balance);
    }
}

public class BankingSystem {
    public static void main(String[] args) {
        BankAccount account = new BankAccount(12345, 1000);
        
        try {
            account.displayBalance();
            account.withdraw(500);
            account.displayBalance();
            account.withdraw(800);  // Insufficient funds
        } catch (InsufficientFundsException e) {
            System.out.println("Error: " + e.getMessage());
            System.out.println("You need " + e.getShortage() + " more");
        }
    }
}
```

### Viva Questions

**Q1: What is a custom exception?**
A: User-defined exception class created by extending Exception or RuntimeException.

**Q2: How to create custom exception?**
A: Extend Exception class (for checked) or RuntimeException (for unchecked).

**Q3: When to create custom exceptions?**
A: When built-in exceptions don't adequately describe the error condition.

**Q4: What is the advantage of custom exceptions?**
A: More meaningful error messages, additional fields for context, better code organization.

**Q5: Should custom exception be checked or unchecked?**
A: Depends on use case. Extend Exception for recoverable errors, RuntimeException for programming errors.

---

## 6. Exception Propagation

### How Exception Propagates

```java
public class ExceptionPropagation {
    
    static void method3() {
        int result = 10 / 0;  // Exception occurs here
        System.out.println(result);
    }
    
    static void method2() {
        method3();  // Exception propagates to method2
    }
    
    static void method1() {
        method2();  // Exception propagates to method1
    }
    
    public static void main(String[] args) {
        try {
            method1();  // Exception propagates to main
        } catch (ArithmeticException e) {
            System.out.println("Exception caught in main");
            e.printStackTrace();
        }
    }
}
```

### Unchecked Exception Propagation

```java
public class UncheckedExceptionPropagation {
    
    static void method1() {
        int result = 10 / 0;
    }
    
    static void method2() {
        method1();  // Unchecked exception automatically propagates
    }
    
    public static void main(String[] args) {
        try {
            method2();
        } catch (ArithmeticException e) {
            System.out.println("Exception handled in main");
        }
    }
}
```

### Checked Exception Propagation

```java
import java.io.*;

public class CheckedExceptionPropagation {
    
    // Checked exception must be declared
    static void method1() throws IOException {
        throw new IOException("IO Error");
    }
    
    static void method2() throws IOException {
        method1();  // Must declare or handle
    }
    
    public static void main(String[] args) {
        try {
            method2();
        } catch (IOException e) {
            System.out.println("Exception handled: " + e.getMessage());
        }
    }
}
```

### Stopping Exception Propagation

```java
public class StoppingPropagation {
    
    static void method2() {
        try {
            int result = 10 / 0;
        } catch (ArithmeticException e) {
            System.out.println("Exception handled in method2");
            // Exception stops here (not propagated)
        }
    }
    
    static void method1() {
        method2();  // No exception propagated
        System.out.println("method1 continues normally");
    }
    
    public static void main(String[] args) {
        method1();
        System.out.println("Program continues");
    }
}
```

### Viva Questions

**Q1: What is exception propagation?**
A: Process of passing exception from one method to another in the call stack.

**Q2: Which exceptions propagate automatically?**
A: Unchecked exceptions (RuntimeException) propagate automatically.

**Q3: Do checked exceptions propagate automatically?**
A: No, checked exceptions must be declared using throws or handled.

**Q4: How to stop exception propagation?**
A: By handling exception using try-catch.

**Q5: In which direction do exceptions propagate?**
A: From callee to caller (upward in the call stack).

---

## 7. Exception Handling Best Practices

### Best Practices Examples

```java
import java.io.*;

public class ExceptionBestPractices {
    
    // ❌ Bad Practice 1: Empty catch block
    static void badPractice1() {
        try {
            int result = 10 / 0;
        } catch (Exception e) {
            // Empty - never do this
        }
    }
    
    // ✅ Good Practice 1: Log or handle exception
    static void goodPractice1() {
        try {
            int result = 10 / 0;
        } catch (ArithmeticException e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // ❌ Bad Practice 2: Catching generic Exception
    static void badPractice2() {
        try {
            FileReader file = new FileReader("test.txt");
        } catch (Exception e) {  // Too generic
            System.out.println("Error occurred");
        }
    }
    
    // ✅ Good Practice 2: Catch specific exceptions
    static void goodPractice2() {
        try {
            FileReader file = new FileReader("test.txt");
        } catch (FileNotFoundException e) {  // Specific
            System.out.println("File not found: " + e.getMessage());
        }
    }
    
    // ❌ Bad Practice 3: Using exception for flow control
    static void badPractice3(String str) {
        try {
            int num = Integer.parseInt(str);
        } catch (NumberFormatException e) {
            // Don't use exceptions for normal flow
        }
    }
    
    // ✅ Good Practice 3: Validate before operation
    static void goodPractice3(String str) {
        if (str != null && str.matches("\\d+")) {
            int num = Integer.parseInt(str);
        } else {
            System.out.println("Invalid number format");
        }
    }
    
    // ✅ Good Practice 4: Use try-with-resources
    static void goodPractice4() {
        try (BufferedReader br = new BufferedReader(new FileReader("test.txt"))) {
            String line = br.readLine();
        } catch (IOException e) {
            System.out.println("IO Error: " + e.getMessage());
        }
    }
    
    // ✅ Good Practice 5: Custom meaningful exceptions
    static void goodPractice5(int age) throws InvalidAgeException {
        if (age < 0) {
            throw new InvalidAgeException("Age cannot be negative: " + age);
        }
    }
    
    // ✅ Good Practice 6: Clean up in finally
    static void goodPractice6() {
        FileReader file = null;
        try {
            file = new FileReader("test.txt");
            // Use file
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        } finally {
            if (file != null) {
                try {
                    file.close();
                } catch (IOException e) {
                    System.out.println("Error closing file");
                }
            }
        }
    }
}
```

### Best Practices Summary

1. **Never leave catch block empty**
2. **Catch specific exceptions, not generic Exception**
3. **Don't use exceptions for flow control**
4. **Use try-with-resources for automatic cleanup**
5. **Create meaningful custom exceptions**
6. **Clean up resources in finally or use try-with-resources**
7. **Log exception details for debugging**
8. **Don't catch Throwable or Error**
9. **Document exceptions using @throws in JavaDoc**
10. **Fail fast - throw exception early**

---

## 8. Packages

### What is a Package?
A package is a namespace that organizes classes and interfaces into groups.

### Benefits of Packages
1. **Namespace management** - Avoid naming conflicts
2. **Access control** - Package-level access protection
3. **Categorization** - Organize related classes
4. **Reusability** - Easy to reuse code

### Package Hierarchy

```
package
├── subpackage1
│   ├── Class1.java
│   └── Class2.java
└── subpackage2
    ├── Class3.java
    └── Class4.java
```

### Creating a Package

```java
// File: com/mycompany/util/Calculator.java
package com.mycompany.util;

public class Calculator {
    public int add(int a, int b) {
        return a + b;
    }
    
    public int subtract(int a, int b) {
        return a - b;
    }
}
```

```java
// File: com/mycompany/util/StringUtil.java
package com.mycompany.util;

public class StringUtil {
    public static String reverse(String str) {
        return new StringBuilder(str).reverse().toString();
    }
    
    public static boolean isPalindrome(String str) {
        String reversed = reverse(str);
        return str.equals(reversed);
    }
}
```

### Using a Package

```java
// File: Main.java
import com.mycompany.util.Calculator;
import com.mycompany.util.StringUtil;

public class Main {
    public static void main(String[] args) {
        
        // Using Calculator
        Calculator calc = new Calculator();
        System.out.println("Sum: " + calc.add(10, 20));
        
        // Using StringUtil
        System.out.println("Reversed: " + StringUtil.reverse("Hello"));
        System.out.println("Palindrome: " + StringUtil.isPalindrome("madam"));
    }
}
```

### Import Statements

```java
// 1. Import specific class
import java.util.ArrayList;

// 2. Import all classes from package
import java.util.*;

// 3. Import static member
import static java.lang.Math.PI;
import static java.lang.Math.sqrt;

// 4. Fully qualified name (no import needed)
java.util.ArrayList list = new java.util.ArrayList();

public class ImportExample {
    public static void main(String[] args) {
        
        // Using imported class
        ArrayList<Integer> list1 = new ArrayList<>();
        
        // Using static import
        System.out.println("PI: " + PI);
        System.out.println("Square root: " + sqrt(16));
        
        // Using fully qualified name
        java.util.Scanner scanner = new java.util.Scanner(System.in);
    }
}
```

### Subpackages

```java
// Parent package
// File: com/company/parent/ParentClass.java
package com.company.parent;

public class ParentClass {
    public void display() {
        System.out.println("Parent package class");
    }
}

// Sub package
// File: com/company/parent/child/ChildClass.java
package com.company.parent.child;

import com.company.parent.ParentClass;

public class ChildClass {
    public void show() {
        System.out.println("Child package class");
        ParentClass parent = new ParentClass();
        parent.display();
    }
}
```

### Package Naming Conventions

```
Domain: example.com
Reversed: com.example

Project: MyProject
Package: com.example.myproject

Subpackages:
com.example.myproject.model
com.example.myproject.view
com.example.myproject.controller
com.example.myproject.util
com.example.myproject.exception
```

### Compilation and Execution

```bash
# Compile with package
javac -d . Calculator.java
# -d . creates package directory structure in current directory

# Compile multiple files
javac -d . com/mycompany/util/*.java

# Run class with package
java com.mycompany.util.Calculator

# Set classpath
javac -classpath . Main.java
java -classpath . Main
```

### Viva Questions

**Q1: What is a package?**
A: A namespace that organizes classes and interfaces into groups.

**Q2: What are the benefits of packages?**
A: Namespace management, access control, categorization, reusability.

**Q3: What is the naming convention for packages?**
A: Lowercase letters, usually reverse domain name (com.company.project).

**Q4: What is the default package?**
A: If no package is specified, class belongs to unnamed default package.

**Q5: Can a class have two public classes?**
A: No, only one public class per file, and file name must match public class name.

**Q6: What is the difference between import and static import?**
A: import imports classes, static import imports static members of a class.

**Q7: Can we import same package/class twice?**
A: Yes, but it's redundant. No error occurs.

**Q8: What is the difference between import java.util.* and import java.util.Date?**
A: * imports all classes, specific import imports only one class.

**Q9: Does importing all classes affect performance?**
A: No, only used classes are loaded at runtime.

**Q10: Can a package have multiple subpackages?**
A: Yes, packages can have nested subpackages.

---

## 9. Access Control with Packages

### Package-Level Access Control

```java
// File: com/example/pack1/Class1.java
package com.example.pack1;

public class Class1 {
    public int publicVar = 10;
    protected int protectedVar = 20;
    int defaultVar = 30;  // package-private
    private int privateVar = 40;
    
    public void publicMethod() {
        System.out.println("Public method");
    }
    
    protected void protectedMethod() {
        System.out.println("Protected method");
    }
    
    void defaultMethod() {  // package-private
        System.out.println("Default method");
    }
    
    private void privateMethod() {
        System.out.println("Private method");
    }
}

// File: com/example/pack1/Class2.java (Same package)
package com.example.pack1;

public class Class2 {
    public static void main(String[] args) {
        Class1 obj = new Class1();
        
        System.out.println(obj.publicVar);      // ✓ Accessible
        System.out.println(obj.protectedVar);   // ✓ Accessible (same package)
        System.out.println(obj.defaultVar);     // ✓ Accessible (same package)
        // System.out.println(obj.privateVar);  // ✗ Not accessible
        
        obj.publicMethod();      // ✓ Accessible
        obj.protectedMethod();   // ✓ Accessible
        obj.defaultMethod();     // ✓ Accessible
        // obj.privateMethod();  // ✗ Not accessible
    }
}

// File: com/example/pack2/Class3.java (Different package)
package com.example.pack2;

import com.example.pack1.Class1;

public class Class3 {
    public static void main(String[] args) {
        Class1 obj = new Class1();
        
        System.out.println(obj.publicVar);      // ✓ Accessible
        // System.out.println(obj.protectedVar); // ✗ Not accessible (different package, no inheritance)
        // System.out.println(obj.defaultVar);   // ✗ Not accessible
        // System.out.println(obj.privateVar);   // ✗ Not accessible
        
        obj.publicMethod();      // ✓ Accessible
        // obj.protectedMethod(); // ✗ Not accessible
        // obj.defaultMethod();   // ✗ Not accessible
    }
}

// File: com/example/pack2/Class4.java (Different package, with inheritance)
package com.example.pack2;

import com.example.pack1.Class1;

public class Class4 extends Class1 {
    public void test() {
        System.out.println(publicVar);      // ✓ Accessible
        System.out.println(protectedVar);   // ✓ Accessible (through inheritance)
        // System.out.println(defaultVar);   // ✗ Not accessible
        // System.out.println(privateVar);   // ✗ Not accessible
        
        publicMethod();      // ✓ Accessible
        protectedMethod();   // ✓ Accessible (through inheritance)
        // defaultMethod();   // ✗ Not accessible
    }
}
```

### Access Modifiers with Packages Summary

| Modifier | Same Class | Same Package | Subclass (Different Package) | Everywhere |
|----------|-----------|--------------|-----------------------------|-----------| 
| public | ✓ | ✓ | ✓ | ✓ |
| protected | ✓ | ✓ | ✓ | ✗ |
| default | ✓ | ✓ | ✗ | ✗ |
| private | ✓ | ✗ | ✗ | ✗ |

---

## 10. Built-in Packages

### Common Java Packages

```java
// 1. java.lang (automatically imported)
public class JavaLang {
    public static void main(String[] args) {
        // String, System, Math, Integer, etc.
        String str = "Hello";
        System.out.println(str);
        int num = Integer.parseInt("123");
        double sqrt = Math.sqrt(16);
    }
}

// 2. java.util (utilities)
import java.util.*;

public class JavaUtil {
    public static void main(String[] args) {
        ArrayList<String> list = new ArrayList<>();
        HashMap<Integer, String> map = new HashMap<>();
        Scanner scanner = new Scanner(System.in);
        Date date = new Date();
        Random random = new Random();
    }
}

// 3. java.io (input/output)
import java.io.*;

public class JavaIO {
    public static void main(String[] args) throws IOException {
        FileReader file = new FileReader("test.txt");
        BufferedReader br = new BufferedReader(file);
        FileWriter fw = new FileWriter("output.txt");
        PrintWriter pw = new PrintWriter(fw);
    }
}

// 4. java.sql (database)
import java.sql.*;

public class JavaSQL {
    public static void main(String[] args) throws SQLException {
        Connection conn = DriverManager.getConnection("url", "user", "password");
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM table");
    }
}

// 5. java.net (networking)
import java.net.*;

public class JavaNet {
    public static void main(String[] args) throws Exception {
        URL url = new URL("https://www.example.com");
        Socket socket = new Socket("localhost", 8080);
        ServerSocket serverSocket = new ServerSocket(8080);
    }
}
```

### Common Packages List

| Package | Description | Common Classes |
|---------|-------------|----------------|
| java.lang | Fundamental classes | String, Math, System, Integer, Object |
| java.util | Utility classes | ArrayList, HashMap, Scanner, Date |
| java.io | Input/output | File, FileReader, BufferedReader, PrintWriter |
| java.sql | Database connectivity | Connection, Statement, ResultSet |
| java.net | Networking | URL, Socket, ServerSocket |
| java.awt | GUI (old) | Frame, Button, TextField |
| javax.swing | GUI (new) | JFrame, JButton, JTextField |
| java.time | Date and Time API | LocalDate, LocalTime, LocalDateTime |

### Viva Questions

**Q1: What is java.lang package?**
A: Contains fundamental classes, automatically imported (String, Math, System, etc.).

**Q2: Which package is automatically imported?**
A: java.lang package

**Q3: What is the purpose of java.util package?**
A: Contains utility classes like collections, date, scanner, etc.

**Q4: What is the difference between java.lang and java.util?**
A: java.lang has fundamental classes (auto-imported), java.util has utility classes (must import).

**Q5: Which package is used for file handling?**
A: java.io package

---

## Practice Programs

### 1. Multiple Exception Handling
```java
import java.util.Scanner;

public class MultipleExceptionDemo {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        
        try {
            System.out.print("Enter array size: ");
            int size = sc.nextInt();
            int[] arr = new int[size];
            
            System.out.print("Enter index: ");
            int index = sc.nextInt();
            
            System.out.print("Enter value: ");
            int value = sc.nextInt();
            
            arr[index] = value;
            
            System.out.print("Enter divisor: ");
            int divisor = sc.nextInt();
            int result = value / divisor;
            
            System.out.println("Result: " + result);
            
        } catch (NegativeArraySizeException e) {
            System.out.println("Array size cannot be negative");
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Invalid array index");
        } catch (ArithmeticException e) {
            System.out.println("Division by zero");
        } catch (Exception e) {
            System.out.println("General exception: " + e.getMessage());
        } finally {
            sc.close();
            System.out.println("Scanner closed");
        }
    }
}
```

### 2. Custom Exception for Banking
```java
class InsufficientBalanceException extends Exception {
    InsufficientBalanceException(String message) {
        super(message);
    }
}

class BankAccount {
    private double balance;
    
    BankAccount(double balance) {
        this.balance = balance;
    }
    
    void withdraw(double amount) throws InsufficientBalanceException {
        if (amount > balance) {
            throw new InsufficientBalanceException("Insufficient balance. Available: " + balance);
        }
        balance -= amount;
        System.out.println("Withdrawn: " + amount);
        System.out.println("Remaining balance: " + balance);
    }
}

public class BankDemo {
    public static void main(String[] args) {
        BankAccount account = new BankAccount(5000);
        
        try {
            account.withdraw(3000);
            account.withdraw(3000);  // Will throw exception
        } catch (InsufficientBalanceException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
```

---

## Summary

### Key Concepts Covered:
✅ Exception Handling (try-catch-finally)
✅ Types of Exceptions (Checked vs Unchecked)
✅ throw and throws keywords
✅ Custom Exceptions
✅ Exception Propagation
✅ Best Practices
✅ Packages (creating and using)
✅ Import statements
✅ Access Control with Packages
✅ Built-in Packages

### Important Points:
- Use try-catch to handle exceptions
- Checked exceptions must be handled or declared
- Unchecked exceptions are optional to handle
- Finally always executes
- throw to throw exception, throws to declare
- Custom exceptions for specific scenarios
- Packages organize classes
- Import to use classes from other packages

**Next**: Study Java4.md for Collections Framework!

---
**End of Java3.md**
