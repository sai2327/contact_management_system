# Contact Management System - PART 1: Foundation Setup

## 📋 Project Introduction

Welcome to **PART 1** of the Contact Management System reconstruction guide. This part establishes the **foundational architecture** that the entire application will be built upon.

In PART 1, we build:
- **Database schema** with soft delete support
- **Layered project structure** for maintainability
- **Database connection utility** for JDBC operations
- **Contact model class** representing business entities
- **DAO interface** defining the contract for data operations

This foundation is critical because:
- It establishes **separation of concerns** between layers
- It defines **data persistence strategy** early
- It creates **contracts** that prevent tight coupling
- It enables **scalability** and **testability** from day one

Without this foundation, the application would become a tangled mess of mixed responsibilities, making it impossible to maintain or extend.

---

## 🗄️ PHASE 1 — Database Design

### SQL Script

```sql
-- ============================================
-- CONTACT MANAGEMENT SYSTEM - DATABASE SETUP
-- ============================================

-- Step 1: Create the database
-- This creates a dedicated schema for our application
CREATE DATABASE IF NOT EXISTS contact_management;

-- Step 2: Switch to the database we just created
-- All subsequent commands will execute in this database
USE contact_management;

-- Step 3: Create the contacts table
-- This table stores all contact information with soft delete support
CREATE TABLE IF NOT EXISTS contacts (
    -- Primary key: Auto-incrementing unique identifier for each contact
    -- INT: Efficient for indexing and foreign key relationships
    -- AUTO_INCREMENT: Database handles ID generation automatically
    -- PRIMARY KEY: Ensures uniqueness and creates clustered index
    id INT AUTO_INCREMENT PRIMARY KEY,
    
    -- Contact's full name
    -- VARCHAR(100): Allows up to 100 characters, efficient storage
    -- NOT NULL: Business rule - every contact must have a name
    name VARCHAR(100) NOT NULL,
    
    -- Phone number
    -- VARCHAR(20): Handles international formats, country codes, extensions
    -- NOT NULL: Contact must have at least one communication method
    phone VARCHAR(20) NOT NULL,
    
    -- Email address
    -- VARCHAR(100): Standard length for email addresses
    -- NULL allowed: Not all contacts may have email
    email VARCHAR(100),
    
    -- Physical address
    -- VARCHAR(255): Longer field for complete address including city, state, zip
    -- NULL allowed: Optional field
    address VARCHAR(255),
    
    -- Contact category/group (e.g., "Family", "Work", "Friend")
    -- VARCHAR(50): Short categorization field
    -- NULL allowed: Contact may not be categorized
    category VARCHAR(50),
    
    -- Additional notes or comments about the contact
    -- TEXT: Large text field for detailed information
    -- NULL allowed: Optional field
    notes TEXT,
    
    -- Soft delete flag: Marks contact as deleted without removing from database
    -- BOOLEAN (TINYINT(1)): 0 = active, 1 = deleted
    -- DEFAULT 0: New contacts are active by default
    -- NOT NULL: Every record must have a delete status
    is_deleted BOOLEAN DEFAULT 0 NOT NULL,
    
    -- Timestamp when contact was created
    -- DATETIME: Stores date and time
    -- DEFAULT CURRENT_TIMESTAMP: Automatically set on insertion
    -- NOT NULL: Every record must have creation time
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
    
    -- Timestamp when contact was last modified
    -- DATETIME: Stores date and time
    -- DEFAULT CURRENT_TIMESTAMP: Set on insertion
    -- ON UPDATE CURRENT_TIMESTAMP: Auto-updates whenever row is modified
    -- NOT NULL: Every record must track last update
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL
);

-- Step 4: Create index on name column for fast searching
-- Searching contacts by name is the most common operation
-- Without this index, database would scan entire table (slow on large datasets)
-- With this index, searches become logarithmic O(log n) instead of linear O(n)
CREATE INDEX idx_contacts_name ON contacts(name);

-- Step 5: Create index on category column for filtering
-- Users often filter contacts by category (e.g., "Show all Work contacts")
-- This index speeds up category-based queries significantly
CREATE INDEX idx_contacts_category ON contacts(category);

-- Step 6: Create composite index on is_deleted column
-- Most queries want to show only active contacts (is_deleted = 0)
-- This index allows database to quickly filter out deleted contacts
-- Also used for "Recycle Bin" feature to show only deleted contacts
CREATE INDEX idx_contacts_deleted ON contacts(is_deleted);
```

### Database Design Explanation

#### **Why This Database Structure?**

**1. Primary Key Strategy**
- We use `INT AUTO_INCREMENT` for the primary key because:
  - **Performance**: Integer comparisons are faster than string comparisons
  - **Storage**: 4 bytes per ID vs 36 bytes for UUID
  - **Simplicity**: Database handles generation automatically
  - **Indexing**: Integers create more efficient B-tree indexes

**2. Column Design Decisions**

- **`name VARCHAR(100) NOT NULL`**
  - Every contact MUST have a name (business rule)
  - 100 characters handles most real-world names including long international names
  - NOT NULL prevents invalid data at database level

- **`phone VARCHAR(20) NOT NULL`**
  - VARCHAR instead of numeric type because phones contain +, -, (), spaces
  - 20 characters handles international formats like "+1 (555) 123-4567 ext 890"
  - NOT NULL ensures at least one contact method exists

- **`email VARCHAR(100)`**
  - Nullable because not everyone has email
  - 100 characters is standard for email addresses
  - Could add CHECK constraint for validation (future enhancement)

- **`address VARCHAR(255)`**
  - Nullable - many contacts may not need address
  - 255 characters accommodates full address: street, city, state, zip, country
  - Could be normalized into separate table (future enhancement for large systems)

- **`category VARCHAR(50)`**
  - Allows user-defined categories without rigid structure
  - Flexible approach: users can create any category
  - Alternative would be foreign key to categories table (more rigid but normalized)

- **`notes TEXT`**
  - TEXT type allows unlimited length (up to 64KB)
  - Nullable - most contacts won't need notes
  - Useful for extra information that doesn't fit other fields

