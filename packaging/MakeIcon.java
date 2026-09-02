import minesweeper.gfx.MinesweeperIcon;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

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
        writeShortLE(out, 0);
        writeShortLE(out, 1);
        writeShortLE(out, count);
        int offset = 6 + 16 * count;
        for (int i = 0; i < count; i++) {
            int s = sizes[i];
            out.write(s == 256 ? 0 : s);
            out.write(s == 256 ? 0 : s);
            out.write(0);
            out.write(0);
            writeShortLE(out, 1);
            writeShortLE(out, 32);
            writeIntLE(out, pngs.get(i).length);
            writeIntLE(out, offset);
            offset += pngs.get(i).length;
        }
        for (byte[] p : pngs) {
            out.write(p);
        }

        String path = args.length > 0 ? args[0] : "icon.ico";
        Files.write(Paths.get(path), out.toByteArray());
        System.out.println("icon.ico written: " + out.size() + " bytes, sizes=" + sizes.length);
    }

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
