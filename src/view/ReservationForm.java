package view;

import command.CommandInvoker;
import command.ReservationCommands;
import dao.CustomerDAO;
import dao.ReservationDAO;
import dao.RoomDAO;
import enums.ReservationStatus;
import model.Customer;
import model.Reservation;
import model.Room;

import javax.swing.*;
import java.awt.*;
import java.util.Date;
import java.util.List;

import static view.CustomerForm.styleTable;
import static view.UITheme.*;

/**
 * Reservation management form.
 * Links Customer and Room via foreign-key spinners with lookup labels.
 * Two date pickers for check-in/check-out; auto-calculates total based on room price × nights.
 * Full Observer + Command pattern integration.
 */
public class ReservationForm extends JPanel {

    private final ReservationDAO resDAO;
    private final CustomerDAO    custDAO;
    private final RoomDAO        roomDAO;
    private final CommandInvoker invoker;
    private final ReservationTableModel tableModel;

    // Form fields
    private JSpinner spnCustomerId, spnRoomId;
    private JSpinner spnCheckIn, spnCheckOut;
    private JSpinner spnGuests, spnTotal, spnDeposit;
    private JComboBox<ReservationStatus> cmbStatus;
    private JTextArea txtRequests;
    private JLabel lblCustomerName, lblRoomInfo, lblNights;

    // Table & state
    private JTable table;
    private JLabel lblStatus;
    private JButton btnSave, btnEdit, btnDelete, btnClear;
    private Reservation editingReservation = null;