**3. Soft Delete Strategy**

- **`is_deleted BOOLEAN DEFAULT 0`**
  - **Why soft delete?** Hard deletes are dangerous:
    - Cannot undo accidental deletions
    - Breaks audit trails
    - Loses historical data
    - May violate compliance requirements
  - **How it works:**
    - 0 = Contact is active
    - 1 = Contact is "deleted" (moved to recycle bin)
  - **Benefits:**
    - Recycle bin feature becomes trivial to implement
    - Can restore deleted contacts
    - Can permanently delete later if needed
    - Maintains referential integrity if other tables reference contacts

**4. Timestamp Tracking**

- **`created_at DATETIME DEFAULT CURRENT_TIMESTAMP`**
  - Automatically records when contact was added
  - Useful for audit trails
  - Enables features like "Recently Added" views
  - Cannot be modified (no ON UPDATE clause)

- **`updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP`**
  - Automatically tracks last modification
  - Updates on ANY column change
  - Enables features like "Recently Modified" views
  - Useful for synchronization and conflict resolution

**5. Indexing Strategy**

- **`idx_contacts_name`**
  - **Most common operation**: Searching contacts by name
  - Without index: O(n) - full table scan
  - With index: O(log n) - binary search through B-tree
  - **Example**: Finding "John" in 100,000 contacts takes <10 comparisons instead of 50,000 average

- **`idx_contacts_category`**
  - **Common filter**: "Show me all Family contacts"
  - Enables fast filtering without scanning entire table
  - **Example**: Finding all "Work" contacts in 100,000 records is nearly instant

- **`idx_contacts_deleted`**
  - **Critical for soft delete**: Most queries want `WHERE is_deleted = 0`
  - Without index: Every query scans all rows (including deleted ones)
  - With index: Database instantly filters to active contacts only
  - **Also used for Recycle Bin**: `WHERE is_deleted = 1` is equally fast

**6. Scalability Considerations**

This database design scales well because:
- **Indexed searches**: Remain fast even with millions of contacts
- **Integer primary key**: Efficient for joins if system expands
- **Timestamp tracking**: Enables pagination and sync features
- **Soft deletes**: No need for deleted_contacts backup table
- **Flexible categories**: No rigid structure to migrate later

**What Would Break If We Skip This?**

- No indexes → Application becomes unusably slow with large datasets
- No soft delete → Cannot implement recycle bin or undo features
- No timestamps → Cannot track contact history or modifications
- No PRIMARY KEY → Cannot uniquely identify contacts, joins fail
- No NOT NULL constraints → Invalid data enters system, causing crashes

---

## 📁 PHASE 2 — Project Folder Structure

### Folder Layout

```
contact-management/
├── src/
│   ├── model/              ← Data classes (Contact.java)
│   ├── dao/                ← Data Access Objects (database layer)
│   ├── service/            ← Business logic layer
│   ├── util/               ← Utility classes (DBConnection.java)
│   ├── ui/                 ← User interface components
│   └── Main.java           ← Application entry point
├── lib/                    ← External JAR files (MySQL connector)
└── bin/                    ← Compiled .class files (generated)
```

### Layered Architecture Explanation

#### **Why This Structure?**

**1. Separation of Concerns**

Each package has a **single responsibility**:

- **`model/`** - Represents business entities (Contact)
  - Contains ONLY data and basic validation
  - No database code
  - No UI code
  - Pure Java beans

- **`dao/`** - Handles ALL database operations
  - SQL queries live here
  - JDBC code lives here
  - Converts between ResultSet and Model objects
  - No UI code
  - No business logic

- **`service/`** - Contains business rules
  - Coordinates between DAO and UI
  - Implements business logic (e.g., "Cannot delete contact with pending orders")
  - Transaction management
  - No direct JDBC
  - No direct UI components

- **`util/`** - Reusable utility classes
  - Database connection management
  - Configuration loaders
  - Validators
  - Helper functions

- **`ui/`** - User interface components
  - Swing/JavaFX components
  - Event handlers
  - View rendering
  - No direct database access
  - Calls service layer only

#### **2. Why Layered Architecture Matters**

**Without layers** (everything in one file):
```java
// BAD: Mixed responsibilities
public class ContactManager extends JFrame {
    // UI code
    JButton saveButton = new JButton("Save");
    
    // Direct database code
    Connection conn = DriverManager.getConnection(...);
    
    // Business logic
    if (name.isEmpty()) { ... }
    
    // SQL in UI event handler
    saveButton.addActionListener(e -> {
        PreparedStatement pst = conn.prepareStatement("INSERT INTO...");
        // Nightmare to maintain, test, or modify
    });
}
```

**Problems with mixed code:**
- Cannot change database without breaking UI
- Cannot test business logic without launching UI
- Cannot reuse components in different contexts
- Cannot work in teams (everyone edits same file)
- Cannot replace Swing with web UI later

**With layers** (proper architecture):
```java
// UI only knows about Service
public class ContactUI extends JFrame {
    private ContactService service = new ContactService();
    
    saveButton.addActionListener(e -> {
        service.addContact(contact); // Clean, testable
    });
}

// Service only knows about DAO
public class ContactService {
    private ContactDAO dao = new ContactDAOImpl();
    
    public void addContact(Contact contact) {
        // Business validation
        if (isValid(contact)) {
            dao.addContact(contact); // Delegates to DAO
        }
    }
}

// DAO only knows about database
public class ContactDAOImpl implements ContactDAO {
    public void addContact(Contact contact) {
        // Pure SQL logic
        String sql = "INSERT INTO contacts...";
        // No UI, no business rules
    }
}
```

**Benefits of layered approach:**
- **Maintainability**: Change database without touching UI
- **Testability**: Test each layer independently
- **Reusability**: Use same DAO in desktop, web, or mobile app
- **Team collaboration**: Different developers work on different layers
- **Flexibility**: Replace MySQL with PostgreSQL by changing DAO only

#### **3. What Would Break If Layers Are Mixed?**

