import java.awt.Image;
import java.util.List;

import greenfoot.Actor;
import zss.Tester.ZombifiedZombieException;

/**
 * A programmable zombie character.
 *
 * @author bdahlem
 * @version 1.0
 */
public abstract class Zombie extends Actor {
	protected volatile Thread thinker;
	protected volatile boolean shouldQuit = false;

	private volatile int numBrains = 0;

	private volatile boolean undead = true;
	private volatile boolean won = false;

	/**
	 * Make a new zombie, you evil person.
	 */
	public Zombie() {
		thinker = new Thread(() -> {
			Thread thisThread = Thread.currentThread();
			// Wait until the zombie is in a world
			while (getWorld() == null && thinker == thisThread) {
			}

			if (shouldQuit) {
				return;
			}

			synchronized (Zombie.class) {
				try {
					Zombie.class.wait(); // Wait for an act signal before beginning the plan
					if (shouldQuit) {
						return;
					}

					plan(); // Follow the plan

					Zombie.class.wait(); // Wait for an act signal after the plan ends for everything to settle down

					if (!shouldQuit && stillTrying()) { // If the Zombie hasn't solved its problems,
						die(); // Kill it
					}

				} catch (InterruptedException | IllegalStateException e) {
					// If the plan is interrupted, or the zombie was removed, causing an illegal
					// state, end the zombie
					if (stillTrying())
						die(true);
				} catch (ZombifiedZombieException e) {
				}

			}
		});

		thinker.start();
	}

	/**
	 * Perform one animation step.
	 */
	@Override
	public final void act() {
		synchronized (Zombie.class) {
			if (!undead || won) { // If the zombie is no more, stop doing things.
				if (!shouldQuit || !thinker.isInterrupted()) {
					shouldQuit = true;
					thinker.interrupt();
				}
			}

			Zombie.class.notify(); // release the lock to perform the next step in the plan
		}
	}

	/**
	 * The special thing about this zombie is that has a plan. The zombie's plan is
	 * run in a separate thread. Commands, such as move() and turnRight() wait their
	 * turn so that they can happen asynchronously with animations, etc.
	 */
	public abstract void plan();

	/**
	 * Determine if this zombie is still struggling to make it in this world.
	 * 
	 * @return true if the zombie has not finished the plan and has not died
	 */
	public boolean stillTrying() {
		boolean worldFinished = false;
		if (getWorld() != null) {
			worldFinished = ((ZombieLand) getWorld()).isFinished();
		}

		return undead && !won && !worldFinished;
	}

	/**
	 * Move forward one step.
	 */
	public final void move() {
		if (shouldQuit) {
			throw new ZombifiedZombieException();
		}
		synchronized (Zombie.class) {
			try {
				Zombie.class.wait(); // Wait for an act signal

				if (stillTrying()) {
					boolean success = handleWall();
					success = success && handleBucket();
					if (success) {
						super.move(1);
					} else {
						undead = false;
						die();
					}
				}
			} catch (InterruptedException e) {
				throw new ZombifiedZombieException();
			}
		}
	}

	/**
	 * Turn 90 degrees to the right.
	 */
	public final void turnRight() {
		turn(1);
	}

	/**
	 * Turn to the right a given number of times
	 * 
	 * @param turns
	 *            the number of times to turn 90 degrees to the right
	 */
	@Override
	public final void turn(int turns) {
		if (shouldQuit) {
			throw new ZombifiedZombieException();
		}
		synchronized (Zombie.class) {
			try {
				Zombie.class.wait();

				int degrees = turns * 90;

				super.turn(degrees);
			} catch (InterruptedException e) {
				throw new ZombifiedZombieException();
			}
		}
	}

	/**
	 * Pick up brains if they exist. End if not.
	 */
	public final void takeBrain() {
		if (shouldQuit) {
			throw new ZombifiedZombieException();
		}
		synchronized (Zombie.class) {
			try {
				Zombie.class.wait();

				if (stillTrying()) {
					Brain b = (Brain) getOneIntersectingObject(Brain.class);

					if (b != null) {
						numBrains++;
						b.removeBrain();
					} else {
						((ZombieLand) getWorld()).finish("Zombie no get brain.", false);
					}
				}
			} catch (InterruptedException e) {
				throw new ZombifiedZombieException();
			}
		}
	}

	/**
	 * Put down a brain if the Zombie has one. End if not.
	 */
	public final void putBrain() {
		if (shouldQuit) {
			throw new ZombifiedZombieException();
		}
		synchronized (Zombie.class) {
			try {
				Zombie.class.wait();

				if (stillTrying()) {
					if (numBrains > 0) {
						numBrains--;

						Brain b = (Brain) getOneIntersectingObject(Brain.class);

						if (b == null) {
							b = new Brain();

							getWorld().addObject(b, getX(), getY());
						} else {
							b.addBrain();
						}
					} else {
						die();
						((ZombieLand) getWorld()).finish("Zombie no have brain.", false);
					}
				}
			} catch (InterruptedException e) {
				throw new ZombifiedZombieException();
			}

		}
	}

