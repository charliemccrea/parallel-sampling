make
echo "=== TEST 1: Puppy greyscale file ==="
java ParallelIsoSampling tests/puppy.grey puppy.jpg 6 0.05
echo "Puppy file sampled"
echo ""
echo "=== TEST 2: Grey Bars file ==="
java ParallelIsoSampling tests/bars.grey bars.jpg 6 0.05
echo ""
echo "=== TEST 3: Earth Image ==="
java ParallelIsoSampling tests/earth.grey earth.jpg 10 0.25
echo "Sampled at a ratio of 0.25"