- **Cannot unit test**: Tests require database AND UI to run
- **Cannot switch UI framework**: Database code embedded in Swing components
- **Cannot switch database**: SQL scattered throughout UI code
- **Cannot reuse logic**: Business rules duplicated in multiple places
- **Cannot scale team**: Everyone conflicts on same files

---

## 🔌 PHASE 3 — DBConnection Utility

### File: `util/DBConnection.java`

```java
package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * DBConnection Utility Class
 * 
 * Purpose: Centralized database connection management
 * 
 * This class provides a single point of control for database connections.
 * Instead of scattering connection logic throughout the application,
 * all classes use this utility to get connections consistently.
 * 
 * Key Design: Static method that returns NEW connection each time
 * (Connection pooling can be added later without changing calling code)
 */
public class DBConnection {
    
    // JDBC URL format: jdbc:mysql://hostname:port/database_name
    // localhost:3306 is default MySQL server location
    // contact_management is our database name
    // useSSL=false: Disables SSL for local development (enable in production)
    // serverTimezone=UTC: Handles timezone consistency across environments
    private static final String URL = "jdbc:mysql://localhost:3306/contact_management?useSSL=false&serverTimezone=UTC";
    
    // Database credentials
    // In production, these should come from environment variables or config file
    // Never hardcode credentials in production code
    private static final String USER = "root";
    private static final String PASSWORD = "root";
    
    /**
     * Get a new database connection
     * 
     * This method creates and returns a NEW connection each time it's called.
     * Caller is responsible for closing the connection after use.
     * 
     * @return Connection object connected to the database
     * @throws SQLException if connection fails (wrong credentials, database down, etc.)
     */
    public static Connection getConnection() throws SQLException {
        // DriverManager.getConnection() does three things:
        // 1. Loads JDBC driver (MySQL Connector/J)
        // 2. Establishes TCP connection to MySQL server
        // 3. Authenticates using provided credentials
        // 4. Returns Connection object representing the session
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
```

### DBConnection Explanation

#### **Purpose of This Class**

The `DBConnection` class is a **utility** that centralizes database connection logic. Every part of the application that needs database access will call `DBConnection.getConnection()`.

#### **Why Not Store a Global Connection?**

**Anti-pattern (BAD approach):**
```java
// BAD: Global static connection
public class DBConnection {
    private static Connection conn = null;
    
    public static Connection getConnection() {
        if (conn == null) {
            conn = DriverManager.getConnection(...);
        }
        return conn; // Always returns same connection
    }
}
```

**Problems with global connection:**
- **Thread safety**: Multiple threads using same connection causes corruption
- **Connection timeout**: If connection sits idle too long, it closes automatically
- **Error recovery**: If connection breaks, entire app breaks
- **Transaction isolation**: All operations share same transaction state
- **Resource leak**: Connection never closes, holds database resources

**Our approach (GOOD):**
```java
// GOOD: New connection each time
public static Connection getConnection() throws SQLException {
    return DriverManager.getConnection(URL, USER, PASSWORD);
}
```

**Benefits:**
- **Thread safe**: Each thread gets its own connection
- **Fresh connection**: No stale connection issues
- **Error isolation**: One failure doesn't break all operations
- **Transaction control**: Each operation has independent transaction
- **Proper cleanup**: Each connection can be closed after use

#### **Line-by-Line Code Explanation**

**1. JDBC URL Construction**
```java
private static final String URL = "jdbc:mysql://localhost:3306/contact_management?useSSL=false&serverTimezone=UTC";
```
- **`jdbc:mysql://`** - Protocol prefix identifying MySQL JDBC driver
- **`localhost:3306`** - MySQL server location (3306 is default port)
- **`contact_management`** - Database name we created in PHASE 1
- **`useSSL=false`** - Disables SSL encryption (OK for local dev, enable in production)
- **`serverTimezone=UTC`** - Prevents timezone conversion warnings/errors

**2. Credentials**
```java
private static final String USER = "root";
private static final String PASSWORD = "root";
```
- Hardcoded for development convenience
- **Production approach**: Load from environment variables or config file
- **Security**: Never commit real credentials to version control

**3. Connection Method**
```java
public static Connection getConnection() throws SQLException {
    return DriverManager.getConnection(URL, USER, PASSWORD);
}
```
- **`static`** - No need to instantiate DBConnection, call directly
- **`throws SQLException`** - Caller must handle connection failures
- **`DriverManager.getConnection()`** - JDBC API that:
  - Loads driver class (mysql-connector-j JAR)
  - Opens TCP socket to database server
  - Sends authentication credentials
  - Receives session token
  - Returns Connection object

#### **How This Connects to Other Layers**

**DAO Layer uses DBConnection:**
```java
public class ContactDAOImpl implements ContactDAO {
    @Override
    public void addContact(Contact contact) {
        // Get connection from utility
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "INSERT INTO contacts...";
            PreparedStatement pst = conn.prepareStatement(sql);
            // Execute query
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
```

**Benefits of centralization:**
- Change database URL once, affects entire app
- Switch from MySQL to PostgreSQL by changing only DBConnection
- Add connection pooling later without changing DAO code
- Add logging/monitoring at single point

#### **Why Try-With-Resources Will Be Important**

When we implement DAO layer, we'll use try-with-resources:
```java
try (Connection conn = DBConnection.getConnection()) {
    // Use connection
} // Automatically closes connection here
```

**Without try-with-resources:**
```java
Connection conn = null;
try {
    conn = DBConnection.getConnection();
    // Use connection
} finally {
    if (conn != null) conn.close(); // Manual cleanup
}
```

**Why this matters:**
- Connections are expensive resources
- MySQL limits concurrent connections (default ~150)
- Unclosed connections cause connection pool exhaustion
- Try-with-resources guarantees cleanup even if exception occurs

#### **What Would Break Without This Class?**

- Every DAO would duplicate connection logic
- Changing database URL requires editing 20+ files
- No single point to add connection pooling
- No centralized error handling
- Testing becomes harder (cannot mock connection source)

