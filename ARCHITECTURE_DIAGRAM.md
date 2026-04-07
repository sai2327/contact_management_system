# 🏗️ ContactManager Architecture & Component Diagram

## System Architecture Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                      USER / CLIENT LAYER                        │
│                   (Part 4: UI Frontend Developer)               │
│                                                                 │
│  Main.java (Entry)                                              │
│       ↓                                                         │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │                ContactUI (Main Dashboard)               │   │
│  │                                                         │   │
│  │  ┌─────────────────────────────────────────────────┐   │   │
│  │  │ Menu | Toolbar | Search | Table | Preview     │   │   │
│  │  │ Statistics | Toast Notifications | Buttons     │   │   │
│  │  └─────────────────────────────────────────────────┘   │   │
│  │                     ↓ ↑ ↓ ↑ ↓ ↑                        │   │
│  │  ┌─ Multiple Dialog Layers ──────────────────────┐   │   │
│  │  │ • LoginDialog (Auth)                          │   │   │
│  │  │ • ContactFormDialog (Add/Edit)                │   │   │
│  │  │ • ImportCSVDialog (File Import)               │   │   │
│  │  │ • BatchOperationsDialog (Bulk Operations)     │   │   │
│  │  │ • ProfileDialog (User Settings)               │   │   │
│  │  │ • RecycleBinDialog (Deleted Contacts)         │   │   │
│  │  │ • ForgotPasswordDialog (Password Recovery)    │   │   │
│  │  │ • RecoveryKeyDialog (2FA Backup Key)          │   │   │
│  │  └─────────────────────────────────────────────────┘   │   │
│  └─────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
                           ↓ ↑
                    Calls Only Service Layer
                           ↓ ↑
┌─────────────────────────────────────────────────────────────────┐
│                    SERVICE / BUSINESS LOGIC                      │
│              (Part 2: Service Layer Developer)                   │
│                                                                 │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │              ContactService                           │   │
│  │  ┌────────────────────────────────────────────────┐   │   │
│  │  │ • setCurrentUserId(userId)                   │   │   │
│  │  │ • getAllContacts()                           │   │   │
│  │  │ • addContact(contact)                        │   │   │
│  │  │ • updateContact(contact)                     │   │   │
│  │  │ • deleteContact(id) [soft delete]            │   │   │
│  │  │ • search(keyword, category, includeDeleted)  │   │   │
│  │  │ • batchDelete(ids)                           │   │   │
│  │  │ • batchUpdateCategory(ids, category)         │   │   │
│  │  │ • importFromCSV(filePath)                    │   │   │
│  │  │ • exportToCSV(filePath)                      │   │   │
│  │  │ • findDuplicates()                           │   │   │
│  │  │ • mergeContacts(keepId, mergeId)             │   │   │
│  │  └────────────────────────────────────────────────┘   │   │
│  └─────────────────────────────────────────────────────────┘   │
│                                                                 │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │              UserService                              │   │
│  │  ┌────────────────────────────────────────────────┐   │   │
│  │  │ • login(username, password)                   │   │   │
│  │  │ • register(username, password, email, hint)  │   │   │
│  │  │ • getUserByUsername(username)                │   │   │
│  │  │ • initiatePasswordRecovery(username)         │   │   │
│  │  │ • verifyOTP(username, otp)                   │   │   │
│  │  │ • resetPassword(username, newPassword)       │   │   │
│  │  │ • validateRecoveryKey(username, key)         │   │   │
│  │  │ • generateRecoveryKey(userId)                │   │   │
│  │  │ • updateProfile(user)                        │   │   │
│  │  └────────────────────────────────────────────────┘   │   │
│  └─────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
                           ↓ ↑
                    Delegates to DAO
                           ↓ ↑
