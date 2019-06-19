package greenfoot;

import java.awt.Color;
import java.awt.Image;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A headless reimplementation of the Greenfoot Actor class
 */
public abstract class Actor {

    private int xPos;
    private int yPos;
    private int dir;
    private World world;

    protected static Map<String, Image> imgs = new HashMap<>();

    public Actor() {
        this.xPos = 0;
        this.yPos = 0;
        this.dir = 0;
    }

    public abstract void act();

    /**
     * Add this actor to a world, at a given location in that world
     *
     * @param newWorld
     * @param x
     * @param y
     */
    public void addToWorld(World newWorld, int x, int y) {
        // if the actor is already in a world, remove it
        if (world != null) {
            world.removeObject(this);
        }

        this.world = newWorld;
        this.setX(x);
        this.setY(y);
    }

    /**
     * Get the world this actor inhabits
     *
     * @return
     */
    public World getWorld() {
        return this.world;
    }

    /**
     * Move this actor to a given x coordinate
     *
     * @param newX
     */
    public void setX(int newX) {
        this.xPos = world.constrainX(newX);
    }

    /**
     * Move this actor to a given y coordinate
     *
     * @param newY
     */
    public void setY(int newY) {
        this.yPos = world.constrainY(newY);
    }

    /**
     * Determine this actor's current X coordinate
     *
     * @return
     */
    public int getX() {
        return this.xPos;
    }

    /**
     * Determine this actor's current Y coordinate
     *
     * @return
     */
    public int getY() {
        return this.yPos;
    }

    /**
     * Move this actor forward a given distance
     *
     * @param distance
     */
    public void move(int distance) {
        double radians = Math.toRadians(this.dir);

        // We round to the nearest integer, to allow moving one unit at an angle
        // to actually move.
        int dx = (int) Math.round(Math.cos(radians) * distance);
        int dy = (int) Math.round(Math.sin(radians) * distance);
        setLocation(this.xPos + dx, this.yPos + dy);
    }

    /**
     * Move this actor to a given location
     *
     * @param x
     * @param y
     */
    public void setLocation(int x, int y) {
        this.setX(x);
        this.setY(y);
    }

    /**
     * Turn this actor to face a given direction in degrees (0 deg is east, 90
     * is south)
     *
     * @param rotation
     */
    public void setRotation(int rotation) {
        // First normalize
        if (rotation >= 360) {
            // Optimize the usual case: rotation has adjusted to a value greater than
            // 360, but is still within the 360 - 720 bound.
            if (rotation < 720) {
                rotation -= 360;
            } else {
                rotation = rotation % 360;
            }
        } else if (rotation < 0) {
            // Likwise, if less than 0, it's likely that the rotation was reduced by
            // a small amount and so will be >= -360.
            if (rotation >= -360) {
                rotation += 360;
            } else {
                rotation = 360 + (rotation % 360);
            }
        }

        dir = rotation;
    }

    /**
     * Determine this actor's heading
     *
     * @return The direction this actor is facing, in degrees (0 deg is east, 90
     * is south)
     */
    public int getRotation() {
        return dir;
    }

    /**
     * Turn a given number of degrees to the right (clockwise)
     *
     * @param amount
     */
    public void turn(int amount) {
        this.setRotation(this.dir + amount);
    }

    /**
     * Turn to face a given location
     *
     * @param x
     * @param y
     */
    public void turnTowards(int x, int y) {
        double a = Math.atan2(y - this.yPos, x - this.xPos);
        this.setRotation((int) Math.toDegrees(a));
    }

    /**
     * Get any objects of a given type touching this actor
     *
     * @param <A>
     * @param cls - the type of object to look for (null for any)
     * @return
     */
    protected <A> List<A> getIntersectingObjects(Class<A> cls) {
        List<A> actors = world.getObjectsAt(xPos, yPos, cls);
        actors.remove(this);
        return actors;
    }

    /**
     * Get one object of a given type touching this actor
     *
     * @param <A>
     *
     * @param cls - the type of object to look for (null for any)
     * @return null when no object is detected
     */
    protected <A> Actor getOneIntersectingObject(Class<A> cls) {
        List<A> actors = world.getObjectsAt(xPos, yPos, cls);
        if (actors.size() > 0) {
            return (Actor) actors.get(0);
        } else {
            return null;
        }
    }

