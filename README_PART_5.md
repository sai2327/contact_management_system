# CONTACT MANAGEMENT SYSTEM - COMPLETE RECONSTRUCTION GUIDE

## PART 5: Advanced Polish, Logging, Persistence, Exception Strategy & Final Integration

---

## Introduction to Part 5

**What Part 5 Builds:**

Part 5 is the capstone of the entire Contact Management System. It elevates the application from a working prototype to a **production-quality desktop application** by introducing:

- **Toast Notification System** — modern, non-blocking feedback replacing clunky dialogs
- **Animated Row Highlighting** — visual feedback confirming user actions in the table
- **Column Visibility Toggle** — power-user feature for customizing the data view
- **Context Menu (Right-Click Actions)** — desktop-standard interaction for quick operations
- **SwingWorker Background Processing** — preventing UI freezes during heavy I/O
- **Settings Persistence** — remembering user preferences across sessions
- **Logging System** — professional diagnostics replacing `System.out.println`
- **Custom Exception Handling** — structured, layered error management
- **Performance & Optimization** — strategies for scaling the system
- **Final System Integration** — complete architectural understanding and data flow

**Why Advanced UI Polish Matters:**

Users judge software quality by what they *see and feel*. A system that works perfectly but provides jarring `JOptionPane` popups, freezes during CSV imports, and forgets user preferences every restart will be perceived as amateur. Toast notifications, smooth animations, and persistent settings are what separate student projects from professional software.

**Why Production Systems Require Logging & Persistence:**

In production, you cannot attach a debugger to a customer's machine. Logs are your **only window** into what happened when something went wrong at 3 AM. Similarly, forcing users to reconfigure their theme and window size every launch creates unnecessary friction. The Preferences API solves this elegantly without file I/O complexity.

**Why Background Processing Improves Responsiveness:**

Swing is single-threaded by design — all UI updates happen on the Event Dispatch Thread (EDT). If you run a 5-second CSV import on the EDT, the entire application freezes: buttons stop responding, the window cannot be moved, and the OS may flag it as "Not Responding." SwingWorker moves heavy work off the EDT while safely publishing progress updates back to it.

**Why Final Integration Understanding Is Critical:**

Building each component in isolation is not enough. A professional developer must understand how **data flows end-to-end**: from a button click, through validation, into the service layer, down to JDBC, into MySQL, and back up through each layer to update the UI. This mental model is what enables you to debug, extend, and refactor with confidence.

**How Part 5 Connects to Previous Parts:**

```
Part 1 (Foundation)        → Database + Model + DAO Interface
Part 2 (Backend)           → DAO Implementation + Service + Validation + Main
Part 3 (Basic UI)          → JTable + Toolbar + Theme + Statistics + Preview
Part 4 (Advanced Features) → Dialogs + Search + CSV + Recycle Bin
Part 5 (Polish & Mastery)  → Toast + Animation + SwingWorker + Logging + Integration
```

Every feature in Part 5 enhances or wraps around components built in Parts 1–4. Nothing here works in isolation — it all integrates into the existing architecture.

---

## PHASE 21 — Toast Notification System

### Why We Need Toast Notifications

Traditional Swing applications use `JOptionPane.showMessageDialog()` for every piece of feedback. This creates a **modal dialog** that:
- Blocks the entire application until the user clicks "OK"
- Breaks workflow by requiring an extra click for routine operations
- Feels dated and aggressive for success messages

Toast notifications solve all of these problems. They are **non-blocking, floating messages** that appear briefly and auto-dismiss — exactly like notifications in Android, macOS, and modern web applications.

### Complete Implementation: Toast.java

```java
package ui;

import javax.swing.*;
import java.awt.*;

/**
 * Toast Notification System
 * 
 * Non-blocking floating notification that auto-disappears.
 * Replaces JOptionPane for success messages.
 * Supports fade in/out animation where platform allows.
 * 
 * Usage: Toast.show(parentComponent, "✔ Success message!");
 */
public class Toast {

    // Default display duration in milliseconds (2 seconds)
    // This is the time the toast remains fully visible BEFORE fade-out begins
    private static final int DEFAULT_DURATION = 2000;

    /**
     * Show a toast with default duration.
     * This is the most commonly used overload.
     * 
     * @param parent - the component to anchor the toast to (usually the JFrame)
     * @param message - the text to display in the toast
     */
    public static void show(Component parent, String message) {
        // Delegate to the full method with default duration
        show(parent, message, DEFAULT_DURATION);
    }

    /**
     * Show a toast with custom duration.
     * 
     * @param parent - the component to anchor the toast to
     * @param message - the text to display
     * @param duration - how long (ms) the toast stays visible before fading
     */
    public static void show(Component parent, String message, int duration) {
        // CRITICAL: All Swing UI operations MUST run on the EDT
        // If this method is called from a background thread (e.g., SwingWorker),
        // invokeLater ensures thread safety
        SwingUtilities.invokeLater(() -> {
            // Step 1: Find the parent window
            // We need the parent window to:
            //   a) Position the toast relative to it
            //   b) Set it as the owner (so toast moves with the window)
            Window parentWindow;
            if (parent instanceof Window) {
                // If the parent IS a window (JFrame, JDialog), use it directly
                parentWindow = (Window) parent;
            } else if (parent != null) {
                // If the parent is a component inside a window, find its ancestor window
                parentWindow = SwingUtilities.getWindowAncestor(parent);
            } else {
                // No parent provided — will position on screen instead
                parentWindow = null;
            }

            // Step 2: Create a JWindow for the toast
            // JWindow is a lightweight, undecorated window — perfect for floating overlays
            // Unlike JDialog, it has no title bar, no close button, no border
            // Setting parentWindow as owner ensures it stays on top of the parent
            JWindow toast = new JWindow(parentWindow);
            toast.setAlwaysOnTop(true); // Ensure toast is visible above all windows

            // Step 3: Build the toast panel
            // The panel provides background color, border, and padding
            JPanel panel = new JPanel(new BorderLayout());
            
            // Use the application's accent color as the toast background
            // This creates visual consistency with the rest of the UI
            Color bg = UITheme.getAccentColor();
            panel.setBackground(bg);
            
            // Compound border: thin darker border + padding
            // The darker border provides definition against any background
            // The padding (10px top/bottom, 20px left/right) ensures text isn't cramped
            panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(bg.darker(), 1),    // Outer: 1px darker border
                BorderFactory.createEmptyBorder(10, 20, 10, 20)    // Inner: padding
            ));

            // Step 4: Create the message label
            // White text on accent-colored background ensures readability
            JLabel label = new JLabel(message);
            label.setFont(new Font("Segoe UI", Font.BOLD, 13));
            label.setForeground(Color.WHITE); // White text contrasts with accent color
            panel.add(label, BorderLayout.CENTER);

            // Step 5: Add panel to window and pack to fit content
            toast.add(panel);
            toast.pack(); // Resize window to exactly fit the panel's preferred size

            // Step 6: Position the toast at the bottom-right of the parent window
            // Bottom-right is the standard position for notifications (Windows, macOS, web)
            // This avoids obscuring the main content area
            try {
                if (parentWindow != null && parentWindow.isVisible()) {
                    // Get the parent window's absolute position on screen
                    Point loc = parentWindow.getLocationOnScreen();
                    Dimension ps = parentWindow.getSize();  // Parent size
                    Dimension ts = toast.getSize();          // Toast size
                    
                    // Calculate position: bottom-right corner with 20px margin
                    toast.setLocation(
                        loc.x + ps.width - ts.width - 20,   // Right edge minus toast width minus margin
                        loc.y + ps.height - ts.height - 40   // Bottom edge minus toast height minus margin
                    );
                } else {
                    // No visible parent — position relative to screen
                    positionOnScreen(toast);
                }
            } catch (Exception e) {
                // getLocationOnScreen() can throw if component is not showing
                positionOnScreen(toast);
            }

            // Step 7: Check if fade animation is supported
            // Window opacity (setOpacity) requires Java 7+ and a supported graphics environment
            // Some headless or remote desktop setups don't support transparency
            boolean canFade = false;
            try {
                toast.setOpacity(0.0f); // Start fully transparent
                canFade = true;          // If no exception, fade is supported
            } catch (Exception e) {
                // UnsupportedOperationException or IllegalComponentStateException
                // Gracefully fall back to non-animated behavior
            }

            // Step 8: Show the toast
            toast.setVisible(true);

            if (canFade) {
                // ANIMATED PATH: Fade in → hold → fade out
                
                // Fade in: increase opacity from 0 to 1 in steps
                final float[] opacity = {0f}; // Array trick to modify inside lambda
                Timer fadeIn = new Timer(25, null); // Fire every 25ms
                fadeIn.addActionListener(e -> {
                    opacity[0] = Math.min(1.0f, opacity[0] + 0.15f); // Increase by 15% each tick
                    try {
                        toast.setOpacity(opacity[0]);
                    } catch (Exception ex) {
                        fadeIn.stop(); // Stop if opacity fails mid-animation
                    }
                    if (opacity[0] >= 1.0f) {
                        fadeIn.stop(); // Fully visible — stop fading in
                        scheduleFadeOut(toast, duration); // Schedule the fade-out after the hold period
                    }
                });
                fadeIn.start();
            } else {
                // NON-ANIMATED PATH: Simply show for duration, then dispose
                Timer closeTimer = new Timer(duration, e -> toast.dispose());
                closeTimer.setRepeats(false); // Only fire once
                closeTimer.start();
            }
        });
    }

    /**
     * Schedule a fade-out animation after a delay.
     * 
     * @param toast - the JWindow to fade out
     * @param delay - milliseconds to wait before starting fade-out
     */
    private static void scheduleFadeOut(JWindow toast, int delay) {
        // Wait timer: holds the toast at full opacity for the specified duration
        Timer waitTimer = new Timer(delay, e -> {
            // After the delay, start fading out
            final float[] opacity = {1.0f}; // Start fully opaque
            Timer fadeOut = new Timer(25, null); // Fire every 25ms
            fadeOut.addActionListener(e2 -> {
                opacity[0] = Math.max(0f, opacity[0] - 0.15f); // Decrease by 15% each tick
                try {
                    toast.setOpacity(opacity[0]);
                } catch (Exception ex) {
                    // If opacity fails, just dispose immediately
                    toast.dispose();
                    fadeOut.stop();
                    return;
                }
                if (opacity[0] <= 0f) {
                    toast.dispose();  // Fully transparent — destroy the window
                    fadeOut.stop();   // Stop the timer to prevent memory leak
                }
            });
            fadeOut.start();
        });
        waitTimer.setRepeats(false); // Only fire once after the delay
        waitTimer.start();
    }

    /**
     * Position toast at bottom-right of screen when no parent window is available.
     * Falls back to screen dimensions from Toolkit.
     * 
     * @param toast - the JWindow to position
     */
    private static void positionOnScreen(JWindow toast) {
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        toast.setLocation(
            screen.width - toast.getWidth() - 20,    // 20px from right edge
            screen.height - toast.getHeight() - 80    // 80px from bottom (above taskbar)
        );
    }
}
```

### Line-by-Line Explanation

**Why JOptionPane Is Not Modern:**

`JOptionPane.showMessageDialog()` creates a **modal dialog** — a window that steals focus and blocks all interaction with the parent application until the user clicks OK. For error messages this is appropriate (you *want* the user to acknowledge a problem). But for success confirmations like "Contact saved!" or "CSV exported!", a modal dialog is:

1. **Disruptive** — it interrupts the user's workflow
2. **Redundant** — the user already knows they just clicked "Save"
3. **Annoying** — after the 10th time clicking OK, users resent the application

Toast notifications provide **passive feedback**. The user sees the confirmation without any required action, and the toast disappears on its own.

**How Timer Works in Swing:**

`javax.swing.Timer` is Swing's built-in mechanism for scheduling repeated or one-shot actions **on the EDT**. This is critical because:

```
Timer fires ActionEvent → ActionListener runs → runs on EDT → safe to modify UI
```

In our toast, we use Timer for three purposes:

1. **Fade-in Timer** (fired every 25ms): Gradually increases `JWindow.setOpacity()` from 0.0 to 1.0 in increments of 0.15. At 25ms intervals with 0.15 steps, the fade-in takes approximately 175ms (7 ticks × 25ms). This is fast enough to feel snappy but slow enough to be perceived as a smooth transition.

2. **Wait Timer** (fired once after `duration` ms): Holds the toast at full opacity for the specified duration (default 2000ms). After this fires, it starts the fade-out timer.

3. **Fade-out Timer** (fired every 25ms): Gradually decreases opacity from 1.0 to 0.0, then calls `toast.dispose()` to destroy the window and free resources.

**Why JWindow Instead of JPanel Overlay:**

We use `JWindow` (a lightweight, undecorated window) instead of a `JPanel` overlay because:

- `JWindow` can extend **beyond the parent frame's bounds** — essential for positioning at the bottom-right edge
- `JWindow` supports `setOpacity()` for native transparency — `JPanel` cannot make its background truly transparent on all platforms
- `JWindow` has no title bar, close button, or decorations — it looks like a floating notification, not a dialog
- Setting `setAlwaysOnTop(true)` ensures the toast is visible even if other windows overlap

