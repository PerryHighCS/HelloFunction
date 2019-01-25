
import java.util.List;

import greenfoot.Actor;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import zss.Tester.ZombifiedZombieException;

/**
 * An evolved zombie with POWERS!
 */
public abstract class UltraZombie extends Zombie {

    public static final int EAST = 0;
    public static final int SOUTH = 90;
    public static final int WEST = 180;
    public static final int NORTH = 270;

    public UltraZombie() {
        super();
    }

    /**
     * A constructor provided for compatibility. Not used.
     *
     * @param numBrains - Ignored
     */
    protected UltraZombie(int numBrains) {
        super(numBrains);
    }

    /**
     * Determine which direction the UltraZombie is facing.
     *
     * @return a value of EAST, SOUTH, WEST, or NORTH
     */
    public final int facing() {
        CompletableFuture<Integer> result = new CompletableFuture<>();

        nextAction(() -> {
            result.complete(dirFacing());
        });

        try {
            return result.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new ZombifiedZombieException();
        }
    }

    private int dirFacing() {
        return (getRotation() / 90) * 90;
    }

    /**
     * Determine if the UltraZombie is facing a particular direction.
     *
     * @param direction The direction to check (NORTH, SOUTH, EAST, or WEST)
     * @return true if the UltraZombie is facing that direction.
     */
    public final boolean isFacing(int direction) {
        CompletableFuture<Boolean> result = new CompletableFuture<>();

        nextAction(() -> {
            result.complete(dirFacing() == direction);
        });

        try {
            return result.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new ZombifiedZombieException();
        }
    }

    /**
     * Turn 90 degrees to the left
     */
    public final void turnLeft() {
        if (shouldQuit) {
            throw new ZombifiedZombieException();
        }

        nextAction(() -> {
            turn(-1);
        });
    }

    /**
     * Turn 180 degrees
     */
    public final void turnAround() {
        if (shouldQuit) {
            throw new ZombifiedZombieException();
        }

        nextAction(() -> {
            turn(2);
        });
    }

    /**
     * Turn the zombie to face a particular direction (NORTH, SOUTH, EAST, or
     * WEST);
     *
     * @param direction the direction to face
     */
    public final void turnTo(int direction) {
        if (shouldQuit) {
            throw new ZombifiedZombieException();
        }
        nextAction(() -> {
            turn((direction - getRotation()) / 90);
        });
    }

    /**
     * Check if there is a wall or the edge of the world to the right of the
     * zombie.
     *
     * @return if there is space to move to the zombie's right
     */
    public final boolean isRightClear() {
        if (shouldQuit) {
            throw new ZombifiedZombieException();
        }
        CompletableFuture<Boolean> result = new CompletableFuture<>();

        nextAction(() -> {
            int dir = dirFacing();
            int dx = 0;
            int dy = 0;

            switch (dir) {
                case EAST:
                    dy = 1;
                    break;
                case SOUTH:
                    dx = -1;
                    break;
                case WEST:
                    dy = -1;
                    break;
                default:
                    dx = 1;
                    break;
            }
            result.complete(checkDelta("Wall", dx, dy) == null
                    && checkDelta(null, dx, dy) != this);
        });

        try {
            return result.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new ZombifiedZombieException();
        }
    }

    /**
     * Check if there is a wall or the edge of the world to the left of the
     * zombie.
     *
     * @return true if there is space to move to the zombie's left
     */
    public final boolean isLeftClear() {
        if (shouldQuit) {
            throw new ZombifiedZombieException();
        }
        CompletableFuture<Boolean> result = new CompletableFuture<>();

        nextAction(() -> {
            int dir = dirFacing();
            int dx = 0;
            int dy = 0;

            switch (dir) {
                case EAST:
                    dy = -1;
                    break;
                case SOUTH:
                    dx = 1;
                    break;
                case WEST:
                    dy = 1;
                    break;
                default:
                    dx = -1;
                    break;
            }

            result.complete(checkDelta("Wall", dx, dy) == null
                    && checkDelta(null, dx, dy) != this);
        });

        try {
            return result.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new ZombifiedZombieException();
        }
    }

    /**
     * Check if there is a wall or the edge of the world to the back of the
     * zombie.
     *
     * @return true if there is space to move behind the zombie
     */
    public final boolean isBackClear() {
        if (shouldQuit) {
            throw new ZombifiedZombieException();
        }
        CompletableFuture<Boolean> result = new CompletableFuture<>();

        nextAction(() -> {
            int dir = dirFacing();
            int dx = 0;
            int dy = 0;

            switch (dir) {
                case EAST:
                    dx = -1;
                    break;
                case SOUTH:
                    dy = -1;
                    break;
                case WEST:
                    dx = 1;
                    break;
                default:
                    dy = 1;
                    break;
            }
            result.complete(checkDelta("Wall", dx, dy) == null
                    && checkDelta(null, dx, dy) != this);
        });

        try {
            return result.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new ZombifiedZombieException();
        }
    }

    /**
     * Check if there is a wall or the edge of the world in a certain direction
     * from the Zombie.
     *
     * @param direction The direction to look for a wall (NORTH, SOUTH, EAST, or
     * WEST)
     * @return if there is space to move in the given direction
     */
    public final boolean isDirectionClear(int direction) {
        if (shouldQuit) {
            throw new ZombifiedZombieException();
        }
        CompletableFuture<Boolean> result = new CompletableFuture<>();

        nextAction(() -> {
            int dx = 0;
            int dy = 0;

            switch (direction) {
                case EAST:
                    dx = 1;
                    break;
                case SOUTH:
                    dy = 1;
                    break;
                case WEST:
                    dx = -1;
                    break;
                case NORTH:
                    dy = -1;
                    break;
            }
            result.complete(checkDelta("Wall", dx, dy) == null
                    && checkDelta(null, dx, dy) != this);
        });

        try {
            return result.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new ZombifiedZombieException();
        }
    }

    /**
     * Check for an object of a particular class at an offset from the zombie or
     * if that distance is beyond the edge of the world
     *
     * @param classname The class to check for. If null, look for the edge of
     * the world
     * @param dx The distance(in cells) along the x-axis to look for the
     * object/edge
     * @param dy The distance(in cells) along the y-axis to look for the
     * object/edge
     * @return The object at the offset, or a reference to this zombie if the
     * offset is off the edge of the world, null if no object of the given class
     * is at that distance or the world does not end within that distance.
     */
    private Actor checkDelta(String classname, int dx, int dy) {
        if (classname != null) {
            List<Actor> objects = getObjectsAtOffset(dx, dy, null);

            for (Actor a : objects) {
                if (a.getClass().getName().equals(classname)) {
                    return a;
                }
            }

            return null;
        } else {
            int nextX = getX() + dx;
            int nextY = getY() + dy;
            if ((nextX >= 0 && nextX < getWorld().getWidth()) && (nextY >= 0 && nextY < getWorld().getHeight())) {
                return getOneObjectAtOffset(dx, dy, null);
            } else {
                return this;
            }
        }
    }
}
