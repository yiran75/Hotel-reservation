import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

public class RoomForm {
    private DefaultTableModel tableModel;
    private JTable table;
    private JTextField txtId, txtPrice;
    private JComboBox<Room.RoomType> comboType;
    private JCheckBox chkAvailable;
    private List<Room> rooms;
    private int selectedRow = -1;

    public JPanel getPanel()
    {
        rooms = FileHandler.loadFromFile("rooms.dat");

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel inputPanel = new JPanel(new GridLayout(5, 2, 5, 10));
        inputPanel.add(new JLabel("Room ID:"));
        txtId = new JTextField();
        inputPanel.add(txtId);

        inputPanel.add(new JLabel("Type:"));
        comboType = new JComboBox<>(Room.RoomType.values());
        inputPanel.add(comboType);

        inputPanel.add(new JLabel("Price/Night ($):"));
        txtPrice = new JTextField();
        inputPanel.add(txtPrice);

        inputPanel.add(new JLabel("Available:"));
        chkAvailable = new JCheckBox();
        chkAvailable.setSelected(true);
        inputPanel.add(chkAvailable);

        JButton btnSave = new JButton("Save");
        JButton btnEdit = new JButton("Edit");
        JButton btnDelete = new JButton("Delete");

        btnSave.addActionListener(this::saveRoom);
        btnEdit.addActionListener(this::editRoom);
        btnDelete.addActionListener(this::deleteRoom);

        JPanel btnPanel = new JPanel(new FlowLayout());
        btnPanel.add(btnSave);
        btnPanel.add(btnEdit);
        btnPanel.add(btnDelete);

        inputPanel.add(new JLabel());
        inputPanel.add(btnPanel);

        // Table
        String[] cols = {"Room ID", "Type", "Price", "Available"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(e ->
        {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() >= 0) {
                selectedRow = table.getSelectedRow();
                Room r = rooms.get(selectedRow);
                txtId.setText(r.getRoomId());
                comboType.setSelectedItem(r.getType());
                txtPrice.setText(String.valueOf(r.getPricePerNight()));
                chkAvailable.setSelected(r.isAvailable());
            }
        });

        refreshTable();

        panel.add(inputPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private void saveRoom(ActionEvent e) {
        String id = txtId.getText().trim();
        double price;
        try {
            price = Double.parseDouble(txtPrice.getText().trim());
            if (price <= 0) throw new NumberFormatException();
        }
        catch (NumberFormatException ex)
        {
            JOptionPane.showMessageDialog(null, "Please enter a valid positive price.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (id.isEmpty())
        {
            JOptionPane.showMessageDialog(null, "Room ID cannot be empty.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (selectedRow == -1) {
            rooms.add(new Room(id, (Room.RoomType) comboType.getSelectedItem(), price));
        } else {
            Room r = rooms.get(selectedRow);
            r.setRoomId(id);
            r.setType((Room.RoomType) comboType.getSelectedItem());
            r.setPricePerNight(price);
            r.setAvailable(chkAvailable.isSelected());
        }

        FileHandler.saveToFile(rooms, "rooms.dat");
        refreshTable();
        clearFields();
        selectedRow = -1;
    }

    private void editRoom(ActionEvent e) {
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(null, "Please select a room to edit.", "No Selection", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void deleteRoom(ActionEvent e)
    {
        if (selectedRow == -1)
        {
            JOptionPane.showMessageDialog(null, "Please select a room to delete.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        rooms.remove(selectedRow);
        FileHandler.saveToFile(rooms, "rooms.dat");
        refreshTable();
        clearFields();
        selectedRow = -1;
    }

    private void refreshTable()
    {
        tableModel.setRowCount(0);
        for (Room r : rooms)
        {
            tableModel.addRow(new Object[]{
                    r.getRoomId(),
                    r.getType(),
                    r.getPricePerNight(),
                    r.isAvailable() ? "Yes" : "No"
            });
        }
    }

    private void clearFields()
    {
        txtId.setText("");
        txtPrice.setText("");
        comboType.setSelectedIndex(0);
        chkAvailable.setSelected(true);
    }
}