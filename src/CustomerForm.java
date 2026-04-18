package view;

import command.CommandInvoker;
import command.CustomerCommands;
import dao.CustomerDAO;
import enums.IdType;
import model.Customer;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Date;

import static view.UITheme.*;

/**
 * Customer management form.
 * - JTable auto-refreshes via Observer pattern (CustomerTableModel)
 * - Save/Edit/Delete actions are dispatched through the Command pattern (CommandInvoker)
 * - Date is captured using JSpinner with SpinnerDateModel (date picker)
 */
public class CustomerForm extends JPanel {

    private final CustomerDAO dao;
    private final CommandInvoker invoker;
    private final CustomerTableModel tableModel;

    // Form fields
    private JTextField txtFirstName, txtLastName, txtEmail, txtPhone, txtAddress, txtIdNumber;
    private JComboBox<IdType> cmbIdType;
    private JSpinner spnDob;
    private JLabel lblStatus;

    // Table
    private JTable table;

    // Edit mode state
    private Customer editingCustomer = null;
    private JButton btnSave, btnEdit, btnDelete, btnClear;

    public CustomerForm(CustomerDAO dao, CommandInvoker invoker) {
        this.dao = dao;
        this.invoker = invoker;
        this.tableModel = new CustomerTableModel(dao);
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(0, 0));
        setBackground(BG_PANEL);

        // ── Header ────────────────────────────────────────────────────────────
        JPanel header = buildHeader();
        add(header, BorderLayout.NORTH);

