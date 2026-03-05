import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class HotelApp extends JFrame {
    private JTextField txtUser = new JTextField();
    private JPasswordField txtPass = new JPasswordField();

    public HotelApp() {
        setTitle("Hotel Login");
        setSize(300, 200);
        setLayout(new GridLayout(3, 2));
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        add(new JLabel(" Username:")); add(txtUser);
        add(new JLabel(" Password:")); add(txtPass);
        JButton btnLogin = new JButton("Login"); add(new JLabel("")); add(btnLogin);

        btnLogin.addActionListener(e -> {
            String u = txtUser.getText();
            String p = new String(txtPass.getPassword());
            if (u.equals("admin") && p.equals("123")) { new AdminFrame(); this.dispose(); }
            else if (u.equals("customer") && p.equals("123")) { new CustomerFrame(); this.dispose(); }
            else { JOptionPane.showMessageDialog(null, "Login Failed!"); }
        });
        setLocationRelativeTo(null);
        setVisible(true);
    }
    public static void main(String[] args) { new HotelApp(); }
}

class AdminFrame extends JFrame {
    private JTextField t1 = new JTextField(), t2 = new JTextField(), t3 = new JTextField();
    AdminFrame() {
        setTitle("Admin Panel"); setSize(400, 300); setLayout(new GridLayout(5, 2));
        add(new JLabel(" Room ID:")); add(t1);
        add(new JLabel(" Type:")); add(t2);
        add(new JLabel(" Price:")); add(t3);
        JButton b = new JButton("Add Room"); add(b);
        b.addActionListener(e -> {
            Room r = new Room.Builder().setRoomId(t1.getText()).setType(t2.getText())
                    .setPrice(Integer.parseInt(t3.getText())).build();
            ArrayList<Room> list = DataManager.getInstance().loadRooms();
            list.add(r); DataManager.getInstance().saveRooms(list);
            JOptionPane.showMessageDialog(null, "Room Saved!");
        });
        setVisible(true);
    }
}

class CustomerFrame extends JFrame {
    private JTextField rId = new JTextField(), days = new JTextField();
    CustomerFrame() {
        setTitle("Customer Panel"); setSize(400, 400); setLayout(new FlowLayout());
        JButton v = new JButton("View Rooms"); add(v);
        add(new JLabel("Room ID:")); add(rId); rId.setColumns(10);
        add(new JLabel("Days:")); add(days); days.setColumns(10);
        JButton b = new JButton("Book Now"); add(b);

        v.addActionListener(e -> {
            StringBuilder sb = new StringBuilder("Available Rooms:\n");
            for(Room r : DataManager.getInstance().loadRooms())
                sb.append(r.getRoomId()).append(" - ").append(r.getPrice()).append(" Tk\n");
            JOptionPane.showMessageDialog(null, sb.toString());
        });

        b.addActionListener(e -> {
            try {
                ArrayList<Room> rooms = DataManager.getInstance().loadRooms();
                Room selected = null;
                for(Room r : rooms) if(r.getRoomId().equals(rId.getText())) selected = r;

                if(selected != null) {
                    Reservation res = new Reservation.Builder()
                            .setResId("RES-" + System.currentTimeMillis() % 100)
                            .setRoomId(rId.getText())
                            .setCustId("Guest")
                            .setTotal(selected.getPrice() * Integer.parseInt(days.getText()))
                            .build();
                    ArrayList<Reservation> list = DataManager.getInstance().loadReservations();
                    list.add(res); DataManager.getInstance().saveReservations(list);
                    JOptionPane.showMessageDialog(null, "Booking Successful!");
                } else { JOptionPane.showMessageDialog(null, "Room not found!"); }
            } catch(Exception ex) { JOptionPane.showMessageDialog(null, "Invalid Input!"); }
        });
        setVisible(true);
    }
}
