import processing.core.PApplet;
import processing.data.Table;
import processing.data.TableRow;

public class Main extends PApplet {
	final boolean DEBUG = true;
	Table table;
	boolean translate = false;
	boolean rotate = false;
	int rotateAngle = 20;
	boolean scale = false;
	int translateX = 0;
	int translateY = 0;
	int countTranslateClicks = 0;
	final boolean translationCutsModel = true;

	final int BENT = 0;    // 0000
	final int BAL = 1;    // 0001
	final int JOBB = 2;    // 0010
	final int LENT = 4;    // 0100
	final int FENT = 8;    // 1000

	float padding = 100;
	float xMin;
	float yMin;
	float xMax;
	float yMax;

	static class Line {
		float x1, y1, x2, y2;

		public Line(float x1, float y1, float x2, float y2) {
			this.x1 = x1;
			this.y1 = y1;
			this.x2 = x2;
			this.y2 = y2;
		}
	}


	public void setup() {
		size(640, 480);

		xMin = padding;
		yMin = padding;
		xMax = width - xMin;
		yMax = height - yMin;

		table = new Table();
		table.addColumn("x");
		table.addColumn("y");
	}

	public void draw() {
		background(204);

		if (DEBUG)
			rect(xMin, yMin, width - 2 * xMin, height - 2 * yMin);

		drawLines(table);
	}

	int Zona(double x, double y) {
		int zona = BENT;

		if (x < xMin) zona |= BAL;
		else if (x > xMax) zona |= JOBB;
		if (y < yMin) zona |= LENT;
		else if (y > yMax) zona |= FENT;

		return zona;
	}

	Line CohenSutherlandSzakaszvago(float x0, float y0, float x1, float y1) {
		int pont1Zona = Zona(x0, y0);
		int pont2Zona = Zona(x1, y1);
		boolean elfogad = false;
		boolean vege = false;

		do {
			if ((pont1Zona | pont2Zona) == 0) {
				// mindket pont bent van
				elfogad = true;
				vege = true;
			} else {
				if ((pont1Zona & pont2Zona) != 0) {
					// mindket pont kozos kulso zonaban van (BAL, JOBB, FENT, LENT)
					// nem fogadjuk el, vege
					vege = true;
				} else {
					float x = 0, y = 0;

					// egy pont biztosan kint van
					int pontKint = Math.max(pont2Zona, pont1Zona);

					// Metszespontok:
					// m = (y1 - y0) / (x1 - x0)
					// x = x0 + (1 / m) * (y[Min/Max] - y0)
					// y = y0 + m * (x[Min/Max] - x0)
					if ((pontKint & FENT) != 0) {
						x = x0 + (x1 - x0) * (yMax - y0) / (y1 - y0);
						y = yMax;
					} else if ((pontKint & LENT) != 0) {
						x = x0 + (x1 - x0) * (yMin - y0) / (y1 - y0);
						y = yMin;
					} else if ((pontKint & JOBB) != 0) {
						y = y0 + (y1 - y0) * (xMax - x0) / (x1 - x0);
						x = xMax;
					} else if ((pontKint & BAL) != 0) {
						y = y0 + (y1 - y0) * (xMin - x0) / (x1 - x0);
						x = xMin;
					}

					if (pontKint == pont1Zona) {
						x0 = x;
						y0 = y;
						pont1Zona = Zona(x0, y0);
					} else {
						x1 = x;
						y1 = y;
						pont2Zona = Zona(x1, y1);
					}
				}
			}
		}
		while (!vege);


		if (elfogad) {
			drawLine(x0, y0, x1, y1);
			return new Line(x0, y0, x1, y1);
		}
		return null;
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

			//drawLine(x1, y1, x2, y2); // helyette CohenSutherlandSzakaszvago()
			Line line = CohenSutherlandSzakaszvago(x1, y1, x2, y2);
			if (translationCutsModel) {
				if (line != null) {
					x1 = (int) line.x1;
					y1 = (int) line.y1;
					x2 = (int) line.x2;
					y2 = (int) line.y2;

					table.getRow(i).setInt("x", x2);
					table.getRow(i).setInt("y", y2);
					table.getRow(i + 1).setInt("x", x1);
					table.getRow(i + 1).setInt("y", y1);
				} else {
					table.removeRow(i + 1);
					table.removeRow(i);
				}
			}

			if (line != null)
				drawLine(line.x1, line.y1, line.x2, line.y2);
		}
	}

	public void mousePressed() {
		if (translate || rotate || scale) {
			if (translate) {
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
		} else {
			TableRow newRow = table.addRow();
			newRow.setInt("x", mouseX);
			newRow.setInt("y", mouseY);
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