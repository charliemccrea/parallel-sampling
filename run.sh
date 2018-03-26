echo ""
echo "=== TEST 1: Puppy greyscale file ==="
java SequentialSampling tests/puppy.grey puppy.jpg 6 0.05
echo "Puppy file sampled"

echo ""
echo "=== TEST 2: Gradient greyscale file ==="
java SequentialSampling tests/gradient.grey gradient.png 6 0.05
echo "Gradient file sampled"

echo ""
echo "=== TEST 3: Desaturated Bars file ==="
java SequentialSampling tests/desaturatedBars.grey desaturatedBars.png 6 0.05
echo "Desaturated bars file sampled"
echo ""

echo ""
echo "=== TEST 4: Color References file ==="
java SequentialSampling tests/ColorValueRef.grey ColorValueRef.png 6 0.05
echo "color ref file sampled"
echo ""

echo ""
echo "=== TEST 5: Earth Image ==="
java SequentialSampling tests/earth.grey earth.jpg 10 0.25
echo "Earth image file sampled"
echo ""
