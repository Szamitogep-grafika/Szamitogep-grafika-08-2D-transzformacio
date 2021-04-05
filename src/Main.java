import processing.core.PApplet;
import processing.data.Table;
import processing.data.TableRow;

public class Main extends PApplet {
	Table table;
	boolean translate = false;
	boolean rotate = false;
	int rotateAngle = 20;
	boolean scale = false;
	int translateX = 0;
	int translateY = 0;
	int countTranslateClicks = 0;

	public void setup() {
		size(640, 480);

		table = new Table();
		table.addColumn("x");
		table.addColumn("y");
		noLoop();
	}

	public void draw() {
		background(204);
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
		int x1, y1, x2, y2, i;

		for (i = 0; i < table.getRowCount() - 1; i += 2) {
			x2 = table.getRow(i).getInt("x");
			y2 = table.getRow(i).getInt("y");

			x1 = table.getRow(i + 1).getInt("x");
			y1 = table.getRow(i + 1).getInt("y");

			drawLine(x1, y1, x2, y2);
		}
	}

	public void mousePressed() {
		if (!translate) {
			TableRow newRow = table.addRow();
			newRow.setInt("x", mouseX);
			newRow.setInt("y", mouseY);
		} else {
			countTranslateClicks++;

			if (countTranslateClicks % 2 == 0) {
				translateX = mouseX - translateX;
				translateY = mouseY - translateY;
				countTranslateClicks = 0;

				for (TableRow row : table.rows()) {
					row.setInt("x", row.getInt("x") + translateX);
					row.setInt("y", row.getInt("y") + translateY);
				}
			} else {
				translateX = mouseX;
				translateY = mouseY;
			}
		}
		redraw();
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
				case 'r': {
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

	public void settings() {
		setup();
	}

	static public void main(String[] passedArgs) {
		PApplet.main("Main");
	}
}