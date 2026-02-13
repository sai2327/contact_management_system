# CONTACT MANAGEMENT SYSTEM - COMPLETE RECONSTRUCTION GUIDE

## PART 4: Advanced CRUD Operations & Data Management

---

## Introduction to Part 4

**What Part 4 Builds:**

Part 4 completes the full CRUD (Create, Read, Update, Delete) lifecycle with professional-grade features:

- **Dialog-based forms** for adding and editing contacts
- **Soft delete + Recycle Bin** for safe deletion and recovery
- **Advanced search and filtering** for efficient data access
- **CSV import/export** for data portability and backup

**Why These Features Matter:**

1. **Dialog-based forms** create a clean, focused user experience by isolating data entry from the main interface
2. **Soft delete with restore** prevents accidental data loss (critical for production systems)
3. **Search and filter** make large contact lists manageable
4. **CSV import/export** enables data migration, backup, and integration with other tools (Excel, Google Sheets)

**How Part 4 Connects to Previous Parts:**

```
Part 1 (Foundation)        → Database + Model + DAO Interface
Part 2 (Backend)           → DAO Implementation + Service + Validation
Part 3 (Basic UI)          → JTable + Theme + Dashboard + Preview
Part 4 (Advanced Features) → Dialogs + Search + CSV + Recycle Bin
```

**Why Backend-to-Frontend Order:**

We built the service layer first (Part 2) so that all UI components (Parts 3-4) can safely delegate business logic. This prevents SQL from leaking into UI code.

---

## PHASE 16 — ADD CONTACT DIALOG

### Complete Implementation: AddContactDialog.java

```java
package ui;

import model.Contact;
import service.ContactService;
import util.ContactValidator;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;

/**
 * Modal dialog for adding a new contact.
 * Uses JDialog instead of JFrame to create a focused, blocking interface.
 */
public class AddContactDialog extends JDialog {
    
    // Service layer for database operations
    private ContactService contactService;
    
    // Form fields for user input
    private JTextField nameField;
    private JTextField phoneField;
    private JTextField emailField;
    private JTextField addressField;
    private JComboBox<String> categoryComboBox;
    
    // Reference to parent frame to refresh table after insertion
    private ContactDashboardUI parentFrame;
    
    /**
     * Constructor: sets up modal dialog with form fields
     * 
     * @param parent - the parent frame (for centering and refresh callback)
     */
    public AddContactDialog(ContactDashboardUI parent) {
        super(parent, "Add New Contact", true); // true = modal (blocks parent interaction)
        this.parentFrame = parent;
        this.contactService = new ContactService();
        
        initializeUI();
    }
    
    /**
     * Initializes the dialog UI with form fields and buttons
     */
    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setSize(450, 400);
        setLocationRelativeTo(parentFrame); // Center dialog relative to parent
        setResizable(false);
        
        // Create form panel with fields
        JPanel formPanel = createFormPanel();
        
        // Create button panel with Save and Cancel
        JPanel buttonPanel = createButtonPanel();
        
        // Add panels to dialog
        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    /**
     * Creates the form panel with labeled input fields
     */
    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8); // Padding between components
        
        // Row 0: Name field
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.3;
        panel.add(new JLabel("Name: *"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        nameField = new JTextField(20);
        panel.add(nameField, gbc);
        
        // Row 1: Phone field
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.3;
        panel.add(new JLabel("Phone:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        phoneField = new JTextField(20);
        panel.add(phoneField, gbc);
        
        // Row 2: Email field
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.3;
        panel.add(new JLabel("Email:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        emailField = new JTextField(20);
        panel.add(emailField, gbc);
        
        // Row 3: Address field
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0.3;
        panel.add(new JLabel("Address:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        addressField = new JTextField(20);
        panel.add(addressField, gbc);
        
        // Row 4: Category dropdown
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weightx = 0.3;
        panel.add(new JLabel("Category:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        categoryComboBox = new JComboBox<>(new String[]{
            "Family", "Friend", "Work", "Other"
        });
        panel.add(categoryComboBox, gbc);
        
        // Row 5: Required field note
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        JLabel noteLabel = new JLabel("* Required field");
        noteLabel.setFont(new Font("Arial", Font.ITALIC, 11));
        noteLabel.setForeground(Color.GRAY);
        panel.add(noteLabel, gbc);
        
        return panel;
    }
    
    /**
     * Creates the button panel with Save and Cancel actions
     */
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        
        // Save button
        JButton saveButton = new JButton("Save Contact");
        saveButton.setPreferredSize(new Dimension(120, 35));
        saveButton.addActionListener(e -> saveContact());
        
        // Cancel button
        JButton cancelButton = new JButton("Cancel");
        cancelButton.setPreferredSize(new Dimension(100, 35));
        cancelButton.addActionListener(e -> dispose()); // Close dialog without saving
        
        panel.add(saveButton);
        panel.add(cancelButton);
        
        return panel;
    }
    
    /**
     * Validates input and saves the new contact to database
     */
    private void saveContact() {
        try {
            // Step 1: Extract input from form fields
            String name = nameField.getText().trim();
            String phone = phoneField.getText().trim();
            String email = emailField.getText().trim();
            String address = addressField.getText().trim();
            String category = (String) categoryComboBox.getSelectedItem();
            
            // Step 2: Validate input using centralized validator
            ContactValidator.validateName(name);
            
            if (!phone.isEmpty()) {
                ContactValidator.validatePhone(phone);
            }
            
            if (!email.isEmpty()) {
                ContactValidator.validateEmail(email);
            }
            
            // Step 3: Create Contact object
            Contact newContact = new Contact();
            newContact.setName(name);
            newContact.setPhone(phone);
            newContact.setEmail(email);
            newContact.setAddress(address);
            newContact.setCategory(category);
            newContact.setDeleted(false); // New contacts start as active
            newContact.setCreatedAt(LocalDateTime.now());
            newContact.setUpdatedAt(LocalDateTime.now());
            
            // Step 4: Save to database via service layer
            contactService.addContact(newContact);
            
            // Step 5: Show success feedback
            Toast.show(this, "Contact added successfully!", Toast.SUCCESS);
            
            // Step 6: Refresh parent table to display new contact
            parentFrame.loadContacts();
            
            // Step 7: Close dialog
            dispose();
            
        } catch (IllegalArgumentException ex) {
            // Validation error: show user-friendly message
            JOptionPane.showMessageDialog(
                this,
                ex.getMessage(),
                "Validation Error",
                JOptionPane.ERROR_MESSAGE
            );
        } catch (Exception ex) {
            // Database error: show technical message
            JOptionPane.showMessageDialog(
                this,
                "Failed to add contact: " + ex.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }
}
```

### Explanation: Add Contact Dialog Architecture

**Purpose of AddContactDialog:**

This dialog provides a dedicated, focused interface for creating new contacts. It validates input, saves to database, and refreshes the main table—all while keeping the UI layer clean of SQL code.

---

**Why JDialog Instead of JFrame?**

```java
extends JDialog  // ✅ Correct for popups
extends JFrame   // ❌ Wrong: creates independent window
```

**JDialog Benefits:**

1. **Modal behavior**: Blocks interaction with parent window until dialog is closed
2. **Visual hierarchy**: User understands this is a temporary task
3. **Centering**: `setLocationRelativeTo(parent)` centers relative to main window
4. **Memory efficient**: Automatically disposed when closed

**What breaks if using JFrame:**
- Multiple windows compete for focus
- User can interact with main table while form is open (confusing)
- Form stays open after saving (poor UX)

---

**Why Modal Dialog is Critical:**

```java
super(parent, "Add New Contact", true); // true = modal
```

