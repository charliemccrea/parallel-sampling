TARGETS=clean parallel_anisotropic omp_anisotropic serial

all: $(TARGETS)

serial:
	javac SequentialSampling.java
parallel_anisotropic:
	gcc -pthread -o p_anisotropic parallel_anisotropic.c
omp_anisotropic:
	gcc -fopenmp -o omp_anisotropic omp_anisotropic.c
clean:
	rm -f p_anisotropic omp_anisotropic *.class
