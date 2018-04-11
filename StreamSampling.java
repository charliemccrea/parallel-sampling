import java.util.*;
import java.util.stream.*;
import java.util.Random;

public class StreamSampling
{
  public static void main(String[] args)
  {
//    Sampling s = new Sampling();

    int w = 1000;
    int h = 1000;
    Random r = new Random(0);
    List<Point> pts = new ArrayList<>();
    for (int i = 0; i < 99999; i++)
    {
      Point p = new Point(r.nextInt(w), r.nextInt(h));
      pts.add(p);
    }

    pts.parallelStream().forEach(pt -> 
        {
          // isConflict(pt);
          //  Do conflict resolution for every point
          //  regenerate new list of points if ratio of hits/misses isn't hit
        }
    );
  }
}

class Point
{
  private int x;
  private int y;
  private int colorValue;
  private int radius;
  
  public Point(int x, int y)
  {
    this.x = x;
    this.y = y;
  }
}

class SamplingThread
{
}
