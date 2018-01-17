package greenfoot;

import static java.awt.image.BufferedImage.TYPE_INT_RGB;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

/**
 * A simplified, headless re-implementation of the Greenfoot World class.
 * 
 * @author bdahl
 */
public abstract class World {
	private final int width;
	private final int height;
	private final int cellSize;
	private final List<Actor> cast;

	/**
	 * Create a rectangular world with given dimensions, with square locations of a
	 * given size.
	 * 
	 * @param worldWidth
	 * @param worldHeight
	 * @param cellSize
	 */
	public World(int worldWidth, int worldHeight, int cellSize) {
		width = worldWidth;
		height = worldHeight;
		this.cellSize = cellSize;
		cast = new ArrayList<>();
	}

	/**
	 * An overridable act method to be called once per act cycle
	 */
	public void act() {
	}

	/**
	 * Add an actor to the world
	 * 
	 * @param a
	 *            the Actor to add
	 * @param x
	 *            the location to add the actor
	 * @param y
	 *            the location to add the actor
	 */
	public synchronized void addObject(Actor a, int x, int y) {
		cast.add(a);
		a.addToWorld(this, x, y);
	}

	/**
	 * Get the size of the cells in this world
	 * 
	 * @return
	 */
	public int getCellSize() {
		return cellSize;
	}

	/**
	 * Get the width of this world (in cells)
	 * 
	 * @return
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * Get the height of this world (in cells)
	 * 
	 * @return
	 */
	public int getHeight() {
		return height;
	}

	private int constrain(int val, int min, int max) {
		val = Math.max(val, min);
		val = Math.min(val, max);
		return val;
	}

	/**
	 * Constrain an x coordinate to the size of this world
	 * 
	 * @param val
	 * @return
	 */
	public int constrainX(int val) {
		return constrain(val, 0, width - 1);
	}

	/**
	 * Constrain a y coordinate to the size of this world
	 * 
	 * @param val
	 * @return
	 */
	public int constrainY(int val) {
		return constrain(val, 0, height - 1);
	}

	/**
	 * Get a list of all of the objects in this world of a given type
	 * 
	 * @param <A>
	 * @param cls
	 *            The type of objects to get (null for any)
	 * @return
	 */
	@SuppressWarnings({ "unchecked" })
	public synchronized <A> List<A> getObjects(Class<A> cls) {
		List<A> objects = new ArrayList<>();
		for (Actor a : cast) {
			if (cls == null || cls.isInstance(a)) {
				objects.add((A) a);
			}
		}

		return objects;
	}

	/**
	 * Get a list of all the objects at a given location
	 * 
	 * @param <A>
	 * @param x
	 * @param y
	 * @param cls
	 *            The type of objects to get (null for any)
	 * @return
	 */
	@SuppressWarnings({ "unchecked" })
	public synchronized <A> List<A> getObjectsAt(int x, int y, Class<A> cls) {
		List<A> objects = new ArrayList<>();
		for (Actor a : cast) {
			if ((cls == null || cls.isInstance(a)) && (a.getX() == x && a.getY() == y)) {
				objects.add((A) a);
			}
		}

		return objects;
	}

	/**
	 * Get the number of items in the world
	 * 
	 * @return
	 */
	public synchronized int numberOfObjects() {
		return cast.size();
	}

	/**
	 * Remove a given object from the world
	 * 
	 * @param object
	 */
	public synchronized void removeObject(Actor object) {
		if (object != null) {
			cast.remove(object);
		}
	}

	/**
	 * Remove all objects in a list from the world
	 * 
	 * @param objects
	 */
	public synchronized void removeObjects(java.util.Collection<? extends Actor> objects) {
		objects.forEach(a -> removeObject(a));
	}

	/**
	 * Get an image of the current state of this world
	 * 
	 * @return
	 */
	public synchronized Image image() {
		BufferedImage img = new BufferedImage(width * cellSize, height * cellSize, TYPE_INT_RGB);

		BufferedImage sprite;

		sprite = loadSprite("World", Color.LIGHT_GRAY);

		Graphics imgG = img.getGraphics();

		for (int x = 0; x < width * cellSize; x += cellSize) {
			for (int y = 0; y < height * cellSize; y += cellSize) {
				imgG.drawImage(sprite, x, y, null);
			}
		}

		cast.forEach(a -> drawActor(a, imgG));

		imgG.dispose();

		Map<String, String> env = System.getenv();

		int width = Integer.parseInt(env.getOrDefault("MAXIMG_WIDTH", "512"));
		int height = Integer.parseInt(env.getOrDefault("MAXIMG_HEIGHT", "512"));
		int imgWidth = img.getWidth();
		int imgHeight = img.getHeight();
		if (imgWidth > width || imgHeight > height) {
			if (img.getWidth() * height < img.getHeight() * width) {
				width = imgWidth * height / imgHeight;
			} else {
				height = imgHeight * width / imgWidth;
			}
			BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
			Graphics2D bGr = image.createGraphics();
			bGr.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
			bGr.drawImage(img, 0, 0, width, height, null);
			bGr.dispose();
			img = image;
		}

		return img;
	}

	/**
	 * Load a sprite from this world's resources
	 * 
	 * @param name
	 *            The name of the sprite to load
	 * @param failFill
	 *            The color to fill the sprite with if loading fails
	 * @return
	 */
	public BufferedImage loadSprite(String name, Color failFill) {
		BufferedImage img;
		try {
			img = ImageIO.read(this.getClass().getResource("/zss/images/" + name + ".png"));
		} catch (IOException e) {
			img = new BufferedImage(cellSize, cellSize, TYPE_INT_RGB);

			if (failFill != null) {
				Graphics g = img.getGraphics();
				g.setColor(Color.LIGHT_GRAY);
				g.fillRect(0, 0, cellSize, cellSize);
				g.dispose();
			}
		}

		return img;
	}

	/**
	 * Draw an actor at its location on a graphics context
	 * 
	 * @param a
	 * @param g
	 */
	private void drawActor(Actor a, Graphics g) {
		int x = a.getX() * cellSize;
		int y = a.getY() * cellSize;

		Image sprite = a.getImage();

		int xPos = x + (cellSize - sprite.getWidth(null)) / 2;
		int yPos = y + (cellSize - sprite.getHeight(null)) / 2;

		g.drawImage(sprite, xPos, yPos, null);
	}

}
