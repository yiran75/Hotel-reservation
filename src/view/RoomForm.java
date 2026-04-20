package view;

import command.CommandInvoker;
import command.RoomCommands;
import dao.RoomDAO;
import enums.RoomStatus;
import enums.RoomType;
import model.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.concurrent.atomic.AtomicInteger;

import static view.CustomerForm.styleTable;
import static view.UITheme.*;

/**
 * Room management form.
 * Dynamically swaps a sub-panel based on selected RoomType, demonstrating polymorphism.
 * Observer auto-refresh and Command pattern for all CRUD actions.
 */
public class RoomForm extends JPanel {

    private final RoomDAO dao;
    private final CommandInvoker invoker;
    private final RoomTableModel tableModel;
    private static final AtomicInteger roomIdGen = new AtomicInteger(100);

    // Common fields
    private JTextField txtRoomNumber, txtDescription;
    private JComboBox<RoomType>   cmbRoomType;
    private JComboBox<RoomStatus> cmbStatus;
    private JSpinner spnPrice, spnCapacity, spnFloor;

    // Sub-panel container
    private JPanel subPanel;
    private CardLayout subCardLayout;

    // Standard sub-fields
    private JCheckBox chkTv, chkWifi;
    private JComboBox<String> cmbBedType;

    // Deluxe sub-fields
    private JCheckBox chkBalcony, chkMiniBar, chkJacuzzi;
    private JComboBox<String> cmbView;

    // Suite sub-fields
    private JSpinner spnNumRooms;
    private JCheckBox chkPool, chkConcierge, chkKitchenette;

    // Table & state
    private JTable table;
    private JLabel lblStatus;
    private JButton btnSave, btnEdit, btnDelete, btnClear;
    private Room editingRoom = null;

