import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class HomeFrame extends JFrame {
    private JTextField txtCustId = new JTextField();
    private JTextField txtCustName = new JTextField();
    private JTextField txtRoomId = new JTextField();
    private JTextField txtPrice = new JTextField();
    private JTextField txtDays = new JTextField();

    public HomeFrame() {
        setTitle("Booking");
        setSize(400, 500);
        setLayout(new GridLayout(7, 2));

        add(new JLabel("Cust ID:")); add(txtCustId);
        add(new JLabel("Name:")); add(txtCustName);
        add(new JLabel("Room ID:")); add(txtRoomId);
        add(new JLabel("Price:")); add(txtPrice);
        add(new JLabel("Days:")); add(txtDays);

        JButton btnSubmit = new JButton("Confirm");
        add(new JLabel("")); add(btnSubmit);

        btnSubmit.addActionListener(e -> {
            try {
                ICustomerFactory factory = new CustomerFactory();
                Customer customer = factory.createCustomer(txtCustId.getText(), txtCustName.getText(), "", "");

                int total = Integer.parseInt(txtPrice.getText()) * Integer.parseInt(txtDays.getText());

                Reservation reservation = new Reservation.Builder()
                        .setResId("RES-" + System.currentTimeMillis() % 100)
                        .setRoomId(txtRoomId.getText())
                        .setCustId(txtCustId.getText())
                        .setTotal(total)
                        .build();

                ArrayList<Reservation> list = DataManager.getInstance().loadReservations();
                list.add(reservation);
                DataManager.getInstance().saveReservations(list);

                JOptionPane.showMessageDialog(this, "Success! Total: " + total);
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Input Error!"); }
        });
        setVisible(true);
    }
}