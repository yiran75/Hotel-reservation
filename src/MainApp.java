import javax.swing.*;
import java.awt.*;

public class MainApp
{
    public static void main(String[] args)
    {
        try
        {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels())
            {
                if ("Nimbus".equals(info.getName()))
                {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e)
        {
            // Fallback to system default
        }

        SwingUtilities.invokeLater(() ->
        {
            JFrame frame = new JFrame("🏨 Hotel Reservation System");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(900, 600);
            frame.setLocationRelativeTo(null);

            JTabbedPane tabbedPane = new JTabbedPane();
            tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            tabbedPane.addTab("🏨 Rooms", new RoomForm().getPanel());
            tabbedPane.addTab("👥 Customers", new CustomerForm().getPanel());
            tabbedPane.addTab("📅 Reservations", new ReservationForm().getPanel());

            frame.add(tabbedPane);
            frame.setVisible(true);
        });
    }
}