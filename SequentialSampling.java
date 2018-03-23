import java.awt.Point;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Random;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.File;

public class SequentialSampling
{
	public static final int RADIUS = 3;
	public static final int MAX_FAILED_PTS = 100000;
	/* DEBUG ATTRS */
	public static final int MAX_ITERATIONS = Integer.MAX_VALUE; //100;

	public static void main(String[] args)
	{
		if (args.length != 2)
		{
			System.out.println("Enter a .grey file name and the picture!");
			return;
		}

		Scanner in;
		int width = 0;
		int height = 0;
		String[] dim;
		String dimStr;

		try
		{
			in     = new Scanner(new File(args[0])); //For filename input.
			dimStr = in.nextLine();
			System.out.println(dimStr);
			dim    = dimStr.split(" ");
			width  = Integer.parseInt(dim[0]);
			height = Integer.parseInt(dim[1]);
		}
		catch (IOException e)
		{
			System.out.println("Failed to use the given file.");
			return;
		}

		Random rand = new Random(99);

		// Create a 2D Array to store
		boolean[][] pixels = new boolean[width][height];
		for (int i = 0; i < width; i++)
		{
			for (int j = 0; j < height; j++)
			{
				pixels[i][j] = false;
			}
		}

		ArrayList<Point> darts = new ArrayList<Point>();
		int failedPts = 0;
		int itrs = 0;

		while (failedPts < MAX_FAILED_PTS && itrs < MAX_ITERATIONS)
		{
			int x = (int) Math.floor(rand.nextDouble() * width);
			int y = (int) Math.floor(rand.nextDouble() * height);
			//System.out.print("Iter: "+ itrs +" "+ x + " "  + y + "\n");
			Point pt = new Point(x, y);
			if (darts.contains(pt))
			{
				//System.out.println("Point used already! " + failedPts);
				failedPts++;
			}
			else
			{
				boolean conflict = false;
				for (int i = -2 * RADIUS; i < RADIUS*2 && !conflict; i++)
				{
					for (int j = -2 * RADIUS; j < RADIUS*2 && !conflict; j++)
					{
						int a = i + x;
						int b = j + y;
						if (a >= 0 && a < width && b >= 0 && b < height)
						{
							if (pixels[a][b])
							{
								System.err.println("Conflict!");
								failedPts++;
								conflict = true;
								//System.out.println("Failed point! " + failedPts);
							}
						}
					}
				}

				if (!conflict)
				{
					//System.out.println("Added " + pt.getX() + " " + pt.getY());
					darts.add(pt);
					pixels[x][y] = true;
				}
			}
			itrs++;
		}

		try
		{
			outSVG(width,height,darts,args[0], args[1]);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		System.out.println("Done. Size: " + darts.size());
	}

	public static void outSVG(int width, int height, ArrayList<Point> pts, String grey, String picture) throws IOException
	{
		// Add a circle tag for each successful point
		// Output as an SVG file.
		System.out.println("Picture background: " + picture);
		StringBuilder str = new StringBuilder();
		str.append("<html>\n<style> svg { background-image: url(\""+picture+"\"); }</style>");
		str.append("<svg width=\""+ width +"\" height=\""+height+"\" version=\"1.1\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns=\"http://www.w3.org/2000/svg\">\n");
		//String file will be the filename of the picture to use in SVG file.
		str.append("<defs>\n<pattern id=\"bg_img\" patternUnits=\"userSpaceOnUse\" width=\""
			+ width + "\" height=\"" + height + "\">\n<image xlink:href=\""
			+ grey +"\" x=\"0\" y=\"0\" width=\"" + width + "\" height=\"" +height+ "\"/>\n"
			+ "</pattern>\n</defs>\n");
		str.append("<path fill=\"url(#bg_img)\" x=\"0\" y=\"0\" stroke=\"black\" width=\""+width+"\" height=\""+height+"\"/>");
		//str.append("<image x=\"0\" y=\"0\" width=\"" + width + "\" height=\"" + height + "\" xlink:href=\"" + file + "\"/>\n");
		for (Point p : pts)
		{
			str.append("<circle cx=\"" + p.getX() + "\" cy=\"" + p.getY() +"\" r=\"" + RADIUS + "\" stroke-width=\"1\" fill=\"none\" stroke=\"blue\" />\n");
		}
		str.append("</svg>\n</html>\n");

		//Write str.toString() to file, open in browser
		BufferedWriter write = new BufferedWriter(new FileWriter(grey + ".html", false));
		write.write(str.toString());
		write.close();
		System.err.println("\n*** Wrote new SVG! ***\n");
	}
}
