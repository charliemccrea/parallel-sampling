TARGETS=clean parallel_anisotropic locked_col_anisotropic omp_anisotropic java_anisotropic serial 
CC=gcc -pthread -Wall -o

all: $(TARGETS)

serial:
	javac SequentialSampling.java
java_anisotropic:
	javac AnisotropicSerial.java
parallel_anisotropic:
	$(CC) p_anisotropic parallel_anisotropic.c
locked_col_anisotropic:
	$(CC) col_threads column_threads.c 
omp_anisotropic:
	gcc -fopenmp -o omp_anisotropic omp_anisotropic.c
clean:
	rm -f p_anisotropic col_threads omp_anisotropic *.class