    public RoomForm(RoomDAO dao, CommandInvoker invoker) {
        this.dao = dao;
        this.invoker = invoker;
        this.tableModel = new RoomTableModel(dao);
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(BG_PANEL);

        add(buildHeader(), BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, buildFormPanel(), buildTablePanel());
        split.setResizeWeight(0.45);
        split.setDividerSize(4);
        split.setBackground(BG_DARK);
        split.setBorder(null);
        add(split, BorderLayout.CENTER);

        JPanel statusBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 4));
        statusBar.setBackground(BG_DARK);
        lblStatus = new JLabel("Ready");
        lblStatus.setFont(FONT_LABEL);
        lblStatus.setForeground(TEXT_SECONDARY);
        statusBar.add(lblStatus);
        add(statusBar, BorderLayout.SOUTH);
    }

    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(BG_DARK);
        p.setBorder(BorderFactory.createEmptyBorder(16, 20, 12, 20));
        JLabel title = new JLabel("Room Management");
        title.setFont(FONT_TITLE); title.setForeground(ACCENT_GOLD);
        JLabel sub = new JLabel("Configure and track all hotel rooms");
        sub.setFont(FONT_SUBTITLE); sub.setForeground(TEXT_SECONDARY);
        JPanel tp = new JPanel(new GridLayout(2, 1, 0, 2));
        tp.setBackground(BG_DARK); tp.add(title); tp.add(sub);
        p.add(tp, BorderLayout.WEST);
        JPanel div = new JPanel(); div.setBackground(ACCENT_GOLD_DIM); div.setPreferredSize(new Dimension(0, 1));
        p.add(div, BorderLayout.SOUTH);
        return p;
    }

    private JPanel buildFormPanel() {
        JPanel outer = new JPanel(new BorderLayout(0, 6));
        outer.setBackground(BG_PANEL);
        outer.setBorder(BorderFactory.createEmptyBorder(12, 18, 8, 18));

        // Common fields
        JPanel common = new JPanel(new GridBagLayout());
        common.setBackground(BG_PANEL);
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4, 6, 4, 6);
        gc.fill = GridBagConstraints.HORIZONTAL;

        txtRoomNumber = makeField(); txtDescription = makeField();
        cmbRoomType   = makeComboBox(RoomType.values());
        cmbStatus     = makeComboBox(RoomStatus.values());
        spnPrice      = makeDoubleSpinner(0, 99999, 150.00, 10.0);
        spnCapacity   = makeIntSpinner(1, 20, 2);
        spnFloor      = makeIntSpinner(1, 100, 1);

        addRow(common, gc, 0, "Room Number *", txtRoomNumber, "Floor", spnFloor);
        addRow(common, gc, 1, "Room Type",     cmbRoomType,   "Status",   cmbStatus);
        addRow(common, gc, 2, "Price/Night ($)",spnPrice,     "Capacity", spnCapacity);
        addRow(common, gc, 3, "Description",   txtDescription, "",        new JLabel(""));

        // Dynamic sub-panel (CardLayout) for type-specific fields
        subCardLayout = new CardLayout();
        subPanel = new JPanel(subCardLayout);
        subPanel.setBackground(BG_PANEL);
        subPanel.add(buildStandardPanel(), "STANDARD");
        subPanel.add(buildDeluxePanel(),   "DELUXE");
        subPanel.add(buildSuitePanel(),    "SUITE");
        subPanel.add(new JPanel(),         "PENTHOUSE");

        // Switch card when RoomType changes
        cmbRoomType.addActionListener(e -> {
            RoomType rt = (RoomType) cmbRoomType.getSelectedItem();
            if (rt != null) subCardLayout.show(subPanel, rt.name());
        });

        outer.add(common,   BorderLayout.NORTH);
        outer.add(subPanel, BorderLayout.CENTER);
        outer.add(buildButtonPanel(), BorderLayout.SOUTH);
        return outer;
    }

    private JPanel buildStandardPanel() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 6));
        p.setBackground(BG_PANEL);
        p.setBorder(sectionBorder("Standard Options"));
        chkTv   = new JCheckBox("TV",   true);  styleCheck(chkTv);
        chkWifi = new JCheckBox("WiFi", true);  styleCheck(chkWifi);
        cmbBedType = makeComboBox(new String[]{"Single", "Double", "Queen", "King"});
        cmbBedType.setPreferredSize(new Dimension(130, 30));
        p.add(makeLabel("Bed Type:")); p.add(cmbBedType);
        p.add(chkTv); p.add(chkWifi);
        return p;
    }

    private JPanel buildDeluxePanel() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 6));
        p.setBackground(BG_PANEL);
        p.setBorder(sectionBorder("Deluxe Options"));
        chkBalcony = new JCheckBox("Balcony",  true); styleCheck(chkBalcony);
        chkMiniBar = new JCheckBox("Mini Bar", true); styleCheck(chkMiniBar);
        chkJacuzzi = new JCheckBox("Jacuzzi",  false); styleCheck(chkJacuzzi);
        cmbView = makeComboBox(new String[]{"City", "Garden", "Pool", "Ocean"});
        cmbView.setPreferredSize(new Dimension(130, 30));
        p.add(makeLabel("View:")); p.add(cmbView);
        p.add(chkBalcony); p.add(chkMiniBar); p.add(chkJacuzzi);
        return p;
    }

    private JPanel buildSuitePanel() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 6));
        p.setBackground(BG_PANEL);
        p.setBorder(sectionBorder("Suite Options"));
        spnNumRooms    = makeIntSpinner(1, 10, 2);
        spnNumRooms.setPreferredSize(new Dimension(60, 30));
        chkPool        = new JCheckBox("Private Pool", false); styleCheck(chkPool);
        chkConcierge   = new JCheckBox("Concierge",   true);  styleCheck(chkConcierge);
        chkKitchenette = new JCheckBox("Kitchenette", true);  styleCheck(chkKitchenette);
        p.add(makeLabel("Rooms:")); p.add(spnNumRooms);
        p.add(chkPool); p.add(chkConcierge); p.add(chkKitchenette);
        return p;
    }

    private void styleCheck(JCheckBox cb) {
        cb.setFont(FONT_FIELD);
        cb.setForeground(TEXT_PRIMARY);
        cb.setBackground(BG_PANEL);
        cb.setOpaque(false);
    }

    private void addRow(JPanel grid, GridBagConstraints gc, int row,
                         String l1, JComponent c1, String l2, JComponent c2) {
        gc.gridy = row;
        gc.weightx = 0.15; gc.gridx = 0; JLabel lb1 = makeLabel(l1); lb1.setHorizontalAlignment(SwingConstants.RIGHT); grid.add(lb1, gc);
        gc.weightx = 0.35; gc.gridx = 1; grid.add(c1, gc);
        gc.weightx = 0.15; gc.gridx = 2; JLabel lb2 = makeLabel(l2); lb2.setHorizontalAlignment(SwingConstants.RIGHT); grid.add(lb2, gc);
        gc.weightx = 0.35; gc.gridx = 3; grid.add(c2, gc);
    }

    private JPanel buildButtonPanel() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 6));
        p.setBackground(BG_PANEL);
        btnClear  = makeSecondaryButton("Clear");
        btnDelete = makeDangerButton("Delete");
        btnEdit   = makeSecondaryButton("Edit");
        btnSave   = makePrimaryButton("Save Room");
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
            BorderFactory.createEmptyBorder(0, 18, 14, 18), sectionBorder("Room Inventory")));
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
        if (editingRoom != null) {
            populateRoom(editingRoom);
            boolean ok = invoker.invoke(new RoomCommands.EditRoomCommand(dao, editingRoom));
            setStatus(ok ? "Room updated." : "Update failed.", ok);
            if (ok) clearForm();
        } else {
            Room r = buildRoom();
            boolean ok = invoker.invoke(new RoomCommands.AddRoomCommand(dao, r));
            setStatus(ok ? "Room saved." : "Save failed.", ok);
            if (ok) clearForm();
        }
    }

    private void handleEdit() {
        int row = table.getSelectedRow();
        if (row < 0) { setStatus("Select a room to edit.", false); return; }
        Room r = tableModel.getRoomAt(table.convertRowIndexToModel(row));
        if (r == null) return;
        editingRoom = r;
        loadIntoForm(r);
        btnSave.setText("Update Room");
        setStatus("Editing Room: " + r.getRoomNumber(), true);
    }

    private void handleDelete() {
        int row = table.getSelectedRow();
        if (row < 0) { setStatus("Select a room to delete.", false); return; }
        Room r = tableModel.getRoomAt(table.convertRowIndexToModel(row));
        if (r == null) return;
        int confirm = JOptionPane.showConfirmDialog(this,
            "Delete Room " + r.getRoomNumber() + "?",
            "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;
        boolean ok = invoker.invoke(new RoomCommands.DeleteRoomCommand(dao, r.getRoomId()));
        setStatus(ok ? "Room deleted." : "Delete failed.", ok);
        if (ok) clearForm();
    }

    // ── Form Utilities ────────────────────────────────────────────────────────

    private boolean validateForm() {
        if (txtRoomNumber.getText().trim().isEmpty()) {
            setStatus("Room number is required.", false); txtRoomNumber.requestFocus(); return false;
        }
        return true;
    }

    private Room buildRoom() {
        RoomType rt = (RoomType) cmbRoomType.getSelectedItem();
        int id = roomIdGen.getAndIncrement();
        String num = txtRoomNumber.getText().trim();
        double price = ((Number) spnPrice.getValue()).doubleValue();
        int cap = ((Number) spnCapacity.getValue()).intValue();
        int floor = ((Number) spnFloor.getValue()).intValue();
        String desc = txtDescription.getText().trim();

        Room room;
        if (rt == RoomType.STANDARD) {
            room = new StandardRoom(id, num, price, cap, floor, desc,
                chkTv.isSelected(), chkWifi.isSelected(), (String) cmbBedType.getSelectedItem());
        } else if (rt == RoomType.DELUXE) {
            room = new DeluxeRoom(id, num, price, cap, floor, desc,
                chkBalcony.isSelected(), chkMiniBar.isSelected(), chkJacuzzi.isSelected(),
                (String) cmbView.getSelectedItem());
        } else if (rt == RoomType.SUITE) {
            room = new SuiteRoom(id, num, price, cap, floor, desc,
                ((Number) spnNumRooms.getValue()).intValue(),
                chkPool.isSelected(), chkConcierge.isSelected(), chkKitchenette.isSelected());
        } else {
            room = new DeluxeRoom(id, num, price, cap, floor, desc, true, true, true, "Ocean");
        }
        room.setStatus((RoomStatus) cmbStatus.getSelectedItem());
        return room;
    }

    private void populateRoom(Room r) {
        r.setRoomNumber(txtRoomNumber.getText().trim());
        r.setPricePerNight(((Number) spnPrice.getValue()).doubleValue());
        r.setCapacity(((Number) spnCapacity.getValue()).intValue());
        r.setFloor(((Number) spnFloor.getValue()).intValue());
        r.setDescription(txtDescription.getText().trim());
        r.setStatus((RoomStatus) cmbStatus.getSelectedItem());
    }

    private void loadIntoForm(Room r) {
        txtRoomNumber.setText(r.getRoomNumber());
        txtDescription.setText(r.getDescription());
        cmbRoomType.setSelectedItem(r.getRoomType());
        cmbStatus.setSelectedItem(r.getStatus());
        spnPrice.setValue(r.getPricePerNight());
        spnCapacity.setValue(r.getCapacity());
        spnFloor.setValue(r.getFloor());
        subCardLayout.show(subPanel, r.getRoomType().name());
    }

    private void clearForm() {
        txtRoomNumber.setText(""); txtDescription.setText("");
        cmbRoomType.setSelectedIndex(0); cmbStatus.setSelectedIndex(0);
        spnPrice.setValue(150.0); spnCapacity.setValue(2); spnFloor.setValue(1);
        editingRoom = null;
        btnSave.setText("Save Room");
        btnEdit.setEnabled(false); btnDelete.setEnabled(false);
        table.clearSelection();
        lblStatus.setText("Ready"); lblStatus.setForeground(TEXT_SECONDARY);
    }

    private void setStatus(String msg, boolean ok) {
        lblStatus.setText(msg);
        lblStatus.setForeground(ok ? SUCCESS_GREEN : ERROR_RED);
    }
}
