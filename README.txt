RESEARCH PROJECT: PARALLELIZING ANISOTROPIC SAMPLING

AUTHORS: Jason Zareski, Charlie McCrea, Georgia Corey, Sam Kiwus


(1) DESCRIPTION

This project's goal was to create a parallel implementation of anisotropic sampling. Anisotropic sampling is used
extensively in computer graphics, and produces an anisotropically-filtered (AF) image. Our task was to make this process
more efficient by dividing up the work between multiple threads.


(2) CONTENTS

C IMPLEMENTATIONS
column_threads.c
omp_anisotropic.c
parallel_anisotropic.c
serial_threaded.c

JAVA IMPLEMENTATIONS
AnisotropicSerial.java
ParallelIsoSampling.java
SequentialSampling.java

PYTHON IMAGE CONVERTER
converter.py

TEST SCRIPTS
array_test.sh
omp_test.sh
open-all.sh
run.sh
run_col_threads.sh
stress_test.sh

OTHER
Makefile
README.txt


(3) HOW TO USE

Use the python converter script to change a image file to text and a text file back to an image based on pixel values.

Usage: python3 converter.py option filename
  * -t Image to text
  * -i Text to image

The next step is compiling the Java program SequentialSampling with the makefile. Simply type 'make' into the terminal.

Once you have a file in .grey and .png format, you may use the Java SequentialSampling program.
'java SequentialSampling puppy.grey puppy.jpg' runs it on a single source image.

You can also run the testing script ./run.sh to run multiple tests at once.

The .grey.html images can be viewed in any web browser that supports svg.

Other .png/.jpg files are the data set that we used for testing. The ColorValueRef was used for
checking the results of the python script and will be useful in the anisotropic version.

The python program requires Pillow import, make sure pip is updated. For Linux & MacOS:
'pip3 install pip' then 'pip3 install Pillow'. An image must be converted
to a .grey file before it may be used in the java program with the original file.

SequentialSampling takes in the .grey and the .jpg/.png file in that order. It outputs a .grey.html file that can be
opened in a web browser to show the distribution of the sampling darts. Darts and their radii are drawn as red circles
on the source image.
