/*********
* CS 470
* Serial Anisotropic Sampling
* Georgia Corey, Sam Kiwus, Charlie McCrea, and Jason Zareski
**********/

import java.awt.Point;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Random;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.File;

public class AnisotropicSerial
{
	/* Settings */
	public static double RATIO = 0.1;
	public static final String CIRCLE_COLOR = "red";

	/* DEBUG ATTRS */
	public static final int MAX_ITERATIONS = Integer.MAX_VALUE;
	public static int hits = 0;
	public static int misses = 0;
	public static int width = 0;
	public static int height = 0;
	public static int [][] rgbValues;

	public static void main(String[] args)
	{
		System.err.println("Errors in CLI? " + validCLI(args));

		Scanner in;
		String[] dim;
		String dimStr;
		long startTime;
		long endTime;
		int [][] rgbValues;

		try
		{
			in     = new Scanner(new File(args[0])); //For filename input.
			dimStr = in.nextLine();
			System.out.println("WxH = " + dimStr);
			dim    = dimStr.split(" ");
			width  = Integer.parseInt(dim[0]);
			height = Integer.parseInt(dim[1]);
			rgbValues = new int[width][height];
			for (int i = 0; i < width; i++)
			{
				for (int j = 0; j < height; j++)
				{
					rgbValues[i][j] = in.nextInt();
				}
			}
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

		ArrayList<Dart> darts = new ArrayList<Dart>();
		int failedPts = 0;
		int numConflicts = 0;
		int itrs = 0;

		startTime = System.currentTimeMillis();
		darts = sample(pixels, rand, failedPts, numConflicts, itrs, rgbValues);
		endTime = System.currentTimeMillis();

		System.out.println("Done!");
		System.out.println("Number of misses: " + misses);
		System.out.println("Creating SVG file...");

		try
		{
			System.out.println("Using grey file: " + args[0] + " and picture file: " + args[1]);
			outSVG(width,height,darts,args[0], args[1]);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		System.out.println("Number of darts: " + darts.size());
		System.out.println("Runtime: " + (endTime - startTime) + " ms");
	}

	public static ArrayList<Dart> sample(boolean[][] pixels, Random rand, int failedPts, int numConflicts, int itrs, int[][] rgbValues)
	{
		ArrayList<Dart> darts = new ArrayList<>();

		while (misses == 0 || (((double) hits/ (double)misses) > RATIO && itrs < MAX_ITERATIONS))
		{
			int x = (int) Math.floor(rand.nextDouble() * width);
			int y = (int) Math.floor(rand.nextDouble() * height);
			Dart pt = new Dart(x, y, rgbValues[x][y]);

			if (!darts.contains(pt))
			{
				boolean conflict = false;
				int rad = pt.getRadius();
				for (int i = -2*rad; i < rad*2 && !conflict; i++)
				{
					for (int j = -2*rad; j < rad*2 && !conflict; j++)
					{
						int a = i + x;
						int b = j + y;
						if (a >= 0 && a < width && b >= 0 && b < height)
						{
							if (pixels[a][b])
							{
								failedPts++;
								conflict = true;
							}
						}
					}
				}

				if (!conflict)
				{
					hits++;
					darts.add(pt);
					pixels[x][y] = true;
					for (int i = -2*rad; i < rad*2 && !conflict; i++)
					{
						for (int j = -2*rad; j < rad*2 && !conflict; j++)
						{
							int a = i + x;
							int b = j + y;
							if (a >= 0 && a < width && b >= 0 && b < height)
							{
								pixels[a][b] = true;
							}
						}
					}
				}
				else
				{
					misses++;
				}
			}
			itrs++;
		}
		return darts;
	}

	public static boolean validCLI(String[] args)
	{
		boolean valid = true;
		RATIO = 0.25;

		if (args.length != 3)
		{
			System.out.println("java AnisotropicSerial <grey:file> <image:file> <ratio:double>");
			return false;
		}

		try
		{
			RATIO = Double.parseDouble(args[2]);
		}
		catch(Exception ex)
		{
			System.err.println("Failed to use given ratio, using default 0.25.");
			valid = false;
		}

		return valid;
	}

	public static void outSVG(int width, int height, ArrayList<Dart> pts, String grey, String picture) throws IOException
	{
		// Add a circle tag for each successful point
		// Output as an SVG file.
		StringBuilder str = new StringBuilder();
		str.append(String.format("<html>\n<style>\n\tsvg {\n\t\tbackground-image: url(\"%s\");\n\t}\n</style>", picture));
		str.append(String.format("<svg width=\"%d\" height=\"%d\" version=\"1.1\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns=\"http://www.w3.org/2000/svg\">\n", width, height));
		str.append(String.format("<defs>\n<pattern id=\"bg_img\" patternUnits=\"userSpaceOnUse\" width=\"%d\" height=\"%d\">\n<image xlink:href=\"%s\" x=\"0\" y=\"0\" width=\"%d\" height=\"%d\"/>\n</pattern>\n</defs>\n", width, height, grey, width, height));
		str.append(String.format("<path fill=\"url(#bg_img)\" x=\"0\" y=\"0\" stroke=\"black\" width=\"%d\" height=\"%d\"/>", width, height));
		BufferedWriter write = new BufferedWriter(new FileWriter(grey + ".html", false));
		write.write(str.toString());

		for (Dart d : pts)
		{
			write.write("<circle cx=\"" + d.getX() + "\" cy=\"" + d.getY() +"\" r=\"" + d.getRadius() + "\" stroke-width=\"1\" fill=\"none\" stroke=\""+CIRCLE_COLOR+"\" />\n");
            write.flush();
		}
		write.write("</svg>\n</html>\n");

		//Write str.toString() to file, open in browser
		write.close();
		//System.err.println("Wrote new SVG");
	}

	public static class Dart
	{
		private int x;
		private int y;
		private int value;
		private int radius;

		public Dart(int width, int height, int value)
		{
			this.x = width;
			this.y = height;
			this.value = value;
      		setRadius();
		}

		public void setRadius()
		{
  			if      (value > 208 && value <= 255) radius = 10;
  			else if (value > 160 && value <= 208) radius =  8;
  			else if (value > 112 && value <= 160) radius =  6;
  			else if (value >  64 && value <= 112) radius =  4;
  			else if (value >  24 && value <=  64) radius =  2;
  			else                                  radius =  1;
  		}

    	public int getRadius() { return radius; }

    	public int getX()      { return x; }

    	public int getY()      { return y; }
	}
}
