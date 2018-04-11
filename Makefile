all:
	javac SequentialSampling.java ParallelIsoSampling.java
clean:
	rm -f ParallelIsoSampling.class SequentialSampling.class tests/*.grey.html
