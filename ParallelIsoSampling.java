import java.awt.Point;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Random;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.File;

public class ParallelIsoSampling
{
	/* Settings */
  public static int RADIUS = 6;
	public static double RATIO = 0.1;
  public static final String CIRCLE_COLOR = "red";
  public static int THREAD_SIZE = 4;

  /* DEBUG ATTRS */
	public static final int MAX_ITERATIONS = Integer.MAX_VALUE;
  public static int hits = 0;
  public static int misses = 0;
  public static int width = 0;
  public static int height = 0;

	public static void main(String[] args)
	{
		Scanner in;
		String[] dim;
		String dimStr;
		long startTime;
		long endTime;

		try
		{
			in     = new Scanner(new File(args[0])); //For filename input.
			dimStr = in.nextLine();
			System.out.println("WxH = " + dimStr);
			dim    = dimStr.split(" ");
			width  = Integer.parseInt(dim[0]);
			height = Integer.parseInt(dim[1]);
		}
		catch (IOException e)
		{
			System.err.println("Failed to use the given file.");
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
		int numConflicts = 0;
		int itrs = 0;

		startTime = System.currentTimeMillis();
    darts = serialSample(pixels, rand, failedPts, numConflicts, itrs);
		Sampling s = new Sampling();
    
    for (int i = 0; i < THREAD_SIZE; i++) {
      s.start();
    }
    endTime = System.currentTimeMillis();

		try
		{
      outSVG(width,height,darts,args[0], args[1]);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

  public static ArrayList<Point> serialSample(boolean[][] pixels, Random rand, int failedPts, int numConflicts, int itrs) {
    ArrayList<Point> darts = new ArrayList<>();
    while (misses == 0 || (((double) hits/ (double)misses) > RATIO && itrs < MAX_ITERATIONS))
		{
			int x = (int) Math.floor(rand.nextDouble() * width);
			int y = (int) Math.floor(rand.nextDouble() * height);
			Point pt = new Point(x, y);
			if (!darts.contains(pt))
			{
				boolean conflict = false;
				for (int i = -2*RADIUS; i < RADIUS*2 && !conflict; i++)
				{
					for (int j = -2*RADIUS; j < RADIUS*2 && !conflict; j++)
					{
						int a = i + x;
						int b = j + y;
						if (a >= 0 && a < width && b >= 0 && b < height)
						{
							if (pixels[a][b])
							{
								//System.err.println("Conflict!");
								failedPts++;
								conflict = true;
								//numConflicts++;
								//System.out.println("Failed point! " + failedPts);
							}
						}
					}
				}

				if (!conflict)
				{
          hits++;
					darts.add(pt);
					pixels[x][y] = true;
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

  public static boolean validCLI(String[] args) {
		boolean valid = true;
    RADIUS = 5;
    RATIO = 0.25;

    if (args.length != 4)
		{
			System.out.println("java SequentialSampling <grey:file> <image:file> <radius:int> <ratio:double>");
			return false;
		}
    try {
      RADIUS = Integer.parseInt(args[2]);
    } catch(Exception ex) {
      System.err.println("Failed to use given radius, using default 5.");
      valid = false;
    }
    try {
      RATIO = Double.parseDouble(args[3]); 
    } catch(Exception ex) {
      System.err.println("Failed to use given ratio, using default 0.25.");
      valid = false;
    }
    return valid;
  }

	public static void outSVG(int width, int height, ArrayList<Point> pts, String grey, String picture) throws IOException
	{
		// Add a circle tag for each successful point
		// Output as an SVG file.
		StringBuilder str = new StringBuilder();
		str.append(String.format("<html>\n<style>\n\tsvg {\n\t\tbackground-image: url(\"%s\");\n\t}\n</style>", picture));
    	str.append(String.format("<svg width=\"%d\" height=\"%d\" version=\"1.1\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns=\"http://www.w3.org/2000/svg\">\n", width, height));
    	str.append(String.format("<defs>\n<pattern id=\"bg_img\" patternUnits=\"userSpaceOnUse\" width=\"%d\" height=\"%d\">\n<image xlink:href=\"%s\" x=\"0\" y=\"0\" width=\"%d\" height=\"%d\"/>\n</pattern>\n</defs>\n", width, height, grey, width, height));
    	str.append(String.format("<path fill=\"url(#bg_img)\" x=\"0\" y=\"0\" stroke=\"black\" width=\"%d\" height=\"%d\"/>", width, height));

		for (Point p : pts)
		{
			str.append("<circle cx=\"" + p.getX() + "\" cy=\"" + p.getY() +"\" r=\"" + RADIUS + "\" stroke-width=\"1\" fill=\"none\" stroke=\""+CIRCLE_COLOR+"\" />\n");
		}
		str.append("</svg>\n</html>\n");

		BufferedWriter write = new BufferedWriter(new FileWriter(grey + ".html", false));
		write.write(str.toString());
		write.close();
	}

  class Sampling extends Thread 
  {

    public synchronized boolean isConflict(int x, int y, boolean[][] pixels)
    {
      boolean conflict = false;  
      for (int i = -2*RADIUS; i < RADIUS*2 && !conflict; i++)
      {
        for (int j = -2*RADIUS; j < RADIUS*2 && !conflict; j++)
        {
          int a = i + x;
          int b = j + y;
          if (a >= 0 && a < width && b >= 0 && b < height)
          {
            if (pixels[a][b])
            {
              conflict = true;
            }
          }
        }
      }
      this.addPoint(x,y,pixels);
      return conflict;
    }

    public synchronized void addPoint(int x, int y, boolean[][] pixels)
    {
      pixels[x][y] = true;
    }

    public ArrayList<Point> sample(boolean[][] pixels, Random rand, int failedPts, int numConflicts, int itrs) {
      int hits = 0, misses = 0;
      ArrayList<Point> darts = new ArrayList<>();
      while (misses == 0 || (((double) hits/ (double)misses) > RATIO && itrs < MAX_ITERATIONS))
      {
        int x = (int) Math.floor(rand.nextDouble() * width);
        int y = (int) Math.floor(rand.nextDouble() * height);
        Point pt = new Point(x, y);
        if (!darts.contains(pt))
        {
          boolean conflict = isConflict(x,y,pixels);
          if (!conflict) hits++; else misses++;
        }
        itrs++;
      }

      return darts;
    }
  }
}
