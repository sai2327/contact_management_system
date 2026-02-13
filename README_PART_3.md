# Contact Management System - PART 3: User Interface Components

## 📋 Introduction to PART 3

Welcome to **PART 3** of the Contact Management System reconstruction guide. This part implements the **complete user interface layer** that brings the backend to life with a professional, interactive desktop application.

### What PART 3 Builds

In PART 3, we implement:
- **JTable Integration** - Display contacts in tabular format with sorting
- **Toolbar with Icons** - Action buttons with safe icon loading for JAR compatibility
- **Theme System** - Dark/Light mode with accent colors for modern UX
- **Statistics Dashboard** - Visual overview of contact counts and categories
- **Contact Preview Panel** - Side panel showing selected contact details

### Why UI Components Come After Backend

**The Correct Order:**
```
PART 1: Database + Models + Interfaces ✓
PART 2: DAO + Service + Validation ✓
PART 3: UI Components ← YOU ARE HERE
PART 4: Advanced Features (Search, Import, etc.)
```

**Why this sequence matters:**

**Backend-first approach benefits:**
- ✅ **Testable**: Backend can be tested independently without UI
- ✅ **Reusable**: Same backend works with Swing, Web UI, Mobile, CLI
- ✅ **Parallel development**: Backend team and UI team can work simultaneously
- ✅ **Clear contracts**: Service layer defines what UI can do
- ✅ **No rework**: UI doesn't change when database changes

**If UI-first (wrong approach):**
```java
// UI directly queries database - BAD
public class ContactTable extends JPanel {
    private void loadData() {
        Connection conn = DriverManager.getConnection(...); // SQL in UI
        ResultSet rs = stmt.executeQuery("SELECT * FROM contacts");
        while (rs.next()) {
            // Mixed layers
        }
    }
}
```

**Problems:**
- Cannot test UI without database running
- Cannot switch from MySQL to PostgreSQL without rewriting UI
- Business logic scattered throughout UI components
- Impossible to add web interface later

**Our approach (correct):**
```java
// UI calls Service layer - GOOD
public class ContactTable extends JPanel {
    private ContactService service = new ContactService();
    
    private void loadData() {
        List<Contact> contacts = service.getAllContacts(); // Clean separation
        updateTable(contacts);
    }
}
```

**Benefits:**
- ✅ UI knows nothing about database
- ✅ UI gets fully validated, business-rule-compliant data
- ✅ Change database without touching UI
- ✅ Easy to test: mock service, test UI independently

### How JTable Connects to Service Layer

**Data flow architecture:**
```
User clicks "Refresh"
    ↓
JTable calls loadContacts()
    ↓
loadContacts() calls service.getAllContacts()
    ↓
Service calls dao.getAll()
    ↓
DAO executes SQL query
    ↓
Database returns rows
    ↓
DAO converts ResultSet → List<Contact>
    ↓
Service returns List<Contact>
    ↓
loadContacts() converts List<Contact> → TableModel rows
    ↓
JTable displays data
```

**Key principle: No SQL in UI**
- UI sees only Contact objects
- UI doesn't know table names, column names, SQL syntax
- Service layer is the gatekeeper

### Why Visual Structure Matters in Desktop Apps

**Poor UI structure:**
```
Everything in one giant JFrame
- 2000+ lines of code
- All buttons, tables, panels mixed together
- Cannot reuse components
- Maintenance nightmare
```

**Good UI structure (our approach):**
```
ContactDashboardUI (main window)
  ├── Toolbar (separate component)
  ├── JTable (with model)
  ├── StatisticsPanel (separate component)
  ├── PreviewPanel (separate component)
  └── ThemeManager (separate utility)
```

**Benefits:**
- ✅ Each component is self-contained
- ✅ Components are reusable
- ✅ Easy to test components individually
- ✅ Easy to add/remove features
- ✅ Team can work on different components simultaneously

---

## 📊 PHASE 11 — JTable Integration

### Updated File: `ui/ContactDashboardUI.java`

```java
package ui;

import model.Contact;
import service.ContactService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * ContactDashboardUI - Main Application Window (Enhanced)
 * 
 * PART 3 ENHANCEMENTS:
 * - JTable integration for displaying contacts
 * - Table model for dynamic data management
 * - Load contacts from Service layer
 * - Auto-refresh on data changes
 */
public class ContactDashboardUI extends JFrame {
    
    // Window dimensions
    private static final int WINDOW_WIDTH = 1200;
    private static final int WINDOW_HEIGHT = 700;
    
    // Service layer instance
    // This is our connection to the backend
    private ContactService contactService;
    
    // UI Components
    private JTable contactTable;
    private DefaultTableModel tableModel;
    
    /**
     * Constructor - Build and initialize the UI
     */
    public ContactDashboardUI() {
        super("Contact Management System");
        
        // Initialize service layer
        this.contactService = new ContactService();
        
        // Build UI
        initializeUI();
        
        // Load initial data from database
        loadContacts();
    }
    
    /**
     * Initialize the user interface
     */
    private void initializeUI() {
        // Window configuration
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // Create UI components
        JPanel topPanel = createTopPanel();
        JPanel centerPanel = createCenterPanel();
        JPanel bottomPanel = createBottomPanel();
        
        // Add to frame
        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    /**
     * Create top panel (toolbar area)
     * Currently placeholder - will be enhanced in PHASE 12
     */
    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setPreferredSize(new Dimension(WINDOW_WIDTH, 60));
        panel.setBackground(new Color(240, 240, 240));
        
        JLabel placeholderLabel = new JLabel("Toolbar (Will be enhanced in PHASE 12)");
        panel.add(placeholderLabel);
        
        return panel;
    }
    
    /**
     * Create center panel with JTable
     * 
     * This is the main content area displaying all contacts in tabular format
     * 
     * Components:
     * - JTable: Displays contact data in rows and columns
     * - DefaultTableModel: Manages table data and structure
     * - JScrollPane: Provides scrolling for large datasets
     */
    private JPanel createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        
        // ========== Create Table Model ==========
        
        // Define column names
        // These are the headers that appear at the top of the table
        String[] columnNames = {
            "ID",           // Contact database ID (hidden from user typically)
            "Name",         // Contact's full name
            "Phone",        // Phone number
            "Email",        // Email address
            "Category",     // Category (Family, Work, Friends, etc.)
            "Created"       // When contact was created
        };
        
        // Create table model
        // DefaultTableModel manages the data structure of the table
        // - Stores data as Object[][]  (2D array)
        // - Handles adding/removing rows
        // - Notifies table when data changes
        // 
        // Parameters:
        // - null: No initial data (we'll load from database)
        // - columnNames: Column headers
        tableModel = new DefaultTableModel(null, columnNames) {
            // Override isCellEditable to make table read-only
            // Without this, users could double-click cells and edit directly
            // We want controlled editing through forms, not inline editing
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // All cells are read-only
            }
        };
        
        // ========== Create JTable ==========
        
        // Create JTable with the model
        // JTable is the visual component
        // TableModel is the data component
        // Separation allows model to be reused in different views
        contactTable = new JTable(tableModel);
        
        // Configure table appearance and behavior
        
        // Set row height for better readability
        // Default is ~16px, which feels cramped
        // 25px gives breathing room and looks professional
        contactTable.setRowHeight(25);
        
        // Enable single row selection
        // User can click one row at a time
        // Alternative: MULTIPLE_INTERVAL_SELECTION allows Ctrl+Click for multiple rows
        contactTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Set font for table content
        // Slightly larger than default for readability
        contactTable.setFont(new Font("Arial", Font.PLAIN, 13));
        
        // Configure table header (column names)
        contactTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        contactTable.getTableHeader().setBackground(new Color(70, 130, 180)); // Steel blue
        contactTable.getTableHeader().setForeground(Color.WHITE);
        contactTable.getTableHeader().setReorderingAllowed(false); // Prevent column dragging
        
        // Set column widths for better proportions
        // Without this, all columns get equal width (looks bad)
        // ID column narrow (just numbers)
        contactTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        contactTable.getColumnModel().getColumn(0).setMaxWidth(80);
        
        // Name column wider (names can be long)
        contactTable.getColumnModel().getColumn(1).setPreferredWidth(200);
        
        // Phone column medium
        contactTable.getColumnModel().getColumn(2).setPreferredWidth(150);
        
        // Email column wider (emails can be long)
        contactTable.getColumnModel().getColumn(3).setPreferredWidth(200);
        
        // Category column medium
        contactTable.getColumnModel().getColumn(4).setPreferredWidth(120);
        
        // Created column medium (dates)
        contactTable.getColumnModel().getColumn(5).setPreferredWidth(150);
        
        // Enable auto-sorting when clicking column headers
        // User can click "Name" to sort by name, etc.
        contactTable.setAutoCreateRowSorter(true);
        
        // Alternate row colors for better readability (zebra striping)
        // This is a common UX pattern in tables
        // We'll implement this with custom cell renderer later
        // For now, default white background
        
        // ========== Wrap in JScrollPane ==========
        
        // JScrollPane provides scrollbars when table is larger than visible area
        // Without this, table would be limited to window size
        // With this, table can have thousands of rows and user can scroll
        JScrollPane scrollPane = new JScrollPane(contactTable);
        
        // Configure scroll pane
        scrollPane.setBorder(BorderFactory.createEmptyBorder()); // Remove border
        scrollPane.getViewport().setBackground(Color.WHITE); // White background
        
        // Add scroll pane to panel
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Create bottom panel (status bar)
     * Currently placeholder - will show statistics in PHASE 14
     */
    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setPreferredSize(new Dimension(WINDOW_WIDTH, 30));
        panel.setBackground(new Color(220, 220, 220));
        
        JLabel placeholderLabel = new JLabel("Status Bar (Statistics will be added in PHASE 14)");
        panel.add(placeholderLabel);
        
        return panel;
    }
    
    /**
     * Load all contacts from database and populate table
     * 
     * This is the KEY method that bridges UI and backend:
     * 1. Calls Service layer to get contacts
     * 2. Converts Contact objects to table rows
     * 3. Updates table model
     * 4. Table automatically refreshes display
     * 
     * Called when:
     * - Application starts (constructor)
     * - User adds/edits/deletes contact
     * - User clicks Refresh button
     */
    public void loadContacts() {
        try {
            // ========== Fetch Data from Service ==========
            
            // Service layer handles all business logic and database access
            // UI just asks for data, doesn't know HOW it's retrieved
            List<Contact> contacts = contactService.getAllContacts();
            
            // ========== Clear Existing Table Data ==========
            
            // Remove all rows from table
            // setRowCount(0) is faster than removing rows one by one
            // This prepares table for fresh data
            tableModel.setRowCount(0);
            
            // ========== Populate Table with Contact Data ==========
            
            // Loop through each contact and add as table row
            for (Contact contact : contacts) {
                // Convert Contact object to Object array (table row)
                // Each array element corresponds to a column
                Object[] row = new Object[6]; // 6 columns
                
                // Map Contact fields to table columns
                row[0] = contact.getId();                    // Column 0: ID
                row[1] = contact.getName();                  // Column 1: Name
                row[2] = contact.getPhone();                 // Column 2: Phone
                row[3] = contact.getEmail() != null          // Column 3: Email
                        ? contact.getEmail() 
                        : "N/A"; // Show "N/A" if email is null
                row[4] = contact.getCategory() != null       // Column 4: Category
                        ? contact.getCategory() 
                        : "Uncategorized";
                row[5] = contact.getCreatedAt();             // Column 5: Created date
                
                // Add row to table model
                // This triggers table update automatically
                tableModel.addRow(row);
            }
            
            // ========== Update Status Bar ==========
            
            // Show contact count in status bar (will be enhanced in PHASE 14)
            // For now, just print to console
            System.out.println("Loaded " + contacts.size() + " contacts");
            
        } catch (Exception e) {
            // Handle errors gracefully
            // Show error dialog to user instead of crashing
            JOptionPane.showMessageDialog(
                this,
                "Error loading contacts: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE
            );
            e.printStackTrace();
        }
    }
    
    /**
     * Refresh table data
     * Convenience method that delegates to loadContacts()
     */
    public void refreshTable() {
        loadContacts();
    }
    
    /**
     * Main method for testing UI independently
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ContactDashboardUI ui = new ContactDashboardUI();
            ui.setVisible(true);
        });
    }
}
```

