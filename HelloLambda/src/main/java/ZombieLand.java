import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import greenfoot.Actor;
import greenfoot.World;

/**
 *
 */
public class ZombieLand extends World {

	private List<GoalObject> goal;
	String message = null;
	private boolean done = false;
	private boolean hasWon;
	private final ClassLoader cl;

	/**
	 * Create a ZombieLand of a given size, with a classloader that can load classes
	 * the world should contain
	 * 
	 * @param width
	 * @param height
	 * @param cl
	 */
	public ZombieLand(int width, int height, ClassLoader cl) {
		super(width, height, 64);
		if (cl == null) {
			try {
				URI url = (new File(".")).toURI();
				URL[] urls = new URL[] { url.toURL() };
				cl = new URLClassLoader(urls, this.getClass().getClassLoader());
			} catch (MalformedURLException e) {
			}
		}
		this.cl = cl;

	}

	/**
	 * Create a ZombieLand from an XML description
	 * 
	 * @param desc
	 *            the description of the world
	 * @param cl
	 *            a ClassLoader to use for loading objects in the world
	 * @return The ZombieLand described by @param desc
	 * 
	 * @throws ClassNotFoundException
	 * @throws NoSuchMethodException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public static ZombieLand loadWorld(String desc, ClassLoader cl)
			throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException,
			InvocationTargetException, ParserConfigurationException, SAXException, IOException {
		StringReader sr = new StringReader(desc);
		InputSource ir = new InputSource(sr);

		// Open and parse the world description XML File
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc = db.parse(ir);
		doc.getDocumentElement().normalize();

		// Get a handle to the root of the world description
		Element document = doc.getDocumentElement();
		Element root = (Element) document.getElementsByTagName("world").item(0);

		// Set the world width and height
		int width = Integer.parseInt(root.getAttribute("width"));
		int height = Integer.parseInt(root.getAttribute("height"));
		ZombieLand realWorld = new ZombieLand(width, height, cl);

		// Get handles to the initial and objective description nodes
		Node initial = root.getElementsByTagName("initial").item(0);
		Node objective = root.getElementsByTagName("objective").item(0);

		// Load and place initial objects
		NodeList initialObjects = ((Element) initial).getElementsByTagName("object");
		for (int i = 0; i < initialObjects.getLength(); i++) {
			// Get the object description
			Element obj = (Element) initialObjects.item(i);

			// Determine the classname that describes the object
			String className = obj.getAttribute("classname");

			Class<?> objClass = null;

			// Make sure the class is loaded into the Java runtime
			try {
				objClass = cl.loadClass(className);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}

			// Determine where the class instances are located
			NodeList locations = obj.getElementsByTagName("location");
			for (int j = 0; j < locations.getLength(); j++) {
				Element pos = (Element) locations.item(j);

				// Find out the coordinates of the location
				int x = Integer.parseInt(pos.getAttribute("x"));
				int y = Integer.parseInt(pos.getAttribute("y"));

				int dir = 0;
				if (pos.hasAttribute("dir")) {
					dir = Integer.parseInt(pos.getAttribute("dir"));
				}

				// Find out how many instances are present in this location
				int count = 1;
				if (pos.hasAttribute("count")) {
					count = Integer.parseInt(pos.getAttribute("count"));
				}

				NodeList callList = pos.getElementsByTagName("call");
				List<String[]> calls = null;
				if (callList.getLength() > 0) {
					calls = new ArrayList<>();

					for (int k = 0; k < callList.getLength(); k++) {
						Element method = ((Element) callList.item(k));
						String[] callSignature = new String[2];
						callSignature[0] = method.getAttribute("name");
						callSignature[1] = method.getAttribute("value");

						calls.add(callSignature);
					}
				}

				Constructor<?> constructor;
				Actor a;

				switch (className) {
				case "Brain":
					// Create instances at this location
					constructor = objClass.getConstructor(int.class);
					a = (Actor) constructor.newInstance(count);
					realWorld.addObject(a, x, y);
					a.setRotation(dir);
					if (calls != null) {
						for (String[] call : calls) {
							Class<?>[] params = { int.class };
							Method m = objClass.getMethod(call[0], params);
							m.invoke(a, Integer.parseInt(call[1]));
						}
					}
					break;

				case "MyZombie":
					// Determine how many brains this zombie is carrying
					int numBrains = 0;
					if (pos.hasAttribute("brains")) {
						numBrains = Integer.parseInt(pos.getAttribute("brains"));
					}

					// Create instances at this location
					constructor = objClass.getConstructor();
					for (; count > 0; count--) {
						Zombie z = (Zombie) constructor.newInstance();

						// Give the zombie some brains
						if (numBrains > 0) {
							try {
								Field nb = Zombie.class.getDeclaredField("numBrains");
								nb.setAccessible(true);
								nb.set(z, numBrains);
							} catch (Exception e) {
							}
						}

						// Add the zombie and face it the correct direction
						realWorld.addObject(z, x, y);
						z.setRotation(dir);

						// Make any setup calls
						if (calls != null) {
							for (String[] call : calls) {
								Class<?>[] params = { int.class };
								Method m = objClass.getMethod(call[0], params);
								m.invoke(z, Integer.parseInt(call[1]));
							}
						}
					}
					break;

				default:
					// Create instances at this location
					constructor = objClass.getConstructor();
					for (; count > 0; count--) {
						a = (Actor) constructor.newInstance();
						realWorld.addObject(a, x, y);
						a.setRotation(dir);

						if (calls != null) {
							for (String[] call : calls) {
								Class<?>[] params = { int.class };
								Method m = objClass.getMethod(call[0], params);
								m.invoke(a, Integer.parseInt(call[1]));
							}
						}
					}
					break;
				}
			}
		}

		// Load the solution for this world
		NodeList goalObjects = ((Element) objective).getElementsByTagName("object");
		realWorld.goal = loadGoals(goalObjects, cl);

		return realWorld;
	}

	/**
	 * Load the intended solution for this world.
	 *
	 * @param goalNodes
	 *            a list of XML DOM Elements describing the solution state of the
	 *            actors in the world
	 * @return a list of objects that can be compared to the actors in the world to
	 *         determine if the solution was reached
	 */
	private static List<GoalObject> loadGoals(NodeList goalNodes, ClassLoader cl) {
		List<GoalObject> goalList = new ArrayList<>();

		for (int i = 0; i < goalNodes.getLength(); i++) {
			Element gEl = (Element) goalNodes.item(i);

			String classname = gEl.getAttribute("classname");

			NodeList locations = gEl.getElementsByTagName("location");
			for (int j = 0; j < locations.getLength(); j++) {
				Element pos = (Element) locations.item(j);

				GoalObject gObj = new GoalObject();
				gObj.classLoader = cl;
				gObj.name = classname;
				gObj.x = Integer.parseInt(pos.getAttribute("x"));
				gObj.y = Integer.parseInt(pos.getAttribute("y"));

				if (pos.hasAttribute("count")) {
					gObj.count = Integer.parseInt(pos.getAttribute("count"));
				}

				if (pos.hasAttribute("dir")) {
					gObj.dir = Integer.parseInt(pos.getAttribute("dir"));
				}

				NodeList callList = pos.getElementsByTagName("call");
				if (callList.getLength() > 0) {
					gObj.calls = new ArrayList<>();

					for (int k = 0; k < callList.getLength(); k++) {
						Element method = ((Element) callList.item(k));
						String[] callSignature = new String[2];
						callSignature[0] = method.getAttribute("name");
						callSignature[1] = method.getAttribute("value");

						gObj.calls.add(callSignature);
					}
				}

				goalList.add(gObj);
			}
		}

		return goalList;
	}

