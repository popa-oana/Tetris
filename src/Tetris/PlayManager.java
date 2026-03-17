package Tetris;

import Mino.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Random;

import static Tetris.GamePanel.music;

public class PlayManager {

    final int WIDTH = 360;
    final int HEIGHT = 600;
    public static int left_x;
    public static int right_x;
    public static int top_y;
    public static int bottom_y;

    private BufferedImage wallpaper;
    private BufferedImage blurredWallpaper;
    private BufferedImage playArea;
    private BufferedImage pauseImage;
    private BufferedImage rotatedPauseImage;
    private BufferedImage gameOverImage;
    private BufferedImage tetrisImage;

    private BufferedImage scorePanelCrop;
    private BufferedImage nextPanelCrop;

    Mino currentMino;
    final int MINO_START_X;
    final int MINO_START_Y;
    Mino nextMino;
    final int NEXTMINO_X;
    final int NEXTMINO_Y;

    public static ArrayList<Block> staticBlocks = new ArrayList<>();

    public static int dropInterval = 60;
    boolean gameOver;
    boolean levelCompleted;

    boolean effectCounterOn;
    int effectCounter;
    ArrayList<Integer> effectY = new ArrayList<>();

    private int animationTick = 0;

    int level = 1;
    int score;
    int levelTargetScore;
    int currentLevelScore;
    int scoreAtLevelStart;

    private static final int MIN_DROP_INTERVAL = 8;

    private GameUI gameUI;

    public PlayManager() {
        left_x = (GamePanel.WIDTH / 2) - (WIDTH / 2);
        right_x = left_x + WIDTH;
        top_y = 50;
        bottom_y = top_y + HEIGHT;

        MINO_START_X = left_x + (WIDTH / 2) - Block.SIZE;
        MINO_START_Y = top_y + Block.SIZE;
        NEXTMINO_X = right_x + 175;
        NEXTMINO_Y = top_y + 500;

        loadImages();

        levelTargetScore = calculateTargetScore(level);
        currentLevelScore = 0;
        scoreAtLevelStart = 0;
        dropInterval = calculateDropInterval(level);

        currentMino = pickMino();
        currentMino.setXY(MINO_START_X, MINO_START_Y);

        nextMino = pickMino();
        nextMino.setXY(NEXTMINO_X, NEXTMINO_Y);
    }

    private void loadImages() {
        wallpaper = ImageUtils.loadImage("/res/wallpaper.png");
        playArea = ImageUtils.loadImage("/res/playzone.png");
        pauseImage = ImageUtils.loadImage("/res/pauseText.png");
        gameOverImage = ImageUtils.loadImage("/res/gameoverText.png");
        tetrisImage = ImageUtils.loadImage("/res/tetris.png");

        // SCALE
        if (tetrisImage != null) {
            tetrisImage = ImageUtils.scaleImage(tetrisImage, 210, 125);
        }

        // BLUR + CROP
        if (wallpaper != null) {
            blurredWallpaper = ImageUtils.blurImage(wallpaper);

            BufferedImage crop1 = ImageUtils.cropImage(
                    wallpaper,
                    80,
                    40,
                    Math.max(200, wallpaper.getWidth() / 3),
                    Math.max(160, wallpaper.getHeight() / 3)
            );
            scorePanelCrop = ImageUtils.scaleImage(crop1, 292, 222);

            BufferedImage crop2 = ImageUtils.cropImage(
                    wallpaper,
                    Math.max(0, wallpaper.getWidth() - 260),
                    Math.max(0, wallpaper.getHeight() - 260),
                    220,
                    220
            );
            nextPanelCrop = ImageUtils.scaleImage(crop2, 192, 192);
        }

        // ROTATE
        if (pauseImage != null) {
            rotatedPauseImage = ImageUtils.rotateImage(pauseImage, -18);
        }
    }

    public void setGameUI(GameUI gameUI) {
        this.gameUI = gameUI;
    }