    public ReservationForm(ReservationDAO resDAO, CustomerDAO custDAO,
                            RoomDAO roomDAO, CommandInvoker invoker) {
        this.resDAO   = resDAO;
        this.custDAO  = custDAO;
        this.roomDAO  = roomDAO;
        this.invoker  = invoker;
        this.tableModel = new ReservationTableModel(resDAO);
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(BG_PANEL);
        add(buildHeader(), BorderLayout.NORTH);
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
            buildFormPanel(), buildTablePanel());
        split.setResizeWeight(0.48);
        split.setDividerSize(4);
        split.setBackground(BG_DARK);
        split.setBorder(null);
        add(split, BorderLayout.CENTER);
        JPanel sb = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 4));
        sb.setBackground(BG_DARK);
        lblStatus = new JLabel("Ready");
        lblStatus.setFont(FONT_LABEL); lblStatus.setForeground(TEXT_SECONDARY);
        sb.add(lblStatus);
        add(sb, BorderLayout.SOUTH);
    }

    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(BG_DARK);
        p.setBorder(BorderFactory.createEmptyBorder(16, 20, 12, 20));
        JLabel title = new JLabel("Reservation Management");
        title.setFont(FONT_TITLE); title.setForeground(ACCENT_GOLD);
        JLabel sub = new JLabel("Book, modify and track guest reservations");
        sub.setFont(FONT_SUBTITLE); sub.setForeground(TEXT_SECONDARY);
        JPanel tp = new JPanel(new GridLayout(2, 1, 0, 2));
        tp.setBackground(BG_DARK); tp.add(title); tp.add(sub);
        p.add(tp, BorderLayout.WEST);
        JPanel div = new JPanel();
        div.setBackground(ACCENT_GOLD_DIM); div.setPreferredSize(new Dimension(0, 1));
        p.add(div, BorderLayout.SOUTH);
        return p;
    }

    private JPanel buildFormPanel() {
        JPanel outer = new JPanel(new BorderLayout(0, 6));
        outer.setBackground(BG_PANEL);
        outer.setBorder(BorderFactory.createEmptyBorder(12, 18, 8, 18));

        JPanel grid = new JPanel(new GridBagLayout());
        grid.setBackground(BG_PANEL);
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(5, 6, 5, 6);
        gc.fill = GridBagConstraints.HORIZONTAL;

        // Customer ID row + lookup label
        spnCustomerId = makeIntSpinner(1000, 99999, 1000);
        lblCustomerName = new JLabel("(enter ID above)");
        lblCustomerName.setFont(FONT_LABEL); lblCustomerName.setForeground(STATUS_BLUE);
        spnCustomerId.addChangeListener(e -> lookupCustomer());

        // Room ID row + lookup label
        spnRoomId = makeIntSpinner(100, 99999, 100);
        lblRoomInfo = new JLabel("(enter ID above)");
        lblRoomInfo.setFont(FONT_LABEL); lblRoomInfo.setForeground(STATUS_BLUE);
        spnRoomId.addChangeListener(e -> lookupRoom());

        // Dates (date pickers)
        spnCheckIn  = makeDateSpinner();
        spnCheckOut = makeDateSpinner();
        lblNights = new JLabel("0 nights");
        lblNights.setFont(FONT_LABEL); lblNights.setForeground(ACCENT_GOLD);

        // Update nights + total whenever dates change
        spnCheckIn.addChangeListener(e  -> recalculate());
        spnCheckOut.addChangeListener(e -> recalculate());

        spnGuests  = makeIntSpinner(1, 20, 1);
        spnTotal   = makeDoubleSpinner(0, 999999, 0.0, 10.0);
        spnDeposit = makeDoubleSpinner(0, 999999, 0.0, 10.0);
        cmbStatus  = makeComboBox(ReservationStatus.values());

        txtRequests = makeTextArea(2, 20);

        // Row 0: Customer ID + name
        addRow(grid, gc, 0, "Customer ID *", spnCustomerId, "Guest Name", lblCustomerName);
        // Row 1: Room ID + room info
        addRow(grid, gc, 1, "Room ID *",    spnRoomId,    "Room Info",   lblRoomInfo);
        // Row 2: Check-in + Check-out
        addRow(grid, gc, 2, "Check-In Date", spnCheckIn, "Check-Out Date", spnCheckOut);
        // Row 3: Nights + Guests
        addRow(grid, gc, 3, "Nights",       lblNights,   "No. of Guests", spnGuests);
        // Row 4: Total + Deposit
        addRow(grid, gc, 4, "Total ($)",    spnTotal,    "Deposit ($)",   spnDeposit);
        // Row 5: Status + Requests
        addRow(grid, gc, 5, "Status",       cmbStatus,
               "Special Requests", new JScrollPane(txtRequests) {{
                   setPreferredSize(new Dimension(180, 48));
                   setBorder(fieldBorder());
                   getViewport().setBackground(BG_FIELD);
               }});

        outer.add(grid,              BorderLayout.CENTER);
        outer.add(buildButtonPanel(), BorderLayout.SOUTH);
        return outer;
    }

    private void addRow(JPanel g, GridBagConstraints gc, int row,
                         String l1, JComponent c1, String l2, JComponent c2) {
        gc.gridy = row;
        gc.weightx = 0.15; gc.gridx = 0;
        JLabel lb1 = makeLabel(l1); lb1.setHorizontalAlignment(SwingConstants.RIGHT); g.add(lb1, gc);
        gc.weightx = 0.35; gc.gridx = 1; g.add(c1, gc);
        gc.weightx = 0.15; gc.gridx = 2;
        JLabel lb2 = makeLabel(l2); lb2.setHorizontalAlignment(SwingConstants.RIGHT); g.add(lb2, gc);
        gc.weightx = 0.35; gc.gridx = 3; g.add(c2, gc);
    }

    private JPanel buildButtonPanel() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 6));
        p.setBackground(BG_PANEL);
        btnClear  = makeSecondaryButton("Clear");
        btnDelete = makeDangerButton("Delete");
        btnEdit   = makeSecondaryButton("Edit");
        btnSave   = makePrimaryButton("Book Now");
        btnDelete.setEnabled(false); btnEdit.setEnabled(false);
        btnClear.addActionListener(e  -> clearForm());
        btnSave.addActionListener(e   -> handleSave());
        btnEdit.addActionListener(e   -> handleEdit());
        btnDelete.addActionListener(e -> handleDelete());
        p.add(btnClear); p.add(btnDelete); p.add(btnEdit); p.add(btnSave);
        return p;
    }

    private JPanel buildTablePanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(BG_CARD);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(0, 18, 14, 18),
            sectionBorder("Reservation Records")));
        table = new JTable(tableModel);
        styleTable(table);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                boolean sel = table.getSelectedRow() >= 0;
                btnEdit.setEnabled(sel); btnDelete.setEnabled(sel);
            }
        });
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBackground(BG_CARD); scroll.getViewport().setBackground(BG_CARD);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER_SUBTLE, 1));
        p.add(scroll, BorderLayout.CENTER);
        return p;
    }

    // ── Handlers ──────────────────────────────────────────────────────────────

    private void handleSave() {
        if (!validateForm()) return;
        if (editingReservation != null) {
            populateReservation(editingReservation);
            boolean ok = invoker.invoke(new ReservationCommands.EditReservationCommand(resDAO, editingReservation));
            setStatus(ok ? "Reservation updated." : "Update failed.", ok);
            if (ok) clearForm();
        } else {
            Reservation r = new Reservation();
            populateReservation(r);
            boolean ok = invoker.invoke(new ReservationCommands.AddReservationCommand(resDAO, r));
            setStatus(ok ? "Reservation booked successfully." : "Booking failed.", ok);
            if (ok) clearForm();
        }
    }

    private void handleEdit() {
        int row = table.getSelectedRow();
        if (row < 0) { setStatus("Select a reservation to edit.", false); return; }
        Reservation r = tableModel.getReservationAt(table.convertRowIndexToModel(row));
        if (r == null) return;
        editingReservation = r;
        loadIntoForm(r);
        btnSave.setText("Update");
        setStatus("Editing Reservation ID: " + r.getReservationId(), true);
    }

    private void handleDelete() {
        int row = table.getSelectedRow();
        if (row < 0) { setStatus("Select a reservation to delete.", false); return; }
        Reservation r = tableModel.getReservationAt(table.convertRowIndexToModel(row));
        if (r == null) return;
        int confirm = JOptionPane.showConfirmDialog(this,
            "Cancel Reservation #" + r.getReservationId() + "?",
            "Confirm Cancellation", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;
        boolean ok = invoker.invoke(new ReservationCommands.DeleteReservationCommand(resDAO, r.getReservationId()));
        setStatus(ok ? "Reservation cancelled." : "Delete failed.", ok);
        if (ok) clearForm();
    }

    // ── Lookups & calculation ─────────────────────────────────────────────────

    private void lookupCustomer() {
        int id = ((Number) spnCustomerId.getValue()).intValue();
        Customer c = custDAO.findById(id);
        if (c != null) {
            lblCustomerName.setText(c.getFullName());
            lblCustomerName.setForeground(SUCCESS_GREEN);
        } else {
            lblCustomerName.setText("Customer not found");
            lblCustomerName.setForeground(ERROR_RED);
        }
    }

    private void lookupRoom() {
        int id = ((Number) spnRoomId.getValue()).intValue();
        Room r = roomDAO.findById(id);
        if (r != null) {
            lblRoomInfo.setText(r.getRoomNumber() + " — " + r.getRoomCategory() +
                                " ($" + String.format("%.0f", r.getPricePerNight()) + "/night)");
            lblRoomInfo.setForeground(SUCCESS_GREEN);
            recalculate();
        } else {
            lblRoomInfo.setText("Room not found");
            lblRoomInfo.setForeground(ERROR_RED);
        }
    }

    private void recalculate() {
        Date in  = (Date) spnCheckIn.getValue();
        Date out = (Date) spnCheckOut.getValue();
        if (in == null || out == null) return;
        long nights = Math.max(1, (out.getTime() - in.getTime()) / (1000L * 60 * 60 * 24));
        lblNights.setText(nights + " night" + (nights != 1 ? "s" : ""));
        int roomId = ((Number) spnRoomId.getValue()).intValue();
        Room r = roomDAO.findById(roomId);
        if (r != null) {
            double total = nights * r.getPricePerNight();
            spnTotal.setValue(total);
            spnDeposit.setValue(total * 0.20); // 20% deposit default
        }
    }

    // ── Form Utilities ────────────────────────────────────────────────────────

    private boolean validateForm() {
        int custId = ((Number) spnCustomerId.getValue()).intValue();
        int roomId = ((Number) spnRoomId.getValue()).intValue();
        if (custDAO.findById(custId) == null) { setStatus("Invalid Customer ID.", false); return false; }
        if (roomDAO.findById(roomId) == null) { setStatus("Invalid Room ID.", false);     return false; }
        Date in = (Date) spnCheckIn.getValue();
        Date out = (Date) spnCheckOut.getValue();
        if (!out.after(in)) { setStatus("Check-out must be after check-in.", false); return false; }
        return true;
    }

    private void populateReservation(Reservation r) {
        r.setCustomerId(((Number) spnCustomerId.getValue()).intValue());
        r.setRoomId(((Number) spnRoomId.getValue()).intValue());
        r.setCheckInDate((Date) spnCheckIn.getValue());
        r.setCheckOutDate((Date) spnCheckOut.getValue());
        r.setNumberOfGuests(((Number) spnGuests.getValue()).intValue());
        r.setTotalAmount(((Number) spnTotal.getValue()).doubleValue());
        r.setDepositAmount(((Number) spnDeposit.getValue()).doubleValue());
        r.setStatus((ReservationStatus) cmbStatus.getSelectedItem());
        r.setSpecialRequests(txtRequests.getText().trim());
    }

    private void loadIntoForm(Reservation r) {
        spnCustomerId.setValue(r.getCustomerId());
        spnRoomId.setValue(r.getRoomId());
        if (r.getCheckInDate()  != null) spnCheckIn.setValue(r.getCheckInDate());
        if (r.getCheckOutDate() != null) spnCheckOut.setValue(r.getCheckOutDate());
        spnGuests.setValue(r.getNumberOfGuests());
        spnTotal.setValue(r.getTotalAmount());
        spnDeposit.setValue(r.getDepositAmount());
        cmbStatus.setSelectedItem(r.getStatus());
        txtRequests.setText(r.getSpecialRequests() != null ? r.getSpecialRequests() : "");
        lookupCustomer(); lookupRoom();
    }

    private void clearForm() {
        spnCustomerId.setValue(1000); spnRoomId.setValue(100);
        spnCheckIn.setValue(new Date()); spnCheckOut.setValue(new Date());
        spnGuests.setValue(1); spnTotal.setValue(0.0); spnDeposit.setValue(0.0);
        cmbStatus.setSelectedIndex(0); txtRequests.setText("");
        lblCustomerName.setText("(enter ID above)"); lblCustomerName.setForeground(STATUS_BLUE);
        lblRoomInfo.setText("(enter ID above)");     lblRoomInfo.setForeground(STATUS_BLUE);
        lblNights.setText("0 nights");
        editingReservation = null;
        btnSave.setText("Book Now");
        btnEdit.setEnabled(false); btnDelete.setEnabled(false);
        table.clearSelection();
        lblStatus.setText("Ready"); lblStatus.setForeground(TEXT_SECONDARY);
    }

    private void setStatus(String msg, boolean ok) {
        lblStatus.setText(msg);
        lblStatus.setForeground(ok ? SUCCESS_GREEN : ERROR_RED);
    }
}
