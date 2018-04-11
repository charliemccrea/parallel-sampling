all:
	javac SequentialSampling.java && gcc -o serial_sampling serial_sampling.c
clean:
	rm -f SequentialSampling.class tests/*.grey.html
