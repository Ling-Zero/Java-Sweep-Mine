package minesweeper.audio;


import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;

public final class SoundPlayer {

    private static final int SAMPLE_RATE = 44100;
    private static final double VOLUME = 0.3;
    private static volatile boolean muted = false;

    private SoundPlayer() {
    }

    public static void setMuted(boolean m) {
        muted = m;
    }

    public static void playClick() {
        play(new Note(523.25, 80),
                new Note(659.25, 80),
                new Note(783.99, 170));
    }

    public static void playMark() {
        play(new Note(880.00, 90),
                new Note(1174.66, 150));
    }

    public static void playUnmark() {
        play(new Note(587.33, 110));
    }

    public static void playWin() {
        play(new Note(523.25, 110),
                new Note(659.25, 110),
                new Note(783.99, 110),
                new Note(1046.50, 280));
    }

    public static void playLose() {
        play(new Note(392.00, 160),
                new Note(311.13, 160),
                new Note(261.63, 320));
    }

    public record Note(double freq, int ms) {
    }

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
                    Thread.sleep(25);
                }
                line.drain();
                line.stop();
                line.close();
            } catch (Exception ignored) {
            }
        }, "sound-player");
        t.setDaemon(true);
        t.start();
    }

    private static void writeTone(SourceDataLine line, double freq, int ms) {
        int total = (int) (SAMPLE_RATE * ms / 1000.0);
        byte[] buf = new byte[total * 2];
        double fade = 0.03;
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
