package minesweeper.ui;

import minesweeper.core.GameConfig;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeListener;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;

public class CustomLevelDialog extends JDialog {

    private final SpinnerNumberModel rowModel = new SpinnerNumberModel(9, 5, 30, 1);
    private final SpinnerNumberModel colModel = new SpinnerNumberModel(9, 5, 50, 1);
    private final SpinnerNumberModel mineModel = new SpinnerNumberModel(10, 1, 449, 1);
    private final JTextField nameField = new JTextField("自定义", 10);

    private GameConfig config;

    public CustomLevelDialog(Frame owner) {
        super(owner, "自定义级别", true);
        buildUI();
        pack();
        setLocationRelativeTo(owner);
        setResizable(false);
    }

    private void buildUI() {
        JPanel form = new JPanel(new GridLayout(4, 2, 10, 10));
        form.setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 12, 8, 12));

        JSpinner rowSpinner = new JSpinner(rowModel);
        JSpinner colSpinner = new JSpinner(colModel);
        JSpinner mineSpinner = new JSpinner(mineModel);

        form.add(new JLabel("行数（5~30）："));
        form.add(rowSpinner);
        form.add(new JLabel("列数（5~50）："));
        form.add(colSpinner);
        form.add(new JLabel("雷数："));
        form.add(mineSpinner);
        form.add(new JLabel("级别名称："));
        form.add(nameField);

        ChangeListener clamp = ignored -> {
            int max = rowModel.getNumber().intValue() * colModel.getNumber().intValue() - 1;
            mineModel.setMaximum(max);
            if (mineModel.getNumber().intValue() > max) {
                mineModel.setValue(max);
            }
        };
        rowModel.addChangeListener(clamp);
        colModel.addChangeListener(clamp);

        JButton ok = new JButton("确定");
        JButton cancel = new JButton("取消");
        ok.addActionListener(ignored -> onOk());
        cancel.addActionListener(ignored -> {
            config = null;
            dispose();
        });
        getRootPane().setDefaultButton(ok);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 6));
        buttons.add(ok);
        buttons.add(cancel);

        add(form, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);
    }

    private void onOk() {
        int rows = rowModel.getNumber().intValue();
        int cols = colModel.getNumber().intValue();
        int mines = mineModel.getNumber().intValue();
        String name = nameField.getText().trim();

        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "级别名称不能为空！",
                    "输入错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (name.length() > 12) {
            JOptionPane.showMessageDialog(this, "级别名称不能超过 12 个字符！",
                    "输入错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (mines >= rows * cols) {
            JOptionPane.showMessageDialog(this, "雷数必须小于 行数×列数！",
                    "输入错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        config = new GameConfig(rows, cols, mines, name);
        dispose();
    }

    public GameConfig getConfig() {
        return config;
    }
}
