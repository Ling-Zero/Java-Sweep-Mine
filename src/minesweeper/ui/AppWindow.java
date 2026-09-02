package minesweeper.ui;

import minesweeper.audio.SoundPlayer;
import minesweeper.core.GameConfig;
import minesweeper.core.LevelManager;
import minesweeper.gfx.GameIcons;
import minesweeper.gfx.MinesweeperIcon;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.KeyStroke;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;

public class AppWindow extends JFrame {

    private final LevelManager levels = new LevelManager();
    private final JLabel levelLabel = new JLabel();
    private final JLabel mineLabel = new JLabel();
    private final JLabel timeLabel = new JLabel();
    private int elapsed;
    private final Timer clock = new Timer(1000, ignored -> timeLabel.setText("用时：" + (++elapsed) + " 秒"));

    private final ButtonGroup diffGroup = new ButtonGroup();
    private JRadioButtonMenuItem[] diffItems;

    private Component boardHolder;
    private GameConfig current;
    private int levelIndex = -1;

    public AppWindow() {
        super("扫雷游戏 — 三关扫雷");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setIconImage(MinesweeperIcon.create(48));
        setJMenuBar(buildMenuBar());
        buildStatusBar();
        startLevel(1);
        pack();
        setLocationRelativeTo(null);
        setMinimumSize(getSize());
    }


