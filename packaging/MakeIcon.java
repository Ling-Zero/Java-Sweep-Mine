import minesweeper.gfx.MinesweeperIcon;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * 打包工具：调用 {@link MinesweeperIcon} 生成多尺寸 icon.ico（PNG 内嵌格式），
 * 供 jpackage --icon 嵌入到 exe 文件图标。
 * 用法：java -cp out;packaging-out MakeIcon [输出路径]
 */
public class MakeIcon {

    public static void main(String[] args) throws Exception {
        int[] sizes = {16, 24, 32, 48, 64, 128, 256};
        List<byte[]> pngs = new ArrayList<>();
        for (int s : sizes) {
            BufferedImage im = MinesweeperIcon.create(s);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ImageIO.write(im, "png", bos);
            pngs.add(bos.toByteArray());
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int count = sizes.length;
        // ICONDIR（小端序）
        writeShortLE(out, 0);          // reserved
        writeShortLE(out, 1);          // type: icon
        writeShortLE(out, count);
        // ICONDIRENTRY * count（小端序）
        int offset = 6 + 16 * count;
        for (int i = 0; i < count; i++) {
            int s = sizes[i];
            out.write(s == 256 ? 0 : s);   // width
            out.write(s == 256 ? 0 : s);   // height
            out.write(0);                  // color count
            out.write(0);                  // reserved
            writeShortLE(out, 1);          // planes
            writeShortLE(out, 32);         // bit count
            writeIntLE(out, pngs.get(i).length);  // bytes in resource
            writeIntLE(out, offset);       // image offset
            offset += pngs.get(i).length;
        }
        for (byte[] p : pngs) {
            out.write(p);
        }

        String path = args.length > 0 ? args[0] : "icon.ico";
        Files.write(Paths.get(path), out.toByteArray());
        System.out.println("icon.ico written: " + out.size() + " bytes, sizes=" + sizes.length);
    }

    /** ICO 格式为小端序，手动写入 */
    private static void writeShortLE(ByteArrayOutputStream out, int v) {
        out.write(v & 0xFF);
        out.write((v >> 8) & 0xFF);
    }

    private static void writeIntLE(ByteArrayOutputStream out, int v) {
        out.write(v & 0xFF);
        out.write((v >> 8) & 0xFF);
        out.write((v >> 16) & 0xFF);
        out.write((v >> 24) & 0xFF);
    }
}