---

## 🏗️ PHASE 4 — Contact Model Class

### File: `model/Contact.java`

```java
package model;

import java.time.LocalDateTime;

/**
 * Contact Model Class
 * 
 * Purpose: Represents a single contact entity in the application
 * 
 * This is a POJO (Plain Old Java Object) that:
 * 1. Holds contact data in memory
 * 2. Provides getter/setter access to fields
 * 3. Maps directly to database table structure
 * 
 * Design principle: No business logic, no database code, just data
 */
public class Contact {
    
    // ==================== FIELDS ====================
    // These fields match the columns in the 'contacts' database table
    
    // Unique identifier (matches database PRIMARY KEY)
    // null for new contacts (database auto-generates)
    // non-null after saving to database
    private Integer id;
    
    // Contact's full name
    // Required field (database has NOT NULL constraint)
    private String name;
    
    // Contact's phone number
    // Required field (database has NOT NULL constraint)
    private String phone;
    
    // Contact's email address
    // Optional field (database allows NULL)
    private String email;
    
    // Contact's physical address
    // Optional field (database allows NULL)
    private String address;
    
    // Contact category (e.g., "Family", "Work", "Friend")
    // Optional field (database allows NULL)
    private String category;
    
    // Additional notes about the contact
    // Optional field (database allows NULL)
    private String notes;
    
    // Soft delete flag
    // false = active contact
    // true = deleted (in recycle bin)
    // Matches database is_deleted column
    private boolean isDeleted;
    
    // Timestamp when contact was created
    // Automatically set by database on INSERT
    // LocalDateTime is Java 8+ replacement for java.util.Date
    private LocalDateTime createdAt;
    
    // Timestamp when contact was last updated
    // Automatically updated by database on UPDATE
    private LocalDateTime updatedAt;
    
    // ==================== CONSTRUCTORS ====================
    
    /**
     * Default Constructor
     * 
     * Used when creating a brand new contact (not yet in database)
     * All fields are null/default values
     * 
     * Usage: Contact contact = new Contact();
     */
    public Contact() {
        // Empty constructor - fields are null/default
        // This is required for frameworks that use reflection
        // (e.g., JSON deserialization, some ORM tools)
    }
    
    /**
     * Constructor for New Contact (before database save)
     * 
     * Used when user fills out form to create contact
     * No ID yet (database will generate it)
     * No timestamps yet (database will generate them)
     * isDeleted defaults to false
     * 
     * @param name Contact's name (required)
     * @param phone Contact's phone (required)
     * @param email Contact's email (optional)
     * @param address Contact's address (optional)
     * @param category Contact's category (optional)
     * @param notes Additional notes (optional)
     */
    public Contact(String name, String phone, String email, String address, String category, String notes) {
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.category = category;
        this.notes = notes;
        this.isDeleted = false; // New contacts are active by default
        // id, createdAt, updatedAt are null until saved to database
    }
    
    /**
     * Full Constructor (for loading from database)
     * 
     * Used when retrieving contacts from database
     * All fields including ID and timestamps are populated
     * 
     * @param id Database primary key
     * @param name Contact's name
     * @param phone Contact's phone
     * @param email Contact's email
     * @param address Contact's address
     * @param category Contact's category
     * @param notes Additional notes
     * @param isDeleted Soft delete flag
     * @param createdAt Creation timestamp
     * @param updatedAt Last update timestamp
     */
    public Contact(Integer id, String name, String phone, String email, String address, 
                   String category, String notes, boolean isDeleted, 
                   LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.category = category;
        this.notes = notes;
        this.isDeleted = isDeleted;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    // ==================== GETTERS ====================
    // Getters provide READ access to private fields
    // This is encapsulation: controlled access to data
    
    public Integer getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public String getEmail() {
        return email;
    }
    
    public String getAddress() {
        return address;
    }
    
    public String getCategory() {
        return category;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public boolean isDeleted() {
        // Boolean getter convention: is___() instead of get___()
        return isDeleted;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    // ==================== SETTERS ====================
    // Setters provide WRITE access to private fields
    // Could add validation logic here if needed
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    // ==================== HELPER METHODS ====================
    
    /**
     * String representation of Contact
     * Useful for debugging and logging
     */
    @Override
    public String toString() {
        return "Contact{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", phone='" + phone + '\'' +
                ", email='" + email + '\'' +
                ", category='" + category + '\'' +
                ", isDeleted=" + isDeleted +
                '}';
    }
}
```

### Contact Model Explanation

#### **Purpose of Model Class**

The `Contact` class is a **data container** that represents a single contact throughout the application. It serves as the **common language** between all layers:

- **Database layer**: DAO converts ResultSet → Contact object
- **Service layer**: Business logic operates on Contact objects
- **UI layer**: Forms and tables display Contact objects

**Without model class**, we'd pass raw data everywhere:
```java
// BAD: Passing individual fields
public void saveContact(String name, String phone, String email, String address, ...) {
    // Nightmare: 10+ parameters
}

// Or raw database structures
public void displayContact(ResultSet rs) {
    // UI directly depends on database structure
}
```

**With model class**:
```java
// GOOD: Passing single object
public void saveContact(Contact contact) {
    // Clean, type-safe, maintainable
}
```

#### **Field Design Decisions**

**1. Why `Integer id` instead of `int id`?**
```java
private Integer id; // Can be null
```
- **`Integer`** (object) can be `null`
- **`int`** (primitive) cannot be null, defaults to 0
- Before saving to database, `id` doesn't exist yet → must be null
- After saving, database generates ID → becomes non-null
- Null vs non-null tells us if contact exists in database

**2. Why `boolean isDeleted` instead of separate deleted contacts?**
```java
private boolean isDeleted;
```
- **Soft delete pattern**: Marking as deleted instead of removing
- Same Contact object represents active or deleted state
- No need for separate DeletedContact class
- Easy to restore: just flip boolean back to false

