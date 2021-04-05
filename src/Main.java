import processing.core.PApplet;
import processing.data.Table;
import processing.data.TableRow;

public class Main extends PApplet {
	Table table;
	boolean translate = false;
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

	void drawLine(float x, float y, float x0, float y0) {
		float m;
		float i, j;

		if (x0 != x) { // nem függőleges
			m = (y0 - y) / (x0 - x);

			if (abs(m) <= 1) {
				j = (x < x0) ? y : y0;
				for (i = Math.min(x, x0); i < (Math.max(x, x0)); i++) {
					point(i, j);
					j += m;
				}
			} else {
				i = (y < y0) ? x : x0;
				for (j = Math.min(y, y0); j < (Math.max(y, y0)); j++) {
					point(i, j);
					i += 1/m;
				}
			}
		} else {    // függőleges
			for (j = Math.min(y, y0); j < (Math.max(y, y0)); j++) {
				point(x, j);
			}
		}
	}

	void drawLines(Table table) {
		int x1, y1, x2, y2, i;

		for (i = 0; i < table.getRowCount()-1; i+=2) {
			x2 = table.getRow(i).getInt("x");
			y2 = table.getRow(i).getInt("y");

			x1 = table.getRow(i+1).getInt("x");
			y1 = table.getRow(i+1).getInt("y");

			drawLine(x1, y1, x2, y2);
		}
	}

	public void mousePressed() {
		redraw();
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
					row.setInt("x",row.getInt("x") + translateX);
					row.setInt("y",row.getInt("y") + translateY);
				}
			} else {
				translateX = mouseX;
				translateY = mouseY;
			}
		}
	}

	public void keyPressed() {
		if (key == 't') {
			translate = !translate;
		}
	}

	public void settings() { setup(); }

	static public void main(String[] passedArgs) {
		PApplet.main("Main");
	}
}