package zss.Tester;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

import javax.imageio.ImageIO;

/**
 * A description of a ZombieLand scenario run
 */
public class Result {
	protected Result(Boolean success, String msg, Image img, double elapsedSec, long actCount) {
		this.success = success;
		this.message = msg;
		this.image = img;
		this.time = elapsedSec;
		this.actCount = actCount;
	}

	private final boolean success;
	private final Image image;
	private final String message;
	private String output;
	private final double time;
	private final long actCount;

	/**
	 * Get the amount of time elapsed during the scenario run
	 * 
	 * @return The amount of time elapsed (in seconds)
	 */
	public double getTime() {
		return time;
	}

	/**
	 * @return the actCount
	 */
	public long getActCount() {
		return actCount;
	}

	/**
	 * Get any System output from the scenario run
	 * 
	 * @return System output from the scenario run
	 */
	public String getOutput() {
		return output;
	}

	protected void setOutput(String output) {
		this.output = output;
	}

	/**
	 * Determine if the scenario run was successful
	 * 
	 * @return true if the run was successful
	 */
	public boolean success() {
		return success;
	}

	/**
	 * Get the scenario finished message
	 * 
	 * @return the scenario finished message
	 */
	public String message() {
		return message;
	}

	/**
	 * Get a snapshot from the end of the scenario run
	 * 
	 * @return the final snapshot of the run
	 */
	public Image image() {
		return image;
	}

	@Override
	public String toString() {
		ByteArrayOutputStream boas = new ByteArrayOutputStream();
		BufferedImage img;
		int width = image.getWidth(null);
		int height = image.getHeight(null);
		if (image instanceof BufferedImage) {
			img = (BufferedImage) image;
		} else {
			img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
			Graphics2D bGr = img.createGraphics();
			bGr.drawImage(image, 0, 0, null);
			bGr.dispose();
		}

		try {
			ImageIO.write(img, "png", boas);
		} catch (IOException e) {
		}

		String imgData = Base64.getEncoder().encodeToString(boas.toByteArray());

		String json = "{\"success\": " + success + ", " + "\"message\": \"" + message + "\", " + "\"elapsed\": " + time
				+ ", " + "\"image\": \"data:image/png;base64," + imgData + "\", " + "\"imgWidth\": " + width + ", "
				+ "\"imgHeight\": " + height + "}";

		return json;
	}

}