### JTable Integration Explanation

#### **Why JTable Uses TableModel**

**The Model-View separation:**

```
┌─────────────────┐
│  JTable (View)  │  ← What user sees (visual representation)
└────────┬────────┘
         │
         ↓
┌─────────────────┐
│ TableModel      │  ← What data exists (structure and content)
│ (DefaultTable   │
│  Model)         │
└─────────────────┘
```

**Why separate model from view?**

**Without separation (hypothetical):**
```java
// BAD: Data and display mixed
public class ContactTable extends JTable {
    private Object[][] data; // Data embedded in view
    
    public void addContact(Contact c) {
        // Must manually update display
        repaint();
        validate();
    }
}
```

**Problems:**
- Cannot reuse data in different views (table, list, chart)
- Difficult to update display when data changes
- Testing requires creating actual UI components
- Cannot serialize/save data independently

**With separation (our approach):**
```java
// GOOD: Model separate from view
DefaultTableModel model = new DefaultTableModel();  // Data
JTable table = new JTable(model);                   // Display

model.addRow(rowData);  // Add to model
// Table automatically updates display!
```

**Benefits:**
- ✅ **Automatic updates**: Change model → view updates automatically
- ✅ **Multiple views**: Same model can drive table, chart, export
- ✅ **Testable**: Test model without creating UI
- ✅ **Reusable**: Model can be saved, loaded, transmitted
- ✅ **MVC pattern**: Clean architecture

#### **Why Table Model Separation Is Important**

**Real-world example: Multi-view application**

```java
// Same data, multiple views
DefaultTableModel model = new DefaultTableModel(data, columns);

JTable table = new JTable(model);           // Tabular view
JList list = new JList(model);              // List view
JTree tree = new JTree(model);              // Tree view
PieChart chart = new PieChart(model);       // Chart view

// Update data once
model.addRow(newData);

// All views update automatically!
```

**Testing benefits:**
```java
// Can test data logic without UI
@Test
public void testContactData() {
    DefaultTableModel model = new DefaultTableModel();
    model.addRow(new Object[]{"John", "123-456-7890"});
    
    assertEquals(1, model.getRowCount());
    assertEquals("John", model.getValueAt(0, 0));
    // No JTable creation needed!
}
```

#### **How Data Flows: Service → DAO → Database**

**Complete flow with code:**

**1. UI requests data:**
```java
// ContactDashboardUI.java
public void loadContacts() {
    List<Contact> contacts = contactService.getAllContacts(); // Step 1
    // ...
}
```

**2. Service delegates to DAO:**
```java
// ContactService.java
public List<Contact> getAllContacts() {
    try {
        return contactDAO.getAll(); // Step 2
    } catch (SQLException e) {
        throw new RuntimeException("Failed to retrieve contacts", e);
    }
}
```

**3. DAO queries database:**
```java
// ContactDAOImpl.java
public List<Contact> getAll() throws SQLException {
    String sql = "SELECT * FROM contacts WHERE is_deleted = 0 ORDER BY name"; // Step 3
    List<Contact> contacts = new ArrayList<>();
    
    try (Connection conn = DBConnection.getConnection();
         PreparedStatement pst = conn.prepareStatement(sql);
         ResultSet rs = pst.executeQuery()) { // Step 4: Database executes
        
        while (rs.next()) {
            contacts.add(mapResultSetToContact(rs)); // Step 5: Convert to Contact
        }
    }
    return contacts; // Step 6: Return to Service
}
```

**4. Service returns to UI:**
```java
// Back to ContactDashboardUI.java
List<Contact> contacts = contactService.getAllContacts(); // Step 7: Receive List<Contact>

for (Contact contact : contacts) {
    Object[] row = new Object[]{
        contact.getId(),
        contact.getName(),
        // ... map to row
    };
    tableModel.addRow(row); // Step 8: Add to table
}
// Step 9: JTable automatically displays
```

**Visual flow:**
```
User Action: Opens app / Clicks refresh
    ↓
UI: loadContacts()
    ↓
Service: getAllContacts()
    ↓
DAO: getAll()
    ↓
Database: SELECT * FROM contacts WHERE is_deleted = 0
    ↓
Database: Returns ResultSet (rows)
    ↓
DAO: Converts ResultSet → List<Contact>
    ↓
Service: Returns List<Contact> (no modification)
    ↓
UI: Converts List<Contact> → Table rows
    ↓
TableModel: Holds row data
    ↓
JTable: Displays to user
```

#### **Why We Must Refresh Table After CRUD**

**The problem:**

```java
// User adds contact
Contact newContact = new Contact("John", "123-456-7890", ...);
contactService.addContact(newContact);

// Contact is now in database
// But table still shows old data!
```

**Why table doesn't auto-update:**
- Table displays what's in TableModel
- TableModel contains data from last loadContacts() call
- Database has changed, but TableModel hasn't
- TableModel doesn't "watch" the database

**The solution:**

```java
// After adding contact
contactService.addContact(newContact);
loadContacts(); // Re-fetch from database and update table
```

**Complete example:**
```java
// Add button action
addButton.addActionListener(e -> {
    // 1. Show dialog, get contact data from user
    Contact newContact = showAddContactDialog();
    
    if (newContact != null) {
        try {
            // 2. Save to database via Service
            contactService.addContact(newContact);
            
            // 3. Refresh table to show new contact
            loadContacts();
            
            // 4. Show success message
            JOptionPane.showMessageDialog(this, "Contact added successfully!");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }
});
```

**Alternative approach (manual update):**
```java
// Instead of reloading everything, just add the row
contactService.addContact(newContact);

Object[] row = new Object[]{
    newContact.getId(),
    newContact.getName(),
    newContact.getPhone(),
    // ...
};
tableModel.addRow(row); // Add to existing model
```

**Why we reload instead:**
- ✅ **Consistency**: Table exactly matches database state
- ✅ **Sorting**: Auto-sorts new contact into correct position
- ✅ **Calculated fields**: Picks up database-generated values (created_at)
- ✅ **Simpler code**: One method handles all updates
- ✅ **Error handling**: If save failed, reload shows correct state

**Performance consideration:**
- Reloading is fine for small datasets (< 10,000 contacts)
- For large datasets, use manual update + periodic full refresh
- For real-time multi-user apps, use database change notifications

#### **Line-by-Line: Table Model Creation**

```java
String[] columnNames = {"ID", "Name", "Phone", "Email", "Category", "Created"};
```
- Define column headers
- These appear at top of table
- User sees these names
- Order matters (matches row data order)

```java
tableModel = new DefaultTableModel(null, columnNames) {
```
- Create table model
- `null` = no initial data (load later)
- `columnNames` = headers
- Anonymous class to override methods

```java
    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }
};
```
- **Critical override**: Make table read-only
- Without this: User can double-click cell and edit directly
- With this: Cannot edit inline (must use Edit button + dialog)

**Why read-only?**
- **Validation**: Need to validate before saving to database
- **UI consistency**: All edits through same dialog
- **Better UX**: Form dialog provides better editing experience
- **Error handling**: Can show validation errors before database update

#### **Line-by-Line: JTable Configuration**

```java
contactTable = new JTable(tableModel);
```
- Create JTable with model
- JTable is the visual component
- Model provides the data

```java
contactTable.setRowHeight(25);
```
- Set row height in pixels
- Default (~16px) is cramped
- 25px provides breathing room
- Improves readability

```java
contactTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
```
- One row selected at a time
- Alternatives:
  - `SINGLE_INTERVAL_SELECTION` = Click + Shift to select range
  - `MULTIPLE_INTERVAL_SELECTION` = Ctrl+Click for multiple

```java
contactTable.getTableHeader().setReorderingAllowed(false);
```
- Prevent column dragging
- User cannot rearrange columns
- Why? Complicates code (column indices change)
- Alternative: Allow reordering, save user's preferred order

```java
contactTable.getColumnModel().getColumn(0).setPreferredWidth(50);
contactTable.getColumnModel().getColumn(0).setMaxWidth(80);
```
- Set column widths
- Preferred width = hint to layout manager
- Max width = cannot grow beyond this
- ID column narrow (just numbers)
- Name/Email columns wider (text can be long)

