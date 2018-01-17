import greenfoot.World;
import greenfoot.Actor;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;

public class Brain extends Actor
{
    private int numBrains;
    
    /**
     * A disembodied brain.  Zombies love brains.
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
    public void act() 
    {
    }    
    
    /**
     * Add one to the number of brains in this pile
     */
    public void addBrain()
    {
        synchronized(Zombie.class)
        {
            this.numBrains++;
        }
    }
    
    /**
     * Set the number of brains in this pile
     * @param num the new number of brains in the pile
     */
    public void setNum(int num)
    {
        synchronized(Zombie.class)
        {
            this.numBrains = num;            
        }
    }
    
    /**
     * Set the number of brains in this pile
     * @return the number of brains in the pile
     */
    public int getNum()
    {
        synchronized(Zombie.class)
        {
            return this.numBrains;
        }
    }
    
    /**
     * Remove one brain from this pile. If this is the last brain,
     * remove the pile from the world.
     */
    public void removeBrain()
    {
        synchronized(Zombie.class)
        {
            try
            {
                this.numBrains--;
            
                // If there are no more brains in the pile
                if (this.numBrains < 1) {
                    World w = getWorld();
                    w.removeObject(this);
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
    
    @Override
    public Image getImage()
    {
        Image sprite = getWorld().loadSprite("Brain", Color.red);
        Graphics g = sprite.getGraphics();
        
        drawString("" + getNum(), sprite.getWidth(null) / 2,
                sprite.getHeight(null) / 2, 28, g);
        
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
