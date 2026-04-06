package Tetris;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class KeyHandler implements KeyListener {

    public static boolean upPressed;
    public static boolean downPressed;
    public static boolean leftPressed;
    public static boolean rightPressed;
    public static boolean pausePressed;
    public static boolean restartPressed;

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();

        if (code == KeyEvent.VK_UP) {
            upPressed = true;
        }

        if (code == KeyEvent.VK_LEFT) {
            leftPressed = true;
        }

        if (code == KeyEvent.VK_DOWN) {
            downPressed = true;
        }

        if (code == KeyEvent.VK_RIGHT) {
            rightPressed = true;
        }

        if (code == KeyEvent.VK_ENTER) {
            restartPressed = true;
        }

        if (code == KeyEvent.VK_SPACE) {
            if (pausePressed) {
                pausePressed = false;
                if (!GamePanel.music.isPlaying()) {
                    GamePanel.music.play(0, true);
                    GamePanel.music.loop();
                }
            } else {
                pausePressed = true;
                GamePanel.music.stop();
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();

        if (code == KeyEvent.VK_UP) {
            upPressed = false;
        }

        if (code == KeyEvent.VK_LEFT) {
            leftPressed = false;
        }

        if (code == KeyEvent.VK_DOWN) {
            downPressed = false;
        }

        if (code == KeyEvent.VK_RIGHT) {
            rightPressed = false;
        }

        if (code == KeyEvent.VK_ENTER) {
            restartPressed = false;
        }
    }
}