	/**
	 * Check the status of the Zombies every frame
	 */
	@Override
	public void act() {
		if (!done) {
			synchronized (Zombie.class) {

				if (checkZombies()) {
					if (checkGoal()) {
					}
				}
			}
		}
	}

	/**
	 * When the mission is ended, stop the world.
	 *
	 * @param success
	 *            true if the goal was met
	 */
	public void finish(boolean success) {
		hasWon = success;
		done = true;
	}

	/**
	 * When the mission is ended, stop the world.
	 *
	 * @param success
	 *            true if the goal was met
	 * @param msg
	 *            A string message describing the ending condition
	 */
	public void finish(String msg, boolean success) {
		hasWon = success;
		message = msg;
		done = true;
	}

	/**
	 * Determine if the zombies met their goal
	 *
	 * @return true if the goal was met
	 */
	public boolean success() {
		return hasWon;
	}

	public String finalMessage() {
		return message;
	}

	/**
	 * Check whether the scenario is complete.
	 *
	 * @return true if the scenario is done
	 */
	public boolean isFinished() {
		return done;
	}

	/**
	 * End the world if there aren't any zombies left.
	 *
	 * @return true if zombies are still active
	 */
	public boolean checkZombies() {
		if (!done) {
			List<Zombie> zombies = getObjects(Zombie.class);

			if (zombies.isEmpty()) {
				finish("Zombie no more.", false);
				return false;
			} else {
				boolean allDead = true;
				for (Zombie z : zombies) {
					if (!z.isDead()) {
						allDead = false;
					}
				}

				if (allDead) {
					finish("Zombie dead.", false);
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Determine if the goal has been reached
	 *
	 * @return true if the goal has been reached
	 */
	public boolean checkGoal() {
		try {
			List<Actor> actors = getObjects(null);
			List<GoalObject> state = new ArrayList<>();
			synchronized (Zombie.class) {
				for (Actor a : actors) {
					GoalObject gObj = new GoalObject();
					gObj.a = a;
					gObj.name = a.getClass().getName();

					gObj.x = a.getX();
					gObj.y = a.getY();
					gObj.dir = a.getRotation();
					gObj.count = 1;
					if (gObj.name.equals("Brain")) {
						try {
							Class<?> objClass = cl.loadClass("Brain");

							Method m = objClass.getMethod("getNum");
							Integer rval = (Integer) m.invoke(a);
							gObj.count = rval;
						} catch (Exception e) {
						}
					}

					if (!gObj.name.contains("$")) {
						boolean duplicate = false;

						for (int i = 0; i < state.size(); i++) {
							GoalObject o = state.get(i);

							if (o.name.equals(gObj.name) && o.x == gObj.x && o.y == gObj.y) {
								duplicate = true;
								o.count = o.count + 1;
								break;
							}
						}

						if (!duplicate) {
							state.add(gObj);
						}
					}
				}

				if (goal != null && state.size() == goal.size()) {
					if (state.containsAll(goal)) {
						finish("Zombie do good.", true);
						return true;
					}
				}
			}
		} catch (Exception e) {
		}

		return false;
	}

	private static class GoalObject {
		public String name;
		public int count = 1;
		public int dir = Integer.MIN_VALUE;
		public int x;
		public int y;
		public List<String[]> calls;
		public Actor a;
		public ClassLoader classLoader;

		@Override
		public boolean equals(Object o) {
			if (o instanceof GoalObject) {
				GoalObject other = (GoalObject) o;

				boolean hasCalls = true;

				if (this.calls != null) {
					if (this.name.equals(other.name)) {
						for (String[] methodCall : this.calls) {
							String methodName = methodCall[0];
							try {
								Class<?> c = classLoader.loadClass(this.name);
								Method m = c.getMethod(methodName);

								String rval = m.invoke(other.a).toString();

								if (!rval.equals(methodCall[1])) {
									hasCalls = false;
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				}

				if (this.dir != Integer.MIN_VALUE) {
					if (this.dir != other.dir) {
						hasCalls = false;
					}
				}
				return this.name.equals(other.name) && this.x == other.x && this.y == other.y
						&& this.count == other.count && hasCalls == true;
			}
			return false;
		}
	}
}
