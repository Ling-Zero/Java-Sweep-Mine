package minesweeper.gfx;


import javax.swing.ImageIcon;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.QuadCurve2D;
import java.awt.image.BufferedImage;

/**
 * 界面用的小图标（地雷/红旗/时钟/对勾），全部由 Graphics2D 绘制，不依赖外部资源。
 */
public final class GameIcons {

    private GameIcons() {
    }

    /** 地雷图标 */
    public static ImageIcon bomb(int size) {
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        double cx = size / 2.0, cy = size / 2.0, r = size * 0.32;

        // 引线
        g.setColor(new Color(0x8A6D3B));
        g.setStroke(new BasicStroke(Math.max(1.5f, size / 10f),
                BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(new QuadCurve2D.Double(cx, cy - r, cx + r * 0.6, cy - r * 1.4, cx + r * 0.15, cy - r * 1.55));
        // 火花
        g.setColor(new Color(0xFFC93C));
        g.fill(new Ellipse2D.Double(cx + r * 0.02, cy - r * 1.72, r * 0.5, r * 0.5));
        // 弹体
        g.setColor(new Color(0x3C3F41));
        g.fill(new Ellipse2D.Double(cx - r, cy - r, 2 * r, 2 * r));
        // 高光
        g.setColor(new Color(255, 255, 255, 190));
        g.fill(new Ellipse2D.Double(cx - r * 0.55, cy - r * 0.55, r * 0.6, r * 0.6));
        g.dispose();
        return new ImageIcon(img);
    }

    /** 红旗图标 */
    public static ImageIcon flag(int size) {
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        double cx = size / 2.0;
        float sw = Math.max(1.5f, size / 10f);
        // 旗杆
        g.setColor(new Color(0x5F6368));
        g.setStroke(new BasicStroke(sw, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(new Line2D.Double(cx, size * 0.12, cx, size * 0.86));
        // 旗面（三角）
        g.setColor(new Color(0xD93025));
        Path2D p = new Path2D.Double();
        p.moveTo(cx + 0.5, size * 0.14);
        p.lineTo(size * 0.92, size * 0.36);
        p.lineTo(cx + 0.5, size * 0.56);
        p.closePath();
        g.fill(p);
        // 底座
        g.setColor(new Color(0x5F6368));
        g.fill(new Ellipse2D.Double(cx - size * 0.22, size * 0.82, size * 0.44, size * 0.12));
        g.dispose();
        return new ImageIcon(img);
    }

    /** 时钟图标 */
    public static ImageIcon clock(int size) {
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        double cx = size / 2.0, cy = size / 2.0, r = size * 0.42;
        float sw = Math.max(1.5f, size / 9f);
        g.setColor(new Color(0x4A5568));
        g.setStroke(new BasicStroke(sw, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(new Ellipse2D.Double(cx - r, cy - r, 2 * r, 2 * r));
        g.draw(new Line2D.Double(cx, cy, cx, cy - r * 0.6));
        g.draw(new Line2D.Double(cx, cy, cx + r * 0.5, cy + r * 0.12));
        double c = Math.max(1.5, size / 14.0);
        g.fill(new Ellipse2D.Double(cx - c, cy - c, 2 * c, 2 * c));
        g.dispose();
        return new ImageIcon(img);
    }

    /** 对勾（成功）图标 */
    public static ImageIcon check(int size) {
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(0x2E9E5B));
        g.fill(new Ellipse2D.Double(0, 0, size, size));
        g.setColor(Color.WHITE);
        g.setStroke(new BasicStroke(Math.max(2f, size / 7f),
                BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(new Line2D.Double(size * 0.28, size * 0.52, size * 0.45, size * 0.68));
        g.draw(new Line2D.Double(size * 0.45, size * 0.68, size * 0.74, size * 0.34));
        g.dispose();
        return new ImageIcon(img);
    }
}
