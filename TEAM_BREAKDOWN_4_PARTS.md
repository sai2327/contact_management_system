# ContactManager Project - Team Breakdown (4 Parts)

## 📋 Project Overview
**Contact Manager System** - A professional desktop CRM application built with Java Swing that allows users to manage contacts, with user authentication, batch operations, import/export, and advanced security features.

**Architecture**: MVC (Model-View-Controller) + Service Layer + DAO Pattern
**Database**: MySQL
**GUI Framework**: Swing
**Total Features**: 20+ advanced features implemented

---

## 🏗️ Project Structure (4 Parts)

```
ContactManagerSwing/
├── Part 1: DATABASE & DAO LAYER (Backend/Data Persistence)
│   ├── dao/
│   ├── util/
│   └── model/
│
├── Part 2: SERVICE & BUSINESS LOGIC LAYER
│   ├── service/
│   └── Supporting utilities
│
├── Part 3: AUTHENTICATION & SECURITY MODULE
│   ├── LoginDialog.java
│   ├── ForgotPasswordDialog.java
│   ├── RecoveryKeyDialog.java
│   ├── ProfileDialog.java
│   └── Related services
│
└── Part 4: UI & FRONTEND LAYER (User Interface)
    ├── ContactUI.java (Main Dashboard)
    ├── Dialogs (Add, Edit, Delete, Import, Batch Ops)
    ├── Components (Statistics, Preview, Toast, Themes)
    ├── Main.java (Entry Point)
    └── Resources (Icons, Emojis)
```

---

# 🔴 **PART 1: DATABASE & DAO LAYER** (Backend Developer / Database Specialist)

## Role: Data Persistence & Database Management

### Responsibilities:
- Implement database schema and tables
- Handle all CRUD (Create, Read, Update, Delete) operations
- Execute complex SQL queries
- Manage user data isolation and security
- Handle data import/export

### Key Files:

#### **📁 dao/ (Data Access Object)**
| File | Purpose | Technologies |
|------|---------|--------------|
| `ContactDAO.java` | Interface defining all contact database operations | Java Interface, Generics |
| `ContactDAOImpl.java` | Implementation of ContactDAO using MySQL | JDBC, PreparedStatement, SQL |
| `UserDAO.java` | Interface for user database operations | Java Interface |
| `UserDAOImpl.java` | User authentication, registration, password recovery | JDBC, SHA-256 Hashing |

#### **📁 util/ (Database Utilities)**
| File | Purpose | Technologies |
|------|---------|--------------|
| `DBConnection.java` | Singleton pattern for MySQL connection management | JDBC, MySQL JDBC Driver |
| `DatabaseInitializer.java` | Auto-create tables on first run (DDL execution) | SQL DDL, Database Initialization |
| `SecurityUtil.java` | Password hashing (SHA-256 + Salt), OTP generation, Recovery keys | Cryptography, SecureRandom |

#### **📁 model/ (Data Models)**
| File | Purpose |
|------|---------|
| `Contact.java` | POJO representing a Contact (name, email, phone, category, etc.) |
| `User.java` | POJO representing a User (username, password_hash, email, security questions) |
| `ImportResult.java` | DTO for CSV import operation results (success count, errors) |

### Key Features Implemented:

1. **CRUD Operations**
   - Add single/batch contacts
   - Retrieve by ID, name, phone, search keyword
   - Update contact info
   - Delete (soft delete & hard delete)

2. **Advanced Queries**
   - Fuzzy duplicate detection (similar names/phones)
   - Contact autocomplete/suggestions
   - Advanced search (keyword + category + deleted filter)
   - Category-based filtering

3. **Batch Operations**
   - Soft delete multiple contacts
   - Hard delete multiple contacts
   - Batch update category

4. **Import/Export**
   - CSV import with error handling
   - CSV export for selected/all contacts

5. **User Data Isolation**
   - Multi-user support (each user sees only their contacts)
   - User-scoped queries (WHERE userId = ?)

6. **Security**
   - SQL injection prevention (PreparedStatement)
   - Password hashing (SHA-256 + Salt)
   - OTP verification
   - Recovery key validation

