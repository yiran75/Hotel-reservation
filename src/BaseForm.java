import javax.swing.*;
import java.awt.*;

abstract class BaseForm extends JFrame {
    public final void initForm() {
        setupLayout();
        addComponents();
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);
        setVisible(true);
    }
    protected abstract void setupLayout();
    protected abstract void addComponents();
}