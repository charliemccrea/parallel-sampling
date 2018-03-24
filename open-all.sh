#!/bin/bash

echo "Opening in default browser, will run in background!"
find . -maxdepth 1 -type f -name "*.grey.html" -exec x-www-browser {} \;