### Database Schema:
```sql
-- Users table
CREATE TABLE Users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) UNIQUE,
    password_hash VARCHAR(255),
    email VARCHAR(100),
    security_question VARCHAR(255),
    security_answer_hash VARCHAR(255),
    recovery_key_hash VARCHAR(255),
    created_at TIMESTAMP
);

-- Contacts table
CREATE TABLE Contacts (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    name VARCHAR(100),
    phone VARCHAR(20),
    email VARCHAR(100),
    category VARCHAR(50),
    is_deleted BOOLEAN DEFAULT 0,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES Users(id)
);
```

### Technologies Used:
- **Language**: Java
- **Database**: MySQL 8.0+
- **Driver**: mysql-connector-j-9.6.0
- **Patterns**: DAO Pattern, Singleton Pattern
- **SQL**: CRUD, Transactions, Prepared Statements
- **Security**: SHA-256 Hashing, JDBC with PreparedStatement

---

# 🟡 **PART 2: SERVICE & BUSINESS LOGIC LAYER** (Backend Developer / Logic Developer)

## Role: Business Logic & Data Orchestration

### Responsibilities:
- Act as bridge between UI and DAO layers
- Implement business logic validation
- Orchestrate multi-step operations
- Handle transactions
- Manage data transformations

### Key Files:

| File | Purpose |
|------|---------|
| `ContactService.java` | Bridge layer for all contact operations; delegates to DAO |
| `UserService.java` | User authentication, registration, password recovery logic |

### Key Features:

#### **ContactService**
- **GET operations**: All contacts, search, filter by category, autocomplete suggestions
- **CREATE**: Add single/batch contacts
- **UPDATE**: Update contact details
- **DELETE**: Soft delete (move to recycle bin), restore, hard delete
- **BATCH**: Batch delete, batch export, batch category update
- **ADVANCED**: Find duplicates, merge contacts, fuzzy search
- **IMPORT/EXPORT**: CSV operations with error handling
- **VALIDATION**: Check duplicates before insert

#### **UserService**
- **Authentication**: Login with username/password verification
- **Registration**: Create new user with validation
- **Password Recovery**: OTP-based password reset
- **Recovery Key**: Generate and validate recovery keys
- **Security**: Hash password, verify hash, manage user sessions
- **User Info**: Get user profile, update email, security questions

### Architecture Diagram:
```
UI Layer (ContactUI.java)
        ↓
Service Layer (ContactService, UserService)
        ↓
DAO Layer (ContactDAO, UserDAO)
        ↓
Database (MySQL)
```

### Business Logic Examples:
```java
// Duplicate detection before adding
ContactService.findDuplicates(name, phone);

// User registration with validation
UserService.registerUser(username, password, email);

// Password recovery flow
UserService.initiatePasswordRecovery(username);
UserService.verifyOTP(username, otp);
UserService.resetPassword(username, newPassword);

// Batch operations
ContactService.batchDelete(List<Integer> contactIds);
ContactService.batchUpdateCategory(List<Integer> ids, "Work");
```

### Technologies Used:
- **Language**: Java
- **Design Patterns**: 
  - Service Locator Pattern
  - Data Transfer Object (DTO)
  - Facade Pattern
- **Validation**: Input validation, business rule checks
- **Error Handling**: Exception handling, logging

---

# 🟢 **PART 3: AUTHENTICATION & SECURITY MODULE** (Full-Stack/Security Developer)

## Role: User Authentication, Authorization & Security

### Responsibilities:
- Implement secure login/registration flow
- Handle password recovery mechanisms
- Manage user profiles and security settings
- Implement 2FA with OTP and Recovery Keys
- Ensure user data protection

### Key Files:

| File | Purpose | Features |
|------|---------|----------|
| `LoginDialog.java` | Login/Registration UI with glassmorphism design | Tab-based UI, password toggle, remember username |
| `ForgotPasswordDialog.java` | OTP-based password reset flow | Send OTP via email, verify OTP, reset password |
| `RecoveryKeyDialog.java` | Recovery key generation and backup | Generate, display, copy to clipboard |
| `RecoveryKeyLoginDialog.java` | Alternative login using recovery key | 2FA backup method |
| `ProfileDialog.java` | User profile management | Update email, change password, security questions |
| `RecycleBinDialog.java` | Deleted contacts management | View, restore, permanently delete |
| `SecurityUtil.java` | Cryptographic utilities | Password hashing, OTP generation, recovery keys |

