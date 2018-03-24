#!/bin/bash
find . -maxdepth 1 -type f -name "*.grey.html" -exec x-www-browser {} \; 2>/dev/null
