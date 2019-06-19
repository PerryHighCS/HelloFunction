
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;

import greenfoot.Actor;
import greenfoot.World;

public class Brain extends Actor {

    private volatile int numBrains;

    /**
     * A disembodied brain. Zombies love brains.
     */
    public Brain() {
        numBrains = 1;
    }

    public Brain(int num) {
        numBrains = num;
    }

    /**
     * Contemplates the meaning of existence; and display the number of brains
     * in this cell.
     */
    @Override
    public void act() {
    }

    /**
     * Add one to the number of brains in this pile
     */
    public void addBrain() {
        this.numBrains++;
    }

    /**
     * Set the number of brains in this pile
     *
     * @param num the new number of brains in the pile
     */
    public void setNum(int num) {
        this.numBrains = num;
    }

    /**
     * Set the number of brains in this pile
     *
     * @return the number of brains in the pile
     */
    public int getNum() {
        return this.numBrains;
    }

    /**
     * Remove one brain from this pile. If this is the last brain, remove the
     * pile from the world.
     */
    public void removeBrain() {
        this.numBrains--;

        // If there are no more brains in the pile
        if (this.numBrains < 1) {
            World w = getWorld();
            if (w != null) {
                w.removeObject(this);
            }
        }
    }

    @Override
    public Image getImage() {
        Image img = imgs.get("Brain");

        if (img == null) {
            img = getWorld().loadSprite("Brain", Color.red);
            imgs.put("Brain", img);
        }
        Image sprite = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);

        Graphics g = sprite.getGraphics();
        g.drawImage(img, 0, 0, null);
        drawString("" + getNum(), sprite.getWidth(null) / 2, sprite.getHeight(null) / 2, 28, g);

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