### Security Features:

1. **Password Management**
   - SHA-256 hashing with random salt
   - Constant-time comparison (prevents timing attacks)
   - Password strength validation
   - Support for password recovery

2. **Two-Factor Authentication (2FA)**
   - OTP (One-Time Password) with 5-minute expiry
   - Recovery keys as backup 2FA method
   - Max 5 OTP attempts before cooldown
   - 60-second cooldown between OTP requests

3. **User Registration Flow**
   ```
   Username Validation
   ├── Email Validation
   ├── Password Validation (min 8 chars)
   ├── Confirmation Email
   └── Account Activation
   ```

4. **Password Recovery Flow**
   ```
   Username/Email
   ├── Verify Security Question
   ├── Send OTP to Email
   ├── User Enters OTP
   ├── User Sets New Password
   └── Success
   ```

5. **Alternative Login (Recovery Key)**
   ```
   Username
   ├── Enter Recovery Key
   ├── Validate Recovery Key
   └── Login Success
   ```

### UI Components:

#### **LoginDialog**
```
┌─────────────────────────────────┐
│ ContactHub Sign In              │
├──────────────┬──────────────────┤
│ LOGIN        │ REGISTER         │
├──────────────┼──────────────────┤
│ • Username   │ • Username       │
│ • Password   │ • Email          │
│ • [Show]     │ • Password       │
│ • Remember   │ • Hint Question  │
│ • Login Btn  │ • Register Btn   │
│              │                  │
│ Forgot? Link │ Link to Recovery │
└──────────────┴──────────────────┘
```

#### **Security Features UI**
- **Password Show/Hide Toggle** - User can reveal password while typing
- **Email Verification** - Confirm email before account creation
- **Security Questions** - Additional verification for password recovery
- **Recovery Key Backup** - Generate and display recovery key at registration
- **OTP Verification** - SMS/Email OTP during password recovery
- **Profile Settings** - Update email, change password, manage recovery key

### Cryptography Technologies:
- **Hashing**: SHA-256 with 16-byte salt
- **Random Generation**: SecureRandom (CSPRNG)
- **Encoding**: Base64 for salt/hash encoding
- **Timing Attack Prevention**: MessageDigest.isEqual()

### Authentication Flow:
```
User Enters Credentials
        ↓
SecurityUtil.verifyPassword()
    (Decode salt, hash input, compare)
        ↓
If Valid → Load UserService.getUser()
        ↓
Return User object to ContactUI
        ↓
Set currentUserId in ContactService
        ↓
All queries scoped to this userId
```

### Technologies Used:
- **Language**: Java Swing
- **Cryptography**: SHA-256, SecureRandom, Base64
- **Email**: (For OTP delivery - can integrate JavaMailSender)
- **UI Patterns**: 
  - Dialog-based modals
  - Glassmorphism design
  - Tab-based forms
- **Validation**: Regex patterns for email/username/password
- **Storage**: Database (MySQL)

---

# 🔵 **PART 4: UI & FRONTEND LAYER** (Frontend/UI Developer)

## Role: User Interface Development, UX, Visual Design

### Responsibilities:
- Build responsive, modern UI components
- Implement user interactions and workflows
- Create professional visual design (themes, icons, animations)
- Handle user input validation and error messages
- Ensure smooth user experience

### Key Files:

#### **📁 Main Components**
| File | Purpose | Features |
|------|---------|----------|
| `Main.java` | Application entry point | DB connection test, UI initialization |
| `ContactUI.java` | Main dashboard/CRM interface | Table, search, filters, statistics |

#### **📁 Dialog Components (Data Entry)**
| File | Purpose | Features |
|------|---------|----------|
| `ContactFormDialog.java` | Add/Edit contact form | Input fields, validation, category selector |
| `ImportCSVDialog.java` | Import contacts from CSV | File chooser, progress bar, error reporting |
| `BatchOperationsDialog.java` | Bulk delete/export operations | Multi-select, progress tracking |
| `DialogHelper.java` | Reusable dialog utilities | Confirmation dialogs, error messages |

