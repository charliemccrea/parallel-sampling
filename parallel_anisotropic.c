#include <stdio.h>
#include <stdlib.h>
#include <stdint.h>
#include <pthread.h>

# define TIMING
//# define DEBUG
# define INFO

# ifdef TIMING
#include <time.h>
# endif

typedef struct {
  long x;
  long y;
  long color_value;
  int radius;
} dart_t;

// Arguments: binary, grey file, /*ratio,*/ num_threads
#define ARGS 3
// When an invalid thread count is specified, will operate serially.
#define SERIAL 1
// Change this if the function to determine the radius increases.
#define COL_WIDTH 100
// Max consecutive misses
//#define MAX_MISSES_PER_THREAD 250
int MAX_MISSES_PER_THREAD;
/*
 * To access the shared pixels array and the hit and miss variables, must use theses locks.
 */
pthread_mutex_t count_lock = PTHREAD_MUTEX_INITIALIZER;

/*
 * Replace above mutexes with array of mutexes corresponding to row/column.
 *
 * Column:
 *  - 10px wide? (img_width / COL_WIDTH = columns)
 *  - dart placement: (d.x / COL_WIDTH = column number?)
 *  - check if the radius of a dart crosses over to the next column, then lock both
 *    - The key change here is that only 3 columns max can be locked by a single dart, more parallel placements
 */
pthread_mutex_t* cols;


long **pixels; //Contains value representing a occupied space or unused space on the image by darts being thrown
long **color_values; //Contains the color value for that pixel space
int nthreads;
long img_width;
long img_height;
long num_cols;
//long misses;

int
cli(int argc, char **argv, FILE **grey_file, int *nthreads)
{
  if (argc != ARGS)
  {
    fprintf(stderr, "The input must contain the arguments: <.grey> <num-threads>\nWhere the .grey is the relative filename to the converted image to text\n");
    return 1;
  }
  
  *grey_file = fopen(argv[1], "r");
  if (*grey_file == NULL)
  {
    fprintf(stderr, "Could not open file %s\n", argv[1]);
    return 1;
  }
  
  *nthreads = atoi(argv[2]);
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

/**
 * Locks appropriate columns for point insertion
 */
int lock_cols(long before, long after, long col)
{
  int ret = 0;
  if (before < col && before >= 0)
  {
    ret = pthread_mutex_trylock(&cols[before]);
  }
  if (ret != 0)
    return -1;

  if (after > col && col <= num_cols)
  {
    ret = pthread_mutex_trylock(&cols[after]);
  }
  if (ret != 0)
  {
    pthread_mutex_unlock(&cols[before]);
    return -1;
  }
  ret = pthread_mutex_trylock(&cols[col]);
  if (ret != 0) 
    return -1;
  else
    return 0;
}

/**
 * Unlocks appropriate columns for point insertion
 */
void unlock_cols(long before, long after, long col)
{
  if (before < col && before >= 0)
  {
    pthread_mutex_unlock(&cols[before]);
  }
  if (after > col && col <= num_cols)
  {
    pthread_mutex_unlock(&cols[after]);
  }
  pthread_mutex_unlock(&cols[col]);
//  printf("Unlocked %ld, %ld, %ld\n", before, col, after);
}

void *sample(void *p)
{
  long i, j, a, b;
  long misses;
  int occupied, col, before, after;
  
  misses = 0;
  
  do
  {
    dart_t d;
    d.x = rand() % img_width;
    d.y = rand() % img_height;
    d.color_value = color_values[d.y][d.x];
    d.radius = radius_size(d.color_value);
    occupied = 0;

    // Find which columns could be effected by this point placement.
    col = d.x / COL_WIDTH;
    before = (d.x - d.radius) / COL_WIDTH;
    after = (d.x + d.radius) / COL_WIDTH;

    // Locks columns if able to, if unable, will skip and find another point to place
    if (lock_cols(before, after, col) == 0)
    {
#     ifdef DEBUG
      if (misses > (MAX_MISSES_PER_THREAD/2)) printf("[DEBUG] CONSECUTIVE MISSES: %ld\n", misses);
#     endif
      // Determine if point and, within its radius, is occupied
      for (i = -1*d.radius; i < d.radius && !occupied; i++)
      {
        for (j = -1*d.radius; j < d.radius && !occupied; j++)
        {
          a = i + d.x;
          b = j + d.y;
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
            a = i + d.x;
            b = j + d.y;
            if (a >= 0 && a < img_width && b >= 0 && b < img_height)
            {
              pixels[b][a] = 1;
            }
          }
        }
      }
      unlock_cols(before, after, col);    
      //pthread_mutex_lock(&count_lock);
      if (occupied)
      {
        misses++;
      }
      else
      {
        misses = 0;
      }
      //pthread_mutex_unlock(&count_lock);
    }
  }
  while(misses < MAX_MISSES_PER_THREAD);
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
  long i;
  long j;
  long value;

  if (cli(argc, argv, &grey_file, /*&exit_ratio,*/ &nthreads)) 
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
  
  num_cols = (img_width / COL_WIDTH);
  num_cols += img_width % COL_WIDTH > 0 ? 1 : 0;
  cols = malloc(sizeof(pthread_mutex_t) * num_cols);
  for (i = 0; i < num_cols; i++)
  {
    pthread_mutex_init(&cols[i], NULL);
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

  MAX_MISSES_PER_THREAD = 15000/nthreads;
# ifdef INFO
  printf("Multithreaded Anisotropic Sampling\n\t\tStarted with %d threads, will end at %d consecutive misses.\n\t\tUsing grey file: %s (%ld, %ld) with %d columns\n", nthreads, MAX_MISSES_PER_THREAD, argv[1], img_width, img_height, num_cols);
# endif 
# ifdef TIMING
  time_t start = time(NULL);
# endif
  time_t t;
  srand((unsigned) time(&t));  

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
  printf("Sampling took %d seconds\n", end - start);
# endif
  for (i = 0; i < img_height; i++)
  {
    free(color_values[i]);
    free(pixels[i]);
  }

  free(color_values);
  free(pixels);
  free(cols);
  return 0;
}
