package Tetris;

import javax.swing.*;
import java.awt.*;

public class GameUI {

    private final PlayManager playManager;
    private final GamePanel gamePanel;

    private JButton pauseButton;
    private JButton restartButton;
    private JButton nextLevelButton;
    private JButton replayLevelButton;

    public GameUI(PlayManager playManager, GamePanel gamePanel) {
        this.playManager = playManager;
        this.gamePanel = gamePanel;
        createButtons();
    }

    private void createButtons() {
        pauseButton = createStyledButton("Pause");
        restartButton = createStyledButton("Restart");
        nextLevelButton = createStyledButton("Next Level");
        replayLevelButton = createStyledButton("Replay");

        pauseButton.setBounds(25, 175, 190, 56);
        restartButton.setBounds(25, 255, 190, 56);

        nextLevelButton.setBounds(PlayManager.left_x + 70, PlayManager.top_y + 410, 220, 52);
        replayLevelButton.setBounds(PlayManager.left_x + 70, PlayManager.top_y + 472, 220, 52);

        nextLevelButton.setVisible(false);
        replayLevelButton.setVisible(false);

        pauseButton.addActionListener(e -> togglePause());

        restartButton.addActionListener(e -> {
            KeyHandler.restartPressed = false;
            playManager.restartGame();
            syncPauseButton();
            gamePanel.requestFocusInWindow();
        });

        nextLevelButton.addActionListener(e -> {
            playManager.goToNextLevel();
            syncPauseButton();
            gamePanel.requestFocusInWindow();
        });

        replayLevelButton.addActionListener(e -> {
            playManager.replayCurrentLevel();
            syncPauseButton();
            gamePanel.requestFocusInWindow();
        });
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Roboto", Font.BOLD, 21));
        button.setBackground(new Color(20, 20, 35, 220));
        button.setBorder(BorderFactory.createLineBorder(Color.WHITE, 3));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    public void addToPanel(JPanel panel) {
        panel.add(pauseButton);
        panel.add(restartButton);
        panel.add(nextLevelButton);
        panel.add(replayLevelButton);
    }

    private void togglePause() {
        if (playManager.isGameOver() || playManager.isLevelCompleted()) {
            return;
        }

        KeyHandler.pausePressed = !KeyHandler.pausePressed;

        if (KeyHandler.pausePressed) {
            GamePanel.music.stop();
        } else {
            if (!GamePanel.music.isPlaying()) {
                GamePanel.music.play(0, true);
                GamePanel.music.loop();
            }
        }

        syncPauseButton();
        gamePanel.requestFocusInWindow();
    }

    public void syncPauseButton() {
        pauseButton.setText(KeyHandler.pausePressed ? "Resume" : "Pause");
    }

    public void showLevelCompleteButtons(boolean show) {
        nextLevelButton.setVisible(show);
        replayLevelButton.setVisible(show);

        if (show) {
            nextLevelButton.repaint();
            replayLevelButton.repaint();
        }
    }
}