**3. Why `LocalDateTime` instead of `Date` or `Timestamp`?**
```java
private LocalDateTime createdAt;
private LocalDateTime updatedAt;
```
- **`LocalDateTime`** is Java 8+ modern date/time API
- More intuitive than old `java.util.Date`
- Immutable (thread-safe)
- Better formatting and parsing methods
- Converts cleanly to/from SQL DATETIME
- No timezone confusion (stored as local time)

**Comparison:**
```java
// Old way (java.util.Date)
Date date = new Date(); // Mutable, confusing
SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd"); // Separate formatter needed

// Modern way (LocalDateTime)
LocalDateTime date = LocalDateTime.now(); // Immutable, clear
String formatted = date.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME); // Built-in
```

#### **Constructor Explanation**

**Why Three Constructors?**

**1. Default Constructor (No arguments)**
```java
public Contact() { }
```
- Required by many frameworks (JSON parsers, ORMs)
- Used when creating empty contact, then filling fields with setters
- **Usage**: `Contact contact = new Contact();`

**2. User Input Constructor (6 fields)**
```java
public Contact(String name, String phone, String email, String address, String category, String notes)
```
- Used when user creates NEW contact via UI form
- No ID (database will generate)
- No timestamps (database will generate)
- `isDeleted` defaults to false
- **Usage**: Saves typing when collecting form data
```java
Contact contact = new Contact(
    nameField.getText(),
    phoneField.getText(),
    emailField.getText(),
    addressField.getText(),
    categoryField.getText(),
    notesField.getText()
);
contactService.add(contact); // ID assigned after saving
```

**3. Database Constructor (All 10 fields)**
```java
public Contact(Integer id, String name, ..., LocalDateTime createdAt, LocalDateTime updatedAt)
```
- Used when loading contacts FROM database
- All fields populated including ID and timestamps
- DAO layer uses this when converting ResultSet to Contact
- **Usage**:
```java
// In DAO layer
ResultSet rs = executeQuery("SELECT * FROM contacts WHERE id = 5");
if (rs.next()) {
    Contact contact = new Contact(
        rs.getInt("id"),
        rs.getString("name"),
        rs.getString("phone"),
        rs.getString("email"),
        rs.getString("address"),
        rs.getString("category"),
        rs.getString("notes"),
        rs.getBoolean("is_deleted"),
        rs.getTimestamp("created_at").toLocalDateTime(),
        rs.getTimestamp("updated_at").toLocalDateTime()
    );
    return contact; // Fully populated
}
```

#### **Why Getters and Setters?**

**Why not public fields?**
```java
// BAD: Public fields
public class Contact {
    public String name; // Direct access
    public String phone; // No control
}

Contact c = new Contact();
c.name = ""; // Invalid data - no validation possible
c.phone = null; // Breaks database NOT NULL constraint
```

**GOOD: Private fields with getters/setters**
```java
public class Contact {
    private String name; // Encapsulated
    
    public void setName(String name) {
        // Could add validation here
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be empty");
        }
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
}
```

**Benefits of encapsulation:**
- **Validation**: Can validate in setters before accepting data
- **Flexibility**: Can change internal representation without breaking callers
- **Debugging**: Can add breakpoints in getters/setters to track changes
- **Read-only fields**: Can provide getter without setter (immutable fields)

**Example - ID should not be changeable by UI:**
```java
// In future, could remove setId() to make ID read-only after creation
// Only DAO layer (trusted code) would set ID via constructor
```

#### **Why No Business Logic in Model?**

The Contact class is a **pure data object**. It should NOT contain:
- Database queries
- UI rendering logic
- Business validation rules
- File I/O operations

**Anti-pattern (BAD):**
```java
public class Contact {
    private String name;
    
    public void saveToDatabase() { // WRONG
        Connection conn = DriverManager.getConnection(...);
        // Model should not know about database
    }
    
    public void displayInUI() { // WRONG
        JLabel label = new JLabel(name);
        // Model should not know about UI
    }
    
    public boolean isValidForSaving() { // WRONG
        // Business rules should be in Service layer
        return name != null && phone != null;
    }
}
```

**Correct approach:**
```java
// Model: Only data
public class Contact {
    private String name;
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}

// DAO: Database operations
public class ContactDAO {
    public void save(Contact contact) { /* SQL here */ }
}

// Service: Business rules
public class ContactService {
    public boolean isValid(Contact contact) { /* Validation here */ }
}

// UI: Display logic
public class ContactUI {
    public void display(Contact contact) { /* Swing components here */ }
}
```

#### **How Model Connects to Other Layers**

```
┌─────────────┐
│     UI      │ ← User sees form
└──────┬──────┘
       │ Creates Contact object from form data
       ↓
┌─────────────┐
│   Service   │ ← Validates Contact
└──────┬──────┘
       │ Passes Contact to DAO
       ↓
┌─────────────┐
│     DAO     │ ← Converts Contact to SQL INSERT
└──────┬──────┘
       │ Executes SQL
       ↓
┌─────────────┐
│  Database   │ ← Stores as table row
└─────────────┘

Then reverse for retrieval:
Database row → ResultSet → Contact object → Service → UI
```

#### **What Would Break Without Model Class?**

- **No type safety**: Passing data as String arrays or HashMaps
- **No IDE support**: No autocomplete, refactoring tools useless
- **Duplication**: Every layer creates its own data structure
- **Tight coupling**: UI depends directly on database ResultSet structure
- **Hard to test**: Cannot create mock data easily

---

## 🔧 PHASE 5 — DAO Interface

### File: `dao/ContactDAO.java`

