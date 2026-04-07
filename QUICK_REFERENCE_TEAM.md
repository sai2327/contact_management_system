# 👥 Team Member Assignment - Quick Reference

## Team Structure (4 Members)

---

## 🔴 **MEMBER 1: Database & DAO Developer**
**Role:** Backend Database Specialist

### What They Build:
- Database schema design & SQL
- DAO interfaces and implementations  
- Database initialization scripts
- Complex SQL queries

### Key Folders/Files:
```
ContactManagerSwing/
├── dao/
│   ├── ContactDAO.java (Interface)
│   ├── ContactDAOImpl.java (CRUD Implementation)
│   ├── UserDAO.java
│   └── UserDAOImpl.java
├── model/
│   ├── Contact.java
│   ├── User.java
│   └── ImportResult.java
└── util/
    ├── DBConnection.java
    └── DatabaseInitializer.java
```

### How Feature is Implemented:
1. **Design Database Schema** → All tables with relationships
2. **Write SQL Queries** → INSERT, SELECT, UPDATE, DELETE with PreparedStatement
3. **Implement CRUD** → Create methods in ContactDAOImpl, UserDAOImpl
4. **Handle Data Isolation** → Add `WHERE user_id = ?` to all queries
5. **Test Connections** → Validate MySQL connectivity

### Example Implementation:
```java
// ContactDAOImpl.java - Add a contact
public void addContact(Contact c) {
    String sql = "INSERT INTO contacts(user_id, name, phone, email, category) VALUES(?,?,?,?,?)";
    try (Connection conn = DBConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setInt(1, c.getUserId());
        stmt.setString(2, c.getName());
        stmt.setString(3, c.getNumber());
        stmt.setString(4, c.getEmail());
        stmt.setString(5, c.getCategory());
        stmt.executeUpdate();
    }
}
```

### Technologies:
```
MySQL 8.0+
├── SQL (DDL, DML, Queries)
├── Transactions
└── Indexes

Java
├── JDBC (mysql-connector-j-9.6.0)
├── PreparedStatement
├── DAO Pattern
└── Singleton Pattern
```

### Key Skills Needed:
- ✅ SQL (CRUD queries, complex joins)
- ✅ JDBC API
- ✅ Database design
- ✅ Connection pooling
- ✅ Transaction management

---

## 🟡 **MEMBER 2: Service & Business Logic Developer**
**Role:** Backend Logic Developer

### What They Build:
- Service layer (bridge between UI and DAO)
- Business logic & validation
- Transaction orchestration
- Data transformation

### Key Files:
```
ContactManagerSwing/service/
├── ContactService.java
└── UserService.java
```

### How Feature is Implemented:
1. **Delegate to DAO** → Call dao methods
2. **Add Validation** → Check business rules (no duplicates, etc.)
3. **Transform Data** → Convert between DTOs and POJOs
4. **Handle Exceptions** → Try-catch and meaningful errors
5. **Implement Logic** → Multi-step operations (e.g., password recovery)

### Example Implementation:
```java
// ContactService.java - Add contact with validation
public void addContact(Contact c) {
    // Validation
    if (contactDAO.existsByName(c.getName())) {
        throw new IllegalArgumentException("Contact already exists");
    }
    
    // Business logic
    c.setCreatedAt(new Timestamp(System.currentTimeMillis()));
    
    // Delegate to DAO
    contactDAO.addContact(c);
}
```

### Features to Implement:
- ✅ Search with filters
- ✅ Batch delete/export
- ✅ Duplicate detection
- ✅ Contact merging
- ✅ User authentication validation
- ✅ Password recovery flow

### Technologies:
```
Java Core
├── Service Pattern
├── Exception Handling
├── Stream API (for data transformation)
├── Collections (List, HashMap)
└── Date/Time handling

Design Patterns
├── Facade
├── Service Locator
└── DTO Pattern
```

### Key Skills Needed:
- ✅ Java object design
- ✅ Collection APIs
- ✅ Exception handling
- ✅ Business logic design
- ✅ API design (service interfaces)

---

