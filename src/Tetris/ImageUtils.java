package Tetris;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.IOException;
import java.net.URL;

public class ImageUtils {

    public static BufferedImage loadImage(String path) {
        try {
            URL url = ImageUtils.class.getResource(path);
            if (url == null) {
                System.out.println("Nu s-a gasit imaginea: " + path);
                return null;
            }
            return ImageIO.read(url);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static BufferedImage scaleImage(BufferedImage src, int width, int height) {
        if (src == null) return null;

        BufferedImage scaled = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = scaled.createGraphics();

        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.drawImage(src, 0, 0, width, height, null);
        g2.dispose();

        return scaled;
    }

    public static BufferedImage cropImage(BufferedImage src, int x, int y, int width, int height) {
        if (src == null) return null;

        x = Math.max(0, x);
        y = Math.max(0, y);
        width = Math.max(1, Math.min(width, src.getWidth() - x));
        height = Math.max(1, Math.min(height, src.getHeight() - y));

        BufferedImage sub = src.getSubimage(x, y, width, height);
        BufferedImage copy = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2 = copy.createGraphics();
        g2.drawImage(sub, 0, 0, null);
        g2.dispose();

        return copy;
    }

    public static BufferedImage rotateImage(BufferedImage src, double angleDegrees) {
        if (src == null) return null;

        double radians = Math.toRadians(angleDegrees);
        double sin = Math.abs(Math.sin(radians));
        double cos = Math.abs(Math.cos(radians));

        int w = src.getWidth();
        int h = src.getHeight();

        int newW = (int) Math.floor(w * cos + h * sin);
        int newH = (int) Math.floor(h * cos + w * sin);

        BufferedImage rotated = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = rotated.createGraphics();

        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        AffineTransform at = new AffineTransform();
        at.translate((newW - w) / 2.0, (newH - h) / 2.0);
        at.rotate(radians, w / 2.0, h / 2.0);

        g2.drawRenderedImage(src, at);
        g2.dispose();

        return rotated;
    }

    public static BufferedImage blurImage(BufferedImage src) {
        if (src == null) return null;

        float[] matrix = {
                1f / 16, 2f / 16, 1f / 16,
                2f / 16, 4f / 16, 2f / 16,
                1f / 16, 2f / 16, 1f / 16
        };

        Kernel kernel = new Kernel(3, 3, matrix);
        ConvolveOp op = new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);

        BufferedImage current = src;

        for (int i = 0; i < 10; i++) {
            BufferedImage blurred = new BufferedImage(
                    current.getWidth(),
                    current.getHeight(),
                    BufferedImage.TYPE_INT_ARGB
            );
            op.filter(current, blurred);
            current = blurred;
        }

        return current;
    }
}