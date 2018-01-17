package run.myCode;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Base64.Decoder;

import javax.imageio.ImageIO;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ZombieResult extends TestResult {
	public static class ScenarioResult extends TestResult.CaseResult {

		private String imgData;
		private Double elapsedTime;

		@JsonProperty("elapsedTime")
		public Double getElapsedTime() {
			return elapsedTime;
		}

		@JsonProperty("elapsedTime")
		public void setElapsedTime(Double elapsedTime) {
			this.elapsedTime = elapsedTime;
		}

		@JsonProperty("image")
		public void setImage(String imgData) {
			this.imgData = imgData;
		}

		@JsonProperty("image")
		public String getImage64() {
			return this.imgData;
		}

		public void setImage(Image image) {
			if (image == null) {
				imgData = "";
				return;
			}

			ByteArrayOutputStream boas = new ByteArrayOutputStream();

			BufferedImage img;
			if (image instanceof BufferedImage) {
				img = (BufferedImage) image;
			} else {
				int width = image.getWidth(null);
				int height = image.getHeight(null);
				img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
				Graphics2D bGr = img.createGraphics();
				bGr.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
				bGr.drawImage(image, 0, 0, width, height, null);
				bGr.dispose();
			}
			try {
				ImageIO.write(img, "png", boas);
			} catch (IOException e) {
			}

			imgData = "data:image/png;base64," + Base64.getEncoder().encodeToString(boas.toByteArray());
		}

		public Image getImage() {
			String data = imgData.substring(imgData.indexOf(',') + 1);
			Decoder decoder = Base64.getDecoder();
			byte[] imgBytes = decoder.decode(data);

			try {
				return ImageIO.read(new ByteArrayInputStream(imgBytes));
			} catch (IOException e) {
				return null;
			}
		}

	}

	public void addTest(String description, String body, boolean success, Image image, Double elapsedSeconds) {
		ScenarioResult test = new ScenarioResult();
		test.description = description;
		test.body = body;
		test.passed = success;
		test.elapsedTime = elapsedSeconds;
		test.setImage(image);

		super.addCase(test);
	}
}
