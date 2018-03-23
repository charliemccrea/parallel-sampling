import java.awt.Point;
import java.util.ArrayList;
import java.util.Scanner;

public class SequentialSampling {

	public static final int RADIUS = 20;
	public static final int MAX_FAILED_PTS = 1000;

	public static void main(String[] args) {
		Scanner in = new Scanner(System.in);
		// Scanner in = new Scanner(args[1]); //For filename input.

		String dimStr = in.nextLine();
		System.out.println(dimStr);
		String[] dim = dimStr.split(" ");
		int width = Integer.parseInt(dim[0]);
		int height = Integer.parseInt(dim[1]);
		// Create a 2D Array to store
		boolean[][] pixels = new boolean[width][height];
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				pixels[i][j] = false;
			}
		}
		ArrayList<Point> darts = new ArrayList<Point>();

		int failedPts = 0;
		while (failedPts < MAX_FAILED_PTS) {
			System.out.println("Looping: ");
			int x = (int) Math.round(Math.random() * width);
			int y = (int) Math.round(Math.random() * height);
			System.out.print(x + " "  + y + "\n");
			Point pt = new Point(x, y);
			if (darts.contains(pt)) {

			} else {
				boolean conflict = false;
				for (int i = -1 * RADIUS; i < RADIUS && !conflict; i++) {
					for (int j = -1 * RADIUS; j < RADIUS && !conflict; j++) {
						int a = i + x;
						int b = j + y;
						if (a >= 0 && a < width && b >= 0 && b < height) {
							if (pixels[a][b]) {
								System.out.println("Conflict!");
								failedPts++;
								conflict = true;
							}
						}
					}
				}
				if (!conflict) {
					failedPts = 0;
					// Add point to darts array list, will add to svg image.
					darts.add(pt);
					// Add svg circle
				}
			}
		}
	}
	//"     <circle cx=\""+width+"\" cy=\""+height+"\" r=\"5\" stroke=\"black\" stroke-width=\"3\" fill=\"black\"/>\n" +
	// needs to be placed in a loop to go through points in an array list
	// need to print to an out svg file
	public static void outSVG(int width, int height, ArrayList<Point> pts, String file) {
		// Add a circle tag for each successful point
		// Output as an SVG file.
		System.out.println("<svg width=\""+ width +"\" height=\""+height+"\" version=\"1.1\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns=\"http://www.w3.org/2000/svg\">\n" + 
				"     <filter id=\"grayscale\">\n" + 
				"          <feColorMatrix type=\"saturate\" values=\"0\"/>\n" + 
				"     </filter>\n" + 
				"     <image class=\"bw\" filter=\"url(#grayscale)\" width=\"100%\" height=\"100%\" xlink:href=\"image.jpg\" />\n" + 
				"     <image class=\"color\" width=\"100%\" height=\"100%\" xlink:href=\"image.jpg\" />\n" + 
				"</svg>");
		
	}
}