## 🟢 **MEMBER 3: Authentication & Security Developer**
**Role:** Full-Stack Security Developer

### What They Build:
- Login/Registration UI
- Password recovery flows
- OTP & Recovery key systems
- User profiles & settings
- Security utilities

### Key Files:
```
ContactManagerSwing/ui/
├── LoginDialog.java
├── ForgotPasswordDialog.java
├── RecoveryKeyDialog.java
├── RecoveryKeyLoginDialog.java
├── ProfileDialog.java
└── RecycleBinDialog.java

ContactManagerSwing/util/
└── SecurityUtil.java
```

### How Features are Implemented:

#### **1. Password Hashing**
```java
// SecurityUtil.java
String hash = SecurityUtil.hashPassword("mypassword");
// Returns: base64_salt$base64_hash

boolean valid = SecurityUtil.verifyPassword("mypassword", hash);
```

#### **2. OTP Flow**
```
User clicks "Forgot Password"
    ↓
Enter username → Verify security question
    ↓
System sends OTP to email
    ↓
User enters OTP (within 5 minutes)
    ↓
User sets new password
    ↓
Update password in database
```

#### **3. Recovery Key Generation**
```java
String recoveryKey = SecurityUtil.generateRecoveryKey();
// 24-character secure key for backup 2FA
```

### Features to Implement:
- ✅ User registration with email validation
- ✅ Secure login with password verification
- ✅ "Forgot Password" with OTP
- ✅ Recovery key generation & backup
- ✅ Alternative login via recovery key
- ✅ Profile settings (update email, password)
- ✅ Password strength validation
- ✅ Security questions for recovery
- ✅ Recycle bin (view deleted contacts)

### UI Workflows:

**LOGIN FLOW:**
```
┌─────────────────────────┐
│ Username: [________]    │
│ Password: [________] 🔓 │
│ □ Remember Me           │
│                         │
│ [LOGIN] [REGISTER]      │
│                         │
│ Forgot Password? Link   │
│ Use Recovery Key? Link  │
└─────────────────────────┘
```

**PASSWORD RECOVERY:**
```
1. Enter Username
2. Answer Security Question
3. System sends OTP to email
4. Enter 6-digit OTP
5. Enter New Password
6. Success!
```

### Technologies:
```
Cryptography
├── SHA-256 Hashing
├── SecureRandom (CSPRNG)
├── Base64 Encoding
└── Constant-time comparison

Swing UI
├── JDialog
├── Tab-based layout
├── Form validation
└── Real-time feedback

Java Security
├── java.security.MessageDigest
├── java.security.SecureRandom
├── java.util.Base64
└── java.security.MessageDigest.isEqual()
```

### Key Skills Needed:
- ✅ Cryptography fundamentals
- ✅ Swing UI development
- ✅ Form validation
- ✅ Security best practices
- ✅ User session management
- ✅ Email integration (for OTP)

---

## 🔵 **MEMBER 4: UI/Frontend Developer**
**Role:** Frontend UI/UX Developer

### What They Build:
- Main contact dashboard (ContactUI)
- Dialog components (add, edit, import, batch)
- UI components (statistics, preview, toast)
- Theming & icons
- User interactions

### Key Files:
```
ContactManagerSwing/ui/
├── ContactUI.java (Main Dashboard)
├── ContactFormDialog.java
├── ImportCSVDialog.java
├── BatchOperationsDialog.java
├── DialogHelper.java
├── ContactPreviewPanel.java
├── StatisticsPanel.java
├── AvatarPanel.java
├── Toast.java
├── EmojiLabel.java
├── UITheme.java
├── IconFactory.java
├── IconRenderer.java
└── QRCodeDialog.java

ContactManagerSwing/
├── Main.java
└── resources/icons/
    └── [icon files]
```

### How Features are Implemented:

#### **1. Main Dashboard (ContactUI)**
```java
// Key Components:
1. Menu Bar → File, View, Tools, Help
2. Tool Bar → Add, Edit, Delete, Import, Export buttons
3. Search Panel → Search box + Category filter
4. Contact Table → Sortable, right-click menu
5. Preview Panel → Selected contact details
6. Status Bar → User info, status messages

// User Interactions:
- Click column header to sort
- Type in search to filter real-time
- Select row to see preview
- Right-click for context menu
- Click buttons for dialogs
```

