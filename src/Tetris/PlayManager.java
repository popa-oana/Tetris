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
    private PhoneControllerServer phoneControllerServer;

    private boolean waitingForPhone = true;
    private boolean phoneHasConnectedOnce = false;

    public PlayManager() {
        left_x = (GamePanel.WIDTH / 2) - (WIDTH / 2);
        right_x = left_x + WIDTH;
        top_y = 50;
        bottom_y = top_y + HEIGHT;

        MINO_START_X = left_x + (WIDTH / 2) - Block.SIZE;
        MINO_START_Y = top_y + Block.SIZE;

        NEXTMINO_X = right_x + 170;
        NEXTMINO_Y = top_y + 520;

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

        if (tetrisImage != null) {
            tetrisImage = ImageUtils.scaleImage(tetrisImage, 210, 125);
        }

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
            nextPanelCrop = ImageUtils.scaleImage(crop2, 172, 172);
        }

        if (pauseImage != null) {
            rotatedPauseImage = ImageUtils.rotateImage(pauseImage, -18);
        }
    }

    public void setGameUI(GameUI gameUI) {
        this.gameUI = gameUI;
    }

    public void setPhoneControllerServer(PhoneControllerServer server) {
        this.phoneControllerServer = server;
    }

    public void setWaitingForPhone(boolean waitingForPhone) {
        this.waitingForPhone = waitingForPhone;
    }

    public void setPhoneHasConnectedOnce(boolean phoneHasConnectedOnce) {
        this.phoneHasConnectedOnce = phoneHasConnectedOnce;
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

    public int getScore() {
        return score;
    }

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

            if (phoneControllerServer != null) {
                phoneControllerServer.pushUiEvent("level");
            }

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

                if (phoneControllerServer != null) {
                    phoneControllerServer.pushUiEvent("gameover");
                }
            }

            currentMino.deactivating = false;

            currentMino = nextMino;
            currentMino.setXY(MINO_START_X, MINO_START_Y);

            nextMino = pickMino();
            nextMino.setXY(NEXTMINO_X, NEXTMINO_Y);

            checkDelete();
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

            if (phoneControllerServer != null) {
                phoneControllerServer.pushUiEvent("line");
            }

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

        if (phoneControllerServer != null) {
            phoneControllerServer.clearUiEvent();
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

        if (phoneControllerServer != null) {
            phoneControllerServer.clearUiEvent();
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

        if (phoneControllerServer != null) {
            phoneControllerServer.clearUiEvent();
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
        int baseY = popupY + 82 + offsetY;

        Font titleFont = new Font("Roboto", Font.BOLD, 40);

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

    private String shorten(String text, int maxLen) {
        if (text == null) return "-";
        if (text.length() <= maxLen) return text;
        return text.substring(0, maxLen - 3) + "...";
    }

    private void drawPhoneConnectionPanel(Graphics2D g2) {
        if (phoneControllerServer == null) return;

        int panelX = right_x + 100;
        int panelY = top_y + 252;
        int panelW = 300;
        int panelH = 160;

        g2.setColor(new Color(8, 8, 18, 225));
        g2.fillRoundRect(panelX, panelY, panelW, panelH, 24, 24);

        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(4f));
        g2.drawRoundRect(panelX, panelY, panelW, panelH, 24, 24);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Roboto", Font.BOLD, 28));
        g2.drawString("PHONE", panelX + 18, panelY + 36);

        boolean connected = phoneControllerServer.isPhoneConnected();

        g2.setColor(connected ? new Color(0, 255, 140) : new Color(255, 80, 80));
        g2.fillOval(panelX + 18, panelY + 60, 18, 18);

        g2.setColor(connected ? new Color(0, 230, 120) : new Color(255, 110, 110));
        g2.setFont(new Font("Roboto", Font.BOLD, 22));
        g2.drawString(connected ? "CONNECTED" : "DISCONNECTED", panelX + 48, panelY + 76);

        g2.setColor(new Color(220, 220, 220));
        g2.setFont(new Font("Roboto", Font.PLAIN, 16));
        g2.drawString("Open on iPhone:", panelX + 18, panelY + 108);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Consolas", Font.PLAIN, 12));
        g2.drawString(shorten(phoneControllerServer.getAccessUrl(), 34), panelX + 18, panelY + 132);
        g2.drawString("Phone: " + shorten(phoneControllerServer.getClientIp(), 22), panelX + 18, panelY + 150);
    }

    private void drawPhoneGateOverlay(Graphics2D g2) {
        if (phoneControllerServer == null) return;
        if (phoneControllerServer.isPhoneConnected()) return;
        if (gameOver || levelCompleted) return;
        if (!waitingForPhone) return;

        int popupW = 380;
        int popupH = 126;
        int popupX = left_x + (WIDTH - popupW) / 2;
        int popupY = top_y + 78;

        g2.setColor(new Color(8, 8, 18, 235));
        g2.fillRoundRect(popupX, popupY, popupW, popupH, 24, 24);

        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(3f));
        g2.drawRoundRect(popupX, popupY, popupW, popupH, 24, 24);

        String title;
        String subtitle;

        if (phoneHasConnectedOnce) {
            title = "Phone controller disconnected";
            subtitle = "Reconnect your phone to continue";
        } else {
            title = "Connect your phone to start";
            subtitle = "Open the phone link shown on the right";
        }

        drawCenteredText(
                g2,
                title,
                new Font("Roboto", Font.BOLD, 24),
                popupX + popupW / 2,
                popupY + 48,
                new Color(255, 110, 110)
        );

        drawCenteredText(
                g2,
                subtitle,
                new Font("Roboto", Font.PLAIN, 18),
                popupX + popupW / 2,
                popupY + 82,
                Color.WHITE
        );
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

        final int scorePanelX = right_x + 100;
        final int scorePanelY = top_y;
        final int scorePanelW = 300;
        final int scorePanelH = 230;

        final int nextPanelX = right_x + 100;
        final int nextPanelY = top_y + 434;
        final int nextPanelSize = 180;
        final int nextPanelInner = 172;

        drawPanelDecor(g2, nextPanelCrop, nextPanelX + 4, nextPanelY + 4, nextPanelInner, nextPanelInner);
        g2.setColor(new Color(255, 255, 255, 25));
        g2.fillRect(nextPanelX + 4, nextPanelY + 4, nextPanelInner, nextPanelInner);
        g2.setColor(Color.WHITE);
        g2.drawRect(nextPanelX, nextPanelY, nextPanelSize, nextPanelSize);
        g2.setFont(new Font("Roboto", Font.BOLD, 28));
        g2.drawString("NEXT", nextPanelX + 42, nextPanelY + 36);

        drawPanelDecor(g2, scorePanelCrop, scorePanelX + 4, scorePanelY + 4, 292, 222);
        g2.setColor(new Color(255, 255, 255, 20));
        g2.fillRect(scorePanelX + 4, scorePanelY + 4, 292, 222);
        g2.setColor(Color.WHITE);
        g2.drawRect(scorePanelX, scorePanelY, scorePanelW, scorePanelH);
        g2.setFont(new Font("Roboto", Font.BOLD, 27));

        int textStartX = scorePanelX + 22;
        int textStartY = scorePanelY + 50;
        int textLineHeight = 34;
        int verticalSpacing = 14;

        g2.drawString("LEVEL: " + level, textStartX, textStartY);
        textStartY += textLineHeight + verticalSpacing;

        g2.drawString("SCORE: " + score, textStartX, textStartY);
        textStartY += textLineHeight + verticalSpacing;

        g2.drawString("TARGET: " + levelTargetScore, textStartX, textStartY);
        textStartY += textLineHeight + verticalSpacing;

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
            int x = left_x + 50;
            int y = top_y + 230;
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
            int x = left_x + 35;
            int y = top_y + 270;
            int width = 300;
            int height = 190;

            if (rotatedPauseImage != null) {
                g2.drawImage(rotatedPauseImage, x, y, width, height, null);
            } else if (pauseImage != null) {
                g2.drawImage(pauseImage, x, y, width, height, null);
            }
        }

        if (levelCompleted) {
            int popupX = left_x + 28;
            int popupY = top_y + 115;
            int popupW = WIDTH - 56;
            int popupH = 250;

            g2.setColor(new Color(8, 8, 18, 238));
            g2.fillRoundRect(popupX, popupY, popupW, popupH, 28, 28);

            g2.setColor(new Color(255, 255, 255, 220));
            g2.setStroke(new BasicStroke(3f));
            g2.drawRoundRect(popupX, popupY, popupW, popupH, 28, 28);

            drawAnimatedWellPlayed(g2, popupX, popupY, popupW);

            drawCenteredText(
                    g2,
                    "LEVEL COMPLETE",
                    new Font("Roboto", Font.BOLD, 22),
                    popupX + popupW / 2,
                    popupY + 135,
                    Color.WHITE
            );

            drawCenteredText(
                    g2,
                    "Choose Next Level or Replay",
                    new Font("Roboto", Font.PLAIN, 18),
                    popupX + popupW / 2,
                    popupY + 175,
                    Color.WHITE
            );
        }

        drawPhoneConnectionPanel(g2);
        drawPhoneGateOverlay(g2);

        if (tetrisImage != null) {
            g2.drawImage(tetrisImage, 18, 8, null);
        }
    }
}