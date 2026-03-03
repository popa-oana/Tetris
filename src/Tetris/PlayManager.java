package Tetris;
import Mino.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.Random;
import java.util.ArrayList;

import static Tetris.GamePanel.music;

public class PlayManager
{
    //Main Play Area
    final int WIDTH = 360;
    final int HEIGHT = 600;
    public static int left_x;
    public static int right_x;
    public static int top_y;
    public static int bottom_y;
    //public Object update;
    private Image wallpaper;
    private Image playArea;
    private Image pauseImage;
    private Image gameOverImage;

    Mino currentMino;
    final int MINO_START_X;
    final int MINO_START_Y;
    Mino nextMino;
    final int NEXTMINO_X;
    final int NEXTMINO_Y;
    public static ArrayList<Block> staticBlocks = new ArrayList<>();

    // Others
    public static int dropInterval = 60; // mino drops in every 60 frames
    boolean gameOver;

    //Effect
    boolean effectCounterOn;
    int effectCounter;
    ArrayList<Integer> effectY = new ArrayList<>();

    //Score
    int level =1;
    int lines;
    int score;
    private GameUI gameUI;
    private BufferedImage tetrisImage;
    public PlayManager() {
        //Main Play Area Frame
        left_x = (GamePanel.WIDTH / 2) - (WIDTH / 2); // 1280/2 - 360/2 = 460
        right_x = left_x + WIDTH;
        top_y = 50;
        bottom_y = top_y + HEIGHT;
        
        // Initialize GameUI
        gameUI = new GameUI();

        MINO_START_X = left_x + (WIDTH / 2) - Block.SIZE;
        MINO_START_Y = top_y + Block.SIZE;
        NEXTMINO_X = right_x + 175;
        NEXTMINO_Y = top_y + 500;

        // Load images
        this.wallpaper    = new ImageIcon(Objects.requireNonNull(getClass().getResource("/res/wallpaper.png"))).getImage();
        this.playArea     = new ImageIcon(Objects.requireNonNull(getClass().getResource("/res/playzone.png"))).getImage();
        this.pauseImage   = new ImageIcon(Objects.requireNonNull(getClass().getResource("/res/pauseText.png"))).getImage();
        this.gameOverImage= new ImageIcon(Objects.requireNonNull(getClass().getResource("/res/gameoverText.png"))).getImage();
        try {
            this.tetrisImage = ImageIO.read(
                    Objects.requireNonNull(getClass().getResource("/res/tetris.png"),
                            "/res/tetris.png not found"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Set the starting Mino
        currentMino = pickMino();
        currentMino.setXY(MINO_START_X, MINO_START_Y);
        nextMino = pickMino();
        nextMino.setXY(NEXTMINO_X, NEXTMINO_Y);
    }

    private Mino pickMino()
    {
        //Pick a random Mino
        Mino mino = null;
        int i = new Random().nextInt(7);

        switch(i)
        {
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

    public void update()
    {
        //Check if the currentMino is active
        if(!currentMino.active)
        {//if the mino is not active, put it into the staticBlocks
            staticBlocks.add(currentMino.b[0]);
            staticBlocks.add(currentMino.b[1]);
            staticBlocks.add(currentMino.b[2]);
            staticBlocks.add(currentMino.b[3]);

            // check if the game is over
            if(currentMino.b[0].x == MINO_START_X && currentMino.b[0].y == MINO_START_Y)
            {
                gameOver = true;
                music.stop();
                GamePanel.se.play(2, false);
            }
            currentMino.deactivating = false;

            //replace the currentMino with the nextMino
            currentMino = nextMino;
            currentMino.setXY(MINO_START_X,MINO_START_Y);
            nextMino = pickMino();
            nextMino.setXY(NEXTMINO_X,NEXTMINO_Y);

            //when a mino become inactive, check if line(s) can be deleted
            checkDelete();
        }
        else
        {
            currentMino.update();
        }
    }

    private void checkDelete()
    {
        int x = left_x;
        int y = top_y;
        int blockCount = 0;
        int lineCount = 0;

        while (x < right_x && y < bottom_y)
        {
            for (int i = 0; i < staticBlocks.size(); i++)
            {
                if( staticBlocks.get(i).x == x && staticBlocks.get(i).y == y)
                {
                    //increase the count if there is a static block
                    blockCount++;
                }
            }
            x += Block.SIZE;

            if (x == right_x)
            {
                //if the blockCount = 0 => y line is filled with blocks and we can delete it
                if (blockCount == 12)
                {
                    effectCounterOn = true;
                    effectY.add(y);
                    for( int i = staticBlocks.size()-1; i > -1; i--)
                    {
                        // remove all the blocks from the y line
                        if (staticBlocks.get(i).y == y)
                        {
                            staticBlocks.remove(i);
                        }
                    }

                    lineCount++;
                    lines++;
                    score += 100 * level; // Add score based on level
                    gameUI.updateHighScore(score); // Update high score if needed
                    
                    //Drop Speed
                    // Increase level every 5 lines completed
                    if(lines % 5 == 0 && dropInterval >1)
                    {
                        level++;
                        if(dropInterval >10)
                        {
                            dropInterval -= 10;
                        }
                        else
                        {
                         dropInterval -=1;
                        }
                    }


                    // after the line has been deleted must slide down blocks
                    for ( int i = 0; i < staticBlocks.size(); i++)
                    {
                        //if a block is above the current y, move it down by the block size
                        if( staticBlocks.get(i).y < y)
                        {
                            staticBlocks.get(i).y += Block.SIZE;
                        }
                    }
                }

                blockCount = 0;
                x = left_x;
                y += Block.SIZE;
            }
        }
        //Add Score
        if(lineCount > 0)
        {
            GamePanel.se.play(1, false);
            int singleLineScore = 10 * level;
            score += singleLineScore * lineCount;
        }
    }

    public void draw(Graphics g) {
        // Cast Graphics to Graphics2D
        Graphics2D g2 = (Graphics2D) g;

        // Draw wallpaper
        if (wallpaper != null) {
            g2.drawImage(wallpaper, 0, 0, GamePanel.WIDTH, GamePanel.HEIGHT, null);
        }

        // Set background color for text and other non-image elements
        g2.setColor(Color.WHITE);
        // Draw play zone PNG image within the frame
        if (playArea != null) {
            g2.drawImage(playArea, left_x, top_y, WIDTH, HEIGHT, null);
        }

        // Set background color for text and other non-image elements
        g2.setColor(Color.WHITE);
        // Draw play zone PNG image within the frame
        if (playArea != null) {
            g2.drawImage(playArea, left_x, top_y, WIDTH, HEIGHT, null);
        }
        // Draw Play Area Frame
        g2.setStroke(new BasicStroke(4f));
        g2.drawRect(left_x - 4, top_y - 4, WIDTH + 8, HEIGHT + 8);

        // Draw next Mino Frame
        int x = right_x + 100;
        int y = bottom_y - 200;
        g2.drawRect(x, y, 200, 200);

        // Draw text for the next Mino Frame
        g2.setFont(new Font("Roboto", Font.BOLD, 30));
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.drawString("NEXT", x + 60, y + 60);

        // Score Panel Constants (fixed dimensions)
        final int SCORE_PANEL_WIDTH = 300;
        final int SCORE_PANEL_HEIGHT = 250;
        final int SCORE_PANEL_PADDING = 20;
        final int TEXT_LINE_HEIGHT = 35;
        final int VERTICAL_SPACING = 12;
        final int TOP_MARGIN = TEXT_LINE_HEIGHT + VERTICAL_SPACING;

        // Position score panel
        int scorePanelX = right_x + 100;
        int scorePanelY = top_y;

        // Draw Score Frame with fixed dimensions
        g2.drawRect(scorePanelX, scorePanelY, SCORE_PANEL_WIDTH, SCORE_PANEL_HEIGHT);
        g2.setFont(new Font("Roboto", Font.BOLD, 30));
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        // Calculate text starting position with padding and top margin
        int textStartX = scorePanelX + SCORE_PANEL_PADDING;
        // Calculate vertical centering with top margin
        int totalTextHeight = (TEXT_LINE_HEIGHT + VERTICAL_SPACING) * 5 - VERTICAL_SPACING;
        int textStartY = scorePanelY + TOP_MARGIN + (SCORE_PANEL_HEIGHT - totalTextHeight - TOP_MARGIN) / 2;
        
        // Draw text with precise positioning
        g2.drawString("LEVEL: " + level, textStartX, textStartY);
        textStartY += TEXT_LINE_HEIGHT + VERTICAL_SPACING;
        g2.drawString("LINES: " + lines, textStartX, textStartY);
        textStartY += TEXT_LINE_HEIGHT + VERTICAL_SPACING;
        g2.drawString("SCORE: " + score, textStartX, textStartY);
        textStartY += TEXT_LINE_HEIGHT + VERTICAL_SPACING;
        g2.drawString("HIGH SCORE: ", textStartX, textStartY);
        textStartY += TEXT_LINE_HEIGHT + VERTICAL_SPACING;
        g2.drawString("" + gameUI.getHighScore(), textStartX, textStartY);

        // Draw the currentMino
        if (currentMino != null) {
            currentMino.draw(g2);
        }

        // Draw the nextMino
        nextMino.draw(g2);

        // Draw Static Blocks
        for (int i = 0; i < staticBlocks.size(); i++) {
            staticBlocks.get(i).draw(g2);
        }

        // Draw Effect
        if (effectCounterOn) {
            effectCounter++;

            // Calculate the alpha value based on the effectCounter to create a fading effect
            int alpha = (int)(255 * (1 - (effectCounter / 10.0)));
            alpha = Math.max(alpha, 0); // Ensure that alpha does not go below 0

            // Set the color with the new alpha value for the fading effect
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
        }

        if (gameOver) {
            x = left_x + 70;
            y = top_y + 320;
            int width = 250;
            int height = 160;
            g2.drawImage(gameOverImage, x, y, width, height, null);

            // Draw "Press Enter to restart" text
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Roboto", Font.BOLD, 30));
            String restartText = "Press Enter to restart";
            g2.drawString(restartText, 20, GamePanel.HEIGHT - 60);

            // Draw "Never give up ;)" text
            String neverGiveUpText = "Never give up ;)";
            g2.drawString(neverGiveUpText, 20, GamePanel.HEIGHT - 30);

            // Restart the game if restartPressed is true
            if(KeyHandler.restartPressed)
            {
                restartGame();
            }
        }



        if (KeyHandler.pausePressed) {
            x = left_x + 70;
            y = top_y + 320;
            int width = 250;
            int height = 160;
            g2.drawImage(pauseImage, x, y, width, height, null);
        }

        // Draw Tetris logo image
        if (tetrisImage != null) {
            int x1 = 20;
            int y1 = 0;
            int width = 170;
            int height = 100;
            g2.drawImage(tetrisImage, x1, y1, width, height, null);
        }
    }

    public void restartGame() {
        // Reset all game variables
        staticBlocks.clear();
        currentMino = pickMino();
        currentMino.setXY(MINO_START_X, MINO_START_Y);
        nextMino = pickMino();
        nextMino.setXY(NEXTMINO_X, NEXTMINO_Y);
        dropInterval = 60;
        level = 1;
        lines = 0;
        score = 0;
        gameOver = false;

        music.play(0, true);
        music.loop();
    }
}
