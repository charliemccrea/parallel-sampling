#include <stdio.h>
#include <stdlib.h>
#include <stdint.h>
#include <pthread.h>

# define TIMING
//# define INFO

# ifdef TIMING
#include <time.h>
# endif

typedef struct {
  long x;
  long y;
  long color_value;
  int radius;
} dart_t;

// Arguments: binary, grey file, ratio, num_threads
#define ARGS 4
// When an invalid thread count is specified, will operate serially.
#define SERIAL 1

/*
 * To access the shared pixels array and the hit and miss variables, must use theses locks.
 */
pthread_mutex_t pixel_lock = PTHREAD_MUTEX_INITIALIZER;
pthread_mutex_t hit_miss_lock = PTHREAD_MUTEX_INITIALIZER;

long **pixels; //Contains value representing a occupied space or unused space on the image by darts being thrown
long **color_values; //Contains the color value for that pixel space

long img_width;
long img_height;
long array_size;
float exit_ratio;
long hits;
long misses;

int
parse_command_line(int argc, char **argv, FILE **grey_file, float *ratio, int *nthreads)
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
  
  *nthreads = atoi(argv[3]);
  if (*nthreads <= 0) {
    *nthreads = SERIAL;
    fprintf(stderr, "Reseting nthreads to %d, given invalid input.\n", *nthreads); 
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
  pthread_mutex_lock(&hit_miss_lock);
  if (!occupied)
  {
    hits++;
  }
  else
  {
    misses++;
  }
  pthread_mutex_unlock(&hit_miss_lock);
}

void *sample(void *p)
{
  do
  {
    dart_t d;
    d.x = rand() % img_width;
    d.y = rand() % img_height;
    d.color_value = color_values[d.y][d.x];
    d.radius = radius_size(d.color_value);
    
    int occupied = 0;

    // (need lock)
    pthread_mutex_lock(&pixel_lock);
    // Determine if point and, within its radius, is occupied
    long i;
    long j;
    for (i = -1*d.radius; i < d.radius && !occupied; i++)
    {
      for (j = -1*d.radius; j < d.radius && !occupied; j++)
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
    //if unoccupied, maintain lock and write new placement of dart to pixel with radius filled.
    if (!occupied)
    {
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
    }     
    pthread_mutex_unlock(&pixel_lock);  
    update_ratio(occupied);
  }
  while(misses == 0 || (double)hits/misses > exit_ratio);
  return NULL;
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
  int nthreads;

  if (parse_command_line(argc, argv, &grey_file, &exit_ratio, &nthreads)) 
  {
    printf("Exited on failed command line parsing.\n");
    return 1;
  }
  pthread_t threads[nthreads];

  if (fscanf(grey_file, "%ld %ld\n", &img_width, &img_height) < 2)
  {
    fprintf(stderr, "Failed to read from given .grey file the dimensions of the image!\n");
    return 1;
  }
  array_size = img_width * img_height;
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
  printf("Multithreaded Anisotropic Sampling\n\t\tStarted with %d threads, will end at %.4f hits/misses.\n\t\tUsing grey file: %s (%ld, %ld)\n", nthreads, exit_ratio, argv[1], img_width, img_height);
# endif 
# ifdef TIMING
  time_t start = time(NULL);
# endif
  time_t t;
  srand((unsigned) time(&t));
  hits = 0;
  misses = 0;
  for (i = 0; i < nthreads; i++)
  {
    pthread_create(&threads[i], NULL, sample, NULL);    
  }  
  for (i = 0; i < nthreads; i++)
  {
    pthread_join(threads[i], NULL);
  }
# ifdef TIMING
  time_t end = time(NULL);
  printf("Sampling took %0.4f seconds\n", (double)(end - start));
# endif
  
  printf("Hits: %ld\nMisses: %ld\n", hits, misses);
  return 0;
}