┌─────────────────────────────────────────────────────────────────┐
│                    DATA ACCESS LAYER (DAO)                      │
│           (Part 1: Database Developer)                          │
│                                                                 │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │          ContactDAO (Interface)                        │   │
│  │  ┌────────────────────────────────────────────────┐   │   │
│  │  │ • setCurrentUserId(userId)                   │   │   │
│  │  │ • addContact(contact)                        │   │   │
│  │  │ • getAllContacts()                           │   │   │
│  │  │ • getById(id)                                │   │   │
│  │  │ • search(keyword)                           │   │   │
│  │  │ • searchByCategory(category)                │   │   │
│  │  │ • advancedSearch(...)                       │   │   │
│  │  │ • getSuggestions(prefix, limit)             │   │   │
│  │  │ • findSimilarContacts(name, phone)          │   │   │
│  │  │ • updateContact(contact)                    │   │   │
│  │  │ • softDeleteById(id)                        │   │   │
│  │  │ • restoreContact(id)                        │   │   │
│  │  │ • exportToCSV(filePath)                     │   │   │
│  │  │ • importFromCSV(filePath)                   │   │   │
│  │  │ • batchDelete(ids)                          │   │   │
│  │  │ • findDuplicates()                          │   │   │
│  │  │ • mergeContacts(keepId, mergeId)            │   │   │
│  │  └────────────────────────────────────────────────┘   │   │
│  │                     ↓ ↑                              │   │
│  │      ContactDAOImpl (Concrete Implementation)        │   │
│  │          (JDBC SQL with PreparedStatement)          │   │
│  └─────────────────────────────────────────────────────────┘   │
│                                                                 │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │          UserDAO & UserDAOImpl                         │   │
│  │  ┌────────────────────────────────────────────────┐   │   │
│  │  │ • addUser(user) / register                   │   │   │
│  │  │ • getUserByUsername(username)                │   │   │
│  │  │ • updatePassword(userId, newHash)            │   │   │
│  │  │ • updateOTPStatus(userId, otp, expiry)      │   │   │
│  │  │ • updateRecoveryKey(userId, keyHash)        │   │   │
│  │  │ • getUserSecurityQuestion(userId)           │   │   │
│  │  │ • verifySecurityAnswer(userId, answer)      │   │   │
│  │  └────────────────────────────────────────────────┘   │   │
│  └─────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
                           ↓ ↑
                    Uses Database / Security Utils
                           ↓ ↑
┌─────────────────────────────────────────────────────────────────┐
│                    UTILITY LAYER                                │
│                                                                 │
│  ┌──────────────────────────────────────────────────────┐      │
│  │ SecurityUtil                                        │      │
│  │ • hashPassword(password)                            │      │
│  │ • verifyPassword(password, storedHash)              │      │
│  │ • generateOTP()                                     │      │
│  │ • generateRecoveryKey()                             │      │
│  │ Algorithms: SHA-256 + Salt, SecureRandom, Base64    │      │
│  └──────────────────────────────────────────────────────┘      │
│                                                                 │
│  ┌──────────────────────────────────────────────────────┐      │
│  │ DBConnection                                        │      │
│  │ • getConnection() [Singleton Pattern]               │      │
│  │ • closeConnection()                                 │      │
│  │ • testConnection()                                  │      │
│  │ JDBC: mysql-connector-j-9.6.0                       │      │
│  └──────────────────────────────────────────────────────┘      │
│                                                                 │
│  ┌──────────────────────────────────────────────────────┐      │
│  │ DatabaseInitializer                                │      │
│  │ • initialize() [Auto-create tables on startup]      │      │
│  │ Executes DDL scripts                                │      │
│  └──────────────────────────────────────────────────────┘      │
└─────────────────────────────────────────────────────────────────┘
                           ↓ ↑
                      Database Driver
                           ↓ ↑
