import java.awt.Point;
import java.util.ArrayList;
import java.util.Scanner;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class SequentialSampling {

	public static final int RADIUS = 20;
	public static final int MAX_FAILED_PTS = 100;

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
			int x = (int) Math.round(Math.random() * width);
			int y = (int) Math.round(Math.random() * height);
			System.err.print(x + " "  + y + "\n");
			Point pt = new Point(x, y);
			if (darts.contains(pt)) {
        System.out.println("Point used already! " + failedPts);
        failedPts++;
			} else {
				boolean conflict = false;
				for (int i = -1 * RADIUS; i < RADIUS && !conflict; i++) {
					for (int j = -1 * RADIUS; j < RADIUS && !conflict; j++) {
						int a = i + x;
						int b = j + y;
						if (a >= 0 && a < width && b >= 0 && b < height) {
							if (pixels[a][b]) {
								System.err.println("Conflict!");
								failedPts++;
								conflict = true;
                System.out.println("Failed point! " + failedPts);
							}
						}
					}
				}
				if (!conflict) {
//					failedPts = 0;
					// Add point to darts array list, will add to svg image.
					darts.add(pt);
          try {
				    outSVG(width,height,darts,"puppy.jpg");
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
			}
		}
	}
	
  public static void outSVG(int width, int height, ArrayList<Point> pts, String file) 
    throws IOException {
		// Add a circle tag for each successful point
		// Output as an SVG file.
    StringBuilder str = new StringBuilder();
    str.append("<svg width=\""+ width +"\" height=\""+height+"\" version=\"1.1\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns=\"http://www.w3.org/2000/svg\">\n");
    str.append("<filter id=\"grayscale\">\n<feColorMatrix type=\"saturate\" values=\"0\"/>\n</filter>\n");

    //String file will be the filename of the picture to use in SVG file.
    str.append("<image x=\"0\" y=\"0\" width=\"" + width + "\" height=\"" + height + "\" xlink:href=\"" + file + "\"/>\n");
    for (Point p : pts) {
      str.append("<circle stroke=\"red\" cx=\"" + p.getX() + "\" cy=\"" + p.getY() +"\" r=\"" + RADIUS + "\" stroke-width=\"4\" fill=\"none\"/>\n");
    }
    str.append("</svg>");
    //Write str.toString() to file, open in browser
    BufferedWriter write = new BufferedWriter(new FileWriter(file + ".svg", false));
    write.write(str.toString());
    write.close();
    System.err.println("\n***Wrote new SVG!***\n");
	}
}
