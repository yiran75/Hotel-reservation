import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class HotelApp extends JFrame {
    private JTextField txtUser;
    private JPasswordField txtPass;
    public static ArrayList<Room> roomList = new ArrayList<>();

    public HotelApp() {
        setTitle("Hotel Login");
        setSize(300, 200);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new GridLayout(3, 2));

        add(new JLabel(" Username:")); txtUser = new JTextField(); add(txtUser);
        add(new JLabel(" Password:")); txtPass = new JPasswordField(); add(txtPass);

        JButton btnLogin = new JButton("Login");
        add(new JLabel("")); add(btnLogin);

        btnLogin.addActionListener(e -> {
            String user = txtUser.getText();
            String pass = new String(txtPass.getPassword());

            if (user.equals("admin") && pass.equals("123")) {
                new AdminFrame();
                this.dispose();
            } else if (user.equals("customer") && pass.equals("123")) {
                new CustomerFrame();
                this.dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Invalid Login!");
            }
        });
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public static void main(String[] args) { new HotelApp(); }
}

class AdminFrame extends JFrame {
    private JTextField txtId, txtType, txtPrice;

    AdminFrame() {
        setTitle("Admin Panel - Add Room");
        setSize(400, 300);
        setLayout(new GridLayout(4, 2));
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        add(new JLabel(" Room ID:")); txtId = new JTextField(); add(txtId);
        add(new JLabel(" Room Type:")); txtType = new JTextField(); add(txtType);
        add(new JLabel(" Price:")); txtPrice = new JTextField(); add(txtPrice);

        JButton btnSave = new JButton("Add Room");
        add(new JLabel("")); add(btnSave);

        btnSave.addActionListener(e -> {
            Room newRoom = new Room.Builder()
                    .setRoomId(txtId.getText())
                    .setType(txtType.getText())
                    .setPrice(Integer.parseInt(txtPrice.getText()))
                    .build();
            HotelApp.roomList.add(newRoom);
            JOptionPane.showMessageDialog(this, "Room Added Successfully!");
        });
        setLocationRelativeTo(null);
        setVisible(true);
    }
}

class CustomerFrame extends JFrame {
    CustomerFrame() {
        setTitle("Customer Panel - Book Room");
        setSize(400, 300);
        setLayout(new FlowLayout());
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JButton btnView = new JButton("View Available Rooms");
        btnView.addActionListener(e -> {
            StringBuilder sb = new StringBuilder("Available Rooms:\n");
            for (Room r : HotelApp.roomList) {
                sb.append("ID: ").append(r.getRoomId()).append(" | Type: ").append(r.getType()).append(" | Price: ").append(r.getPrice()).append("\n");
            }
            JOptionPane.showMessageDialog(this, sb.toString());
        });

        add(btnView);
        setLocationRelativeTo(null);
        setVisible(true);
    }
  }