**Modal (true):**
- Blocks parent window interaction
- User must complete or cancel form before continuing
- Prevents data inconsistency (can't add 2 contacts simultaneously)

**Non-modal (false):**
- User can click main window while form is open
- Can open multiple add dialogs (database confusion)
- Poor user experience

---

**How Validation Connects to Service Layer:**

```
User Input → Validator → Service → DAO → Database
```

**Step-by-step flow:**

1. **User fills form** and clicks "Save"
2. **AddContactDialog extracts input** from text fields
3. **ContactValidator validates** name, phone, email formats
4. **If validation passes**, create Contact object
5. **ContactService.addContact()** is called
6. **Service calls DAO**, which executes SQL INSERT
7. **On success**, dialog shows success message
8. **Parent table refreshes** to display new contact

**Why this architecture:**

- **Single Responsibility**: Dialog handles UI, Validator handles rules, Service handles logic
- **Reusability**: Validation logic can be used by edit dialog, import dialog, etc.
- **Testability**: Each layer can be unit tested independently

---

**Why UI Must Not Contain SQL:**

**❌ Bad approach (SQL in UI):**

```java
private void saveContact() {
    String sql = "INSERT INTO contacts (name, phone) VALUES (?, ?)";
    Connection conn = DriverManager.getConnection(URL, USER, PASS);
    PreparedStatement stmt = conn.prepareStatement(sql);
    stmt.setString(1, nameField.getText());
    stmt.setString(2, phoneField.getText());
    stmt.executeUpdate();
}
```

**Problems:**

1. **SQL leaks into UI**: Dialog must know database schema
2. **No validation**: Invalid data reaches database
3. **No error handling**: Connection leaks if exception occurs
4. **Not reusable**: Cannot add contact from CSV import without duplicating SQL
5. **Hard to test**: Must mock database to test UI

---

**✅ Good approach (Layered architecture):**

```java
private void saveContact() {
    // Validation
    ContactValidator.validateName(name);
    
    // Business logic
    Contact contact = new Contact(...);
    contactService.addContact(contact);
    
    // UI updates
    parentFrame.loadContacts();
    dispose();
}
```

**Benefits:**

1. **UI is clean**: Only handles user interaction
2. **Validation is centralized**: Same rules for all entry points
3. **Service handles SQL**: UI doesn't know about database
4. **Reusable**: CSV import can call same service method
5. **Testable**: Can test service without Swing components

---

**Event Handling Explanation:**

**Save Button Click:**

```java
saveButton.addActionListener(e -> saveContact());
```

**What happens:**

1. User clicks "Save Contact" button
2. ActionListener fires event
3. Lambda expression `e -> saveContact()` is executed
4. `saveContact()` method runs on Event Dispatch Thread (EDT)
5. Validation, service call, and UI updates occur synchronously
6. If successful, dialog closes via `dispose()`

**Cancel Button Click:**

```java
cancelButton.addActionListener(e -> dispose());
```

- Closes dialog immediately
- No validation, no saving
- Parent table unchanged

---

**GridBagLayout Explanation:**

**Why GridBagLayout?**

```java
panel.setLayout(new GridBagLayout());
```

**Alternatives and why they fail:**

- **FlowLayout**: Fields would wrap to next line (ugly)
- **BorderLayout**: Can't align labels and fields nicely
- **BoxLayout**: Hard to align labels with equal width
- **GridLayout**: All cells same size (labels too wide)

**GridBagLayout benefits:**

- **Precise control**: Each component gets custom constraints
- **Alignment**: Labels align right, fields align left
- **Resizing**: Fields stretch, labels don't
- **Professional look**: Standard form layout

**Key constraints:**

```java
gbc.fill = GridBagConstraints.HORIZONTAL; // Stretch horizontally
gbc.insets = new Insets(8, 8, 8, 8);      // Padding
gbc.weightx = 0.3; // Label takes 30% width
gbc.weightx = 0.7; // Field takes 70% width
```

---

**Why Refresh Table After Insert:**

```java
parentFrame.loadContacts();
```

**What happens if we skip this:**

1. User adds contact "John Doe"
2. Database contains "John Doe"
3. JTable still shows old data (doesn't know about "John Doe")
4. User confused: "Did my save work?"

**Flow with refresh:**

```
Save → Database INSERT → loadContacts() → Query Database → Update TableModel → JTable refreshes
```

**Why this works:**

- `loadContacts()` queries database for latest data
- Converts `List<Contact>` to table rows
- Updates DefaultTableModel
- JTable automatically repaints

---

**What Breaks If Removed:**

**Without validation:**
- Empty names inserted: `""` in database
- Invalid emails: `"not-an-email"` accepted
- Malformed phones: `"abc123"` saved

**Without service layer:**
- SQL leaks into UI
- Cannot reuse add logic
- Hard to add logging, security, caching

**Without modal dialog:**
- User can open 10 dialogs simultaneously
- Duplicate contacts inserted
- Confusing user experience

**Without dispose():**
- Dialog stays open after saving
- Memory leak (dialogs never garbage collected)
- User clicks Save multiple times → duplicate inserts

---

## PHASE 17 — EDIT CONTACT DIALOG

### Complete Implementation: EditContactDialog.java

```java
package ui;

import model.Contact;
import service.ContactService;
import util.ContactValidator;

import javax.swing.*;
import java.awt.*;

/**
 * Modal dialog for editing an existing contact.
 * Pre-fills form fields with current contact data.
 */
public class EditContactDialog extends JDialog {
    
    private ContactService contactService;
    private Contact contactToEdit; // The contact being edited
    private ContactDashboardUI parentFrame;
    
    // Form fields (same as Add dialog)
    private JTextField nameField;
    private JTextField phoneField;
    private JTextField emailField;
    private JTextField addressField;
    private JComboBox<String> categoryComboBox;
    
    /**
     * Constructor: receives the contact to edit
     * 
     * @param parent - parent frame for centering and refresh
     * @param contact - the contact object to edit (must have valid ID)
     */
    public EditContactDialog(ContactDashboardUI parent, Contact contact) {
        super(parent, "Edit Contact", true);
        this.parentFrame = parent;
        this.contactToEdit = contact;
        this.contactService = new ContactService();
        
        initializeUI();
        populateFields(); // Pre-fill form with existing data
    }
    
    /**
     * Initializes the dialog UI (same structure as Add dialog)
     */
    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setSize(450, 400);
        setLocationRelativeTo(parentFrame);
        setResizable(false);
        
        JPanel formPanel = createFormPanel();
        JPanel buttonPanel = createButtonPanel();
        
        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    /**
     * Creates form panel with input fields
     */
    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);
        
        // Name field
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.3;
        panel.add(new JLabel("Name: *"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        nameField = new JTextField(20);
        panel.add(nameField, gbc);
        
        // Phone field
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.3;
        panel.add(new JLabel("Phone:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        phoneField = new JTextField(20);
        panel.add(phoneField, gbc);
        
        // Email field
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.3;
        panel.add(new JLabel("Email:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        emailField = new JTextField(20);
        panel.add(emailField, gbc);
        
        // Address field
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0.3;
        panel.add(new JLabel("Address:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        addressField = new JTextField(20);
        panel.add(addressField, gbc);
        
        // Category dropdown
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weightx = 0.3;
        panel.add(new JLabel("Category:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        categoryComboBox = new JComboBox<>(new String[]{
            "Family", "Friend", "Work", "Other"
        });
        panel.add(categoryComboBox, gbc);
        
        return panel;
    }
    
    /**
     * Creates button panel with Update and Cancel buttons
     */
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        
        // Update button (different from Add dialog's "Save")
        JButton updateButton = new JButton("Update Contact");
        updateButton.setPreferredSize(new Dimension(140, 35));
        updateButton.addActionListener(e -> updateContact());
        
        JButton cancelButton = new JButton("Cancel");
        cancelButton.setPreferredSize(new Dimension(100, 35));
        cancelButton.addActionListener(e -> dispose());
        
        panel.add(updateButton);
        panel.add(cancelButton);
        
        return panel;
    }
    
    /**
     * Pre-fills form fields with existing contact data
     * Called after UI initialization
     */
    private void populateFields() {
        nameField.setText(contactToEdit.getName());
        phoneField.setText(contactToEdit.getPhone());
        emailField.setText(contactToEdit.getEmail());
        addressField.setText(contactToEdit.getAddress());
        categoryComboBox.setSelectedItem(contactToEdit.getCategory());
    }
    
    /**
     * Validates input and updates the contact in database
     */
    private void updateContact() {
        try {
            // Step 1: Extract updated values from form
            String name = nameField.getText().trim();
            String phone = phoneField.getText().trim();
            String email = emailField.getText().trim();
            String address = addressField.getText().trim();
            String category = (String) categoryComboBox.getSelectedItem();
            
            // Step 2: Validate input
            ContactValidator.validateName(name);
            
            if (!phone.isEmpty()) {
                ContactValidator.validatePhone(phone);
            }
            
            if (!email.isEmpty()) {
                ContactValidator.validateEmail(email);
            }
            
            // Step 3: Update the Contact object (KEEP SAME ID)
            contactToEdit.setName(name);
            contactToEdit.setPhone(phone);
            contactToEdit.setEmail(email);
            contactToEdit.setAddress(address);
            contactToEdit.setCategory(category);
            // Note: ID, createdAt, isDeleted remain unchanged
            
            // Step 4: Update in database via service layer
            contactService.updateContact(contactToEdit);
            
            // Step 5: Show success feedback
            Toast.show(this, "Contact updated successfully!", Toast.SUCCESS);
            
            // Step 6: Refresh parent table
            parentFrame.loadContacts();
            
            // Step 7: Close dialog
            dispose();
            
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(
                this,
                ex.getMessage(),
                "Validation Error",
                JOptionPane.ERROR_MESSAGE
            );
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                this,
                "Failed to update contact: " + ex.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }
}
```

### Explanation: Edit Contact Dialog Architecture

**Purpose of EditContactDialog:**

Allows users to modify existing contact information while preserving the contact's ID, creation timestamp, and deletion status. This implements the UPDATE operation in CRUD.

---

**Why We Pass Contact Object:**

```java
public EditContactDialog(ContactDashboardUI parent, Contact contact)
```

**Correct approach:**

- Parent frame queries selected row from JTable
- Retrieves full Contact object (with ID)
- Passes object to dialog constructor
- Dialog pre-fills form fields
- On update, same ID is used in SQL UPDATE

**Why this works:**

```
ID: 42
UPDATE contacts SET name='New Name' WHERE id=42;
```

**What breaks if we don't pass Contact:**

```java
// ❌ Bad: Only pass name
new EditContactDialog(parent, "John Doe");
```

**Problems:**

1. **No ID**: Cannot identify which record to update
2. **No pre-fill**: Fields start empty (bad UX)
3. **Duplicate search**: Must query database again to find contact
4. **Name collision**: Multiple "John Doe" contacts exist—which one to update?

---

**Why We Update By ID (Not By Name):**

**✅ Correct (Update by ID):**

```java
contactService.updateContact(contactToEdit); // contactToEdit.getId() = 42

// Service → DAO → SQL:
UPDATE contacts 
SET name=?, phone=?, email=?, address=?, category=?, updated_at=NOW()
WHERE id=?;
```

**Benefits:**

1. **Unique identifier**: ID is primary key (guaranteed unique)
2. **Name can change**: User can rename "John" to "Jonathan"
3. **Concurrent updates**: Multiple users can edit different contacts safely
4. **Referential integrity**: Future features (contact groups) can reference by ID

---

**❌ Wrong (Update by name):**

```java
UPDATE contacts SET phone=? WHERE name=?;
```

**Problems:**

1. **Duplicate names**: "John Smith" appears 3 times—which one gets updated?
2. **Cannot rename**: Updating name would create new record
3. **Race conditions**: User A edits John #1, User B edits John #2, but SQL updates both
4. **No foreign key support**: Cannot link contacts to groups/tags

**Example scenario that breaks:**

```
Database has:
- ID 5: "Bob"
- ID 8: "Bob"  (different person, same name)

User edits ID 8:
UPDATE contacts SET phone='555-1234' WHERE name='Bob';

Result: BOTH Bob records get phone 555-1234 (wrong!)
```

---

**What Breaks If We Update By Name:**

**Scenario 1: Duplicate Names**

```
Initial state:
ID 10: "Alice Johnson", phone: "555-1111"
ID 15: "Alice Johnson", phone: "555-2222" (different person)

User selects ID 15 and changes phone to "555-3333"

SQL executed:
UPDATE contacts SET phone='555-3333' WHERE name='Alice Johnson';

Result:
ID 10: phone becomes "555-3333" (WRONG - we didn't select this one)
ID 15: phone becomes "555-3333" (correct)
```

**Scenario 2: Name Change**

```
User wants to rename "Bob" to "Robert"

SQL executed:
UPDATE contacts SET name='Robert' WHERE name='Bob';

Result:
- Database now has "Robert"
- Code searches for "Bob" to update phone
- Cannot find "Bob" (because we renamed it)
- Later update fails
```

---

**How Dialog Improves Modularity:**

**Modularity benefits:**

1. **Reusability**:
   - `AddContactDialog` for new contacts
   - `EditContactDialog` for updates
   - Both share validation logic
   - Both share form layout code

2. **Separation of concerns**:
   ```
   ContactDashboardUI     → Displays table, handles selection
   EditContactDialog      → Handles form input, validation
   ContactService         → Executes update logic
   ContactDAOImpl         → Executes SQL UPDATE
   ```

3. **Testing**:
   - Can test dialog in isolation
   - Can test service without UI
   - Can test DAO with mock data

**What breaks if we put edit form inside main window:**

```java
// ❌ Bad: Edit fields in main window
class ContactDashboardUI {
    private JTextField editNameField;
    private JTextField editPhoneField;
    // ... 10 more fields ...
    
    public void editContact() {
        // 200 lines of form code mixed with table code
    }
}
```

**Problems:**

1. **Messy UI**: Edit fields always visible (even when not editing)
2. **Cannot reuse**: Edit logic locked inside dashboard
3. **Hard to test**: Must instantiate entire dashboard to test edit
4. **Poor UX**: User can interact with table while editing (confusing)

---

**populateFields() Method Explanation:**

```java
private void populateFields() {
    nameField.setText(contactToEdit.getName());
    phoneField.setText(contactToEdit.getPhone());
    emailField.setText(contactToEdit.getEmail());
    addressField.setText(contactToEdit.getAddress());
    categoryComboBox.setSelectedItem(contactToEdit.getCategory());
}
```

**Why this is called after UI initialization:**

1. **Order matters**:
   ```java
   initializeUI();    // Creates nameField
   populateFields();  // Sets nameField.setText()
   ```

2. **What breaks if reversed**:
   ```java
   populateFields();  // nameField is null → NullPointerException
   initializeUI();    // nameField created, but already populated with null
   ```

**Why pre-filling improves UX:**

- **User sees current values**: Knows what they're editing
- **Partial edits**: Can change phone without re-typing name
- **Cancel detection**: User can see changes before saving

**What breaks if we don't pre-fill:**

```java
// ❌ Bad: Empty fields
nameField.setText(""); // User must re-type everything
```

**Result:**

- User opens edit dialog, sees empty form
- Thinks: "Did I select the right contact?"
- Must manually type everything again
- Accidentally leaves fields blank → overwrites data with empty strings

---

**Why Validation is Identical to Add Dialog:**

Both dialogs use same validation:

```java
ContactValidator.validateName(name);
ContactValidator.validatePhone(phone);
ContactValidator.validateEmail(email);
```

**Why this is good architecture:**

1. **Consistency**: Add and Edit enforce same rules
2. **Maintainability**: Change validation once, affects both dialogs
3. **Testability**: Test validator once, trust it everywhere

**What breaks if validation differs:**

```java
// ❌ Bad: Different rules
AddContactDialog:    name length < 50
EditContactDialog:   name length < 100

User adds "John" → works
User edits "John" to 60-char name → works
User can no longer add new contacts with 60-char names (inconsistent)
```

---

## PHASE 18 — SOFT DELETE + RECYCLE BIN DIALOG

### Complete Implementation: RecycleBinDialog.java

```java
package ui;

import model.Contact;
import service.ContactService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Modal dialog displaying soft-deleted contacts.
 * Allows restoring contacts or permanently deleting them.
 */
public class RecycleBinDialog extends JDialog {
    
    private ContactService contactService;
    private ContactDashboardUI parentFrame;
    
    // Table for displaying deleted contacts
    private JTable deletedContactsTable;
    private DefaultTableModel tableModel;
    
    // Action buttons
    private JButton restoreButton;
    private JButton permanentDeleteButton;
    private JButton closeButton;
    
    /**
     * Constructor: initializes recycle bin dialog
     * 
     * @param parent - parent frame for centering and refresh callback
     */
    public RecycleBinDialog(ContactDashboardUI parent) {
        super(parent, "Recycle Bin", true);
        this.parentFrame = parent;
        this.contactService = new ContactService();
        
        initializeUI();
        loadDeletedContacts();
    }
    
    /**
     * Initializes the dialog UI with table and buttons
     */
    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setSize(700, 500);
        setLocationRelativeTo(parentFrame);
        
        // Title panel
        JPanel titlePanel = createTitlePanel();
        
        // Table panel with deleted contacts
        JPanel tablePanel = createTablePanel();
        
        // Button panel with Restore, Delete, Close
        JPanel buttonPanel = createButtonPanel();
        
        add(titlePanel, BorderLayout.NORTH);
        add(tablePanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    /**
     * Creates title panel with informative message
     */
    private JPanel createTitlePanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
        
        JLabel titleLabel = new JLabel("🗑️ Deleted Contacts");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        
        JLabel infoLabel = new JLabel("  (Select a contact to restore or permanently delete)");
        infoLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        infoLabel.setForeground(Color.GRAY);
        
        panel.add(titleLabel);
        panel.add(infoLabel);
        
        return panel;
    }
    
    /**
     * Creates table panel displaying deleted contacts
     */
    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        
        // Define table columns
        String[] columnNames = {"ID", "Name", "Phone", "Email", "Category", "Deleted At"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Read-only table
            }
        };
        
        // Create JTable
        deletedContactsTable = new JTable(tableModel);
        deletedContactsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        deletedContactsTable.setRowHeight(25);
        deletedContactsTable.getColumnModel().getColumn(0).setPreferredWidth(50);  // ID
        deletedContactsTable.getColumnModel().getColumn(1).setPreferredWidth(150); // Name
        deletedContactsTable.getColumnModel().getColumn(2).setPreferredWidth(120); // Phone
        deletedContactsTable.getColumnModel().getColumn(3).setPreferredWidth(180); // Email
        deletedContactsTable.getColumnModel().getColumn(4).setPreferredWidth(100); // Category
        deletedContactsTable.getColumnModel().getColumn(5).setPreferredWidth(100); // Deleted At
        
        // Add scroll pane
        JScrollPane scrollPane = new JScrollPane(deletedContactsTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Empty state message
        if (tableModel.getRowCount() == 0) {
            JLabel emptyLabel = new JLabel("No deleted contacts", SwingConstants.CENTER);
            emptyLabel.setForeground(Color.GRAY);
            panel.add(emptyLabel, BorderLayout.SOUTH);
        }
        
        return panel;
    }
    
    /**
     * Creates button panel with action buttons
     */
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        
        // Restore button
        restoreButton = new JButton("♻️ Restore Selected");
        restoreButton.setPreferredSize(new Dimension(150, 35));
        restoreButton.addActionListener(e -> restoreContact());
        
        // Permanent delete button
        permanentDeleteButton = new JButton("❌ Delete Forever");
        permanentDeleteButton.setPreferredSize(new Dimension(150, 35));
        permanentDeleteButton.setForeground(Color.RED);
        permanentDeleteButton.addActionListener(e -> permanentlyDeleteContact());
        
        // Close button
        closeButton = new JButton("Close");
        closeButton.setPreferredSize(new Dimension(100, 35));
        closeButton.addActionListener(e -> dispose());
        
        panel.add(restoreButton);
        panel.add(permanentDeleteButton);
        panel.add(closeButton);
        
        return panel;
    }
    
    /**
     * Loads deleted contacts from database and populates table
     */
    private void loadDeletedContacts() {
        try {
            // Clear existing rows
            tableModel.setRowCount(0);
            
            // Query deleted contacts from service layer
            List<Contact> deletedContacts = contactService.getDeletedContacts();
            
            // Populate table with deleted contacts
            for (Contact contact : deletedContacts) {
                Object[] row = {
                    contact.getId(),
                    contact.getName(),
                    contact.getPhone(),
                    contact.getEmail(),
                    contact.getCategory(),
                    contact.getUpdatedAt() // Last updated = deletion time
                };
                tableModel.addRow(row);
            }
            
            // Update button states based on row count
            boolean hasRows = tableModel.getRowCount() > 0;
            restoreButton.setEnabled(hasRows);
            permanentDeleteButton.setEnabled(hasRows);
            
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                this,
                "Failed to load deleted contacts: " + ex.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }
    
    /**
     * Restores selected contact (sets is_deleted = false)
     */
    private void restoreContact() {
        int selectedRow = deletedContactsTable.getSelectedRow();
        
        // Validate selection
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(
                this,
                "Please select a contact to restore",
                "No Selection",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }
        
        try {
            // Get contact ID from selected row
            int contactId = (int) tableModel.getValueAt(selectedRow, 0);
            
            // Restore via service layer
            contactService.restoreContact(contactId);
            
            // Show success message
            Toast.show(this, "Contact restored successfully!", Toast.SUCCESS);
            
            // Refresh recycle bin table
            loadDeletedContacts();
            
            // Refresh main dashboard table (to show restored contact)
            parentFrame.loadContacts();
            
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                this,
                "Failed to restore contact: " + ex.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }
    
    /**
     * Permanently deletes selected contact from database (hard delete)
     */
    private void permanentlyDeleteContact() {
        int selectedRow = deletedContactsTable.getSelectedRow();
        
        // Validate selection
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(
                this,
                "Please select a contact to delete permanently",
                "No Selection",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }
        
        // Get contact name for confirmation dialog
        String contactName = (String) tableModel.getValueAt(selectedRow, 1);
        
        // Show confirmation dialog (CRITICAL for permanent delete)
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to permanently delete \"" + contactName + "\"?\n\n" +
            "⚠️ This action CANNOT be undone!",
            "Confirm Permanent Deletion",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        
        // User clicked NO or closed dialog
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        
        try {
            // Get contact ID from selected row
            int contactId = (int) tableModel.getValueAt(selectedRow, 0);
            
            // Permanently delete via service layer
            contactService.hardDeleteContact(contactId);
            
            // Show success message
            Toast.show(this, "Contact permanently deleted", Toast.INFO);
            
            // Refresh recycle bin table
            loadDeletedContacts();
            
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                this,
                "Failed to delete contact: " + ex.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }
}
```

### Explanation: Soft Delete + Recycle Bin Architecture

**Purpose of RecycleBinDialog:**

Implements a "Recycle Bin" feature similar to operating system trash folders. Users can:

1. **View deleted contacts** (soft-deleted, still in database)
2. **Restore contacts** (undo deletion)
3. **Permanently delete** (hard delete from database)

This provides a safety net against accidental deletions.

---

**Why Soft Delete Is Safer:**

**Soft Delete (is_deleted = true):**

```sql
UPDATE contacts SET is_deleted = true, updated_at = NOW() WHERE id = ?;
```

**Benefits:**

1. **Recoverability**: Contact data still exists in database
2. **Audit trail**: Can track when contact was deleted
3. **Accidental deletion protection**: User can undo mistake
4. **Regulatory compliance**: Some laws require data retention
5. **Relationships preserved**: If contact has linked data (future: groups, notes), foreign keys remain valid

**Hard Delete (actual deletion):**

```sql
DELETE FROM contacts WHERE id = ?;
```

**Risks:**

1. **Irreversible**: Data gone forever (unless you have database backup)
2. **Foreign key violations**: If other tables reference this contact, delete fails
3. **No audit trail**: Cannot track who deleted or when
4. **User error**: Accidental clicks cause permanent data loss

---

**Why Restore Logic Is Separate:**

**Restore vs. Undelete:**

```java
// Restore (soft delete → active)
contactService.restoreContact(contactId);

// SQL executed:
UPDATE contacts SET is_deleted = false, updated_at = NOW() WHERE id = ?;
```

**Why separate method:**

1. **Clear intent**: Method name shows business purpose
2. **Validation**: Service can check if contact is actually deleted before restoring
3. **Business logic**: Future: send "contact restored" notification
4. **Audit logging**: Track restore events separately from updates

**What breaks if we reuse update method:**

```java
// ❌ Bad: Reuse updateContact() for restore
contact.setDeleted(false);
contactService.updateContact(contact);
```

**Problems:**

1. **Unclear intent**: Does this update name or restore deletion?
2. **No validation**: Can "restore" active contacts (meaningless)
3. **Audit confusion**: Logs show "contact updated" instead of "contact restored"
4. **Future features**: Cannot add restore-specific logic (e.g., send notifications)

---

**Why Permanent Delete Should Be Confirmed:**

```java
int confirm = JOptionPane.showConfirmDialog(
    this,
    "⚠️ This action CANNOT be undone!",
    "Confirm Permanent Deletion",
    JOptionPane.YES_NO_OPTION,
    JOptionPane.WARNING_MESSAGE
);

if (confirm != JOptionPane.YES_OPTION) {
    return; // User clicked NO or closed dialog
}
```

**Why confirmation is critical:**

1. **Accidental clicks**: User might click wrong button
2. **Data loss**: Permanent delete cannot be undone
3. **Legal protection**: Company can prove user was warned
4. **User awareness**: Forces user to read warning message

**What breaks without confirmation:**

```
Scenario:
- User opens recycle bin
- Accidentally double-clicks contact
- Contact is permanently deleted
- User: "Wait, I didn't mean to do that!"
- Support team: "Sorry, data is gone forever"
```

**Confirmation dialog benefits:**

- **Two-step process**: User must click twice (reduces accidents)
- **Warning message**: User sees "CANNOT be undone"
- **Cancel option**: User can back out

---

**How This Implements Full Lifecycle Management:**

**Contact Lifecycle:**

```
1. Create       → addContact()                    (is_deleted = false)
2. Read         → getById() / getAll()            (show only is_deleted = false)
3. Update       → updateContact()                 (modify fields)
4. Soft Delete  → softDeleteContact()             (is_deleted = true)
5. Restore      → restoreContact()                (is_deleted = false)
6. Hard Delete  → hardDeleteContact()             (delete from database)
```

**Why this is "complete lifecycle":**

- **Every state is manageable**: Active, Deleted, Permanently Gone
- **Every transition is controlled**: Create → Update → Soft Delete → Restore → Hard Delete
- **No data loss** until user confirms permanent deletion

**What breaks without lifecycle management:**

```java
// ❌ Bad: Only hard delete
contactService.deleteContact(id);

// SQL:
DELETE FROM contacts WHERE id = ?;
```

**Problems:**

1. **No undo**: Accidental deletion is permanent
2. **No recycle bin**: Cannot view deleted contacts
3. **No audit**: Cannot track deletion history
4. **User fear**: Users afraid to delete (might need later)

---

**How Recycle Bin Table Works:**

**Data flow:**

```
1. User clicks "Recycle Bin" button in dashboard
2. RecycleBinDialog opens
3. loadDeletedContacts() called
4. Service queries: SELECT * FROM contacts WHERE is_deleted = true
5. Loop through deleted contacts
6. Add each contact as row in table
7. JTable displays deleted contacts
```

**Why separate table:**

- **Isolation**: Deleted contacts don't clutter main table
- **Focus**: User can review deletions separately
- **Actions**: Different buttons (Restore vs. Edit)

---

**Why Table Is Read-Only:**

```java
@Override
public boolean isCellEditable(int row, int column) {
    return false; // Cannot edit deleted contacts
}
```

**Why read-only:**

1. **Simplicity**: User focuses on restore/delete actions
2. **Data integrity**: Editing deleted contacts is confusing
3. **Business logic**: Must restore before editing

**Workflow:**

```
To edit deleted contact:
1. Restore it (moves to main table)
2. Edit it in main dashboard
3. If needed, delete again
```

**What breaks if table is editable:**

```
User double-clicks deleted contact name
Changes "John" to "Jonathan"
Which method should fire?
  - updateContact()? (But contact is deleted)
  - restoreContact() + updateContact()? (Two operations)
  - Error? (Confusing)
```

---

**Why Recycle Bin Refreshes Both Tables:**

```java
private void restoreContact() {
    // Restore in database
    contactService.restoreContact(contactId);
    
    // Refresh recycle bin table (remove restored contact)
    loadDeletedContacts();
    
    // Refresh main dashboard (show restored contact)
    parentFrame.loadContacts();
}
```

**Why both refreshes:**

1. **Recycle Bin refresh**: Contact disappears from deleted list
2. **Dashboard refresh**: Contact reappears in main list

**What breaks if we skip dashboard refresh:**

```
User restores "John Doe"
Recycle Bin: "John Doe" disappears ✅
Dashboard: Still doesn't show "John Doe" ❌
User: "Did it restore? I don't see it!"
User closes and reopens app → "John Doe" appears
Confusing!
```

---

**Permanent Delete vs. Soft Delete:**

| Feature | Soft Delete | Permanent Delete |
|---------|------------|------------------|
| SQL | `UPDATE is_deleted = true` | `DELETE FROM contacts` |
| Recoverable | Yes (restore) | No (gone forever) |
| Audit trail | Yes (row still exists) | No (row deleted) |
| Foreign keys | Preserved | Broken (cascade delete) |
| Storage | Uses space | Frees space |
| User safety | High | Low |
| Use case | Normal deletion | Cleanup, privacy compliance |

**When to use each:**

- **Soft delete**: Default for all user-initiated deletions
- **Permanent delete**: User confirms in recycle bin, or automated cleanup (e.g., delete contacts older than 1 year)

---

## PHASE 19 — ADVANCED SEARCH & FILTER SYSTEM

### Implementation: Search & Filter in ContactDashboardUI.java

Add these components to your existing `ContactDashboardUI` class:

```java
package ui;

import model.Contact;
import service.ContactService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;

public class ContactDashboardUI extends JFrame {
    
    private ContactService contactService;
    private JTable contactTable;
    private DefaultTableModel tableModel;
    
    // ===== NEW: Search & Filter Components =====
    private JTextField searchField;
    private JComboBox<String> categoryFilterComboBox;
    private JButton searchButton;
    private JButton clearSearchButton;
    
    public ContactDashboardUI() {
        contactService = new ContactService();
        initializeUI();
        loadContacts();
    }
    
    private void initializeUI() {
        setTitle("Contact Management System");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        setLayout(new BorderLayout(10, 10));
        
        // Top panel with search and filters
        JPanel topPanel = createSearchPanel();
        
        // Center panel with contact table
        JPanel centerPanel = createTablePanel();
        
        // Bottom panel with statistics
        JPanel bottomPanel = createStatsPanel();
        
        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    /**
     * Creates search panel with search field, category filter, and search buttons
     */
    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Search & Filter"));
        
        // Search label
        JLabel searchLabel = new JLabel("Search:");
        
        // Search text field
        searchField = new JTextField(20);
        searchField.setToolTipText("Search by name");
        
        // Real-time search: trigger search on every keystroke
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                performSearch(); // Auto-search as user types
            }
        });
        
        // Category filter label
        JLabel categoryLabel = new JLabel("Category:");
        
        // Category filter dropdown
        categoryFilterComboBox = new JComboBox<>(new String[]{
            "All", "Family", "Friend", "Work", "Other"
        });
        categoryFilterComboBox.addActionListener(e -> performSearch()); // Auto-filter on selection
        
        // Search button (manual trigger)
        searchButton = new JButton("🔍 Search");
        searchButton.addActionListener(e -> performSearch());
        
        // Clear search button
        clearSearchButton = new JButton("Clear");
        clearSearchButton.addActionListener(e -> clearSearch());
        
        // Add components to panel
        panel.add(searchLabel);
        panel.add(searchField);
        panel.add(categoryLabel);
        panel.add(categoryFilterComboBox);
        panel.add(searchButton);
        panel.add(clearSearchButton);
        
        return panel;
    }
    
    /**
     * Performs search based on current search field and category filter
     * Combines name search with category filtering
     */
    private void performSearch() {
        try {
            // Get search criteria
            String searchQuery = searchField.getText().trim();
            String selectedCategory = (String) categoryFilterComboBox.getSelectedItem();
            
            List<Contact> results;
            
            // Case 1: Search by name + filter by category
            if (!searchQuery.isEmpty() && !"All".equals(selectedCategory)) {
                results = contactService.searchByNameAndCategory(searchQuery, selectedCategory);
            }
            // Case 2: Search by name only
            else if (!searchQuery.isEmpty()) {
                results = contactService.searchByName(searchQuery);
            }
            // Case 3: Filter by category only
            else if (!"All".equals(selectedCategory)) {
                results = contactService.searchByCategory(selectedCategory);
            }
            // Case 4: No search criteria (show all)
            else {
                results = contactService.getAllContacts();
            }
            
            // Update table with search results
            populateTable(results);
            
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                this,
                "Search failed: " + ex.getMessage(),
                "Search Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }
    
    /**
     * Clears search field and category filter, then reloads all contacts
     */
    private void clearSearch() {
        searchField.setText("");
        categoryFilterComboBox.setSelectedIndex(0); // Select "All"
        loadContacts(); // Reload all contacts
    }
    
    /**
     * Loads all active contacts from database and populates table
     */
    public void loadContacts() {
        try {
            List<Contact> contacts = contactService.getAllContacts();
            populateTable(contacts);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                this,
                "Failed to load contacts: " + ex.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }
    
    /**
     * Populates table with given list of contacts
     * 
     * @param contacts - list of contacts to display
     */
    private void populateTable(List<Contact> contacts) {
        // Clear existing rows
        tableModel.setRowCount(0);
        
        // Add each contact as a row
        for (Contact contact : contacts) {
            Object[] row = {
                contact.getId(),
                contact.getName(),
                contact.getPhone(),
                contact.getEmail(),
                contact.getCategory()
            };
            tableModel.addRow(row);
        }
    }
    
    /**
     * Creates table panel with contact table
     */
    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Define table columns
        String[] columnNames = {"ID", "Name", "Phone", "Email", "Category"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        // Create JTable
        contactTable = new JTable(tableModel);
        contactTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        contactTable.setRowHeight(25);
        
        // Add scroll pane
        JScrollPane scrollPane = new JScrollPane(contactTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Creates statistics panel (placeholder)
     */
    private JPanel createStatsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.add(new JLabel("Total Contacts: 0"));
        return panel;
    }
}
```

### Service Layer Updates: Add Search Methods to ContactService.java

```java
package service;

import dao.ContactDAO;
import dao.ContactDAOImpl;
import model.Contact;
import util.ContactValidator;

import java.util.List;
import java.util.stream.Collectors;

public class ContactService {
    
    private ContactDAO contactDAO;
    
    public ContactService() {
        this.contactDAO = new ContactDAOImpl();
    }
    
    // ... existing methods ...
    
    /**
     * Searches contacts by name (case-insensitive partial match)
     * Delegates to DAO layer for database query
     * 
     * @param name - search query (partial name)
     * @return list of matching contacts
     */
    public List<Contact> searchByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return getAllContacts();
        }
        return contactDAO.searchByName(name);
    }
    
    /**
     * Filters contacts by category
     * Delegates to DAO layer for database query
     * 
     * @param category - category to filter by
     * @return list of contacts in specified category
     */
    public List<Contact> searchByCategory(String category) {
        if (category == null || category.equals("All")) {
            return getAllContacts();
        }
        return contactDAO.searchByCategory(category);
    }
    
    /**
     * Searches contacts by name AND filters by category
     * Combines two search criteria
     * 
     * @param name - search query (partial name)
     * @param category - category to filter by
     * @return list of contacts matching both criteria
     */
    public List<Contact> searchByNameAndCategory(String name, String category) {
        // Get contacts matching name
        List<Contact> nameMatches = searchByName(name);
        
        // Filter by category
        if (category != null && !"All".equals(category)) {
            return nameMatches.stream()
                .filter(c -> category.equals(c.getCategory()))
                .collect(Collectors.toList());
        }
        
        return nameMatches;
    }
}
```

### DAO Layer Updates: Add Search Methods to ContactDAOImpl.java

```java
package dao;

import model.Contact;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ContactDAOImpl implements ContactDAO {
    
    // ... existing methods ...
    
    /**
     * Searches contacts by name using SQL LIKE
     * Uses case-insensitive partial matching
     * 
     * @param name - search query
     * @return list of matching contacts
     */
    @Override
    public List<Contact> searchByName(String name) {
        List<Contact> results = new ArrayList<>();
        
        // SQL: LIKE '%query%' matches any substring
        String sql = "SELECT * FROM contacts WHERE is_deleted = false AND name LIKE ? ORDER BY name ASC";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            // Add wildcards for partial matching
            stmt.setString(1, "%" + name + "%");
            
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                results.add(mapResultSetToContact(rs));
            }
            
        } catch (SQLException e) {
            throw new RuntimeException("Search by name failed: " + e.getMessage(), e);
        }
        
        return results;
    }
    
    /**
     * Filters contacts by exact category match
     * 
     * @param category - category to filter by
     * @return list of contacts in specified category
     */
    @Override
    public List<Contact> searchByCategory(String category) {
        List<Contact> results = new ArrayList<>();
        
        // SQL: Exact category match
        String sql = "SELECT * FROM contacts WHERE is_deleted = false AND category = ? ORDER BY name ASC";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, category);
            
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                results.add(mapResultSetToContact(rs));
            }
            
        } catch (SQLException e) {
            throw new RuntimeException("Search by category failed: " + e.getMessage(), e);
        }
        
        return results;
    }
    
    /**
     * Helper method: maps ResultSet row to Contact object
     */
    private Contact mapResultSetToContact(ResultSet rs) throws SQLException {
        Contact contact = new Contact();
        contact.setId(rs.getInt("id"));
        contact.setName(rs.getString("name"));
        contact.setPhone(rs.getString("phone"));
        contact.setEmail(rs.getString("email"));
        contact.setAddress(rs.getString("address"));
        contact.setCategory(rs.getString("category"));
        contact.setDeleted(rs.getBoolean("is_deleted"));
        contact.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        contact.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return contact;
    }
}
```

### Explanation: Advanced Search & Filter System

**Purpose of Search & Filter:**

Allows users to quickly find specific contacts without scrolling through entire list. Essential for scalability as contact list grows.

---

**Why Filtering Must Be in Service Layer:**

**✅ Correct (Service layer filtering):**

```java
// UI calls service
List<Contact> results = contactService.searchByName("John");

// Service calls DAO
public List<Contact> searchByName(String name) {
    return contactDAO.searchByName(name);
}

// DAO executes SQL
SELECT * FROM contacts WHERE name LIKE ? AND is_deleted = false;
```

**Benefits:**

1. **Database efficiency**: Only matching contacts are fetched (not all contacts)
2. **Memory efficiency**: Doesn't load unnecessary data
3. **Reusable**: CSV export, statistics, etc. can also use search
4. **Testable**: Can test search without UI

---

**❌ Wrong (UI filtering):**

```java
// Bad: Load all contacts, filter in UI
List<Contact> allContacts = contactService.getAllContacts(); // 10,000 contacts

// Filter in memory
List<Contact> results = new ArrayList<>();
for (Contact c : allContacts) {
    if (c.getName().contains("John")) {
        results.add(c);
    }
}
```

**Problems:**

1. **Slow**: Loads ALL contacts from database every time
2. **Memory usage**: 10,000 contacts in RAM (only need 5 matches)
3. **Not scalable**: Performance degrades as database grows
4. **Index wasted**: Database has `idx_name` index, but we don't use it

---

**Why UI Should Not Filter Database Directly:**

**❌ Bad (SQL in UI):**

```java
// Bad: UI executes SQL
String sql = "SELECT * FROM contacts WHERE name LIKE ?";
Connection conn = DriverManager.getConnection(...);
PreparedStatement stmt = conn.prepareStatement(sql);
stmt.setString(1, "%" + searchField.getText() + "%");
ResultSet rs = stmt.executeQuery();
```

**Problems:**

1. **Architecture violation**: UI knows about database schema
2. **Cannot reuse**: CSV export must duplicate SQL
3. **Hard to test**: Must mock database in UI tests
4. **Security risk**: UI code has database credentials

---

**How Search Improves Scalability:**

**Indexed search (fast):**

```sql
SELECT * FROM contacts WHERE name LIKE '%John%' AND is_deleted = false;

-- MySQL uses idx_name index
-- Time: 5ms (even with 100,000 contacts)
```

**Without index (slow):**

```sql
SELECT * FROM contacts WHERE name LIKE '%John%';

-- MySQL scans all rows (full table scan)
-- Time: 500ms (with 100,000 contacts)
```

**Why our index helps:**

```sql
INDEX idx_name (name);
INDEX idx_active_contacts (is_deleted, name);
```

**Compound index optimization:**

```sql
WHERE is_deleted = false AND name LIKE '%John%'

-- Uses idx_active_contacts
-- Filters is_deleted first (reduces rows)
-- Then searches name within remaining rows
```

---

**How Indexing Supports Performance:**

**Index types used:**

1. **idx_name (name)**: Single-column index for name searches
2. **idx_active_contacts (is_deleted, name)**: Composite index for filtered searches

**Query optimization:**

```sql
-- Query 1: Search all contacts
SELECT * FROM contacts WHERE name LIKE '%John%';
-- Uses: idx_name

-- Query 2: Search active contacts
SELECT * FROM contacts WHERE is_deleted = false AND name LIKE '%John%';
-- Uses: idx_active_contacts (better)
```

**Performance comparison (100,000 contacts):**

| Scenario | Without Index | With idx_name | With idx_active_contacts |
|----------|---------------|---------------|--------------------------|
| Search "John" | 800ms | 50ms | 50ms |
| Search active "John" | 800ms | 80ms (scan + filter) | 15ms |
| Filter by category | 800ms | 800ms | 800ms (needs idx_category) |

**Suggested index improvements:**

```sql
-- Add category index for category filtering
CREATE INDEX idx_category ON contacts(is_deleted, category);

-- Optimizes:
SELECT * FROM contacts WHERE is_deleted = false AND category = 'Family';
```

---

**Real-Time Search Explanation:**

```java
searchField.addKeyListener(new KeyAdapter() {
    @Override
    public void keyReleased(KeyEvent e) {
        performSearch(); // Auto-search as user types
    }
});
```

**How it works:**

1. User types "J" → `keyReleased()` fires → `performSearch()` called → Query: `LIKE '%J%'` → Table updates
2. User types "o" (now "Jo") → `keyReleased()` fires → Query: `LIKE '%Jo%'` → Table updates
3. User types "h" (now "Joh") → Query: `LIKE '%Joh%'` → Table updates
4. User types "n" (now "John") → Query: `LIKE '%John%'` → Table updates

**Benefits:**

- **Instant feedback**: User sees results immediately
- **No button click**: More intuitive than clicking "Search"
- **Progressive refinement**: User can see how filtering narrows results

**Performance consideration:**

- **Risk**: Executes database query on every keystroke
- **Mitigation**: Index makes queries fast (5-15ms)
- **Alternative**: Debouncing (wait 300ms after last keystroke before searching)

**Debouncing implementation (optional):**

```java
private Timer searchTimer;

searchField.addKeyListener(new KeyAdapter() {
    @Override
    public void keyReleased(KeyEvent e) {
        if (searchTimer != null) {
            searchTimer.stop(); // Cancel previous timer
        }
        
        // Wait 300ms before searching
        searchTimer = new Timer(300, evt -> performSearch());
        searchTimer.setRepeats(false);
        searchTimer.start();
    }
});
```

**Benefits of debouncing:**

- Reduces database queries (only search after user stops typing)
- Better performance on slow databases
- Still feels instant (300ms is imperceptible)

---

**Combined Search Logic:**

```java
// Case 1: Name + Category
if (!searchQuery.isEmpty() && !"All".equals(selectedCategory)) {
    results = contactService.searchByNameAndCategory(searchQuery, selectedCategory);
}
// Case 2: Name only
else if (!searchQuery.isEmpty()) {
    results = contactService.searchByName(searchQuery);
}
// Case 3: Category only
else if (!"All".equals(selectedCategory)) {
    results = contactService.searchByCategory(selectedCategory);
}
// Case 4: No criteria (show all)
else {
    results = contactService.getAllContacts();
}
```

**Why 4 cases:**

1. **Name + Category**: "John" in "Family" → Most specific
2. **Name only**: "John" in any category → Broad search
3. **Category only**: All contacts in "Work" → Filter
4. **No criteria**: Show all active contacts → Default view

**Alternative approach (single query):**

```java
// Build dynamic SQL
StringBuilder sql = new StringBuilder("SELECT * FROM contacts WHERE is_deleted = false");

if !searchQuery.isEmpty()) {
    sql.append(" AND name LIKE ?");
}

if (!"All".equals(category)) {
    sql.append(" AND category = ?");
}

sql.append(" ORDER BY name ASC");
```

**Why we don't use dynamic SQL:**

- **Prone to errors**: Easy to miss spaces in string concatenation
- **SQL injection risk**: If not careful with parameter binding
- **Less readable**: Hard to see what query does
- **Harder to test**: More code paths to test

**Why separate methods are better:**

- **Clear intent**: Each method has specific purpose
- **Type safety**: Compiler checks parameters
- **Reusable**: Can call `searchByName()` independently
- **Testable**: Each method is a unit test

---

## PHASE 20 — CSV EXPORT & CSV IMPORT

### Complete Implementation: CSVExporter.java

```java
package util;

import model.Contact;

import javax.swing.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Utility class for exporting contacts to CSV format.
 * Provides file chooser dialog and CSV writing functionality.
 */
public class CSVExporter {
    
    /**
     * Exports list of contacts to CSV file chosen by user
     * 
     * @param contacts - list of contacts to export
     * @param parentFrame - parent frame for file chooser dialog
     * @return true if export succeeded, false if canceled or failed
     */
    public static boolean exportToCSV(List<Contact> contacts, JFrame parentFrame) {
        // Step 1: Show file chooser dialog
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Contacts to CSV");
        fileChooser.setSelectedFile(new File("contacts_export.csv")); // Default filename
        
        // Filter: Only show .csv files
        fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().toLowerCase().endsWith(".csv");
            }
            
            @Override
            public String getDescription() {
                return "CSV Files (*.csv)";
            }
        });
        
        // Show save dialog
        int result = fileChooser.showSaveDialog(parentFrame);
        
        // User canceled
        if (result != JFileChooser.APPROVE_OPTION) {
            return false;
        }
        
        // Get selected file
        File file = fileChooser.getSelectedFile();
        
        // Ensure .csv extension
        if (!file.getName().toLowerCase().endsWith(".csv")) {
            file = new File(file.getAbsolutePath() + ".csv");
        }
        
        // Step 2: Write contacts to CSV file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            
            // Write CSV header
            writer.write("ID,Name,Phone,Email,Address,Category,Created At,Updated At");
            writer.newLine();
            
            // Write each contact as CSV row
            for (Contact contact : contacts) {
                String line = buildCSVLine(contact);
                writer.write(line);
                writer.newLine();
            }
            
            // Show success message
            JOptionPane.showMessageDialog(
                parentFrame,
                "Successfully exported " + contacts.size() + " contacts to:\n" + file.getAbsolutePath(),
                "Export Successful",
                JOptionPane.INFORMATION_MESSAGE
            );
            
            return true;
            
        } catch (IOException e) {
            // Show error message
            JOptionPane.showMessageDialog(
                parentFrame,
                "Failed to export contacts:\n" + e.getMessage(),
                "Export Error",
                JOptionPane.ERROR_MESSAGE
            );
            return false;
        }
    }
    
    /**
     * Builds a CSV line from contact object
     * Handles special characters (commas, quotes, newlines)
     * 
     * @param contact - contact to convert to CSV line
     * @return CSV-formatted string
     */
    private static String buildCSVLine(Contact contact) {
        return String.format("%d,%s,%s,%s,%s,%s,%s,%s",
            contact.getId(),
            escapeCSV(contact.getName()),
            escapeCSV(contact.getPhone()),
            escapeCSV(contact.getEmail()),
            escapeCSV(contact.getAddress()),
            escapeCSV(contact.getCategory()),
            contact.getCreatedAt().toString(),
            contact.getUpdatedAt().toString()
        );
    }
    
    /**
     * Escapes special characters for CSV format
     * Handles commas, quotes, and newlines
     * 
     * CSV rules:
     * - If field contains comma, quote, or newline → wrap in quotes
     * - If field contains quote → escape as double quote ("")
     * 
     * @param value - string to escape
     * @return CSV-escaped string
     */
    private static String escapeCSV(String value) {
        if (value == null) {
            return "";
        }
        
        // Check if escaping is needed
        boolean needsQuotes = value.contains(",") || value.contains("\"") || value.contains("\n");
        
        if (needsQuotes) {
            // Escape quotes: " → ""
            String escaped = value.replace("\"", "\"\"");
            
            // Wrap in quotes
            return "\"" + escaped + "\"";
        }
        
        return value;
    }
}
```

### Complete Implementation: CSVImporter.java

```java
package util;

import model.Contact;
import service.ContactService;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for importing contacts from CSV format.
 * Provides file chooser dialog, CSV parsing, and duplicate handling.
 */
public class CSVImporter {
    
    private ContactService contactService;
    
    public CSVImporter(ContactService contactService) {
        this.contactService = contactService;
    }
    
    /**
     * Imports contacts from CSV file chosen by user
     * Shows progress dialog and handles duplicates
     * 
     * @param parentFrame - parent frame for dialog
     * @return number of contacts successfully imported
     */
    public int importFromCSV(JFrame parentFrame) {
        // Step 1: Show file chooser dialog
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Import Contacts from CSV");
        
        // Filter: Only show .csv files
        fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().toLowerCase().endsWith(".csv");
            }
            
            @Override
            public String getDescription() {
                return "CSV Files (*.csv)";
            }
        });
        
        // Show open dialog
        int result = fileChooser.showOpenDialog(parentFrame);
        
        // User canceled
        if (result != JFileChooser.APPROVE_OPTION) {
            return 0;
        }
        
        // Get selected file
        File file = fileChooser.getSelectedFile();
        
        // Step 2: Read and import contacts
        try {
            List<Contact> importedContacts = parseCSV(file);
            
            if (importedContacts.isEmpty()) {
                JOptionPane.showMessageDialog(
                    parentFrame,
                    "No valid contacts found in CSV file",
                    "Import Warning",
                    JOptionPane.WARNING_MESSAGE
                );
                return 0;
            }
            
            // Step 3: Import contacts with duplicate handling
            int successCount = 0;
            int duplicateCount = 0;
            
            for (Contact contact : importedContacts) {
                try {
                    // Check for duplicate (by name + phone)
                    if (isDuplicate(contact)) {
                        duplicateCount++;
                        continue; // Skip duplicate
                    }
                    
                    // Validate and add contact
                    ContactValidator.validateName(contact.getName());
                    contactService.addContact(contact);
                    successCount++;
                    
                } catch (Exception e) {
                    // Skip invalid contact
                    System.err.println("Skipped invalid contact: " + contact.getName() + " - " + e.getMessage());
                }
            }
            
            // Step 4: Show import summary
            String message = String.format(
                "Import Complete!\n\n" +
                "✅ Successfully imported: %d contacts\n" +
                "⚠️ Skipped duplicates: %d contacts",
                successCount, duplicateCount
            );
            
            JOptionPane.showMessageDialog(
                parentFrame,
                message,
                "Import Summary",
                JOptionPane.INFORMATION_MESSAGE
            );
            
            return successCount;
            
        } catch (IOException e) {
            JOptionPane.showMessageDialog(
                parentFrame,
                "Failed to read CSV file:\n" + e.getMessage(),
                "Import Error",
                JOptionPane.ERROR_MESSAGE
            );
            return 0;
        }
    }
    
    /**
     * Parses CSV file and converts to list of Contact objects
     * 
     * @param file - CSV file to parse
     * @return list of contacts parsed from CSV
     */
    private List<Contact> parseCSV(File file) throws IOException {
        List<Contact> contacts = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            
            // Read and skip header line
            String headerLine = reader.readLine();
            if (headerLine == null) {
                return contacts; // Empty file
            }
            
            // Read data lines
            String line;
            int lineNumber = 1;
            
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                
                try {
                    Contact contact = parseCSVLine(line);
                    if (contact != null) {
                        contacts.add(contact);
                    }
                } catch (Exception e) {
                    System.err.println("Skipped line " + lineNumber + ": " + e.getMessage());
                }
            }
        }
        
        return contacts;
    }
    
    /**
     * Parses a single CSV line into Contact object
     * Handles quoted fields and escaped characters
     * 
     * @param line - CSV line to parse
     * @return Contact object, or null if parsing fails
     */
    private Contact parseCSVLine(String line) {
        // Simple CSV parsing (handles quoted fields)
        List<String> fields = splitCSVLine(line);
        
        if (fields.size() < 5) {
            return null; // Invalid line
        }
        
        Contact contact = new Contact();
        
        try {
            // Skip ID (field 0) - database will auto-generate
            contact.setName(fields.get(1));
            contact.setPhone(fields.get(2));
            contact.setEmail(fields.get(3));
            contact.setAddress(fields.get(4));
            contact.setCategory(fields.get(5));
            contact.setDeleted(false); // Imported contacts are active
            contact.setCreatedAt(LocalDateTime.now());
            contact.setUpdatedAt(LocalDateTime.now());
            
            return contact;
            
        } catch (Exception e) {
            return null; // Invalid data
        }
    }
    
    /**
     * Splits CSV line into fields, handling quoted values
     * 
     * Example:
     *   John,Doe,"123 Main St, Apt 4","(555) 123-4567"
     *   → ["John", "Doe", "123 Main St, Apt 4", "(555) 123-4567"]
     * 
     * @param line - CSV line to split
     * @return list of field values
     */
    private List<String> splitCSVLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder currentField = new StringBuilder();
        boolean insideQuotes = false;
        
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            
            if (c == '\"') {
                // Handle escaped quotes ("")
                if (insideQuotes && i + 1 < line.length() && line.charAt(i + 1) == '\"') {
                    currentField.append('\"');
                    i++; // Skip next quote
                } else {
                    insideQuotes = !insideQuotes; // Toggle quote mode
                }
            } else if (c == ',' && !insideQuotes) {
                // End of field
                fields.add(currentField.toString());
                currentField.setLength(0); // Clear for next field
            } else {
                currentField.append(c);
            }
        }
        
        // Add last field
        fields.add(currentField.toString());
        
        return fields;
    }
    
    /**
     * Checks if contact already exists in database
     * Considers duplicate if same name AND same phone
     * 
     * @param contact - contact to check
     * @return true if duplicate exists
     */
    private boolean isDuplicate(Contact contact) {
        try {
            // Search by name
            List<Contact> existing = contactService.searchByName(contact.getName());
            
            // Check if any have matching phone
            for (Contact existingContact : existing) {
                if (contact.getPhone().equals(existingContact.getPhone())) {
                    return true; // Duplicate found
                }
            }
            
            return false; // Not a duplicate
            
        } catch (Exception e) {
            return false; // If search fails, assume not duplicate
        }
    }
}
```

### Integration: Add Export/Import Buttons to ContactDashboardUI.java

```java
// Add to toolbar in ContactDashboardUI
private JToolBar createToolbar() {
    JToolBar toolbar = new JToolBar();
    toolbar.setFloatable(false);
    
    // ... existing buttons ...
    
    // Export button
    JButton exportButton = new JButton("📤 Export CSV");
    exportButton.addActionListener(e -> exportContacts());
    toolbar.add(exportButton);
    
    // Import button
    JButton importButton = new JButton("📥 Import CSV");
    importButton.addActionListener(e -> importContacts());
    toolbar.add(importButton);
    
    return toolbar;
}

/**
 * Exports current contacts to CSV file
 */
private void exportContacts() {
    try {
        // Get current contacts from table
        List<Contact> contacts = contactService.getAllContacts();
        
        if (contacts.isEmpty()) {
            JOptionPane.showMessageDialog(
                this,
                "No contacts to export",
                "Export Warning",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }
        
        // Export to CSV
        CSVExporter.exportToCSV(contacts, this);
        
    } catch (Exception ex) {
        JOptionPane.showMessageDialog(
            this,
            "Export failed: " + ex.getMessage(),
            "Export Error",
            JOptionPane.ERROR_MESSAGE
        );
    }
}

/**
 * Imports contacts from CSV file
 */
private void importContacts() {
    CSVImporter importer = new CSVImporter(contactService);
    int importedCount = importer.importFromCSV(this);
    
    if (importedCount > 0) {
        // Refresh table to show imported contacts
        loadContacts();
    }
}
```

### Explanation: CSV Export & Import System

**Purpose of CSV Import/Export:**

Enables data portability, backup, and integration with external tools (Excel, Google Sheets, CRM systems).

---

**Why File I/O Must Handle Exceptions:**

**File operations that can fail:**

1. **File not found**: User selects non-existent file
2. **Permission denied**: User doesn't have write access to folder
3. **Disk full**: Not enough space to write file
4. **File locked**: Another program has file open
5. **Invalid format**: CSV file has corrupted data

**Try-catch pattern:**

```java
try {
    // Attempt file operation
    writer.write(data);
} catch (IOException e) {
    // Handle error gracefully
    JOptionPane.showMessageDialog(parent, "Failed: " + e.getMessage());
}
```

**What breaks without exception handling:**

```java
// ❌ Bad: No exception handling
writer.write(data); // Compiler error: "Unhandled IOException"
```

**Why Java forces exception handling:**

- **Checked exceptions**: `IOException` must be caught or declared
- **Safety**: Prevents crashes from file errors
- **User-friendly**: Can show error dialog instead of stack trace

---

**Why Background Thread Prevents UI Freeze:**

**Problem with synchronous I/O:**

```java
// On EDT (Event Dispatch Thread)
exportButton.addActionListener(e -> {
    // This blocks the EDT for 5 seconds
    CSVExporter.exportToCSV(contacts, this); // 5 seconds
    
    // UI is frozen during export:
    // - Cannot move window
    // - Cannot click buttons
    // - Appears crashed
});
```

**Solution: Background thread with SwingWorker:**

```java
exportButton.addActionListener(e -> {
    new SwingWorker<Void, Void>() {
        @Override
        protected Void doInBackground() throws Exception {
            // Runs on background thread
            CSVExporter.exportToCSV(contacts, parentFrame);
            return null;
        }
        
        @Override
        protected void done() {
            // Runs on EDT after export completes
            JOptionPane.showMessageDialog(parentFrame, "Export complete!");
        }
    }.execute();
});
```

**Benefits:**

1. **UI responsive**: User can interact with app during export
2. **Progress feedback**: Can show progress bar
3. **Cancelable**: User can cancel long-running export

**When to use SwingWorker:**

- **Large files**: Exporting 10,000 contacts takes 10+ seconds
- **Network operations**: Uploading to cloud storage
- **Heavy computation**: Generating reports

**When NOT to use SwingWorker:**

- **Small files**: Exporting 100 contacts takes <1 second (imperceptible)
- **Adds complexity**: More code to maintain
- **Our implementation**: Use synchronous for simplicity (can upgrade later)

---

**How CSV Improves Portability:**

**CSV benefits:**

1. **Universal format**: Readable by Excel, Google Sheets, Numbers, LibreOffice
2. **Human-readable**: Can open in text editor
3. **Simple parsing**: Easy to implement import/export
4. **Database migration**: Move contacts between systems
5. **Backup**: Users can save contacts locally

**Alternative formats:**

| Format | Pros | Cons |
|--------|------|------|
| CSV | Simple, universal | No type safety |
| JSON | Type-safe, structured | Harder to edit manually |
| XML | Standardized, validated | Verbose, complex |
| SQLite | Full database | Not human-readable |

**Why CSV is best for contacts:**

- **User familiarity**: Everyone knows Excel/Sheets
- **Easy editing**: User can bulk-edit in spreadsheet
- **Portability**: Can import into Gmail, Outlook, phone

---

**How CSV Export Works:**

**Step-by-step flow:**

```
1. User clicks "Export CSV" button
2. JFileChooser dialog opens
3. User selects save location and filename
4. Loop through contacts
5. Convert each contact to CSV line
6. Handle special characters (commas, quotes)
7. Write to file using BufferedWriter
8. Show success message
```

**CSV format example:**

```csv
ID,Name,Phone,Email,Address,Category,Created At,Updated At
1,John Doe,555-1234,john@email.com,"123 Main St, Apt 4",Family,2026-01-15T10:30:00,2026-01-15T10:30:00
2,Jane Smith,555-5678,jane@email.com,456 Oak Ave,Friend,2026-01-16T14:20:00,2026-01-16T14:20:00
```

**Special character handling:**

```java
// Field contains comma → wrap in quotes
"123 Main St, Apt 4" → "\"123 Main St, Apt 4\""

// Field contains quote → escape as double quote
He said "Hi" → "He said ""Hi"""

// Field contains newline → wrap in quotes
"Line 1\nLine 2" → "\"Line 1\nLine 2\""
```

---

**How CSV Import Works:**

**Step-by-step flow:**

```
1. User clicks "Import CSV" button
2. JFileChooser dialog opens
3. User selects CSV file
4. Read file line by line using BufferedReader
5. Skip header line
6. Parse each line into Contact object
7. Validate contact data
8. Check for duplicates
9. Add non-duplicate contacts to database
10. Show import summary (success count, duplicate count)
```

**Duplicate detection logic:**

```java
private boolean isDuplicate(Contact contact) {
    // Search by name
    List<Contact> existing = contactService.searchByName(contact.getName());
    
    // Check if any have matching phone
    for (Contact existingContact : existing) {
        if (contact.getPhone().equals(existingContact.getPhone())) {
            return true; // Duplicate: same name AND phone
        }
    }
    
    return false;
}
```

**Why check name AND phone:**

- **Name alone**: "John Smith" is common (false positives)
- **Phone alone**: User might change phone number
- **Name + Phone**: Very unlikely to match unless duplicate

**Alternative: Use email as duplicate key**

```java
if (contact.getEmail().equals(existingContact.getEmail())) {
    return true; // Duplicate by email
}
```

---

**Error Handling in Import:**

**Types of errors handled:**

1. **Invalid CSV format**:
   ```
   Skipped line 5: Expected 8 fields, found 3
   ```

2. **Validation errors**:
   ```
   Skipped invalid contact: "" (Name cannot be empty)
   ```

3. **Duplicate contacts**:
   ```
   Skipped duplicate: John Doe (555-1234)
   ```

4. **File I/O errors**:
   ```
   Failed to read CSV file: Permission denied
   ```

**Graceful degradation:**

- **Skip invalid lines**: Don't abort entire import
- **Show summary**: User knows what succeeded/failed
- **Log errors**: Print to console for debugging

---

**How This Prepares for Backup System:**

**Current CSV export enables:**

1. **Manual backup**: User exports contacts weekly
2. **Data migration**: Move contacts to new app
3. **Sharing**: Send CSV via email

**Future backup system can add:**

1. **Automatic export**: Scheduled daily backup
2. **Cloud sync**: Upload to Google Drive/Dropbox
3. **Version history**: Keep multiple backup snapshots
4. **Differential backup**: Only save changed contacts

**Architecture foundation:**

```java
// Current: Manual export
CSVExporter.exportToCSV(contacts, file);

// Future: Automatic backup
class BackupScheduler {
    void scheduleDailyBackup() {
        Timer timer = new Timer(24 * 60 * 60 * 1000, e -> {
            File backupFile = new File("backup_" + LocalDate.now() + ".csv");
            CSVExporter.exportToCSV(contactService.getAllContacts(), backupFile);
        });
        timer.start();
    }
}
```

---

## Summary: Part 4 Complete

**What Part 4 Accomplished:**

1. ✅ **Add Contact Dialog**: Modal form for creating new contacts with validation
2. ✅ **Edit Contact Dialog**: Pre-filled form for updating existing contacts
3. ✅ **Soft Delete + Recycle Bin**: Safe deletion with restore capability
4. ✅ **Advanced Search & Filter**: Real-time search by name and category filtering
5. ✅ **CSV Export**: Data portability and manual backup
6. ✅ **CSV Import**: Batch contact addition with duplicate detection

**Complete CRUD Lifecycle:**

```
Create  → AddContactDialog
Read    → JTable + PreviewPanel + Search
Update  → EditContactDialog
Delete  → Soft Delete → RecycleBinDialog → Restore or Hard Delete
```

**Data Flow Summary:**

```
User Input (Dialogs)
    ↓
Validation (ContactValidator)
    ↓
Business Logic (ContactService)
    ↓
Database Operations (ContactDAOImpl)
    ↓
MySQL Database (contacts table)
    ↓
UI Updates (JTable refresh)
```

**Architecture Benefits:**

- **Layered separation**: UI, Service, DAO, Database
- **Reusable components**: Dialogs, validators, utilities
- **Safe operations**: Soft delete, confirmation dialogs, validation
- **Scalable design**: Indexed search, background threads (future)
- **Professional features**: CSV import/export, recycle bin, real-time search

---

## README_PART_4 Complete. Ready for PART 5.