    public boolean isLevelCompleted() {
        return levelCompleted;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    private Mino pickMino() {
        Mino mino = null;
        int i = new Random().nextInt(7);

        switch (i) {
            case 0: mino = new Mino_L1(); break;
            case 1: mino = new Mino_L2(); break;
            case 2: mino = new Mino_Square(); break;
            case 3: mino = new Mino_Bar(); break;
            case 4: mino = new Mino_T(); break;
            case 5: mino = new Mino_Z1(); break;
            case 6: mino = new Mino_Z2(); break;
        }

        return mino;
    }

    private int calculateTargetScore(int level) {
        return 300 + (level - 1) * 200;
    }

    private int calculateDropInterval(int level) {
        int value = 60 - (level - 1) * 6;
        return Math.max(value, MIN_DROP_INTERVAL);
    }

    public int getRemainingPoints() {
        return Math.max(0, levelTargetScore - currentLevelScore);
    }

    // =========================
    // RLE - COMPRESIA TABLEI
    // =========================

    public int[][] buildBoardMatrix() {
        int rows = HEIGHT / Block.SIZE;
        int cols = WIDTH / Block.SIZE;

        int[][] board = new int[rows][cols];

        for (Block block : staticBlocks) {
            int col = (block.x - left_x) / Block.SIZE;
            int row = (block.y - top_y) / Block.SIZE;

            if (row >= 0 && row < rows && col >= 0 && col < cols) {
                board[row][col] = 1;
            }
        }

        return board;
    }

    public String boardMatrixToBinaryString() {
        int[][] board = buildBoardMatrix();
        StringBuilder sb = new StringBuilder();

        for (int row = 0; row < board.length; row++) {
            for (int col = 0; col < board[row].length; col++) {
                sb.append(board[row][col]);
            }
        }

        return sb.toString();
    }

    public String getCompressedBoardRLE() {
        String binaryBoard = boardMatrixToBinaryString();
        return RLECompressor.compress(binaryBoard);
    }

    public String getCompressionStats() {
        String original = boardMatrixToBinaryString();
        String compressed = getCompressedBoardRLE();

        return "Original length = " + original.length() +
                " | Compressed length = " + compressed.length();
    }

    private void printBoardCompressionInfo() {
        String original = boardMatrixToBinaryString();
        String compressed = getCompressedBoardRLE();

        System.out.println("======================================");
        System.out.println("Board binary: " + original);
        System.out.println("Board RLE:    " + compressed);
        System.out.println(getCompressionStats());
        System.out.println("======================================");
    }

    private void addScore(int points) {
        score += points;
        currentLevelScore += points;

        if (currentLevelScore >= levelTargetScore && !levelCompleted) {
            levelCompleted = true;
            KeyHandler.pausePressed = false;
            music.stop();

            if (gameUI != null) {
                gameUI.showLevelCompleteButtons(true);
            }
        }
    }

    public void update() {
        if (gameOver || levelCompleted) {
            return;
        }

        if (!currentMino.active) {
            staticBlocks.add(currentMino.b[0]);
            staticBlocks.add(currentMino.b[1]);
            staticBlocks.add(currentMino.b[2]);
            staticBlocks.add(currentMino.b[3]);

            if (currentMino.b[0].x == MINO_START_X && currentMino.b[0].y == MINO_START_Y) {
                gameOver = true;
                music.stop();
                GamePanel.se.play(2, false);
            }

            currentMino.deactivating = false;

            currentMino = nextMino;
            currentMino.setXY(MINO_START_X, MINO_START_Y);

            nextMino = pickMino();
            nextMino.setXY(NEXTMINO_X, NEXTMINO_Y);

            checkDelete();

            // Afisare compresie RLE dupa fiecare piesa fixata
            printBoardCompressionInfo();

        } else {
            currentMino.update();
        }
    }

    private void checkDelete() {
        int x = left_x;
        int y = top_y;
        int blockCount = 0;
        int lineCount = 0;

        while (x < right_x && y < bottom_y) {
            for (int i = 0; i < staticBlocks.size(); i++) {
                if (staticBlocks.get(i).x == x && staticBlocks.get(i).y == y) {
                    blockCount++;
                }
            }

            x += Block.SIZE;

            if (x == right_x) {
                if (blockCount == 12) {
                    effectCounterOn = true;
                    effectY.add(y);

                    for (int i = staticBlocks.size() - 1; i > -1; i--) {
                        if (staticBlocks.get(i).y == y) {
                            staticBlocks.remove(i);
                        }
                    }

                    lineCount++;

                    for (int i = 0; i < staticBlocks.size(); i++) {
                        if (staticBlocks.get(i).y < y) {
                            staticBlocks.get(i).y += Block.SIZE;
                        }
                    }
                }

                blockCount = 0;
                x = left_x;
                y += Block.SIZE;
            }
        }

        if (lineCount > 0) {
            GamePanel.se.play(1, false);

            int pointsEarned;
            switch (lineCount) {
                case 1: pointsEarned = 100 * level; break;
                case 2: pointsEarned = 250 * level; break;
                case 3: pointsEarned = 400 * level; break;
                case 4: pointsEarned = 600 * level; break;
                default: pointsEarned = 100 * lineCount * level; break;
            }

            addScore(pointsEarned);
        }
    }

    private void resetBoardOnly() {
        staticBlocks.clear();
        effectCounterOn = false;
        effectCounter = 0;
        effectY.clear();
        animationTick = 0;

        currentMino = pickMino();
        currentMino.setXY(MINO_START_X, MINO_START_Y);

        nextMino = pickMino();
        nextMino.setXY(NEXTMINO_X, NEXTMINO_Y);
    }

    public void goToNextLevel() {
        if (!levelCompleted) return;

        level++;
        levelCompleted = false;
        gameOver = false;

        levelTargetScore = calculateTargetScore(level);
        currentLevelScore = 0;
        scoreAtLevelStart = score;
        dropInterval = calculateDropInterval(level);

        resetBoardOnly();

        if (gameUI != null) {
            gameUI.showLevelCompleteButtons(false);
        }

        music.play(0, true);
        music.loop();
    }

    public void replayCurrentLevel() {
        score = scoreAtLevelStart;
        currentLevelScore = 0;
        levelCompleted = false;
        gameOver = false;
        dropInterval = calculateDropInterval(level);

        resetBoardOnly();

        if (gameUI != null) {
            gameUI.showLevelCompleteButtons(false);
        }

        music.play(0, true);
        music.loop();
    }

    public void restartGame() {
        staticBlocks.clear();

        level = 1;
        score = 0;
        scoreAtLevelStart = 0;
        currentLevelScore = 0;
        levelTargetScore = calculateTargetScore(level);

        dropInterval = calculateDropInterval(level);
        gameOver = false;
        levelCompleted = false;
        KeyHandler.pausePressed = false;
        animationTick = 0;

        effectCounterOn = false;
        effectCounter = 0;
        effectY.clear();

        currentMino = pickMino();
        currentMino.setXY(MINO_START_X, MINO_START_Y);

        nextMino = pickMino();
        nextMino.setXY(NEXTMINO_X, NEXTMINO_Y);

        if (gameUI != null) {
            gameUI.showLevelCompleteButtons(false);
        }

        music.play(0, true);
        music.loop();
    }

    private void drawCenteredText(Graphics2D g2, String text, Font font, int centerX, int y, Color color) {
        FontMetrics fm = g2.getFontMetrics(font);
        int textX = centerX - fm.stringWidth(text) / 2;
        g2.setFont(font);
        g2.setColor(color);
        g2.drawString(text, textX, y);
    }

    private void drawAnimatedWellPlayed(Graphics2D g2, int popupX, int popupY, int popupW) {
        animationTick++;

        double wave = Math.sin(animationTick * 0.10);
        int offsetY = (int) (wave * 3);

        int centerX = popupX + popupW / 2;
        int baseY = popupY + 88 + offsetY;

        Font titleFont = new Font("Roboto", Font.BOLD, 46);

        drawCenteredText(g2, "Well played!", titleFont, centerX + 2, baseY + 2, new Color(0, 0, 0, 170));
        drawCenteredText(g2, "Well played!", titleFont, centerX, baseY, Color.WHITE);
    }

    private void drawPanelDecor(Graphics2D g2, BufferedImage img, int x, int y, int w, int h) {
        if (img == null) return;

        Composite old = g2.getComposite();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.75f));
        g2.drawImage(img, x, y, w, h, null);
        g2.setComposite(old);

