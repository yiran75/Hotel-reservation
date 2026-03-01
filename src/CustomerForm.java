import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

public class CustomerForm {
    private DefaultTableModel tableModel;
    private JTable table;
    private JTextField txtId, txtName, txtEmail, txtPhone;
    private List<Customer> customers;
    private int selectedRow = -1;

    public JPanel getPanel() {
        customers = FileHandler.loadFromFile("customers.dat");

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel inputPanel = new JPanel(new GridLayout(5, 2, 5, 10));
        inputPanel.add(new JLabel("Customer ID:"));
        txtId = new JTextField();
        inputPanel.add(txtId);

        inputPanel.add(new JLabel("Name:"));
        txtName = new JTextField();
        inputPanel.add(txtName);

        inputPanel.add(new JLabel("Email:"));
        txtEmail = new JTextField();
        inputPanel.add(txtEmail);

        inputPanel.add(new JLabel("Phone:"));
        txtPhone = new JTextField();
        inputPanel.add(txtPhone);

        JButton btnSave = new JButton("Save");
        JButton btnEdit = new JButton("Edit");
        JButton btnDelete = new JButton("Delete");

        btnSave.addActionListener(this::saveCustomer);
        btnEdit.addActionListener(this::editCustomer);
        btnDelete.addActionListener(this::deleteCustomer);

        JPanel btnPanel = new JPanel(new FlowLayout());
        btnPanel.add(btnSave);
        btnPanel.add(btnEdit);
        btnPanel.add(btnDelete);

        inputPanel.add(new JLabel());
        inputPanel.add(btnPanel);

        String[] cols = {"ID", "Name", "Email", "Phone"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() >= 0) {
                selectedRow = table.getSelectedRow();
                Customer c = customers.get(selectedRow);
                txtId.setText(c.getCustomerId());
                txtName.setText(c.getName());
                txtEmail.setText(c.getEmail());
                txtPhone.setText(c.getPhone());
            }
        });

        refreshTable();

        panel.add(inputPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private void saveCustomer(ActionEvent e) {
        String id = txtId.getText().trim();
        String name = txtName.getText().trim();
        String email = txtEmail.getText().trim();
        String phone = txtPhone.getText().trim();

        if (id.isEmpty() || name.isEmpty() || email.isEmpty() || phone.isEmpty()) {
            JOptionPane.showMessageDialog(null, "All fields are required.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (selectedRow == -1) {
            customers.add(new Customer(id, name, email, phone));
        } else {
            Customer c = customers.get(selectedRow);
            c.setCustomerId(id);
            c.setName(name);
            c.setEmail(email);
            c.setPhone(phone);
        }

        FileHandler.saveToFile(customers, "customers.dat");
        refreshTable();
        clearFields();
        selectedRow = -1;
    }

    private void editCustomer(ActionEvent e) {
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(null, "Select a customer to edit.", "No Selection", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void deleteCustomer(ActionEvent e) {
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(null, "Select a customer to delete.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        customers.remove(selectedRow);
        FileHandler.saveToFile(customers, "customers.dat");
        refreshTable();
        clearFields();
        selectedRow = -1;
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        for (Customer c : customers) {
            tableModel.addRow(new Object[]{
                    c.getCustomerId(), c.getName(), c.getEmail(), c.getPhone()
            });
        }
    }

    private void clearFields() {
        txtId.setText("");
        txtName.setText("");
        txtEmail.setText("");
        txtPhone.setText("");
    }
}