package minesweeper.gfx;


import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.QuadCurve2D;
import java.awt.image.BufferedImage;

/**
 * 扫雷游戏图标：圆角渐变背景 + 地雷（引线、火花与高光）。
 * <ul>
 *   <li>运行时：{@code AppWindow} 用 {@link #create(int)} 作为窗口/任务栏图标；</li>
 *   <li>打包时：packaging/MakeIcon.java 调用 {@link #create(int)} 生成多尺寸 icon.ico，供 JDK 打包工具嵌入 exe。</li>
 * </ul>
 */
public final class MinesweeperIcon {

    private MinesweeperIcon() {
    }

    /** 绘制任意尺寸的图标（正方形，边长 s 像素） */
    public static BufferedImage create(int s) {
        BufferedImage img = new BufferedImage(s, s, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        double radius = s * 0.22;

        // 背景：圆角矩形 + 蓝渐变
        GradientPaint bg = new GradientPaint(0f, 0f, new Color(0x63A0E8),
                0f, (float) s, new Color(0x2B5FA5));
        g.setPaint(bg);
        g.fillRoundRect(0, 0, s, s, (int) radius, (int) radius);

        // 顶部柔光 / 底部暗角
        g.setColor(new Color(255, 255, 255, 70));
        g.fillRoundRect(0, 0, s, (int) (s * 0.42), (int) radius, (int) radius);
        g.setColor(new Color(0, 0, 0, 45));
        g.fillRoundRect(0, (int) (s * 0.66), s, (int) (s * 0.34), (int) radius, (int) radius);

        // 引线（从弹体顶部向上弯曲）
        g.setColor(new Color(0x8A6D3B));
        g.setStroke(new BasicStroke(Math.max(2f, (float) (s * 0.055)),
                BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(new QuadCurve2D.Double(s * 0.50, s * 0.315,
                s * 0.62, s * 0.22,
                s * 0.585, s * 0.16));

        // 火花（引线末端）
        double spx = s * 0.585, spy = s * 0.155;
        g.setColor(new Color(0xFFC93C));
        g.fillOval((int) (spx - s * 0.055), (int) (spy - s * 0.055),
                (int) (s * 0.11), (int) (s * 0.11));
        g.setColor(new Color(0xFFF3B0));
        g.fillOval((int) (spx - s * 0.024), (int) (spy - s * 0.024),
                (int) (s * 0.048), (int) (s * 0.048));

        // 弹体
        double bx = s * 0.5, by = s * 0.58, br = s * 0.265;
        g.setColor(new Color(0x34373C));
        g.fillOval((int) (bx - br), (int) (by - br), (int) (2 * br), (int) (2 * br));

        // 弹体高光与底部反光
        g.setColor(new Color(255, 255, 255, 210));
        g.fillOval((int) (bx - br * 0.62), (int) (by - br * 0.62),
                (int) (br * 0.7), (int) (br * 0.7));
        g.setColor(new Color(255, 255, 255, 45));
        g.fillOval((int) (bx - br * 0.45), (int) (by + br * 0.42),
                (int) (br * 0.9), (int) (br * 0.5));

        g.dispose();
        return img;
    }
}
