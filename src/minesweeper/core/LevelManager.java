package minesweeper.core;


/**
 * 关卡与难度配置管理。
 * <p>
 * “三关扫雷”玩法：依次通过 第一关(初级) → 第二关(中级) → 第三关(高级)，
 * 每过一关询问用户是否继续下一关。
 */
public class LevelManager {

    public static final int TOTAL_LEVELS = 3;

    /** 三关扫雷的关卡配置（难度逐关提升） */
    private static final GameConfig[] LEVELS = {
            new GameConfig(8, 8, 10, "第一关·初级"),
            new GameConfig(12, 12, 20, "第二关·中级"),
            new GameConfig(16, 16, 40, "第三关·高级"),
    };

    /** “难度”菜单中的普通难度配置 */
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
