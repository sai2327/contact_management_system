package ui;

import model.Contact;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * QRCodeDialog
 *
 * Generates and displays a QR code for a contact.
 * QR content is a vCard (standard format readable by any phone scanner).
 *
 * Requires: ZXing core JAR in lib/ folder.
 *   Download: https://repo1.maven.org/maven2/com/google/zxing/core/3.5.3/core-3.5.3.jar
 *   Place at:  <workspace>/lib/core-3.5.3.jar
 */
public class QRCodeDialog extends JDialog {

    private static final int QR_SIZE = 300;

    public QRCodeDialog(Frame parent, Contact contact) {
        super(parent, "QR Code - " + contact.getName(), true);
        buildUI(contact);
        pack();
        setResizable(false);
        setLocationRelativeTo(parent);
    }

    private void buildUI(Contact contact) {
        Color bg = UITheme.getPanelBackground();
        Color fg = UITheme.getForeground();

        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBackground(bg);
        root.setBorder(new EmptyBorder(20, 20, 16, 20));
        setContentPane(root);

        // ── Title ──
        JLabel lblTitle = new JLabel(contact.getName(), SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(fg);
        root.add(lblTitle, BorderLayout.NORTH);

        // ── QR Image ──
        JLabel qrLabel;
        try {
            BufferedImage qrImg = generateQR(buildVCard(contact), QR_SIZE);
            qrLabel = new JLabel(new ImageIcon(qrImg));
        } catch (Exception e) {
            // ZXing not available or error — show a clear message
            qrLabel = new JLabel(
                "<html><center><b>QR generation failed.</b><br><br>"
                + "Please download the ZXing core JAR:<br>"
                + "<tt>core-3.5.3.jar</tt><br><br>"
                + "URL (copy into browser):<br>"
                + "<tt>repo1.maven.org/maven2/com/google/<br>zxing/core/3.5.3/core-3.5.3.jar</tt><br><br>"
                + "Place the jar inside:<br>"
                + "<tt>java project/lib/</tt></center></html>",
                SwingConstants.CENTER);
            qrLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            qrLabel.setForeground(Color.RED);
            qrLabel.setPreferredSize(new Dimension(QR_SIZE, QR_SIZE));
        }

        JPanel imgPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        imgPanel.setBackground(Color.WHITE); // QR always on white background
        imgPanel.setBorder(BorderFactory.createLineBorder(UITheme.getBorderColor(), 1));
        imgPanel.add(qrLabel);
        root.add(imgPanel, BorderLayout.CENTER);

        // ── Info text ──
        JLabel lblInfo = new JLabel("Scan with any phone camera to get contact details.", SwingConstants.CENTER);
        lblInfo.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblInfo.setForeground(UITheme.isDarkMode() ? new Color(180, 180, 180) : new Color(100, 100, 100));

        // ── Close button ──
        JButton btnClose = new JButton("Close");
        btnClose.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnClose.addActionListener(e -> dispose());

        JPanel southPanel = new JPanel(new BorderLayout(0, 8));
        southPanel.setBackground(bg);
        southPanel.add(lblInfo, BorderLayout.NORTH);
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnRow.setBackground(bg);
        btnRow.add(btnClose);
        southPanel.add(btnRow, BorderLayout.SOUTH);

        root.add(southPanel, BorderLayout.SOUTH);
    }

    // ─────────────────────────────────────────────────
    //  QR generation (ZXing core JAR)
    // ─────────────────────────────────────────────────

    /**
     * Generate a QR code as a BufferedImage.
     * Uses reflection so the code compiles even before ZXing is on the classpath.
     */
    private static BufferedImage generateQR(String content, int size) throws Exception {
        // Use reflection to avoid hard compile-time dependency on ZXing
        Class<?> writerClass   = Class.forName("com.google.zxing.qrcode.QRCodeWriter");
        Class<?> formatClass   = Class.forName("com.google.zxing.BarcodeFormat");
        Class<?> matrixClass   = Class.forName("com.google.zxing.common.BitMatrix");

        Object writer       = writerClass.getDeclaredConstructor().newInstance();
        Object qrFormat     = formatClass.getField("QR_CODE").get(null);

        // encode(String contents, BarcodeFormat format, int width, int height)
        Object matrix = writerClass.getMethod("encode",
                String.class, formatClass, int.class, int.class)
                .invoke(writer, content, qrFormat, size, size);

        // Convert BitMatrix → BufferedImage manually (no javase jar needed)
        int w = (int) matrixClass.getMethod("getWidth").invoke(matrix);
        int h = (int) matrixClass.getMethod("getHeight").invoke(matrix);

        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                boolean black = (boolean) matrixClass.getMethod("get", int.class, int.class)
                        .invoke(matrix, x, y);
                img.setRGB(x, y, black ? 0x000000 : 0xFFFFFF);
            }
        }
        return img;
    }

    // ─────────────────────────────────────────────────
    //  vCard builder  — readable by all phone cameras
    // ─────────────────────────────────────────────────

    private static String buildVCard(Contact c) {
        StringBuilder sb = new StringBuilder();
        sb.append("BEGIN:VCARD\n");
        sb.append("VERSION:3.0\n");
        sb.append("FN:").append(safe(c.getName())).append("\n");
        sb.append("TEL:").append(safe(c.getNumber())).append("\n");
        if (c.getEmail() != null && !c.getEmail().isEmpty()) {
            sb.append("EMAIL:").append(c.getEmail()).append("\n");
        }
        if (c.getCategory() != null) {
            sb.append("CATEGORIES:").append(c.getCategory()).append("\n");
        }
        sb.append("END:VCARD");
        return sb.toString();
    }

    private static String safe(String s) {
        return s == null ? "" : s.trim();
    }
}
