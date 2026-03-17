package Tetris;

import javax.swing.*;
import java.awt.*;

public class GameUI {

    private static final int BUTTON_WIDTH = 200;
    private static final int BUTTON_HEIGHT = 50;

    private static final int BUTTON_X = 20;

    // butoane mutate mai jos ca sa nu se mai apropie de logo
    private static final int PAUSE_BUTTON_Y = 140;
    private static final int RESTART_BUTTON_Y = PAUSE_BUTTON_Y + BUTTON_HEIGHT + 20;

    private final PlayManager playManager;
    private final GamePanel gamePanel;

    private JButton pauseButton;
    private JButton restartButton;
    private JButton nextLevelButton;
    private JButton replayLevelButton;

    public GameUI(PlayManager playManager, GamePanel gamePanel) {
        this.playManager = playManager;
        this.gamePanel = gamePanel;

        Font sideButtonFont = new Font("Roboto", Font.BOLD, 30);
        Font popupButtonFont = new Font("Roboto", Font.BOLD, 24);

        // Pause / Resume
        pauseButton = new JButton("Pause");
        pauseButton.setBounds(BUTTON_X, PAUSE_BUTTON_Y, BUTTON_WIDTH, BUTTON_HEIGHT);
        styleSideButton(pauseButton, sideButtonFont);
        pauseButton.addActionListener(e -> {
            togglePause();
            gamePanel.requestFocusInWindow();
        });

        // Restart
        restartButton = new JButton("Restart");
        restartButton.setBounds(BUTTON_X, RESTART_BUTTON_Y, BUTTON_WIDTH, BUTTON_HEIGHT);
        styleSideButton(restartButton, sideButtonFont);
        restartButton.addActionListener(e -> {
            playManager.restartGame();
            gamePanel.requestFocusInWindow();
        });

        // butoane popup pentru level complete
        nextLevelButton = new JButton("Next Level");
        nextLevelButton.setBounds(PlayManager.left_x + 85, PlayManager.top_y + 405, 190, 48);
        stylePopupButton(nextLevelButton, popupButtonFont);
        nextLevelButton.addActionListener(e -> {
            playManager.goToNextLevel();
            gamePanel.requestFocusInWindow();
        });

        replayLevelButton = new JButton("Replay");
        replayLevelButton.setBounds(PlayManager.left_x + 85, PlayManager.top_y + 480, 190, 48);
        stylePopupButton(replayLevelButton, popupButtonFont);
        replayLevelButton.addActionListener(e -> {
            playManager.replayCurrentLevel();
            gamePanel.requestFocusInWindow();
        });

        nextLevelButton.setVisible(false);
        replayLevelButton.setVisible(false);
    }

    private void styleSideButton(JButton button, Font font) {
        button.setFont(font);
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(0, 0, 0, 0));
        button.setBorder(BorderFactory.createLineBorder(Color.WHITE, 4));
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
    }

    private void stylePopupButton(JButton button, Font font) {
        button.setFont(font);
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(8, 8, 18, 245));
        button.setOpaque(true);
        button.setBorder(BorderFactory.createLineBorder(Color.WHITE, 3));
        button.setFocusPainted(false);
    }

    private void togglePause() {
        if (playManager.isGameOver() || playManager.isLevelCompleted()) {
            return;
        }

        KeyHandler.pausePressed = !KeyHandler.pausePressed;
        pauseButton.setText(KeyHandler.pausePressed ? "Resume" : "Pause");

        if (KeyHandler.pausePressed) {
            GamePanel.music.stop();
        } else {
            GamePanel.music.play(0, true);
            GamePanel.music.loop();
        }
    }

    public void syncPauseButton() {
        pauseButton.setText(KeyHandler.pausePressed ? "Resume" : "Pause");
    }

    public void showLevelCompleteButtons(boolean show) {
        nextLevelButton.setVisible(show);
        replayLevelButton.setVisible(show);
    }

    public void addToPanel(JPanel panel) {
        panel.add(pauseButton);
        panel.add(restartButton);
        panel.add(nextLevelButton);
        panel.add(replayLevelButton);
    }
}