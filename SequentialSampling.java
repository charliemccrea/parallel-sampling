import java.awt.Point;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Random;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class SequentialSampling {

	public static final int RADIUS = 5;
	public static final int MAX_FAILED_PTS = 1000;
  public static Random rand = new Random(0);
  /* DEBUG ATTRS */
  public static final int MAX_ITERATIONS = Integer.MAX_VALUE;//100;

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
		int itrs = 0;
    while (failedPts < MAX_FAILED_PTS && itrs < MAX_ITERATIONS) {
			int x = (int) Math.round(rand.nextDouble() * width);
			int y = (int) Math.round(rand.nextDouble() * height);
			System.out.print("Iter: "+ itrs +" "+ x + " "  + y + "\n");
			Point pt = new Point(x, y);
			if (darts.contains(pt)) {
        System.out.println("Point used already! " + failedPts);
        failedPts++;
			} else {
				boolean conflict = false;
				for (int i = -2 * RADIUS; i < RADIUS*2 && !conflict; i++) {
					for (int j = -2 * RADIUS; j < RADIUS*2 && !conflict; j++) {
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
					System.out.println("Added " + pt.getX() + " " + pt.getY());
          darts.add(pt);
          pixels[x][y] = true;
          try {
				    outSVG(width,height,darts,"puppy.jpg");
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
			}
      itrs++;
		}
    System.out.println("Done. Size: " + darts.size());
	}
	
  public static void outSVG(int width, int height, ArrayList<Point> pts, String file) 
    throws IOException {
		// Add a circle tag for each successful point
		// Output as an SVG file.
    StringBuilder str = new StringBuilder();
    str.append("<html>\n<style> svg { background-image: url(\""+file+"\"); }</style>");
    str.append("<svg width=\""+ width +"\" height=\""+height+"\" version=\"1.1\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns=\"http://www.w3.org/2000/svg\">\n");
    //String file will be the filename of the picture to use in SVG file.
    str.append("<defs>\n<pattern id=\"bg_img\" patternUnits=\"userSpaceOnUse\" width=\"" 
        + width + "\" height=\"" + height + "\">\n<image xlink:href=\"" 
        + file +"\" x=\"0\" y=\"0\" width=\"" + width + "\" height=\"" +height+ "\"/>\n"
        + "</pattern>\n</defs>\n");
    str.append("<path fill=\"url(#bg_img)\" x=\"0\" y=\"0\" stroke=\"black\" width=\""+width+"\" height=\""+height+"\"/>");
    //str.append("<image x=\"0\" y=\"0\" width=\"" + width + "\" height=\"" + height + "\" xlink:href=\"" + file + "\"/>\n");
    for (Point p : pts) {
      str.append("<circle cx=\"" + p.getX() + "\" cy=\"" + p.getY() +"\" r=\"" + RADIUS + "\" stroke-width=\"1\" fill=\"none\" stroke=\"blue\" />\n");
    }
      //str.append("<circle cx=\"" + pts.get(0).getX() + "\" cy=\"" + pts.get(0).getY() +"\" r=\"" + RADIUS + "\" stroke-width=\"3\" fill=\"none\" stroke=\"blue\" />\n");
    str.append("</svg>\n</html>\n");
    //Write str.toString() to file, open in browser
    BufferedWriter write = new BufferedWriter(new FileWriter(file + ".html", false));
    write.write(str.toString());
    write.close();
    System.err.println("\n***Wrote new SVG!***\n");
	}
}