```java
package dao;

import model.Contact;
import java.sql.SQLException;
import java.util.List;

/**
 * ContactDAO Interface
 * 
 * Purpose: Defines the contract for Contact data access operations
 * 
 * This is an INTERFACE (not a class) that specifies WHAT operations
 * are available for contact data, but not HOW they are implemented.
 * 
 * Key principle: Programming to interface, not implementation
 * 
 * Benefits:
 * - Service layer depends on this interface, not on specific database code
 * - Can swap implementations (MySQL, PostgreSQL, MongoDB, in-memory)
 * - Easy to mock for testing
 * - Clear contract between layers
 */
public interface ContactDAO {
    
    /**
     * Add a new contact to the database
     * 
     * Takes a Contact object and inserts it into the database.
     * After insertion, the contact's ID field should be updated
     * with the auto-generated primary key.
     * 
     * @param contact The contact to add (must not be null)
     * @throws SQLException if database operation fails
     * 
     * Usage: contactDAO.addContact(newContact);
     * 
     * Preconditions:
     * - contact.name must not be null
     * - contact.phone must not be null
     * 
     * Postconditions:
     * - Contact is stored in database
     * - contact.id is populated with generated ID
     * - contact.createdAt and updatedAt are set by database
     */
    void addContact(Contact contact) throws SQLException;
    
    /**
     * Update an existing contact in the database
     * 
     * Takes a Contact object with existing ID and updates
     * the corresponding database row with new field values.
     * 
     * @param contact The contact to update (must have valid id)
     * @throws SQLException if database operation fails
     * 
     * Usage: contactDAO.updateContact(modifiedContact);
     * 
     * Preconditions:
     * - contact.id must not be null
     * - Contact with this ID must exist in database
     * 
     * Postconditions:
     * - Database row is updated with new values
     * - updated_at timestamp is automatically refreshed by database
     */
    void updateContact(Contact contact) throws SQLException;
    
    /**
     * Soft delete a contact (move to recycle bin)
     * 
     * Marks the contact as deleted by setting is_deleted = true.
     * Does NOT physically remove the row from database.
     * Contact can be restored later using restore() method.
     * 
     * @param id The ID of the contact to soft delete
     * @throws SQLException if database operation fails
     * 
     * Usage: contactDAO.softDelete(5);
     * 
     * Preconditions:
     * - Contact with this ID must exist
     * - Contact must not already be deleted
     * 
     * Postconditions:
     * - Contact's is_deleted flag is set to true (1)
     * - Contact still exists in database
     * - Contact will not appear in getAll() results
     * - Contact will appear in getDeleted() results
     */
    void softDelete(int id) throws SQLException;
    
    /**
     * Restore a soft-deleted contact
     * 
     * Marks the contact as active by setting is_deleted = false.
     * Recovers contact from recycle bin.
     * 
     * @param id The ID of the contact to restore
     * @throws SQLException if database operation fails
     * 
     * Usage: contactDAO.restore(5);
     * 
     * Preconditions:
     * - Contact with this ID must exist
     * - Contact must be currently deleted (is_deleted = true)
     * 
     * Postconditions:
     * - Contact's is_deleted flag is set to false (0)
     * - Contact appears in getAll() results
     * - Contact removed from getDeleted() results
     */
    void restore(int id) throws SQLException;
    
    /**
     * Permanently delete a contact from database
     * 
     * Physically removes the row from database. This is irreversible.
     * Use with caution - usually called from "Empty Recycle Bin" feature.
     * 
     * @param id The ID of the contact to permanently delete
     * @throws SQLException if database operation fails
     * 
     * Usage: contactDAO.hardDelete(5);
     * 
     * Preconditions:
     * - Contact with this ID must exist
     * - Typically only called on already soft-deleted contacts
     * 
     * Postconditions:
     * - Contact row is permanently removed from database
     * - Cannot be recovered
     */
    void hardDelete(int id) throws SQLException;
    
    /**
     * Retrieve a single contact by ID
     * 
     * Fetches one contact from database using primary key lookup.
     * 
     * @param id The ID of the contact to retrieve
     * @return Contact object if found, null if not found
     * @throws SQLException if database operation fails
     * 
     * Usage: Contact contact = contactDAO.getById(5);
     * 
     * Preconditions:
     * - id must be positive integer
     * 
     * Postconditions:
     * - Returns Contact object with all fields populated if found
     * - Returns null if no contact with this ID exists
     * - Includes soft-deleted contacts (check isDeleted flag)
     */
    Contact getById(int id) throws SQLException;
    
    /**
     * Retrieve all active (non-deleted) contacts
     * 
     * Fetches all contacts where is_deleted = false.
     * This is the default view shown to users.
     * 
     * @return List of active contacts (empty list if none found)
     * @throws SQLException if database operation fails
     * 
     * Usage: List<Contact> contacts = contactDAO.getAll();
     * 
     * Postconditions:
     * - Returns List<Contact> (never null, may be empty)
     * - Only includes contacts where is_deleted = false
     * - Sorted by name (or ID, depending on implementation)
     */
    List<Contact> getAll() throws SQLException;
    
    /**
     * Retrieve all deleted contacts (recycle bin)
     * 
     * Fetches all contacts where is_deleted = true.
     * Used for "Recycle Bin" or "Trash" view.
     * 
     * @return List of deleted contacts (empty list if none found)
     * @throws SQLException if database operation fails
     * 
     * Usage: List<Contact> deletedContacts = contactDAO.getDeleted();
     * 
     * Postconditions:
     * - Returns List<Contact> (never null, may be empty)
     * - Only includes contacts where is_deleted = true
     * - Typically sorted by updated_at descending (recently deleted first)
     */
    List<Contact> getDeleted() throws SQLException;
    
    /**
     * Search for contacts by name (partial match)
     * 
     * Searches for contacts whose name contains the search term.
     * Case-insensitive search. Only returns active contacts.
     * 
     * @param searchTerm The text to search for in contact names
     * @return List of matching contacts (empty list if none found)
     * @throws SQLException if database operation fails
     * 
     * Usage: List<Contact> results = contactDAO.searchByName("john");
     * 
     * Preconditions:
     * - searchTerm should not be null (empty string returns all contacts)
     * 
     * Postconditions:
     * - Returns List<Contact> (never null, may be empty)
     * - Only includes contacts where name contains searchTerm
     * - Only includes active contacts (is_deleted = false)
     * - Case-insensitive: "john" matches "John", "Johnny", "Johnson"
     */
    List<Contact> searchByName(String searchTerm) throws SQLException;
    
    /**
     * Get contacts filtered by category
     * 
     * Retrieves all active contacts belonging to a specific category.
     * 
     * @param category The category to filter by (e.g., "Family", "Work")
     * @return List of contacts in this category (empty list if none found)
     * @throws SQLException if database operation fails
     * 
     * Usage: List<Contact> workContacts = contactDAO.getByCategory("Work");
     * 
     * Preconditions:
     * - category should not be null
     * 
     * Postconditions:
     * - Returns List<Contact> (never null, may be empty)
     * - Only includes contacts where category matches exactly
     * - Only includes active contacts (is_deleted = false)
     * - Case-sensitive: "Work" != "work"
     */
    List<Contact> getByCategory(String category) throws SQLException;
}
```

