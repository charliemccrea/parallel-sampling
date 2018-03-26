#!/bin/bash
find tests/ -maxdepth 1 -type f -name "*.grey.html" -exec bash -c 'x-www-browser {} &' \; 2>/dev/null
