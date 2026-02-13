# Contact Management System - PART 2: Backend Implementation

## 📋 Introduction to PART 2

Welcome to **PART 2** of the Contact Management System reconstruction guide. This part implements the **backend logic layer** that bridges the database foundation from Part 1 with the user interface that will come in Part 3.

### What PART 2 Builds

In PART 2, we implement:
- **DAO Implementation** - Actual SQL queries and database operations
- **Service Layer** - Business logic coordinator between UI and database
- **Validation Layer** - Centralized input validation and data integrity checks
- **Main Entry Point** - Application launcher with proper thread management
- **Base UI Frame** - Empty Swing window structure ready for components

### How It Connects to PART 1

PART 1 established the foundation:
- ✅ Database schema with `contacts` table
- ✅ Project folder structure (model, dao, service, util, ui)
- ✅ `DBConnection` utility for database connectivity
- ✅ `Contact` model class representing data entities
- ✅ `ContactDAO` interface defining data operation contracts

PART 2 brings this foundation to life:
- 📌 Implements the `ContactDAO` interface with real SQL
- 📌 Creates service layer to coordinate operations
- 📌 Adds validation to protect data integrity
- 📌 Launches application with proper Swing setup

### Why Backend Must Be Completed Before UI

**Wrong approach** (UI-first development):
```java
// UI directly talks to database - BAD
public class ContactUI extends JFrame {
    private void saveButtonClicked() {
        Connection conn = DriverManager.getConnection(...); // Mixed layers
        String sql = "INSERT INTO contacts..."; // SQL in UI code
        // Nightmare to maintain
    }
}
```

**Problems:**
- Cannot test backend without building entire UI
- Cannot change database without breaking UI
- Cannot reuse logic in different interfaces (web, mobile, CLI)
- Database code scattered across UI components

**Correct approach** (Backend-first development):
```java
// Backend layer is complete and testable
ContactDAO dao = new ContactDAOImpl();
dao.addContact(contact); // Works independently

// UI just calls backend
public class ContactUI extends JFrame {
    private ContactService service = new ContactService();
    
    private void saveButtonClicked() {
        service.addContact(contact); // Clean separation
    }
}
```

**Benefits:**
- Backend can be tested independently (unit tests, integration tests)
- Backend can be reused in multiple UIs (desktop, web, mobile)
- Changes to database don't propagate to UI
- Team can work in parallel (backend team, UI team)

By completing backend in PART 2, we ensure:
- ✅ All business logic is centralized and testable
- ✅ Database operations are abstracted and reusable
- ✅ UI in PART 3 will be clean and maintainable
- ✅ Application follows proper layered architecture

---

## 🗄️ PHASE 6 — DAO Implementation

### File: `dao/ContactDAOImpl.java`

```java
package dao;

import model.Contact;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ContactDAOImpl - Implementation of ContactDAO interface
 * 
 * Purpose: Handles ALL database operations for Contact entities
 * 
 * This class contains:
 * - SQL query strings
 * - JDBC connection and statement handling
 * - ResultSet to Contact object mapping
 * - Exception propagation
 * 
 * Design principles:
 * - Each method opens its own connection (thread-safe)
 * - PreparedStatement for SQL injection prevention
 * - try-with-resources for automatic resource cleanup
 * - Soft delete pattern (UPDATE instead of DELETE)
 */
public class ContactDAOImpl implements ContactDAO {
    
    /**
     * Add a new contact to the database
     * 
     * Inserts contact data and retrieves auto-generated ID
     * 
     * @param contact Contact object with user data (ID is null)
     * @throws SQLException if database operation fails
     */
    @Override
    public void addContact(Contact contact) throws SQLException {
        // SQL INSERT statement with placeholders (?)
        // Does NOT include id (auto-generated), is_deleted (defaults to 0),
        // created_at/updated_at (database handles with CURRENT_TIMESTAMP)
        String sql = "INSERT INTO contacts (name, phone, email, address, category, notes) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        
        // try-with-resources: Automatically closes Connection and PreparedStatement
        // Resources are closed in reverse order: PreparedStatement first, then Connection
        try (Connection conn = DBConnection.getConnection();
             // Statement.RETURN_GENERATED_KEYS tells database to return the auto-generated ID
             PreparedStatement pst = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            // Set parameters for placeholders (?), indexed from 1 (not 0!)
            // setString() handles null values correctly and escapes special characters
            pst.setString(1, contact.getName());       // First ? = name
            pst.setString(2, contact.getPhone());      // Second ? = phone
            pst.setString(3, contact.getEmail());      // Third ? = email (can be null)
            pst.setString(4, contact.getAddress());    // Fourth ? = address (can be null)
            pst.setString(5, contact.getCategory());   // Fifth ? = category (can be null)
            pst.setString(6, contact.getNotes());      // Sixth ? = notes (can be null)
            
            // executeUpdate() executes INSERT/UPDATE/DELETE statements
            // Returns number of rows affected (we don't need this here)
            pst.executeUpdate();
            
            // Retrieve the auto-generated ID assigned by database
            try (ResultSet generatedKeys = pst.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    // Update the contact object with the new ID
                    // Now the caller knows the database ID of this contact
                    contact.setId(generatedKeys.getInt(1));
                }
            }
        }
        // Connection and PreparedStatement are automatically closed here
        // Even if exception occurs, resources are still closed
    }
    
    /**
     * Update an existing contact in the database
     * 
     * Updates all fields of contact with specified ID
     * 
     * @param contact Contact object with ID and updated fields
     * @throws SQLException if database operation fails
     */
    @Override
    public void updateContact(Contact contact) throws SQLException {
        // SQL UPDATE statement
        // updated_at column is automatically updated by database (ON UPDATE CURRENT_TIMESTAMP)
        // We don't update is_deleted here (use softDelete/restore for that)
        String sql = "UPDATE contacts SET name = ?, phone = ?, email = ?, " +
                     "address = ?, category = ?, notes = ? " +
                     "WHERE id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            
            // Set all fields (same as addContact)
            pst.setString(1, contact.getName());
            pst.setString(2, contact.getPhone());
            pst.setString(3, contact.getEmail());
            pst.setString(4, contact.getAddress());
            pst.setString(5, contact.getCategory());
            pst.setString(6, contact.getNotes());
            
            // WHERE clause uses the contact's ID
            pst.setInt(7, contact.getId());
            
            // Execute the UPDATE
            pst.executeUpdate();
        }
    }
    
    /**
     * Soft delete a contact (mark as deleted, don't remove from database)
     * 
     * Sets is_deleted = true, making contact invisible in getAll()
     * Contact can be restored with restore() method
     * 
     * @param id ID of contact to soft delete
     * @throws SQLException if database operation fails
     */
    @Override
    public void softDelete(int id) throws SQLException {
        // UPDATE instead of DELETE - this is the soft delete pattern
        // Sets is_deleted flag to 1 (true)
        String sql = "UPDATE contacts SET is_deleted = 1 WHERE id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            
            pst.setInt(1, id);
            pst.executeUpdate();
        }
    }
    
    /**
     * Restore a soft-deleted contact
     * 
     * Sets is_deleted = false, making contact visible again
     * 
     * @param id ID of contact to restore
     * @throws SQLException if database operation fails
     */
    @Override
    public void restore(int id) throws SQLException {
        // Opposite of softDelete - sets is_deleted back to 0 (false)
        String sql = "UPDATE contacts SET is_deleted = 0 WHERE id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            
            pst.setInt(1, id);
            pst.executeUpdate();
        }
    }
    
    /**
     * Permanently delete a contact from database
     * 
     * Physically removes the row - this is irreversible
     * Use with caution, typically only from "Empty Recycle Bin" feature
     * 
     * @param id ID of contact to permanently delete
     * @throws SQLException if database operation fails
     */
    @Override
    public void hardDelete(int id) throws SQLException {
        // Actual DELETE statement - removes row permanently
        // No undo possible after this
        String sql = "DELETE FROM contacts WHERE id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            
            pst.setInt(1, id);
            pst.executeUpdate();
        }
    }
    
    /**
     * Retrieve a single contact by ID
     * 
     * Fetches one contact regardless of is_deleted status
     * 
     * @param id ID of contact to retrieve
     * @return Contact object if found, null if not found
     * @throws SQLException if database operation fails
     */
    @Override
    public Contact getById(int id) throws SQLException {
        // SELECT single row by primary key (very fast, uses index)
        String sql = "SELECT * FROM contacts WHERE id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            
            pst.setInt(1, id);
            
            // executeQuery() for SELECT statements (returns ResultSet)
            try (ResultSet rs = pst.executeQuery()) {
                // ResultSet is like a cursor pointing to rows
                // next() moves to first row, returns true if row exists
                if (rs.next()) {
                    // Row found - convert ResultSet to Contact object
                    return mapResultSetToContact(rs);
                }
            }
        }
        
        // No row found with this ID
        return null;
    }
    
    /**
     * Retrieve all active (non-deleted) contacts
     * 
     * This is the default view shown to users
     * 
     * @return List of active contacts (empty list if none)
     * @throws SQLException if database operation fails
     */
    @Override
    public List<Contact> getAll() throws SQLException {
        // Only select contacts where is_deleted = 0 (active contacts)
        // ORDER BY name for alphabetical sorting
        String sql = "SELECT * FROM contacts WHERE is_deleted = 0 ORDER BY name";
        
        // List to store results
        List<Contact> contacts = new ArrayList<>();
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            
            // Loop through all rows in ResultSet
            while (rs.next()) {
                // Convert each row to Contact object and add to list
                contacts.add(mapResultSetToContact(rs));
            }
        }
        
        return contacts; // Returns empty list if no contacts found
    }
    
    /**
     * Retrieve all deleted contacts (recycle bin)
     * 
     * Used for "Recycle Bin" or "Trash" view
     * 
     * @return List of deleted contacts (empty list if none)
     * @throws SQLException if database operation fails
     */
    @Override
    public List<Contact> getDeleted() throws SQLException {
        // Only select contacts where is_deleted = 1 (deleted contacts)
        // ORDER BY updated_at DESC - show recently deleted first
        String sql = "SELECT * FROM contacts WHERE is_deleted = 1 ORDER BY updated_at DESC";
        
        List<Contact> contacts = new ArrayList<>();
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            
            while (rs.next()) {
                contacts.add(mapResultSetToContact(rs));
            }
        }
        
        return contacts;
    }
    
    /**
     * Search for contacts by name (case-insensitive partial match)
     * 
     * Returns only active contacts matching search term
     * 
     * @param searchTerm Text to search for in contact names
     * @return List of matching contacts (empty list if none)
     * @throws SQLException if database operation fails
     */
    @Override
    public List<Contact> searchByName(String searchTerm) throws SQLException {
        // LIKE clause for partial matching
        // % = wildcard (matches any characters)
        // %searchTerm% = matches if searchTerm appears anywhere in name
        // LOWER() = case-insensitive comparison
        String sql = "SELECT * FROM contacts WHERE LOWER(name) LIKE LOWER(?) AND is_deleted = 0 ORDER BY name";
        
        List<Contact> contacts = new ArrayList<>();
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            
            // Add wildcards around search term
            // If searchTerm = "john", query becomes LIKE "%john%"
            // This matches "John", "Johnny", "Johnson", etc.
            pst.setString(1, "%" + searchTerm + "%");
            
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    contacts.add(mapResultSetToContact(rs));
                }
            }
        }
        
        return contacts;
    }
    
    /**
     * Get all contacts in a specific category
     * 
     * Returns only active contacts
     * 
     * @param category Category to filter by (exact match)
     * @return List of contacts in this category (empty list if none)
     * @throws SQLException if database operation fails
     */
    @Override
    public List<Contact> getByCategory(String category) throws SQLException {
        // Exact match on category (case-sensitive)
        String sql = "SELECT * FROM contacts WHERE category = ? AND is_deleted = 0 ORDER BY name";
        
        List<Contact> contacts = new ArrayList<>();
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            
            pst.setString(1, category);
            
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    contacts.add(mapResultSetToContact(rs));
                }
            }
        }
        
        return contacts;
    }
    
    /**
     * Helper method: Convert ResultSet row to Contact object
     * 
     * This method extracts all columns from current ResultSet position
     * and constructs a fully-populated Contact object
     * 
     * ResultSet column access:
     * - Can use column name: rs.getString("name")
     * - Can use column index: rs.getString(2) [1-based, not 0-based]
     * - We use column names for clarity
     * 
     * @param rs ResultSet positioned at a valid row
     * @return Fully-populated Contact object
     * @throws SQLException if column doesn't exist or type mismatch
     */
    private Contact mapResultSetToContact(ResultSet rs) throws SQLException {
        // Create Contact using full constructor (all 10 fields)
        return new Contact(
            // Get integer from 'id' column
            rs.getInt("id"),
            
            // Get strings from text columns
            rs.getString("name"),
            rs.getString("phone"),
            rs.getString("email"),        // May be null
            rs.getString("address"),      // May be null
            rs.getString("category"),     // May be null
            rs.getString("notes"),        // May be null
            
            // Get boolean from 'is_deleted' column
            // MySQL stores boolean as TINYINT (0 or 1)
            // getBoolean() converts 0→false, 1→true
            rs.getBoolean("is_deleted"),
            
            // Get timestamps and convert to LocalDateTime
            // rs.getTimestamp() returns java.sql.Timestamp
            // toLocalDateTime() converts to java.time.LocalDateTime
            rs.getTimestamp("created_at").toLocalDateTime(),
            rs.getTimestamp("updated_at").toLocalDateTime()
        );
    }
}
```