    /**
     * Get the actors within a given horizontal and vertical distance from this
     * actor. Searches a square or plus shaped area 2*distance across centered
     * on this actor.
     *
     * @param <A>
     * @param distance
     * @param diagonal true includes all cells in the square centered on this
     * actor
     * @param cls The type of object to look for (null for any)
     * @return
     */
    protected <A> List<A> getNeighbours(int distance, boolean diagonal, Class<A> cls) {
        List<A> objects = new ArrayList<>();

        int minX = world.constrainX(xPos - distance);
        int maxX = world.constrainX(xPos + distance);
        int minY = world.constrainY(yPos - distance);
        int maxY = world.constrainY(yPos + distance);
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                // If not diagonal, only check cells horizontally or vertically
                // aligned with the actor's cell
                if (!diagonal && (x != xPos && y != yPos)) {
                    continue;
                }

                objects.addAll(world.getObjectsAt(x, y, cls));
            }
        }

        objects.remove(this);

        return objects;
    }

    /**
     * Get all of the objects in a cell dx, dy away from this actor's location
     *
     * @param <A>
     * @param dx
     * @param dy
     * @param cls The type of objects to look for (null for any)
     * @return
     */
    protected <A> List<A> getObjectsAtOffset(int dx, int dy, Class<A> cls) {
        return world.getObjectsAt(xPos + dx, yPos + dy, cls);
    }

    /**
     * Get one object in a cell dx, dy awat from this actor's location
     *
     * @param <A>
     *
     * @param dx
     * @param dy
     * @param cls The type of object to look for (null for any)
     * @return null when no object is detected
     */
    protected <A> Actor getOneObjectAtOffset(int dx, int dy, Class<A> cls) {
        List<A> objects = world.getObjectsAt(xPos + dx, yPos + dy, cls);
        if (objects.size() > 0) {
            return (Actor) objects.get(0);
        } else {
            return null;
        }
    }

    /**
     * Get all objects within a given radius of this actor
     *
     * @param <A>
     * @param radius
     * @param cls
     * @return
     */
    protected <A> List<A> getObjectsInRange(int radius, Class<A> cls) {
        List<A> objects = getNeighbours(radius, true, cls);

        for (int i = objects.size() - 1; i >= 0; i++) {
            Actor a = (Actor) objects.get(i);
            int aX = a.getX();
            int aY = a.getY();

            if (Math.sqrt(Math.pow(xPos - aX, 2) + Math.pow(yPos - aY, 2)) > radius) {
                objects.remove(a);
            }
        }

        return objects;
    }

    /**
     * Determine if this object is touching another object
     *
     * @param cls the type of object to search for (null for any)
     * @return
     */
    protected boolean isTouching(Class<?> cls) {
        return getIntersectingObjects(cls).size() > 0;
    }

    /**
     * Remove all objects of a given type touching the actor
     *
     * @param cls the type of object to remove (null for any)
     */
    protected void removeTouching(Class<?> cls) {
        getIntersectingObjects(cls).forEach(a -> world.removeObject((Actor) a));
    }

    /**
     * Determine if this actor is at the same location as another
     *
     * @param other
     * @return
     */
    protected boolean intersects(Actor other) {
        return (this.xPos == other.xPos && this.yPos == other.yPos);
    }

    /**
     * Determine if the actor has reached the edge of the world
     *
     * @return
     */
    public boolean isAtEdge() {
        return (this.xPos == 0 || this.xPos == world.getWidth() - 1 || this.yPos == 0
                || this.yPos == world.getHeight() - 1);
    }

    /**
     * Get this actor's sprite image
     *
     * @return
     */
    public Image getImage() {
        String spriteName = this.getClass().getSimpleName();

        Image sprite = imgs.get(spriteName);
        if (sprite == null) {
            sprite = loadSprite(spriteName, null);
            imgs.put(spriteName, sprite);
        }
        return sprite;
    }

    /**
     * Load a sprite from the world's resources
     *
     * @param name
     * @param failFill
     * @return
     */
    protected Image loadSprite(String name, Color failFill) {
        return world.loadSprite(name, Color.red);
    }
}