#### **📁 Advanced UI Components**
| File | Purpose | Features |
|------|---------|----------|
| `ContactPreviewPanel.java` | Side panel showing selected contact details | Avatar, contact info, action buttons |
| `StatisticsPanel.java` | Dashboard statistics panel | Contact count, category breakdown, charts |
| `AvatarPanel.java` | Avatar/profile picture display | Circular image, placeholder if no image |
| `Toast.java` | Non-blocking notification system | Success/error messages, auto-dismiss |
| `EmojiLabel.java` | Emoji rendering in UI | Category icons with emojis |

#### **📁 Design & Theming**
| File | Purpose | Features |
|------|---------|----------|
| `UITheme.java` | Light/Dark theme definitions | Colors, fonts, styling constants |
| `IconFactory.java` | Centralized icon management | SVG/PNG icons, icon caching |
| `IconRenderer.java` | Custom icon rendering | Table cell icons, button icons |
| `QRCodeDialog.java` | QR code generation/display | Contact sharing via QR code |

#### **📁 Resources**
| Folder | Purpose |
|--------|---------|
| `resources/icons/` | SVG/PNG icon files for buttons, categories, etc. |

### Main Dashboard Features (ContactUI):

```
┌──────────────────────────────────────────────────────┐
│ 📁 File  📋 View  🔧 Tools  ?  Help  👤 [ User v ]   │
├──────────────────────────────────────────────────────┤
│ [🔍 Search] [📁 Category ▼] [📊 Stats] [🎨 Theme]   │
│ [➕ Add] [✏️ Edit] [🗑️ Delete] [📂 Recycle Bin]      │
│ [📥 Import] [📤 Export] [📋 Batch] [👤 Profile]      │
├────────────────────────┬────────────────────────────┤
│                        │                            │
│ Contact Table          │ Contact Preview Panel      │
│                        │ ┌───────────────────────┐  │
│ Name │ Phone │ Email   │ │ 👤 Avatar             │  │
│ ─────┼───────┼─────────│ │                       │  │
│ John │ 123.. │ john..  │ │ Name: John Doe        │  │
│ Jane │ 456.. │ jane..  │ │ Phone: +1234567890    │  │
│      │       │         │ │ Email: john@email.com │  │
│      │       │         │ │ Category: Work        │  │
│      │       │         │ │ Created: 2025-01-15   │  │
│      │       │         │ │                       │  │
│      │       │         │ │ [Edit] [Delete]       │  │
│      │       │         │ └───────────────────────┘  │
└────────────────────────┴────────────────────────────┘
```

### User Workflows:

#### **1. Add Contact Flow**
```
Click "Add" Button
    ↓
ContactFormDialog opens (blank form)
    ├── User enters: Name, Phone, Email, Category
    ├── Validation: Check duplicates, format validation
    └── Save → Service.addContact() → Toast notification
```

#### **2. Search & Filter Flow**
```
User types in search box
    ↓
Real-time search suggestions (autocomplete)
    ├── If category selected → Filter by category
    ├── Advanced search (keyword + category + include deleted)
    └── Results update table dynamically
```

#### **3. Import Contacts Flow**
```
Click "Import" Button
    ↓
ImportCSVDialog opens file chooser
    ├── Select CSV file
    ├── Validation: Check file format
    ├── Progress bar while importing
    ├── Error reporting (duplicates, format errors)
    └── Success toast with count imported
```

#### **4. Delete Contact Flow**
```
Select contact → Click "Delete"
    ↓
Confirmation dialog
    ├── OK → Soft delete (move to Recycle Bin)
    ├── Trash icon shows in table
    └── User can restore from Recycle Bin
```

### UI Technologies:

1. **Swing Components**
   - JFrame, JDialog, JPanel (layouts)
   - JTable with DefaultTableModel (data display)
   - JTextField, JPasswordField, JComboBox (inputs)
   - JButton, JLabel, JMenuBar (controls)
   - JPanelwith CardLayout (tab-like switching)

2. **Layout Managers**
   - BorderLayout (main sections)
   - GridBagLayout (form layouts)
   - CardLayout (tab panels)
   - FlowLayout (button groups)

3. **Design Patterns**
   - MVC for dialog components
   - Observer pattern for table updates
   - Singleton for UI theme
   - Builder pattern for complex dialogs

