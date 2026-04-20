package view;

import command.CommandInvoker;
import dao.CustomerDAO;
import dao.ReservationDAO;
import dao.RoomDAO;
import model.*;

import javax.swing.*;
import java.awt.*;
import java.util.Date;

import static view.UITheme.*;

/**
 * Main application window — JFrame with custom tab bar hosting all three forms.
 * Bootstraps DAOs, CommandInvoker, and seeds sample data on first run.
 */
public class MainFrame extends JFrame {

    private final CustomerDAO    customerDAO;
    private final RoomDAO        roomDAO;
    private final ReservationDAO reservationDAO;
    private final CommandInvoker invoker;

    public MainFrame() {
        customerDAO    = new CustomerDAO();
        roomDAO        = new RoomDAO();
        reservationDAO = new ReservationDAO();
        invoker        = new CommandInvoker();

        seedSampleDataIfEmpty();
        initFrame();
    }

    private void initFrame() {
        setTitle("Grand Lumière — Hotel Management System");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1100, 760);
        setMinimumSize(new Dimension(900, 650));
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_DARK);

        // ── Header Banner ─────────────────────────────────────────────────────
        JPanel banner = buildBanner();
        add(banner, BorderLayout.NORTH);

        // ── Tabbed Content ────────────────────────────────────────────────────
        JTabbedPane tabs = buildTabs();
        add(tabs, BorderLayout.CENTER);

        setVisible(true);
    }

    private JPanel buildBanner() {
        JPanel p = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                // Subtle horizontal gold gradient strip
                for (int x = 0; x < getWidth(); x++) {
                    float t = (float) x / getWidth();
                    int r = (int)(18 + t * 8);
                    int gr = (int)(18 + t * 5);
                    int b = (int)(24 + t * 10);
                    g2.setColor(new Color(r, gr, b));
                    g2.drawLine(x, 0, x, getHeight());
                }
            }
        };
        p.setPreferredSize(new Dimension(0, 64));
        p.setBorder(BorderFactory.createEmptyBorder(0, 24, 0, 24));

        JLabel brand = new JLabel("✦  GRAND LUMIÈRE");
        brand.setFont(new Font("Georgia", Font.PLAIN, 20));
        brand.setForeground(ACCENT_GOLD);

        JLabel tagline = new JLabel("Hotel Management System  |  v1.0");
        tagline.setFont(FONT_SUBTITLE);
        tagline.setForeground(TEXT_HINT);

        JPanel left = new JPanel(new GridLayout(2, 1, 0, 2));
        left.setOpaque(false);
        left.add(brand); left.add(tagline);
        p.add(left, BorderLayout.WEST);

        // Bottom gold rule
        JPanel rule = new JPanel();
        rule.setBackground(ACCENT_GOLD_DIM);
        rule.setPreferredSize(new Dimension(0, 1));
        p.add(rule, BorderLayout.SOUTH);

        return p;
    }

    private JTabbedPane buildTabs() {
        JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP);
        tabs.setBackground(BG_DARK);
        tabs.setForeground(TEXT_SECONDARY);
        tabs.setFont(FONT_HEADER);
        tabs.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));

        // Override UI for dark tab appearance
        UIManager.put("TabbedPane.background",          BG_DARK);
        UIManager.put("TabbedPane.foreground",          TEXT_SECONDARY);
        UIManager.put("TabbedPane.selected",            BG_PANEL);
        UIManager.put("TabbedPane.selectedForeground",  ACCENT_GOLD);
        UIManager.put("TabbedPane.contentBorderInsets", new Insets(0, 0, 0, 0));
        tabs.updateUI();

        // ── Tab 1: Guests ─────────────────────────────────────────────────────
        CustomerForm customerForm = new CustomerForm(customerDAO, invoker);
        tabs.addTab("  Guests  ", customerForm);

        // ── Tab 2: Rooms ──────────────────────────────────────────────────────
        RoomForm roomForm = new RoomForm(roomDAO, invoker);
        tabs.addTab("  Rooms  ", roomForm);

        // ── Tab 3: Reservations ───────────────────────────────────────────────
        ReservationForm reservationForm = new ReservationForm(reservationDAO, customerDAO, roomDAO, invoker);
        tabs.addTab("  Reservations  ", reservationForm);

        // ── Tab 4: Dashboard ──────────────────────────────────────────────────
        tabs.addTab("  Dashboard  ", buildDashboard());

        return tabs;
    }

    private JPanel buildDashboard() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(BG_PANEL);
        p.setBorder(BorderFactory.createEmptyBorder(24, 32, 24, 32));

        JLabel title = new JLabel("System Dashboard");
        title.setFont(FONT_TITLE); title.setForeground(ACCENT_GOLD);
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));
        p.add(title, BorderLayout.NORTH);

        JPanel cards = new JPanel(new GridLayout(2, 3, 16, 16));
        cards.setBackground(BG_PANEL);
        cards.add(statCard("Total Guests",       String.valueOf(customerDAO.findAll().size()),   STATUS_BLUE));
        cards.add(statCard("Total Rooms",         String.valueOf(roomDAO.findAll().size()),       ACCENT_GOLD));
        cards.add(statCard("Reservations",        String.valueOf(reservationDAO.findAll().size()), SUCCESS_GREEN));
        cards.add(statCard("Available Rooms",     String.valueOf(roomDAO.findAvailable().size()), SUCCESS_GREEN));
        cards.add(statCard("Architecture",        "MVC + DAO",                                   ACCENT_GOLD));
        cards.add(statCard("Design Patterns",     "Observer · Command",                          TEXT_SECONDARY));
        p.add(cards, BorderLayout.CENTER);

        // Architecture legend
        JPanel legend = new JPanel(new GridLayout(0, 1, 0, 4));
        legend.setBackground(BG_PANEL);
        legend.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        legend.add(legendItem("Model Layer",    "Customer · Room (abstract) · Reservation — all implement Serializable"));
        legend.add(legendItem("DAO Layer",      "CustomerDAO · RoomDAO · ReservationDAO — sequential-access .dat files"));
        legend.add(legendItem("Observer Pattern", "DAOs implement DataObservable; TableModels implement DataObserver → auto-refresh JTable"));
        legend.add(legendItem("Command Pattern",  "All CRUD actions wrapped in Command objects; dispatched via CommandInvoker"));
        legend.add(legendItem("Polymorphism",    "StandardRoom · DeluxeRoom · SuiteRoom extend abstract Room"));
        p.add(legend, BorderLayout.SOUTH);
        return p;
    }

    private JPanel statCard(String label, String value, Color accent) {
        JPanel c = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(accent);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 12, 12);
                g2.dispose();
            }
        };
        c.setOpaque(false);
        c.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
        JLabel val = new JLabel(value, SwingConstants.CENTER);
        val.setFont(new Font("Georgia", Font.PLAIN, 28));
        val.setForeground(accent);
        JLabel lbl = new JLabel(label, SwingConstants.CENTER);
        lbl.setFont(FONT_LABEL);
        lbl.setForeground(TEXT_SECONDARY);
        c.add(val, BorderLayout.CENTER);
        c.add(lbl, BorderLayout.SOUTH);
        return c;
    }

    private JLabel legendItem(String key, String desc) {
        JLabel l = new JLabel("<html><b style='color:#C4A05A'>" + key + ":</b>  " + desc + "</html>");
        l.setFont(FONT_LABEL);
        l.setForeground(TEXT_SECONDARY);
        return l;
    }

    // ── Sample Data Seeding ───────────────────────────────────────────────────

    private void seedSampleDataIfEmpty() {
        if (!customerDAO.findAll().isEmpty()) return; // Already seeded

        // Sample customers
        Customer c1 = new Customer("Eleanor", "Vance", "e.vance@email.com", "+1-555-0101",
            "42 Maple Street, Boston MA", enums.IdType.PASSPORT, "PA1234567", new Date());
        Customer c2 = new Customer("James", "Harrington", "j.harrington@corp.com", "+1-555-0202",
            "88 Park Avenue, New York NY", enums.IdType.DRIVERS_LICENSE, "DL9876543", new Date());
        customerDAO.save(c1);
        customerDAO.save(c2);

        // Sample rooms (polymorphic)
        Room r1 = new StandardRoom(101, "101", 120.00, 2, 1,
            "Comfortable standard room with city view", true, true, "Queen");
        Room r2 = new DeluxeRoom(201, "201", 240.00, 3, 2,
            "Spacious deluxe room with balcony", true, true, false, "Garden");
        Room r3 = new SuiteRoom(301, "301", 480.00, 5, 3,
            "Luxury suite with full amenities", 3, false, true, true);
        roomDAO.save(r1); roomDAO.save(r2); roomDAO.save(r3);

        // Sample reservation linking c1 → r2
        Date checkIn  = new Date();
        Date checkOut = new Date(checkIn.getTime() + 3L * 24 * 60 * 60 * 1000);
        Reservation res = new Reservation(c1.getCustomerId(), r2.getRoomId(),
            checkIn, checkOut, 2, 720.00, 144.00, "Late check-in requested");
        reservationDAO.save(res);
    }

    // ── Entry Point ───────────────────────────────────────────────────────────

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception ignored) {}
            UITheme.applyGlobalDefaults();
            new MainFrame();
        });
    }
}
