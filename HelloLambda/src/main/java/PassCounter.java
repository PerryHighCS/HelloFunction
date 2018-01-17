import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import static java.awt.image.BufferedImage.TYPE_INT_ARGB;

public class PassCounter extends ZombieDetector
{
    private int passCount = 0;
    private boolean showCount = false;
    
    
    /**
     * Create a ZombieDetector that shows the number of times a zombie has
     * stepped on it
     * @param display whether to show the pass counter
     */
    public PassCounter(boolean display) {
        showCount = display;
    }
        
    /**
     * When a zombie is detected, increment the pass counter.
     */
    @Override
    public void detected()
    {
        
        passCount++;
    }
    
    /**
     * Retrieve the number of times a zombie has stepped on the detector.
     * @return the number of times the zombie passed by
     */
    public int getPasses()
    {
        return passCount;
    }
    
    /**
     * Turn on the counter display
     * @param display whether to show the pass counter
     */
    public void displayPasses(boolean display)
    {
        showCount = display;
    }
    
    
    @Override
    public Image getImage()
    {
        Image sprite = new BufferedImage(64, 64, TYPE_INT_ARGB);
        Graphics g = sprite.getGraphics();
        
        if (showCount) {
            drawString("" + getPasses(), sprite.getWidth(null) / 2,
                    sprite.getHeight(null) / 2, 28, g);
        }
        
        g.dispose();
        return sprite;
    }
    
    
    private void drawString(String msg, int x, int y, int fontSize, Graphics g) {
        Font f = new Font("Helvitica", Font.BOLD, fontSize);
        g.setFont(f);
        
        FontMetrics fm = g.getFontMetrics();
        int h = fm.getHeight();
        int w = fm.stringWidth(msg);
        
        x = x - (w / 2);
        y = y + (h / 2);
        g.setColor(Color.BLACK);
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx != 0 && dy != 0) {
                    g.drawString(msg, x + dx, y + dy);
                }
            }
        }
        g.setColor(Color.WHITE);
        g.drawString(msg, x, y);
    }
}