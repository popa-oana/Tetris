package Tetris;

import javax.sound.sampled.*;
import java.net.URL;
import java.util.Objects;

public class Sound {

    private Clip musicClip;
    URL[] url = new URL[10];

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
                // Pentru muzica de fundal folosim un singur clip reutilizabil
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
                musicClip.setFramePosition(0);
                musicClip.start();

            } else {
                // Pentru efecte sonore folosim clip separat
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
}