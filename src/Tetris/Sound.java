package Tetris;
import  java.net.URL;
import java.util.Objects;
import javax.sound.sampled.*;

public class Sound {
    Clip musicClip;
    URL[] url = new URL[10];

    public Sound() {
        try {
            url[0] = Objects.requireNonNull(getClass().getResource("/res/background.wav"));
            url[1] = Objects.requireNonNull(getClass().getResource("/res/delete line.wav"));
            url[2] = Objects.requireNonNull(getClass().getResource("/res/gameover.wav"));
            url[3] = Objects.requireNonNull(getClass().getResource("/res/rotation.wav"));
            url[4] = Objects.requireNonNull(getClass().getResource("/res/touch floor.wav"));
        } catch (NullPointerException e) {
            e.printStackTrace();          // one of the files wasn’t found on the class-path
        }

        // Initialize musicClip
        try {
            musicClip = AudioSystem.getClip();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    public boolean isPlaying() {
        if (musicClip != null) { // Check if musicClip is initialized
            return musicClip.isRunning();
        }
        return false;
    }


    public void play(int i, boolean music) {
        try {
            if (url[i] == null) {
                System.out.println("Resource not found for index: " + i);
                return;
            }

            AudioInputStream ais = AudioSystem.getAudioInputStream(url[i]);
            Clip clip = music ? musicClip : AudioSystem.getClip();

            if (clip.isRunning()) {
                clip.stop(); // Stop the clip if it's already playing
            }

            clip.open(ais);
            clip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    clip.close();
                }
            });

            ais.close();
            clip.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    public void loop() {
        if (musicClip != null) { // Check if musicClip is initialized
            musicClip.loop(Clip.LOOP_CONTINUOUSLY);
        }
    }

    public void stop() {
        if (musicClip != null) { // Check if musicClip is initialized
            musicClip.stop();
            musicClip.close();
        }
    }
}