#### **2. Add/Edit Dialog**
```java
ContactFormDialog dialog = new ContactFormDialog(parent, contact);
dialog.setVisible(true);

// Components:
- Text fields: Name, Phone, Email
- Combo box: Category selector
- Buttons: Save, Cancel
- Error labels: Show validation errors
```

#### **3. Import CSV Dialog**
```
1. Show file chooser
2. Allow user to select CSV
3. Progress bar while importing
4. Show success/error count
5. Display error details if any
```

#### **4. Statistics Panel**
```
Total Contacts: 150
Work: 45 | Friends: 60 | Family: 45

[Chart visualization]
```

#### **5. Toast Notifications**
```java
Toast.success("Contact added successfully!", 3000); // 3 sec auto-dismiss
Toast.error("Failed to update contact", 3000);
```

### Features to Implement:
- ✅ Contact table with sorting/filtering
- ✅ Add/Edit/Delete contact dialogs
- ✅ Import CSV dialog with progress
- ✅ Batch operations dialog
- ✅ Search with autocomplete suggestions  
- ✅ Category filter dropdown
- ✅ Contact preview side panel
- ✅ Statistics dashboard
- ✅ Toast notifications (non-blocking)
- ✅ Right-click context menu
- ✅ Dark/Light theme toggle
- ✅ Icons for categories & actions
- ✅ Recycle bin view
- ✅ Profile settings access

### UI Layout Diagram:
```
┌─ MENU BAR ──────────────────────────────────────┐
│ File | View | Tools | Help                      │
├─────────────────────────────────────────────────┤
│ TOOL BAR                                        │
│ [➕] [✏️] [🗑️] [📂] [📥] [📤] [📋] [👤] [🎨] │
├──────────────────────────────────────────────────┤
│  [Search] [Category ▼]                          │
├────────────────────────┬─────────────────────────┤
│                        │                         │
│    CONTACT TABLE       │  PREVIEW PANEL          │
│                        │  ┌───────────────────┐  │
│  Name │Phone │Email    │  │ 👤 Contact Info   │  │
│  ──────────────────    │  │                   │  │
│  John  │123   │john@.. │  │ [Edit] [Delete]   │  │
│  Jane  │456   │jane@.. │  │                   │  │
│                        │  └───────────────────┘  │
│                        │                         │
├────────────────────────┴─────────────────────────┤
│ STATUS BAR: Ready | User: john_doe | 10 contacts
└──────────────────────────────────────────────────┘
```

### Technologies:
```
Swing Components
├── JFrame, JDialog, JPanel
├── JTable with TableModel
├── JTextField, JComboBox
├── JButton, JLabel, JMenu
├── JToolBar, JScrollPane
├── CardLayout (tabs)
└── BorderLayout, GridBagLayout

Advanced Features
├── Custom TableCellRenderer (icons)
├── TableRowSorter (sorting)
├── JPopupMenu (right-click)
├── Timer (animations)
├── Image handling
└── Custom painting

Design Elements
├── Glassmorphism design
├── Theme system (Light/Dark)
├── Icon system (Factory pattern)
├── Toast notifications
└── Smooth animations
```

### Key Skills Needed:
- ✅ Swing API (components, layouts)
- ✅ UI/UX design principles
- ✅ Event handling (mouse, keyboard)
- ✅ Graphics & painting
- ✅ Layout managers
- ✅ Custom cell renderers
- ✅ Theme/styling
- ✅ Icon design/usage

---

## 🔗 How They Work Together

### Data Flow Example: "Add Contact"
```
User clicks [Add] Button
    ↓ (Member 4 - UI)
ContactFormDialog opens, user enters data, clicks Save
    ↓ (Member 4 - UI validates)
Check for duplicates using Service layer
    ↓ (Member 2 - Service)
ContactService.addContact() validates & delegates
    ↓ (Member 1 - Database)
ContactDAO.addContact() executes INSERT query
    ↓ (Database stores data)
Success!
    ↓ (Member 4 - UI)
Toast notification "Contact added successfully!"
    ↓
Table updates with new contact
```