	/**
	 * Check if this actor is touching an object with the given classname
	 * 
	 * @param classname
	 *            The name of the object type to check for
	 * @return true if the zombie is touching an object of the given type
	 */
	public final boolean isTouching(String classname) {
		List<Actor> objects = getObjectsAtOffset(0, 0, null);

		for (Actor a : objects) {
			if (a.getClass().getName().equals(classname)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Remove one object that the zombie is touching
	 * 
	 * @param classname
	 *            the name of the type of object to remove
	 */
	public final void removeTouching(String classname) {
		List<Actor> objects = getObjectsAtOffset(0, 0, null);

		for (Actor a : objects) {
			if (a.getClass().getName().equals(classname)) {
				getWorld().removeObject(a);
				return;
			}
		}
	}

	/**
	 * Check if this Zombie is carrying a brain.
	 * 
	 * @return true if the zombie has brains
	 */
	public final boolean haveBrains() {
		if (shouldQuit) {
			throw new ZombifiedZombieException();
		}
		synchronized (Zombie.class) {
			try {
				Zombie.class.wait();

				return numBrains > 0;
			} catch (InterruptedException e) {
				throw new ZombifiedZombieException();
			}
		}
	}

	/**
	 * Check if there is a brain where the zombie is standing.
	 * 
	 * @return true if there is a brain on the ground where the zombie is standing
	 */
	public final boolean isBrainHere() {
		synchronized (Zombie.class) {
			return (isTouching("Brain"));
		}
	}

	/**
	 * Check if there is a wall or the edge of the world in front of the zombie.
	 * 
	 * @return true if there is space to move in front of the zombie
	 */
	public final boolean isFrontClear() {
		if (shouldQuit) {
			throw new ZombifiedZombieException();
		}
		synchronized (Zombie.class) {
			try {
				Zombie.class.wait();

				return checkFront("Wall", 1) == null && checkFront(null, 1) != this;
			} catch (InterruptedException e) {
				throw new ZombifiedZombieException();
			}
		}
	}

	/**
	 * Die, for reals this time.
	 */
	public final void die() {
		if (shouldQuit) {
			throw new ZombifiedZombieException();
		}
		synchronized (Zombie.class) {
			try {
				Zombie.class.wait();

				undead = false;
				shouldQuit = true;
			} catch (InterruptedException e) {
				throw new ZombifiedZombieException();
			}
		}
	}

	/**
	 * Die, for reals this time.
	 * 
	 * @param fast
	 */
	public final void die(boolean fast) {
		if (!fast) {
			die();
		} else {
			undead = false;
			shouldQuit = true;
		}
	}

	/**
	 * Check if this zombie is dead, or just undead
	 * 
	 * @return true if the zombie was killed
	 */
	public final boolean isDead() {
		return undead == false;
	}

	/**
	 * This Zombie has reached its goal in afterlife!
	 */
	public final void win() {
		if (!won) {
			won = true;
		}
	}

	/**
	 * Check if this zombie has accomplished everything it could hope for.
	 * 
	 * @return true if this zombie reached a goal
	 */
	public boolean hasWon() {
		return won;
	}

	/**
	 * Handle a wall in front of the zombie. Everything ends if we crash into a
	 * wall.
	 */
	private boolean handleWall() {
		if (checkFront("Wall", 1) != null || checkFront(null, 1) == this) {
			((ZombieLand) getWorld()).finish("Zombie hit wall.", false);
			return false;
		}
		return true;
	}

	/**
	 * Handle a bucket in front of the zombie. Running into a bucket tries to push
	 * it. If it can't be pushed, everything ends.
	 */
	private boolean handleBucket() {
		Actor bucket = checkFront("Bucket", 1);
		if (bucket != null) {
			if (tryPush(bucket, getRotation()) == false) {
				((ZombieLand) getWorld()).finish("Bucket no move.", false);
				return false;
			}
		}
		return true;
	}

	/**
	 * Attempt to push an object in a given direction
	 */
	private boolean tryPush(Actor item, int dir) {
		dir = dir / 90;
		int dx = 0;
		int dy = 0;

		switch (dir) {
		case 0:
			dx = 1;
			break;
		case 1:
			dy = 1;
			break;
		case 2:
			dx = -1;
			break;
		case 3:
		default:
			dy = -1;
			break;
		}

		if (checkFront("Wall", 2) == null && checkFront(null, 2) != this) {
			item.setLocation(item.getX() + dx, item.getY() + dy);
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Check for an object of a particular class in front of the zombie or if that
	 * distance is beyond the edge of the world
	 *
	 * @param classname
	 *            The class to check for. If null, look for the edge of the world
	 * @param distance
	 *            The distance (in cells) to the front to look for the object/edge
	 * @return The object at the distance, or a reference to this zombie if the
	 *         front is off the edge of the world, null if no object of the given
	 *         class is at that distance or the world does not end within that
	 *         distance.
	 */
	private Actor checkFront(String classname, int distance) {
		int dir = getRotation() / 90;
		int dx = 0;
		int dy = 0;

		switch (dir) {
		case 0:
			dx = 1;
			break;
		case 1:
			dy = 1;
			break;
		case 2:
			dx = -1;
			break;
		case 3:
		default:
			dy = -1;
			break;
		}

		dx *= distance;
		dy *= distance;

		if (classname != null) {
			List<Actor> objects = getObjectsAtOffset(dx, dy, null);

			for (Actor a : objects) {
				if (a.getClass().getSimpleName().equals(classname)) {
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

	@Override
	public Image getImage() {
		String spriteName = "Zombie-right";

		if (hasWon()) {
			spriteName = "Zombie-won";
		} else if (isDead()) {
			spriteName = "Zombie-dead";
		} else {
			int dir = getRotation() / 90;
			switch (dir) {
			case 0:
				spriteName = "Zombie-right";
				break;
			case 1:
				spriteName = "Zombie-down";
				break;
			case 2:
				spriteName = "Zombie-left";
				break;
			case 3:
				spriteName = "Zombie-up";
				break;
			}
		}

		Image sprite = imgs.get(spriteName);
		if (sprite == null) {
			sprite = loadSprite(spriteName, null);
			imgs.put(spriteName, sprite);
		}
		return sprite;
	}

	public Thread getThread() {
		return thinker;
	}

	public void endIt() {
		shouldQuit = true;
		thinker.interrupt();
	}
}
