package minesweeper.audio;


import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;

/**
 * 音效播放器：用正弦波实时合成简短的提示音，无需任何外部音频文件。
 * <ul>
 *   <li>左键点到安全格：短促愉悦的琶音（C5-E5-G5）</li>
 *   <li>右键标记地雷：清脆的双音提示</li>
 *   <li>取消标记：低一档的提示音</li>
 *   <li>过关：上行的成功旋律</li>
 *   <li>踩雷：下行的失败音</li>
 * </ul>
 * 播放在线程中进行，不会阻塞界面。
 */
public final class SoundPlayer {

    private static final int SAMPLE_RATE = 44100;
    /** 总体音量（0~1）：0.3 约为原音量(0.8)的 1/3，听起来轻柔适中 */
    private static final double VOLUME = 0.3;
    private static volatile boolean muted = false;

    private SoundPlayer() {
    }

    public static void setMuted(boolean m) {
        muted = m;
    }

    /** 左键点到安全格：简短愉悦的音乐 */
    public static void playClick() {
        play(new Note(523.25, 80),   // C5
                new Note(659.25, 80), // E5
                new Note(783.99, 170)); // G5
    }

    /** 右键标记地雷：提示音乐 */
    public static void playMark() {
        play(new Note(880.00, 90),   // A5
                new Note(1174.66, 150)); // D6
    }

    /** 右键取消标记：提示音乐 */
    public static void playUnmark() {
        play(new Note(587.33, 110)); // D5
    }

    /** 过关：上行成功旋律 */
    public static void playWin() {
        play(new Note(523.25, 110),
                new Note(659.25, 110),
                new Note(783.99, 110),
                new Note(1046.50, 280));
    }

    /** 踩雷：下行失败音 */
    public static void playLose() {
        play(new Note(392.00, 160),  // G4
                new Note(311.13, 160), // Eb4
                new Note(261.63, 320)); // C4
    }

    /** 一个音符：频率（Hz）与时长（ms） */
    public record Note(double freq, int ms) {
    }

    /** 依次播放若干音符 */
    private static void play(Note... notes) {
        if (muted) {
            return;
        }
        Thread t = new Thread(() -> {
            try {
                AudioFormat fmt = new AudioFormat(SAMPLE_RATE, 16, 1, true, false);
                SourceDataLine line = AudioSystem.getSourceDataLine(fmt);
                line.open(fmt);
                line.start();
                for (Note n : notes) {
                    writeTone(line, n.freq(), n.ms());
                    Thread.sleep(25); // 音与音之间稍作停顿
                }
                line.drain();
                line.stop();
                line.close();
            } catch (Exception ignored) {
                // 无声卡等环境静默失败，不影响游戏运行
            }
        }, "sound-player");
        t.setDaemon(true);
        t.start();
    }

    /** 合成并写入一段带淡入淡出的正弦波（含少量二次谐波，音色更柔和） */
    private static void writeTone(SourceDataLine line, double freq, int ms) {
        int total = (int) (SAMPLE_RATE * ms / 1000.0);
        byte[] buf = new byte[total * 2];
        double fade = 0.03; // 首尾 3% 淡入淡出，避免爆音
        for (int i = 0; i < total; i++) {
            double t = (double) i / SAMPLE_RATE;
            double pos = (double) i / total;
            double env = 1.0;
            if (pos < fade) {
                env = pos / fade;
            } else if (pos > 1 - fade) {
                env = (1 - pos) / fade;
            }
            double v = (Math.sin(2 * Math.PI * freq * t)
                    + 0.35 * Math.sin(2 * Math.PI * freq * 2 * t)) * 0.5 * env;
            short s = (short) (v * Short.MAX_VALUE * VOLUME);
            buf[i * 2] = (byte) (s & 0xff);
            buf[i * 2 + 1] = (byte) ((s >> 8) & 0xff);
        }
        line.write(buf, 0, buf.length);
    }
}
