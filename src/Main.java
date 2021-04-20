import processing.core.PApplet;
import processing.data.Table;
import processing.data.TableRow;
import processing.event.MouseEvent;

public class Main extends PApplet {
	Table table;
	boolean translate = false;
	boolean rotate = false;
	int rotateAngle = 30;
	boolean scale = false;
	float transformX, transformY;
	int countClicks = 0;

	final int width = 640;
	final int height = 480;
	final float originX = width / 3;
	final float originY = height / 3;

	final float[][] tInit = {{1, 0, 0}, {0, 1, 0}, {0, 0, 1}};
	float[] pInit = new float[]{0, 0, 1};

	final class Tinit {
		final float[][] matrix = {{1, 0, 0}, {0, 1, 0}, {0, 0, 1}};

		Tinit() {
		}
	}

	public void setup() {
		size(width, height);

		table = new Table();
		table.addColumn("x");
		table.addColumn("y");

		TableRow newRow = table.addRow();
		newRow.setFloat("x", 325);
		newRow.setFloat("y", 245);

		newRow = table.addRow();
		newRow.setFloat("x", 340);
		newRow.setFloat("y", 340);
	}

	public void draw() {
		background(204);
		line(0, originY, width, originY);
		line(originX, 0, originX, height);

		drawLines(table);
	}

	void drawLine(float x1, float y1, float x2, float y2) {
		float m;
		float i, j;

		if (x2 != x1) { // nem függőleges
			m = (y2 - y1) / (x2 - x1);

			if (abs(m) <= 1) {
				j = (x1 < x2) ? y1 : y2;
				for (i = Math.min(x1, x2); i < (Math.max(x1, x2)); i++) {
					point(i, j);
					j += m;
				}
			} else {
				i = (y1 < y2) ? x1 : x2;
				for (j = Math.min(y1, y2); j < (Math.max(y1, y2)); j++) {
					point(i, j);
					i += 1 / m;
				}
			}
		} else {    // függőleges
			for (j = Math.min(y1, y2); j < (Math.max(y1, y2)); j++) {
				point(x1, j);
			}
		}
	}

	void drawLines(Table table) {
		int i;
		float x1, y1, x2, y2;

		for (i = 0; i < table.getRowCount() - 1; i += 2) {
			x2 = table.getRow(i).getFloat("x");
			y2 = table.getRow(i).getFloat("y");

			x1 = table.getRow(i + 1).getFloat("x");
			y1 = table.getRow(i + 1).getFloat("y");

			drawLine(x1, y1, x2, y2);
		}
	}

	float[] matrixMultiplication(float[][] t, float[] p) {
		float[] transformed = new float[]{0, 0, 1};

		for (int i = 0; i < t.length; i++) {
			float sum = 0;
			for (int j = 0; j < t.length; j++) {
				sum += t[i][j] * p[j];
			}
			transformed[i] = sum;
		}

		return transformed;
	}

	void transform(float[][] T, float originX, float originY, boolean checkOverflow, String method) {
		for (TableRow row : table.rows()) {
			float[] p = {0, 0, 1};
			p[0] = row.getFloat("x") - originX;
			p[1] = row.getFloat("y") - originY;

			p = matrixMultiplication(T, p);

			row.setFloat("x", p[0] + originX);
			row.setFloat("y", p[1] + originY);
		}

		if (checkOverflow) checkOverflow(method);
	}

	void translate(boolean checkOverflow) {
		countClicks++;

		if (countClicks % 2 == 0) {
			transformX = mouseX - transformX;
			transformY = mouseY - transformY;
			countClicks = 0;

			translate(transformX, transformY, checkOverflow);
		} else {
			transformX = mouseX;
			transformY = mouseY;
		}
	}

	public void translate(float transformX, float transformY, boolean checkOverflow) {
		float[][] T = new Tinit().matrix;
		T[0][2] = transformX;
		T[1][2] = transformY;

		transform(T, 0,0,checkOverflow, "translate");
	}

	void rotate(boolean checkOverflow) {
		float[][] T = new Tinit().matrix;
		T[0][0] = cos(radians(rotateAngle));
		T[0][1] = -sin(radians(rotateAngle));
		T[1][0] = sin(radians(rotateAngle));
		T[1][1] = cos(radians(rotateAngle));

		transform(T, originX, originY, checkOverflow, "translate");
	}

	void scale(boolean checkOverflow) {
		countClicks++;

		if (countClicks % 2 == 0) {
			transformX = mouseX - transformX;
			transformY = mouseY - transformY;
			countClicks = 0;

			scale((float) (transformX * 1), (float) (transformY * 1), checkOverflow);
		} else {
			transformX = mouseX;
			transformY = mouseY;
		}
	}

	void scale(float transformX, float transformY, boolean checkOverflow) {
		float[][] T = new Tinit().matrix;
		T[0][0] = transformX;
		T[1][1] = transformY;

		transform(T, originX, originY, checkOverflow, "scale");
	}

	public void scale(float scale) {
		for (TableRow row : table.rows()) {
			row.setFloat("x", mouseX + (row.getFloat("x") - mouseX) * scale);
			row.setFloat("y", mouseY + (row.getFloat("y") - mouseY) * scale);
		}
	}

	public void checkOverflow(String method) {
		float minX = originX, minY = originY, maxX = width, maxY = height;
		float deltaX = 0, deltaY = 0;
		float x, y;

		for (TableRow row : table.rows()) {
			x = row.getFloat("x");
			y = row.getFloat("y");

			if (x < minX) minX = x;
			if (y < minY) minY = y;
			if (x > maxX) maxX = x;
			if (y > maxY) maxY = y;
		}
		switch (method) {
			case "translate": {
				if (originX - minX > 0) deltaX = originX - minX;
				if (width - maxX < 0) deltaX = width - maxX;
				if (originY - minY > 0) deltaY = originY - minY;
				if (height - maxY < 0) deltaY = height - maxY;

				if (deltaX != 0 || deltaY != 0) translate(deltaX, deltaY, false);
				break;
			}
			case "scale": {
				if (deltaX != 0 || deltaY != 0) translate(deltaX, deltaY, false);
				break;
			}
		}
	}

	public void mousePressed() {
		if (translate || rotate || scale) {
			if (translate) {
				translate(true);
			}
			if (rotate) {
				rotate(true);
			}
			if (scale) {
				scale(true);
			}
		} else {
			TableRow newRow = table.addRow();
			newRow.setFloat("x", mouseX);
			newRow.setFloat("y", mouseY);
		}
	}

	public void keyPressed() {
		if (table.getRowCount() % 2 == 0)   // Megkezdett modell-elem esetén a transzformációk nem kapcsolhatók be
			switch (key) {                  // A három funkció közül egyszerre csak az egyik működjön
				case 't': {
					translate = !translate;
					rotate = false;
					scale = false;
					break;
				}
				case 'f': {
					rotate = !rotate;
					translate = false;
					scale = false;
					break;
				}
				case 's': {
					scale = !scale;
					translate = false;
					rotate = false;
					break;
				}
			}
	}

	public void mouseWheel(MouseEvent event) {
		float e = event.getCount();
		if (e < 0)
			scale((float) 1.1);
		else
			scale((float) (1.0 / 1.1));
	}

	public void settings() {
		setup();
	}

	static public void main(String[] passedArgs) {
		PApplet.main("Main");
	}
}