┌─────────────────────────────────────────────────────────────────┐
│                    PERSISTENCE LAYER                            │
│                    MySQL Database                               │
│                                                                 │
│  Database: java_project                                        │
│  ┌────────────────────────────────────────────────────────┐   │
│  │ Table: Users                                          │   │
│  │ ┌──────────────────────────────────────────────────┐  │   │
│  │ │ • id (INT PRIMARY KEY)                           │  │   │
│  │ │ • username (VARCHAR UNIQUE)                      │  │   │
│  │ │ • password_hash (VARCHAR SHA256)                 │  │   │
│  │ │ • email (VARCHAR)                                │  │   │
│  │ │ • security_question (VARCHAR)                    │  │   │
│  │ │ • security_answer_hash (VARCHAR)                 │  │   │
│  │ │ • recovery_key_hash (VARCHAR)                    │  │   │
│  │ │ • created_at (TIMESTAMP)                         │  │   │
│  │ └──────────────────────────────────────────────────┘  │   │
│  └────────────────────────────────────────────────────────┘   │
│                                                                 │
│  ┌────────────────────────────────────────────────────────┐   │
│  │ Table: Contacts                                      │   │
│  │ ┌──────────────────────────────────────────────────┐  │   │
│  │ │ • id (INT PRIMARY KEY)                          │  │   │
│  │ │ • user_id (INT, FOREIGN KEY → Users)            │  │   │
│  │ │ • name (VARCHAR)                                │  │   │
│  │ │ • phone (VARCHAR)                               │  │   │
│  │ │ • email (VARCHAR)                               │  │   │
│  │ │ • category (VARCHAR: Friends/Work/Family/...)   │  │   │
│  │ │ • is_deleted (BOOLEAN)                          │  │   │
│  │ │ • created_at (TIMESTAMP)                        │  │   │
│  │ │ • updated_at (TIMESTAMP)                        │  │   │
│  │ │ INDEX: (user_id, name)                          │  │   │
│  │ │ INDEX: (user_id, phone)                         │  │   │
│  │ │ INDEX: (user_id, is_deleted)                    │  │   │
│  │ └──────────────────────────────────────────────────┘  │   │
│  └────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
```

---

## Data Flow Examples

### Example 1: Add Contact Flow

```
USER CLICKS [ADD] BUTTON
    ↓
Part 4 (UI) - ContactUI.btnAdd.addActionListener()
    ↓
ContactFormDialog opens (modal dialog)
    ↓
User fills: Name, Phone, Email, Category
    ↓
User clicks [SAVE]
    ↓
Part 4 (UI) - Basic validation (empty checks, format)
    ↓
Create Contact object with userId
    ↓
Part 2 (Service) - contactService.addContact(contact)
    ↓
    ├─→ Check duplicates: contactDAO.findSimilarContacts()
    ├─→ If duplicate exists → throw exception → error at Part 4
    ├─→ Set timestamps
    └─→ Delegate: contactDAO.addContact(contact)
    ↓
Part 1 (DAO) - ContactDAOImpl.addContact(contact)
    ↓
    ├─→ Build SQL: INSERT INTO contacts VALUES(?,?,?,?,?)
    ├─→ Use PreparedStatement (prevents SQL injection)
    ├─→ Bind: userId, name, phone, email, category
    └─→ Execute INSERT
    ↓
Database - MySQL stores the contact
    ↓
SUCCESS: Return from DAO → Service → UI
    ↓
Part 4 (UI) - Toast.success("Contact added!")
    ↓
Refresh table: contactService.getAllContacts()
    ↓
Display new contact in table with animation
```

### Example 2: User Login Flow

```
APPLICATION STARTS
    ↓
Main.java - Entry point
    ↓
    ├─→ DBConnection.testConnection() [Part 1 Util]
    ├─→ DatabaseInitializer.initialize() [Part 1 Util]
    └─→ Set Swing Look & Feel
    ↓
Part 4 (UI) - Show LoginDialog
    ↓
LoginDialog - Tab: LOGIN
    ├─→ User enters username + password
    └─→ Clicks [LOGIN]
    ↓
