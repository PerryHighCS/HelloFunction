import greenfoot.Actor;

public class Fire extends Actor {

	private static final int FIRE = 0;
	private static final int SMOKE = 1;
	private int fireMode;

	public Fire() {
		fireMode = FIRE;
	}

	/**
	 * Animate the fire
	 */
	@Override
	public void act() {
		if (fireMode == SMOKE) {
			((ZombieLand) getWorld()).checkZombies();
			getWorld().removeObject(this);
			return;
		}

		checkForZombies();
		checkForWater();
	}

	/**
	 * See if a zombie has wandered into this fire, burn it up if so.
	 */
	private void checkForZombies() {
		while (fireMode == FIRE && isTouching(Zombie.class)) {
			Zombie z = (Zombie) getOneIntersectingObject(Zombie.class);
			z.die(true);

			fireMode = SMOKE;
		}
	}

	/**
	 * See if a bucket of water has been pushed into this fire, burn it up and
	 * extinguish if so.
	 */
	private void checkForWater() {
		synchronized (Zombie.class) {
			ClassLoader cl = this.getClass().getClassLoader();

			try {
				Class<?> bucketClass = cl.loadClass("Bucket");

				while (isTouching(bucketClass)) {
					removeTouching(bucketClass);
					fireMode = SMOKE;
				}
			} catch (ClassNotFoundException e) {
			}
		}
	}
}
