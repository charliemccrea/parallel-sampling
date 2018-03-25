# parallel-sampling

Use the python converter script to change a image file to text and a text file back to an image based on pixel values.

Usage: python3 converter.py option filename
  * -t Image to text
  * -i Text to image

  This requires Pillow import, make sure pip is updated. For Linux & MacOS:
  '$pip3 install pip' then '$pip3 install Pillow'. And image must be converted
  to a .gray file before it may be used in the java program with the original file.