Part 3 (UI) - LoginDialog.btnLogin.actionListener()
    ↓
    ├─→ Validate empty fields
    └─→ Call UserService.login(username, password)
    ↓
Part 2 (Service) - UserService.login()
    ↓
    ├─→ Delegate: userDAO.getUserByUsername(username)
    ↓
Part 1 (DAO) - UserDAOImpl.getUserByUsername()
    ↓
    ├─→ SQL: SELECT * FROM users WHERE username = ?
    ├─→ Use PreparedStatement (safe)
    ├─→ Return User object with password_hash
    ↓
Part 2 (Service) - Back to login()
    ↓
    ├─→ If User not found → throw exception
    └─→ Call SecurityUtil.verifyPassword(inputPassword, user.passwordHash)
    ↓
Part 3 (Util) - SecurityUtil.verifyPassword()
    ↓
    ├─→ Split stored hash: base64_salt$base64_hash
    ├─→ Decode salt from base64
    ├─→ Hash input password WITH stored salt
    ├─→ Compare hashes (constant-time comparison)
    ↓
Part 2 (Service) - Back from verification
    ↓
    ├─→ If password matches → return User object
    └─→ If mismatch → throw exception
    ↓
Part 3 (UI) - LoginDialog notification
    ↓
    ├─→ If success: Hide dialog, return User to Main.java
    └─→ If fail: Show error, clear password field
    ↓
Main.java - Received User object
    ↓
    ├─→ Create ContactUI(user)
    ├─→ Set userId in ContactService
    ├─→ Show ContactUI frame
    └─→ All future queries auto-scoped to userId
```

### Example 3: Search & Filter Flow

```
USER TYPES IN SEARCH BOX
    ↓
Part 4 (UI) - txtSearch.addKeyListener() / DocumentListener
    ↓
REAL-TIME: Every keystroke triggers:
    ├─→ Get search text
    ├─→ Get selected category filter
    └─→ Get "includeDeleted" checkbox state
    ↓
Part 2 (Service) - contactService.search(keyword, category, includeDeleted)
    ↓
Part 1 (DAO) - contactDAO.advancedSearch()
    ↓
    SQL Builds Dynamically:
    SELECT * FROM contacts 
    WHERE user_id = ?
    AND (name LIKE ? OR phone LIKE ? OR email LIKE ?)
    AND (category = ? OR category IS NULL)
    AND (is_deleted = 0 OR is_deleted = 1)  [based on checkbox]
    ↓
Database executes query
    ↓
Part 1 (DAO) returns List<Contact>
    ↓
Part 4 (UI) - Update table with results
    ↓
Also show autocomplete dropdown:
    contactService.getSuggestions(prefix, 10)
    ↓
Display 10 matching names in dropdown
    ↓
User clicks on suggestion
    ↓
Fill search box, apply filters
    ↓
Update table
```

---

## Component Interactions: Who Calls Whom

```
                    ┌─ ContactUI
                    │   └─→ ContactService (always)
                    │       └─→ ContactDAO (never direct)
                    │
        Part 4 ─────┼─ LoginDialog
        (UI)        │   └─→ UserService
        Component   │       └─→ SecurityUtil
        Hierarchy   │       └─→ UserDAO
                    │
                    └─ Various Dialogs
                        └─→ ContactService / UserService
                            └─→ DAO Layer

Rule: UI Never Talks Directly to DAO
      Service Always Stands Between
```

---

## Thread & Event Flow

```
Event Dispatch Thread (EDT)
    ↓
User clicks button on JFrame
    ↓
Event fires on EDT
    ↓
Swing calls actionListener() on EDT
    ↓
Part 4 (UI) - Lightweight operations on EDT
    ├─→ Get form input
    ├─→ Basic validation
    └─→ Create objects
    ↓