        g2.setColor(new Color(255, 255, 255, 55));
        g2.drawRect(x, y, w, h);
    }

    public void draw(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;

        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (KeyHandler.pausePressed && blurredWallpaper != null) {
            g2.drawImage(blurredWallpaper, 0, 0, GamePanel.WIDTH, GamePanel.HEIGHT, null);
            g2.setColor(new Color(15, 15, 35, 130));
            g2.fillRect(0, 0, GamePanel.WIDTH, GamePanel.HEIGHT);
        } else if (wallpaper != null) {
            g2.drawImage(wallpaper, 0, 0, GamePanel.WIDTH, GamePanel.HEIGHT, null);
        }

        g2.setColor(Color.WHITE);

        if (playArea != null) {
            g2.drawImage(playArea, left_x, top_y, WIDTH, HEIGHT, null);
        }

        g2.setStroke(new BasicStroke(4f));
        g2.drawRect(left_x - 4, top_y - 4, WIDTH + 8, HEIGHT + 8);

        int x = right_x + 100;
        int y = bottom_y - 200;

        drawPanelDecor(g2, nextPanelCrop, x + 4, y + 4, 192, 192);
        g2.setColor(new Color(255, 255, 255, 25));
        g2.fillRect(x + 4, y + 4, 192, 192);
        g2.setColor(Color.WHITE);

        g2.drawRect(x, y, 200, 200);
        g2.setFont(new Font("Roboto", Font.BOLD, 30));
        g2.drawString("NEXT", x + 60, y + 60);

        final int SCORE_PANEL_WIDTH = 300;
        final int SCORE_PANEL_HEIGHT = 230;
        final int SCORE_PANEL_PADDING = 22;
        final int TEXT_LINE_HEIGHT = 34;
        final int VERTICAL_SPACING = 14;
        final int TOP_MARGIN = 50;

        int scorePanelX = right_x + 100;
        int scorePanelY = top_y;

        drawPanelDecor(g2, scorePanelCrop, scorePanelX + 4, scorePanelY + 4, 292, 222);
        g2.setColor(new Color(255, 255, 255, 20));
        g2.fillRect(scorePanelX + 4, scorePanelY + 4, 292, 222);
        g2.setColor(Color.WHITE);

        g2.drawRect(scorePanelX, scorePanelY, SCORE_PANEL_WIDTH, SCORE_PANEL_HEIGHT);
        g2.setFont(new Font("Roboto", Font.BOLD, 27));

        int textStartX = scorePanelX + SCORE_PANEL_PADDING;
        int textStartY = scorePanelY + TOP_MARGIN;

        g2.drawString("LEVEL: " + level, textStartX, textStartY);
        textStartY += TEXT_LINE_HEIGHT + VERTICAL_SPACING;

        g2.drawString("SCORE: " + score, textStartX, textStartY);
        textStartY += TEXT_LINE_HEIGHT + VERTICAL_SPACING;

        g2.drawString("TARGET: " + levelTargetScore, textStartX, textStartY);
        textStartY += TEXT_LINE_HEIGHT + VERTICAL_SPACING;

        g2.drawString("REMAINING: " + getRemainingPoints(), textStartX, textStartY);

        if (currentMino != null) currentMino.draw(g2);
        if (nextMino != null) nextMino.draw(g2);

        for (int i = 0; i < staticBlocks.size(); i++) {
            staticBlocks.get(i).draw(g2);
        }

        if (effectCounterOn) {
            effectCounter++;

            int alpha = (int) (255 * (1 - (effectCounter / 10.0)));
            alpha = Math.max(alpha, 0);

            Color fadeColor = new Color(255, 255, 255, alpha);
            g2.setColor(fadeColor);

            for (int i = 0; i < effectY.size(); i++) {
                g2.fillRect(left_x, effectY.get(i), WIDTH, Block.SIZE);
            }

            if (effectCounter >= 10) {
                effectCounterOn = false;
                effectCounter = 0;
                effectY.clear();
            }

            g2.setColor(Color.WHITE);
        }

        if (gameOver) {
            x = left_x + 50;
            y = top_y + 230;
            int width = 270;
            int height = 120;

            if (gameOverImage != null) {
                g2.drawImage(gameOverImage, x, y, width, height, null);
            }

            g2.setColor(new Color(0, 0, 0, 210));
            g2.fillRoundRect(left_x + 35, top_y + 370, WIDTH - 70, 90, 25, 25);

            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Roboto", Font.BOLD, 26));
            g2.drawString("Press Enter to restart", left_x + 55, top_y + 425);
        }

        if (KeyHandler.pausePressed) {
            x = left_x + 35;
            y = top_y + 270;
            int width = 300;
            int height = 190;

            if (rotatedPauseImage != null) {
                g2.drawImage(rotatedPauseImage, x, y, width, height, null);
            } else if (pauseImage != null) {
                g2.drawImage(pauseImage, x, y, width, height, null);
            }
        }

        if (levelCompleted) {
            int popupX = left_x + 18;
            int popupY = top_y + 120;
            int popupW = WIDTH - 36;
            int popupH = 430;

            g2.setColor(new Color(8, 8, 18, 235));
            g2.fillRoundRect(popupX, popupY, popupW, popupH, 30, 30);

            g2.setColor(new Color(0, 0, 0, 120));
            g2.fillRoundRect(popupX + 12, popupY + 12, popupW - 24, popupH - 24, 24, 24);

            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(3f));
            g2.drawRoundRect(popupX, popupY, popupW, popupH, 30, 30);

            drawAnimatedWellPlayed(g2, popupX, popupY, popupW);

            drawCenteredText(
                    g2,
                    "LEVEL COMPLETE",
                    new Font("Roboto", Font.BOLD, 24),
                    popupX + popupW / 2,
                    popupY + 165,
                    Color.WHITE
            );

            drawCenteredText(
                    g2,
                    "Choose Next Level or Replay",
                    new Font("Roboto", Font.PLAIN, 20),
                    popupX + popupW / 2,
                    popupY + 215,
                    Color.WHITE
            );
        }

        if (tetrisImage != null) {
            g2.drawImage(tetrisImage, 18, 8, null);
        }
    }
}