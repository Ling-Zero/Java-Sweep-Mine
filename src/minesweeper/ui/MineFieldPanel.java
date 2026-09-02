package minesweeper.ui;

import minesweeper.audio.SoundPlayer;
import minesweeper.core.GameConfig;
import minesweeper.core.MineField;
import minesweeper.gfx.GameIcons;
import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class MineFieldPanel extends JPanel {

    public interface Listener {
        void onMineCountChanged(int remaining);

        void onGameStart();

        void onGameOver(boolean won);
    }

    private static final int BASE_CELL = 34;
    private static final int MIN_CELL = 14;
    private static final int MAX_CELL = 48;
    private static final int PAD = 8;

    public static final Color BOARD_BG = new Color(0x9AA0A6);
    private static final Color COVER_COLOR = new Color(0xB9BEC4);
    private static final Color COVER_LIGHT = new Color(0xD8DCE0);
    private static final Color COVER_DARK = new Color(0x8B9198);
    private static final Color REVEAL_COLOR = new Color(0xF1F3F5);
    private static final Color GRID_COLOR = new Color(0xC4C9CF);

    private static final Color[] NUMBER_COLORS = {
            Color.BLACK,
            new Color(0x1A6FE0),
            new Color(0x1E8E3E),
            new Color(0xE03131),
            new Color(0x173A8F),
            new Color(0x8B1FA9),
            new Color(0x0B8F8F),
            new Color(0x20242A),
            new Color(0x8A8A8A),
    };

    private enum CellIcon { MINE, FLAG, WRONG }

    private final MineField model;
    private final Font numberFont;
    private final java.awt.Image mineBomb;

    private int cellSize = BASE_CELL;
    private int originX, originY;

    public MineFieldPanel(GameConfig cfg, Listener listener) {
        this.model = new MineField(cfg, new MineField.Listener() {
            @Override
            public void onGameStart() {
                listener.onGameStart();
            }

            @Override
            public void onGameOver(boolean won) {
                listener.onGameOver(won);
            }

            @Override
            public void onMineCountChanged(int remaining) {
                listener.onMineCountChanged(remaining);
            }
        });
        this.numberFont = getFont().deriveFont(Font.BOLD, 18f);
        this.mineBomb = GameIcons.bomb(24).getImage();

        setPreferredSize(new Dimension(model.getCols() * BASE_CELL + 2 * PAD,
                model.getRows() * BASE_CELL + 2 * PAD));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getButton() != MouseEvent.BUTTON1 && e.getButton() != MouseEvent.BUTTON3) {
                    return;
                }
                int c = (e.getX() - originX) / cellSize;
                int r = (e.getY() - originY) / cellSize;
                if (r < 0 || r >= model.getRows() || c < 0 || c >= model.getCols()) {
                    return;
                }
                if (e.getButton() == MouseEvent.BUTTON1) {
                    onLeftClick(r, c);
                } else {
                    onRightClick(r, c);
                }
            }
        });
        listener.onMineCountChanged(model.getRemainingMines());
    }


    private void onLeftClick(int r, int c) {
        if (model.isRevealed(r, c)) {
            chord(r, c);
            return;
        }
        MineField.OpenResult res = model.open(r, c);
        if (res == MineField.OpenResult.REVEALED) {
            SoundPlayer.playClick();
        }
        repaint();
    }

    private void onRightClick(int r, int c) {
        boolean wasFlagged = model.isFlagged(r, c);
        model.toggleFlag(r, c);
        boolean nowFlagged = model.isFlagged(r, c);
        if (wasFlagged != nowFlagged) {
            if (nowFlagged) {
                SoundPlayer.playMark();
            } else {
                SoundPlayer.playUnmark();
            }
        }
        repaint();
    }

    private void chord(int r, int c) {
        int n = model.aroundCount(r, c);
        if (n == 0) {
            return;
        }
        int cnt = 0;
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                if (dr == 0 && dc == 0) {
                    continue;
                }
                int nr = r + dr, nc = c + dc;
                if (inRange(nr, nc) && model.isFlagged(nr, nc)) {
                    cnt++;
                }
            }
        }
        if (cnt != n) {
            return;
        }
        boolean openedAny = false;
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                if (dr == 0 && dc == 0) {
                    continue;
                }
                int nr = r + dr, nc = c + dc;
                if (!inRange(nr, nc) || model.isFlagged(nr, nc) || model.isRevealed(nr, nc)) {
                    continue;
                }
                MineField.OpenResult res = model.open(nr, nc);
                if (res == MineField.OpenResult.REVEALED) {
                    openedAny = true;
                }
                if (res == MineField.OpenResult.LOST || res == MineField.OpenResult.WON) {
                    break;
                }
            }
        }
        if (openedAny) {
            SoundPlayer.playClick();
        }
        repaint();
    }

    private boolean inRange(int r, int c) {
        return r >= 0 && r < model.getRows() && c >= 0 && c < model.getCols();
    }


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setColor(BOARD_BG);
        g2.fillRect(0, 0, getWidth(), getHeight());

        int availW = Math.max(0, getWidth() - 2 * PAD);
        int availH = Math.max(0, getHeight() - 2 * PAD);
        int target = (int) Math.min((double) availW / model.getCols(), (double) availH / model.getRows());
        cellSize = Math.max(MIN_CELL, Math.min(MAX_CELL, target));
        int boardW = cellSize * model.getCols();
        int boardH = cellSize * model.getRows();
        originX = (getWidth() - boardW) / 2;
        originY = (getHeight() - boardH) / 2;
        if (cellSize <= 0) {
            g2.dispose();
            return;
        }

        for (int r = 0; r < model.getRows(); r++) {
            for (int c = 0; c < model.getCols(); c++) {
                int x = originX + c * cellSize;
                int y = originY + r * cellSize;
                paintCell(g2, r, c, x, y, cellSize);
            }
        }
        g2.dispose();
    }

    private void paintCell(Graphics2D g, int r, int c, int x, int y, int cs) {
        if (model.isRevealed(r, c)) {
            g.setColor(REVEAL_COLOR);
            g.fillRect(x, y, cs, cs);
            g.setColor(GRID_COLOR);
            g.drawRect(x, y, cs, cs);
            if (model.isMine(r, c)) {
                drawIcon(g, CellIcon.MINE, x, y, cs);
            } else if (model.aroundCount(r, c) > 0) {
                drawNumber(g, model.aroundCount(r, c), x, y, cs);
            }
        } else if (model.isFlagged(r, c)) {
            paintCover(g, x, y, cs);
            drawIcon(g, model.isFinished() && !model.isMine(r, c) ? CellIcon.WRONG : CellIcon.FLAG, x, y, cs);
        } else {
            paintCover(g, x, y, cs);
        }
    }

    private void paintCover(Graphics2D g, int x, int y, int cs) {
        g.setColor(COVER_COLOR);
        g.fillRect(x, y, cs, cs);
        g.setColor(COVER_LIGHT);
        g.fillRect(x, y, cs, 3);
        g.fillRect(x, y, 3, cs);
        g.setColor(COVER_DARK);
        g.fillRect(x, y + cs - 3, cs, 3);
        g.fillRect(x + cs - 3, y, 3, cs);
    }

    private void drawNumber(Graphics2D g, int n, int x, int y, int cs) {
        g.setFont(numberFont.deriveFont((float) Math.max(12, cs * 0.5)));
        g.setColor(NUMBER_COLORS[n]);
        FontMetrics fm = g.getFontMetrics();
        String s = String.valueOf(n);
        int tx = x + (cs - fm.stringWidth(s)) / 2;
        int ty = y + (cs - fm.getHeight()) / 2 + fm.getAscent();
        g.drawString(s, tx, ty);
    }

    private void drawIcon(Graphics2D g, CellIcon kind, int x, int y, int cs) {
        switch (kind) {
            case MINE: {
                int imgSize = Math.max(10, cs - 8);
                int bx = x + (cs - imgSize) / 2;
                int by = y + (cs - imgSize) / 2;
                g.drawImage(mineBomb, bx, by, imgSize, imgSize, null);
                break;
            }
            case FLAG: {
                double cx = x + cs * 0.5;
                g.setColor(new Color(0x5F6368));
                g.setStroke(new BasicStroke(Math.max(1.5f, (float) (cs * 0.045)),
                        BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.drawLine((int) cx, (int) (y + cs * 0.12), (int) cx, (int) (y + cs * 0.86));
                g.setColor(new Color(0xD93025));
                int[] xs = {(int) (cx + 0.5), (int) (x + cs * 0.92), (int) (cx + 0.5)};
                int[] ys = {(int) (y + cs * 0.14), (int) (y + cs * 0.36), (int) (y + cs * 0.56)};
                g.fillPolygon(xs, ys, 3);
                g.setColor(new Color(0x5F6368));
                g.fillOval((int) (x + cs * 0.28), (int) (y + cs * 0.82),
                        (int) (cs * 0.44), (int) (cs * 0.12));
                break;
            }
            case WRONG: {
                g.setColor(new Color(0xD93025));
                g.setStroke(new BasicStroke(Math.max(2f, (float) (cs * 0.08)),
                        BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.drawLine((int) (x + cs * 0.18), (int) (y + cs * 0.18),
                        (int) (x + cs * 0.82), (int) (y + cs * 0.82));
                g.drawLine((int) (x + cs * 0.82), (int) (y + cs * 0.18),
                        (int) (x + cs * 0.18), (int) (y + cs * 0.82));
                break;
            }
        }
    }
}
