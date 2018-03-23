echo "Running with puppy greyscale text file."
java SequentialSampling puppy.grey puppy.jpg
echo "Puppy file sampled."

echo "Running with gradient greyscale text file."
java SequentialSampling gradient.grey gradient.png
echo "Gradient file sampled."

echo "Running with color ref text file."
java SequentialSampling ColorValueRef.grey ColorValueRef.png
echo "color ref file sampled."