For Heavy Operations (DB):
    ├─→ Part 2 (Service) - Can run on EDT (simple delegation)
    └─→ Part 1 (DAO) - JDBC blocks on EDT
        (OK for small queries, but could use Thread pool
         for large batch operations)
    ↓
Results back to EDT
    ↓
Part 4 (UI) - Update components
    ├─→ repaint() table
    ├─→ Show Toast
    └─→ Refresh UI
```

---

## Security Architecture

```
┌─────────────────────────────────────────────────┐
│           User Request (Password)               │
└──────────────────┬────────────────────────────────┘
                   ↓
         ┌─────────────────────┐
         │  SecurityUtil       │
         │  hashPassword()     │
         │  - Generate salt    │
         │  - SHA-256 hash     │
         │  - Base64 encode    │
         │  Returns:           │
         │  base64_salt$hash   │
         └─────────────────────┘
                   ↓
         Store in Database
         users.password_hash
                   ↓
        ┌──────────────────────────────┐
        │   During Login:              │
        │   User enters password       │
        └──────────────┬───────────────┘
                       ↓
         ┌─────────────────────────────┐
         │ SecurityUtil.verify()       │
         │ - Parse stored hash         │
         │ - Extract salt              │
         │ - Hash input WITH salt      │
         │ - Constant-time compare     │
         │ - Returns: boolean          │
         └──────────┬────────────────────┘
                    ↓
          Result to LoginDialog
                    ↓
          ├─→ True: Login success
          └─→ False: Login fail
```

---

## Database Query Scoping (User Isolation)

```
Every Query Must Include userId Check:

✅ GOOD:
    SELECT * FROM contacts 
    WHERE user_id = ? 
    AND (name LIKE ? OR phone LIKE ?)
    [setInt(1, currentUserId)]

❌ BAD:
    SELECT * FROM contacts 
    WHERE name LIKE ?
    [This returns all contacts for all users!]

How Scoping Works:
    1. User logs in
    2. Main.java passes User object to ContactUI
    3. ContactUI passes userId to ContactService.setCurrentUserId()
    4. ContactService stores currentUserId internally
    5. Every ContactService method passes userId to DAO
    6. Every DAO method adds "WHERE user_id = ?" to SQL
    7. ContactDAOImpl keeps track of currentUserId
    8. All queries are automatically scoped
```

---

## Technologies @ Each Layer

```
LAYER               TECHNOLOGIES                  PARADIGM
─────────────────────────────────────────────────────────────
Part 4: UI          Swing, AWT, Graphics2D        Event-driven,
                    MVC, Observer                 Component-based

Part 2: Service     Java Collections,             Facade Pattern,
                    Lambda, Stream API            Delegation

Part 3: Security    SHA-256, SecureRandom,        Cryptography,
                    Base64, MessageDigest         Security best practices

Part 1: DAO         JDBC, PreparedStatement,      DAO Pattern,
                    SQL (DDL/DML)                 Singleton, Repository

Database            MySQL 8.0+, InnoDB            Relational, ACID
                    SQL, Indexes                  

File System         CSV files, Batch scripts      File I/O
```

---

## Key Architectural Principles

1. **Separation of Concerns**
   - UI handles display only
   - Service handles business logic
   - DAO handles persistence
   - Utils handle cross-cutting concerns

2. **Single Responsibility**
   - Each class has one reason to change
   - DAO: database queries only
   - Service: business logic only
   - UI: user interactions only

3. **Dependency Inversion**
   - UI depends on Service interface
   - Service depends on DAO interface
   - Low-level DAO doesn't depend on UI

4. **User Data Isolation**
   - Every query includes userId filter
   - No user can see another user's contacts
   - Database schema enforces FK relationship

5. **Security First**
   - All passwords hashed (SHA-256 + salt)
   - All SQL queries use PreparedStatement
   - All user input validated
   - No hardcoded credentials
   - OTP for password recovery
   - Recovery keys for 2FA backup

