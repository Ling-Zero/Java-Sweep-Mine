package minesweeper.ui;


import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * 扫雷游戏入口。
 */
public class MineSweeper {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
                // 使用默认外观
            }
            new AppWindow().setVisible(true);
        });
    }
}