**Without width configuration:**
```
┌────────┬────────┬────────┬────────┐
│   ID   │  Name  │ Phone  │ Email  │  ← All equal width (bad)
└────────┴────────┴────────┴────────┘
```

**With width configuration:**
```
┌───┬──────────────┬────────────┬──────────────────┐
│ ID│ Name         │ Phone      │ Email            │  ← Proportional (good)
└───┴──────────────┴────────────┴──────────────────┘
```

```java
contactTable.setAutoCreateRowSorter(true);
```
- **Powerful feature**: Click column header to sort
- Click "Name" → sorts alphabetically
- Click again → reverses sort
- Works on all columns automatically
- Sorting happens in table view, not database

#### **Line-by-Line: loadContacts() Method**

```java
List<Contact> contacts = contactService.getAllContacts();
```
- Call Service layer
- Service handles business logic + DAO
- Returns List of Contact objects
- UI doesn't know about SQL, database, JDBC

```java
tableModel.setRowCount(0);
```
- Clear all existing rows
- Fast way to empty table
- Alternative: `while (model.getRowCount() > 0) model.removeRow(0);` (slower)

```java
for (Contact contact : contacts) {
```
- Loop through each contact
- Convert to table row format

```java
    Object[] row = new Object[6];
```
- Create array for one row
- Length matches number of columns (6)
- Each element is one cell

```java
    row[0] = contact.getId();
    row[1] = contact.getName();
    row[2] = contact.getPhone();
```
- Map Contact fields to array positions
- Position must match column order
- Type is Object (table accepts any type)

```java
    row[3] = contact.getEmail() != null ? contact.getEmail() : "N/A";
```
- Handle null email
- Database allows NULL, table should show something
- "N/A" = Not Available (user-friendly)
- Alternative: "" (empty string)

```java
    tableModel.addRow(row);
```
- Add row to model
- Model notifies table
- Table repaints to show new row
- Automatic visual update

```java
} catch (Exception e) {
    JOptionPane.showMessageDialog(this, "Error loading contacts: " + e.getMessage(), ...);
}
```
- Graceful error handling
- Don't crash application on database error
- Show user-friendly error dialog
- Log to console for debugging

---

## 🛠️ PHASE 12 — Toolbar + Safe Icon Loading

### Updated File: `ui/ContactDashboardUI.java` (createTopPanel method)

```java
/**
 * Create top panel (toolbar with action buttons)
 * 
 * PHASE 12 ENHANCEMENT:
 * - JToolBar with action buttons
 * - Icon loading with fallback
 * - Professional appearance
 */
private JPanel createTopPanel() {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setBackground(new Color(240, 240, 240));
    
    // ========== Create Toolbar ==========
    
    // JToolBar is a specialized container for tool buttons
    // Features:
    // - Can be floated (dragged out of window)
    // - Has built-in layout for buttons
    // - Standard look and feel
    JToolBar toolbar = new JToolBar();
    toolbar.setFloatable(false); // Prevent toolbar from being dragged out
    toolbar.setBackground(new Color(240, 240, 240));
    
    // ========== Create Buttons ==========
    
    // Add Contact button
    JButton addButton = createToolbarButton(
        "Add Contact",              // Button text
        "icons/add.png",            // Icon path (relative to classpath)
        "Add a new contact"         // Tooltip text
    );
    
    // Edit Contact button
    JButton editButton = createToolbarButton(
        "Edit",
        "icons/edit.png",
        "Edit selected contact"
    );
    
    // Delete Contact button
    JButton deleteButton = createToolbarButton(
        "Delete",
        "icons/delete.png",
        "Delete selected contact"
    );
    
    // Refresh button
    JButton refreshButton = createToolbarButton(
        "Refresh",
        "icons/refresh.png",
        "Refresh contact list"
    );
    
    // Search button
    JButton searchButton = createToolbarButton(
        "Search",
        "icons/search.png",
        "Search contacts"
    );
    
    // Recycle Bin button
    JButton recycleBinButton = createToolbarButton(
        "Recycle Bin",
        "icons/recycle.png",
        "View deleted contacts"
    );
    
    // ========== Add Action Listeners ==========
    
    // Refresh button action (functional now)
    refreshButton.addActionListener(e -> {
        loadContacts(); // Reload table data
        JOptionPane.showMessageDialog(this, "Contacts refreshed!");
    });
    
    // Other buttons - placeholder actions (will be implemented in PART 4)
    addButton.addActionListener(e -> {
        JOptionPane.showMessageDialog(this, "Add Contact dialog will be implemented in PART 4");
    });
    
    editButton.addActionListener(e -> {
        JOptionPane.showMessageDialog(this, "Edit Contact dialog will be implemented in PART 4");
    });
    
    deleteButton.addActionListener(e -> {
        JOptionPane.showMessageDialog(this, "Delete functionality will be implemented in PART 4");
    });
    
    searchButton.addActionListener(e -> {
        JOptionPane.showMessageDialog(this, "Search dialog will be implemented in PART 4");
    });
    
    recycleBinButton.addActionListener(e -> {
        JOptionPane.showMessageDialog(this, "Recycle Bin dialog will be implemented in PART 4");
    });
    
    // ========== Add Buttons to Toolbar ==========
    
    toolbar.add(addButton);
    toolbar.add(editButton);
    toolbar.add(deleteButton);
    toolbar.addSeparator(); // Visual separator between button groups
    toolbar.add(refreshButton);
    toolbar.add(searchButton);
    toolbar.addSeparator();
    toolbar.add(recycleBinButton);
    
    // Add toolbar to panel
    panel.add(toolbar, BorderLayout.CENTER);
    
    return panel;
}

/**
 * Create a toolbar button with icon and text
 * 
 * This method handles:
 * - Safe icon loading (works in both IDE and JAR)
 * - Icon scaling to appropriate size
 * - Fallback if icon not found
 * - Consistent button styling
 * 
 * @param text Button label
 * @param iconPath Path to icon file (relative to classpath)
 * @param tooltip Tooltip text shown on hover
 * @return Configured JButton
 */
private JButton createToolbarButton(String text, String iconPath, String tooltip) {
    JButton button = new JButton(text);
    
    // ========== Load Icon Safely ==========
    
    // Load icon using class loader
    // This works in both development (IDE) and production (JAR file)
    try {
        // Get icon as resource stream
        // getClass().getResource() looks in classpath (works in JAR)
        // Contrast with: new File("icons/add.png") - fails in JAR
        java.net.URL iconURL = getClass().getClassLoader().getResource(iconPath);
        
        if (iconURL != null) {
            // Icon found - load and scale it
            ImageIcon originalIcon = new ImageIcon(iconURL);
            
            // Scale icon to appropriate size
            // Toolbar icons are typically 24x24 or 32x32 pixels
            Image scaledImage = originalIcon.getImage().getScaledInstance(
                24, 24,                     // Target width and height
                Image.SCALE_SMOOTH          // Scaling algorithm (high quality)
            );
            
            // Create new ImageIcon with scaled image
            ImageIcon scaledIcon = new ImageIcon(scaledImage);
            
            // Set icon on button
            button.setIcon(scaledIcon);
        } else {
            // Icon not found - button will show text only
            System.out.println("Warning: Icon not found: " + iconPath);
            // Button still functional, just without icon
        }
    } catch (Exception e) {
        // Error loading icon - log but don't crash
        System.err.println("Error loading icon " + iconPath + ": " + e.getMessage());
        // Button will show text without icon
    }
    
    // ========== Configure Button ==========
    
    // Set tooltip (shown when mouse hovers)
    button.setToolTipText(tooltip);
    
    // Set button to show both icon and text
    // Other options:
    // - button.setText(null) = icon only
    // - button.setIcon(null) = text only
    button.setHorizontalTextPosition(SwingConstants.CENTER);
    button.setVerticalTextPosition(SwingConstants.BOTTOM);
    
    // Make button focusable (allows keyboard navigation)
    button.setFocusPainted(false); // Remove focus border (cleaner look)
    
    // Set consistent size
    button.setPreferredSize(new Dimension(90, 60));
    
    return button;
}
```

### Toolbar and Icon Loading Explanation

#### **Why File-Path Loading Fails**

**Development environment (IDE):**
```
Project Structure:
src/
  ui/
    ContactDashboardUI.java
  icons/
    add.png
    edit.png
```

**File-path loading:**
```java
// WRONG: File path loading
ImageIcon icon = new ImageIcon("icons/add.png");
// In IDE: Works (file exists in filesystem)
```

**Production (JAR file):**
```
contactmanager.jar (ZIP archive)
  ├── ui/
  │   └── ContactDashboardUI.class
  └── icons/
      ├── add.png
      └── edit.png
```

**Same code in JAR:**
```java
ImageIcon icon = new ImageIcon("icons/add.png");
// In JAR: FAILS - "icons/add.png" is not a file, it's inside ZIP
// Result: Buttons show no icons
```

**Why it fails:**
- JAR file is a ZIP archive
- Icons are inside the ZIP, not on filesystem
- `new File("icons/add.png")` looks for filesystem file
- File doesn't exist outside JAR

#### **Why getResource() Works in JAR**

**Correct approach:**
```java
// CORRECT: Resource loading
java.net.URL iconURL = getClass().getClassLoader().getResource("icons/add.png");
ImageIcon icon = new ImageIcon(iconURL);
```

**How it works:**

**In IDE:**
```java
getResource("icons/add.png")
→ Looks in classpath (src/ folder)
→ Finds src/icons/add.png
→ Returns file:///path/to/project/src/icons/add.png
→ ImageIcon loads from file
✓ Works
```

**In JAR:**
```java
getResource("icons/add.png")
→ Looks in classpath (inside JAR)
→ Finds icons/add.png inside contactmanager.jar
→ Returns jar:file:///path/to/contactmanager.jar!/icons/add.png
→ ImageIcon loads from JAR entry
✓ Works
```

**Key difference:**
- File path: Looks in filesystem only
- Resource path: Looks in classpath (filesystem OR JAR)