### DAO Implementation Explanation

#### **Purpose of This Class**

`ContactDAOImpl` is the **implementation** of the `ContactDAO` interface from Part 1. This is where the rubber meets the road - actual SQL queries execute here.

**Responsibilities:**
- Opens database connections
- Constructs SQL queries
- Executes queries via JDBC
- Converts database rows to Java objects
- Handles database errors

**What this class does NOT do:**
- ❌ Validate input (that's Service layer's job)
- ❌ Handle business logic (that's Service layer's job)
- ❌ Display to user (that's UI layer's job)
- ❌ Keep global connection (each method gets fresh connection)

#### **Why PreparedStatement Instead of Statement?**

**WRONG approach** (vulnerable to SQL injection):
```java
// DANGER: SQL Injection vulnerability
String name = userInput; // Imagine user types: "; DROP TABLE contacts; --
String sql = "INSERT INTO contacts (name) VALUES ('" + name + "')";
Statement stmt = conn.createStatement();
stmt.execute(sql);
// Resulting SQL: INSERT INTO contacts (name) VALUES (''; DROP TABLE contacts; --')
// Entire database gets destroyed!
```

**Attack example:**
```
User enters name: Robert'); DROP TABLE contacts; --
Resulting SQL: INSERT INTO contacts (name) VALUES ('Robert'); DROP TABLE contacts; --')
First statement: INSERT INTO contacts (name) VALUES ('Robert')
Second statement: DROP TABLE contacts
Comment out rest: --')
Result: Your entire contacts table is deleted!
```

**CORRECT approach** (PreparedStatement prevents injection):
```java
// SAFE: PreparedStatement escapes dangerous characters
String sql = "INSERT INTO contacts (name) VALUES (?)";
PreparedStatement pst = conn.prepareStatement(sql);
pst.setString(1, userInput); // Automatically escaped
// Even malicious input is treated as literal string
```

**How PreparedStatement protects:**
- Query structure is sent to database first
- Parameters are sent separately
- Database knows "this is data, not SQL commands"
- Special characters like `'`, `"`, `;` are escaped automatically
- Impossible to inject additional SQL commands

**Additional benefits:**
- **Performance**: Database can cache query plan, only parameters change
- **Type safety**: setInt(), setString(), setDate() ensure correct types
- **Readability**: `?` placeholders are clearer than string concatenation

#### **Why Try-With-Resources?**

**OLD approach** (manual cleanup):
```java
Connection conn = null;
PreparedStatement pst = null;
ResultSet rs = null;
try {
    conn = DBConnection.getConnection();
    pst = conn.prepareStatement(sql);
    rs = pst.executeQuery();
    // Use results
} catch (SQLException e) {
    e.printStackTrace();
} finally {
    // Must close in reverse order
    if (rs != null) try { rs.close(); } catch (SQLException e) { }
    if (pst != null) try { pst.close(); } catch (SQLException e) { }
    if (conn != null) try { conn.close(); } catch (SQLException e) { }
}
// Verbose, error-prone, easy to forget
```

**MODERN approach** (try-with-resources):
```java
try (Connection conn = DBConnection.getConnection();
     PreparedStatement pst = conn.prepareStatement(sql);
     ResultSet rs = pst.executeQuery()) {
    // Use results
}
// Automatically closes ALL resources, even if exception occurs
// Closes in reverse order: rs, pst, conn
```

**Why this matters:**
- **Connection limit**: MySQL default ~150 concurrent connections
- **Unclosed connections**: Accumulate until server refuses new connections
- **Memory leaks**: Each connection holds memory/resources
- **Database locks**: Unclosed connections may hold table locks
- **Exception safety**: Resources close even if code throws exception

**What happens if you don't close:**
```
Request 1: Opens connection 1 ✓ (Forgot to close)
Request 2: Opens connection 2 ✓ (Forgot to close)
...
Request 150: Opens connection 150 ✓ (Forgot to close)
Request 151: Cannot get connection! SQLException: Too many connections
Application crashes or hangs
```

#### **Why Connection Per Method Instead of Global?**

**WRONG approach** (shared connection):
```java
public class ContactDAOImpl implements ContactDAO {
    private static Connection conn; // DANGEROUS
    
    public ContactDAOImpl() {
        conn = DBConnection.getConnection(); // One connection forever
    }
    
    public void addContact(Contact c) {
        PreparedStatement pst = conn.prepareStatement(...); // Reuse same connection
    }
}
```

**Problems with shared connection:**
1. **Thread safety**: Two threads using same connection simultaneously = data corruption
2. **Connection timeout**: Connection closes after idle period, entire app breaks
3. **Transaction isolation**: All operations share same transaction context
4. **Error recovery**: If connection breaks, must restart application
5. **Resource management**: Connection holds resources forever

**CORRECT approach** (connection per method):
```java
public void addContact(Contact contact) throws SQLException {
    try (Connection conn = DBConnection.getConnection()) { // Fresh connection
        // Use connection
    } // Connection closed automatically
}
```

**Benefits:**
- **Thread-safe**: Each thread gets its own connection
- **Fresh**: No stale connection issues
- **Independent**: One failure doesn't break others
- **Transactional**: Each operation is atomic
- **Clean**: Resources released immediately

#### **Line-by-Line: addContact() Method**

```java
public void addContact(Contact contact) throws SQLException {
```
- Method signature matches interface
- `throws SQLException` - lets caller handle database errors

```java
String sql = "INSERT INTO contacts (name, phone, email, address, category, notes) " +
             "VALUES (?, ?, ?, ?, ?, ?)";
```
- SQL INSERT statement with 6 placeholders (`?`)
- Does NOT include:
  - `id` - auto-generated by database (AUTO_INCREMENT)
  - `is_deleted` - defaults to 0 in database
  - `created_at` - defaults to CURRENT_TIMESTAMP in database
  - `updated_at` - defaults to CURRENT_TIMESTAMP in database

```java
try (Connection conn = DBConnection.getConnection();
     PreparedStatement pst = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
```
- Open connection using utility from Part 1
- Create PreparedStatement with SQL
- `Statement.RETURN_GENERATED_KEYS` - tells database to return auto-generated ID
- Both resources automatically close at end of try block

```java
pst.setString(1, contact.getName());
pst.setString(2, contact.getPhone());
pst.setString(3, contact.getEmail());
// ... etc
```
- Set values for each `?` placeholder
- **IMPORTANT**: Parameters are 1-indexed, not 0-indexed!
- `setString(1, ...)` sets first `?`
- `setString(2, ...)` sets second `?`
- `setString()` handles null values correctly
- Special characters are automatically escaped

