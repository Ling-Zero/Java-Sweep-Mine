package minesweeper.core;


import java.util.ArrayDeque;
import java.util.Random;

public class MineField {

    public interface Listener {
        void onGameStart();

        void onGameOver(boolean won);

        void onMineCountChanged(int remaining);
    }

    public enum OpenResult {
        REVEALED,
        FLAGGED,
        ALREADY_OPEN,
        LOST,
        WON,
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

    public int aroundCount(int r, int c) {
        return around[r][c];
    }

    public int getRemainingMines() {
        return mineCount - flagCount;
    }


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

    public void toggleFlag(int r, int c) {
        if (finished || revealed[r][c]) {
            return;
        }
        flagged[r][c] = !flagged[r][c];
        flagCount += flagged[r][c] ? 1 : -1;
        listener.onMineCountChanged(getRemainingMines());
    }


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
