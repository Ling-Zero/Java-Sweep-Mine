package minesweeper.core;


import java.util.ArrayDeque;
import java.util.Random;

/**
 * 扫雷核心逻辑模型（与界面无关，便于单独测试）。
 * 负责：布雷、翻开（含空白区域扩散）、标记、胜负判定。
 * <p>
 * 界面层通过 getter 读取状态，调用 {@link #open(int, int)} / {@link #toggleFlag(int, int)}
 * 改变状态；模型通过 {@link Listener} 通知“游戏开始 / 游戏结束 / 剩余雷数变化”。
 */
public class MineField {

    /** 模型事件回调 */
    public interface Listener {
        /** 第一次点击后布雷完成，游戏开始（用于启动计时） */
        void onGameStart();

        /** 游戏结束：won 为 true 表示通关，false 表示踩雷 */
        void onGameOver(boolean won);

        /** 标记/取消标记后，剩余雷数变化 */
        void onMineCountChanged(int remaining);
    }

    /** {@link #open(int, int)} 的返回结果 */
    public enum OpenResult {
        /** 正常翻开 */
        REVEALED,
        /** 点击了已标记的格子，未发生任何变化 */
        FLAGGED,
        /** 点击了已翻开的格子（由界面层处理 chord 快速翻开） */
        ALREADY_OPEN,
        /** 踩雷，游戏结束 */
        LOST,
        /** 通关，游戏结束 */
        WON,
        /** 游戏已结束，本次点击被忽略 */
        IGNORED
    }

    private final int rows, cols, mineCount;
    private final boolean[][] mines;
    private final boolean[][] revealed;
    private final boolean[][] flagged;
    private final int[][] around;
    private final Listener listener;

    private boolean started;
    private boolean finished;
    private int openedSafe;
    private int flagCount;

    public MineField(GameConfig cfg, Listener listener) {
        this(cfg.rows(), cfg.cols(), cfg.mines(), listener);
    }

    public MineField(int rows, int cols, int mineCount, Listener listener) {
        this.rows = rows;
        this.cols = cols;
        this.mineCount = mineCount;
        this.listener = listener;
        this.mines = new boolean[rows][cols];
        this.revealed = new boolean[rows][cols];
        this.flagged = new boolean[rows][cols];
        this.around = new int[rows][cols];
    }

    // ------------------------------------------------------------ 状态读取

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }

    public boolean isFinished() {
        return finished;
    }

    public boolean isMine(int r, int c) {
        return mines[r][c];
    }

    public boolean isRevealed(int r, int c) {
        return revealed[r][c];
    }

    public boolean isFlagged(int r, int c) {
        return flagged[r][c];
    }

    /** 周围 8 格中的地雷数（仅对已翻开数字格有意义） */
    public int aroundCount(int r, int c) {
        return around[r][c];
    }

    /** 剩余雷数 = 总雷数 - 已标记数 */
    public int getRemainingMines() {
        return mineCount - flagCount;
    }

    // ------------------------------------------------------------ 操作

    /** 左键翻开 (r,c)。第一次点击后布雷，保证该格安全。 */
    public OpenResult open(int r, int c) {
        if (finished) {
            return OpenResult.IGNORED;
        }
        if (revealed[r][c]) {
            return OpenResult.ALREADY_OPEN;
        }
        if (flagged[r][c]) {
            return OpenResult.FLAGGED;
        }
        if (!started) {
            started = true;
            placeMines(r, c);
            listener.onGameStart();
        }
        if (mines[r][c]) {
            finish(false);
            return OpenResult.LOST;
        }
        floodReveal(r, c);
        if (openedSafe == rows * cols - mineCount) {
            finish(true);
            return OpenResult.WON;
        }
        return OpenResult.REVEALED;
    }

    /** 右键标记/取消标记 (r,c)（已翻开或已结束时忽略） */
    public void toggleFlag(int r, int c) {
        if (finished || revealed[r][c]) {
            return;
        }
        flagged[r][c] = !flagged[r][c];
        flagCount += flagged[r][c] ? 1 : -1;
        listener.onMineCountChanged(getRemainingMines());
    }

    // ------------------------------------------------------------ 内部

    /** 第一次点击后布雷，并统计周围雷数 */
    private void placeMines(int safeR, int safeC) {
        Random rnd = new Random();
        int placed = 0;
        while (placed < mineCount) {
            int r = rnd.nextInt(rows);
            int c = rnd.nextInt(cols);
            if ((r == safeR && c == safeC) || mines[r][c]) {
                continue;
            }
            mines[r][c] = true;
            placed++;
        }
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (!mines[r][c]) {
                    continue;
                }
                for (int dr = -1; dr <= 1; dr++) {
                    for (int dc = -1; dc <= 1; dc++) {
                        int nr = r + dr, nc = c + dc;
                        if (inRange(nr, nc)) {
                            around[nr][nc]++;
                        }
                    }
                }
            }
        }
    }

    private boolean inRange(int r, int c) {
        return r >= 0 && r < rows && c >= 0 && c < cols;
    }

    /** 翻开一片空白区域（广度优先扩散） */
    private void floodReveal(int r, int c) {
        ArrayDeque<int[]> queue = new ArrayDeque<>();
        queue.add(new int[]{r, c});
        while (!queue.isEmpty()) {
            int[] p = queue.poll();
            int pr = p[0], pc = p[1];
            if (!inRange(pr, pc) || revealed[pr][pc] || flagged[pr][pc] || mines[pr][pc]) {
                continue;
            }
            revealed[pr][pc] = true;
            openedSafe++;
            if (around[pr][pc] == 0) {
                for (int dr = -1; dr <= 1; dr++) {
                    for (int dc = -1; dc <= 1; dc++) {
                        if (dr == 0 && dc == 0) {
                            continue;
                        }
                        queue.add(new int[]{pr + dr, pc + dc});
                    }
                }
            }
        }
    }

    /** 结束本局：won 时自动给剩余地雷插旗，否则揭开全部地雷 */
    private void finish(boolean won) {
        finished = true;
        if (won) {
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    if (mines[r][c] && !flagged[r][c]) {
                        flagged[r][c] = true;
                        flagCount++;
                    }
                }
            }
            listener.onMineCountChanged(getRemainingMines());
        } else {
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    if (mines[r][c]) {
                        revealed[r][c] = true;
                    }
                }
            }
        }
        listener.onGameOver(won);
    }
}
