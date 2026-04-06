# JAVA4 - Collections Framework

## Table of Contents
1. [Collections Framework Introduction](#1-collections-framework-introduction)
2. [Collection Interface](#2-collection-interface)
3. [List Interface](#3-list-interface)
4. [Set Interface](#4-set-interface)
5. [Queue Interface](#5-queue-interface)
6. [Map Interface](#6-map-interface)
7. [Iterator and ListIterator](#7-iterator-and-listiterator)
8. [Collections Utility Class](#8-collections-utility-class)
9. [Comparable and Comparator](#9-comparable-and-comparator)
10. [Generics in Collections](#10-generics-in-collections)

---

## 1. Collections Framework Introduction

### What is Collections Framework?
A unified architecture for representing and manipulating collections of objects. It provides interfaces, implementations, and algorithms.

### Collection Hierarchy

```
                    Iterable
                       |
                   Collection
                /      |       \
              List    Set      Queue
             / | \    / | \       |
          AL LL  V  HS TS LHS   PQ
          
          (Separate)
              Map
             / | \
           HM TM LHM

AL  = ArrayList
LL  = LinkedList
V   = Vector
HS  = HashSet
TS  = TreeSet
LHS = LinkedHashSet
PQ  = PriorityQueue
HM  = HashMap
TM  = TreeMap
LHM = LinkedHashMap
```

### Benefits of Collections Framework
1. **Reduced programming effort** - Ready-to-use data structures and algorithms
2. **Increased performance** - High-performance implementations
3. **Interoperability** - Collections can be passed and manipulated uniformly
4. **Reduced effort to learn APIs** - Consistent interface design
5. **Reusability** - Standard interfaces for collections

### Collection vs Collections

| Collection | Collections |
|-----------|-------------|
| Interface | Class |
| Root interface of framework | Utility class with static methods |
| Defines methods for collection objects | Provides algorithms (sort, search, etc.) |
| Part of java.util | Part of java.util |

### Viva Questions

**Q1: What is Collections Framework?**
A: A unified architecture providing interfaces and classes for storing and manipulating groups of objects.

**Q2: What is the difference between Collection and Collections?**
A: Collection is an interface, Collections is a utility class with static methods.

**Q3: What are the main interfaces in Collections Framework?**
A: Collection, List, Set, Queue, Map

**Q4: What package contains Collections Framework?**
A: java.util package

**Q5: What is the root interface of Collections Framework?**
A: Iterable interface (Collection extends Iterable)

---

## 2. Collection Interface

### Collection Interface Methods

```java
import java.util.*;

public class CollectionMethods {
    public static void main(String[] args) {
        
        Collection<String> collection = new ArrayList<>();
        
        // 1. add() - Add element
        collection.add("Java");
        collection.add("Python");
        collection.add("C++");
        System.out.println("Collection: " + collection);
        
        // 2. size() - Get size
        System.out.println("Size: " + collection.size());
        
        // 3. contains() - Check if element exists
        System.out.println("Contains Java: " + collection.contains("Java"));
        
        // 4. remove() - Remove element
        collection.remove("Python");
        System.out.println("After removal: " + collection);
        
        // 5. isEmpty() - Check if empty
        System.out.println("Is empty: " + collection.isEmpty());
        
        // 6. clear() - Remove all elements
        // collection.clear();
        
        // 7. addAll() - Add all elements from another collection
        Collection<String> other = Arrays.asList("JavaScript", "Ruby");
        collection.addAll(other);
        System.out.println("After addAll: " + collection);
        
        // 8. removeAll() - Remove all elements present in another collection
        collection.removeAll(other);
        System.out.println("After removeAll: " + collection);
        
        // 9. toArray() - Convert to array
        Object[] arr = collection.toArray();
        System.out.println("Array: " + Arrays.toString(arr));
        
        // 10. iterator() - Get iterator
        Iterator<String> it = collection.iterator();
        while (it.hasNext()) {
            System.out.println(it.next());
        }
    }
}
```

---

## 3. List Interface

### List Interface Characteristics
- **Ordered** - Maintains insertion order
- **Indexed** - Elements accessed by index
- **Duplicates allowed**
- **Null values allowed**

### A. ArrayList

```java
import java.util.*;

public class ArrayListExample {
    public static void main(String[] args) {
        
        // Creating ArrayList
        ArrayList<Integer> list = new ArrayList<>();
        
        // 1. add() - Add elements
        list.add(10);
        list.add(20);
        list.add(30);
        list.add(20);  // Duplicates allowed
        System.out.println("ArrayList: " + list);
        
        // 2. add(index, element) - Add at specific index
        list.add(1, 15);
        System.out.println("After insertion: " + list);
        
        // 3. get() - Access element
        System.out.println("Element at index 2: " + list.get(2));
        
        // 4. set() - Update element
        list.set(0, 100);
        System.out.println("After update: " + list);
        
        // 5. remove(index) - Remove by index
        list.remove(1);
        System.out.println("After removal: " + list);
        
        // 6. remove(object) - Remove by object
        list.remove(Integer.valueOf(20));
        System.out.println("After removing 20: " + list);
        
        // 7. indexOf() - First occurrence
        list.add(30);
        System.out.println("Index of 30: " + list.indexOf(30));
        
        // 8. lastIndexOf() - Last occurrence
        System.out.println("Last index of 30: " + list.lastIndexOf(30));
        
        // 9. contains() - Check existence
        System.out.println("Contains 100: " + list.contains(100));
        
        // 10. size() - Get size
        System.out.println("Size: " + list.size());
        
        // 11. Iterating
        System.out.println("Iteration:");
        for (int num : list) {
            System.out.print(num + " ");
        }
        System.out.println();
        
        // 12. subList() - Get sublist
        List<Integer> sublist = list.subList(0, 2);
        System.out.println("Sublist: " + sublist);
        
        // 13. clear() - Remove all
        // list.clear();
    }
}
```

### ArrayList Internal Working

```java
public class ArrayListInternal {
    public static void main(String[] args) {
        
        // Default capacity: 10
        ArrayList<Integer> list = new ArrayList<>();  // capacity = 10
        
        // With initial capacity
        ArrayList<Integer> list2 = new ArrayList<>(100);  // capacity = 100
        
        // When capacity exceeds:
        // New capacity = (Old capacity * 3) / 2 + 1
        // Example: 10 -> 16 -> 25 -> 38...
        
        for (int i = 0; i < 15; i++) {
            list.add(i);
        }
        
        System.out.println("Size: " + list.size());
    }
}
```

### B. LinkedList

```java
import java.util.*;

public class LinkedListExample {
    public static void main(String[] args) {
        
        LinkedList<String> list = new LinkedList<>();
        
        // 1. add() - Add at end
        list.add("Java");
        list.add("Python");
        list.add("C++");
        System.out.println("LinkedList: " + list);
        
        // 2. addFirst() - Add at beginning
        list.addFirst("C");
        System.out.println("After addFirst: " + list);
        
        // 3. addLast() - Add at end
        list.addLast("JavaScript");
        System.out.println("After addLast: " + list);
        
        // 4. getFirst() - Get first element
        System.out.println("First: " + list.getFirst());
        
        // 5. getLast() - Get last element
        System.out.println("Last: " + list.getLast());
        
        // 6. removeFirst() - Remove first
        list.removeFirst();
        System.out.println("After removeFirst: " + list);
        
        // 7. removeLast() - Remove last
        list.removeLast();
        System.out.println("After removeLast: " + list);
        
        // 8. offer() - Add element (Queue method)
        list.offer("Ruby");
        System.out.println("After offer: " + list);
        
        // 9. poll() - Remove and return first (Queue method)
        String first = list.poll();
        System.out.println("Polled: " + first);
        System.out.println("After poll: " + list);
        
        // 10. peek() - View first element without removing
        System.out.println("Peek: " + list.peek());
    }
}
```

### C. Vector

```java
import java.util.*;

public class VectorExample {
    public static void main(String[] args) {
        
        // Vector is synchronized (thread-safe)
        Vector<Integer> vector = new Vector<>();
        
        // All ArrayList methods work
        vector.add(10);
        vector.add(20);
        vector.add(30);
        System.out.println("Vector: " + vector);
        
        // Additional Vector methods
        
        // 1. addElement() - Legacy method
        vector.addElement(40);
        
        // 2. capacity() - Current capacity
        System.out.println("Capacity: " + vector.capacity());
        
        // 3. firstElement() - First element
        System.out.println("First: " + vector.firstElement());
        
        // 4. lastElement() - Last element
        System.out.println("Last: " + vector.lastElement());
        
        // 5. removeElement() - Remove by element
        vector.removeElement(Integer.valueOf(20));
        System.out.println("After removal: " + vector);
    }
}
```

### ArrayList vs LinkedList vs Vector

| Feature | ArrayList | LinkedList | Vector |
|---------|-----------|------------|--------|
| Data Structure | Dynamic array | Doubly linked list | Dynamic array |
| Access Time | O(1) | O(n) | O(1) |
| Insert/Delete | O(n) | O(1) | O(n) |
| Thread-Safe | No | No | Yes (synchronized) |
| Performance | Fast access | Fast insert/delete | Slower (overhead) |
| Memory | Less overhead | More overhead (nodes) | Less overhead |
| Use Case | Frequent access | Frequent insert/delete | Thread-safe list |

### Viva Questions

**Q1: What is the difference between ArrayList and LinkedList?**
A: ArrayList uses dynamic array (fast access O(1)), LinkedList uses doubly-linked list (fast insert/delete O(1) at ends).

**Q2: Which is faster for random access?**
A: ArrayList is faster for random access.

**Q3: Which is faster for insertion/deletion?**
A: LinkedList is faster for insertion/deletion (especially at beginning/end).

**Q4: What is the default capacity of ArrayList?**
A: 10 elements

**Q5: Is ArrayList thread-safe?**
A: No, ArrayList is not synchronized (not thread-safe).

**Q6: What is Vector?**
A: Synchronized (thread-safe) version of ArrayList, but slower due to overhead.

**Q7: Can List contain duplicate elements?**
A: Yes, List can contain duplicates.

**Q8: Can List contain null values?**
A: Yes, List can contain null values.

---

## 4. Set Interface

### Set Interface Characteristics
- **No duplicates** - Unique elements only
- **Unordered** - No insertion order (except LinkedHashSet, TreeSet is sorted)
- **One null allowed** - Only one null element (HashSet)

### A. HashSet

```java
import java.util.*;

public class HashSetExample {
    public static void main(String[] args) {
        
        // Creating HashSet
        HashSet<String> set = new HashSet<>();
        
        // 1. add() - Add elements
        set.add("Java");
        set.add("Python");
        set.add("C++");
        set.add("Java");  // Duplicate ignored
        System.out.println("HashSet: " + set);  // No duplicates, unordered
        
        // 2. contains() - Check existence
        System.out.println("Contains Java: " + set.contains("Java"));
        
        // 3. remove() - Remove element
        set.remove("Python");
        System.out.println("After removal: " + set);
        
        // 4. size() - Get size
        System.out.println("Size: " + set.size());
        
        // 5. isEmpty() - Check if empty
        System.out.println("Is empty: " + set.isEmpty());
        
        // 6. Null values
        set.add(null);
        System.out.println("With null: " + set);
        
        // 7. Iteration
        System.out.println("Iteration:");
        for (String lang : set) {
            System.out.println(lang);
        }
        
        // 8. clear() - Remove all
        // set.clear();
    }
}
```

### HashSet Internal Working

```java
public class HashSetInternal {
    public static void main(String[] args) {
        
        // HashSet internally uses HashMap
        // Elements stored as keys, value is dummy object (PRESENT)
        
        HashSet<Integer> set = new HashSet<>();
        set.add(10);  // Internally: map.put(10, PRESENT)
        set.add(20);
        set.add(10);  // Returns false, duplicate key
        
        System.out.println(set);
        
        // Hash collision handling: Chaining (LinkedList at each bucket)
        // Load factor: 0.75 (rehashing occurs when 75% full)
        // Initial capacity: 16
    }
}
```

### B. LinkedHashSet

```java
import java.util.*;

public class LinkedHashSetExample {
    public static void main(String[] args) {
        
        // Maintains insertion order
        LinkedHashSet<String> set = new LinkedHashSet<>();
        
        set.add("Java");
        set.add("Python");
        set.add("C++");
        set.add("JavaScript");
        
        System.out.println("LinkedHashSet: " + set);
        // Output: [Java, Python, C++, JavaScript] (insertion order maintained)
        
        // Same methods as HashSet
        
        // Performance: Slightly slower than HashSet due to maintaining order
    }
}
```

### C. TreeSet

```java
import java.util.*;

public class TreeSetExample {
    public static void main(String[] args) {
        
        // Sorted set (ascending order)
        TreeSet<Integer> set = new TreeSet<>();
        
        set.add(50);
        set.add(20);
        set.add(40);
        set.add(10);
        set.add(30);
        
        System.out.println("TreeSet: " + set);  // [10, 20, 30, 40, 50] (sorted)
        
        // TreeSet specific methods
        
        // 1. first() - Get first (smallest)
        System.out.println("First: " + set.first());
        
        // 2. last() - Get last (largest)
        System.out.println("Last: " + set.last());
        
        // 3. headSet() - Elements less than given element
        System.out.println("HeadSet (<30): " + set.headSet(30));
        
        // 4. tailSet() - Elements greater than or equal to given element
        System.out.println("TailSet (>=30): " + set.tailSet(30));
        
        // 5. subSet() - Elements in range
        System.out.println("SubSet (20-40): " + set.subSet(20, 40));
        
        // 6. higher() - Next higher element
        System.out.println("Higher than 25: " + set.higher(25));
        
        // 7. lower() - Next lower element
        System.out.println("Lower than 25: " + set.lower(25));
        
        // 8. ceiling() - Greater than or equal
        System.out.println("Ceiling of 25: " + set.ceiling(25));
        
        // 9. floor() - Less than or equal
        System.out.println("Floor of 25: " + set.floor(25));
        
        // 10. pollFirst() - Remove and return first
        System.out.println("Poll first: " + set.pollFirst());
        System.out.println("After pollFirst: " + set);
        
        // 11. pollLast() - Remove and return last
        System.out.println("Poll last: " + set.pollLast());
        System.out.println("After pollLast: " + set);
        
        // Descending order
        TreeSet<Integer> descendingSet = (TreeSet<Integer>) set.descendingSet();
        System.out.println("Descending: " + descendingSet);
    }
}
```

### TreeSet with Custom Objects

```java
import java.util.*;

class Student implements Comparable<Student> {
    int id;
    String name;
    
    Student(int id, String name) {
        this.id = id;
        this.name = name;
    }
    
    // Must implement compareTo for TreeSet
    @Override
    public int compareTo(Student other) {
        return this.id - other.id;  // Sort by id
    }
    
    @Override
    public String toString() {
        return id + "-" + name;
    }
}

public class TreeSetCustom {
    public static void main(String[] args) {
        
        TreeSet<Student> set = new TreeSet<>();
        set.add(new Student(3, "Charlie"));
        set.add(new Student(1, "Alice"));
        set.add(new Student(2, "Bob"));
        
        System.out.println("Sorted students: " + set);
        // Output: [1-Alice, 2-Bob, 3-Charlie]
    }
}
```

### HashSet vs LinkedHashSet vs TreeSet

| Feature | HashSet | LinkedHashSet | TreeSet |
|---------|---------|---------------|---------|
| Order | Unordered | Insertion order | Sorted (natural/custom) |
| Data Structure | HashMap | HashMap + LinkedList | Red-Black Tree |
| Performance | O(1) | O(1) | O(log n) |
| Null | One null allowed | One null allowed | No null (causes NPE) |
| Use Case | Unique elements only | Unique + maintain order | Unique + sorted |

### Viva Questions

**Q1: What is the difference between HashSet and TreeSet?**
A: HashSet is unordered (O(1) operations), TreeSet is sorted (O(log n) operations).

**Q2: Which Set maintains insertion order?**
A: LinkedHashSet maintains insertion order.

**Q3: Can Set contain duplicate elements?**
A: No, Set contains only unique elements.

**Q4: Can HashSet contain null values?**
A: Yes, HashSet can contain one null value.

**Q5: Can TreeSet contain null values?**
A: No, TreeSet doesn't allow null (throws NullPointerException).

**Q6: What is the internal implementation of HashSet?**
A: HashSet internally uses HashMap where elements are stored as keys.

**Q7: Which is faster: HashSet or TreeSet?**
A: HashSet is faster (O(1) vs O(log n)).

**Q8: What happens if we add duplicate element to Set?**
A: add() returns false, element is not added.

---

## 5. Queue Interface

### Queue Interface Characteristics
- **FIFO** - First In First Out (by default)
- **Priority Queue** - Elements ordered by priority
- **Deque** - Double-ended queue (add/remove from both ends)

### A. Queue (LinkedList implementation)

```java
import java.util.*;

public class QueueExample {
    public static void main(String[] args) {
        
        // Queue interface, LinkedList implementation
        Queue<String> queue = new LinkedList<>();
        
        // 1. add() - Add element (throws exception if fails)
        queue.add("First");
        queue.add("Second");
        queue.add("Third");
        System.out.println("Queue: " + queue);
        
        // 2. offer() - Add element (returns false if fails)
        queue.offer("Fourth");
        System.out.println("After offer: " + queue);
        
        // 3. remove() - Remove and return head (throws exception if empty)
        String removed = queue.remove();
        System.out.println("Removed: " + removed);
        System.out.println("After remove: " + queue);
        
        // 4. poll() - Remove and return head (returns null if empty)
        String polled = queue.poll();
        System.out.println("Polled: " + polled);
        System.out.println("After poll: " + queue);
        
        // 5. element() - View head (throws exception if empty)
        System.out.println("Element: " + queue.element());
        
        // 6. peek() - View head (returns null if empty)
        System.out.println("Peek: " + queue.peek());
        
        System.out.println("Final queue: " + queue);
    }
}
```

### B. PriorityQueue

```java
import java.util.*;

public class PriorityQueueExample {
    public static void main(String[] args) {
        
        // Natural ordering (min-heap by default)
        PriorityQueue<Integer> pq = new PriorityQueue<>();
        
        pq.add(50);
        pq.add(10);
        pq.add(30);
        pq.add(20);
        pq.add(40);
        
        System.out.println("PriorityQueue: " + pq);  // Internal representation
        
        // Elements removed in priority order (ascending)
        System.out.println("Removing elements:");
        while (!pq.isEmpty()) {
            System.out.print(pq.poll() + " ");  // 10 20 30 40 50
        }
        System.out.println();
        
        // Max-heap (reverse order)
        PriorityQueue<Integer> maxHeap = new PriorityQueue<>(Collections.reverseOrder());
        maxHeap.add(50);
        maxHeap.add(10);
        maxHeap.add(30);
        maxHeap.add(20);
        maxHeap.add(40);
        
        System.out.println("Max-heap removal:");
        while (!maxHeap.isEmpty()) {
            System.out.print(maxHeap.poll() + " ");  // 50 40 30 20 10
        }
        System.out.println();
    }
}
```

### PriorityQueue with Custom Objects

```java
import java.util.*;

class Task implements Comparable<Task> {
    String name;
    int priority;
    
    Task(String name, int priority) {
        this.name = name;
        this.priority = priority;
    }
    
    @Override
    public int compareTo(Task other) {
        return this.priority - other.priority;  // Lower priority number = higher priority
    }
    
    @Override
    public String toString() {
        return name + "(" + priority + ")";
    }
}

public class PriorityQueueCustom {
    public static void main(String[] args) {
        
        PriorityQueue<Task> taskQueue = new PriorityQueue<>();
        
        taskQueue.add(new Task("Low priority task", 3));
        taskQueue.add(new Task("High priority task", 1));
        taskQueue.add(new Task("Medium priority task", 2));
        
        System.out.println("Processing tasks:");
        while (!taskQueue.isEmpty()) {
            System.out.println(taskQueue.poll());
        }
        // Output: High(1), Medium(2), Low(3)
    }
}
```

### C. Deque (ArrayDeque)

```java
import java.util.*;

public class DequeExample {
    public static void main(String[] args) {
        
        // Deque - Double-ended queue
        Deque<String> deque = new ArrayDeque<>();
        
        // Add at front
        deque.addFirst("A");
        deque.addFirst("B");
        System.out.println("After addFirst: " + deque);  // [B, A]
        
        // Add at end
        deque.addLast("C");
        deque.addLast("D");
        System.out.println("After addLast: " + deque);  // [B, A, C, D]
        
        // Get first and last
        System.out.println("First: " + deque.getFirst());  // B
        System.out.println("Last: " + deque.getLast());    // D
        
        // Remove from front
        System.out.println("Remove first: " + deque.removeFirst());  // B
        System.out.println("After removeFirst: " + deque);
        
        // Remove from end
        System.out.println("Remove last: " + deque.removeLast());    // D
        System.out.println("After removeLast: " + deque);
        
        // Deque can be used as Stack
        deque.push("X");  // Same as addFirst
        deque.push("Y");
        System.out.println("After push: " + deque);
        
        System.out.println("Pop: " + deque.pop());  // Same as removeFirst
        System.out.println("After pop: " + deque);
    }
}
```

### Viva Questions

**Q1: What is Queue?**
A: Interface representing FIFO (First In First Out) collection.

**Q2: What is PriorityQueue?**
A: Queue where elements are ordered based on their priority (natural ordering or custom comparator).

**Q3: What is Deque?**
A: Double-ended queue that allows insertion and removal from both ends.

**Q4: What is the difference between add() and offer()?**
A: add() throws exception if fails, offer() returns false.

**Q5: What is the difference between remove() and poll()?**
A: remove() throws exception if empty, poll() returns null.

**Q6: Can PriorityQueue contain null?**
A: No, PriorityQueue doesn't allow null.

**Q7: What is the internal implementation of PriorityQueue?**
A: Min-heap (binary heap) data structure.

---

## 6. Map Interface

### Map Interface Characteristics
- **Key-Value pairs**
- **No duplicate keys** (keys are unique)
- **One key can map to one value**
- **Not part of Collection interface**

### A. HashMap

```java
import java.util.*;

public class HashMapExample {
    public static void main(String[] args) {
        
        // Creating HashMap
        HashMap<Integer, String> map = new HashMap<>();
        
        // 1. put() - Add key-value pair
        map.put(1, "Java");
        map.put(2, "Python");
        map.put(3, "C++");
        map.put(4, "JavaScript");
        System.out.println("HashMap: " + map);
        
        // 2. get() - Get value by key
        System.out.println("Value for key 2: " + map.get(2));
        
        // 3. Duplicate key - Overwrites value
        map.put(2, "Ruby");
        System.out.println("After duplicate key: " + map);
        
        // 4. containsKey() - Check if key exists
        System.out.println("Contains key 3: " + map.containsKey(3));
        
        // 5. containsValue() - Check if value exists
        System.out.println("Contains value 'Java': " + map.containsValue("Java"));
        
        // 6. remove() - Remove by key
        map.remove(4);
        System.out.println("After removal: " + map);
        
        // 7. size() - Get size
        System.out.println("Size: " + map.size());
        
        // 8. isEmpty() - Check if empty
        System.out.println("Is empty: " + map.isEmpty());
        
        // 9. keySet() - Get all keys
        System.out.println("Keys: " + map.keySet());
        
        // 10. values() - Get all values
        System.out.println("Values: " + map.values());
        
        // 11. entrySet() - Get all key-value pairs
        System.out.println("EntrySet: " + map.entrySet());
        
        // 12. Iterating HashMap
        System.out.println("\nIteration using entrySet:");
        for (Map.Entry<Integer, String> entry : map.entrySet()) {
            System.out.println(entry.getKey() + " => " + entry.getValue());
        }
        
        System.out.println("\nIteration using keySet:");
        for (Integer key : map.keySet()) {
            System.out.println(key + " => " + map.get(key));
        }
        
        // 13. putIfAbsent() - Add only if key doesn't exist
        map.putIfAbsent(2, "Kotlin");  // Won't add, key 2 exists
        map.putIfAbsent(5, "Go");      // Will add
        System.out.println("After putIfAbsent: " + map);
        
        // 14. getOrDefault() - Get value or default
        System.out.println("Key 10: " + map.getOrDefault(10, "Not Found"));
        
        // 15. replace() - Replace value for key
        map.replace(1, "Java SE");
        System.out.println("After replace: " + map);
        
        // 16. Null values
        map.put(6, null);
        map.put(null, "Null Key");
        System.out.println("With null: " + map);
        
        // 17. clear() - Remove all
        // map.clear();
    }
}
```

### HashMap Internal Working

```java
public class HashMapInternal {
    public static void main(String[] args) {
        
        // HashMap uses array of buckets (nodes)
        // Default capacity: 16
        // Load factor: 0.75 (rehashing when 75% full)
        
        HashMap<Integer, String> map = new HashMap<>();
        
        // When put():
        // 1. Calculate hashCode of key
        // 2. Calculate index: hashCode % capacity
        // 3. Store at index (bucket)
        // 4. If collision: Use LinkedList (chaining)
        // 5. Java 8+: If bucket size > 8, convert to TreeMap (balanced tree)
        
        map.put(1, "One");    // hashCode(1) % 16 = some index
        map.put(17, "Seventeen");  // Might collide with 1
        
        System.out.println(map);
        
        // Performance: O(1) average, O(n) worst case (all elements in one bucket)
    }
}
```

### B. LinkedHashMap

```java
import java.util.*;

public class LinkedHashMapExample {
    public static void main(String[] args) {
        
        // Maintains insertion order
        LinkedHashMap<Integer, String> map = new LinkedHashMap<>();
        
        map.put(3, "Three");
        map.put(1, "One");
        map.put(4, "Four");
        map.put(2, "Two");
        
        System.out.println("LinkedHashMap: " + map);
        // Output: {3=Three, 1=One, 4=Four, 2=Two} (insertion order)
        
        // All HashMap methods work
        
        // Access order mode (moves accessed entry to end)
        LinkedHashMap<Integer, String> accessOrderMap = 
            new LinkedHashMap<>(16, 0.75f, true);
        
        accessOrderMap.put(1, "One");
        accessOrderMap.put(2, "Two");
        accessOrderMap.put(3, "Three");
        
        System.out.println("Before access: " + accessOrderMap);
        accessOrderMap.get(1);  // Access key 1
        System.out.println("After accessing 1: " + accessOrderMap);
        // Entry for key 1 moved to end
    }
}
```

### C. TreeMap

```java
import java.util.*;

public class TreeMapExample {
    public static void main(String[] args) {
        
        // Sorted by keys (ascending order)
        TreeMap<Integer, String> map = new TreeMap<>();
        
        map.put(3, "Three");
        map.put(1, "One");
        map.put(4, "Four");
        map.put(2, "Two");
        
        System.out.println("TreeMap: " + map);
        // Output: {1=One, 2=Two, 3=Three, 4=Four} (sorted by keys)
        
        // TreeMap specific methods
        
        // 1. firstKey() / lastKey()
        System.out.println("First key: " + map.firstKey());
        System.out.println("Last key: " + map.lastKey());
        
        // 2. firstEntry() / lastEntry()
        System.out.println("First entry: " + map.firstEntry());
        System.out.println("Last entry: " + map.lastEntry());
        
        // 3. headMap() - Keys less than given key
        System.out.println("HeadMap (<3): " + map.headMap(3));
        
        // 4. tailMap() - Keys greater than or equal to given key
        System.out.println("TailMap (>=3): " + map.tailMap(3));
        
        // 5. subMap() - Keys in range
        System.out.println("SubMap (2-4): " + map.subMap(2, 4));
        
        // 6. higherKey() / lowerKey()
        System.out.println("Higher than 2: " + map.higherKey(2));
        System.out.println("Lower than 2: " + map.lowerKey(2));
        
        // 7. ceilingKey() / floorKey()
        System.out.println("Ceiling of 2.5: " + map.ceilingKey(2));
        System.out.println("Floor of 2.5: " + map.floorKey(3));
        
        // 8. pollFirstEntry() / pollLastEntry()
        System.out.println("Poll first: " + map.pollFirstEntry());
        System.out.println("After pollFirst: " + map);
        
        // Descending map
        NavigableMap<Integer, String> descendingMap = map.descendingMap();
        System.out.println("Descending: " + descendingMap);
    }
}
```

### D. Hashtable

```java
import java.util.*;

public class HashtableExample {
    public static void main(String[] args) {
        
        // Hashtable is synchronized (thread-safe) - legacy class
        Hashtable<Integer, String> table = new Hashtable<>();
        
        table.put(1, "One");
        table.put(2, "Two");
        table.put(3, "Three");
        
        System.out.println("Hashtable: " + table);
        
        // Null not allowed (throws NullPointerException)
        // table.put(null, "Null Key");  // Error
        // table.put(4, null);           // Error
        
        // All basic Map methods work same as HashMap
    }
}
```

### HashMap vs LinkedHashMap vs TreeMap vs Hashtable

| Feature | HashMap | LinkedHashMap | TreeMap | Hashtable |
|---------|---------|---------------|---------|-----------|
| Order | Unordered | Insertion order | Sorted (keys) | Unordered |
| Null key | 1 null allowed | 1 null allowed | No null | No null |
| Null values | Allowed | Allowed | Allowed | No null |
| Thread-safe | No | No | No | Yes |
| Performance | O(1) | O(1) | O(log n) | O(1) |
| Use Case | Fast access | Maintain order | Sorted keys | Thread-safe |

### Viva Questions

**Q1: What is Map?**
A: Interface for storing key-value pairs with unique keys.

**Q2: What is the difference between HashMap and TreeMap?**
A: HashMap is unordered (O(1)), TreeMap is sorted by keys (O(log n)).

**Q3: Which Map maintains insertion order?**
A: LinkedHashMap maintains insertion order.

**Q4: Can HashMap have duplicate keys?**
A: No, keys must be unique. Duplicate key overwrites the value.

**Q5: Can HashMap have null keys?**
A: Yes, HashMap allows one null key and multiple null values.

**Q6: What is the internal implementation of HashMap?**
A: Array of buckets (nodes) with chaining for collision handling.

**Q7: What is the difference between HashMap and Hashtable?**
A: HashMap is not synchronized (no null), Hashtable is synchronized (thread-safe, no null).

**Q8: What is load factor in HashMap?**
A: Threshold (default 0.75) when HashMap is rehashed to increase capacity.

---

## 7. Iterator and ListIterator

### A. Iterator

```java
import java.util.*;

public class IteratorExample {
    public static void main(String[] args) {
        
        ArrayList<String> list = new ArrayList<>();
        list.add("Java");
        list.add("Python");
        list.add("C++");
        list.add("JavaScript");
        
        // Getting iterator
        Iterator<String> iterator = list.iterator();
        
        // 1. hasNext() - Check if more elements exist
        // 2. next() - Get next element
        System.out.println("Forward iteration:");
        while (iterator.hasNext()) {
            String element = iterator.next();
            System.out.println(element);
        }
        
        // 3. remove() - Remove current element
        System.out.println("\nRemoving elements containing 'a':");
        iterator = list.iterator();
        while (iterator.hasNext()) {
            String element = iterator.next();
            if (element.contains("a")) {
                iterator.remove();  // Safe removal during iteration
            }
        }
        System.out.println("After removal: " + list);
        
        // 4. forEachRemaining() - Java 8
        list.add("Ruby");
        list.add("Go");
        iterator = list.iterator();
        System.out.println("\nUsing forEachRemaining:");
        iterator.forEachRemaining(e -> System.out.println(e));
    }
}
```

### B. ListIterator

```java
import java.util.*;

public class ListIteratorExample {
    public static void main(String[] args) {
        
        ArrayList<String> list = new ArrayList<>();
        list.add("A");
        list.add("B");
        list.add("C");
        list.add("D");
        
        // ListIterator - bidirectional iterator
        ListIterator<String> iterator = list.listIterator();
        
        // Forward iteration
        System.out.println("Forward:");
        while (iterator.hasNext()) {
            System.out.println(iterator.next() + " at index " + iterator.previousIndex());
        }
        
        // Backward iteration
        System.out.println("\nBackward:");
        while (iterator.hasPrevious()) {
            System.out.println(iterator.previous() + " at index " + iterator.nextIndex());
        }
        
        // set() - Replace element
        iterator = list.listIterator();
        while (iterator.hasNext()) {
            String element = iterator.next();
            if (element.equals("B")) {
                iterator.set("B_Modified");
            }
        }
        System.out.println("\nAfter set: " + list);
        
        // add() - Add element
        iterator = list.listIterator();
        while (iterator.hasNext()) {
            String element = iterator.next();
            if (element.equals("C")) {
                iterator.add("C_New");
            }
        }
        System.out.println("After add: " + list);
        
        // Starting from specific index
        ListIterator<String> it2 = list.listIterator(2);
        System.out.println("\nFrom index 2:");
        while (it2.hasNext()) {
            System.out.println(it2.next());
        }
    }
}
```

### Iterator vs ListIterator

| Feature | Iterator | ListIterator |
|---------|----------|--------------|
| Direction | Forward only | Bidirectional |
| Methods | hasNext(), next(), remove() | All + hasPrevious(), previous(), set(), add() |
| Works on | Collection | List only |
| Index | No index methods | nextIndex(), previousIndex() |
| Modification | Remove only | Remove, Set, Add |

### Fail-Fast vs Fail-Safe

```java
import java.util.*;
import java.util.concurrent.*;

public class FailFastFailSafe {
    public static void main(String[] args) {
        
        // Fail-Fast: Throws ConcurrentModificationException
        System.out.println("Fail-Fast:");
        ArrayList<Integer> list = new ArrayList<>();
        list.add(1);
        list.add(2);
        list.add(3);
        
        try {
            for (Integer num : list) {
                System.out.println(num);
                list.add(4);  // Concurrent modification - throws exception
            }
        } catch (ConcurrentModificationException e) {
            System.out.println("ConcurrentModificationException caught");
        }
        
        // Fail-Safe: Works on copy, no exception
        System.out.println("\nFail-Safe:");
        CopyOnWriteArrayList<Integer> safeList = new CopyOnWriteArrayList<>();
        safeList.add(1);
        safeList.add(2);
        safeList.add(3);
        
        for (Integer num : safeList) {
            System.out.println(num);
            safeList.add(4);  // No exception, but changes not visible in current iteration
        }
        System.out.println("Final list: " + safeList);
    }
}
```

### Viva Questions

**Q1: What is Iterator?**
A: Interface to traverse collections one element at a time.

**Q2: What is the difference between Iterator and ListIterator?**
A: Iterator is unidirectional (forward only), ListIterator is bidirectional (forward and backward).

**Q3: Can we modify collection while iterating?**
A: Not directly (causes ConcurrentModificationException). Use iterator.remove().

**Q4: What is fail-fast iterator?**
A: Iterator that throws ConcurrentModificationException if collection is modified during iteration.

**Q5: What is fail-safe iterator?**
A: Iterator that works on copy of collection, doesn't throw exception on modification.

**Q6: Which collections are fail-safe?**
A: CopyOnWriteArrayList, ConcurrentHashMap

**Q7: What methods does Iterator have?**
A: hasNext(), next(), remove(), forEachRemaining()

---

## 8. Collections Utility Class

### Collections Class Methods

```java
import java.util.*;

public class CollectionsUtility {
    public static void main(String[] args) {
        
        ArrayList<Integer> list = new ArrayList<>();
        list.add(50);
        list.add(20);
        list.add(40);
        list.add(10);
        list.add(30);
        
        System.out.println("Original: " + list);
        
        // 1. sort() - Sort in natural order
        Collections.sort(list);
        System.out.println("After sort: " + list);
        
        // 2. sort with Comparator - Reverse order
        Collections.sort(list, Collections.reverseOrder());
        System.out.println("Reverse sort: " + list);
        
        // 3. reverse() - Reverse the list
        Collections.reverse(list);
        System.out.println("After reverse: " + list);
        
        // 4. shuffle() - Random order
        Collections.shuffle(list);
        System.out.println("After shuffle: " + list);
        
        // 5. max() - Maximum element
        System.out.println("Max: " + Collections.max(list));
        
        // 6. min() - Minimum element
        System.out.println("Min: " + Collections.min(list));
        
        // 7. frequency() - Count occurrences
        list.add(10);
        list.add(10);
        System.out.println("Frequency of 10: " + Collections.frequency(list, 10));
        
        // 8. binarySearch() - Search in sorted list
        Collections.sort(list);
        int index = Collections.binarySearch(list, 30);
        System.out.println("Index of 30: " + index);
        
        // 9. copy() - Copy elements
        ArrayList<Integer> dest = new ArrayList<>(Arrays.asList(new Integer[list.size()]));
        Collections.copy(dest, list);
        System.out.println("Copied list: " + dest);
        
        // 10. fill() - Fill all elements
        Collections.fill(list, 0);
        System.out.println("After fill: " + list);
        
        // 11. replaceAll() - Replace elements
        list = new ArrayList<>(Arrays.asList(1, 2, 3, 2, 4));
        Collections.replaceAll(list, 2, 99);
        System.out.println("After replaceAll: " + list);
        
        // 12. swap() - Swap elements
        Collections.swap(list, 0, 4);
        System.out.println("After swap: " + list);
        
        // 13. rotate() - Rotate elements
        Collections.rotate(list, 2);
        System.out.println("After rotate: " + list);
        
        // 14. disjoint() - Check if no common elements
        List<Integer> list2 = Arrays.asList(10, 20, 30);
        System.out.println("Disjoint: " + Collections.disjoint(list, list2));
        
        // 15. addAll() - Add multiple elements
        Collections.addAll(list, 100, 200, 300);
        System.out.println("After addAll: " + list);
        
        // 16. unmodifiableList() - Create immutable list
        List<Integer> immutableList = Collections.unmodifiableList(list);
        System.out.println("Immutable list: " + immutableList);
        // immutableList.add(500);  // Throws UnsupportedOperationException
        
        // 17. synchronizedList() - Thread-safe list
        List<Integer> syncList = Collections.synchronizedList(new ArrayList<>());
        syncList.add(1);
        syncList.add(2);
        System.out.println("Synchronized list: " + syncList);
        
        // 18. singleton() - Immutable set with one element
        Set<String> singletonSet = Collections.singleton("Only One");
        System.out.println("Singleton set: " + singletonSet);
        
        // 19. emptyList() / emptySet() / emptyMap()
        List<String> empty = Collections.emptyList();
        System.out.println("Empty list: " + empty);
    }
}
```

### Viva Questions

**Q1: What is Collections class?**
A: Utility class with static methods for operating on collections (sort, search, etc.).

**Q2: What is the difference between Collection and Collections?**
A: Collection is interface, Collections is utility class.

**Q3: How to sort a list?**
A: Using Collections.sort(list)

**Q4: How to make a list read-only?**
A: Using Collections.unmodifiableList(list)

**Q5: How to make a list thread-safe?**
A: Using Collections.synchronizedList(list)

---

## 9. Comparable and Comparator

### A. Comparable Interface

```java
import java.util.*;

class Student implements Comparable<Student> {
    int id;
    String name;
    int age;
    
    Student(int id, String name, int age) {
        this.id = id;
        this.name = name;
        this.age = age;
    }
    
    // Natural ordering by id
    @Override
    public int compareTo(Student other) {
        return this.id - other.id;
        // Positive: this > other
        // Negative: this < other
        // Zero: this == other
    }
    
    @Override
    public String toString() {
        return id + "-" + name + "-" + age;
    }
}

public class ComparableExample {
    public static void main(String[] args) {
        
        ArrayList<Student> students = new ArrayList<>();
        students.add(new Student(3, "Charlie", 22));
        students.add(new Student(1, "Alice", 20));
        students.add(new Student(2, "Bob", 21));
        
        System.out.println("Before sorting: " + students);
        
        Collections.sort(students);  // Uses compareTo()
        
        System.out.println("After sorting: " + students);
    }
}
```

### B. Comparator Interface

```java
import java.util.*;

class Employee {
    int id;
    String name;
    double salary;
    
    Employee(int id, String name, double salary) {
        this.id = id;
        this.name = name;
        this.salary = salary;
    }
    
    @Override
    public String toString() {
        return id + "-" + name + "-" + salary;
    }
}

// Comparator for sorting by name
class NameComparator implements Comparator<Employee> {
    @Override
    public int compare(Employee e1, Employee e2) {
        return e1.name.compareTo(e2.name);
    }
}

// Comparator for sorting by salary
class SalaryComparator implements Comparator<Employee> {
    @Override
    public int compare(Employee e1, Employee e2) {
        return Double.compare(e1.salary, e2.salary);
    }
}

public class ComparatorExample {
    public static void main(String[] args) {
        
        ArrayList<Employee> employees = new ArrayList<>();
        employees.add(new Employee(3, "Charlie", 50000));
        employees.add(new Employee(1, "Alice", 60000));
        employees.add(new Employee(2, "Bob", 55000));
        
        System.out.println("Original: " + employees);
        
        // Sort by name
        Collections.sort(employees, new NameComparator());
        System.out.println("Sorted by name: " + employees);
        
        // Sort by salary
        Collections.sort(employees, new SalaryComparator());
        System.out.println("Sorted by salary: " + employees);
        
        // Using anonymous class
        Collections.sort(employees, new Comparator<Employee>() {
            @Override
            public int compare(Employee e1, Employee e2) {
                return e1.id - e2.id;
            }
        });
        System.out.println("Sorted by id (anonymous): " + employees);
        
        // Using lambda expression (Java 8)
        Collections.sort(employees, (e1, e2) -> e1.name.compareTo(e2.name));
        System.out.println("Sorted by name (lambda): " + employees);
        
        // Using Comparator methods (Java 8)
        Collections.sort(employees, Comparator.comparing(e -> e.salary));
        System.out.println("Sorted by salary (method reference): " + employees);
        
        // Reverse order
        Collections.sort(employees, Comparator.comparing(Employee::getSalary).reversed());
    }
}
```

### Comparable vs Comparator

| Feature | Comparable | Comparator |
|---------|------------|------------|
| Package | java.lang | java.util |
| Method | compareTo(Object) | compare(Object, Object) |
| Sorting logic | Inside class (natural ordering) | Outside class (multiple orderings) |
| Modification | Modify original class | No need to modify class |
| Single/Multiple | Single sorting sequence | Multiple sorting sequences |
| Use case | Default sorting | Custom sorting |

### Viva Questions

**Q1: What is Comparable?**
A: Interface for defining natural ordering of objects (compareTo method).

**Q2: What is Comparator?**
A: Interface for defining custom ordering of objects (compare method).

**Q3: What is the difference between Comparable and Comparator?**
A: Comparable defines natural ordering (in class), Comparator defines custom ordering (outside class).

**Q4: Can we have multiple Comparators?**
A: Yes, we can create multiple Comparator implementations for different sorting criteria.

**Q5: Which package contains Comparable?**
A: java.lang package

**Q6: Which package contains Comparator?**
A: java.util package

---

## 10. Generics in Collections

### Generics Basics

```java
import java.util.*;

public class GenericsExample {
    public static void main(String[] args) {
        
        // Without generics (before Java 5) - Not type-safe
        ArrayList list1 = new ArrayList();
        list1.add("String");
        list1.add(10);  // Can add different types
        list1.add(20.5);
        
        // Need type casting and risk of ClassCastException
        String str = (String) list1.get(0);
        // String str2 = (String) list1.get(1);  // ClassCastException at runtime
        
        // With generics - Type-safe
        ArrayList<String> list2 = new ArrayList<>();
        list2.add("Java");
        list2.add("Python");
        // list2.add(10);  // Compile error - type safety
        
        String str2 = list2.get(0);  // No type casting needed
        
        // Multiple types
        HashMap<Integer, String> map = new HashMap<>();
        map.put(1, "One");
        map.put(2, "Two");
        
        // Wildcards
        ArrayList<? extends Number> numbers;  // Upper bound (Number and subclasses)
        numbers = new ArrayList<Integer>();
        numbers = new ArrayList<Double>();
        // numbers = new ArrayList<String>();  // Error
        
        ArrayList<? super Integer> superIntegers;  // Lower bound (Integer and superclasses)
        superIntegers = new ArrayList<Integer>();
        superIntegers = new ArrayList<Number>();
        superIntegers = new ArrayList<Object>();
    }
}
```

### Benefits of Generics
1. **Type Safety** - Compile-time type checking
2. **No Type Casting** - Automatic type casting
3. **Code Reusability** - Generic methods and classes
4. **Compile-time Errors** - Catch errors early

---

## Practice Programs

### 1. Find Duplicate Elements
```java
import java.util.*;

public class FindDuplicates {
    public static void main(String[] args) {
        ArrayList<Integer> list = new ArrayList<>(Arrays.asList(1, 2, 3, 2, 4, 5, 3, 6));
        
        Set<Integer> unique = new HashSet<>();
        Set<Integer> duplicates = new HashSet<>();
        
        for (Integer num : list) {
            if (!unique.add(num)) {
                duplicates.add(num);
            }
        }
        
        System.out.println("Duplicates: " + duplicates);
    }
}
```

### 2. Frequency of Elements
```java
import java.util.*;

public class FrequencyCount {
    public static void main(String[] args) {
        List<String> list = Arrays.asList("apple", "banana", "apple", "orange", "banana", "apple");
        
        Map<String, Integer> frequencyMap = new HashMap<>();
        
        for (String fruit : list) {
            frequencyMap.put(fruit, frequencyMap.getOrDefault(fruit, 0) + 1);
        }
        
        System.out.println("Frequency Map: " + frequencyMap);
    }
}
```

### 3. Remove Duplicates
```java
import java.util.*;

public class RemoveDuplicates {
    public static void main(String[] args) {
        ArrayList<Integer> list = new ArrayList<>(Arrays.asList(1, 2, 3, 2, 4, 5, 3, 6));
        
        // Method 1: Using Set
        Set<Integer> set = new LinkedHashSet<>(list);
        list = new ArrayList<>(set);
        System.out.println("After removing duplicates: " + list);
        
        // Method 2: Using Stream (Java 8)
        // list = list.stream().distinct().collect(Collectors.toList());
    }
}
```

---

## Summary

### Key Concepts Covered:
✅ Collections Framework Overview
✅ List (ArrayList, LinkedList, Vector)
✅ Set (HashSet, LinkedHashSet, TreeSet)
✅ Queue (PriorityQueue, Deque)
✅ Map (HashMap, LinkedHashMap, TreeMap, Hashtable)
✅ Iterator and ListIterator
✅ Collections Utility Class
✅ Comparable and Comparator
✅ Generics

### Quick Reference:

| Need | Use |
|------|-----|
| Unique elements | HashSet |
| Unique + sorted | TreeSet |
| Unique + order | LinkedHashSet |
| Indexed access | ArrayList |
| Fast insert/delete | LinkedList |
| Key-value pairs | HashMap |
| Sorted keys | TreeMap |
| FIFO queue | LinkedList |
| Priority queue | PriorityQueue |
| Thread-safe | Vector, Hashtable, or Collections.synchronized* |

### Important Points:
- List allows duplicates, Set doesn't
- Map stores key-value pairs (keys are unique)
- ArrayList is faster for access, LinkedList for insertion/deletion
- HashSet is unordered, TreeSet is sorted, LinkedHashSet maintains order
- Use Comparable for natural ordering, Comparator for custom ordering
- Generics provide type safety and eliminate casting

---
**End of Java4.md**

**Congratulations!** You now have complete coverage of Java Core concepts across 4 comprehensive README files!