**ClassLoader hierarchy:**
```java
// Three ways to load resource:

// 1. Via class
getClass().getResource("/icons/add.png");
// "/" = absolute path from root of classpath

// 2. Via class loader
getClass().getClassLoader().getResource("icons/add.png");
// No leading "/" = relative to classpath root

// 3. Via class loader (static context)
ClassLoader.getSystemResource("icons/add.png");
// Use in static methods
```

**Path rules:**
```java
// With class.getResource():
getClass().getResource("/icons/add.png");   // Absolute (leading /)
getClass().getResource("../icons/add.png"); // Relative to class package

// With classLoader.getResource():
getClassLoader().getResource("icons/add.png"); // Always relative to classpath root
// Never use leading "/" with ClassLoader

// Our choice:
getClass().getClassLoader().getResource("icons/add.png");
// Clear, works in both IDE and JAR
```

#### **Why Icons Must Be Scaled**

**Original icon size problem:**

```java
// No scaling
ImageIcon icon = new ImageIcon(iconURL);
button.setIcon(icon);

// If icon is 128x128 pixels:
// Button becomes HUGE
// Toolbar looks broken
// Inconsistent sizes
```

**Visual problem:**
```
┌──────────────┐  ┌───┐  ┌────┐
│              │  │   │  │    │  ← Buttons different sizes
│  Add (128px) │  │Ed │  │Del │     (icons are different dimensions)
│              │  │   │  │    │
└──────────────┘  └───┘  └────┘
```

**Scaling solution:**
```java
Image scaledImage = originalIcon.getImage().getScaledInstance(
    24, 24,              // Target size (all icons same)
    Image.SCALE_SMOOTH   // High quality scaling
);
ImageIcon scaledIcon = new ImageIcon(scaledImage);
```

**Result:**
```
┌──────┐  ┌──────┐  ┌──────┐
│ Add  │  │ Edit │  │Delete│  ← All buttons same size
│ 24px │  │ 24px │  │ 24px │     Professional appearance
└──────┘  └──────┘  └──────┘
```

**Scaling algorithms:**
```java
Image.SCALE_DEFAULT    // Fastest, lowest quality
Image.SCALE_FAST       // Fast, lower quality
Image.SCALE_SMOOTH     // Slower, highest quality (our choice)
Image.SCALE_REPLICATE  // Medium speed, medium quality
Image.SCALE_AREA_AVERAGING // Good for downscaling
```

**Why SCALE_SMOOTH:**
- Best visual quality
- Smooth edges
- No pixelation
- Performance acceptable for small icons
- Icons loaded once at startup, not during runtime

#### **How Toolbar Improves UX**

**Without toolbar (menu-only):**
```
File  Edit  View  Help
┌────────────────────────────┐
│                            │
│  Contact Table             │
│                            │
└────────────────────────────┘

To add contact:
1. Click File menu
2. Click New Contact
3. Dialog opens
// 3 clicks, mouse movement
```

**With toolbar:**
```
[Add] [Edit] [Delete] | [Refresh] [Search] | [Recycle Bin]
┌────────────────────────────┐
│                            │
│  Contact Table             │
│                            │
└────────────────────────────┘

To add contact:
1. Click Add button
// 1 click
```

**UX benefits:**
- ✅ **Faster access**: Common actions one click away
- ✅ **Visual**: Icons are faster to recognize than text
- ✅ **Discoverable**: User sees available actions immediately
- ✅ **Professional**: Modern applications have toolbars
- ✅ **Consistent**: Standard pattern users expect

