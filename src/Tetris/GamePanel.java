package Tetris;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

public class GamePanel extends JPanel implements Runnable {

    public static final int WIDTH = 1280;
    public static final int HEIGHT = 720;
    final int FPS = 60;

    private static final int GIF_X = 25;
    private static final int GIF_W = 260;
    private static final int GIF_H = 340;

    Thread gameThread;
    PlayManager pm;
    public static Sound music = new Sound();
    public static Sound se = new Sound();
    private GameUI gameUI;

    private AnimatedGifPanel gifPanel;

    GamePanel() {
        this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        this.setLayout(null);

        this.addKeyListener(new KeyHandler());
        this.setFocusable(true);

        pm = new PlayManager();

        gameUI = new GameUI(pm, this);
        pm.setGameUI(gameUI);

        initGifPanel();
        gameUI.addToPanel(this);
    }

    private void initGifPanel() {
        URL gifUrl = getClass().getResource("/res/video.gif");

        if (gifUrl == null) {
            System.out.println("Nu s-a gasit /res/video.gif");
            gifPanel = new AnimatedGifPanel(null);
        } else {
            gifPanel = new AnimatedGifPanel(new ImageIcon(gifUrl));
        }

        // aliniere pe baza cu tabla de joc:
        // baza GIF-ului = baza tablei
        int gifY = PlayManager.bottom_y - GIF_H;

        gifPanel.setBounds(GIF_X, gifY, GIF_W, GIF_H);
        this.add(gifPanel);
    }

    public void launchGame() {
        gameThread = new Thread(this);
        gameThread.start();

        music.play(0, true);
        music.loop();
    }

    public void syncUI() {
        if (gameUI != null) {
            gameUI.syncPauseButton();
        }
    }

    @Override
    public void run() {
        double drawInterval = 1000000000.0 / FPS;
        double delta = 0;
        long lastTime = System.nanoTime();
        long currentTime;

        while (gameThread != null) {
            currentTime = System.nanoTime();
            delta += (currentTime - lastTime) / drawInterval;
            lastTime = currentTime;

            if (delta > 1) {
                update();
                repaint();
                delta--;
            }
        }
    }

    private void update() {
        if (!KeyHandler.pausePressed && !pm.isGameOver() && !pm.isLevelCompleted()) {
            pm.update();
        }

        if (KeyHandler.restartPressed) {
            KeyHandler.restartPressed = false;
            pm.restartGame();
        }

        syncUI();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        pm.draw(g);
    }

    private static class AnimatedGifPanel extends JPanel {

        private final ImageIcon gifIcon;

        public AnimatedGifPanel(ImageIcon gifIcon) {
            this.gifIcon = gifIcon;
            setOpaque(true);
            setBackground(Color.BLACK);
            setBorder(BorderFactory.createLineBorder(Color.WHITE, 4));

            Timer timer = new Timer(40, e -> repaint());
            timer.start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            if (gifIcon == null) {
                g.setColor(Color.WHITE);
                g.setFont(new Font("Arial", Font.BOLD, 18));
                FontMetrics fm = g.getFontMetrics();
                String text = "video.gif not found";
                int x = (getWidth() - fm.stringWidth(text)) / 2;
                int y = getHeight() / 2;
                g.drawString(text, x, y);
                return;
            }

            int panelW = getWidth() - 8;
            int panelH = getHeight() - 8;

            int imgW = gifIcon.getIconWidth();
            int imgH = gifIcon.getIconHeight();

            if (imgW <= 0 || imgH <= 0) return;

            double scale = Math.min((double) panelW / imgW, (double) panelH / imgH);

            int drawW = (int) (imgW * scale);
            int drawH = (int) (imgH * scale);

            int x = (getWidth() - drawW) / 2;
            int y = (getHeight() - drawH) / 2;

            Graphics2D g2 = (Graphics2D) g.create();
            double sx = (double) drawW / imgW;
            double sy = (double) drawH / imgH;

            g2.translate(x, y);
            g2.scale(sx, sy);
            gifIcon.paintIcon(this, g2, 0, 0);
            g2.dispose();
        }
    }
}