    private JMenuBar buildMenuBar() {
        JMenuBar bar = new JMenuBar();

        JMenu game = new JMenu("游戏(G)");
        game.setMnemonic(KeyEvent.VK_G);
        game.setIcon(GameIcons.bomb(16));

        JMenuItem restart = new JMenuItem("新游戏(N)");
        restart.setMnemonic(KeyEvent.VK_N);
        restart.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0));
        restart.setIcon(GameIcons.bomb(16));
        restart.addActionListener(ignored -> newGame(current));
        game.add(restart);
        game.addSeparator();

        JMenu diffMenu = new JMenu("难度");
        diffMenu.setIcon(GameIcons.flag(16));
        diffItems = new JRadioButtonMenuItem[LevelManager.PRESETS.length + 1];
        for (int i = 0; i < LevelManager.PRESETS.length; i++) {
            final GameConfig cfg = LevelManager.PRESETS[i];
            JRadioButtonMenuItem item = new JRadioButtonMenuItem(cfg.name());
            diffGroup.add(item);
            item.addActionListener(ignored -> {
                levelIndex = -1;
                newGame(cfg);
            });
            diffItems[i] = item;
            diffMenu.add(item);
        }
        diffMenu.addSeparator();
        JRadioButtonMenuItem customItem = new JRadioButtonMenuItem("自定义级别...");
        diffGroup.add(customItem);
        customItem.addActionListener(ignored -> openCustomDialog());
        diffItems[LevelManager.PRESETS.length] = customItem;
        diffMenu.add(customItem);
        game.add(diffMenu);
        game.addSeparator();

        JMenu levelMenu = new JMenu("关卡（三关扫雷）");
        levelMenu.setIcon(GameIcons.flag(16));
        for (int i = 0; i < levels.getTotalLevels(); i++) {
            final int idx = i + 1;
            JMenuItem item = new JMenuItem(levels.getLevel(i).name());
            item.addActionListener(ignored -> startLevel(idx));
            levelMenu.add(item);
        }
        game.add(levelMenu);
        game.addSeparator();

        JCheckBoxMenuItem soundItem = new JCheckBoxMenuItem("音效", true);
        soundItem.addActionListener(ignored -> SoundPlayer.setMuted(!soundItem.isSelected()));
        game.add(soundItem);
        game.addSeparator();

        JMenuItem exit = new JMenuItem("退出(X)");
        exit.setMnemonic(KeyEvent.VK_X);
        exit.addActionListener(ignored -> System.exit(0));
        game.add(exit);

        JMenu help = new JMenu("帮助(H)");
        help.setMnemonic(KeyEvent.VK_H);
        JMenuItem about = new JMenuItem("关于");
        about.setIcon(GameIcons.check(16));
        about.addActionListener(ignored -> JOptionPane.showMessageDialog(this,
                """
                扫雷游戏（三关扫雷）

                左键：翻开方块（点到安全格播放愉悦音乐）
                右键：标记/取消标记地雷（标记时播放提示音乐）

                玩法：依次通过 第一关→第二关→第三关，过一关询问是否继续下一关；
                也可以从“难度”菜单选择初级/中级/高级，或用“自定义级别...”
                设置行数、列数、雷数和级别名称。""",
                "关于", JOptionPane.INFORMATION_MESSAGE, GameIcons.bomb(32)));
        help.add(about);
        bar.add(game);
        bar.add(help);
        return bar;
    }

    private void buildStatusBar() {
        levelLabel.setFont(levelLabel.getFont().deriveFont(Font.BOLD, 13f));
        mineLabel.setIcon(GameIcons.bomb(16));
        mineLabel.setIconTextGap(4);
        timeLabel.setIcon(GameIcons.clock(16));
        timeLabel.setIconTextGap(4);

        JPanel status = new JPanel(new BorderLayout(12, 0));
        status.setBackground(new Color(0xE7EAEE));
        status.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(0xC9CDD2)),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        left.setOpaque(false);
        left.add(levelLabel);
        left.add(mineLabel);
        left.add(timeLabel);

        JLabel hint = new JLabel("左键：翻开　|　右键：标记地雷");
        hint.setForeground(new Color(0x7A828A));
        hint.setFont(hint.getFont().deriveFont(Font.ITALIC, 12f));

        status.add(left, BorderLayout.WEST);
        status.add(hint, BorderLayout.EAST);
        add(status, BorderLayout.SOUTH);
    }


    private void startLevel(int idx) {
        levelIndex = idx;
        newGame(levels.getLevel(idx - 1));
    }

    private void newGame(GameConfig cfg) {
        this.current = cfg;
        clock.stop();
        elapsed = 0;
        timeLabel.setText("用时：0 秒");
        levelLabel.setText("当前级别：" + cfg.name() + "（" + cfg.rows() + "×" + cfg.cols() + "，" + cfg.mines() + " 雷）");
        mineLabel.setText("剩余雷数：" + cfg.mines());

        if (boardHolder != null) {
            remove(boardHolder);
        }
        MineFieldPanel field = new MineFieldPanel(cfg, new MineFieldPanel.Listener() {
            @Override
            public void onMineCountChanged(int remaining) {
                mineLabel.setText("剩余雷数：" + remaining);
            }

            @Override
            public void onGameStart() {
                elapsed = 0;
                clock.start();
            }

            @Override
            public void onGameOver(boolean won) {
                clock.stop();
                if (won) {
                    handleWin();
                } else {
                    handleLose();
                }
            }
        });
        JPanel wrapper = new GradientPanel();
        wrapper.add(field, BorderLayout.CENTER);
        boardHolder = wrapper;
        add(wrapper, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    private void handleLose() {
        SoundPlayer.playLose();
        int r = JOptionPane.showConfirmDialog(this,
                "很遗憾，你踩到地雷了！\n是否重新开始本局？",
                "游戏结束", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE,
                GameIcons.bomb(48));
        if (r == JOptionPane.YES_OPTION) {
            newGame(current);
        }
    }

    private void handleWin() {
        SoundPlayer.playWin();
        if (levelIndex >= 1 && levelIndex < levels.getTotalLevels()) {
            int next = levelIndex + 1;
            int r = JOptionPane.showConfirmDialog(this,
                    "恭喜通过第 " + levelIndex + " 关（" + current.name() + "）！\n是否继续下一关？",
                    "过关", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
                    GameIcons.check(48));
            if (r == JOptionPane.YES_OPTION) {
                startLevel(next);
            }
        } else if (levelIndex == levels.getTotalLevels()) {
            int r = JOptionPane.showConfirmDialog(this,
                    "太棒了！你已经通过了全部三关，通关成功！\n是否重新开始挑战？",
                    "通关成功", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
                    GameIcons.check(48));
            if (r == JOptionPane.YES_OPTION) {
                startLevel(1);
            }
        } else {
            int r = JOptionPane.showConfirmDialog(this,
                    "恭喜过关！（当前为" + current.name() + "模式）\n是否再来一局？",
                    "过关", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
                    GameIcons.check(48));
            if (r == JOptionPane.YES_OPTION) {
                newGame(current);
            }
        }
    }


    private void openCustomDialog() {
        JRadioButtonMenuItem prev = selectedDifficulty();
        CustomLevelDialog dlg = new CustomLevelDialog(this);
        dlg.setVisible(true);
        GameConfig cfg = dlg.getConfig();
        if (cfg != null) {
            levelIndex = -1;
            newGame(cfg);
        } else if (prev != null) {
            prev.setSelected(true);
        }
    }

    private JRadioButtonMenuItem selectedDifficulty() {
        for (JRadioButtonMenuItem item : diffItems) {
            if (item != null && item.isSelected()) {
                return item;
            }
        }
        return null;
    }

    private static final class GradientPanel extends JPanel {
        GradientPanel() {
            super(new BorderLayout());
            setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setPaint(new GradientPaint(0, 0, new Color(0xDDE2E7), 0, getHeight(), new Color(0xAAB2BA)));
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.dispose();
        }
    }
}
