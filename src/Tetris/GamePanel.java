package Tetris;
import javax.swing.*;
import java.awt.*;

public class GamePanel extends JPanel implements Runnable
{
    public static final int WIDTH = 1280;
    public static final int HEIGHT = 720;
    final int FPS=60;
    Thread gameThread;
    PlayManager pm;
    public static Sound music = new Sound();
    public static Sound se = new Sound();
    private GameUI gameUI;
    // Load wallpaper image
    private Image wallpaper;


    GamePanel()
    {
        //Panel Settings
        this.setPreferredSize(new Dimension(WIDTH,HEIGHT));
       // this.setBackground(Color.BLACK);
        this.setLayout(null);
        //Implement KeyListener
        this.addKeyListener(new KeyHandler());
        this.setFocusable(true);

        pm = new PlayManager();
        gameUI = new GameUI(pm, this);
        gameUI.addToPanel(this);
    }

    public void launchGame()
    {
        gameThread = new Thread(this);
        gameThread.start();

        music.play(0,true);
        music.loop();
    }

    public void syncUI()
    {
        if (gameUI != null) {
            gameUI.syncPauseButton();
        }
    }

    @Override
    public void run()
    {
        //Game Loop
        double drawInterval = 1000000000/FPS;
        double delta = 0;
        long lastTime = System.nanoTime();
        long currentTime;

        while(gameThread != null)
        {
            currentTime = System.nanoTime();
            delta += (currentTime - lastTime) / drawInterval;
            lastTime = currentTime;

            if ( delta > 1)
            {
                update();
                repaint();
                delta--;
            }
        }
    }

    private void update()
    {
        if (!KeyHandler.pausePressed && !pm.gameOver)
        {
            pm.update();
        }

        // Sync UI elements after key events
        if (KeyHandler.restartPressed) {
            KeyHandler.restartPressed = false;
            pm.restartGame();
        }
        syncUI();
    }

    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);

        Graphics g2 = (Graphics2D)g;
        pm.draw(g2);
    }
}
