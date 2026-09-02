package minesweeper.ui;


import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class MineSweeper {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }
            new AppWindow().setVisible(true);
        });
    }
}
