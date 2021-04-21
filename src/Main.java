import processing.core.PApplet;
import processing.data.Table;
import processing.data.TableRow;
import processing.event.MouseEvent;

public class Main extends PApplet {
	int originX;  // TESTING
	int originY; // TESTING

	Table table;
	boolean translate = false;
	boolean rotate = false;
	final int rotateAngle = 20;
	boolean scale = false;
	float transformX, transformY;
	int countClicks = 0;

	final static class Tinit {
		final float[][] matrix = {{1, 0, 0}, {0, 1, 0}, {0, 0, 1}};

		Tinit() {}
	}

	class MinMax {
		float minX, minY, maxX, maxY;

		public MinMax(float minX, float minY, float maxX, float maxY) {
			this.minX = minX;
			this.minY = minY;
			this.maxX = maxX;
			this.maxY = maxY;
		}

		public void find() {
			float x, y;
			for (TableRow row : table.rows()) {
				x = row.getFloat("x");
				y = row.getFloat("y");

				if (x < minX) minX = x;
				if (y < minY) minY = y;
				if (x > maxX) maxX = x;
				if (y > maxY) maxY = y;
			}
		}
	}

	public void setup() {
		size(640, 480);
		originX = width / 2;  // TESTING
		originY = height / 2; // TESTING

		table = new Table();
		table.addColumn("x");
		table.addColumn("y");

		TableRow newRow = table.addRow();
		newRow.setFloat("x", 325);
		newRow.setFloat("y", 245);

		newRow = table.addRow();
		newRow.setFloat("x", 325);
		newRow.setFloat("y", 340);
	}

	public void draw() {
		background(204);
		line(0, originY, width, originY);  // TESTING
		line(originX, 0, originX, height); // TESTING

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

		if (checkOverflow)
			checkOverflow(method);
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

		transform(T, 0, 0, checkOverflow, "translate");
	}

	void rotate(boolean checkOverflow) {
		float[][] T = new Tinit().matrix;
		T[0][0] = cos(radians(rotateAngle));
		T[0][1] = -sin(radians(rotateAngle));
		T[1][0] = sin(radians(rotateAngle));
		T[1][1] = cos(radians(rotateAngle));

		transform(T, originX, originY, checkOverflow, "translate");

		T = new Tinit().matrix;
		transform(T, originX, originY, checkOverflow, "scale");
	}

	void scale(boolean checkOverflow) {
		countClicks++;

		if (countClicks % 2 == 0) {
			transformX = mouseX - transformX;
			transformY = mouseY - transformY;
			countClicks = 0;

			// TESTING
			if (transformX > 0) {
				transformX = 2;
				transformY = 2;
			} else {
				transformX = 0.5F;
				transformY = 0.5F;
			}
			// TESTING END

			scale(transformX, transformY, checkOverflow);
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

	public void checkOverflow(String method) {
		float deltaX = 0, deltaY = 0;

		switch (method) {
			case "translate": {
				MinMax mm = new MinMax(originX, originY, width, height);
				mm.find();
				if (originX - mm.minX > 0) deltaX = originX - mm.minX;
				if (width - mm.maxX < 0) deltaX = width - mm.maxX;
				if (originY - mm.minY > 0) deltaY = originY - mm.minY;
				if (height - mm.maxY < 0) deltaY = height - mm.maxY;

				if (deltaX != 0 || deltaY != 0) translate(deltaX, deltaY, false);
				break;
			}
			case "scale": {
				TableRow row0 = table.getRow(0);

				MinMax mm = new MinMax(row0.getFloat("x"), row0.getFloat("y"), row0.getFloat("x"), row0.getFloat("y"));
				mm.find();

				deltaX = 1;
				deltaY = 1;

				if (mm.maxX - mm.minX > width - originX) {
					deltaX = (width - originX) / (mm.maxX - mm.minX);
				}
				if (mm.maxY - mm.minY > height - originY) {
					deltaY = (height - originY) / (mm.maxY - mm.minY);
				}

				scale(deltaX, deltaY, false);
				checkOverflow("translate");
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


	// TESTING
	public void mouseWheel(MouseEvent event) {
		float e = event.getCount();
		if (e < 0)
			scale(2, 2, true);
		else
			scale(0.5F, 0.5F, true);
	}
	// TESTING END

	public void settings() {
		setup();
	}

	static public void main(String[] passedArgs) {
		PApplet.main("Main");
	}
}