4. **Advanced Features**
   - Table sorting (click column header)
   - Table row highlighting (animated)
   - Right-click context menu
   - Toast notifications (non-blocking)
   - Dark/Light theme toggle
   - Custom cell renderers (icons, colors)
   - Search suggestions popup

5. **Visual Effects**
   - Smooth animations for row highlights
   - Rounded corners for panels
   - Gradient backgrounds
   - Theme-aware colors
   - Icon badges for categories

### UI Customization:

#### **Theme System**
```java
UITheme.setTheme(Theme.LIGHT); // Light theme
UITheme.setTheme(Theme.DARK);  // Dark theme

// Colors adapt automatically
- Primary Color: Blue/Green/Purple (user selectable)
- Text Color: White (dark) / Black (light)
- Background: Dark gray (dark mode) / Light gray (light mode)
```

#### **Icon System**
```java
Icon addIcon = IconFactory.getIcon("add", IconSize.MEDIUM);
Icon deleteIcon = IconFactory.getIcon("delete", IconSize.SMALL);

// Icons cached for performance
```

### Accessibility Features:
- Keyboard navigation (Tab, Enter, Escape)
- Tooltips on buttons
- Status bar for action feedback
- Error messages in dialogs
- Color-coded indicators
- Font size options

### Technologies Used:
- **GUI Framework**: Java Swing
- **Language**: Java
- **Design**: Glassmorphism, Material Design inspired
- **Icons**: SVG/PNG with custom rendering
- **Themes**: Custom color systems (Light/Dark)
- **Input Validation**: Regex, user feedback
- **Notifications**: Toast notifications
- **Data Binding**: Manual (Swing tables)
- **Animation**: Timer-based (smooth transitions)

---

## 📊 Integration Points Between Parts

```
Part 1 (Database) ←→ Part 2 (Service)
    ↑                    ↑
    └────────────────────→ Part 3 (Auth) & Part 4 (UI)
    
Flow:
1. User logs in (Part 3)    → Sends credentials to Part 2
2. Part 2 validates         → Delegates to Part 1 for lookup
3. Part 1 returns User obj  → Part 2 passes to Part 3
4. Part 4 displays UI       → Calls Part 2 for all operations
5. Part 2 enforces scoping  → Adds userId to all Part 1 queries
```

---

## 🛠️ Tech Stack Summary

| Component | Technology | Version |
|-----------|-----------|---------|
| **Language** | Java | 11+ |
| **GUI Framework** | Swing | Built-in |
| **Database** | MySQL | 8.0+ |
| **JDBC Driver** | mysql-connector-j | 9.6.0 |
| **Build Tool** | Batch Scripts | .bat |
| **IDE** | Any (VS Code, IntelliJ IDEA) | - |

---

## 📚 Key Design Patterns Used

| Pattern | Part | Purpose |
|---------|------|---------|
| **DAO Pattern** | Part 1 | Abstraction of data access logic |
| **Service Locator** | Part 2 | Bridge between UI and DAO |
| **Singleton** | Part 1, 3 | Database connection, Theme |
| **MVC** | Part 4 | UI organization |
| **Factory** | Part 4 | Icon and dialog creation |
| **Observer** | Part 4 | Table updates notification |
| **Facade** | Part 2 | Simplified service interface |

---

## 🚀 Development Workflow

1. **Part 1 Developer** setups database schema and implements DAO operations
2. **Part 2 Developer** creates service layer using Part 1's interfaces
3. **Part 3 Developer** implements authentication using Parts 1 & 2
4. **Part 4 Developer** builds UI, integrating all previous parts

All parts should communicate through well-defined interfaces (Service layer as the facade).

---

## 📝 File Statistics

| Part | Files | Lines of Code | Focus |
|------|-------|----------------|-------|
| Part 1 | 6 files | ~2000 | Data persistence, SQL, JDBC |
| Part 2 | 2 files | ~500 | Business logic, validation |
| Part 3 | 6 files | ~1500 | Authentication, security, UI |
| Part 4 | 15+ files | ~3000+ | UI components, user experience |
| **Total** | **30+ files** | **~7000+** | Full-featured CRM |

---

**End of Team Breakdown Document**
