import java.util.*;
import java.util.stream.*;

public class StreamSampling
{
  public static void main(String[] args)
  {
    int w = 1000;
    int h = 1000;
    Random r = new Random(0);
    List<Point> pts = new ArrayList<>();
    for (int i = 0; i < 99999; i++)
    {
      Point p = new Point((int)Math.floor(r.rand(w)), (int)Math.floor(r.rand(h)));
      pts.add(p);
    }
    pts.parallelStream().forEach(p -> 

  }

  

}

public class SamplingThread
{
}
