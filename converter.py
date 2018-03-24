# Jason Zareski
# Converts a greyscale image format with the Python3 ImageIO lib to a text
# based representation of the pixels of the image with the encode option.
# This converted file can be used with our serial and 
# parallel sampling programs.  The output file from the encode option 
# can be decoded by this same script with the -d option.

from PIL import Image
import sys
import getopt

#Print usage for this script's options
def usage():
    print("Usage: python <script> -<i/t> <filename>\ntext->png -i\npng->text -t");
    return;

#Take an image and convert to text-based representation.
def toText(filename):
    image = Image.open(filename)
    output = open(filename.split(".")[0] + ".grey", 'w')
    pixels = image.load()
    width, height = image.size
    output.write("%d %d\n" % (width, height))
    for x in range(width):
        for y in range(height):
            s = ""
            if type(pixels[x,y]) is list:
                s = str(pixels[x,y][0])
            elif type(pixels[x,y]) is int:
                s = str(pixels[x,y])
            output.write(s + "\n")
    return;

#Take text-based representation and convert to image format.
def toImage(filename):
    infile = open(filename, 'r')
    width, height = map(int, infile.readline().split(" "))
    image = Image.new('L', (width, height))
    for x in range(width):
        for y in range(height):
            pix = int(infile.readline())
            image.putpixel((x,y), pix)
    image.save(filename.split(".")[0] + ".converted.png")
    return;

if len(sys.argv) < 1:
    usage();
try:
    opts,args = getopt.getopt(sys.argv[1:], "t:i:u", 
            ["decode", "encode", "usage"])
except getopt.GetoptError as err:
    print(str(err))
    sys.exit(2)
for opt, arg in opts:
    if opt == "-t" or opt == "--text":
        #Take a converted image as text to image file
        toText(arg);
    elif opt == "-i" or opt == "--image":
        #Take a image file and convert to text representation
        toImage(arg);
    elif opt == "-u" or opt == "--usage":
        usage();