```java
pst.executeUpdate();
```
- Execute the INSERT statement
- `executeUpdate()` is for INSERT/UPDATE/DELETE (modifies data)
- `executeQuery()` is for SELECT (retrieves data)
- Returns number of rows affected (we don't use it here)

```java
try (ResultSet generatedKeys = pst.getGeneratedKeys()) {
    if (generatedKeys.next()) {
        contact.setId(generatedKeys.getInt(1));
    }
}
```
- Get the auto-generated ID from database
- `getGeneratedKeys()` returns ResultSet with generated values
- `generatedKeys.next()` moves to first row
- `getInt(1)` gets first column (the ID)
- **Critical**: Update the contact object's ID field
- Now caller knows database ID of newly inserted contact

#### **Line-by-Line: mapResultSetToContact() Method**

```java
private Contact mapResultSetToContact(ResultSet rs) throws SQLException {
```
- Helper method to convert database row to Java object
- `private` - only used internally in this class
- Takes ResultSet positioned at valid row
- Returns fully-populated Contact object

```java
return new Contact(
    rs.getInt("id"),
```
- Extract `id` column as integer
- Could also use `rs.getInt(1)` for first column (1-indexed)
- Column names more readable than indexes

```java
    rs.getString("name"),
    rs.getString("phone"),
    rs.getString("email"),
```
- Extract text columns as strings
- `getString()` returns null if database value is NULL
- No need for null checks - Contact class accepts nulls

```java
    rs.getBoolean("is_deleted"),
```
- Extract boolean column
- MySQL stores boolean as TINYINT(1): 0 or 1
- `getBoolean()` converts: 0 → false, 1 → true

```java
    rs.getTimestamp("created_at").toLocalDateTime(),
    rs.getTimestamp("updated_at").toLocalDateTime()
```
- Extract timestamp columns
- `getTimestamp()` returns `java.sql.Timestamp`
- `.toLocalDateTime()` converts to `java.time.LocalDateTime`
- Contact class uses LocalDateTime (from Part 1)

#### **How Soft Delete Works**

**Traditional (hard) delete:**
```java
// Physically removes row from database
DELETE FROM contacts WHERE id = 5;
// Row is gone forever - cannot undo
```

**Soft delete (our approach):**
```java
// Just marks row as deleted
UPDATE contacts SET is_deleted = 1 WHERE id = 5;
// Row still exists, just hidden from normal queries
```

**Why soft delete?**
- ✅ **Undo**: Can restore with `UPDATE contacts SET is_deleted = 0`
- ✅ **Audit trail**: Keep track of what was deleted and when
- ✅ **Compliance**: Some regulations require keeping data
- ✅ **Recovery**: Accidental deletes can be recovered
- ✅ **Statistics**: Can analyze deleted contacts

**Implementation details:**
```java
// softDelete() - marks as deleted
public void softDelete(int id) throws SQLException {
    String sql = "UPDATE contacts SET is_deleted = 1 WHERE id = ?";
    // Execute UPDATE, not DELETE
}

// restore() - unmarks as deleted
public void restore(int id) throws SQLException {
    String sql = "UPDATE contacts SET is_deleted = 0 WHERE id = ?";
    // Just flip the flag back
}

// getAll() - excludes deleted contacts
public List<Contact> getAll() throws SQLException {
    String sql = "SELECT * FROM contacts WHERE is_deleted = 0"; // Filter out deleted
}

// getDeleted() - shows only deleted contacts
public List<Contact> getDeleted() throws SQLException {
    String sql = "SELECT * FROM contacts WHERE is_deleted = 1"; // Show only deleted
}
```

**When to hard delete:**
- User explicitly empties recycle bin
- Data retention policy requires purging old data
- Compliance requires permanent deletion

#### **What Would Break Without DAO Layer?**

**Without DAO** (UI directly accesses database):
```java
public class ContactUI extends JFrame {
    private void saveContact() {
        // UI knows about JDBC - BAD
        Connection conn = DriverManager.getConnection("jdbc:mysql://...");
        PreparedStatement pst = conn.prepareStatement("INSERT INTO...");
        // SQL scattered throughout UI
    }
    
    private void loadContacts() {
        // Duplicate connection logic - BAD
        Connection conn = DriverManager.getConnection("jdbc:mysql://...");
        // Same code repeated everywhere
    }
}
```

**Problems:**
- **Duplication**: Database code repeated in every UI class
- **Maintenance nightmare**: Change database = edit 50 UI files
- **Cannot test UI**: Requires database connection to test button clicks
- **Cannot reuse**: Desktop UI, web UI, mobile UI all duplicate SQL
- **No abstraction**: UI depends on specific database structure
- **Hard to optimize**: Cannot add caching without editing all UI code

**With DAO:**
```java
public class ContactUI extends JFrame {
    private ContactDAO dao = new ContactDAOImpl();
    
    private void saveContact() {
        dao.addContact(contact); // Clean, simple
    }
    
    private void loadContacts() {
        List<Contact> contacts = dao.getAll(); // Reusable
    }
}
```

**Benefits:**
- ✅ Database code centralized in one place
- ✅ Change database by editing one file (ContactDAOImpl)
- ✅ Can test UI with mock DAO (no database needed)
- ✅ Can reuse DAO in desktop, web, mobile apps
- ✅ UI doesn't know or care about SQL
- ✅ Can add caching/logging/monitoring at DAO layer

---

## 🔧 PHASE 7 — Service Layer

### File: `service/ContactService.java`

```java
package service;

import dao.ContactDAO;
import dao.ContactDAOImpl;
import model.Contact;

import java.sql.SQLException;
import java.util.List;

/**
 * ContactService - Business Logic Layer
 * 
 * Purpose: Coordinates between UI and DAO, enforces business rules
 * 
 * This layer:
 * - Validates input before passing to DAO
 * - Applies business rules (e.g., "cannot delete last contact")
 * - Handles transactions (future: multiple DAO operations in one transaction)
 * - Provides simplified API for UI
 * - Translates DAO exceptions to user-friendly messages
 * 
 * Key principle: UI should NEVER call DAO directly
 * 
 * Why?
 * - Business logic should not be in UI (hard to test, duplicated)
 * - Business logic should not be in DAO (mixes data access with rules)
 * - Service layer is the ONLY place for business rules
 */
public class ContactService {
    
    // Reference to DAO interface (not implementation)
    // Could be swapped with different implementation (mocked for testing)
    private ContactDAO contactDAO;
    
    /**
     * Constructor - Initialize with DAO implementation
     * 
     * In future, this could use dependency injection framework
     * For now, we create DAO implementation directly
     */
    public ContactService() {
        this.contactDAO = new ContactDAOImpl();
    }
    
    /**
     * Constructor for dependency injection (testing)
     * 
     * Allows passing in mock DAO for unit testing
     * 
     * Usage: ContactService service = new ContactService(mockDAO);
     */
    public ContactService(ContactDAO contactDAO) {
        this.contactDAO = contactDAO;
    }
    
    /**
     * Add a new contact
     * 
     * Validates input, then delegates to DAO
     * 
     * Business rules:
     * - Name must not be empty
     * - Phone must not be empty
     * - Email format must be valid (if provided)
     * 
     * @param contact Contact to add
     * @throws IllegalArgumentException if validation fails
     * @throws RuntimeException if database operation fails
     */
    public void addContact(Contact contact) {
        // Validation BEFORE calling DAO
        validateContact(contact);
        
        try {
            // Delegate to DAO layer
            contactDAO.addContact(contact);
            // After this, contact.id is populated by DAO
        } catch (SQLException e) {
            // Convert SQLException to RuntimeException with user-friendly message
            // In production, you might log this and show user-friendly error
            throw new RuntimeException("Failed to add contact: " + e.getMessage(), e);
        }
    }
    
    /**
     * Update an existing contact
     * 
     * Validates input, ensures contact exists, then updates
     * 
     * Business rules:
     * - Contact must have ID (must exist in database)
     * - Name must not be empty
     * - Phone must not be empty
     * - Email format must be valid (if provided)
     * 
     * @param contact Contact with updated fields
     * @throws IllegalArgumentException if validation fails or contact doesn't exist
     * @throws RuntimeException if database operation fails
     */
    public void updateContact(Contact contact) {
        // Ensure contact has ID
        if (contact.getId() == null || contact.getId() <= 0) {
            throw new IllegalArgumentException("Cannot update contact: Invalid or missing ID");
        }
        
        // Validate fields
        validateContact(contact);
        
        try {
            // Verify contact exists before updating
            Contact existing = contactDAO.getById(contact.getId());
            if (existing == null) {
                throw new IllegalArgumentException("Cannot update: Contact with ID " + contact.getId() + " does not exist");
            }
            
            // Delegate to DAO
            contactDAO.updateContact(contact);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update contact: " + e.getMessage(), e);
        }
    }
    
    /**
     * Soft delete a contact (move to recycle bin)
     * 
     * Marks contact as deleted without removing from database
     * 
     * Business rules:
     * - Contact must exist
     * - Contact must not already be deleted
     * 
     * @param id ID of contact to delete
     * @throws IllegalArgumentException if contact doesn't exist or already deleted
     * @throws RuntimeException if database operation fails
     */
    public void deleteContact(int id) {
        try {
            // Verify contact exists and is not already deleted
            Contact contact = contactDAO.getById(id);
            if (contact == null) {
                throw new IllegalArgumentException("Cannot delete: Contact with ID " + id + " does not exist");
            }
            if (contact.isDeleted()) {
                throw new IllegalArgumentException("Contact is already deleted");
            }
            
            // Delegate to DAO
            contactDAO.softDelete(id);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete contact: " + e.getMessage(), e);
        }
    }
    
    /**
     * Restore a soft-deleted contact
     * 
     * Recovers contact from recycle bin
     * 
     * Business rules:
     * - Contact must exist
     * - Contact must currently be deleted
     * 
     * @param id ID of contact to restore
     * @throws IllegalArgumentException if contact doesn't exist or not deleted
     * @throws RuntimeException if database operation fails
     */
    public void restoreContact(int id) {
        try {
            // Verify contact exists and is currently deleted
            Contact contact = contactDAO.getById(id);
            if (contact == null) {
                throw new IllegalArgumentException("Cannot restore: Contact with ID " + id + " does not exist");
            }
            if (!contact.isDeleted()) {
                throw new IllegalArgumentException("Contact is not deleted, cannot restore");
            }
            
            // Delegate to DAO
            contactDAO.restore(id);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to restore contact: " + e.getMessage(), e);
        }
    }
    
    /**
     * Permanently delete a contact
     * 
     * Removes contact from database - irreversible
     * Typically only used when emptying recycle bin
     * 
     * Business rules:
     * - Contact must exist
     * - Typically only delete already soft-deleted contacts (optional enforcement)
     * 
     * @param id ID of contact to permanently delete
     * @throws IllegalArgumentException if contact doesn't exist
     * @throws RuntimeException if database operation fails
     */
    public void permanentlyDeleteContact(int id) {
        try {
            // Verify contact exists
            Contact contact = contactDAO.getById(id);
            if (contact == null) {
                throw new IllegalArgumentException("Cannot delete: Contact with ID " + id + " does not exist");
            }
            
            // Optional: Ensure contact is already soft-deleted
            // Uncomment to enforce this rule:
            // if (!contact.isDeleted()) {
            //     throw new IllegalArgumentException("Contact must be soft-deleted first");
            // }
            
            // Delegate to DAO
            contactDAO.hardDelete(id);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to permanently delete contact: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get a single contact by ID
     * 
     * @param id ID of contact
     * @return Contact object or null if not found
     * @throws RuntimeException if database operation fails
     */
    public Contact getContactById(int id) {
        try {
            return contactDAO.getById(id);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to retrieve contact: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get all active contacts
     * 
     * Returns only non-deleted contacts, sorted by name
     * 
     * @return List of active contacts (empty list if none)
     * @throws RuntimeException if database operation fails
     */
    public List<Contact> getAllContacts() {
        try {
            return contactDAO.getAll();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to retrieve contacts: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get all deleted contacts (recycle bin)
     * 
     * Returns only soft-deleted contacts
     * 
     * @return List of deleted contacts (empty list if none)
     * @throws RuntimeException if database operation fails
     */
    public List<Contact> getDeletedContacts() {
        try {
            return contactDAO.getDeleted();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to retrieve deleted contacts: " + e.getMessage(), e);
        }
    }
    
    /**
     * Search contacts by name
     * 
     * Case-insensitive partial match
     * Returns only active contacts
     * 
     * @param searchTerm Text to search for
     * @return List of matching contacts (empty list if none)
     * @throws RuntimeException if database operation fails
     */
    public List<Contact> searchContacts(String searchTerm) {
        // Validate search term
        if (searchTerm == null) {
            searchTerm = "";
        }
        
        try {
            return contactDAO.searchByName(searchTerm);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to search contacts: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get contacts by category
     * 
     * Returns only active contacts in specified category
     * 
     * @param category Category to filter by
     * @return List of contacts in category (empty list if none)
     * @throws RuntimeException if database operation fails
     */
    public List<Contact> getContactsByCategory(String category) {
        // Validate category
        if (category == null || category.trim().isEmpty()) {
            throw new IllegalArgumentException("Category cannot be empty");
        }
        
        try {
            return contactDAO.getByCategory(category);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to retrieve contacts by category: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get count of active contacts
     * 
     * Useful for statistics without loading all contact data
     * 
     * @return Number of active contacts
     */
    public int getActiveContactCount() {
        try {
            return contactDAO.getAll().size();
            // In production, implement COUNT query for efficiency
            // Currently loads all contacts just to count them (inefficient)
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count contacts: " + e.getMessage(), e);
        }
    }
    
    /**
     * Validate contact fields
     * 
     * Enforces business rules for contact data
     * 
     * Rules:
     * - Name must not be null or empty
     * - Phone must not be null or empty
     * - Email must be valid format (if provided)
     * 
     * @param contact Contact to validate
     * @throws IllegalArgumentException if validation fails
     */
    private void validateContact(Contact contact) {
        // Check contact is not null
        if (contact == null) {
            throw new IllegalArgumentException("Contact cannot be null");
        }
        
        // Validate name (required)
        if (contact.getName() == null || contact.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Contact name is required");
        }
        
        // Validate phone (required)
        if (contact.getPhone() == null || contact.getPhone().trim().isEmpty()) {
            throw new IllegalArgumentException("Contact phone is required");
        }
        
        // Validate email format (if provided)
        if (contact.getEmail() != null && !contact.getEmail().trim().isEmpty()) {
            if (!isValidEmail(contact.getEmail())) {
                throw new IllegalArgumentException("Invalid email format");
            }
        }
        
        // Additional validations can be added here:
        // - Phone format validation
        // - Name length restrictions
        // - Category whitelist
        // etc.
    }
    
    /**
     * Simple email validation
     * 
     * Checks for basic email format: something@something.something
     * Not perfect, but catches obvious errors
     * 
     * @param email Email to validate
     * @return true if format is valid, false otherwise
     */
    private boolean isValidEmail(String email) {
        // Simple regex for basic email validation
        // Matches: name@domain.ext
        // Not comprehensive (full email regex is very complex)
        // But catches most common errors
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }
}
```

### Service Layer Explanation

#### **Purpose of Service Layer**

The Service layer is the **brains** of the application. It sits between UI and DAO, coordinating operations and enforcing business rules.

**Responsibility hierarchy:**
```
UI Layer
   ↓ "User clicked Save button"
Service Layer ← YOU ARE HERE
   ↓ "Validate input, apply business rules"
DAO Layer
   ↓ "Execute SQL query"
Database
```

**What Service does:**
- ✅ **Validates input**: Ensure data meets business requirements
- ✅ **Enforces business rules**: "Cannot delete last administrator"
- ✅ **Coordinates operations**: Multiple DAO calls in one logical operation
- ✅ **Error handling**: Convert technical errors to user-friendly messages
- ✅ **Transaction management**: Ensure related operations succeed or fail together

**What Service does NOT do:**
- ❌ Display to user (that's UI's job)
- ❌ Execute SQL (that's DAO's job)
- ❌ Know about Swing components (that's UI's job)
- ❌ Know about JDBC (that's DAO's job)

#### **Why UI Must Not Directly Call DAO**

**WRONG approach** (UI calls DAO directly):
```java
public class ContactUI extends JFrame {
    private ContactDAO dao = new ContactDAOImpl();
    
    private void saveButtonClicked() {
        Contact contact = getContactFromForm();
        
        // Validation in UI - BAD
        if (contact.getName().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name required");
            return;
        }
        
        dao.addContact(contact); // Direct DAO call - BAD
    }
}
```

**Problems:**
1. **Duplication**: Every UI screen must duplicate validation logic
2. **Inconsistency**: Different screens may validate differently
3. **Cannot bypass UI**: Cannot add contact via API or CLI without UI
4. **Hard to test**: Must instantiate UI components to test business logic
5. **Business rules in UI**: "Cannot delete last admin" logic scattered everywhere

**CORRECT approach** (UI calls Service):
```java
public class ContactUI extends JFrame {
    private ContactService service = new ContactService();
    
    private void saveButtonClicked() {
        Contact contact = getContactFromForm();
        
        try {
            service.addContact(contact); // Service handles validation
            JOptionPane.showMessageDialog(this, "Contact saved!");
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, e.getMessage()); // User-friendly error
        }
    }
}
```

**Benefits:**
- ✅ Validation centralized in one place (Service)
- ✅ Business rules consistent across all UIs
- ✅ Can add contacts via desktop UI, web UI, CLI, API - all use same Service
- ✅ Easy to test: `service.addContact(contact)` - no UI needed
- ✅ Business logic separated from presentation

#### **How Service Improves Scalability**

**Scenario: Add transaction support**

Without Service layer:
```java
// Must edit EVERY UI screen that does multiple operations
public class ContactUI extends JFrame {
    private void transferContactToGroup() {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // Start transaction
            
            dao.updateContact(contact);
            dao.addToGroup(contact, group);
            
            conn.commit(); // Must remember this in EVERY screen
        } catch (Exception e) {
            conn.rollback(); // Must remember this in EVERY screen
        }
    }
}
```

With Service layer:
```java
// Edit ONLY Service - all UIs automatically get transaction support
public class ContactService {
    public void transferContactToGroup(Contact contact, Group group) {
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            
            dao.updateContact(contact);
            dao.addToGroup(contact, group);
            
            conn.commit(); // One place
        } catch (Exception e) {
            conn.rollback();
            throw new RuntimeException("Transfer failed", e);
        }
    }
}

// UI unchanged
public class ContactUI extends JFrame {
    private void transferButtonClicked() {
        service.transferContactToGroup(contact, group); // Just works
    }
}
```

#### **Line-by-Line: addContact() Method**

```java
public void addContact(Contact contact) {
```
- Public method - UI can call this
- Takes Contact object (not individual fields)
- Doesn't return anything (void) - contact.id gets set by DAO

```java
validateContact(contact);
```
- **Critical**: Validate BEFORE calling DAO
- If validation fails, throws IllegalArgumentException
- Method exits here if invalid - DAO never called
- Centralized validation logic (reused by updateContact too)

```java
try {
    contactDAO.addContact(contact);
```
- Delegate actual database work to DAO
- Service doesn't know about SQL
- Service doesn't know about JDBC
- After this line, contact.id is populated by DAO

```java
} catch (SQLException e) {
    throw new RuntimeException("Failed to add contact: " + e.getMessage(), e);
}
```
- Catch SQLException from DAO layer
- Wrap in RuntimeException with user-friendly message
- Alternative: Create custom exception hierarchy (more advanced)
- UI catches RuntimeException and displays error to user

**Why wrap SQLException?**
- UI shouldn't know about SQLException (technical detail)
- UI should catch domain-specific exceptions
- Service translates technical errors to business errors
- In production, log SQLException for debugging, show generic message to user

#### **Line-by-Line: deleteContact() Method**

```java
public void deleteContact(int id) {
    try {
        Contact contact = contactDAO.getById(id);
```
- **Business rule enforcement**: Check if contact exists first
- DAO would silently succeed even if contact doesn't exist (UPDATE affects 0 rows)
- Service ensures meaningful error message

```java
        if (contact == null) {
            throw new IllegalArgumentException("Cannot delete: Contact with ID " + id + " does not exist");
        }
```
- Guard clause: Exit early if contact doesn't exist
- Throw exception with clear message
- UI can catch and display this message to user

```java
        if (contact.isDeleted()) {
            throw new IllegalArgumentException("Contact is already deleted");
        }
```
- Another business rule: Cannot delete what's already deleted
- Prevents confusion: User sees "deleted" in recycle bin, clicks delete again
- Better UX: Show "already deleted" message instead of silent success

```java
        contactDAO.softDelete(id);
```
- Only call DAO if all validations pass
- DAO trusts Service has validated input
- Clean separation: DAO does data, Service does rules

#### **Constructor with Dependency Injection**

```java
public ContactService() {
    this.contactDAO = new ContactDAOImpl();
}

public ContactService(ContactDAO contactDAO) {
    this.contactDAO = contactDAO;
}
```

**Why two constructors?**

**Default constructor** (production use):
```java
ContactService service = new ContactService();
// Internally creates ContactDAOImpl
// Talks to real database
```

**Injection constructor** (testing):
```java
ContactDAO mockDAO = new MockContactDAO(); // Fake DAO for testing
ContactService service = new ContactService(mockDAO);
// Now service uses mock instead of real database
```

**Testing example:**
```java
@Test
public void testAddContactWithEmptyName() {
    // Create mock DAO (doesn't need database)
    ContactDAO mockDAO = new MockContactDAO();
    ContactService service = new ContactService(mockDAO);
    
    // Create invalid contact
    Contact contact = new Contact("", "123456789", null, null, null, null);
    
    // Verify service throws exception
    assertThrows(IllegalArgumentException.class, () -> {
        service.addContact(contact);
    });
    
    // No database needed for this test!
}
```

#### **What Happens If Service Layer Is Removed?**

**Without Service** (UI directly calls DAO):

**Problem 1: Validation duplication**
```java
// ContactListUI.java
if (name.isEmpty()) throw new Exception("Name required");
dao.addContact(contact);

// ContactFormUI.java
if (name.isEmpty()) throw new Exception("Name required"); // Duplicated!
dao.addContact(contact);

// ImportCSVUI.java
// Oops, forgot to validate - invalid data enters database
dao.addContact(contact);
```

**Problem 2: Business rule inconsistency**
```java
// Screen 1: Allows empty phone
dao.addContact(contact);

// Screen 2: Requires phone
if (phone.isEmpty()) throw new Exception();
dao.addContact(contact);

// Result: Inconsistent data in database
```

**Problem 3: Cannot change database without breaking UI**
```java
// To add transaction support, must edit ALL UI screens:
// ContactListUI.java - add transaction
// ContactFormUI.java - add transaction
// ImportCSVUI.java - add transaction
// ... 20 more files
```

**With Service:**

**Solution 1: Validation centralized**
```java
// ContactService.java - ONE place
private void validateContact(Contact c) {
    if (name.isEmpty()) throw new Exception("Name required");
}

// All UIs automatically get same validation
service.addContact(contact);
```

**Solution 2: Business rules consistent**
```java
// Service enforces rules consistently
// ALL screens use same logic
// Impossible to bypass rules
```

**Solution 3: Change service, UIs unchanged**
```java
// Add transaction to Service - done!
// All UIs automatically get transactions
// No UI code changes needed
```

---

## ✅ PHASE 8 — Validation Layer

### File: `util/ContactValidator.java`

```java
package util;

import model.Contact;

/**
 * ContactValidator - Centralized Validation Utility
 * 
 * Purpose: Provides reusable validation methods for Contact data
 * 
 * Why separate validator class?
 * - Validation logic can be complex (regex, business rules)
 * - Same validation used by Service, UI, Import features
 * - Easy to add new validation rules without modifying multiple classes
 * - Can be tested independently
 * 
 * This class contains ONLY validation logic:
 * - No database access
 * - No UI code
 * - No business logic (that's Service's job)
 * - Just pure validation: input → true/false or exception
 */
public class ContactValidator {
    
    // Validation constants
    // Centralized - change once, affects all validations
    
    /** Maximum length for contact name */
    private static final int MAX_NAME_LENGTH = 100;
    
    /** Maximum length for phone number */
    private static final int MAX_PHONE_LENGTH = 20;
    
    /** Maximum length for email address */
    private static final int MAX_EMAIL_LENGTH = 100;
    
    /** Maximum length for address */
    private static final int MAX_ADDRESS_LENGTH = 255;
    
    /** Maximum length for category */
    private static final int MAX_CATEGORY_LENGTH = 50;
    
    /**
     * Validate name field
     * 
     * Rules:
     * - Must not be null
     * - Must not be empty (or only whitespace)
     * - Must not exceed maximum length
     * - Should contain only letters, spaces, and common punctuation
     * 
     * @param name Name to validate
     * @throws IllegalArgumentException if validation fails
     */
    public static void validateName(String name) {
        // Check for null
        if (name == null) {
            throw new IllegalArgumentException("Name cannot be null");
        }
        
        // Check for empty (trim removes leading/trailing whitespace)
        if (name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be empty");
        }
        
        // Check length
        if (name.length() > MAX_NAME_LENGTH) {
            throw new IllegalArgumentException("Name cannot exceed " + MAX_NAME_LENGTH + " characters");
        }
        
        // Check for valid characters
        // Allows: letters (any language), spaces, hyphens, apostrophes, periods
        // Blocks: numbers, special symbols (@, #, $, etc.)
        if (!name.matches("^[\\p{L}\\s'.\\-]+$")) {
            throw new IllegalArgumentException("Name contains invalid characters. Use only letters, spaces, hyphens, apostrophes, and periods.");
        }
    }
    
    /**
     * Validate phone number
     * 
     * Rules:
     * - Must not be null
     * - Must not be empty (or only whitespace)
     * - Must not exceed maximum length
     * - Should contain only digits, spaces, and phone formatting characters
     * 
     * @param phone Phone number to validate
     * @throws IllegalArgumentException if validation fails
     */
    public static void validatePhone(String phone) {
        // Check for null
        if (phone == null) {
            throw new IllegalArgumentException("Phone cannot be null");
        }
        
        // Check for empty
        if (phone.trim().isEmpty()) {
            throw new IllegalArgumentException("Phone cannot be empty");
        }
        
        // Check length
        if (phone.length() > MAX_PHONE_LENGTH) {
            throw new IllegalArgumentException("Phone cannot exceed " + MAX_PHONE_LENGTH + " characters");
        }
        
        // Check for valid characters
        // Allows: digits, spaces, hyphens, parentheses, plus sign (for country code)
        // Examples: "1234567890", "+1 (555) 123-4567", "555-1234"
        if (!phone.matches("^[0-9\\s()\\-+]+$")) {
            throw new IllegalArgumentException("Phone contains invalid characters. Use only digits, spaces, hyphens, parentheses, and plus sign.");
        }
        
        // Check minimum digit count (at least 7 digits for valid phone)
        // Remove all non-digit characters and count remaining
        String digitsOnly = phone.replaceAll("[^0-9]", "");
        if (digitsOnly.length() < 7) {
            throw new IllegalArgumentException("Phone must contain at least 7 digits");
        }
    }
    
    /**
     * Validate email address
     * 
     * Rules:
     * - Can be null (email is optional field)
     * - If provided, must not be empty
     * - If provided, must not exceed maximum length
     * - If provided, must be valid email format
     * 
     * @param email Email to validate
     * @throws IllegalArgumentException if validation fails
     */
    public static void validateEmail(String email) {
        // Email is optional - null is OK
        if (email == null) {
            return; // Valid - no further checks needed
        }
        
        // If provided, must not be empty
        if (email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty (leave null if not provided)");
        }
        
        // Check length
        if (email.length() > MAX_EMAIL_LENGTH) {
            throw new IllegalArgumentException("Email cannot exceed " + MAX_EMAIL_LENGTH + " characters");
        }
        
        // Validate email format
        // Regex explanation:
        // ^[A-Za-z0-9+_.-]+ = username part (letters, numbers, +, _, ., -)
        // @ = required @ symbol
        // [A-Za-z0-9.-]+ = domain part (letters, numbers, ., -)
        // \\. = required dot before extension
        // [A-Za-z]{2,} = extension (at least 2 letters: .com, .uk, etc.)
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new IllegalArgumentException("Invalid email format. Expected format: name@domain.com");
        }
    }
    
    /**
     * Validate address (if provided)
     * 
     * Rules:
     * - Can be null (address is optional)
     * - If provided, must not exceed maximum length
     * 
     * @param address Address to validate
     * @throws IllegalArgumentException if validation fails
     */
    public static void validateAddress(String address) {
        // Address is optional - null is OK
        if (address == null || address.trim().isEmpty()) {
            return; // Valid
        }
        
        // Check length
        if (address.length() > MAX_ADDRESS_LENGTH) {
            throw new IllegalArgumentException("Address cannot exceed " + MAX_ADDRESS_LENGTH + " characters");
        }
        
        // No format validation for address (too varied internationally)
        // Any characters allowed
    }
    
    /**
     * Validate category (if provided)
     * 
     * Rules:
     * - Can be null (category is optional)
     * - If provided, must not exceed maximum length
     * 
     * @param category Category to validate
     * @throws IllegalArgumentException if validation fails
     */
    public static void validateCategory(String category) {
        // Category is optional - null is OK
        if (category == null || category.trim().isEmpty()) {
            return; // Valid
        }
        
        // Check length
        if (category.length() > MAX_CATEGORY_LENGTH) {
            throw new IllegalArgumentException("Category cannot exceed " + MAX_CATEGORY_LENGTH + " characters");
        }
        
        // Optionally: Enforce whitelist of allowed categories
        // String[] allowedCategories = {"Family", "Friends", "Work", "Other"};
        // if (!Arrays.asList(allowedCategories).contains(category)) {
        //     throw new IllegalArgumentException("Category must be one of: " + String.join(", ", allowedCategories));
        // }
    }
    
    /**
     * Validate entire Contact object
     * 
     * Convenience method that validates all required fields
     * 
     * @param contact Contact to validate
     * @throws IllegalArgumentException if any validation fails
     */
    public static void validateContact(Contact contact) {
        // Check contact object itself
        if (contact == null) {
            throw new IllegalArgumentException("Contact cannot be null");
        }
        
        // Validate all fields
        // Required fields throw exception if invalid
        validateName(contact.getName());
        validatePhone(contact.getPhone());
        
        // Optional fields only throw if provided but invalid
        validateEmail(contact.getEmail());
        validateAddress(contact.getAddress());
        validateCategory(contact.getCategory());
        
        // Notes field has no validation (TEXT column, any content allowed)
    }
    
    /**
     * Check if name is valid (boolean return)
     * 
     * Non-throwing version for UI real-time validation
     * 
     * @param name Name to check
     * @return true if valid, false if invalid
     */
    public static boolean isValidName(String name) {
        try {
            validateName(name);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    
    /**
     * Check if phone is valid (boolean return)
     * 
     * Non-throwing version for UI real-time validation
     * 
     * @param phone Phone to check
     * @return true if valid, false if invalid
     */
    public static boolean isValidPhone(String phone) {
        try {
            validatePhone(phone);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    
    /**
     * Check if email is valid (boolean return)
     * 
     * Non-throwing version for UI real-time validation
     * 
     * @param email Email to check
     * @return true if valid, false if invalid
     */
    public static boolean isValidEmail(String email) {
        try {
            validateEmail(email);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
```

### Validation Layer Explanation

#### **Purpose of Validation Layer**

The Validator is a **utility class** that centralizes all data validation logic. Instead of scattering validation throughout the application, we put it all in one place.

**Why separate validator?**
- ✅ **Reusability**: Service, UI, Import features all use same validation
- ✅ **Consistency**: Same rules applied everywhere
- ✅ **Maintainability**: Change rule once, affects entire app
- ✅ **Testability**: Can test validation independently
- ✅ **Clarity**: Validation logic clearly documented in one file

#### **Why Validation Must Not Be Inside UI**

**WRONG approach** (validation in UI components):
```java
// ContactFormDialog.java
private void saveButtonClicked() {
    String name = nameField.getText();
    if (name.isEmpty()) { // Validation in UI
        JOptionPane.showMessageDialog(this, "Name required");
        return;
    }
    // ...
}

// ImportCSVDialog.java
private void importContact(String name) {
    // Oops, forgot to validate - invalid data imported!
    service.addContact(contact);
}
```

**Problems:**
- **Duplication**: Each UI screen reimplements validation
- **Inconsistency**: Different screens may have different rules
- **Easy to bypass**: Forgot to validate in import feature
- **Hard to test**: Must create UI components to test validation logic
- **Cannot reuse**: API, CLI, batch jobs must reimplement validation

**CORRECT approach** (centralized validation):
```java
// ContactValidator.java - ONE place
public static void validateName(String name) {
    if (name == null || name.trim().isEmpty()) {
        throw new IllegalArgumentException("Name required");
    }
}

// ContactFormDialog.java
try {
    ContactValidator.validateName(nameField.getText());
} catch (IllegalArgumentException e) {
    JOptionPane.showMessageDialog(this, e.getMessage());
}

// ImportCSVDialog.java
try {
    ContactValidator.validateName(name); // Same validation
} catch (IllegalArgumentException e) {
    log.error("Invalid data in CSV: " + e.getMessage());
}

// Service layer
public void addContact(Contact contact) {
    ContactValidator.validateContact(contact); // Guaranteed validation
    dao.addContact(contact);
}
```

**Benefits:**
- ✅ Validation defined once, used everywhere
- ✅ Impossible to bypass (Service validates before DAO)
- ✅ Easy to change rules (edit one method)
- ✅ Can test validation without UI
- ✅ Consistent error messages

#### **How This Protects Database Integrity**

**Layer defense:**
```
User Input (potentially malicious)
    ↓
UI Validation (first line of defense - immediate feedback)
    ↓
Service Validation (second line - cannot bypass)
    ↓
Database Constraints (last line - NOT NULL, CHECK)
```

**Example attack scenario:**

**Without validation:**
```java
// Malicious user modifies HTTP request
POST /api/contacts
{
  "name": "",  // Empty name
  "phone": "abc", // Invalid phone
  "email": "not-an-email"
}

// Service blindly accepts
dao.addContact(contact);

// DATABASE ERROR: Cannot insert NULL into 'name' column
// Application crashes, error exposed to user
```

**With validation:**
```java
// Same malicious request
POST /api/contacts
{
  "name": "",
  "phone": "abc",
  "email": "not-an-email"
}

// Service validates BEFORE database
ContactValidator.validateContact(contact);
// Throws: IllegalArgumentException("Name required")

// Controller catches
catch (IllegalArgumentException e) {
    return Response.status(400).entity(e.getMessage()).build();
}

// User sees: "Name required"
// Database never touched
// No crash, clean error
```

#### **Line-by-Line: validateName() Method**

```java
public static void validateName(String name) {
```
- Static method - no need to create validator instance
- Takes String, not Contact (focused validation)
- Throws exception instead of returning boolean (clear error message)

```java
if (name == null) {
    throw new IllegalArgumentException("Name cannot be null");
}
```
- **Critical**: Check null first
- Without this: `name.trim()` would throw NullPointerException (confusing)
- With this: Clear message "Name cannot be null"

```java
if (name.trim().isEmpty()) {
    throw new IllegalArgumentException("Name cannot be empty");
}
```
- `trim()` removes leading/trailing whitespace
- Catches "   " (spaces only) as invalid
- User must provide actual content, not just spaces

```java
if (name.length() > MAX_NAME_LENGTH) {
    throw new IllegalArgumentException("Name cannot exceed " + MAX_NAME_LENGTH + " characters");
}
```
- Enforce database column limit (VARCHAR(100))
- Prevents database truncation or error
- Better to show error before saving than after
- Constant MAX_NAME_LENGTH = one place to change limit

```java
if (!name.matches("^[\\p{L}\\s'.\\-]+$")) {
    throw new IllegalArgumentException("Name contains invalid characters...");
}
```
- Regex validation for allowed characters
- **`\\p{L}`** = any Unicode letter (supports international names: José, Sören, 李明)
- **`\\s`** = whitespace (spaces between names)
- **`'`** = apostrophe (O'Brien, D'Angelo)
- **`.`** = period (Jr., Sr., Ph.D.)
- **`\\-`** = hyphen (Mary-Jane, Jean-Claude)
- **`^...$`** = entire string must match (no invalid chars anywhere)
- Blocks: numbers, @, #, $, %, etc.

**Why validate characters?**
- Prevents injection attempts (SQL, XSS)
- Ensures data quality (names shouldn't contain @#$)
- Improves sorting/searching (consistent format)

#### **Line-by-Line: validatePhone() Method**

```java
public static void validatePhone(String phone) {
```
- Phone validation is tricky - many international formats
- We balance strictness (catch errors) with flexibility (allow formats)

```java
if (!phone.matches("^[0-9\\s()\\-+]+$")) {
    throw new IllegalArgumentException("Phone contains invalid characters...");
}
```
- Allow: digits, spaces, hyphens, parentheses, plus sign
- Supports formats:
  - "1234567890"
  - "+1 (555) 123-4567"
  - "555-123-4567"
  - "+44 20 1234 5678"
- Blocks: letters, #, *, other symbols

```java
String digitsOnly = phone.replaceAll("[^0-9]", "");
if (digitsOnly.length() < 7) {
    throw new IllegalArgumentException("Phone must contain at least 7 digits");
}
```
- Strip all formatting: "+1 (555) 123-4567" → "15551234567"
- Count only digits
- Minimum 7 digits (shortest valid phone numbers)
- Prevents: "123", "---", "+++", etc.

**Why minimum 7 digits?**
- Local numbers: 7 digits (555-1234)
- National numbers: 10 digits (555-123-4567)
- International: 11+ digits (+1-555-123-4567)
- 7 is safe minimum that allows all valid formats

#### **Line-by-Line: validateEmail() Method**

```java
if (email == null) {
    return; // Valid - email is optional
}
```
- **Critical difference**: Email is nullable field
- NULL means "no email provided" - perfectly valid
- Only validate if user provided email

```java
if (email.trim().isEmpty()) {
    throw new IllegalArgumentException("Email cannot be empty (leave null if not provided)");
}
```
- Empty string vs null are different
- Empty string "" means "user entered nothing but field was submitted"
- Should be null instead
- Helps catch UI bugs where empty string sent instead of null

```java
if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
    throw new IllegalArgumentException("Invalid email format...");
}
```
- Email regex - not perfect but catches most errors
- **username**: letters, numbers, +, _, ., -
- **@**: required
- **domain**: letters, numbers, ., -
- **extension**: at least 2 letters (.com, .uk, .museum)

**Valid emails:**
- john@example.com ✓
- jane.doe@company.co.uk ✓
- user+tag@domain.com ✓

**Invalid emails:**
- @example.com ✗ (no username)
- user@.com ✗ (no domain)
- user@domain ✗ (no extension)
- user name@domain.com ✗ (space in username)

**Note**: Perfect email validation is extremely complex (RFC 5322 is 50+ pages). This regex is pragmatic - catches common errors without being overly strict.

#### **Throwing vs Returning Boolean**

**Two validation styles provided:**

**Style 1: Throwing (for Service layer)**
```java
public static void validateName(String name) {
    if (invalid) throw new IllegalArgumentException("Error message");
}

// Usage in Service
try {
    ContactValidator.validateName(name);
    // If we reach here, name is valid
    dao.addContact(contact);
} catch (IllegalArgumentException e) {
    // Show error to user
    throw new RuntimeException(e.getMessage());
}
```

**Style 2: Boolean (for UI real-time feedback)**
```java
public static boolean isValidName(String name) {
    try {
        validateName(name);
        return true;
    } catch (IllegalArgumentException e) {
        return false;
    }
}

// Usage in UI
nameField.addKeyListener(new KeyAdapter() {
    public void keyReleased(KeyEvent e) {
        if (ContactValidator.isValidName(nameField.getText())) {
            nameField.setBackground(Color.WHITE); // Valid - white background
        } else {
            nameField.setBackground(Color.PINK); // Invalid - pink background
        }
    }
});
```

**Why both?**
- Service needs exception with message to show user
- UI needs boolean for real-time feedback (typing)
- Boolean version reuses throwing version (DRY principle)

#### **What Would Break Without Validation?**

**Scenario 1: No validation**
```java
// User enters garbage data
Contact contact = new Contact("", "", "not-email", null, null, null);
dao.addContact(contact);

// Database error: Name cannot be NULL
// SQLException propagates to UI
// User sees: "com.mysql.jdbc.exceptions.MySQLIntegrityConstraintViolationException..."
// Terrible user experience
```

**Scenario 2: Validation only in UI**
```java
// UI validates
if (name.isEmpty()) { error(); }

// But import CSV feature doesn't
importCSV.forEach(row -> dao.addContact(row)); // No validation
// Result: Invalid data in database from CSV import
// Manual cleanup required
```

**Scenario 3: With centralized validation**
```java
// Service ALWAYS validates
public void addContact(Contact contact) {
    ContactValidator.validateContact(contact); // Cannot skip
    dao.addContact(contact);
}

// UI shows error before saving
// Import shows error during CSV processing
// API returns 400 Bad Request with clear message
// Database always contains valid data
```

---

## 🚀 PHASE 9 — Main Application Entry

### File: `Main.java`

```java
import javax.swing.SwingUtilities;
import ui.ContactDashboardUI;

/**
 * Main - Application Entry Point
 * 
 * Purpose: Launch the Contact Management System
 * 
 * This is the entry point of the application.
 * When user runs the program, this main() method executes first.
 * 
 * Responsibilities:
 * - Initialize the Swing UI on the correct thread
 * - Create and display the main window
 * - (Future: Load configuration, initialize logging, etc.)
 * 
 * Key principle: Swing components must be created/modified on Event Dispatch Thread (EDT)
 */
public class Main {
    
    /**
     * Application entry point
     * 
     * This method is called by the JVM when program starts
     * 
     * @param args Command-line arguments (not used currently)
     */
    public static void main(String[] args) {
        // Print startup message (helpful for debugging)
        System.out.println("Starting Contact Management System...");
        
        // SwingUtilities.invokeLater() schedules code to run on Event Dispatch Thread
        // This is CRITICAL for Swing applications
        //
        // Why?
        // - Swing is not thread-safe
        // - All UI operations must happen on EDT
        // - main() runs on main thread, not EDT
        // - invokeLater() moves execution to EDT
        //
        // What happens without it?
        // - Race conditions
        // - Deadlocks
        // - Random crashes
        // - UI freezing
        // - Unpredictable behavior
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                // This code runs on Event Dispatch Thread
                
                // Create the main window
                // Constructor builds the entire UI structure
                ContactDashboardUI dashboard = new ContactDashboardUI();
                
                // Make window visible
                // setVisible(true) must be called to display window
                // Window is created hidden by default
                dashboard.setVisible(true);
                
                System.out.println("Contact Management System started successfully!");
            }
        });
        
        // Lambda version (Java 8+): Same functionality, cleaner syntax
        // Uncomment to use lambda instead of anonymous class above:
        // SwingUtilities.invokeLater(() -> {
        //     ContactDashboardUI dashboard = new ContactDashboardUI();
        //     dashboard.setVisible(true);
        // });
    }
}
```

### Main Entry Point Explanation

#### **Purpose of Main Class**

The `Main` class is the **entry point** of the application. When you double-click the JAR file or run `java Main`, the JVM calls this `main()` method.

**Responsibilities:**
- ✅ Launch the application
- ✅ Initialize Swing on correct thread
- ✅ Create and show main window
- ✅ (Future: Load config files, initialize logging, setup database connection pool)

**What Main does NOT do:**
- ❌ Contain UI code (that's ContactDashboardUI's job)
- ❌ Contain business logic (that's Service's job)
- ❌ Connect to database directly (DAO handles that when needed)

#### **Why SwingUtilities.invokeLater()?**

This is **critical** for Swing applications. Understanding this prevents 90% of Swing bugs.

**The Problem: Swing is Not Thread-Safe**

When you launch a Java program:
```
JVM starts
    ↓
Creates "main thread"
    ↓
Calls main() method on main thread
```

But Swing has its own thread called **Event Dispatch Thread (EDT)**:
```
Swing initializes
    ↓
Creates "Event Dispatch Thread" (EDT)
    ↓
All UI operations must happen on EDT
```

**Two threads exist:**
- **Main thread** - Runs your main() method
- **EDT** - Handles all Swing UI operations

**WRONG approach** (create UI on main thread):
```java
public static void main(String[] args) {
    // This runs on main thread (WRONG!)
    ContactDashboardUI dashboard = new ContactDashboardUI();
    dashboard.setVisible(true);
    // Race conditions, potential crashes
}
```

**Problems:**
- Creating UI on wrong thread
- Swing components accessed from multiple threads simultaneously
- Race conditions
- Deadlocks
- Random crashes (sometimes works, sometimes doesn't)
- Hard to debug (non-deterministic failures)

**CORRECT approach** (create UI on EDT):
```java
public static void main(String[] args) {
    // This runs on main thread
    SwingUtilities.invokeLater(() -> {
        // This runs on EDT (CORRECT!)
        ContactDashboardUI dashboard = new ContactDashboardUI();
        dashboard.setVisible(true);
    });
}
```

**How invokeLater() works:**
1. main() runs on main thread
2. invokeLater() schedules task for EDT
3. main() returns (main thread exits)
4. EDT picks up task and executes it
5. All UI operations happen on EDT

**Visual timeline:**
```
Main Thread:                EDT:
  │                           │
  ├─ main() starts            │
  ├─ invokeLater(task)   ────→ task queued
  └─ main() ends              │
                              ├─ task executes
                              ├─ Create UI
                              ├─ Show window
                              └─ Wait for events
```

#### **What Happens If You Don't Use invokeLater()?**

**Example bug scenario:**

```java
// WRONG
public static void main(String[] args) {
    ContactDashboardUI ui = new ContactDashboardUI(); // Main thread creates components
    ui.setVisible(true);
}

// Later, EDT processes button click
button.addActionListener(e -> {
    label.setText("Clicked"); // EDT modifies component
    // CRASH: Two threads accessing same component!
});
```

**Typical errors without invokeLater():**
- `IllegalStateException: Component must be accessed from EDT`
- `ConcurrentModificationException`
- `NullPointerException` (race condition)
- UI freezing
- Components not updating
- Deadlocks

**Real-world manifestation:**
```
First run: Works fine ✓
Second run: Works fine ✓
Third run: CRASH ✗ (Why??)
Fourth run: Works fine ✓

Developer: "It works on my machine!"
User: "It crashes randomly!"

Root cause: Race condition - timing-dependent
Depends on: CPU speed, load, random thread scheduling
```

#### **Why Thread Safety Matters**

**Swing components are not synchronized:**
```java
public class JLabel {
    private String text; // Not synchronized
    
    public void setText(String text) {
        this.text = text; // Not thread-safe
        repaint(); // Triggers render
    }
}
```

**Two threads modify simultaneously:**
```
Thread 1: label.setText("Hello")
Thread 2: label.setText("World")

Possible outcomes:
1. Text = "Hello" ✓
2. Text = "World" ✓
3. Text = "Hell" + "World" = "Hellorld" ✗ (corrupted)
4. Crash ✗ (NullPointerException during string copy)
5. UI never updates ✗ (repaint() lost)
```

**EDT solves this:**
```
All UI operations queued on EDT
EDT processes one at a time
No simultaneous access
No race conditions
Predictable behavior
```

#### **Line-by-Line: main() Method**

```java
public static void main(String[] args) {
```
- Entry point - JVM calls this when program starts
- `static` - No instance needed, JVM can call directly
- `void` - Doesn't return anything to JVM
- `String[] args` - Command-line arguments (e.g., `java Main config.properties`)

```java
System.out.println("Starting Contact Management System...");
```
- Print to console (not visible to end users in packaged app)
- Useful for debugging: confirms program is starting
- Production apps would use proper logging framework (log4j, slf4j)

```java
SwingUtilities.invokeLater(new Runnable() {
```
- **Critical line** - Schedules code to run on EDT
- Takes `Runnable` - interface with single `run()` method
- Anonymous class implementation (Java < 8 style)
- Alternative: Lambda (Java 8+): `SwingUtilities.invokeLater(() -> { ... });`

```java
    @Override
    public void run() {
        // This code executes on EDT, not main thread
```
- Override `Runnable.run()` method
- Code inside runs on Event Dispatch Thread
- Safe to create/modify Swing components here

```java
        ContactDashboardUI dashboard = new ContactDashboardUI();
```
- Create main window instance
- Constructor builds entire UI structure (next phase)
- Window is created but not visible yet

```java
        dashboard.setVisible(true);
```
- Make window visible on screen
- Without this, window exists in memory but user can't see it
- `setVisible(true)` triggers rendering and display

```java
        System.out.println("Contact Management System started successfully!");
```
- Confirmation message - useful for debugging
- Indicates UI is fully initialized and displayed

#### **Alternative: invokeLater() vs invokeAndWait()**

**Two methods available:**

**invokeLater()** - Asynchronous
```java
SwingUtilities.invokeLater(() -> {
    // Create UI
});
System.out.println("This prints immediately");
// UI may not be created yet when this prints
```
- Queues task and returns immediately
- Main thread continues without waiting
- UI created "sometime later" on EDT
- **Use case**: Normal application launch

**invokeAndWait()** - Synchronous
```java
try {
    SwingUtilities.invokeAndWait(() -> {
        // Create UI
    });
    System.out.println("This prints after UI is ready");
    // UI is guaranteed to be created when this prints
} catch (Exception e) {
    e.printStackTrace();
}
```
- Queues task and WAITS for completion
- Main thread blocks until EDT finishes task
- **Use case**: When you need UI ready before continuing (rare)
- **Danger**: Can cause deadlock if EDT is waiting for main thread

**For main() method, always use invokeLater()** - safer and standard practice.

#### **What Happens Behind the Scenes**

**Complete execution flow:**

```
1. User runs: java Main
   │
2. JVM starts, creates main thread
   │
3. JVM calls Main.main() on main thread
   │
4. main() prints "Starting..."
   │
5. main() calls SwingUtilities.invokeLater(runnable)
   │
6. invokeLater() adds runnable to EDT queue
   │
7. main() prints "This runs first!"
   │
8. main() ends, main thread exits
   │
9. EDT picks up runnable from queue
   │
10. EDT executes runnable.run()
    │
11. EDT creates ContactDashboardUI
    │
12. EDT makes window visible
    │
13. EDT enters event loop (waits for user actions)
    │
14. User clicks button
    │
15. EDT executes button's ActionListener
    │
16. User closes window
    │
17. EDT calls System.exit(0)
    │
18. JVM shuts down
```

---

## 🖼️ PHASE 10 — Base Swing JFrame Layout

### File: `ui/ContactDashboardUI.java`

```java
package ui;

import javax.swing.*;
import java.awt.*;

/**
 * ContactDashboardUI - Main Application Window
 * 
 * Purpose: Provides the main user interface for the Contact Management System
 * 
 * This JFrame is the root container for the entire application UI.
 * It provides the base layout structure that will hold:
 * - Top panel (toolbar with action buttons)
 * - Center panel (contact table/list)
 * - Bottom panel (status bar/statistics)
 * 
 * Design: BorderLayout for flexible, responsive design
 * 
 * Future enhancements:
 * - Add JTable for contact list (Part 3)
 * - Add toolbar buttons (Add, Edit, Delete, Search)
 * - Add status bar showing contact count
 * - Add menu bar (File, Edit, View, Help)
 */
public class ContactDashboardUI extends JFrame {
    
    // Window dimensions
    // These constants make it easy to change window size in one place
    private static final int WINDOW_WIDTH = 1000;
    private static final int WINDOW_HEIGHT = 600;
    
    /**
     * Constructor - Build and initialize the UI
     * 
     * Called when creating new instance: new ContactDashboardUI()
     * Sets up window properties and creates layout structure
     */
    public ContactDashboardUI() {
        // Call superclass (JFrame) constructor with window title
        // This title appears in window title bar and taskbar
        super("Contact Management System");
        
        // Initialize the user interface
        initializeUI();
    }
    
    /**
     * Initialize the user interface
     * 
     * Sets up window properties and creates the layout structure
     * Broken into separate method for clarity and organization
     */
    private void initializeUI() {
        // ========== Window Configuration ==========
        
        // Set window size
        // setSize(width, height) sets the window dimensions in pixels
        // Without this, window would be tiny (default size)
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        
        // Center window on screen
        // setLocationRelativeTo(null) centers window on primary monitor
        // null = relative to screen, not another component
        // Without this, window appears in top-left corner (poor UX)
        setLocationRelativeTo(null);
        
        // Set close operation
        // Defines what happens when user clicks X button
        // EXIT_ON_CLOSE - Terminate entire application (calls System.exit(0))
        // DISPOSE_ON_CLOSE - Close window but keep application running (for multi-window apps)
        // DO_NOTHING_ON_CLOSE - Ignore close button (for unsaved changes prompt)
        // HIDE_ON_CLOSE - Hide window but keep it in memory
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // Set layout manager
        // BorderLayout divides container into 5 regions: NORTH, SOUTH, EAST, WEST, CENTER
        // Each region can hold one component
        // CENTER region expands to fill available space
        // Perfect for: toolbar (NORTH), content (CENTER), status bar (SOUTH)
        setLayout(new BorderLayout());
        
        // ========== Create UI Components ==========
        
        // Top panel - Will hold toolbar with action buttons
        // Currently just placeholder with label
        JPanel topPanel = createTopPanel();
        
        // Center panel - Will hold contact table
        // Currently just placeholder with label
        JPanel centerPanel = createCenterPanel();
        
        // Bottom panel - Will hold status bar
        // Currently just placeholder with label
        JPanel bottomPanel = createBottomPanel();
        
        // ========== Add Components to Frame ==========
        
        // Add panels to frame using BorderLayout positions
        // BorderLayout.NORTH - Top of window (toolbar)
        add(topPanel, BorderLayout.NORTH);
        
        // BorderLayout.CENTER - Middle of window (main content)
        // CENTER expands to fill remaining space
        add(centerPanel, BorderLayout.CENTER);
        
        // BorderLayout.SOUTH - Bottom of window (status bar)
        add(bottomPanel, BorderLayout.SOUTH);
        
        // Note: We don't use EAST or WEST in this layout
        // Could be used for side panels in future (e.g., category filter)
    }
    
    /**
     * Create top panel (toolbar area)
     * 
     * This will eventually contain:
     * - "Add Contact" button
     * - "Edit Contact" button
     * - "Delete Contact" button
     * - "Search" text field
     * - "Recycle Bin" button
     * 
     * For now, just a placeholder panel
     * 
     * @return JPanel containing toolbar components
     */
    private JPanel createTopPanel() {
        // Create panel with FlowLayout
        // FlowLayout arranges components left-to-right, wrapping to next line if needed
        // Like text in a word processor
        // Good for toolbars with multiple buttons
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        // Set preferred height
        // Preferred size is a hint to layout manager
        // Actual size may differ based on layout constraints
        panel.setPreferredSize(new Dimension(WINDOW_WIDTH, 60));
        
        // Set background color for visibility (temporary)
        // Light gray makes panel boundaries visible during development
        // Will be removed when actual toolbar is added
        panel.setBackground(new Color(240, 240, 240));
        
        // Add placeholder label
        // Shows where toolbar will go
        // Will be replaced with actual buttons in Part 3
        JLabel placeholderLabel = new JLabel("Toolbar (Buttons will be added in Part 3)");
        placeholderLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        panel.add(placeholderLabel);
        
        return panel;
    }
    
    /**
     * Create center panel (main content area)
     * 
     * This will eventually contain:
     * - JTable displaying all contacts
     * - JScrollPane wrapping the table
     * - Table columns: Name, Phone, Email, Category
     * - Row selection and sorting
     * 
     * For now, just a placeholder panel
     * 
     * @return JPanel containing main content
     */
    private JPanel createCenterPanel() {
        // Create panel with BorderLayout
        // BorderLayout allows JTable to expand fully in CENTER
        JPanel panel = new JPanel(new BorderLayout());
        
        // Set background color for visibility (temporary)
        panel.setBackground(Color.WHITE);
        
        // Add placeholder label (centered)
        // Shows where contact table will go
        // Will be replaced with JTable + JScrollPane in Part 3
        JLabel placeholderLabel = new JLabel("Contact Table (JTable will be added in Part 3)", SwingConstants.CENTER);
        placeholderLabel.setFont(new Font("Arial", Font.BOLD, 20));
        placeholderLabel.setForeground(Color.GRAY);
        panel.add(placeholderLabel, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Create bottom panel (status bar area)
     * 
     * This will eventually contain:
     * - Contact count ("Total: 150 contacts")
     * - Category filter ("Showing: Work")
     * - Last update time
     * 
     * For now, just a placeholder panel
     * 
     * @return JPanel containing status bar
     */
    private JPanel createBottomPanel() {
        // Create panel with FlowLayout (left-aligned)
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        // Set preferred height (status bars are typically thin)
        panel.setPreferredSize(new Dimension(WINDOW_WIDTH, 30));
        
        // Set background color for visibility (temporary)
        panel.setBackground(new Color(220, 220, 220));
        
        // Add placeholder label
        // Shows where status information will go
        // Will be updated dynamically in Part 3
        JLabel placeholderLabel = new JLabel("Status Bar (Statistics will be added in Part 3)");
        placeholderLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        panel.add(placeholderLabel);
        
        return panel;
    }
    
    /**
     * Main method for testing UI independently
     * 
     * Allows running this UI class directly for development/testing
     * In production, Main.java launches the application
     * 
     * To test: Run this file directly in IDE
     * 
     * @param args Command-line arguments (not used)
     */
    public static void main(String[] args) {
        // Same EDT initialization as Main.java
        SwingUtilities.invokeLater(() -> {
            ContactDashboardUI ui = new ContactDashboardUI();
            ui.setVisible(true);
            System.out.println("ContactDashboardUI test window displayed");
        });
    }
}
```

### Base Swing UI Explanation

#### **Purpose of ContactDashboardUI**

`ContactDashboardUI` is the **main window** of the application. When program starts, this is what user sees.

**Extends JFrame:**
- `JFrame` is top-level window container in Swing
- Has title bar, minimize/maximize/close buttons
- Can contain other components (panels, buttons, tables)

**Current state:**
- Empty placeholder layout (Part 2)
- Shows where components will go
- Establishes structure for Part 3

**Future state (Part 3):**
- Toolbar with buttons
- JTable with contact data
- Status bar with statistics
- Fully functional UI

#### **Why BorderLayout?**

**BorderLayout divides window into 5 regions:**

```
┌──────────────────────────────────┐
│           NORTH                  │ ← Toolbar (fixed height)
├──────────────────────────────────┤
│  │                          │    │
│W │                          │ E  │
│E │        CENTER            │ A  │ ← Main content (expands)
│S │                          │ S  │
│T │                          │ T  │
│  │                          │    │
├──────────────────────────────────┤
│           SOUTH                  │ ← Status bar (fixed height)
└──────────────────────────────────┘
```

**Our usage:**
- **NORTH**: Toolbar (Add, Edit, Delete buttons)
- **CENTER**: Contact table (main content)
- **SOUTH**: Status bar (contact count)
- **EAST/WEST**: Not used (could add side panels later)

**Why BorderLayout for main window?**
- ✅ **Natural fit**: Toolbars at top, content in middle, status at bottom
- ✅ **Responsive**: CENTER expands to fill available space
- ✅ **Simple**: Only 5 regions, easy to understand
- ✅ **Standard**: Used by most applications (Eclipse, IntelliJ, browsers)

**Comparison with other layouts:**

**FlowLayout** (like text flowing in document):
```
[Button] [Button] [Button]
[Button] [Button]
```
- Good for: Toolbars, button groups
- Bad for: Main windows (no control over expansion)

**GridLayout** (equal-sized grid):
```
┌───────┬───────┬───────┐
│ Cell  │ Cell  │ Cell  │
├───────┼───────┼───────┤
│ Cell  │ Cell  │ Cell  │
└───────┴───────┴───────┘
```
- Good for: Calculator buttons, game boards
- Bad for: Applications (everything same size)

**BorderLayout** (5 regions):
```
┌─────────────────────────┐
│ NORTH (fixed height)    │
├─────────────────────────┤
│ CENTER (expands)        │
├─────────────────────────┤
│ SOUTH (fixed height)    │
└─────────────────────────┘
```
- Good for: Application windows ✓
- Perfect fit for our needs ✓

#### **Why Layout Managers Matter**

**Without layout manager** (absolute positioning):
```java
// Absolute positioning - BAD
JButton button = new JButton("Click");
button.setBounds(100, 50, 200, 30); // x, y, width, height
panel.add(button);
```

**Problems:**
- ❌ Doesn't resize when window resizes
- ❌ Breaks on different screen resolutions
- ❌ Breaks on different operating systems (fonts, borders different sizes)
- ❌ Not accessible (screen readers, high DPI)
- ❌ Maintenance nightmare (manual calculation of positions)

**Window resize example:**
```
Original:
┌────────────────┐
│ [Button]       │  ← Button at fixed position
└────────────────┘

Maximized:
┌─────────────────────────────────┐
│ [Button]                        │  ← Button still at same position, looks broken
└─────────────────────────────────┘
```

**With layout manager** (BorderLayout):
```java
// Layout manager - GOOD
JPanel toolbar = new JPanel();
toolbar.add(button);
frame.add(toolbar, BorderLayout.NORTH);
```

**Benefits:**
- ✅ Automatically resizes with window
- ✅ Adapts to different screen sizes
- ✅ Works across operating systems
- ✅ Accessible
- ✅ Maintainable

**Window resize example:**
```
Original:
┌────────────────┐
│ [Toolbar     ] │  ← Toolbar spans width
├────────────────┤
│ Content        │
└────────────────┘

Maximized:
┌─────────────────────────────────┐
│ [Toolbar                      ] │  ← Toolbar automatically expands
├─────────────────────────────────┤
│ Content                         │
└─────────────────────────────────┘
```

#### **Why setLocationRelativeTo(null)?**

```java
setLocationRelativeTo(null);
```

**Without this:**
```
Screen: ┌─────────────────────────────────┐
        │[Window]                         │  ← Appears at (0,0) - top-left corner
        │                                 │
        │                                 │
        └─────────────────────────────────┘
```

**With this:**
```
Screen: ┌─────────────────────────────────┐
        │                                 │
        │         [Window]                │  ← Centered on screen
        │                                 │
        └─────────────────────────────────┘
```

**Why centering improves UX:**
- ✅ Professional appearance
- ✅ Easier to find on screen
- ✅ Works on multi-monitor setups
- ✅ Standard practice for applications

**Multi-monitor behavior:**
- Centers on primary monitor
- Works correctly even with different monitor sizes
- Can be dragged to secondary monitor by user

#### **Why setDefaultCloseOperation()?**

```java
setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
```

**Four options:**

**EXIT_ON_CLOSE** - Terminate application
```java
setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
// User clicks X → System.exit(0) → JVM shuts down → Application ends
```
- **Use when**: Single-window application
- **Effect**: Clicking X terminates program
- **Our choice**: We use this (simple contact manager)

**DISPOSE_ON_CLOSE** - Close window, keep app running
```java
setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
// User clicks X → Window closes → Other windows still open → App continues
```
- **Use when**: Multi-window application
- **Effect**: Clicking X closes this window only
- **Example**: Document editor with multiple document windows

**HIDE_ON_CLOSE** - Hide window, keep in memory
```java
setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
// User clicks X → Window hides → Can be shown again with setVisible(true)
```
- **Use when**: Window might be reopened
- **Effect**: Window hidden but recoverable
- **Example**: Preferences dialog that can be reopened

**DO_NOTHING_ON_CLOSE** - Ignore close button
```java
setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
addWindowListener(new WindowAdapter() {
    public void windowClosing(WindowEvent e) {
        if (JOptionPane.showConfirmDialog(frame, "Unsaved changes. Really quit?") == YES) {
            System.exit(0);
        }
    }
});
```
- **Use when**: Need to confirm before closing (unsaved changes)
- **Effect**: Custom handling of close action
- **Example**: Text editor with unsaved document

#### **Line-by-Line: Constructor and Initialization**

```java
public class ContactDashboardUI extends JFrame {
```
- Extend JFrame - inherit all window functionality
- ContactDashboardUI **is-a** JFrame
- Can use all JFrame methods (setSize, setVisible, etc.)

```java
private static final int WINDOW_WIDTH = 1000;
private static final int WINDOW_HEIGHT = 600;
```
- Constants for window dimensions
- `static final` = cannot be changed
- Centralized - change once, affects entire class
- Dimensions chosen for: typical screen size, comfortable reading

**Why these dimensions?**
- 1000×600 fits on 1366×768 screens (common laptop resolution)
- Leaves room for taskbar
- Good aspect ratio for contact table (more horizontal than vertical)
- Not too large (doesn't overwhelm small screens)
- Not too small (enough room for content)

```java
public ContactDashboardUI() {
    super("Contact Management System");
```
- Constructor - called when creating new instance
- `super(title)` - call JFrame constructor with window title
- Title appears in: window title bar, taskbar, window switcher (Alt+Tab)

```java
    initializeUI();
}
```
- Delegate UI setup to separate method
- Keeps constructor clean
- Makes code more organized and readable

```java
private void initializeUI() {
```
- `private` - internal method, not for external use
- Sets up all window properties and layout
- Broken into smaller methods (createTopPanel, createCenterPanel, createBottomPanel)

```java
setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
```
- Set window dimensions in pixels
- Uses constants defined earlier
- Alternative: `pack()` - resize to fit contents (use after adding all components)

```java
setLocationRelativeTo(null);
```
- Center window on screen
- `null` = relative to screen (not another component)
- Call AFTER setSize() (needs dimensions to calculate center)

```java
setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
```
- Clicking X terminates application
- Appropriate for single-window app
- Alternative: DISPOSE_ON_CLOSE for multi-window

```java
setLayout(new BorderLayout());
```
- Set layout manager for frame
- BorderLayout divides into 5 regions (NORTH, SOUTH, EAST, WEST, CENTER)
- Perfect for application windows

#### **Line-by-Line: createTopPanel()**

```java
private JPanel createTopPanel() {
```
- Creates toolbar panel
- Returns JPanel ready to be added to frame
- `private` - only used internally

```java
JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
```
- Create JPanel with FlowLayout
- FlowLayout arranges components left-to-right
- `FlowLayout.LEFT` = align components to left (instead of center)

**Why FlowLayout for toolbar?**
- Buttons naturally flow left-to-right
- Automatically wraps if window too narrow
- Simple for button groups

```java
panel.setPreferredSize(new Dimension(WINDOW_WIDTH, 60));
```
- Set preferred height to 60 pixels
- Preferred size is a hint to layout manager
- BorderLayout respects preferred height of NORTH panel
- Width doesn't matter (NORTH spans full width automatically)

```java
panel.setBackground(new Color(240, 240, 240));
```
- Light gray background
- Temporary - makes panel boundaries visible during development
- Will be removed or adjusted when real toolbar added

```java
JLabel placeholderLabel = new JLabel("Toolbar (Buttons will be added in Part 3)");
```
- Temporary label showing what goes here
- Will be replaced with actual buttons in Part 3:
  - Add Contact button
  - Edit Contact button
  - Delete Contact button
  - Search field
  - Recycle Bin button

#### **How This Prepares for JTable Integration**

**Current structure (Part 2):**
```
ContactDashboardUI (JFrame)
  ├── Top Panel (placeholder)
  ├── Center Panel (placeholder)
  └── Bottom Panel (placeholder)
```

**Future structure (Part 3):**
```
ContactDashboardUI (JFrame)
  ├── Top Panel (toolbar)
  │   ├── Add Button
  │   ├── Edit Button
  │   ├── Delete Button
  │   └── Search Field
  ├── Center Panel
  │   └── JScrollPane
  │       └── JTable (contact data)
  └── Bottom Panel (status bar)
      └── Label ("Total: 150 contacts")
```

**Migration will be easy:**
```java
// Current (Part 2)
private JPanel createCenterPanel() {
    JPanel panel = new JPanel(new BorderLayout());
    JLabel placeholder = new JLabel("Placeholder");
    panel.add(placeholder, BorderLayout.CENTER);
    return panel;
}

// Future (Part 3)
private JPanel createCenterPanel() {
    JPanel panel = new JPanel(new BorderLayout());
    
    // Create table
    JTable contactTable = new JTable(tableModel);
    JScrollPane scrollPane = new JScrollPane(contactTable);
    
    // Add to panel (replaces placeholder)
    panel.add(scrollPane, BorderLayout.CENTER);
    return panel;
}
// Only this method changes - rest of UI unchanged!
```

**Benefits of this structure:**
- ✅ Easy to replace placeholders with real components
- ✅ Each panel is self-contained
- ✅ Layout already correct
- ✅ Window sizing already correct
- ✅ Part 3 only modifies panel contents, not structure

---

## ✅ Summary of PART 2

### What We Built

**PHASE 6 - DAO Implementation**
- Implemented ContactDAOImpl with all CRUD operations
- Used PreparedStatement for SQL injection prevention
- Used try-with-resources for automatic resource cleanup
- Implemented soft delete pattern
- Mapped ResultSet to Contact objects

**PHASE 7 - Service Layer**
- Created ContactService as business logic coordinator
- Validated input before calling DAO
- Enforced business rules
- Translated SQLException to user-friendly exceptions
- Provided clean API for UI layer

**PHASE 8 - Validation Layer**
- Created ContactValidator for centralized validation
- Validated name, phone, email with regex
- Enforced length limits matching database constraints
- Provided both throwing and boolean validation methods
- Protecting database integrity

**PHASE 9 - Main Entry Point**
- Created Main class as application launcher
- Used SwingUtilities.invokeLater() for thread safety
- Explained Event Dispatch Thread (EDT)
- Initialized application correctly

**PHASE 10 - Base UI Frame**
- Created ContactDashboardUI extending JFrame
- Used BorderLayout for responsive design
- Created placeholder panels for toolbar, content, status bar
- Set up window properties (size, location, close operation)
- Prepared structure for JTable integration in Part 3

### Backend Architecture Complete

The backend is now fully functional:
```
UI Layer (Part 2 - basic structure)
    ↓
Service Layer (Part 2 - validation & business logic) ✓
    ↓
DAO Layer (Part 2 - database operations) ✓
    ↓
Database (Part 1 - schema & design) ✓
```

### What's Next

PART 3 will build the full UI:
- JTable with contact data display
- Toolbar with action buttons (Add, Edit, Delete)
- Search functionality
- Contact form dialog
- Recycle bin dialog
- Statistics panel
- Complete event handling

---

**README_PART_2 for PART 2 Complete. Ready for PART 3.**