**Toolbar best practices:**
- Most common actions only (don't overcrowd)
- Group related actions (use separators)
- Icons + text (more accessible than icons alone)
- Tooltips for clarification
- Keyboard shortcuts for power users (add later)

---

## 🎨 PHASE 13 — Theme System (Dark/Light + Accent Colors)

### New File: `ui/ThemeManager.java`

```java
package ui;

import javax.swing.*;
import javax.swing.table.JTableHeader;
import java.awt.*;

/**
 * ThemeManager - Centralized Theme System
 * 
 * Purpose: Provides dark/light mode with accent colors
 * 
 * Why needed:
 * - Default Swing themes look dated (circa 2000)
 * - Users expect modern dark/light mode options
 * - Consistent color scheme improves professionalism
 * - Accessibility (high contrast for vision impairment)
 * 
 * Features:
 * - Dark mode with carefully chosen colors
 * - Light mode with clean palette
 * - Accent colors for visual interest
 * - Applies theme to entire application
 * - One-line theme switching
 */
public class ThemeManager {
    
    // ========== Theme Enumeration ==========
    
    /**
     * Available themes
     */
    public enum Theme {
        LIGHT,
        DARK
    }
    
    // ========== Light Mode Colors ==========
    
    // Background colors
    private static final Color LIGHT_BG_PRIMARY = Color.WHITE;
    private static final Color LIGHT_BG_SECONDARY = new Color(245, 245, 245);
    private static final Color LIGHT_BG_TERTIARY = new Color(230, 230, 230);
    
    // Text colors
    private static final Color LIGHT_TEXT_PRIMARY = new Color(33, 33, 33);
    private static final Color LIGHT_TEXT print_SECONDARY = new Color(115, 115, 115);
    
    // Border colors
    private static final Color LIGHT_BORDER = new Color(200, 200, 200);
    
    // Accent color (blue)
    private static final Color LIGHT_ACCENT = new Color(0, 122, 204);
    
    // Selection colors
    private static final Color LIGHT_SELECTION_BG = new Color(184, 207, 229);
    private static final Color LIGHT_SELECTION_FG = new Color(33, 33, 33);
    
    // ========== Dark Mode Colors ==========
    
    // Background colors (carefully chosen for readability)
    private static final Color DARK_BG_PRIMARY = new Color(30, 30, 30);       // Main background
    private static final Color DARK_BG_SECONDARY = new Color(40, 40, 40);     // Panels, cards
    private static final Color DARK_BG_TERTIARY = new Color(50, 50, 50);      // Headers, toolbars
    
    // Text colors (high contrast for readability)
    private static final Color DARK_TEXT_PRIMARY = new Color(230, 230, 230);  // Main text
    private static final Color DARK_TEXT_SECONDARY = new Color(180, 180, 180); // Secondary text
    
    // Border colors (subtle, not too bright)
    private static final Color DARK_BORDER = new Color(60, 60, 60);
    
    // Accent color (adjusted for dark theme)
    private static final Color DARK_ACCENT = new Color(100, 180, 255);
    
    // Selection colors (visible but not jarring)
    private static final Color DARK_SELECTION_BG = new Color(70, 130, 180);
    private static final Color DARK_SELECTION_FG = Color.WHITE;
    
    // ========== Current Theme ==========
    
    private static Theme currentTheme = Theme.LIGHT; // Default theme
    
    /**
     * Apply theme to entire application
     * 
     * This method:
     * 1. Sets UIManager defaults (affects new components)
     * 2. Updates existing components recursively
     * 3. Forces repaint
     * 
     * Call this:
     * - At application startup (before creating UI)
     * - When user switches theme
     * 
     * @param theme Theme to apply (LIGHT or DARK)
     */
    public static void applyTheme(Theme theme) {
        currentTheme = theme;
        
        if (theme == Theme.DARK) {
            applyDarkTheme();
        } else {
            applyLightTheme();
        }
    }
    
    /**
     * Apply dark theme
     */
    private static void applyDarkTheme() {
        // ========== UIManager Defaults ==========
        // These affect ALL future Swing components
        
        // Panel backgrounds
        UIManager.put("Panel.background", DARK_BG_PRIMARY);
        UIManager.put("OptionPane.background", DARK_BG_PRIMARY);
        UIManager.put("ScrollPane.background", DARK_BG_PRIMARY);
        
        // Text components
        UIManager.put("TextField.background", DARK_BG_SECONDARY);
        UIManager.put("TextField.foreground", DARK_TEXT_PRIMARY);
        UIManager.put("TextField.caretForeground", DARK_TEXT_PRIMARY);
        UIManager.put("TextField.inactiveForeground", DARK_TEXT_SECONDARY);
        UIManager.put("TextField.selectionBackground", DARK_SELECTION_BG);
        UIManager.put("TextField.selectionForeground", DARK_SELECTION_FG);
        
        UIManager.put("TextArea.background", DARK_BG_SECONDARY);
        UIManager.put("TextArea.foreground", DARK_TEXT_PRIMARY);
        UIManager.put("TextArea.caretForeground", DARK_TEXT_PRIMARY);
        UIManager.put("TextArea.inactiveForeground", DARK_TEXT_SECONDARY);
        UIManager.put("TextArea.selectionBackground", DARK_SELECTION_BG);
        UIManager.put("TextArea.selectionForeground", DARK_SELECTION_FG);
        
        // Labels
        UIManager.put("Label.foreground", DARK_TEXT_PRIMARY);
        UIManager.put("Label.disabledForeground", DARK_TEXT_SECONDARY);
        
        // Buttons
        UIManager.put("Button.background", DARK_BG_SECONDARY);
        UIManager.put("Button.foreground", DARK_TEXT_PRIMARY);
        UIManager.put("Button.select", DARK_BG_TERTIARY); // Pressed state
        
        // Tables
        UIManager.put("Table.background", DARK_BG_PRIMARY);
        UIManager.put("Table.foreground", DARK_TEXT_PRIMARY);
        UIManager.put("Table.selectionBackground", DARK_SELECTION_BG);
        UIManager.put("Table.selectionForeground", DARK_SELECTION_FG);
        UIManager.put("Table.gridColor", DARK_BORDER);
        UIManager.put("Table.focusCellBackground", DARK_SELECTION_BG);
        UIManager.put("Table.focusCellForeground", DARK_SELECTION_FG);
        
        // Table header (requires manual styling - see applyThemeToComponent)
        UIManager.put("TableHeader.background", DARK_BG_TERTIARY);
        UIManager.put("TableHeader.foreground", DARK_TEXT_PRIMARY);
        
        // Scroll bars
        UIManager.put("ScrollBar.background", DARK_BG_SECONDARY);
        UIManager.put("ScrollBar.thumb", DARK_BG_TERTIARY);
        UIManager.put("ScrollBar.thumbHighlight", DARK_BORDER);
        UIManager.put("ScrollBar.thumbShadow", DARK_BORDER);
        UIManager.put("ScrollBar.track", DARK_BG_PRIMARY);
        
        // Menus
        UIManager.put("Menu.background", DARK_BG_SECONDARY);
        UIManager.put("Menu.foreground", DARK_TEXT_PRIMARY);
        UIManager.put("MenuBar.background", DARK_BG_SECONDARY);
        UIManager.put("MenuBar.foreground", DARK_TEXT_PRIMARY);
        UIManager.put("MenuItem.background", DARK_BG_SECONDARY);
        UIManager.put("MenuItem.foreground", DARK_TEXT_PRIMARY);
        UIManager.put("MenuItem.selectionBackground", DARK_SELECTION_BG);
        UIManager.put("MenuItem.selectionForeground", DARK_SELECTION_FG);
        
        // Borders
        UIManager.put("Component.borderColor", DARK_BORDER);
    }
    
    /**
     * Apply light theme
     */
    private static void applyLightTheme() {
        // ========== UIManager Defaults ==========
        
        // Panel backgrounds
        UIManager.put("Panel.background", LIGHT_BG_PRIMARY);
        UIManager.put("OptionPane.background", LIGHT_BG_PRIMARY);
        UIManager.put("ScrollPane.background", LIGHT_BG_PRIMARY);
        
        // Text components
        UIManager.put("TextField.background", Color.WHITE);
        UIManager.put("TextField.foreground", LIGHT_TEXT_PRIMARY);
        UIManager.put("TextField.caretForeground", LIGHT_TEXT_PRIMARY);
        UIManager.put("TextField.inactiveForeground", LIGHT_TEXT_SECONDARY);
        UIManager.put("TextField.selectionBackground", LIGHT_SELECTION_BG);
        UIManager.put("TextField.selectionForeground", LIGHT_SELECTION_FG);
        
        UIManager.put("TextArea.background", Color.WHITE);
        UIManager.put("TextArea.foreground", LIGHT_TEXT_PRIMARY);
        UIManager.put("TextArea.caretForeground", LIGHT_TEXT_PRIMARY);
        UIManager.put("TextArea.inactiveForeground", LIGHT_TEXT_SECONDARY);
        UIManager.put("TextArea.selectionBackground", LIGHT_SELECTION_BG);
        UIManager.put("TextArea.selectionForeground", LIGHT_SELECTION_FG);
        
        // Labels
        UIManager.put("Label.foreground", LIGHT_TEXT_PRIMARY);
        UIManager.put("Label.disabledForeground", LIGHT_TEXT_SECONDARY);
        
        // Buttons
        UIManager.put("Button.background", LIGHT_BG_SECONDARY);
        UIManager.put("Button.foreground", LIGHT_TEXT_PRIMARY);
        UIManager.put("Button.select", LIGHT_BG_TERTIARY);
        
        // Tables
        UIManager.put("Table.background", Color.WHITE);
        UIManager.put("Table.foreground", LIGHT_TEXT_PRIMARY);
        UIManager.put("Table.selectionBackground", LIGHT_SELECTION_BG);
        UIManager.put("Table.selectionForeground", LIGHT_SELECTION_FG);
        UIManager.put("Table.gridColor", LIGHT_BORDER);
        UIManager.put("Table.focusCellBackground", LIGHT_SELECTION_BG);
        UIManager.put("Table.focusCellForeground", LIGHT_SELECTION_FG);
        
        // Table header
        UIManager.put("TableHeader.background", LIGHT_BG_TERTIARY);
        UIManager.put("TableHeader.foreground", LIGHT_TEXT_PRIMARY);
        
        // Scroll bars
        UIManager.put("ScrollBar.background", LIGHT_BG_SECONDARY);
        UIManager.put("ScrollBar.thumb", LIGHT_BG_TERTIARY);
        UIManager.put("ScrollBar.thumbHighlight", Color.WHITE);
        UIManager.put("ScrollBar.thumbShadow", LIGHT_BORDER);
        UIManager.put("ScrollBar.track", LIGHT_BG_SECONDARY);
        
        // Menus
        UIManager.put("Menu.background", LIGHT_BG_PRIMARY);
        UIManager.put("Menu.foreground", LIGHT_TEXT_PRIMARY);
        UIManager.put("MenuBar.background", LIGHT_BG_SECONDARY);
        UIManager.put("MenuBar.foreground", LIGHT_TEXT_PRIMARY);
        UIManager.put("MenuItem.background", LIGHT_BG_PRIMARY);
        UIManager.put("MenuItem.foreground", LIGHT_TEXT_PRIMARY);
        UIManager.put("MenuItem.selectionBackground", LIGHT_SELECTION_BG);
        UIManager.put("MenuItem.selectionForeground", LIGHT_SELECTION_FG);
        
        // Borders
        UIManager.put("Component.borderColor", LIGHT_BORDER);
    }
    
    /**
     * Apply theme to specific component and its children
     * 
     * UIManager defaults only affect NEW components.
     * Existing components need manual update.
     * This method recursively updates component tree.
     * 
     * @param component Root component to update
     */
    public static void applyThemeToComponent(Component component) {
        if (component == null) return;
        
        // Update this component
        updateComponentColors(component);
        
        // Recursively update children
        if (component instanceof Container) {
            Container container = (Container) component;
            for (Component child : container.getComponents()) {
                applyThemeToComponent(child); // Recursive call
            }
        }
        
        // Force repaint
        component.revalidate();
        component.repaint();
    }
    
    /**
     * Update colors for specific component
     * 
     * Handles special cases that UIManager doesn't cover
     */
    private static void updateComponentColors(Component component) {
        Color bg = currentTheme == Theme.DARK ? DARK_BG_PRIMARY : LIGHT_BG_PRIMARY;
        Color fg = currentTheme == Theme.DARK ? DARK_TEXT_PRIMARY : LIGHT_TEXT_PRIMARY;
        
        // Set basic colors
        component.setBackground(bg);
        component.setForeground(fg);
        
        // ========== Special Component Handling ==========
        
        // JTable header needs manual styling
        if (component instanceof JTable) {
            JTable table = (JTable) component;
            JTableHeader header = table.getTableHeader();
            
            if (header != null) {
                if (currentTheme == Theme.DARK) {
                    header.setBackground(DARK_BG_TERTIARY);
                    header.setForeground(DARK_TEXT_PRIMARY);
                } else {
                    header.setBackground(new Color(70, 130, 180)); // Keep light theme blue
                    header.setForeground(Color.WHITE);
                }
            }
        }
        
        // ScrollPane viewport background
        if (component instanceof JScrollPane) {
            JScrollPane scrollPane = (JScrollPane) component;
            scrollPane.getViewport().setBackground(bg);
        }
        
        // Buttons
        if (component instanceof JButton) {
            Color buttonBg = currentTheme == Theme.DARK ? DARK_BG_SECONDARY : LIGHT_BG_SECONDARY;
            component.setBackground(buttonBg);
        }
    }
    
    /**
     * Get current theme
     */
    public static Theme getCurrentTheme() {
        return currentTheme;
    }
    
    /**
     * Toggle between light and dark theme
     */
    public static void toggleTheme() {
        if (currentTheme == Theme.DARK) {
            applyTheme(Theme.LIGHT);
        } else {
            applyTheme(Theme.DARK);
        }
    }
    
    /**
     * Get accent color for current theme
     */
    public static Color getAccentColor() {
        return currentTheme == Theme.DARK ? DARK_ACCENT : LIGHT_ACCENT;
    }
    
    /**
     * Get primary background color for current theme
     */
    public static Color getPrimaryBackground() {
        return currentTheme == Theme.DARK ? DARK_BG_PRIMARY : LIGHT_BG_PRIMARY;
    }
    
    /**
     * Get secondary background color for current theme
     */
    public static Color getSecondaryBackground() {
        return currentTheme == Theme.DARK ? DARK_BG_SECONDARY : LIGHT_BG_SECONDARY;
    }
    
    /**
     * Get primary text color for current theme
     */
    public static Color getPrimaryText() {
        return currentTheme == Theme.DARK ? DARK_TEXT_PRIMARY : LIGHT_TEXT_PRIMARY;
    }
    
    /**
     * Get secondary text color for current theme
     */
    public static Color getSecondaryText() {
        return currentTheme == Theme.DARK ? DARK_TEXT_SECONDARY : LIGHT_TEXT_SECONDARY;
    }
}
```

### Usage in ContactDashboardUI:

```java
// In ContactDashboardUI constructor, before initializeUI():
public ContactDashboardUI() {
    super("Contact Management System");
    
    // Apply theme BEFORE creating components
    ThemeManager.applyTheme(ThemeManager.Theme.DARK); // Or LIGHT
    
    this.contactService = new ContactService();
    initializeUI();
    loadContacts();
    
    // Apply theme to existing components (if any were created before theme)
    ThemeManager.applyThemeToComponent(this);
}
```

### Theme System Explanation

#### **Why UIManager Alone Is Not Enough**

**UIManager sets defaults for the Look and Feel:**
```java
UIManager.put("Panel.background", Color.BLACK);
```

**What this does:**
- Sets DEFAULT color for NEW Panel instances
- Doesn't affect EXISTING panels
- Doesn't affect ALL component types (some ignore UIManager)

**Example problem:**
```java
// Create panel
JPanel panel = new JPanel();
panel.setBackground(Color.WHITE); // Explicitly white

// Apply theme
UIManager.put("Panel.background", Color.BLACK);

// Panel is STILL white!
// Explicit setBackground() overrides UIManager default
```

**Why we need both UIManager AND manual updates:**

**UIManager:**
- Sets defaults for future components
- Ensures consistency for new dialogs, windows

**Manual updates (applyThemeToComponent):**
- Updates existing components
- Handles components that ignore UIManager
- Applies theme to entire component tree

**Complete solution:**
```java
// Step 1: Set UIManager defaults
UIManager.put("Panel.background", darkColor);

// Step 2: Update existing components
ThemeManager.applyThemeToComponent(mainWindow);
// Recursively updates all existing panels
```

#### **Why JTable Header Must Be Styled Manually**

**The problem:**

```java
// JTable uses UIManager for body
UIManager.put("Table.background", DARK_BG);
JTable table = new JTable();
// Table body: Dark ✓

// But header has separate rendering
JTableHeader header = table.getTableHeader();
// Header: Still light ✗ (uses different defaults)
```

**Why headers are special:**
- JTableHeader is separate component
- Has its own renderer
- Uses different UIManager keys
- Some Look and Feels ignore UIManager for headers

**Manual solution:**
```java
JTableHeader header = table.getTableHeader();
header.setBackground(DARK_BG_TERTIARY);
header.setForeground(DARK_TEXT_PRIMARY);
header.setFont(new Font("Arial", Font.BOLD, 13));
```

**Where this is done:**
```java
private static void updateComponentColors(Component component) {
    if (component instanceof JTable) {
        JTable table = (JTable) component;
        JTableHeader header = table.getTableHeader();
        // Manually style header
        header.setBackground(...);
        header.setForeground(...);
    }
}
```

#### **Why Contrast Matters in Dark Themes**

**Poor contrast (unreadable):**
```
Dark background: #1a1a1a (very dark gray)
Text color: #333333 (dark gray)
Contrast ratio: 1.5:1
Result: Cannot read text ✗
```

**Good contrast (readable):**
```
Dark background: #1e1e1e (RGB 30,30,30)
Text color: #e6e6e6 (RGB 230,230,230)
Contrast ratio: 12:1
Result: Easy to read ✓
```

**WCAG Standards (Web Content Accessibility Guidelines):**
- **AA compliance**: 4.5:1 contrast minimum
- **AAA compliance**: 7:1 contrast minimum
- Our theme: ~12:1 (exceeds AAA)

**Our color choices:**
```java
// Dark theme
DARK_BG_PRIMARY = new Color(30, 30, 30);      // Very dark, not pure black
DARK_TEXT_PRIMARY = new Color(230, 230, 230); // Very light, not pure white

// Why not pure black (#000000)?
// - Pure black + pure white = harsh, eye strain
// - Slightly softened colors reduce fatigue
// - Closer to ink on paper (natural reading)
```

**Selection colors:**
```java
DARK_SELECTION_BG = new Color(70, 130, 180);  // Steel blue
DARK_SELECTION_FG = Color.WHITE;               // White text

// Why blue?
// - Universal selection color (familiar to users)
// - High contrast with both bg and text
// - Accessible for color blindness
```

#### **How Theme System Improves User Experience**

**Modern expectations:**
- ✅ Dark mode standard in 2026 (VS Code, IDEs, browsers)
- ✅ Reduces eye strain in low light
- ✅ Saves battery on OLED screens
- ✅ Professional appearance

**Accessibility:**
- ✅ High contrast for vision impairment
- ✅ Customizable (can add more themes)
- ✅ Consistent colors (no jarring changes)

**Implementation benefits:**
- ✅ Centralized color management
- ✅ One-line theme switching
- ✅ Easy to add new themes
- ✅ Colors defined once, used everywhere

**Usage scenarios:**
```java
// User preference storage (future)
public class Settings {
    public static void saveTheme(Theme theme) {
        Preferences prefs = Preferences.userRoot();
        prefs.put("theme", theme.name());
    }
    
    public static Theme loadTheme() {
        Preferences prefs = Preferences.userRoot();
        String themeName = prefs.get("theme", "LIGHT");
        return Theme.valueOf(themeName);
    }
}

// At startup
Theme savedTheme = Settings.loadTheme();
ThemeManager.applyTheme(savedTheme);
```

#### **Line-by-Line: Color Definitions**

```java
private static final Color DARK_BG_PRIMARY = new Color(30, 30, 30);
```
- Primary background = main window background
- RGB(30,30,30) = very dark gray, not pure black
- Softer on eyes than #000000

```java
private static final Color DARK_BG_SECONDARY = new Color(40, 40, 40);
```
- Secondary background = panels, cards, elevated elements
- Slightly lighter than primary (visual hierarchy)
- Distinguishes different UI levels

```java
private static final Color DARK_BG_TERTIARY = new Color(50, 50, 50);
```
- Tertiary background = headers, toolbars, active elements
- Even lighter = draws attention
- Three-level hierarchy: primary < secondary < tertiary

```java
private static final Color DARK_TEXT_PRIMARY = new Color(230, 230, 230);
```
- Main text color
- RGB(230,230,230) = very light gray, not pure white
- 12:1 contrast with DARK_BG_PRIMARY (excellent readability)

```java
private static final Color DARK_ACCENT = new Color(100, 180, 255);
```
- Accent color for important elements (buttons, links)
- Lighter blue works better on dark background
- Draws eye without being garish

#### **Line-by-Line: applyThemeToComponent**

```java
public static void applyThemeToComponent(Component component) {
```
- Recursively updates component tree
- Handles components created before theme applied

```java
updateComponentColors(component);
```
- Update this component's colors

```java
if (component instanceof Container) {
    Container container = (Container) component;
    for (Component child : container.getComponents()) {
        applyThemeToComponent(child); // Recursive
    }
}
```
- Recursion: update all children
- Goes deep into component hierarchy
- Ensures no component missed

```java
component.revalidate();
component.repaint();
```
- **revalidate()**: Recalculate layout
- **repaint()**: Redraw with new colors
- Both needed for visual update

---

## 📈 PHASE 14 — Statistics Dashboard Panel

### New File: `ui/StatisticsPanel.java`

```java
package ui;

import service.ContactService;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.Contact;

/**
 * StatisticsPanel - Dashboard showing contact statistics
 * 
 * Purpose: Provide visual overview of contact data
 * 
 * Displays:
 * - Total active contacts
 * - Category distribution
 * - Deleted contacts count
 * - Visual cards with color coding
 * 
 * Design: Card-based layout with statistics
 */
public class StatisticsPanel extends JPanel {
    
    private ContactService contactService;
    
    // Labels to update
    private JLabel totalLabel;
    private JLabel deletedLabel;
    private JPanel categoryPanel;
    
    /**
     * Constructor
     */
    public StatisticsPanel() {
        this.contactService = new ContactService();
        
        setLayout(new FlowLayout(FlowLayout.LEFT, 15, 10));
        setBackground(ThemeManager.getSecondaryBackground());
        setPreferredSize(new Dimension(1200, 100));
        
        initializeComponents();
        updateStatistics();
    }
    
    /**
     * Initialize UI components
     */
    private void initializeComponents() {
        // Total contacts card
        totalLabel = new JLabel("0");
        JPanel totalCard = createStatCard(
            "Total Contacts",
            totalLabel,
            new Color(52, 152, 219), // Blue
            "👥"
        );
        add(totalCard);
        
        // Categories card
        categoryPanel = new JPanel();
        categoryPanel.setLayout(new BoxLayout(categoryPanel, BoxLayout.Y_AXIS));
        categoryPanel.setOpaque(false);
        
        JPanel categoriesCard = createStatCard(
            "Categories",
            categoryPanel,
            new Color(46, 204, 113), // Green
            "📂"
        );
        add(categoriesCard);
        
        // Deleted contacts card
        deletedLabel = new JLabel("0");
        JPanel deletedCard = createStatCard(
            "Deleted",
            deletedLabel,
            new Color(231, 76, 60), // Red
            "🗑️"
        );
        add(deletedCard);
    }
    
    /**
     * Create a statistics card
     * 
     * Cards are visual containers showing one metric
     * 
     * @param title Card title
     * @param contentComponent Component showing the value
     * @param accentColor Card accent color
     * @param icon Emoji icon
     * @return Styled panel
     */
    private JPanel createStatCard(String title, Component contentComponent, Color accentColor, String icon) {
        // Main card panel
        JPanel card = new JPanel();
        card.setLayout(new BorderLayout(10, 10));
        card.setPreferredSize(new Dimension(200, 80));
        card.setBackground(ThemeManager.getPrimaryBackground());
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(accentColor, 2),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        // Title label
        JLabel titleLabel = new JLabel(icon + " " + title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 12));
        titleLabel.setForeground(accentColor);
        card.add(titleLabel, BorderLayout.NORTH);
        
        // Content component (label or panel)
        if (contentComponent instanceof JLabel) {
            JLabel label = (JLabel) contentComponent;
            label.setFont(new Font("Arial", Font.BOLD, 24));
            label.setForeground(ThemeManager.getPrimaryText());
        }
        card.add(contentComponent, BorderLayout.CENTER);
        
        return card;
    }
    
    /**
     * Update statistics from database
     * 
     * Fetches fresh data and updates display
     * Call this after any contact changes
     */
    public void updateStatistics() {
        try {
            // Get all active contacts
            List<Contact> contacts = contactService.getAllContacts();
            
            // Update total count
            totalLabel.setText(String.valueOf(contacts.size()));
            
            // Calculate category distribution
            Map<String, Integer> categoryCounts = new HashMap<>();
            for (Contact contact : contacts) {
                String category = contact.getCategory();
                if (category == null || category.trim().isEmpty()) {
                    category = "Uncategorized";
                }
                categoryCounts.put(category, categoryCounts.getOrDefault(category, 0) + 1);
            }
            
            // Update category panel
            categoryPanel.removeAll();
            for (Map.Entry<String, Integer> entry : categoryCounts.entrySet()) {
                JLabel catLabel = new JLabel(entry.getKey() + ": " + entry.getValue());
                catLabel.setFont(new Font("Arial", Font.PLAIN, 11));
                catLabel.setForeground(ThemeManager.getPrimaryText());
                categoryPanel.add(catLabel);
            }
            
            // Get deleted count
            List<Contact> deleted = contactService.getDeletedContacts();
            deletedLabel.setText(String.valueOf(deleted.size()));
            
            // Refresh display
            revalidate();
            repaint();
            
        } catch (Exception e) {
            System.err.println("Error updating statistics: " + e.getMessage());
        }
    }
}
```

### Statistics Panel Explanation

#### **How Statistics Are Fetched from Service Layer**

**Data flow:**
```
User opens app / refreshes
    ↓
StatisticsPanel.updateStatistics()
    ↓
contactService.getAllContacts()
    ↓
DAO queries database
    ↓
Returns List<Contact>
    ↓
Panel calculates statistics (count, categories)
    ↓
Updates labels
    ↓
Display refreshes
```

**Why fetch from Service, not DAO:**
```java
// WRONG: Panel talks to DAO
public class StatisticsPanel {
    private ContactDAO dao = new ContactDAOImpl();
    
    public void update() {
        List<Contact> contacts = dao.getAll(); // Bypasses business logic
    }
}

// RIGHT: Panel talks to Service
public class StatisticsPanel {
    private ContactService service = new ContactService();
    
    public void update() {
        List<Contact> contacts = service.getAllContacts(); // Goes through validation
    }
}
```

**Benefits:**
- Service applies business rules
- Service handles errors gracefully
- Service can add caching without changing UI
- Consistent with rest of application

#### **Why Dashboard Improves UX**

**Without dashboard:**
```
┌────────────────────────────────┐
│ Contact Table (just rows)     │
│ John Smith                     │
│ Jane Doe                       │
│ ...                            │
└────────────────────────────────┘

Question: How many contacts do I have?
Answer: Scroll through entire table and count
```

**With dashboard:**
```
┌──────────┬──────────┬──────────┐
│ Total: 150│Family: 45│Deleted: 3│
└──────────┴──────────┴──────────┘
┌────────────────────────────────┐
│ Contact Table                  │
│ ...                            │
└────────────────────────────────┘

Question: How many contacts do I have?
Answer: Instant - see 150 at top
```

**UX benefits:**
- ✅ **At-a-glance overview**: No need to analyze table
- ✅ **Quick insights**: See category distribution immediately
- ✅ **Awareness**: Know about deleted contacts
- ✅ **Professional**: Modern apps show dashboards
- ✅ **Visual interest**: Color-coded cards look better than plain table

#### **How Separation of Panel Improves Modularity**

**Monolithic approach (bad):**
```java
public class ContactDashboardUI extends JFrame {
    private void createUI() {
        // 2000 lines of mixed code
        // Table code
        // Statistics code
        // Toolbar code
        // Preview code
        // Everything tangled together
    }
}
```

**Modular approach (good):**
```java
public class ContactDashboardUI extends JFrame {
    private void createUI() {
        JPanel toolbar = new ToolbarPanel();
        JPanel stats = new StatisticsPanel();
        JTable table = createTable();
        JPanel preview = new PreviewPanel();
        
        // Compose panels
        add(stats, BorderLayout.NORTH);
        add(table, BorderLayout.CENTER);
        add(preview, BorderLayout.EAST);
    }
}
```

**Benefits:**
- ✅ **Reusability**: StatisticsPanel can be used in other windows
- ✅ **Testability**: Test StatisticsPanel independently
- ✅ **Maintainability**: Bug in stats? Only edit StatisticsPanel.java
- ✅ **Team collaboration**: Different developers work on different panels
- ✅ **Clarity**: Each panel has single responsibility

**Real-world example:**
```java
// Reuse StatisticsPanel in different contexts

// Main dashboard
mainWindow.add(new StatisticsPanel());

// Print preview
printDialog.add(new StatisticsPanel());

// Export report
reportWindow.add(new StatisticsPanel());

// All show same data, consistent appearance
```

---

## 👁️ PHASE 15 — Contact Preview Side Panel

### New File: `ui/PreviewPanel.java`

```java
package ui;

import model.Contact;

import javax.swing.*;
import java.awt.*;
import java.time.format.DateTimeFormatter;

/**
 * PreviewPanel - Contact detail preview
 * 
 * Purpose: Show selected contact details in side panel
 * 
 * Features:
 * - Displays all contact fields
 * - Updates when table selection changes
 * - Clean, readable layout
 * - Handles null values gracefully
 * 
 * Integration: Listens to JTable selection events
 */
public class PreviewPanel extends JPanel {
    
    // UI Components
    private JLabel nameLabel;
    private JLabel phoneLabel;
    private JLabel emailLabel;
    private JLabel addressLabel;
    private JLabel categoryLabel;
    private JLabel notesArea;
    private JLabel createdLabel;
    private JLabel updatedLabel;
    
    // Date formatter
    private static final DateTimeFormatter DATE_FORMATTER = 
        DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
    
    /**
     * Constructor
     */
    public PreviewPanel() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(300, 600));
        setBackground(ThemeManager.getSecondaryBackground());
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeManager.getSecondaryText(), 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        initializeComponents();
        showEmptyState();
    }
    
    /**
     * Initialize UI components
     */
    private void initializeComponents() {
        // Main content panel
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);
        
        // Title
        JLabel titleLabel = new JLabel("Contact Details");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(ThemeManager.getPrimaryText());
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(titleLabel);
        contentPanel.add(Box.createVerticalStrut(20));
        
        // Name field
        nameLabel = createValueLabel("", 16, Font.BOLD);
        contentPanel.add(createFieldSection("Name", nameLabel));
        contentPanel.add(Box.createVerticalStrut(15));
        
        // Phone field
        phoneLabel = createValueLabel("", 14, Font.PLAIN);
        contentPanel.add(createFieldSection("Phone", phoneLabel));
        contentPanel.add(Box.createVerticalStrut(15));
        
        // Email field
        emailLabel = createValueLabel("", 14, Font.PLAIN);
        contentPanel.add(createFieldSection("Email", emailLabel));
        contentPanel.add(Box.createVerticalStrut(15));
        
        // Address field
        addressLabel = createValueLabel("", 14, Font.PLAIN);
        contentPanel.add(createFieldSection("Address", addressLabel));
        contentPanel.add(Box.createVerticalStrut(15));
        
        // Category field
        categoryLabel = createValueLabel("", 14, Font.PLAIN);
        contentPanel.add(createFieldSection("Category", categoryLabel));
        contentPanel.add(Box.createVerticalStrut(15));
        
        // Notes field
        notesArea = createValueLabel("", 12, Font.ITALIC);
        contentPanel.add(createFieldSection("Notes", notesArea));
        contentPanel.add(Box.createVerticalStrut(20));
        
        // Separator
        JSeparator separator = new JSeparator();
        separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        contentPanel.add(separator);
        contentPanel.add(Box.createVerticalStrut(15));
        
        // Created timestamp
        createdLabel = createValueLabel("", 11, Font.PLAIN);
        createdLabel.setForeground(ThemeManager.getSecondaryText());
        contentPanel.add(createFieldSection("Created", createdLabel));
        contentPanel.add(Box.createVerticalStrut(10));
        
        // Updated timestamp
        updatedLabel = createValueLabel("", 11, Font.PLAIN);
        updatedLabel.setForeground(ThemeManager.getSecondaryText());
        contentPanel.add(createFieldSection("Updated", updatedLabel));
        
        // Wrap in scroll pane (for long addresses/notes)
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setOpaque(false);
        
        add(scrollPane, BorderLayout.CENTER);
    }
    
    /**
     * Create a field section (label + value)
     * 
     * @param fieldName Field label
     * @param valueLabel Label showing the value
     */
    private JPanel createFieldSection(String fieldName, JLabel valueLabel) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Field name label (bold, smaller)
        JLabel nameLabel = new JLabel(fieldName);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 11));
        nameLabel.setForeground(ThemeManager.getSecondaryText());
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(nameLabel);
        panel.add(Box.createVerticalStrut(5));
        
        // Value label
        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(valueLabel);
        
        return panel;
    }
    
    /**
     * Create a value label with specified font
     */
    private JLabel createValueLabel(String text, int fontSize, int fontStyle) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", fontStyle, fontSize));
        label.setForeground(ThemeManager.getPrimaryText());
        return label;
    }
    
    /**
     * Update preview with contact details
     * 
     * This is the KEY method - called when table selection changes
     * 
     * @param contact Contact to display (null to show empty state)
     */
    public void updatePreview(Contact contact) {
        if (contact == null) {
            showEmptyState();
            return;
        }
        
        // Update all fields
        nameLabel.setText(contact.getName());
        phoneLabel.setText(contact.getPhone());
        
        // Handle optional fields (show "N/A" if null)
        emailLabel.setText(contact.getEmail() != null && !contact.getEmail().isEmpty()
            ? contact.getEmail()
            : "N/A");
        
        addressLabel.setText(contact.getAddress() != null && !contact.getAddress().isEmpty()
            ? contact.getAddress()
            : "N/A");
        
        categoryLabel.setText(contact.getCategory() != null && !contact.getCategory().isEmpty()
            ? contact.getCategory()
            : "Uncategorized");
        
        notesArea.setText(contact.getNotes() != null && !contact.getNotes().isEmpty()
            ? contact.getNotes()
            : "No notes");
        
        // Format timestamps
        if (contact.getCreatedAt() != null) {
            createdLabel.setText(contact.getCreatedAt().format(DATE_FORMATTER));
        } else {
            createdLabel.setText("N/A");
        }
        
        if (contact.getUpdatedAt() != null) {
            updatedLabel.setText(contact.getUpdatedAt().format(DATE_FORMATTER));
        } else {
            updatedLabel.setText("N/A");
        }
        
        // Repaint to show changes
        revalidate();
        repaint();
    }
    
    /**
     * Show empty state (no contact selected)
     */
    private void showEmptyState() {
        nameLabel.setText("No contact selected");
        phoneLabel.setText("");
        emailLabel.setText("");
        addressLabel.setText("");
        categoryLabel.setText("");
        notesArea.setText("");
        createdLabel.setText("");
        updatedLabel.setText("");
        
        revalidate();
        repaint();
    }
}
```

### Integration in ContactDashboardUI:

```java
// In ContactDashboardUI class, add field:
private PreviewPanel previewPanel;

// In initializeUI(), after creating centerPanel:
private void initializeUI() {
    // ... existing code ...
    
    // Create preview panel
    previewPanel = new PreviewPanel();
    
    // Add to frame (right side)
    add(previewPanel, BorderLayout.EAST);
    
    // Add selection listener to table
    contactTable.getSelectionModel().addListSelectionListener(e -> {
        if (!e.getValueIsAdjusting()) { // Ignore intermediate events
            int selectedRow = contactTable.getSelectedRow();
            if (selectedRow >= 0) {
                // Get contact ID from selected row
                int contactId = (int) tableModel.getValueAt(selectedRow, 0);
                
                // Fetch full contact details
                try {
                    Contact contact = contactService.getContactById(contactId);
                    previewPanel.updatePreview(contact);
                } catch (Exception ex) {
                    System.err.println("Error loading contact: " + ex.getMessage());
                    previewPanel.updatePreview(null);
                }
            } else {
                // No selection
                previewPanel.updatePreview(null);
            }
        }
    });
}
```

### Preview Panel Explanation

#### **Why Preview Panel Improves Usability**

**Without preview:**
```
┌────────────────────────────────┐
│ Name        Phone       Email  │
│ John Smith  555-1234    ...    │  ← Truncated email
│ Jane Doe    555-5678    ...    │
└────────────────────────────────┘

To see full email: Double-click → Edit dialog → Cancel
(3 steps just to read data)
```

**With preview:**
```
┌──────────────────┬─────────────┐
│ Name      Phone  │ Details:    │
│ John → 555-1234  │ Name: John  │
│ Jane   555-5678  │ Phone: 555..│
                    │ Email: john@│
                    │ example.com │
                    │ Address: 123│
                    │ Main St...  │
└──────────────────┴─────────────┘

Click row → See all details immediately
(1 step)
```

**UX benefits:**
- ✅ **Instant access**: Click row, see full details
- ✅ **No truncation**: Preview shows complete text
- ✅ **Context**: See details while browsing list
- ✅ **Efficiency**: No need to open dialog just to read
- ✅ **Modern**: Standard pattern (email clients, file managers)

**Real-world analogies:**
- Email client: List + preview pane
- File manager: File list + details pane
- Music player: Song list + now playing
- IDE: File list + code preview

#### **How JTable Selection Listener Works**

**The mechanism:**

```java
// When user clicks table row:
contactTable.getSelectionModel().addListSelectionListener(e -> {
    // This code executes
});
```

**Step-by-step flow:**

1. **User clicks row in table**
```
User: *clicks row 5*
```

2. **Table selection model changes**
```java
ListSelectionModel model = contactTable.getSelectionModel();
// model.selectedIndex changes from -1 to 5
```

3. **Model fires ListSelectionEvent**
```java
ListSelectionEvent event = new ListSelectionEvent(model, ...);
// Event contains: source, first/last index changed
```

4. **All registered listeners receive event**
```java
listener.valueChanged(event);
// Our lambda executes
```

5. **Check if adjustment is finished**
```java
if (!e.getValueIsAdjusting()) {
    // User finished selection (mouse released, not dragging)
}
```

**Why check getValueIsAdjusting()?**

```java
// WITHOUT check:
contactTable.getSelectionModel().addListSelectionListener(e -> {
    loadContactDetails(); // Called MULTIPLE times during drag
});

// Drag from row 1 to row 5:
// Event fired: row 1 selected
// Event fired: row 2 selected
// Event fired: row 3 selected
// Event fired: row 4 selected
// Event fired: row 5 selected
// Result: 5 database queries! (slow, wasteful)

// WITH check:
contactTable.getSelectionModel().addListSelectionListener(e -> {
    if (!e.getValueIsAdjusting()) {
        loadContactDetails(); // Called ONCE when drag finishes
    }
});

// Drag from row 1 to row 5:
// Event fired: row 1 (isAdjusting=true, ignored)
// Event fired: row 2 (isAdjusting=true, ignored)
// Event fired: row 3 (isAdjusting=true, ignored)
// Event fired: row 4 (isAdjusting=true, ignored)
// Event fired: row 5 (isAdjusting=false, PROCESSED)
// Result: 1 database query (efficient)
```

6. **Get selected row index**
```java
int selectedRow = contactTable.getSelectedRow();
// Returns: 0-based index of selected row
// Returns: -1 if no selection
```

7. **Get contact ID from table**
```java
int contactId = (int) tableModel.getValueAt(selectedRow, 0);
// Column 0 = ID column
// Extract ID from table row
```

8. **Fetch full contact from database**
```java
Contact contact = contactService.getContactById(contactId);
// Service → DAO → Database
// Returns complete Contact object
```

9. **Update preview panel**
```java
previewPanel.updatePreview(contact);
// Preview refreshes display
```

#### **Why UI Must Not Contain SQL**

**WRONG approach (SQL in UI):**
```java
public class PreviewPanel extends JPanel {
    public void updatePreview(int contactId) {
        // SQL in UI - VERY BAD
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT * FROM contacts WHERE id = ?";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setInt(1, contactId);
            ResultSet rs = pst.executeQuery();
            
            if (rs.next()) {
                nameLabel.setText(rs.getString("name"));
                // Direct database access in UI
            }
        }
    }
}
```

**Problems:**
- ❌ **Mixed responsibilities**: UI knows about database
- ❌ **Tight coupling**: Cannot change database without changing UI
- ❌ **No validation**: Bypass business rules
- ❌ **Duplication**: SQL repeated in multiple UI components
- ❌ **Cannot test**: UI test requires database connection
- ❌ **Cannot reuse**: Desktop UI, web UI both duplicate SQL

**CORRECT approach (Service layer):**
```java
public class PreviewPanel extends JPanel {
    private ContactService service = new ContactService();
    
    public void updatePreview(int contactId) {
        // No SQL - clean separation
        Contact contact = service.getContactById(contactId);
        nameLabel.setText(contact.getName());
        // UI works with Contact objects only
    }
}
```

**Benefits:**
- ✅ **Separation of concerns**: UI for display, Service for logic
- ✅ **Loose coupling**: Change database without touching UI
- ✅ **Validation applied**: Service ensures business rules
- ✅ **No duplication**: SQL in one place (DAO)
- ✅ **Testable**: Mock service for UI tests
- ✅ **Reusable**: Desktop, web, mobile all use same Service

#### **How Data Flows from Service to UI**

**Complete flow:**

```
1. User clicks table row
    ↓
2. JTable fires ListSelectionEvent
    ↓
3. Listener receives event
    ↓
4. Get selected row index from table
    ↓
5. Extract contact ID from that row
    ↓
6. Call contactService.getContactById(id)
    ↓
7. Service calls dao.getById(id)
    ↓
8. DAO executes SQL query
    ↓
9. Database returns row
    ↓
10. DAO converts ResultSet → Contact object
    ↓
11. Service returns Contact to UI
    ↓
12. UI calls previewPanel.updatePreview(contact)
    ↓
13. Preview extracts fields from Contact object
    ↓
14. Preview updates labels
    ↓
15. User sees contact details
```

**Code trace:**
```java
// Step 1-3: User clicks row, listener fires
contactTable.getSelectionModel().addListSelectionListener(e -> {
    
    // Step 4: Get row index
    int selectedRow = contactTable.getSelectedRow();
    
    // Step 5: Extract contact ID
    int contactId = (int) tableModel.getValueAt(selectedRow, 0);
    
    // Step 6-11: Service layer (abstracts database)
    Contact contact = contactService.getContactById(contactId);
    
    // Step 12-15: Update preview
    previewPanel.updatePreview(contact);
});
```

#### **Line-by-Line: updatePreview() Method**

```java
public void updatePreview(Contact contact) {
```
- Public method - called from ContactDashboardUI
- Takes Contact object - never deals with database directly

```java
if (contact == null) {
    showEmptyState();
    return;
}
```
- Handle null case (no selection or error)
- Show empty state instead of crashing

```java
nameLabel.setText(contact.getName());
phoneLabel.setText(contact.getPhone());
```
- Direct mapping: Contact field → UI label
- Simple, readable, maintainable

```java
emailLabel.setText(contact.getEmail() != null && !contact.getEmail().isEmpty()
    ? contact.getEmail()
    : "N/A");
```
- Null handling: Show "N/A" if email is null or empty
- Better UX than showing blank space or "null"

```java
if (contact.getCreatedAt() != null) {
    createdLabel.setText(contact.getCreatedAt().format(DATE_FORMATTER));
}
```
- Format LocalDateTime to human-readable string
- "Feb 12, 2026 14:30" instead of "2026-02-12T14:30:00"

```java
revalidate();
repaint();
```
- Force visual update
- Ensures changes appear immediately

---

## ✅ Summary of PART 3

### What We Built

**PHASE 11 - JTable Integration**
- Integrated JTable with DefaultTableModel
- Implemented loadContacts() to populate table from Service
- Set up table columns, row height, sorting
- Explained Model-View separation
- Showed complete data flow: UI → Service → DAO → Database

**PHASE 12 - Toolbar + Safe Icon Loading**
- Created JToolBar with action buttons
- Implemented safe icon loading with getClass().getResource()
- Added icon scaling for consistent appearance
- Explained why file paths fail in JAR
- Demonstrated graceful fallback when icons missing

**PHASE 13 - Theme System**
- Built ThemeManager with Dark/Light modes
- Created carefully chosen color palettes
- Implemented theme application with UIManager + manual updates
- Explained contrast ratios for accessibility
- Showed how to style JTable headers manually

**PHASE 14 - Statistics Dashboard**
- Created StatisticsPanel with card-based layout
- Implemented live statistics (total, categories, deleted)
- Showed color-coded cards for visual interest
- Explained modular panel benefits

**PHASE 15 - Contact Preview Panel**
- Built PreviewPanel showing selected contact details
- Integrated with JTable selection listener
- Explained ListSelectionEvent handling
- Demonstrated clean data flow from Service to UI
- Showed proper null handling and formatting

### Complete UI Architecture

```
ContactDashboardUI (Main Window)
├── ThemeManager (Dark/Light modes)
├── JToolBar (Action buttons)
├── StatisticsPanel (Dashboard cards)
├── JTable + TableModel (Contact list)
└── PreviewPanel (Contact details)
```

### Key Principles Demonstrated

- ✅ **No SQL in UI**: All database access through Service layer
- ✅ **Model-View separation**: TableModel holds data, JTable displays
- ✅ **Modular components**: Each panel is self-contained
- ✅ **Theme consistency**: Centralized color management
- ✅ **Safe resource loading**: getResource() works in JAR
- ✅ **Accessibility**: High contrast colors, readable sizes
- ✅ **Professional appearance**: Modern dark/light themes

### What's Next

PART 4 will add functionality:
- Contact form dialogs (Add/Edit)
- Delete confirmation and soft delete
- Search functionality with filters
- Recycle Bin dialog
- CSV import/export
- Complete event handling and validation

---

**README_PART_3 Complete. Ready for PART 4.**