### Authentication Flow: "User Logs In"
```
User enters username & password in LoginDialog
    ↓ (Member 4 - UI)
Click Login button
    ↓ (Member 3 - Security)
LoginDialog calls UserService.login(username, password)
    ↓ (Member 2 - Service)
UserService calls UserDAO.getUserByUsername()
    ↓ (Member 1 - Database)
DAO returns User object with password_hash
    ↓ (Member 3 - Security)
SecurityUtil.verifyPassword(input, storedHash)
    ↓ (Member 2 - Service)
If valid, return User object
    ↓ (Member 3 - Security)
Pass User to ContactUI, set currentUserId
    ↓ (Member 4 - UI)
Show main dashboard with user's contacts
    ↓ (All Members)
All contact queries now scoped to this userId
```

---

## 📋 Task Checklist by Member

### Member 1 (Database):
- [ ] Create database schema (users, contacts tables)
- [ ] Implement ContactDAO methods (CRUD)
- [ ] Implement UserDAO methods
- [ ] Handle user data isolation (userId scoping)
- [ ] Write CSV import/export queries
- [ ] Test all SQL queries with sample data
- [ ] Document database structure

### Member 2 (Service):
- [ ] Create ContactService with all methods
- [ ] Create UserService with auth logic
- [ ] Implement duplicate detection
- [ ] Implement batch operations
- [ ] Add validation & error handling
- [ ] Create DTOs for data transfer
- [ ] Test service layer methods
- [ ] Document service APIs

### Member 3 (Security):
- [ ] Implement password hashing (SHA-256 + salt)
- [ ] Implement OTP generation & verification
- [ ] Implement recovery key system
- [ ] Build LoginDialog (login + register tabs)
- [ ] Build ForgotPasswordDialog (OTP flow)
- [ ] Build RecoveryKeyDialog (key backup)
- [ ] Build ProfileDialog (update settings)
- [ ] Integrate with UserService

### Member 4 (UI):
- [ ] Build main ContactUI dashboard
- [ ] Create ContactFormDialog (add/edit)
- [ ] Create ImportCSVDialog
- [ ] Create BatchOperationsDialog
- [ ] Implement statistics panel
- [ ] Implement contact preview panel
- [ ] Create theme system (Light/Dark)
- [ ] Implement search with suggestions
- [ ] Add toast notifications
- [ ] Create icon system
- [ ] Implement sorting & filtering
- [ ] Build recycle bin dialog
- [ ] Polish UI/UX

---

## 🎯 Success Criteria

| Part | Success Criteria |
|------|-----------------|
| **Part 1** | All CRUD queries work, data isolation enforced, no SQL injection possible |
| **Part 2** | Service layer is stable, business logic is testable, no UI logic in services |
| **Part 3** | Passwords are hashed securely, OTP flow works end-to-end, recovery key system functional |
| **Part 4** | UI is responsive, tables sort/filter work, all dialogs are modal, theme toggling works |

---

## 📞 Inter-Member Communication Points

### Member 1 ↔ Member 2
- Share DAO interface contracts
- Agree on database schema design
- Define how userId scoping works

### Member 2 ↔ Member 3
- UserService must provide: login(), register(), verifyOTP(), resetPassword()
- SecurityUtil provides: hashPassword(), verifyPassword(), generateOTP()

### Member 2 ↔ Member 4
- Service methods must return clean objects (Contact, User, ImportResult)
- UI must always call Service, never call DAO directly
- Define exceptions/error codes to display

### Member 3 ↔ Member 4
- LoginDialog must call UserService methods
- After login, pass User object to ContactUI
- ContactUI sets userId in ContactService

### Member 4 ↔ All
- All call ContactService/UserService only
- Service layer is the single integration point
- UI handles only display, not business logic

---

**Document Version:** 1.0  
**Last Updated:** 2025  
**Team Size:** 4 Members
