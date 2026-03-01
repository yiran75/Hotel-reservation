import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

public class ReservationForm {
    private DefaultTableModel tableModel;
    private JTable table;
    private JTextField txtResId, txtCustId, txtRoomId, txtCheckIn, txtCheckOut;
    private JComboBox<Reservation.Status> comboStatus;
    private List<Reservation> reservations;
    private int selectedRow = -1;

    public JPanel getPanel() {
        reservations = FileHandler.loadFromFile("reservations.dat");

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel inputPanel = new JPanel(new GridLayout(7, 2, 5, 8));
        inputPanel.add(new JLabel("Reservation ID:"));
        txtResId = new JTextField();
        inputPanel.add(txtResId);

        inputPanel.add(new JLabel("Customer ID:"));
        txtCustId = new JTextField();
        inputPanel.add(txtCustId);

        inputPanel.add(new JLabel("Room ID:"));
        txtRoomId = new JTextField();
        inputPanel.add(txtRoomId);

        inputPanel.add(new JLabel("Check-in (YYYY-MM-DD):"));
        txtCheckIn = new JTextField();
        inputPanel.add(txtCheckIn);

        inputPanel.add(new JLabel("Check-out (YYYY-MM-DD):"));
        txtCheckOut = new JTextField();
        inputPanel.add(txtCheckOut);

        inputPanel.add(new JLabel("Status:"));
        comboStatus = new JComboBox<>(Reservation.Status.values());
        inputPanel.add(comboStatus);

        JButton btnSave = new JButton("Save");
        JButton btnEdit = new JButton("Edit");
        JButton btnDelete = new JButton("Delete");

        btnSave.addActionListener(this::saveReservation);
        btnEdit.addActionListener(this::editReservation);
        btnDelete.addActionListener(this::deleteReservation);

        JPanel btnPanel = new JPanel(new FlowLayout());
        btnPanel.add(btnSave);
        btnPanel.add(btnEdit);
        btnPanel.add(btnDelete);

        inputPanel.add(new JLabel());
        inputPanel.add(btnPanel);

        String[] cols = {"Res ID", "Customer", "Room", "Check-in", "Check-out", "Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() >= 0) {
                selectedRow = table.getSelectedRow();
                Reservation r = reservations.get(selectedRow);
                txtResId.setText(r.getReservationId());
                txtCustId.setText(r.getCustomerId());
                txtRoomId.setText(r.getRoomId());
                txtCheckIn.setText(r.getCheckInDate().toString());
                txtCheckOut.setText(r.getCheckOutDate().toString());
                comboStatus.setSelectedItem(r.getStatus());
            }
        });

        refreshTable();

        panel.add(inputPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private void saveReservation(ActionEvent e) {
        String resId = txtResId.getText().trim();
        String custId = txtCustId.getText().trim();
        String roomId = txtRoomId.getText().trim();
        LocalDate checkIn, checkOut;

        try {
            checkIn = LocalDate.parse(txtCheckIn.getText().trim(), DateTimeFormatter.ISO_LOCAL_DATE);
            checkOut = LocalDate.parse(txtCheckOut.getText().trim(), DateTimeFormatter.ISO_LOCAL_DATE);
            if (!checkOut.isAfter(checkIn)) {
                throw new DateTimeParseException("Check-out must be after check-in", "", 0);
            }
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(null, "Please enter valid dates in YYYY-MM-DD format,\nand ensure check-out is after check-in.", "Date Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (resId.isEmpty() || custId.isEmpty() || roomId.isEmpty()) {
            JOptionPane.showMessageDialog(null, "All ID fields are required.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (selectedRow == -1) {
            reservations.add(new Reservation(resId, custId, roomId, checkIn, checkOut));
        } else {
            Reservation r = reservations.get(selectedRow);
            r.setReservationId(resId);
            r.setCustomerId(custId);
            r.setRoomId(roomId);
            r.setCheckInDate(checkIn);
            r.setCheckOutDate(checkOut);
            r.setStatus((Reservation.Status) comboStatus.getSelectedItem());
        }

        FileHandler.saveToFile(reservations, "reservations.dat");
        refreshTable();
        clearFields();
        selectedRow = -1;
    }

    private void editReservation(ActionEvent e) {
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(null, "Select a reservation to edit.", "No Selection", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void deleteReservation(ActionEvent e) {
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(null, "Select a reservation to delete.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        reservations.remove(selectedRow);
        FileHandler.saveToFile(reservations, "reservations.dat");
        refreshTable();
        clearFields();
        selectedRow = -1;
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        for (Reservation r : reservations) {
            tableModel.addRow(new Object[]{
                    r.getReservationId(),
                    r.getCustomerId(),
                    r.getRoomId(),
                    r.getCheckInDate(),
                    r.getCheckOutDate(),
                    r.getStatus()
            });
        }
    }

    private void clearFields() {
        txtResId.setText("");
        txtCustId.setText("");
        txtRoomId.setText("");
        txtCheckIn.setText("");
        txtCheckOut.setText("");
        comboStatus.setSelectedIndex(0);
    }
}