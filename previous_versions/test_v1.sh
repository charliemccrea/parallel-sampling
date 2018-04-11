echo ""
echo "=== TEST 1: Puppy greyscale file ==="
java SequentialSampling puppy.grey puppy.jpg
echo "Puppy file sampled"

echo ""
echo "=== TEST 2: Gradient greyscale file ==="
java SequentialSampling gradient.grey gradient.png
echo "Gradient file sampled"

echo ""
echo "=== TEST 3: Desaturated Bars file ==="
java SequentialSampling desaturatedBars.grey desaturatedBars.png
echo "Desaturated bars file sampled"
echo ""

echo ""
echo "=== TEST 4: Color References file ==="
java SequentialSampling ColorValueRef.grey ColorValueRef.png
echo "color ref file sampled"
echo ""

echo ""
echo "=== TEST 5: Earth Image ==="
java SequentialSampling earth.grey earth.jpg
echo "Earth image file sampled"
echo ""