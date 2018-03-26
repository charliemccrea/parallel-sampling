make
echo "=== TEST 1: Earth Image (Ratio 0.25) ==="
java SequentialSampling tests/earth.grey earth.jpg 10 0.25
echo ""
echo "=== TEST 2: Earth Image (Ratio 0.20) ==="
java SequentialSampling tests/earth.grey earth.jpg 10 0.20
echo ""
echo "=== TEST 3: Earth Image (Ratio 0.15) ==="
java SequentialSampling tests/earth.grey earth.jpg 10 0.15
echo ""
echo "=== TEST 4: Earth Image (Ratio 0.10) ==="
java SequentialSampling tests/earth.grey earth.jpg 10 0.10
echo ""
echo "=== TEST 5: Earth Image (Ratio 0.05) ==="
java SequentialSampling tests/earth.grey earth.jpg 10 0.05
echo ""
echo "=== TEST 6: Earth Image (Ratio 0.025) ==="
java SequentialSampling tests/earth.grey earth.jpg 10 0.025
echo ""
echo "=== TEST 7: Earth Image (Ratio 0.00125) ==="
java SequentialSampling tests/earth.grey earth.jpg 10 0.00125