        // ── Center: form + table split ────────────────────────────────────────
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, buildFormPanel(), buildTablePanel());
        split.setResizeWeight(0.40);
        split.setDividerSize(4);
        split.setBackground(BG_DARK);
        split.setBorder(null);
        add(split, BorderLayout.CENTER);

        // ── Status bar ────────────────────────────────────────────────────────
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

        JLabel title = new JLabel("Guest Management");
        title.setFont(FONT_TITLE);
        title.setForeground(ACCENT_GOLD);

        JLabel subtitle = new JLabel("Register and manage hotel guests");
        subtitle.setFont(FONT_SUBTITLE);
        subtitle.setForeground(TEXT_SECONDARY);

        JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 2));
        textPanel.setBackground(BG_DARK);
        textPanel.add(title);
        textPanel.add(subtitle);
        p.add(textPanel, BorderLayout.WEST);

        // Gold divider line
        JPanel divider = new JPanel();
        divider.setBackground(ACCENT_GOLD_DIM);
        divider.setPreferredSize(new Dimension(0, 1));
        p.add(divider, BorderLayout.SOUTH);
        return p;
    }

    private JPanel buildFormPanel() {
        JPanel outer = new JPanel(new BorderLayout(0, 8));
        outer.setBackground(BG_PANEL);
        outer.setBorder(BorderFactory.createEmptyBorder(14, 18, 10, 18));

        JPanel grid = new JPanel(new GridBagLayout());
        grid.setBackground(BG_PANEL);
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(5, 6, 5, 6);
        gc.fill = GridBagConstraints.HORIZONTAL;

        // Row 0 – First Name, Last Name
        addFormRow(grid, gc, 0, "First Name *", txtFirstName = makeField(), "Last Name *", txtLastName = makeField());

        // Row 1 – Email, Phone
        addFormRow(grid, gc, 1, "Email Address *", txtEmail = makeField(), "Phone Number", txtPhone = makeField());

        // Row 2 – ID Type, ID Number
        cmbIdType = makeComboBox(IdType.values());
        addFormRow(grid, gc, 2, "ID Type", cmbIdType, "ID Number", txtIdNumber = makeField());

        // Row 3 – Date of Birth (date picker), Address
        spnDob = makeDateSpinner();
        addFormRow(grid, gc, 3, "Date of Birth", spnDob, "Address", txtAddress = makeField());

        outer.add(grid, BorderLayout.CENTER);
        outer.add(buildButtonPanel(), BorderLayout.SOUTH);
        return outer;
    }

    /** Helper to add a 2-column form row with labels. */
    @SuppressWarnings("unchecked")
    private void addFormRow(JPanel grid, GridBagConstraints gc, int row,
                             String lbl1, JComponent comp1, String lbl2, JComponent comp2) {
        gc.gridy = row;
        gc.weightx = 0.15; gc.gridx = 0;
        JLabel l1 = makeLabel(lbl1); l1.setHorizontalAlignment(SwingConstants.RIGHT);
        grid.add(l1, gc);
        gc.weightx = 0.35; gc.gridx = 1; grid.add(comp1, gc);
        gc.weightx = 0.15; gc.gridx = 2;
        JLabel l2 = makeLabel(lbl2); l2.setHorizontalAlignment(SwingConstants.RIGHT);
        grid.add(l2, gc);
        gc.weightx = 0.35; gc.gridx = 3; grid.add(comp2, gc);
    }

    private JPanel buildButtonPanel() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 6));
        p.setBackground(BG_PANEL);

        btnClear  = makeSecondaryButton("Clear");
        btnDelete = makeDangerButton("Delete");
        btnEdit   = makeSecondaryButton("Edit");
        btnSave   = makePrimaryButton("Save Guest");

        btnDelete.setEnabled(false);
        btnEdit.setEnabled(false);

        btnClear.addActionListener(e -> clearForm());
        btnSave.addActionListener(e -> handleSave());
        btnEdit.addActionListener(e -> handleEdit());
        btnDelete.addActionListener(e -> handleDelete());

        p.add(btnClear);
        p.add(btnDelete);
        p.add(btnEdit);
        p.add(btnSave);
        return p;
    }

    private JPanel buildTablePanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(BG_CARD);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(0, 18, 14, 18),
            sectionBorder("Guest Records")
        ));

        table = new JTable(tableModel);
        styleTable(table);

        // Click on row populates form for editing
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) onRowSelected();
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBackground(BG_CARD);
        scroll.getViewport().setBackground(BG_CARD);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER_SUBTLE, 1));
        p.add(scroll, BorderLayout.CENTER);
        return p;
    }

    // ── Event Handlers ────────────────────────────────────────────────────────

    private void handleSave() {
        if (!validateForm()) return;

        if (editingCustomer != null) {
            // Update existing via Command
            populateCustomer(editingCustomer);
            boolean ok = invoker.invoke(new CustomerCommands.EditCustomerCommand(dao, editingCustomer));
            setStatus(ok ? "Guest updated successfully." : "Update failed.", ok);
            if (ok) { clearForm(); }
        } else {
            // Add new via Command
            Customer c = new Customer();
            populateCustomer(c);
            boolean ok = invoker.invoke(new CustomerCommands.AddCustomerCommand(dao, c));
            setStatus(ok ? "Guest saved successfully." : "Save failed.", ok);
            if (ok) clearForm();
        }
    }

    private void handleEdit() {
        int row = table.getSelectedRow();
        if (row < 0) { setStatus("Please select a guest to edit.", false); return; }
        Customer c = tableModel.getCustomerAt(table.convertRowIndexToModel(row));
        if (c == null) return;
        editingCustomer = c;
        loadIntoForm(c);
        btnSave.setText("Update Guest");
        btnEdit.setEnabled(false);
        setStatus("Editing Guest ID: " + c.getCustomerId(), true);
    }

    private void handleDelete() {
        int row = table.getSelectedRow();
        if (row < 0) { setStatus("Please select a guest to delete.", false); return; }
        Customer c = tableModel.getCustomerAt(table.convertRowIndexToModel(row));
        if (c == null) return;
        int confirm = JOptionPane.showConfirmDialog(this,
            "Delete guest \"" + c.getFullName() + "\"?\nThis cannot be undone.",
            "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;
        boolean ok = invoker.invoke(new CustomerCommands.DeleteCustomerCommand(dao, c.getCustomerId()));
        setStatus(ok ? "Guest deleted." : "Delete failed.", ok);
        if (ok) clearForm();
    }

    private void onRowSelected() {
        boolean hasSelection = table.getSelectedRow() >= 0;
        btnEdit.setEnabled(hasSelection);
        btnDelete.setEnabled(hasSelection);
    }

    // ── Form Utilities ────────────────────────────────────────────────────────

    private boolean validateForm() {
        if (txtFirstName.getText().trim().isEmpty()) { setStatus("First name is required.", false); txtFirstName.requestFocus(); return false; }
        if (txtLastName.getText().trim().isEmpty())  { setStatus("Last name is required.", false);  txtLastName.requestFocus();  return false; }
        if (txtEmail.getText().trim().isEmpty())      { setStatus("Email is required.", false);      txtEmail.requestFocus();     return false; }
        return true;
    }

    private void populateCustomer(Customer c) {
        c.setFirstName(txtFirstName.getText().trim());
        c.setLastName(txtLastName.getText().trim());
        c.setEmail(txtEmail.getText().trim());
        c.setPhone(txtPhone.getText().trim());
        c.setAddress(txtAddress.getText().trim());
        c.setIdType((IdType) cmbIdType.getSelectedItem());
        c.setIdNumber(txtIdNumber.getText().trim());
        c.setDateOfBirth((Date) spnDob.getValue());
    }

    private void loadIntoForm(Customer c) {
        txtFirstName.setText(c.getFirstName());
        txtLastName.setText(c.getLastName());
        txtEmail.setText(c.getEmail());
        txtPhone.setText(c.getPhone());
        txtAddress.setText(c.getAddress());
        cmbIdType.setSelectedItem(c.getIdType());
        txtIdNumber.setText(c.getIdNumber());
        if (c.getDateOfBirth() != null) spnDob.setValue(c.getDateOfBirth());
    }

    private void clearForm() {
        txtFirstName.setText(""); txtLastName.setText(""); txtEmail.setText("");
        txtPhone.setText(""); txtAddress.setText(""); txtIdNumber.setText("");
        cmbIdType.setSelectedIndex(0);
        spnDob.setValue(new Date());
        editingCustomer = null;
        btnSave.setText("Save Guest");
        btnEdit.setEnabled(false);
        btnDelete.setEnabled(false);
        table.clearSelection();
        lblStatus.setText("Ready");
        lblStatus.setForeground(TEXT_SECONDARY);
    }

    private void setStatus(String msg, boolean ok) {
        lblStatus.setText(msg);
        lblStatus.setForeground(ok ? SUCCESS_GREEN : ERROR_RED);
    }

    static void styleTable(JTable t) {
        t.setFont(FONT_TABLE);
        t.setForeground(TEXT_PRIMARY);
        t.setBackground(BG_CARD);
        t.setSelectionBackground(ACCENT_GOLD_DIM);
        t.setSelectionForeground(TEXT_PRIMARY);
        t.setGridColor(BORDER_SUBTLE);
        t.setRowHeight(28);
        t.setShowGrid(true);
        t.setIntercellSpacing(new Dimension(0, 1));
        t.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        t.setFillsViewportHeight(true);

        // Sortable
        t.setAutoCreateRowSorter(true);

        // Header
        JTableHeader header = t.getTableHeader();
        header.setFont(FONT_HEADER);
        header.setBackground(BG_DARK);
        header.setForeground(ACCENT_GOLD);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, ACCENT_GOLD_DIM));
        header.setReorderingAllowed(false);

        // Alternating row colors
        t.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int col) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                setFont(FONT_TABLE);
                setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                if (isSelected) {
                    setBackground(ACCENT_GOLD_DIM);
                    setForeground(TEXT_PRIMARY);
                } else {
                    setBackground(row % 2 == 0 ? BG_CARD : BG_FIELD);
                    setForeground(TEXT_PRIMARY);
                }
                return this;
            }
        });
    }
}
