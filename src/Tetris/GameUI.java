package Tetris;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;

public class GameUI {
    private static final int BUTTON_WIDTH = 200;
    private static final int BUTTON_HEIGHT = 50;
    private final int BUTTON_X = 20;
    private final int PAUSE_BUTTON_Y = 120;
    private final int RESTART_BUTTON_Y = PAUSE_BUTTON_Y + BUTTON_HEIGHT + 20;
    private GamePanel gamePanel;
    
    private JButton pauseButton;
    private JButton restartButton;
    private JLabel highScoreLabel;
    private int highScore;
    private File highScoreFile;

    public GameUI() {
        highScoreFile = new File("highscore.txt");
        loadHighScore();
        
        pauseButton = new JButton("Pause");
        pauseButton.setBounds(BUTTON_X, PAUSE_BUTTON_Y, BUTTON_WIDTH, BUTTON_HEIGHT);
        pauseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                togglePause();
            }
        });

        restartButton = new JButton("Restart");
        restartButton.setBounds(BUTTON_X, RESTART_BUTTON_Y, BUTTON_WIDTH, BUTTON_HEIGHT);
        restartButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                restartGame();
            }
        });

        highScoreLabel = new JLabel("HIGH SCORE: " + highScore);
        highScoreLabel.setBounds(BUTTON_X, 200, BUTTON_WIDTH, 30);
        highScoreLabel.setFont(new Font("Arial", Font.BOLD, 14));
    }

    private void togglePause() {
        KeyHandler.pausePressed = !KeyHandler.pausePressed;
        pauseButton.setText(KeyHandler.pausePressed ? "Resume" : "Pause");
        if (KeyHandler.pausePressed) {
            GamePanel.music.stop();
        } else {
            GamePanel.music.play(0, true);
            GamePanel.music.loop();
        }
        gamePanel.requestFocusInWindow();
    }

    public void syncPauseButton() {
        pauseButton.setText(KeyHandler.pausePressed ? "Resume" : "Pause");
    }

    private PlayManager playManager;

    public GameUI(PlayManager playManager, GamePanel gamePanel) {
        this.playManager = playManager;
        this.gamePanel = gamePanel;
        highScoreFile = new File("highscore.txt");
        loadHighScore();

        Font buttonFont = new Font("Roboto", Font.BOLD, 30);
        
        pauseButton = new JButton("Pause");
        pauseButton.setBounds(BUTTON_X, PAUSE_BUTTON_Y, BUTTON_WIDTH, BUTTON_HEIGHT);
        pauseButton.setFont(buttonFont);
        pauseButton.setForeground(Color.WHITE);
        pauseButton.setBackground(new Color(0, 0, 0, 0));
        pauseButton.setBorder(BorderFactory.createLineBorder(Color.WHITE, 4));
        pauseButton.setContentAreaFilled(false);
        pauseButton.setFocusPainted(false);
        pauseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                togglePause();
                gamePanel.requestFocusInWindow();
            }
        });

        restartButton = new JButton("Restart");
        restartButton.setBounds(BUTTON_X, RESTART_BUTTON_Y, BUTTON_WIDTH, BUTTON_HEIGHT);
        restartButton.setFont(buttonFont);
        restartButton.setForeground(Color.WHITE);
        restartButton.setBackground(new Color(0, 0, 0, 0));
        restartButton.setBorder(BorderFactory.createLineBorder(Color.WHITE, 4));
        restartButton.setContentAreaFilled(false);
        restartButton.setFocusPainted(false);
        restartButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                restartGame();
                gamePanel.requestFocusInWindow();
            }
        });

        highScoreLabel = new JLabel("HIGH SCORE: " + highScore);
        highScoreLabel.setBounds(BUTTON_X, 200, BUTTON_WIDTH, 30);
        highScoreLabel.setFont(new Font("Arial", Font.BOLD, 14));
    }

    private void restartGame() {
        playManager.score = 0;
        playManager.level = 1;
        playManager.lines = 0;
        PlayManager.staticBlocks.clear();
        playManager.currentMino.setXY(playManager.MINO_START_X, playManager.MINO_START_Y);
        playManager.nextMino.setXY(playManager.NEXTMINO_X, playManager.NEXTMINO_Y);
    }

    private void loadHighScore() {
        try {
            if (highScoreFile.exists()) {
                BufferedReader reader = new BufferedReader(new FileReader(highScoreFile));
                String content = reader.readLine();
                if (content != null && !content.isEmpty()) {
                    highScore = Integer.parseInt(content.trim());
                }
                reader.close();
            }
        } catch (IOException | NumberFormatException e) {
            highScore = 0;
        }
    }

    public void updateHighScore(int currentScore) {
        if (currentScore > highScore) {
            highScore = currentScore;
            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(highScoreFile));
                writer.write(String.valueOf(highScore));
                writer.newLine(); // Add new line for better file format
                writer.close();
                highScoreLabel.setText("HIGH SCORE: " + highScore); // Update UI immediately
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public int getHighScore() {
        return highScore;
    }

    public void addToPanel(JPanel panel) {
        panel.add(pauseButton);
        panel.add(restartButton);
    }
}
