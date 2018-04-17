#include <stdio.h>
#include <stdlib.h>
#include <stdint.h>

# define TIMING
# define INFO

# ifdef TIMING
#include <time.h>
# endif
# ifdef _OPENMP
#include <omp.h>
# endif

typedef struct {
  long x;
  long y;
  long color_value;
  int radius;
} dart_t;

// Arguments: binary, grey file, ratio
#define ARGS 3
// When an invalid thread count is specified, will operate serially.
#define SERIAL 1

long **pixels; //Contains value representing a occupied space or unused space on the image by darts being thrown
long **color_values; //Contains the color value for that pixel space

long img_width;
long img_height;
float exit_ratio;
long hits;
long misses;

int
parse_command_line(int argc, char **argv, FILE **grey_file, float *ratio)
{
  if (argc != ARGS)
  {
    fprintf(stderr, "The input must contain the arguments:\n\t./parallel_sampling <.grey> <ratio> <num-threads>\nWhere the .grey is the relative filename to the converted image to text\nAnd ratio is a value between 0.01 and 0.99 to determine when to stop sampling the image for the hits/misses ratio.\n");
    return 1;
  }
  
  *grey_file = fopen(argv[1], "r");
  if (*grey_file == NULL)
  {
    fprintf(stderr, "Could not open file %s\n", argv[1]);
    return 1;
  }
  
  if (sscanf(argv[2], "%f", ratio) < 1)
  {
    fprintf(stderr, "Could not get ratio from input %s\n", argv[1]);
    return 1;
  }
  
  return 0;
}

long
radius_size(long color_value)
{
  long radius = 0;
  if (color_value > 208)
    radius = 10;
  else if (color_value > 160)
    radius = 8;
  else if (color_value > 112)
    radius = 6;
  else if (color_value > 64)
    radius = 4;
  else if (color_value > 24)
    radius = 2;
  else
    radius = 1;
  return radius;  
}

void
update_ratio(int occupied)
{
  if (!occupied)
  {
    hits++;
  }
  else
  {
    misses++;
  }
}

void sample()
{
    int occupied = 0;
    printf("OMP Spawning threads\n");
  do
  {
    //# pragma omp parallel default(none) shared(occupied,hits,misses,color_values,img_width,img_height,exit_ratio,pixels) 
    //{
        dart_t d;
        d.x = rand() % img_width;
        d.y = rand() % img_height;
        d.color_value = color_values[d.y][d.x];
        d.radius = radius_size(d.color_value);
        
        occupied = 0;

        long i;
        long j;
        # pragma omp parallel for collapse(2)
        for (i = -1*d.radius; i < d.radius; i++)
        {
          for (j = -1*d.radius; j < d.radius; j++)
          {
            long a = i + d.x;
            long b = j + d.y;
            if (a >= 0 && a < img_width && b >= 0 && b < img_height)
            {
              if (pixels[b][a])
              {
                occupied = 1;
              }
            }
          }
        }
        # pragma omp barrier
        //printf("Occupied: %d\n", occupied);
        //if unoccupied, maintain lock and write new placement of dart to pixel with radius filled.
        if (!occupied)
        {
          # pragma omp parallel for collapse(2)
          for (i = -1 * d.radius; i < d.radius; i++)
          {
            for (j = -1 * d.radius; j < d.radius; j++)
            {
              long a = i + d.x;
              long b = j + d.y;
              if (a >= 0 && a < img_width && b >= 0 && b < img_height)
              {
                pixels[b][a] = 1;
              }
            }
          }
          //printf("New Point: %ld %ld\n", d.x, d.y);
        }     
        # pragma omp critical
        {
            update_ratio(occupied);
        }
        # pragma omp barrier
    //}
    //printf("Ratio: %.4f, hits: %ld, misses: %ld\n", (double) hits/misses, hits, misses);
  }
  while(misses == 0 || (double)hits/misses > exit_ratio);
}

int
setup_arrays()
{
  color_values = calloc(img_height, sizeof(long));
  pixels = calloc(img_height, sizeof(long));
  long i;
  for (i = 0; i < img_height; i++)
  {
    color_values[i] = calloc(img_width, sizeof(long));
    pixels[i] = calloc(img_width, sizeof(long));
    if (color_values[i] == NULL)
    { 
      printf("Color values failed allocation\n");
      return 1;
    }
    if (pixels[i] == NULL)
    {
      printf("Pixels failed allocation\n");
      return 1;
    }
  }
  return 0;
}

int
main(int argc, char **argv)
{
  FILE* grey_file;

  if (parse_command_line(argc, argv, &grey_file, &exit_ratio)) 
  {
    printf("Exited on failed command line parsing.\n");
    return 1;
  }

  if (fscanf(grey_file, "%ld %ld\n", &img_width, &img_height) < 2)
  {
    fprintf(stderr, "Failed to read from given .grey file the dimensions of the image!\n");
    return 1;
  }
  if (setup_arrays())
  {
    return 1;
  }
  if (color_values == NULL)
  {
    fprintf(stderr, "Could not allocate memory for the image's color values!\n");
    return 1;
  }
  if (pixels == NULL)
  {
    fprintf(stderr,"Cannot allocate space for the pixel values.\n");
    return 1;
  }
# ifdef INFO
  printf("Reading in color values...\n");
# endif
  long i;
  long j;
  long value;
  for (i = 0; i < img_height; i++)
  {
    for (j = 0; j < img_width; j++)
    {
      if (fscanf(grey_file, "%ld\n", &value) == 1)
      {
        color_values[i][j] = value;
      }
    }
  }  
# ifdef INFO
  printf("Multithreaded Anisotropic Sampling\n\t\tStarted with %d threads, will end at %.4f hits/misses.\n\t\tUsing grey file: %s (%ld, %ld)\n", omp_get_max_threads(), exit_ratio, argv[1], img_width, img_height);
# endif 
# ifdef TIMING
  time_t start = time(NULL);
# endif
  time_t t;
  srand((unsigned) time(&t));
  hits = 0;
  misses = 0;

  sample();
# ifdef TIMING
  time_t end = time(NULL);
  printf("Sampling took %0.2f seconds\n", (double)(end - start));
# endif
  
  printf("Hits: %ld\nMisses: %ld\n", hits, misses);
  return 0;
}