### DAO Interface Explanation

#### **Why Use an Interface?**

An **interface** defines a **contract** without implementation. Think of it as a blueprint that says "any ContactDAO must provide these methods."

**Without interface** (direct implementation):
```java
// Service layer directly uses implementation
public class ContactService {
    private ContactDAOImpl dao = new ContactDAOImpl(); // Tight coupling
    
    public void addContact(Contact c) {
        dao.addContact(c); // Depends on specific implementation
    }
}
```

**Problems:**
- Service is **tightly coupled** to MySQL implementation
- Cannot switch to PostgreSQL without changing Service
- Cannot test Service without real database
- Hard to add features like caching or logging

**With interface** (correct approach):
```java
// Service depends on interface
public class ContactService {
    private ContactDAO dao; // Interface, not implementation
    
    public ContactService(ContactDAO dao) {
        this.dao = dao; // Any implementation can be injected
    }
    
    public void addContact(Contact c) {
        dao.addContact(c); // Works with any implementation
    }
}
```

**Benefits:**
- Service knows **what** DAO does, not **how**
- Can swap implementations without changing Service
- Can inject mock DAO for testing
- Can add caching layer transparently

#### **Programming to Interface Pattern**

```java
// Multiple implementations of same interface
public class ContactDAOMySQLImpl implements ContactDAO {
    // Uses MySQL database
}

public class ContactDAOPostgreSQLImpl implements ContactDAO {
    // Uses PostgreSQL database
}

public class ContactDAOInMemoryImpl implements ContactDAO {
    // Uses ArrayList (for testing)
}

public class ContactDAOCachedImpl implements ContactDAO {
    // Wraps another DAO and adds caching
}

// Service works with ANY of these
ContactDAO dao = new ContactDAOMySQLImpl(); // Production
ContactDAO dao = new ContactDAOInMemoryImpl(); // Testing
ContactDAO dao = new ContactDAOCachedImpl(new ContactDAOMySQLImpl()); // Cached production
```

#### **Method-by-Method Explanation**

**1. addContact(Contact contact)**
```java
void addContact(Contact contact) throws SQLException;
```
- **Purpose**: Insert new contact into database
- **When called**: User clicks "Save" on new contact form
- **Input**: Contact object with user data (no ID yet)
- **Output**: None (void), but contact.id gets populated by implementation
- **SQL**: `INSERT INTO contacts (name, phone, ...) VALUES (?, ?, ...)`
- **Why void?**: Modifies the passed Contact object's ID field (side effect)

**2. updateContact(Contact contact)**
```java
void updateContact(Contact contact) throws SQLException;
```
- **Purpose**: Update existing contact in database
- **When called**: User edits contact and clicks "Save"
- **Input**: Contact object with existing ID and modified fields
- **Output**: None (void)
- **SQL**: `UPDATE contacts SET name=?, phone=? ... WHERE id=?`
- **Why not return Contact?**: Input Contact is already up-to-date

**3. softDelete(int id)**
```java
void softDelete(int id) throws SQLException;
```
- **Purpose**: Mark contact as deleted (move to recycle bin)
- **When called**: User clicks "Delete" button
- **Input**: Contact ID to delete
- **Output**: None (void)
- **SQL**: `UPDATE contacts SET is_deleted=1 WHERE id=?`
- **Why soft delete?**: Can be undone with restore()

**4. restore(int id)**
```java
void restore(int id) throws SQLException;
```
- **Purpose**: Undelete a soft-deleted contact
- **When called**: User clicks "Restore" in recycle bin
- **Input**: Contact ID to restore
- **Output**: None (void)
- **SQL**: `UPDATE contacts SET is_deleted=0 WHERE id=?`
- **Why separate method?**: Makes intention explicit

**5. hardDelete(int id)**
```java
void hardDelete(int id) throws SQLException;
```
- **Purpose**: Permanently remove contact from database
- **When called**: User empties recycle bin
- **Input**: Contact ID to delete forever
- **Output**: None (void)
- **SQL**: `DELETE FROM contacts WHERE id=?`
- **Why separate from softDelete?**: Safety - hard to call accidentally

**6. getById(int id)**
```java
Contact getById(int id) throws SQLException;
```
- **Purpose**: Retrieve single contact by primary key
- **When called**: User clicks contact in list to view details
- **Input**: Contact ID
- **Output**: Contact object or null if not found
- **SQL**: `SELECT * FROM contacts WHERE id=?`
- **Why return null?**: Indicates contact doesn't exist (alternative: throw exception)

**7. getAll()**
```java
List<Contact> getAll() throws SQLException;
```
- **Purpose**: Retrieve all active contacts
- **When called**: Application loads main contact list
- **Input**: None
- **Output**: List of Contact objects (empty list if none)
- **SQL**: `SELECT * FROM contacts WHERE is_deleted=0 ORDER BY name`
- **Why return List?**: Multiple contacts, need ordered collection

**8. getDeleted()**
```java
List<Contact> getDeleted() throws SQLException;
```
- **Purpose**: Retrieve all deleted contacts (recycle bin)
- **When called**: User opens recycle bin view
- **Input**: None
- **Output**: List of deleted Contact objects
- **SQL**: `SELECT * FROM contacts WHERE is_deleted=1 ORDER BY updated_at DESC`
- **Why separate from getAll()?**: Different use case, different UI view