**Why the Array Trick `final float[] opacity = {0f}`:**

Java lambdas and anonymous inner classes can only access **effectively final** local variables. Since we need to modify the opacity value inside the Timer's `ActionListener`, we wrap it in a single-element array. The array reference is final, but its contents can be mutated. This is a common Swing/Java pattern for mutable state inside callbacks.

**Integration with Main Frame:**

Toast is called throughout `ContactUI.java` after successful operations:

```java
// After delete
Toast.show(this, "✔ Contact moved to Recycle Bin!");

// After theme toggle
Toast.show(this, "🎨 Switched to Dark mode");

// After clipboard copy
Toast.show(this, "✔ Number copied!");

// After export
Toast.show(this, "✔ Contacts exported successfully!");
```

The `this` reference is the `ContactUI` JFrame. Toast uses it to find the parent window and position the notification at the bottom-right corner of the application.

---

## PHASE 22 — Animated Row Highlight

### Why Animated Row Highlight Matters

When a user adds a new contact, edits an existing one, or restores from the Recycle Bin, the JTable reloads all data. After reload, the table looks exactly the same as before — the user has **no visual confirmation** that their action affected a specific row. This is especially problematic when the table has many rows and the affected row might not be visible without scrolling.

Animated row highlighting solves this by:

1. **Scrolling** the affected row into view
2. **Selecting** the row so the user can see which record was affected
3. **Temporarily coloring** the row with the accent color for 1.5 seconds
4. **Fading** back to the normal row color

This creates an unmistakable visual link between the user's action and the affected data.

### Complete Code Updates in ContactUI.java

The row highlight system requires three pieces working together: **instance fields** for tracking the highlighted row, a **custom renderer** that applies the highlight color, and the **`highlightRow()` method** that triggers the animation.

#### Instance Fields (declared in ContactUI class body)

```java
// ===== Row Highlight =====
// These fields track which row is currently highlighted and what color to use.
// They are read by the custom cell renderer during painting.
private int highlightedViewRow = -1;    // -1 means no row is highlighted
private Color highlightColor = null;     // null means use normal row color
```

**Why these are instance fields and not local variables:**

The custom renderer is called by Swing's painting system, which happens asynchronously. When `contactTable.repaint()` is called, Swing schedules a repaint on the EDT. The renderer needs to access `highlightedViewRow` and `highlightColor` at paint time, which may be a different method invocation than when `highlightRow()` was called. Instance fields persist across method calls, making this communication possible.

#### Custom Cell Renderer (inside `createTablePanel()`)

```java
// Custom renderer for row highlighting + alternating rows
// This renderer is called for EVERY cell in the table during painting
DefaultTableCellRenderer customRenderer = new DefaultTableCellRenderer() {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
        
        // Call super to get default rendering (text, alignment, etc.)
        Component c = super.getTableCellRendererComponent(
            table, value, isSelected, hasFocus, row, column);
        
        // Priority 1: If this row is the highlighted row AND not selected by user
        // The highlight takes priority over alternating colors but NOT over selection
        if (!isSelected && row == highlightedViewRow && highlightColor != null) {
            c.setBackground(highlightColor);        // Apply highlight color
            c.setForeground(UITheme.getForeground()); // Keep text readable
        }
        // Priority 2: Normal alternating row colors (zebra stripes)
        else if (!isSelected) {
            // Even rows get the base background, odd rows get a slightly different shade
            c.setBackground(row % 2 == 0 ? UITheme.getBackground() : getAlternateRowColor());
            c.setForeground(UITheme.getForeground());
        }
        // Priority 3: If row IS selected, Swing's default selection colors are used
        // (We don't override isSelected behavior — Swing handles it)
        
        // Add left padding to all cells for visual comfort
        ((JLabel) c).setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
        
        return c;
    }
};

// Apply this renderer to both Object and Integer column types
contactTable.setDefaultRenderer(Object.class, customRenderer);
contactTable.setDefaultRenderer(Integer.class, customRenderer);
```

**Why the renderer must be lightweight:**

Swing's rendering system uses the **flyweight pattern** — a single renderer instance is reused for every cell in the table. During a full table repaint, the renderer's `getTableCellRendererComponent()` method is called **once per visible cell**. For a table with 6 columns and 20 visible rows, that's 120 calls per repaint. Each call must:

- Complete in microseconds (not milliseconds)
- Not allocate new objects unnecessarily
- Not perform I/O or database queries

Our renderer only reads two instance fields (`highlightedViewRow`, `highlightColor`) and sets background/foreground colors — all O(1) operations. This keeps rendering fast even for large tables.

#### The `highlightRow()` Method

```java
/**
 * Briefly highlight a row with accent color after add/edit/restore.
 * 
 * Flow:
 * 1. Convert model row index to view row index (accounts for sorting)
 * 2. Select and scroll to the row
 * 3. Set highlight color (accent with alpha transparency)
 * 4. Repaint table to apply highlight via custom renderer
 * 5. Start a Timer to clear the highlight after 1.5 seconds
 * 
 * @param modelRow - the row index in the table MODEL (not the view)
 */
private void highlightRow(int modelRow) {
    try {
        // Convert model row to view row
        // This is CRITICAL because the table might be sorted differently than the model.
        // If the user sorted by Name (column 1), model row 0 might appear as view row 15.
        // We must highlight the row where the user SEES it, not where it is in the model.
        int viewRow = contactTable.convertRowIndexToView(modelRow);
        if (viewRow < 0) return; // Row not visible (shouldn't happen, but defensive check)

        // Select the row — gives it the selection highlight color
        contactTable.setRowSelectionInterval(viewRow, viewRow);
        
        // Scroll to make the row visible if it's outside the viewport
        // getCellRect returns the rectangle of the cell at (viewRow, 0)
        // scrollRectToVisible ensures that rectangle is in the visible viewport
        contactTable.scrollRectToVisible(contactTable.getCellRect(viewRow, 0, true));

        // Create a semi-transparent version of the accent color
        // Alpha = 100 (out of 255) makes it visually distinct but not overwhelming
        Color accent = UITheme.getAccentColor();
        highlightColor = new Color(
            accent.getRed(), accent.getGreen(), accent.getBlue(), 100 // 100 = ~40% opacity
        );
        
        // Store the view row index for the renderer to check
        highlightedViewRow = viewRow;
        
        // Trigger a repaint — the custom renderer will see highlightedViewRow and apply the color
        contactTable.repaint();

        // Schedule highlight removal after 1.5 seconds
        Timer timer = new Timer(1500, e -> {
            highlightedViewRow = -1;   // Reset — no row is highlighted
            highlightColor = null;      // Reset — use normal colors
            contactTable.repaint();     // Repaint to restore normal appearance
        });
        timer.setRepeats(false); // Only fire once — we don't want repeating cleanup
        timer.start();
        
    } catch (Exception e) {
        // Silent fail — row highlight is cosmetic
        // We NEVER want a highlight failure to prevent the actual add/edit from completing
    }
}
```

#### How `highlightRow()` Is Called After Operations

After adding a new contact:

```java
private void openAddDialog() {
    ContactFormDialog dialog = new ContactFormDialog(this, contactService);
    dialog.setVisible(true);

    if (dialog.isSaveSuccessful()) {
        loadAllContacts();         // Reload table data
        refreshStatistics();       // Update count displays
        updateStatusBar();
        updateEmptyState();

        // Find the newest contact (highest ID) and highlight it
        if (tableModel.getRowCount() > 0) {
            int maxId = -1;
            int maxModelRow = -1;
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                int id = (int) tableModel.getValueAt(i, 0);
                if (id > maxId) {
                    maxId = id;
                    maxModelRow = i;
                }
            }
            if (maxModelRow >= 0) {
                highlightRow(maxModelRow); // Highlight the new contact's row
            }
        }
    }
}
```

After editing an existing contact:

```java
private void openEditDialog() {
    // ... (validation omitted for brevity) ...
    
    ContactFormDialog dialog = new ContactFormDialog(this, contactService, contact);
    dialog.setVisible(true);

    if (dialog.isSaveSuccessful()) {
        int editedId = selectedContactId;   // Remember which contact was edited
        loadAllContacts();                   // Reload table data
        refreshStatistics();
        updateStatusBar();

        // Find the edited contact's row and highlight it
        int modelRow = findModelRowById(editedId);
        if (modelRow >= 0) {
            highlightRow(modelRow);
            // Also update the preview panel with the new data
            Contact updated = contactService.getById(editedId);
            if (updated != null) previewPanel.showContact(updated);
        }
    }
}
```

**Helper method to find a row by contact ID:**

```java
/**
 * Find a model row index by contact ID.
 * Scans the table model linearly. For tables with <10,000 rows this is instant.
 * 
 * @param id - the contact's database ID
 * @return model row index, or -1 if not found
 */
private int findModelRowById(int id) {
    for (int i = 0; i < tableModel.getRowCount(); i++) {
        if ((int) tableModel.getValueAt(i, 0) == id) return i;
    }
    return -1;
}
```

### How Table Repaint Works

When `contactTable.repaint()` is called:

