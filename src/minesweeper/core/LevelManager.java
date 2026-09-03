package minesweeper.core;


public class LevelManager {

    public static final int TOTAL_LEVELS = 3;

    private static final GameConfig[] LEVELS = {
            new GameConfig(8, 8, 10, "第一关·初级"),
            new GameConfig(12, 12, 20, "第二关·中级"),
            new GameConfig(16, 16, 40, "第三关·高级"),
    };

    public static final GameConfig[] PRESETS = {
            new GameConfig(9, 9, 10, "初级"),
            new GameConfig(16, 16, 40, "中级"),
            new GameConfig(16, 30, 99, "高级"),
    };

    public int getTotalLevels() {
        return TOTAL_LEVELS;
    }

    public GameConfig getLevel(int index) {
        return LEVELS[index];
    }
}