**9. searchByName(String searchTerm)**
```java
List<Contact> searchByName(String searchTerm) throws SQLException;
```
- **Purpose**: Find contacts by partial name match
- **When called**: User types in search box
- **Input**: Search text (e.g., "john")
- **Output**: List of matching contacts
- **SQL**: `SELECT * FROM contacts WHERE name LIKE ? AND is_deleted=0`
- **Why LIKE?**: Allows partial matching ("john" finds "Johnny Johnson")

**10. getByCategory(String category)**
```java
List<Contact> getByCategory(String category) throws SQLException;
```
- **Purpose**: Filter contacts by category
- **When called**: User selects category filter dropdown
- **Input**: Category name (e.g., "Work")
- **Output**: List of contacts in that category
- **SQL**: `SELECT * FROM contacts WHERE category=? AND is_deleted=0`
- **Why separate method?**: Common operation, optimized with index

#### **Why This Interface Design?**

**Comprehensive CRUD operations:**
- **Create**: addContact()
- **Read**: getById(), getAll(), getDeleted(), searchByName(), getByCategory()
- **Update**: updateContact()
- **Delete**: softDelete(), hardDelete(), restore()

**Why so many read methods?**
- **getAll()**: Default view (main contact list)
- **getDeleted()**: Special view (recycle bin)
- **searchByName()**: User-initiated search
- **getByCategory()**: Filter/grouping feature
- **getById()**: Detail view, editing

Each method serves a specific UI feature. Without these methods, higher layers would need to filter results themselves (inefficient).

#### **Why `throws SQLException`?**

```java
void addContact(Contact contact) throws SQLException;
```

Interface declares that implementations **might throw** SQLException. This means:
- Caller must handle SQLException (try-catch or propagate)
- Implementation is allowed to throw database errors
- Alternative: Catch in DAO and throw custom exception (more advanced)

**Why not catch inside DAO?**
```java
// Current approach: Let SQLException bubble up
public void addContact(Contact contact) throws SQLException {
    // If SQL fails, exception propagates to caller
}

// Alternative: Wrap in custom exception
public void addContact(Contact contact) {
    try {
        // Execute SQL
    } catch (SQLException e) {
        throw new DataAccessException("Failed to add contact", e);
    }
}
```

Both approaches are valid. Declaring `throws SQLException` is simpler for initial implementation.

#### **How Interface Connects to Implementation**

```java
// Interface (this file)
public interface ContactDAO {
    void addContact(Contact contact) throws SQLException;
}

// Implementation (next part)
public class ContactDAOImpl implements ContactDAO {
    @Override
    public void addContact(Contact contact) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "INSERT INTO contacts (name, phone, email, address, category, notes) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement pst = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pst.setString(1, contact.getName());
            pst.setString(2, contact.getPhone());
            pst.setString(3, contact.getEmail());
            pst.setString(4, contact.getAddress());
            pst.setString(5, contact.getCategory());
            pst.setString(6, contact.getNotes());
            pst.executeUpdate();
            
            // Get generated ID
            ResultSet generatedKeys = pst.getGeneratedKeys();
            if (generatedKeys.next()) {
                contact.setId(generatedKeys.getInt(1));
            }
        }
    }
}
```

**Service layer uses interface:**
```java
public class ContactService {
    private ContactDAO dao = new ContactDAOImpl();
    
    public void addContact(Contact contact) {
        try {
            dao.addContact(contact); // Calls implementation
        } catch (SQLException e) {
            // Handle error
        }
    }
}
```

#### **What Would Break Without DAO Interface?**

**Without interface** (Service calls implementation directly):
```java
public class ContactService {
    private ContactDAOImpl dao = new ContactDAOImpl(); // Concrete class
    
    public void addContact(Contact contact) {
        dao.addContact(contact); // Tightly coupled
    }
}
```

**Problems:**
1. **Cannot test Service**: Must use real database, cannot mock
2. **Cannot switch databases**: Changing MySQL to PostgreSQL requires editing Service
3. **Cannot add caching**: No way to intercept DAO calls
4. **Violates SOLID principles**: Service depends on implementation details
5. **Hard to refactor**: Changes to DAO implementation may break Service

**With interface:**
```java
public class ContactService {
    private ContactDAO dao; // Interface
    
    public ContactService(ContactDAO dao) {
        this.dao = dao; // Injected
    }
}

// Testing
ContactService service = new ContactService(new MockDAO()); // Uses mock

// Production
ContactService service = new ContactService(new ContactDAOImpl()); // Uses MySQL

// Cached
ContactService service = new ContactService(new CachedDAO(new ContactDAOImpl())); // Transparent caching
```

---

## ✅ Summary of PART 1

### What We Built

**PHASE 1 - Database Design**
- Created `contact_management` database
- Created `contacts` table with soft delete
- Added strategic indexes for performance
- Explained every column and constraint

**PHASE 2 - Project Folder Structure**
- Established layered architecture
- Separated model, dao, service, util, ui packages
- Explained separation of concerns

**PHASE 3 - DBConnection Utility**
- Created centralized database connection manager
- Used DriverManager for JDBC connections
- Explained why not to use global connection

**PHASE 4 - Contact Model**
- Created Contact POJO with all fields
- Added three constructors for different use cases
- Provided getters and setters for encapsulation
- Explained why models should be data-only

**PHASE 5 - DAO Interface**
- Defined contract for data access operations
- Included CRUD methods plus search/filter
- Explained why interface separates contract from implementation
- Prepared for implementation in next part

### Why This Foundation Matters

This foundation enables:
- **Scalability**: Can handle millions of contacts with proper indexing
- **Maintainability**: Each layer can be modified independently
- **Testability**: Can mock any layer for unit testing
- **Flexibility**: Can switch databases without rewriting application
- **Team collaboration**: Multiple developers can work on different layers

### What's Next

PART 2 will build upon this foundation:
- Implement ContactDAOImpl with actual SQL queries
- Create ContactService with business logic
- Add error handling and validation
- Connect all layers together

---

**README for PART 1 Complete. Ready for PART 2.**