1. Swing adds a **repaint request** to the EDT queue (it doesn't repaint immediately)
2. On the next EDT cycle, Swing's `RepaintManager` processes the request
3. It calls `paintComponent()` on the `JTable`
4. `JTable.paintComponent()` iterates over all **visible** cells
5. For each cell, it calls our custom renderer's `getTableCellRendererComponent()`
6. The renderer checks `highlightedViewRow` and applies the appropriate background color
7. The returned `Component` is used as a "stamp" — its appearance is painted onto the table's graphics context

This flyweight rendering system means no extra components are created. The same renderer instance is configured and "stamped" for each cell, making it extremely memory-efficient.

**Conceptual Note on Smooth Fade:**

A true smooth fade (gradually transitioning from highlight color to normal color) would require a Timer that decreases the alpha value in steps over ~500ms (e.g., from alpha=100 down to alpha=0 in 10 steps at 50ms intervals). This is straightforward to implement:

```java
// Conceptual fade implementation (not in current code)
Timer fadeTimer = new Timer(50, null);
final int[] alpha = {100};
fadeTimer.addActionListener(e -> {
    alpha[0] -= 10;
    if (alpha[0] <= 0) {
        highlightedViewRow = -1;
        highlightColor = null;
        fadeTimer.stop();
    } else {
        Color accent = UITheme.getAccentColor();
        highlightColor = new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), alpha[0]);
    }
    contactTable.repaint();
});
fadeTimer.start();
```

We use the simpler "highlight then clear" approach because it's reliable, easy to understand, and achieves the primary goal of visual feedback. Adding smooth fade is a refinement that can be introduced later.

---

## PHASE 23 — Column Visibility Toggle

### Why Column Visibility Toggle Matters

Different users need different information. A user making phone calls only needs Name and Number. A user managing categories needs Name, Category, and Email. Power users want everything visible. Column visibility toggle lets users **customize their view** without changing the data.

This is a feature found in every professional data management application (Excel, Outlook, Jira, database GUIs).

### Complete Implementation

The column visibility system has three components:

1. **View Menu with JCheckBoxMenuItems** — the user-facing toggle controls
2. **Saved column references** — preserving `TableColumn` objects so they can be re-added
3. **`updateColumnVisibility()` method** — the engine that rebuilds the column model

#### Instance Fields

```java
// ===== Column Visibility =====
// savedColumns: Maps column name → TableColumn object
// We MUST save references to TableColumn objects because once removed from
// the column model, the only way to re-add them is if we kept a reference.
// LinkedHashMap preserves insertion order, which matches the original column order.
private LinkedHashMap<String, TableColumn> savedColumns = new LinkedHashMap<>();

// columnVisibility: Maps column name → boolean (true = visible)
// Tracks which columns the user wants to see. Updated when menu items are toggled.
private LinkedHashMap<String, Boolean> columnVisibility = new LinkedHashMap<>();

// Menu item references — needed to programmatically check/uncheck if settings are loaded
private JCheckBoxMenuItem chkEmail, chkCategory, chkCreatedAt;
```

#### View Menu Setup (inside `createMenuBar()`)

```java
private JMenuBar createMenuBar() {
    JMenuBar menuBar = new JMenuBar();

    // ===== View Menu =====
    JMenu viewMenu = new JMenu("View");

    // --- Columns Submenu ---
    // Each JCheckBoxMenuItem represents one toggleable column
    // ID, Name, and Number are NOT toggleable — they are essential for identification
    JMenu columnsMenu = new JMenu("Columns");
    
    // Email column toggle — starts checked (visible)
    chkEmail = new JCheckBoxMenuItem("Email", true);
    chkEmail.addActionListener(e -> toggleColumn("Email", chkEmail.isSelected()));
    
    // Category column toggle — starts checked (visible)
    chkCategory = new JCheckBoxMenuItem("Category", true);
    chkCategory.addActionListener(e -> toggleColumn("Category", chkCategory.isSelected()));
    
    // Created At column toggle — starts checked (visible)
    chkCreatedAt = new JCheckBoxMenuItem("Created At", true);
    chkCreatedAt.addActionListener(e -> toggleColumn("Created At", chkCreatedAt.isSelected()));
    
    columnsMenu.add(chkEmail);
    columnsMenu.add(chkCategory);
    columnsMenu.add(chkCreatedAt);
    viewMenu.add(columnsMenu);

    viewMenu.addSeparator();

    // --- Accent Color Submenu ---
    JMenu accentMenu = new JMenu("Accent Color");
    ButtonGroup accentGroup = new ButtonGroup();
    
    JRadioButtonMenuItem blueItem = new JRadioButtonMenuItem("Blue", true);
    blueItem.addActionListener(e -> updateAccentColor(UITheme.ACCENT_BLUE));
    
    JRadioButtonMenuItem greenItem = new JRadioButtonMenuItem("Green");
    greenItem.addActionListener(e -> updateAccentColor(UITheme.ACCENT_GREEN));
    
    JRadioButtonMenuItem purpleItem = new JRadioButtonMenuItem("Purple");
    purpleItem.addActionListener(e -> updateAccentColor(UITheme.ACCENT_PURPLE));
    
    // ButtonGroup ensures only one accent color can be active at a time
    accentGroup.add(blueItem);
    accentGroup.add(greenItem);
    accentGroup.add(purpleItem);
    
    accentMenu.add(blueItem);
    accentMenu.add(greenItem);
    accentMenu.add(purpleItem);
    viewMenu.add(accentMenu);

    menuBar.add(viewMenu);
    return menuBar;
}
```

#### Saving Column References (inside `createTablePanel()`, after table creation)

```java
// Save column references for visibility toggle
// This MUST happen BEFORE any columns are removed
// We iterate through all columns and store their TableColumn objects
for (int i = 0; i < COLUMN_NAMES.length; i++) {
    TableColumn col = contactTable.getColumnModel().getColumn(i);
    savedColumns.put(COLUMN_NAMES[i], col);     // Save reference by name
    columnVisibility.put(COLUMN_NAMES[i], true); // All visible by default
}
```

#### Toggle Logic

```java
/**
 * Called when a checkbox menu item is toggled.
 * Updates the visibility map and rebuilds the column model.
 * 
 * @param columnName - the name of the column to toggle (e.g., "Email")
 * @param visible - true to show, false to hide
 */
private void toggleColumn(String columnName, boolean visible) {
    columnVisibility.put(columnName, visible); // Update the visibility state
    updateColumnVisibility();                   // Rebuild the column model
}

/**
 * Rebuild the table's column model based on the current visibility settings.
 * 
 * This method uses a "remove all, then re-add visible" strategy because
 * JTable's column model doesn't support hiding columns in place.
 * The TableColumn objects still exist in savedColumns — they are just not
 * added to the column model when hidden.
 */
private void updateColumnVisibility() {
    // Step 1: Remove ALL columns from the table's column model
    // This clears the visual display completely
    TableColumnModel cm = contactTable.getColumnModel();
    while (cm.getColumnCount() > 0) {
        cm.removeColumn(cm.getColumn(0));
    }
    
    // Step 2: Re-add only the columns marked as visible
    // LinkedHashMap preserves insertion order, so columns appear in original order
    // This is why we used LinkedHashMap instead of HashMap
    for (String name : COLUMN_NAMES) {
        if (columnVisibility.getOrDefault(name, true)) {
            cm.addColumn(savedColumns.get(name));
        }
    }
    // The table automatically repaints when the column model changes
}
```

### How JTable Column Model Works

`JTable` separates data from presentation through two key models:

```
TableModel (DefaultTableModel)
├── Contains the actual data (rows × all columns)
├── Column count never changes
└── Data access is always by model index

TableColumnModel
├── Controls which columns are DISPLAYED
├── Controls column order, width, renderer
├── Can have fewer columns than the TableModel
└── View indexes may differ from model indexes
```

When you remove a `TableColumn` from the `TableColumnModel`, the **data is not deleted** — it still exists in the `TableModel`. The column is simply not rendered. This is why our approach works:

1. All 6 columns of data always exist in `DefaultTableModel`
2. `savedColumns` holds references to all 6 `TableColumn` objects
3. `updateColumnVisibility()` controls which of those 6 are in the `TableColumnModel`
4. When a hidden column is re-shown, its `TableColumn` (with original width, renderer, etc.) is re-added

**What Happens If Columns Are Removed Incorrectly:**

If you don't save `TableColumn` references before removing them, they become **garbage collected**. You cannot re-add them because you have no reference. You would need to create new `TableColumn` objects, losing any custom widths, renderers, or header values the user may have set.

Additionally, if you try to access data by column index after removing columns, the indexes shift. Column 3 (Email) in the original table becomes column 2 if the column before it was removed. This is why our code always uses `convertRowIndexToModel()` and never hardcodes view column indexes when accessing data.

---

## PHASE 24 — Context Menu (Right-Click Actions)

### Why Context Menus Improve Usability

Context menus (right-click menus) are a **universal desktop interaction pattern**. Users expect right-clicking on a data item to show relevant actions. Without a context menu, users must:

1. Select a row
2. Move the mouse to the toolbar
3. Click the appropriate button

With a context menu, they can:

1. Right-click the row → actions appear immediately at the mouse cursor

This saves mouse travel, reduces cognitive load, and follows platform conventions (Windows Explorer, macOS Finder, every text editor, every IDE).

### Complete Implementation

```java
// ==================== CONTEXT MENU ====================

/**
 * Handle right-click (context menu trigger) on the table.
 * Called from MouseAdapter attached to contactTable.
 * 
 * @param e - the MouseEvent to check for popup trigger
 */
private void handleContextMenu(MouseEvent e) {
    // isPopupTrigger() is platform-aware:
    // - Windows: returns true on mouseReleased for right-click
    // - macOS: returns true on mousePressed for Ctrl+click
    // We call this from BOTH mousePressed and mouseReleased to cover all platforms
    if (!e.isPopupTrigger()) return;

    // Find which table row was right-clicked
    int row = contactTable.rowAtPoint(e.getPoint());
    if (row >= 0) {
        // Select the right-clicked row (user expects visual feedback)
        contactTable.setRowSelectionInterval(row, row);
        
        // Convert view row to model row for data access
        // This is critical when the table is sorted — view row ≠ model row
        int modelRow = contactTable.convertRowIndexToModel(row);

        // Create a new popup menu for each right-click
        // Creating fresh prevents stale state from previous popups
        JPopupMenu contextMenu = new JPopupMenu();

        // ===== Edit Action =====
        JMenuItem editItem = new JMenuItem("✏ Edit");
        editItem.addActionListener(ev -> openEditDialog());
        contextMenu.add(editItem);

        // ===== Delete Action =====
        JMenuItem deleteItem = new JMenuItem("🗑 Delete");
        deleteItem.addActionListener(ev -> deleteContact());
        contextMenu.add(deleteItem);

        contextMenu.addSeparator(); // Visual separator between groups

        // ===== Copy Phone Number =====
        // Get the phone number from the model (column 2 = Number)
        String number = (String) tableModel.getValueAt(modelRow, 2);
        JMenuItem copyNumberItem = new JMenuItem("📞 Copy Number");
        copyNumberItem.addActionListener(ev -> {
            copyToClipboard(number);             // Copy to system clipboard
            Toast.show(this, "✔ Number copied!"); // Non-blocking confirmation
        });
        contextMenu.add(copyNumberItem);

        // ===== Copy Email =====
        // Get the email from the model (column 3 = Email)
        String email = (String) tableModel.getValueAt(modelRow, 3);
        JMenuItem copyEmailItem = new JMenuItem("📧 Copy Email");
        // Disable if email is empty — prevents copying blank strings
        copyEmailItem.setEnabled(email != null && !email.isEmpty());
        copyEmailItem.addActionListener(ev -> {
            copyToClipboard(email);
            Toast.show(this, "✔ Email copied!");
        });
        contextMenu.add(copyEmailItem);

        // Show the popup menu at the mouse cursor position
        // e.getX(), e.getY() are relative to the contactTable component
        contextMenu.show(contactTable, e.getX(), e.getY());
    }
}

/**
 * Copy text to the system clipboard.
 * Uses java.awt.datatransfer API for cross-platform clipboard access.
 * 
 * @param text - the string to copy
 */
private void copyToClipboard(String text) {
    if (text != null && !text.isEmpty()) {
        // StringSelection wraps a String as a Transferable object
        // Transferable is the clipboard's data format abstraction
        StringSelection selection = new StringSelection(text);
        
        // Get the system clipboard and set our content
        // The second parameter (ClipboardOwner) is null — we don't need to be
        // notified when another application overwrites our clipboard content
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
    }
}
```

#### Mouse Listener Registration (inside `createTablePanel()`)

```java
// Double-click to edit + right-click context menu
contactTable.addMouseListener(new MouseAdapter() {
    // Double-click: open edit dialog
    public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2 && contactTable.getSelectedRow() != -1) {
            openEditDialog();
        }
    }
    
    // Right-click: show context menu
    // We check BOTH mousePressed and mouseReleased because:
    // - Windows triggers popup on mouseReleased
    // - macOS triggers popup on mousePressed
    // - Linux varies by distribution
    // isPopupTrigger() handles the platform detection internally
    public void mousePressed(MouseEvent e) { handleContextMenu(e); }
    public void mouseReleased(MouseEvent e) { handleContextMenu(e); }
});
```

### How Mouse Listeners Detect Right-Click

Java provides `MouseEvent.isPopupTrigger()` as the **platform-independent** way to detect a context menu request:

| Platform | Trigger Event | Physical Action |
|----------|--------------|-----------------|
| Windows  | `mouseReleased` | Right mouse button release |
| macOS    | `mousePressed` | Ctrl + left click, or right click |
| Linux    | `mouseReleased` | Right mouse button release |

**Why we don't use `e.getButton() == MouseEvent.BUTTON3`:**

While `BUTTON3` is usually the right mouse button, this breaks on:
- macOS with Ctrl+click (which is BUTTON1 + Ctrl modifier)
- Single-button mice (common on older Macs)
- Accessibility configurations that remap buttons

`isPopupTrigger()` handles all these cases correctly because the JVM knows the platform's conventions.

### Why Clipboard API Is Used

The `java.awt.datatransfer` package provides platform-independent clipboard access:

```java
StringSelection selection = new StringSelection(text);
Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
```

This writes to the **system clipboard** — the same clipboard used by Ctrl+C/Ctrl+V across all applications. After `copyToClipboard("555-1234")`, the user can switch to any other application and Ctrl+V to paste the phone number.

Without this API, copied data would only be available within our application, which defeats the purpose of "Copy Number" and "Copy Email" functionality.

---

## PHASE 25 — SwingWorker Background Processing

### Why Long Tasks Freeze the UI

Swing has a **single-threaded rendering model**. All UI updates — painting, event handling, layout — happen on the **Event Dispatch Thread (EDT)**. When you call a method from a button click, that method runs on the EDT:

```
User clicks "Import" button
    → EDT processes ActionEvent
    → ActionListener.actionPerformed() runs ON THE EDT
    → If this method reads a 10MB CSV file...
    → EDT is BLOCKED for 5 seconds
    → No painting, no event handling, no button clicks
    → OS shows "Application Not Responding"
```

The solution is to run heavy work on a **background thread** and only update the UI from the EDT. `SwingWorker` provides exactly this pattern.

### How SwingWorker Works

```
SwingWorker<Result, Progress>
│
├── doInBackground()     → Runs on BACKGROUND thread (safe for I/O, DB, computation)
│   ├── publish(chunks)  → Sends intermediate results to EDT
│   └── return result    → Final result
│
├── process(chunks)      → Runs on EDT (safe to update UI with intermediate data)
│
└── done()               → Runs on EDT after doInBackground() completes
    └── get()            → Retrieves the return value from doInBackground()
```

### Complete Implementation: CSV Export with SwingWorker

```java
/**
 * Export contacts to CSV using SwingWorker for background processing.
 * This prevents the UI from freezing during file I/O.
 * 
 * Flow:
 * 1. User selects file via JFileChooser (runs on EDT — quick)
 * 2. SwingWorker runs CSV export on background thread
 * 3. done() runs on EDT to show success/error toast
 */
private void exportToCSV() {
    // Step 1: File selection — this is a modal dialog, runs on EDT
    // JFileChooser is fast (no I/O), so it's fine on the EDT
    JFileChooser fileChooser = new JFileChooser();
    fileChooser.setDialogTitle("Export Contacts to CSV");
    fileChooser.setSelectedFile(new java.io.File("contacts.csv"));

    int userSelection = fileChooser.showSaveDialog(this);
    if (userSelection != JFileChooser.APPROVE_OPTION) return; // User cancelled

    String filePath = fileChooser.getSelectedFile().getAbsolutePath();

    // Step 2: Run export in background
    // SwingWorker<Void, Void> — no return value, no intermediate progress
    new SwingWorker<Void, Void>() {
        
        private Exception error = null; // Capture any exception from background thread

        /**
         * doInBackground() runs on a BACKGROUND thread.
         * It is safe to perform:
         * - File I/O (reading/writing CSV)
         * - Database queries
         * - Network calls
         * - Any long-running computation
         * 
         * It is NOT safe to:
         * - Modify Swing components (setText, setVisible, repaint)
         * - Show dialogs (JOptionPane)
         * - Access Swing models (DefaultTableModel)
         */
        @Override
        protected Void doInBackground() throws Exception {
            try {
                // This calls DAO layer which writes to file
                // Could take seconds for large datasets
                contactService.exportToCSV(filePath);
            } catch (Exception e) {
                error = e; // Save exception for done() to handle
            }
            return null;
        }

        /**
         * done() runs on the EDT after doInBackground() completes.
         * It is safe to update UI here.
         */
        @Override
        protected void done() {
            if (error != null) {
                // Show error dialog — modal is appropriate for errors
                JOptionPane.showMessageDialog(
                    ContactUI.this,
                    "Error exporting: " + error.getMessage(),
                    "Export Error",
                    JOptionPane.ERROR_MESSAGE
                );
            } else {
                // Show success toast — non-blocking for success
                Toast.show(ContactUI.this, "✔ Contacts exported successfully!");
            }
        }
    }.execute(); // execute() starts the SwingWorker
}
```

### Complete Implementation: CSV Import with SwingWorker and Progress

```java
/**
 * Import contacts from CSV using SwingWorker with progress reporting.
 * 
 * This example shows the full SwingWorker lifecycle:
 * - doInBackground() reads the CSV file
 * - publish() sends status updates to the EDT
 * - process() updates a progress label on the EDT
 * - done() shows the final result
 */
private void importCSVWithProgress(String filePath) {
    // Create a simple progress dialog
    JDialog progressDialog = new JDialog(this, "Importing...", false); // false = non-modal
    JLabel progressLabel = new JLabel("Starting import...");
    progressLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
    progressLabel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
    progressDialog.add(progressLabel);
    progressDialog.pack();
    progressDialog.setLocationRelativeTo(this);
    progressDialog.setVisible(true);

    // SwingWorker<ImportResult, String>
    // ImportResult = final return type from doInBackground()
    // String = intermediate progress type sent via publish()
    new SwingWorker<ImportResult, String>() {

        @Override
        protected ImportResult doInBackground() throws Exception {
            // publish() sends messages to the EDT for UI updates
            // These messages are batched and delivered to process()
            publish("Reading CSV file...");
            
            // Perform the actual import (file I/O + database inserts)
            ImportResult result = contactService.importFromCSV(filePath);
            
            publish("Import complete. Processing results...");
            return result;
        }

        /**
         * process() runs on the EDT.
         * Receives batched messages from publish().
         * 
         * @param chunks - list of messages published since last process() call
         *                  May contain multiple messages if publish() was called rapidly
         */
        @Override
        protected void process(java.util.List<String> chunks) {
            // Show the LATEST message (last in the batch)
            // Earlier messages in the batch are outdated by the time process() runs
            String latestMessage = chunks.get(chunks.size() - 1);
            progressLabel.setText(latestMessage);
        }

        /**
         * done() runs on the EDT after doInBackground() completes.
         * Use get() to retrieve the return value or catch exceptions.
         */
        @Override
        protected void done() {
            progressDialog.dispose(); // Close progress dialog
            
            try {
                ImportResult result = get(); // Retrieve result from doInBackground()
                
                // Refresh table and statistics
                loadAllContacts();
                refreshStatistics();
                updateStatusBar();
                updateEmptyState();
                
                // Show result summary
                Toast.show(ContactUI.this,
                    "✔ Imported " + result.getSuccessCount() + " contacts" +
                    (result.getFailCount() > 0 ? " (" + result.getFailCount() + " failed)" : "")
                );
                
            } catch (Exception e) {
                JOptionPane.showMessageDialog(
                    ContactUI.this,
                    "Import failed: " + e.getMessage(),
                    "Import Error",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }.execute();
}
```

### Detailed Explanation

**Why SwingWorker Is Safe:**

SwingWorker enforces a strict threading contract:

| Method | Thread | Safe For |
|--------|--------|----------|
| `doInBackground()` | Background worker thread | File I/O, DB queries, computation |
| `process()` | EDT | Updating labels, progress bars, tables |
| `done()` | EDT | Final UI updates, showing results |
| `publish()` | Background thread | Sending data to `process()` |
| `get()` | Any thread (blocks if not done) | Retrieving result |

This contract means you **cannot accidentally** update the UI from the wrong thread — the framework structure makes the distinction explicit.

**What Breaks If Heavy DB Tasks Run on EDT:**

```
User clicks "Import 10,000 contacts"
    → EDT starts processing
    → Database: 10,000 INSERT statements (takes 8 seconds)
    → During those 8 seconds:
        × User cannot click any button
        × User cannot close the window
        × Scroll bars don't respond
        × Window cannot be moved or resized
        × Timer events don't fire (toasts freeze)
        × OS may show "Not Responding" dialog
        × User thinks application crashed
        × User force-kills the process
        × Unsaved data is lost
```

With SwingWorker:

```
User clicks "Import 10,000 contacts"
    → EDT starts SwingWorker.execute()
    → execute() creates background thread and returns IMMEDIATELY
    → EDT is free — buttons work, window is responsive
    → Background thread: 10,000 INSERT statements
    → publish("Imported 500 of 10,000...")  → process() updates label on EDT
    → publish("Imported 5,000 of 10,000...") → process() updates label on EDT
    → doInBackground() returns result
    → done() runs on EDT: shows toast, refreshes table
    → Total UI freeze time: ~0ms
```

**Why `publish()` and `process()` Use Batching:**

If `publish()` is called 10,000 times rapidly (once per row), Swing doesn't call `process()` 10,000 times. Instead, it batches the published values and delivers them in groups. In `process(List<String> chunks)`, the `chunks` list might contain 50-100 messages at once. This prevents the EDT from being overwhelmed with progress updates.

Best practice: In `process()`, only act on `chunks.get(chunks.size() - 1)` — the most recent message. Previous messages are stale.

---

## PHASE 26 — Settings Persistence (Preferences API)

### Why Settings Persistence Matters

Every time the user launches the application, they have to:
- Switch to their preferred theme (Dark/Light)
- Select their accent color
- Resize the window to their preferred dimensions
- Re-show/hide columns they customized

This is frustrating. Professional applications remember user preferences. Java provides the `java.util.prefs.Preferences` API for exactly this purpose.

### Complete Implementation: SettingsManager.java

```java
package util;

import java.awt.Color;
import java.awt.Dimension;
import java.util.prefs.Preferences;

/**
 * SettingsManager - Persists user preferences using Java Preferences API.
 * 
 * The Preferences API stores data in:
 * - Windows: Windows Registry (HKEY_CURRENT_USER\Software\JavaSoft\Prefs)
 * - macOS: ~/Library/Preferences/com.apple.java.util.prefs.plist
 * - Linux: ~/.java/.userPrefs/
 * 
 * Advantages over file-based persistence:
 * 1. No file path management needed
 * 2. No file permission issues
 * 3. No file format parsing (no JSON/XML/properties files)
 * 4. Automatic per-user isolation (each OS user has their own preferences)
 * 5. Thread-safe by default
 * 
 * Usage:
 *   SettingsManager.saveTheme(true);  // Save dark mode preference
 *   boolean isDark = SettingsManager.loadTheme(); // Retrieve it later
 */
public class SettingsManager {

    // Preferences node for our application
    // The node path is derived from the class — ensures uniqueness
    // Node name: /util/SettingsManager (based on package + class name)
    private static final Preferences prefs = Preferences.userNodeForPackage(SettingsManager.class);

    // ===== Preference Keys =====
    // These are string keys used to store/retrieve values.
    // Using constants prevents typos and enables refactoring.
    private static final String KEY_DARK_MODE = "dark_mode";
    private static final String KEY_ACCENT_RED = "accent_red";
    private static final String KEY_ACCENT_GREEN = "accent_green";
    private static final String KEY_ACCENT_BLUE = "accent_blue";
    private static final String KEY_WINDOW_WIDTH = "window_width";
    private static final String KEY_WINDOW_HEIGHT = "window_height";
    private static final String KEY_WINDOW_X = "window_x";
    private static final String KEY_WINDOW_Y = "window_y";
    private static final String KEY_COL_EMAIL_VISIBLE = "col_email_visible";
    private static final String KEY_COL_CATEGORY_VISIBLE = "col_category_visible";
    private static final String KEY_COL_CREATEDAT_VISIBLE = "col_createdat_visible";

    // ==================== THEME PERSISTENCE ====================

    /**
     * Save the current theme preference.
     * 
     * @param isDarkMode - true for dark theme, false for light theme
     */
    public static void saveTheme(boolean isDarkMode) {
        // putBoolean stores a boolean value under the given key
        // The value is persisted immediately to the backing store
        prefs.putBoolean(KEY_DARK_MODE, isDarkMode);
    }

    /**
     * Load the saved theme preference.
     * 
     * @return true if dark mode was saved, false otherwise
     *         Default: false (light mode) if no preference exists
     */
    public static boolean loadTheme() {
        // getBoolean retrieves the value, or returns the default (false) if not set
        // This means first-time users automatically get light mode
        return prefs.getBoolean(KEY_DARK_MODE, false);
    }

    // ==================== ACCENT COLOR PERSISTENCE ====================

    /**
     * Save the current accent color.
     * Colors are stored as separate R, G, B integer values because
     * Preferences API doesn't have a putColor() method.
     * 
     * @param color - the accent color to persist
     */
    public static void saveAccentColor(Color color) {
        prefs.putInt(KEY_ACCENT_RED, color.getRed());     // 0-255
        prefs.putInt(KEY_ACCENT_GREEN, color.getGreen()); // 0-255
        prefs.putInt(KEY_ACCENT_BLUE, color.getBlue());   // 0-255
    }

    /**
     * Load the saved accent color.
     * 
     * @return the saved Color, or default Blue accent if not set
     */
    public static Color loadAccentColor() {
        // Default values match ACCENT_BLUE (66, 133, 244)
        int r = prefs.getInt(KEY_ACCENT_RED, 66);
        int g = prefs.getInt(KEY_ACCENT_GREEN, 133);
        int b = prefs.getInt(KEY_ACCENT_BLUE, 244);
        return new Color(r, g, b);
    }

    // ==================== WINDOW SIZE PERSISTENCE ====================

    /**
     * Save the current window dimensions and position.
     * Called when the window is moved or resized.
     * 
     * @param x - window X position on screen
     * @param y - window Y position on screen
     * @param width - window width in pixels
     * @param height - window height in pixels
     */
    public static void saveWindowBounds(int x, int y, int width, int height) {
        prefs.putInt(KEY_WINDOW_X, x);
        prefs.putInt(KEY_WINDOW_Y, y);
        prefs.putInt(KEY_WINDOW_WIDTH, width);
        prefs.putInt(KEY_WINDOW_HEIGHT, height);
    }

    /**
     * Load the saved window width.
     * @return saved width, or 1300 (default) if not set
     */
    public static int loadWindowWidth() {
        return prefs.getInt(KEY_WINDOW_WIDTH, 1300);
    }

    /**
     * Load the saved window height.
     * @return saved height, or 780 (default) if not set
     */
    public static int loadWindowHeight() {
        return prefs.getInt(KEY_WINDOW_HEIGHT, 780);
    }

    /**
     * Load the saved window X position.
     * @return saved X, or -1 if not set (caller should center on screen)
     */
    public static int loadWindowX() {
        return prefs.getInt(KEY_WINDOW_X, -1);
    }

    /**
     * Load the saved window Y position.
     * @return saved Y, or -1 if not set (caller should center on screen)
     */
    public static int loadWindowY() {
        return prefs.getInt(KEY_WINDOW_Y, -1);
    }

    // ==================== COLUMN VISIBILITY PERSISTENCE ====================

    /**
     * Save column visibility preferences.
     * 
     * @param emailVisible - whether Email column is shown
     * @param categoryVisible - whether Category column is shown
     * @param createdAtVisible - whether Created At column is shown
     */
    public static void saveColumnVisibility(boolean emailVisible, 
                                             boolean categoryVisible, 
                                             boolean createdAtVisible) {
        prefs.putBoolean(KEY_COL_EMAIL_VISIBLE, emailVisible);
        prefs.putBoolean(KEY_COL_CATEGORY_VISIBLE, categoryVisible);
        prefs.putBoolean(KEY_COL_CREATEDAT_VISIBLE, createdAtVisible);
    }

    /**
     * Load Email column visibility.
     * @return true if visible (default: true)
     */
    public static boolean loadEmailColumnVisible() {
        return prefs.getBoolean(KEY_COL_EMAIL_VISIBLE, true);
    }

    /**
     * Load Category column visibility.
     * @return true if visible (default: true)
     */
    public static boolean loadCategoryColumnVisible() {
        return prefs.getBoolean(KEY_COL_CATEGORY_VISIBLE, true);
    }

    /**
     * Load Created At column visibility.
     * @return true if visible (default: true)
     */
    public static boolean loadCreatedAtColumnVisible() {
        return prefs.getBoolean(KEY_COL_CREATEDAT_VISIBLE, true);
    }

    // ==================== UTILITY ====================

    /**
     * Clear all saved preferences (reset to defaults).
     * Useful for "Reset Settings" feature.
     */
    public static void clearAll() {
        try {
            prefs.clear(); // Removes all key-value pairs from this node
        } catch (Exception e) {
            System.err.println("Failed to clear preferences: " + e.getMessage());
        }
    }
}
```

### Integration with ContactUI.java

#### Loading Settings on Startup (in constructor or `initComponents()`)

```java
public ContactUI() {
    this.contactService = new ContactService();

    // LOAD SAVED PREFERENCES BEFORE building UI
    // Theme must be applied before any components are created
    boolean savedDarkMode = SettingsManager.loadTheme();
    if (savedDarkMode) {
        UITheme.applyDarkTheme();
    } else {
        UITheme.applyLightTheme();
    }

    // Load saved accent color
    Color savedAccent = SettingsManager.loadAccentColor();
    UITheme.setAccentColor(savedAccent);

    initComponents();

    // Apply saved window size and position
    int w = SettingsManager.loadWindowWidth();
    int h = SettingsManager.loadWindowHeight();
    setSize(w, h);

    int x = SettingsManager.loadWindowX();
    int y = SettingsManager.loadWindowY();
    if (x >= 0 && y >= 0) {
        setLocation(x, y); // Restore saved position
    } else {
        setLocationRelativeTo(null); // Center on screen (first launch)
    }

    // Apply saved column visibility
    boolean emailVis = SettingsManager.loadEmailColumnVisible();
    boolean catVis = SettingsManager.loadCategoryColumnVisible();
    boolean createdVis = SettingsManager.loadCreatedAtColumnVisible();
    chkEmail.setSelected(emailVis);
    chkCategory.setSelected(catVis);
    chkCreatedAt.setSelected(createdVis);
    toggleColumn("Email", emailVis);
    toggleColumn("Category", catVis);
    toggleColumn("Created At", createdVis);

    // Save window bounds when moved or resized
    addComponentListener(new java.awt.event.ComponentAdapter() {
        public void componentResized(java.awt.event.ComponentEvent e) {
            SettingsManager.saveWindowBounds(getX(), getY(), getWidth(), getHeight());
        }
        public void componentMoved(java.awt.event.ComponentEvent e) {
            SettingsManager.saveWindowBounds(getX(), getY(), getWidth(), getHeight());
        }
    });

    loadAllContacts();
    refreshStatistics();
    updateStatusBar();
    updateEmptyState();
    setupKeyboardShortcuts();
}
```

#### Saving Settings on Theme/Accent Changes

```java
private void toggleTheme() {
    UITheme.toggleTheme();
    SettingsManager.saveTheme(UITheme.isDarkMode()); // PERSIST the change

    // ... existing theme update code ...
}

private void updateAccentColor(Color color) {
    UITheme.setAccentColor(color);
    SettingsManager.saveAccentColor(color); // PERSIST the change

    // ... existing accent update code ...
}

private void toggleColumn(String columnName, boolean visible) {
    columnVisibility.put(columnName, visible);
    updateColumnVisibility();

    // PERSIST column visibility
    SettingsManager.saveColumnVisibility(
        columnVisibility.getOrDefault("Email", true),
        columnVisibility.getOrDefault("Category", true),
        columnVisibility.getOrDefault("Created At", true)
    );
}
```

### Why Preferences API Is Better Than File for Small Settings

| Aspect | Preferences API | Properties File |
|--------|----------------|-----------------|
| **Setup** | Zero — no file creation needed | Must create, locate, and manage file |
| **Parsing** | Built-in type methods (`getBoolean`, `getInt`) | All values are strings — you parse manually |
| **Location** | OS handles it (Registry, plist, etc.) | You must choose and document file path |
| **Per-user** | Automatic — each OS user has separate prefs | You must implement user-specific paths |
| **Thread safety** | Built-in | You must synchronize file access |
| **Corruption** | OS manages integrity | File can be corrupted by concurrent writes |
| **Portability** | Preferences move with user profile | File must be in a known, fixed location |

For small configuration data (theme, accent, window size), Preferences API is the clear winner. For large structured data (saved queries, custom templates), a JSON or SQLite file may be more appropriate.

---

## PHASE 27 — Logging System

### Why Production Apps Need Logging

In development, you can set breakpoints and step through code. In production, you cannot. When a customer reports "the import failed yesterday afternoon," your only tool for diagnosis is **logs**.

`System.out.println()` is not logging. It has no timestamps, no severity levels, no file output, no structured format, and cannot be enabled/disabled per module. Professional logging provides all of these.

### Complete Implementation: AppLogger.java

```java
package util;

import java.io.IOException;
import java.util.logging.*;

/**
 * Application Logger - Centralized logging using java.util.logging (JUL).
 * 
 * Log Levels (from most severe to least):
 *   SEVERE  → System-breaking errors (database down, file not found)
 *   WARNING → Recoverable problems (validation failure, timeout with retry)
 *   INFO    → Normal operations (contact added, CSV exported, app started)
 *   CONFIG  → Configuration information (theme loaded, DB URL)
 *   FINE    → Detailed debugging (SQL queries, method entry/exit)
 *   FINER   → More detailed debugging
 *   FINEST  → Most granular tracing
 * 
 * Usage:
 *   AppLogger.info("Contact added: " + contact.getName());
 *   AppLogger.severe("Database connection failed", exception);
 *   AppLogger.warning("Duplicate contact skipped: " + name);
 */
public class AppLogger {

    // Single logger instance for the entire application
    // Logger name "ContactManager" appears in every log entry
    private static final Logger logger = Logger.getLogger("ContactManager");

    // Static initializer — runs once when the class is first loaded
    static {
        try {
            setupLogger();
        } catch (IOException e) {
            System.err.println("CRITICAL: Failed to initialize logger: " + e.getMessage());
            // Application can still run — console logging will work as fallback
        }
    }

    /**
     * Configure the logger with console and file handlers.
     * 
     * This creates two output destinations:
     * 1. Console (stdout) — for development and immediate visibility
     * 2. File (app.log) — for persistent, reviewable logs
     */
    private static void setupLogger() throws IOException {
        // Remove default console handler to prevent duplicate output
        // Java's root logger has a default ConsoleHandler that would duplicate our custom one
        Logger rootLogger = Logger.getLogger("");
        Handler[] handlers = rootLogger.getHandlers();
        for (Handler handler : handlers) {
            rootLogger.removeHandler(handler);
        }

        // Set logger to accept ALL levels
        // Individual handlers can filter further
        logger.setLevel(Level.ALL);

        // ===== Console Handler =====
        // Outputs to System.err (standard for logging)
        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setLevel(Level.INFO); // Console shows INFO and above
        consoleHandler.setFormatter(new SimpleFormatter() {
            // Custom format: [LEVEL] timestamp - message
            @Override
            public String format(LogRecord record) {
                return String.format("[%s] %s - %s%n",
                    record.getLevel().getName(),                                // Log level
                    new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")       // Timestamp
                        .format(new java.util.Date(record.getMillis())),
                    formatMessage(record)                                        // Message text
                );
            }
        });
        logger.addHandler(consoleHandler);

        // ===== File Handler =====
        // Writes to app.log file with rotation
        // Parameters:
        //   "app.log" — base filename
        //   1024 * 1024 — max file size (1MB) before rotation
        //   3 — keep up to 3 rotated files (app.log, app.log.1, app.log.2)
        //   true — append to existing file (don't overwrite on restart)
        FileHandler fileHandler = new FileHandler("app.log", 1024 * 1024, 3, true);
        fileHandler.setLevel(Level.ALL);  // File captures ALL levels (including FINE/FINER)
        fileHandler.setFormatter(new SimpleFormatter() {
            @Override
            public String format(LogRecord record) {
                String message = String.format("[%s] %s - %s",
                    record.getLevel().getName(),
                    new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                        .format(new java.util.Date(record.getMillis())),
                    formatMessage(record)
                );
                // If an exception is attached, append its stack trace
                if (record.getThrown() != null) {
                    java.io.StringWriter sw = new java.io.StringWriter();
                    record.getThrown().printStackTrace(new java.io.PrintWriter(sw));
                    message += "\n" + sw.toString();
                }
                return message + "\n";
            }
        });
        logger.addHandler(fileHandler);

        logger.info("Logger initialized successfully");
    }

    // ==================== CONVENIENCE METHODS ====================

    /**
     * Log an informational message.
     * Use for: successful operations, state changes, startup/shutdown
     * 
     * @param message - description of what happened
     */
    public static void info(String message) {
        logger.info(message);
    }

    /**
     * Log a warning message.
     * Use for: recoverable errors, unexpected but handled situations
     * 
     * @param message - description of the issue
     */
    public static void warning(String message) {
        logger.warning(message);
    }

    /**
     * Log a severe error message with exception.
     * Use for: database failures, unrecoverable errors, crashes
     * 
     * @param message - description of the failure
     * @param throwable - the exception that occurred
     */
    public static void severe(String message, Throwable throwable) {
        logger.log(Level.SEVERE, message, throwable);
    }

    /**
     * Log a severe error message without exception.
     * Use for: critical business logic failures
     * 
     * @param message - description of the failure
     */
    public static void severe(String message) {
        logger.severe(message);
    }

    /**
     * Log a debug message (only appears in file, not console).
     * Use for: SQL queries, method parameters, internal state
     * 
     * @param message - detailed debug information
     */
    public static void debug(String message) {
        logger.fine(message);
    }

    /**
     * Log a configuration message.
     * Use for: settings loaded, database URL, theme applied
     * 
     * @param message - configuration details
     */
    public static void config(String message) {
        logger.config(message);
    }
}
```

### Integration with Existing Layers

#### In ContactService.java

```java
public void addContact(Contact c) {
    AppLogger.info("Adding contact: " + c.getName() + " (" + c.getNumber() + ")");
    try {
        contactDAO.addContact(c);
        AppLogger.info("Contact added successfully: " + c.getName());
    } catch (Exception e) {
        AppLogger.severe("Failed to add contact: " + c.getName(), e);
        throw e; // Re-throw so UI can handle it
    }
}

public void softDeleteById(int id) {
    AppLogger.info("Soft deleting contact ID: " + id);
    contactDAO.softDeleteById(id);
    AppLogger.info("Contact soft deleted: ID " + id);
}

public void updateContact(Contact c) {
    AppLogger.info("Updating contact ID: " + c.getId() + " Name: " + c.getName());
    try {
        // ... existing validation logic ...
        contactDAO.updateContact(c);
        AppLogger.info("Contact updated successfully: ID " + c.getId());
    } catch (RuntimeException e) {
        AppLogger.warning("Update validation failed: " + e.getMessage());
        throw e;
    }
}

public ImportResult importFromCSV(String filePath) {
    AppLogger.info("Starting CSV import from: " + filePath);
    ImportResult result = contactDAO.importFromCSV(filePath);
    AppLogger.info("CSV import complete: " + result.getSuccessCount() + " success, " 
                   + result.getFailCount() + " failed");
    return result;
}
```

#### In ContactDAOImpl.java (for SQL-level logging)

```java
public List<Contact> search(String keyword) {
    AppLogger.debug("Executing search query with keyword: " + keyword);
    // ... existing code ...
    AppLogger.debug("Search returned " + results.size() + " results");
    return results;
}
```

#### In DBConnection.java

```java
public static Connection getConnection() {
    try {
        if (connection == null || connection.isClosed()) {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            AppLogger.config("Database connected: " + URL);
        }
    } catch (ClassNotFoundException e) {
        AppLogger.severe("MySQL JDBC Driver not found!", e);
    } catch (SQLException e) {
        AppLogger.severe("Database connection failed: " + URL, e);
    }
    return connection;
}
```

#### In Main.java

```java
public static void main(String[] args) {
    AppLogger.info("========================================");
    AppLogger.info("Contact Management System - Starting...");
    AppLogger.info("========================================");
    
    AppLogger.info("Testing database connection...");
    if (!DBConnection.testConnection()) {
        AppLogger.severe("Cannot connect to database! Application cannot start.");
        // ... error dialog ...
        return;
    }
    
    AppLogger.info("Database connection successful. Launching UI...");
    
    SwingUtilities.invokeLater(() -> {
        ContactUI contactUI = new ContactUI();
        contactUI.display();
        AppLogger.info("Application started successfully");
    });
}
```

### Sample Log Output (app.log)

```
[INFO] 2026-02-13 09:30:01 - ========================================
[INFO] 2026-02-13 09:30:01 - Contact Management System - Starting...
[INFO] 2026-02-13 09:30:01 - ========================================
[INFO] 2026-02-13 09:30:01 - Testing database connection...
[CONFIG] 2026-02-13 09:30:02 - Database connected: jdbc:mysql://localhost:3306/jdbcdb
[INFO] 2026-02-13 09:30:02 - Database connection successful. Launching UI...
[INFO] 2026-02-13 09:30:02 - Logger initialized successfully
[INFO] 2026-02-13 09:30:03 - Application started successfully
[INFO] 2026-02-13 09:30:15 - Adding contact: John Smith (555-1234)
[INFO] 2026-02-13 09:30:15 - Contact added successfully: John Smith
[INFO] 2026-02-13 09:30:22 - Starting CSV import from: C:\Users\sai\contacts_backup.csv
[INFO] 2026-02-13 09:30:24 - CSV import complete: 47 success, 3 failed
[WARNING] 2026-02-13 09:31:10 - Update validation failed: A contact with name 'John Smith' already exists!
[SEVERE] 2026-02-13 09:45:00 - Database connection failed: jdbc:mysql://localhost:3306/jdbcdb
com.mysql.cj.jdbc.exceptions.CommunicationsException: Communications link failure
    at com.mysql.cj.jdbc.ConnectionImpl.createNewIO(ConnectionImpl.java:825)
    at com.mysql.cj.jdbc.ConnectionImpl.<init>(ConnectionImpl.java:448)
    ...
```

### Why System.out Is Not Professional

| Aspect | `System.out.println()` | `java.util.logging` |
|--------|----------------------|---------------------|
| **Levels** | None — everything looks the same | SEVERE, WARNING, INFO, FINE, etc. |
| **Timestamp** | None | Automatic timestamp on every entry |
| **File output** | Only to console | Console + file + network + custom |
| **Rotation** | N/A | Automatic file rotation by size |
| **Filtering** | Cannot disable specific messages | Filter by level, by module |
| **Stack traces** | Manual `e.printStackTrace()` | Automatic with `log(Level, msg, throwable)` |
| **Performance** | Always writes (even if nobody reads) | Can disable levels (e.g., disable FINE in production) |
| **Format** | Unstructured text | Configurable, parseable format |

In production, `System.out.println("Error: " + e)` tells you *something* went wrong. `[SEVERE] 2026-02-13 09:45:00 - Database connection failed: jdbc:mysql://localhost:3306/jdbcdb` followed by a full stack trace tells you *exactly* what went wrong, *when*, and *where*.

---

## PHASE 28 — Custom Exception Handling Strategy

### Why Custom Exceptions Improve Clarity

Java's generic `RuntimeException`, `SQLException`, and `Exception` tell you *that* something went wrong but not *what category of problem* it is. Custom exceptions enable:

1. **Meaningful catch blocks** — `catch (ValidationException e)` vs `catch (Exception e)`
2. **Layer-appropriate messages** — the UI shows "Invalid phone format" not "SQLException: column 'number' cannot be null"
3. **Separation of concerns** — the DAO throws `DatabaseException`, the Service throws `ValidationException`, the UI catches both and handles them differently

### Complete Exception Hierarchy

```java
package service;

/**
 * Base exception for all application-specific errors.
 * 
 * Extends RuntimeException (unchecked) because:
 * 1. Checked exceptions force try/catch at every call site, adding noise
 * 2. Most callers cannot meaningfully recover from these errors
 * 3. The global exception handler in the UI catches everything anyway
 * 
 * All custom exceptions extend this base class, enabling:
 *   catch (AppException e) → catches ALL application errors
 *   catch (ValidationException e) → catches only validation errors
 */
public class AppException extends RuntimeException {

    // Error code for programmatic handling (optional)
    // Useful for logging, analytics, or internationalization
    private final String errorCode;

    /**
     * Constructor with message only.
     * 
     * @param message - human-readable error description
     */
    public AppException(String message) {
        super(message);
        this.errorCode = "APP_ERROR";
    }

    /**
     * Constructor with message and error code.
     * 
     * @param message - human-readable error description
     * @param errorCode - machine-readable error identifier
     */
    public AppException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * Constructor with message and cause.
     * 
     * @param message - human-readable error description
     * @param cause - the original exception that triggered this error
     */
    public AppException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "APP_ERROR";
    }

    /**
     * Full constructor with message, error code, and cause.
     * 
     * @param message - human-readable error description
     * @param errorCode - machine-readable error identifier  
     * @param cause - the original exception
     */
    public AppException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
```

```java
package service;

/**
 * Thrown when user input fails validation rules.
 * 
 * Examples:
 * - Empty name field
 * - Invalid email format
 * - Phone number exceeding max length
 * - Duplicate contact name
 * 
 * The UI catches this to display a warning dialog (not an error dialog)
 * because the user can fix the problem by correcting their input.
 */
public class ValidationException extends AppException {

    // The field that failed validation (e.g., "name", "email", "number")
    private final String fieldName;

    /**
     * Constructor with message only.
     * Use when the failing field is not specific or multiple fields failed.
     */
    public ValidationException(String message) {
        super(message, "VALIDATION_ERROR");
        this.fieldName = null;
    }

    /**
     * Constructor with message and field name.
     * Use when you can identify which specific field failed.
     * 
     * @param message - human-readable error description
     * @param fieldName - the form field that failed validation
     */
    public ValidationException(String message, String fieldName) {
        super(message, "VALIDATION_ERROR");
        this.fieldName = fieldName;
    }

    /**
     * Get the name of the field that failed validation.
     * Can be used to highlight the specific field in the form.
     * 
     * @return field name, or null if not specified
     */
    public String getFieldName() {
        return fieldName;
    }
}
```

```java
package dao;

import service.AppException;

/**
 * Thrown when a database operation fails.
 * 
 * This wraps SQLException with a meaningful application-level message.
 * The original SQLException is preserved as the cause for logging,
 * but the message is user-friendly.
 * 
 * Examples:
 * - "Failed to save contact" (wrapping: INSERT failed due to constraint violation)
 * - "Database connection lost" (wrapping: CommunicationsException)
 * - "Failed to load contacts" (wrapping: SELECT query syntax error)
 */
public class DatabaseException extends AppException {

    /**
     * Constructor with user-friendly message and original cause.
     * 
     * @param message - what operation failed (user-readable)
     * @param cause - the original SQLException
     */
    public DatabaseException(String message, Throwable cause) {
        super(message, "DATABASE_ERROR", cause);
    }

    /**
     * Constructor with message only.
     * Use when no original exception is available.
     * 
     * @param message - what operation failed
     */
    public DatabaseException(String message) {
        super(message, "DATABASE_ERROR");
    }
}
```

### How Service Layer Throws Custom Exceptions

```java
// In ContactService.java

public void addContact(Contact c) {
    // Validate first — throw ValidationException if invalid
    String validationError = validateContact(c);
    if (validationError != null) {
        throw new ValidationException(validationError);
    }

    // Check for duplicates — throw ValidationException (it's a data rule, not DB error)
    if (existsByName(c.getName())) {
        throw new ValidationException(
            "A contact with name '" + c.getName() + "' already exists!",
            "name"  // Field name — UI can highlight this field
        );
    }
    if (existsByNumber(c.getNumber())) {
        throw new ValidationException(
            "A contact with number '" + c.getNumber() + "' already exists!",
            "number"
        );
    }

    // Call DAO — any SQLException becomes DatabaseException
    try {
        contactDAO.addContact(c);
        AppLogger.info("Contact added: " + c.getName());
    } catch (Exception e) {
        AppLogger.severe("Failed to add contact: " + c.getName(), e);
        throw new DatabaseException("Failed to save contact. Please try again.", e);
    }
}

public void updateContact(Contact c) {
    String validationError = validateContactForUpdate(c);
    if (validationError != null) {
        throw new ValidationException(validationError);
    }

    Contact existing = contactDAO.getById(c.getId());
    if (existing == null) {
        throw new ValidationException("Contact not found!", "id");
    }

    // Check for duplicate name (excluding self)
    if (!existing.getName().equalsIgnoreCase(c.getName())) {
        Contact duplicate = contactDAO.getByName(c.getName());
        if (duplicate != null && duplicate.getId() != c.getId()) {
            throw new ValidationException(
                "A contact with name '" + c.getName() + "' already exists!",
                "name"
            );
        }
    }

    // Check for duplicate number (excluding self)
    if (!existing.getNumber().equals(c.getNumber())) {
        Contact duplicate = contactDAO.getByNumber(c.getNumber());
        if (duplicate != null && duplicate.getId() != c.getId()) {
            throw new ValidationException(
                "A contact with number '" + c.getNumber() + "' already exists!",
                "number"
            );
        }
    }

    try {
        contactDAO.updateContact(c);
        AppLogger.info("Contact updated: ID " + c.getId());
    } catch (Exception e) {
        AppLogger.severe("Failed to update contact: ID " + c.getId(), e);
        throw new DatabaseException("Failed to update contact. Please try again.", e);
    }
}
```

### How DAO Layer Wraps SQL Exceptions

```java
// In ContactDAOImpl.java

public void addContact(Contact c) {
    String sql = "INSERT INTO contacts (name, number, email, category) VALUES (?, ?, ?, ?)";
    
    try (Connection conn = DBConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
        
        ps.setString(1, c.getName());
        ps.setString(2, c.getNumber());
        ps.setString(3, c.getEmail());
        ps.setString(4, c.getCategory());
        ps.executeUpdate();
        
    } catch (SQLException e) {
        AppLogger.severe("SQL error in addContact: " + e.getSQLState(), e);
        // Wrap raw SQL exception in application-specific exception
        throw new DatabaseException("Failed to save contact to database", e);
    }
}
```

### How UI Catches and Handles Exceptions

```java
// In ContactFormDialog.java — Save button action

private void saveContact() {
    try {
        Contact contact = buildContactFromForm(); // Read form fields
        
        if (isEditMode) {
            contactService.updateContact(contact);
            Toast.show(this, "✔ Contact updated!");
        } else {
            contactService.addContact(contact);
            Toast.show(this, "✔ Contact added!");
        }
        
        saveSuccessful = true;
        dispose(); // Close dialog
        
    } catch (ValidationException e) {
        // VALIDATION ERROR: Show as warning (user can fix it)
        // highlight the failing field if fieldName is available
        JOptionPane.showMessageDialog(this,
            e.getMessage(),
            "Validation Error",
            JOptionPane.WARNING_MESSAGE  // Warning icon — user recoverable
        );
        
        // Optionally highlight the field
        if ("name".equals(e.getFieldName())) {
            nameField.requestFocus();
            nameField.selectAll();
        } else if ("number".equals(e.getFieldName())) {
            phoneField.requestFocus();
            phoneField.selectAll();
        }
        
    } catch (DatabaseException e) {
        // DATABASE ERROR: Show as error (system problem)
        AppLogger.severe("Database error in save", e);
        JOptionPane.showMessageDialog(this,
            e.getMessage(),
            "Database Error",
            JOptionPane.ERROR_MESSAGE  // Error icon — system problem
        );
        
    } catch (Exception e) {
        // UNEXPECTED ERROR: Catch-all for anything we didn't anticipate
        AppLogger.severe("Unexpected error in save", e);
        JOptionPane.showMessageDialog(this,
            "An unexpected error occurred. Please check the logs.",
            "Error",
            JOptionPane.ERROR_MESSAGE
        );
    }
}
```

### Why This Layering Matters

```
Layer        | Throws                  | Catches               | Shows to User
-------------|------------------------|-----------------------|------------------
DAO          | DatabaseException      | SQLException          | (nothing — DAO has no UI)
Service      | ValidationException    | (nothing — re-throws) | (nothing — Service has no UI)
             | DatabaseException      |                       |
UI           | (nothing)              | ValidationException   | "Invalid email format"
             |                        | DatabaseException     | "Failed to save. Try again."
             |                        | Exception             | "Unexpected error. Check logs."
```

**Why UI Should Not Expose Raw SQL Exceptions:**

A raw `SQLException` message looks like:

```
com.mysql.cj.jdbc.exceptions.MysqlDataTruncation: Data truncation: 
Data too long for column 'number' at row 1
```

This is **meaningless and frightening** to a non-technical user. Our custom exception wraps it as:

```
"Number cannot exceed 15 characters!"
```

The original `SQLException` is still available via `getCause()` for logging and debugging, but the user never sees it.

---

## PHASE 29 — Performance & Optimization Considerations

### Index Usage

MySQL uses indexes to speed up `WHERE`, `ORDER BY`, and `JOIN` clauses. Without indexes, every query performs a **full table scan** — checking every row.

Our `contacts` table benefits from indexes on frequently-queried columns:

```sql
-- The primary key (id) is automatically indexed
-- These additional indexes improve search and validation performance:

-- Speed up name lookups (used by existsByName, getByName, search)
CREATE INDEX idx_contacts_name ON contacts(name);

-- Speed up number lookups (used by existsByNumber, getByNumber)
CREATE INDEX idx_contacts_number ON contacts(number);

-- Speed up category filters (used by searchByCategory)
CREATE INDEX idx_contacts_category ON contacts(category);

-- Speed up soft delete queries (every getAllContacts filters by is_deleted)
CREATE INDEX idx_contacts_is_deleted ON contacts(is_deleted);

-- Composite index for the most common query pattern
-- WHERE is_deleted = 0 ORDER BY name (getAllSortedByName for active contacts)
CREATE INDEX idx_contacts_active_name ON contacts(is_deleted, name);
```

**When indexes help:** Columns used in `WHERE`, `ORDER BY`, `JOIN ON` clauses with selective values (a column with 90% the same value is not very selective).

**When indexes hurt:** Every `INSERT` and `UPDATE` must also update all relevant indexes. For a table with 5 indexes, each insert does 6 write operations (1 table + 5 indexes). This is acceptable for our contact manager (low write frequency) but would be a concern for a high-throughput logging system.

### Pagination Strategy

Loading 100,000 contacts into a `JTable` at once is wasteful and slow. Pagination loads only one "page" of data at a time:

```sql
-- Page 1 (rows 1-50)
SELECT * FROM contacts WHERE is_deleted = 0 ORDER BY name LIMIT 50 OFFSET 0;

-- Page 2 (rows 51-100)
SELECT * FROM contacts WHERE is_deleted = 0 ORDER BY name LIMIT 50 OFFSET 50;

-- Page N
SELECT * FROM contacts WHERE is_deleted = 0 ORDER BY name LIMIT 50 OFFSET ((N-1) * 50);
```

The UI would add "Previous" and "Next" buttons (or a scrollable page selector). Benefits:

- **Memory:** Only 50 `Contact` objects in memory instead of 100,000
- **Network:** Only 50 rows transferred per query instead of 100,000
- **Rendering:** JTable renders 50 rows instantly instead of choking on 100,000
- **Perceived speed:** First page appears in <100ms regardless of total dataset size

### Lazy Loading Concept

Lazy loading delays data retrieval until it's actually needed. Our contact table loads only basic fields (name, number, email, category). The full contact details (address, notes, avatar — if added in future) would only be loaded when the user **selects** a row and the preview panel needs them:

```java
// Eager loading (current approach - loads everything upfront)
List<Contact> contacts = contactDAO.getAllContacts(); // All fields for all rows

// Lazy loading (optimized approach)
List<ContactSummary> summaries = contactDAO.getAllSummaries(); // Only name, number, category
// Then, when user selects a row:
Contact fullContact = contactDAO.getById(selectedId); // Full details for ONE row
```

This matters when contacts have large fields (binary avatars, long notes) that would waste memory if loaded for every row in the table.

### Connection Pooling (Conceptual)

Our current `DBConnection` uses a **single shared connection**:

```java
private static Connection connection = null; // ONE connection for everything
```

This works for a single-user desktop app but has limitations:

1. **Concurrent access:** If SwingWorker sends a query while the EDT is also querying, they share the same connection — potentially corrupting statement state
2. **Connection death:** If the connection drops, the entire app must reconnect
3. **No parallelism:** Only one SQL statement can execute at a time

**Connection pooling** (using libraries like HikariCP, Apache DBCP, or C3P0) maintains a **pool of ready-to-use connections**:

```
Pool [size=5]
├── Connection 1 — available
├── Connection 2 — in use (SwingWorker CSV import)
├── Connection 3 — available
├── Connection 4 — in use (EDT loading contacts)
└── Connection 5 — available

// Usage:
Connection conn = pool.getConnection();  // Borrows from pool (instant)
try { ... } finally {
    conn.close();  // Returns to pool (NOT actually closed)
}
```

For our single-user desktop app, pooling is optional. For a multi-user version (web app), it's **essential**.

### Caching Concept

Some data rarely changes but is queried frequently. For example, `countContacts()` is called on almost every UI operation to update the status bar, but the count only changes when contacts are added or deleted.

```java
// Without caching: hits database every time
public int countContacts() {
    // SELECT COUNT(*) FROM contacts WHERE is_deleted = 0
    // Full table scan or index scan EVERY call
    return contactDAO.countContacts();
}

// With caching: hits database only when data changes
private int cachedCount = -1; // -1 = not loaded yet

public int countContacts() {
    if (cachedCount < 0) {
        cachedCount = contactDAO.countContacts(); // Load from DB first time
    }
    return cachedCount;
}

// Invalidate cache when data changes
public void addContact(Contact c) {
    contactDAO.addContact(c);
    cachedCount = -1; // Force reload on next count query
}

public void softDeleteById(int id) {
    contactDAO.softDeleteById(id);
    cachedCount = -1; // Force reload on next count query
}
```

Caching trades **memory** for **speed**. The `cachedCount` variable uses 4 bytes but saves a database round-trip (typically 1-10ms) on every status bar update.

**Cache invalidation** is the hard part — you must clear the cache whenever the underlying data changes. Failing to invalidate causes **stale data** (showing count=50 when there are actually 51 contacts). This is why the saying goes: *"There are only two hard things in computer science: cache invalidation and naming things."*

### Avoiding Unnecessary Table Refresh

Our current code calls `loadAllContacts()` after almost every operation. This:

1. Clears all table data (`tableModel.setRowCount(0)`)
2. Queries all contacts from the database
3. Adds each contact as a new row

For 100 contacts, this takes <50ms — invisible. For 10,000 contacts, it might take 500ms — noticeable as a flicker.

**Targeted updates** modify only the affected rows:

```java
// Instead of reloading everything after an add:
private void addContactOptimized(Contact newContact) {
    contactService.addContact(newContact);
    
    // Add only the new row to the existing table model
    Object[] row = {
        newContact.getId(), newContact.getName(), newContact.getNumber(),
        newContact.getEmail(), newContact.getCategory(),
        newContact.getCreatedAt().toString()
    };
    tableModel.addRow(row); // Adds ONE row — doesn't touch existing rows
}

// Instead of reloading everything after a delete:
private void deleteContactOptimized(int modelRow) {
    int id = (int) tableModel.getValueAt(modelRow, 0);
    contactService.softDeleteById(id);
    
    tableModel.removeRow(modelRow); // Removes ONE row — doesn't touch others
}

// Instead of reloading everything after an update:
private void updateContactOptimized(int modelRow, Contact updated) {
    contactService.updateContact(updated);
    
    // Update specific cells in the existing row
    tableModel.setValueAt(updated.getName(), modelRow, 1);
    tableModel.setValueAt(updated.getNumber(), modelRow, 2);
    tableModel.setValueAt(updated.getEmail(), modelRow, 3);
    tableModel.setValueAt(updated.getCategory(), modelRow, 4);
}
```

We use `loadAllContacts()` in the current implementation for **simplicity and correctness** — it guarantees the table always matches the database. Targeted updates are an optimization to apply when table size becomes a performance concern.

---

## PHASE 30 — Final System Integration & Data Flow Explanation

### System Architecture Diagram

```
┌──────────────────────────────────────────────────────────────────┐
│                     CONTACT MANAGEMENT SYSTEM                     │
│                     Architecture Overview                         │
├──────────────────────────────────────────────────────────────────┤
│                                                                   │
│  ┌─────────────────────────── UI LAYER ──────────────────────┐   │
│  │                                                            │   │
│  │  ContactUI.java (Main Dashboard JFrame)                    │   │
│  │  ├── JToolBar (Add, Edit, Delete, Import, Export, Theme)   │   │
│  │  ├── SearchPanel (JTextField + JComboBox filter)           │   │
│  │  ├── JTable + DefaultTableModel + TableRowSorter           │   │
│  │  ├── StatisticsPanel (Total, Deleted, Categories)          │   │
│  │  ├── ContactPreviewPanel (Avatar + Details)                │   │
│  │  ├── JPopupMenu (Right-click context menu)                 │   │
│  │  └── View Menu (Column toggle, Accent color)               │   │
│  │                                                            │   │
│  │  ContactFormDialog.java (Add/Edit modal dialog)            │   │
│  │  RecycleBinDialog.java (Restore/purge deleted contacts)    │   │
│  │  ImportCSVDialog.java (CSV import with preview)            │   │
│  │                                                            │   │
│  │  Toast.java (Non-blocking floating notifications)          │   │
│  │  UITheme.java (Dark/Light theme + Accent colors)           │   │
│  │  IconFactory.java / IconRenderer.java (Visual assets)      │   │
│  │  AvatarPanel.java / EmojiLabel.java (Contact avatars)      │   │
│  │                                                            │   │
│  └──────────────────────────┬─────────────────────────────────┘   │
│                             │                                     │
│                             │ Calls methods on                    │
│                             ▼                                     │
│  ┌───────────────── SERVICE LAYER ───────────────────────────┐   │
│  │                                                            │   │
│  │  ContactService.java                                       │   │
│  │  ├── addContact(Contact)      → Validates, then delegates  │   │
│  │  ├── updateContact(Contact)   → Checks duplicates, then    │   │
│  │  │                              delegates                  │   │
│  │  ├── softDeleteById(int)      → Delegates to DAO           │   │
│  │  ├── restoreContact(int)      → Delegates to DAO           │   │
│  │  ├── search(String)           → Delegates to DAO           │   │
│  │  ├── getAllContacts()          → Delegates to DAO           │   │
│  │  ├── importFromCSV(String)    → Delegates to DAO           │   │
│  │  ├── exportToCSV(String)      → Delegates to DAO           │   │
│  │  └── validateContact(Contact) → Business rule validation   │   │
│  │                                                            │   │
│  │  AppException / ValidationException / DatabaseException    │   │
│  │  (Custom exception hierarchy for structured error handling)│   │
│  │                                                            │   │
│  └──────────────────────────┬─────────────────────────────────┘   │
│                             │                                     │
│                             │ Calls methods on                    │
│                             ▼                                     │
│  ┌─────────────────── DAO LAYER ─────────────────────────────┐   │
│  │                                                            │   │
│  │  ContactDAO.java (Interface — contract definition)         │   │
│  │  ContactDAOImpl.java (Implementation — JDBC + SQL)         │   │
│  │  ├── addContact(Contact)      → INSERT INTO contacts ...   │   │
│  │  ├── updateContact(Contact)   → UPDATE contacts SET ...    │   │
│  │  ├── softDeleteById(int)      → UPDATE ... is_deleted = 1  │   │
│  │  ├── getAllContacts()          → SELECT ... is_deleted = 0  │   │
│  │  ├── search(String)           → SELECT ... LIKE '%kw%'     │   │
│  │  ├── importFromCSV(String)    → BufferedReader + INSERT     │   │
│  │  └── exportToCSV(String)      → SELECT + BufferedWriter     │   │
│  │                                                            │   │
│  │  DatabaseException (wraps SQLException)                    │   │
│  │                                                            │   │
│  └──────────────────────────┬─────────────────────────────────┘   │
│                             │                                     │
│                             │ Uses JDBC                           │
│                             ▼                                     │
│  ┌─────────────────── UTIL LAYER ────────────────────────────┐   │
│  │                                                            │   │
│  │  DBConnection.java (Singleton JDBC connection manager)     │   │
│  │  SettingsManager.java (Java Preferences API wrapper)       │   │
│  │  AppLogger.java (java.util.logging centralized logger)     │   │
│  │                                                            │   │
│  └──────────────────────────┬─────────────────────────────────┘   │
│                             │                                     │
│                             │ TCP/IP (JDBC protocol)              │
│                             ▼                                     │
│  ┌────────────────── DATABASE LAYER ─────────────────────────┐   │
│  │                                                            │   │
│  │  MySQL Server (localhost:3306)                              │   │
│  │  └── Database: jdbcdb                                      │   │
│  │      └── Table: contacts                                   │   │
│  │          ├── id (INT, AUTO_INCREMENT, PRIMARY KEY)          │   │
│  │          ├── name (VARCHAR 50, NOT NULL)                    │   │
│  │          ├── number (VARCHAR 15, NOT NULL, UNIQUE)          │   │
│  │          ├── email (VARCHAR 50)                             │   │
│  │          ├── category (VARCHAR 20, DEFAULT 'Friends')       │   │
│  │          ├── is_deleted (BOOLEAN, DEFAULT FALSE)            │   │
│  │          ├── created_at (TIMESTAMP, DEFAULT NOW)            │   │
│  │          └── updated_at (TIMESTAMP, ON UPDATE NOW)          │   │
│  │                                                            │   │
│  └────────────────────────────────────────────────────────────┘   │
│                                                                   │
│  ┌────────────────── MODEL LAYER ────────────────────────────┐   │
│  │                                                            │   │
│  │  Contact.java (POJO — used across ALL layers)              │   │
│  │  ImportResult.java (CSV import result summary)             │   │
│  │  (Models are shared — they are the common language)        │   │
│  │                                                            │   │
│  └────────────────────────────────────────────────────────────┘   │
│                                                                   │
└──────────────────────────────────────────────────────────────────┘
```

### Data Flow: Adding a New Contact

```
User types name, phone, email, category in ContactFormDialog
        │
        │ Clicks "Save" button
        ▼
┌─ UI Layer: ContactFormDialog.saveContact() ──────────────────────┐
│  1. Reads text from JTextField fields                             │
│  2. Creates new Contact object from form data                     │
│  3. Calls contactService.addContact(contact)                      │
│  4. If success: Toast.show("✔ Contact added!"), close dialog      │
│  5. If ValidationException: show warning, highlight failing field  │
│  6. If DatabaseException: show error dialog                       │
└────────────────────────┬──────────────────────────────────────────┘
                         │
                         ▼
┌─ Service Layer: ContactService.addContact(Contact c) ────────────┐
│  1. validateContact(c) → checks empty fields, format, length      │
│     → throws ValidationException if invalid                       │
│  2. existsByName(c.getName()) → checks for duplicate name         │
│     → throws ValidationException if duplicate                     │
│  3. existsByNumber(c.getNumber()) → checks for duplicate number   │
│     → throws ValidationException if duplicate                     │
│  4. contactDAO.addContact(c) → delegates to DAO                   │
│  5. AppLogger.info("Contact added: " + c.getName())               │
└────────────────────────┬──────────────────────────────────────────┘
                         │
                         ▼
┌─ DAO Layer: ContactDAOImpl.addContact(Contact c) ────────────────┐
│  1. SQL: "INSERT INTO contacts (name, number, email, category)    │
│          VALUES (?, ?, ?, ?)"                                     │
│  2. Get Connection from DBConnection.getConnection()              │
│  3. Create PreparedStatement with the SQL                         │
│  4. Set parameters: ps.setString(1, c.getName()), etc.            │
│  5. Execute: ps.executeUpdate()                                   │
│  6. If SQLException → throw new DatabaseException(msg, e)         │
└────────────────────────┬──────────────────────────────────────────┘
                         │
                         ▼
┌─ Database Layer: MySQL ──────────────────────────────────────────┐
│  1. Receives INSERT statement via JDBC protocol                   │
│  2. Validates constraints (NOT NULL, UNIQUE, data types)          │
│  3. Generates AUTO_INCREMENT id                                   │
│  4. Sets created_at = NOW()                                       │
│  5. Writes row to disk (InnoDB storage engine)                    │
│  6. Updates indexes (PRIMARY KEY, any secondary indexes)          │
│  7. Returns affected row count (1) to JDBC driver                 │
└──────────────────────────────────────────────────────────────────┘
```

### Data Flow: Loading All Contacts (Reverse Direction)

```
ContactUI constructor or refresh button click
        │
        ▼
┌─ UI Layer: ContactUI.loadAllContacts() ──────────────────────────┐
│  1. tableModel.setRowCount(0)  → clear all existing rows          │
│  2. contacts = contactService.getAllContacts()                     │
│  3. For each Contact c in the list:                               │
│     → Extract fields: c.getId(), c.getName(), etc.                │
│     → Create Object[] row array                                   │
│     → tableModel.addRow(row)  → JTable auto-repaints              │
│  4. updateStatusBar() → shows "Total: X | Deleted: Y"            │
│  5. updateEmptyState() → shows empty panel if 0 contacts          │
└────────────────────────┬──────────────────────────────────────────┘
                         │ (step 2)
                         ▼
┌─ Service Layer: ContactService.getAllContacts() ─────────────────┐
│  1. return contactDAO.getAllContacts()                             │
│  (In this case, Service is a pass-through — no extra logic)       │
│  (Service layer still exists because future versions may add:     │
│   - Caching                                                       │
│   - Access control                                                │
│   - Audit logging                                                 │
│   - Data transformation)                                          │
└────────────────────────┬──────────────────────────────────────────┘
                         │
                         ▼
┌─ DAO Layer: ContactDAOImpl.getAllContacts() ──────────────────────┐
│  1. SQL: "SELECT * FROM contacts WHERE is_deleted = 0             │
│          ORDER BY created_at DESC"                                │
│  2. Get Connection from DBConnection                              │
│  3. Execute PreparedStatement                                     │
│  4. ResultSet rs = ps.executeQuery()                              │
│  5. While rs.next():                                              │
│     → Create new Contact()                                        │
│     → contact.setId(rs.getInt("id"))                              │
│     → contact.setName(rs.getString("name"))                       │
│     → ... (map all columns to Contact fields)                     │
│     → Add to List<Contact>                                        │
│  6. Close resources (try-with-resources)                          │
│  7. Return List<Contact>                                          │
└────────────────────────┬──────────────────────────────────────────┘
                         │
                         ▼
┌─ Database Layer: MySQL ──────────────────────────────────────────┐
│  1. Receives SELECT statement                                     │
│  2. Query optimizer checks indexes                                │
│  3. Reads rows from disk where is_deleted = 0                     │
│  4. Sorts by created_at DESC                                      │
│  5. Returns ResultSet via JDBC protocol                           │
│  6. Each rs.next() fetches the next row from the result           │
└──────────────────────────────────────────────────────────────────┘
```

### Why Separation of Concerns Matters

Each layer has a **single responsibility** and **hides its implementation details** from other layers:

**UI Layer knows:**
- How to create buttons, tables, dialogs
- How to validate form input visually (red borders, focus)
- How to display Contact data in JTable rows
- How to call ContactService methods

**UI Layer does NOT know:**
- What database engine is used (MySQL? PostgreSQL? SQLite?)
- What SQL syntax is needed
- What JDBC driver to load
- How connections are managed

**Service Layer knows:**
- Business validation rules (name ≤ 50 chars, unique numbers)
- Which DAO methods to call for each operation
- When to log events

**Service Layer does NOT know:**
- How data is displayed (Swing? Web? CLI?)
- What SQL is executed
- How connections are pooled

**DAO Layer knows:**
- SQL syntax for MySQL
- How to map ResultSet columns to Contact fields
- How to use PreparedStatement for safe queries
- JDBC connection management

**DAO Layer does NOT know:**
- Why a contact is being added (UI button? CSV import? API call?)
- Business validation rules
- How errors will be displayed

This separation means you can:

1. **Replace the database** by writing a new `ContactDAOImpl` for PostgreSQL — the Service and UI layers don't change
2. **Replace the UI** by building a web frontend — the Service and DAO layers don't change
3. **Add a REST API** that calls the same `ContactService` — the DAO layer doesn't change
4. **Unit test each layer** in isolation using mock objects

### How Each Layer Protects the Other

```
UI Layer
├── PROTECTS USER FROM: raw SQL errors, null pointer crashes, thread violations
├── TRANSLATES: Contact objects → visual table rows
└── CATCHES: ValidationException (warning dialog), DatabaseException (error dialog)

Service Layer
├── PROTECTS UI FROM: invalid data reaching the database
├── PROTECTS DAO FROM: duplicate entries, malformed input
├── TRANSLATES: raw Contact → validated Contact
└── THROWS: ValidationException (before DAO call), lets DatabaseException pass through

DAO Layer  
├── PROTECTS SERVICE FROM: raw JDBC complexity, SQL injection
├── PROTECTS DATABASE FROM: malformed queries (via PreparedStatement)
├── TRANSLATES: Contact ↔ SQL rows
└── THROWS: DatabaseException (wrapping SQLException)

Database Layer
├── PROTECTS DATA FROM: constraint violations, concurrent corruption
├── ENFORCES: NOT NULL, UNIQUE, PRIMARY KEY, AUTO_INCREMENT
└── PROVIDES: ACID guarantees (Atomicity, Consistency, Isolation, Durability)
```

### How This Design Scales

**Scaling to more features:**

Adding a "Notes" field to contacts requires changes in:
1. **Database:** `ALTER TABLE contacts ADD COLUMN notes TEXT`
2. **Model:** Add `notes` field + getter/setter to `Contact.java`
3. **DAO:** Update SQL queries to include `notes` column
4. **UI:** Add a `JTextArea` to `ContactFormDialog`

Service layer likely needs **no changes** — validation rules may be updated, but the workflow is identical. Each layer change is isolated and predictable.

**Scaling to more users (web application):**

```
Current Architecture:                    Web Architecture:
┌──────────┐                            ┌──────────────────┐
│ Swing UI │                            │   Web Frontend   │
│ (JFrame) │                            │ (React/Angular)  │
└────┬─────┘                            └────────┬─────────┘
     │                                           │ HTTP/REST
     │ Direct Java call                          ▼
     │                                  ┌──────────────────┐
     │                                  │  REST Controller  │
     │                                  │  (Spring Boot)    │
     │                                  └────────┬─────────┘
     ▼                                           │
┌────────────┐                          ┌────────▼─────────┐
│  Service   │  ← SAME CLASS! →        │    Service        │
│   Layer    │                          │     Layer         │
└────┬───────┘                          └────────┬─────────┘
     │                                           │
     ▼                                           ▼
┌────────────┐                          ┌────────────────────┐
│  DAO Layer │  ← SAME CLASS! →        │    DAO Layer        │
└────┬───────┘                          └────────┬───────────┘
     │                                           │
     ▼                                           ▼
┌────────────┐                          ┌────────────────────┐
│   MySQL    │  ← SAME DB! →           │      MySQL          │
└────────────┘                          └────────────────────┘
```

The Service and DAO layers are **100% reusable** in a web application. Only the UI layer changes (from Swing to REST controllers + web frontend). This is the ultimate payoff of layered architecture — the most complex, well-tested code (business logic, data access) never needs to be rewritten.

**How this system could evolve into a web app:**

1. Add **Spring Boot** as the web framework
2. Create `ContactController.java` with `@RestController` annotations
3. The controller calls the **same `ContactService`** methods
4. Replace `DBConnection` singleton with Spring-managed **connection pool** (HikariCP)
5. Annotate `ContactService` methods with `@Transactional` for proper transaction management
6. Build a **React/Angular/Vue** frontend that calls the REST endpoints
7. Deploy to a server — multiple users can access simultaneously

The foundation you built in Parts 1–5 maps directly to enterprise Java architecture patterns (MVC, Repository Pattern, Service Layer, DTO).

### Complete System File Map

```
ContactManagerSwing/
├── Main.java                    ← Application entry point
│
├── model/
│   ├── Contact.java             ← POJO shared across all layers
│   └── ImportResult.java        ← CSV import result container
│
├── dao/
│   ├── ContactDAO.java          ← Interface (contract)
│   ├── ContactDAOImpl.java      ← JDBC implementation
│   └── DatabaseException.java   ← DAO-level exception
│
├── service/
│   ├── ContactService.java      ← Business logic + validation
│   ├── AppException.java        ← Base custom exception
│   └── ValidationException.java ← Validation-specific exception
│
├── ui/
│   ├── ContactUI.java           ← Main dashboard (JFrame + JTable)
│   ├── ContactFormDialog.java   ← Add/Edit dialog (JDialog)
│   ├── RecycleBinDialog.java    ← Soft delete management
│   ├── ImportCSVDialog.java     ← CSV import with preview
│   ├── StatisticsPanel.java     ← Dashboard statistics bar
│   ├── ContactPreviewPanel.java ← Side panel with contact details
│   ├── Toast.java               ← Non-blocking notifications
│   ├── UITheme.java             ← Dark/Light theme engine
│   ├── IconFactory.java         ← Toolbar icon generator
│   ├── IconRenderer.java        ← Table/label icon renderer
│   ├── AvatarPanel.java         ← Contact avatar circle
│   └── EmojiLabel.java          ← Emoji-based labels
│
├── util/
│   ├── DBConnection.java        ← Singleton JDBC connection
│   ├── SettingsManager.java     ← Preferences API persistence
│   └── AppLogger.java           ← Centralized logging system
│
├── database.sql                 ← Database creation script
├── sample_contacts.csv          ← Test data for CSV import
├── compile.bat                  ← Windows build script
└── run.bat                      ← Windows run script
```

### Summary of Design Patterns Used

| Pattern | Where Used | Why |
|---------|-----------|-----|
| **MVC (Model-View-Controller)** | Model=Contact, View=ContactUI, Controller=ContactService | Separates data, display, and logic |
| **DAO (Data Access Object)** | ContactDAO + ContactDAOImpl | Isolates database operations behind an interface |
| **Singleton** | DBConnection, AppLogger | One shared instance for connection/logging |
| **Observer** | JTable ListSelectionListener, ActionListener | UI components react to events without tight coupling |
| **Flyweight** | JTable cell renderer | One renderer instance reused for all cells |
| **Template Method** | SwingWorker (doInBackground → done) | Framework defines workflow, we fill in specifics |
| **Strategy** | ContactDAO interface | DAO implementation can be swapped (MySQL → PostgreSQL) |
| **Facade** | ContactService | Simplifies complex DAO operations into clean methods |
| **Factory** | IconFactory | Creates icon objects without exposing creation logic |

### What You've Built

Over these 30 phases across 5 parts, you have built a **complete, professional-quality desktop application** that demonstrates:

- **Database design** with schema creation, constraints, and soft delete
- **Layered architecture** with proper separation of concerns
- **JDBC data access** with PreparedStatement and SQL injection prevention
- **Service layer** with business validation and duplicate detection
- **Swing UI** with JTable, JToolBar, dialogs, menus, and keyboard shortcuts
- **Theme system** with dark/light mode and accent colors
- **Toast notifications** replacing modal dialogs for success feedback
- **Animated row highlight** for visual action confirmation
- **Column visibility toggle** for customizable data views
- **Context menu** with clipboard integration
- **SwingWorker** for responsive background processing
- **Settings persistence** using Java Preferences API
- **Professional logging** replacing System.out.println
- **Custom exception hierarchy** for structured error handling
- **Performance concepts** including indexing, pagination, and caching

This is not just a contact manager — it is a **reference architecture** for building any data-driven desktop application in Java.

---

**README_PART_5 Complete. Full Project Documentation Finished.**
