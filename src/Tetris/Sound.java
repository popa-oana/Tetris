package Tetris;

import javax.sound.sampled.*;
import java.net.URL;
import java.util.Objects;

public class Sound {

    private Clip musicClip;
    private final URL[] url = new URL[10];
    private float musicVolume = 1.0f;

    public Sound() {
        try {
            url[0] = Objects.requireNonNull(getClass().getResource("/res/background.wav"));
            url[1] = Objects.requireNonNull(getClass().getResource("/res/delete line.wav"));
            url[2] = Objects.requireNonNull(getClass().getResource("/res/gameover.wav"));
            url[3] = Objects.requireNonNull(getClass().getResource("/res/rotation.wav"));
            url[4] = Objects.requireNonNull(getClass().getResource("/res/touch floor.wav"));
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    public boolean isPlaying() {
        return musicClip != null && musicClip.isRunning();
    }

    public void play(int i, boolean music) {
        try {
            if (url[i] == null) {
                System.out.println("Resource not found for index: " + i);
                return;
            }

            AudioInputStream ais = AudioSystem.getAudioInputStream(url[i]);

            if (music) {
                if (musicClip != null) {
                    if (musicClip.isRunning()) {
                        musicClip.stop();
                    }
                    if (musicClip.isOpen()) {
                        musicClip.close();
                    }
                }

                musicClip = AudioSystem.getClip();
                musicClip.open(ais);
                applyVolumeToClip(musicClip, musicVolume);
                musicClip.setFramePosition(0);
                musicClip.start();

            } else {
                Clip clip = AudioSystem.getClip();
                clip.open(ais);
                clip.setFramePosition(0);
                clip.start();

                clip.addLineListener(event -> {
                    if (event.getType() == LineEvent.Type.STOP) {
                        clip.close();
                    }
                });
            }

            ais.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loop() {
        if (musicClip != null && musicClip.isOpen()) {
            musicClip.loop(Clip.LOOP_CONTINUOUSLY);
        }
    }

    public void stop() {
        if (musicClip != null) {
            if (musicClip.isRunning()) {
                musicClip.stop();
            }
            if (musicClip.isOpen()) {
                musicClip.close();
            }
        }
    }

    public void setVolume(float value) {
        musicVolume = Math.max(0.0f, Math.min(1.0f, value));

        if (musicClip != null && musicClip.isOpen()) {
            applyVolumeToClip(musicClip, musicVolume);
        }
    }

    public float getVolume() {
        return musicVolume;
    }

    private void applyVolumeToClip(Clip clip, float volume) {
        if (clip == null || !clip.isOpen()) {
            return;
        }

        try {
            FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);

            float min = gainControl.getMinimum();
            float max = gainControl.getMaximum();

            if (volume <= 0.0001f) {
                gainControl.setValue(min);
                return;
            }

            float curved = 0.15f + 0.85f * volume;
            float dB = min + (max - min) * curved;

            if (dB < min) dB = min;
            if (dB > max) dB = max;

            gainControl.setValue(dB);

        } catch (IllegalArgumentException e) {
            System.out.println("Volume control not supported for this clip.");
        }
    }
}