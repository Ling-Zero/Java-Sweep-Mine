package minesweeper.core;


/**
 * 一局扫雷的配置：行数、列数、雷数与级别名称（不可变）。
 */
public record GameConfig(int rows, int cols, int mines, String name) {
}
