#!/bin/bash
#SBATCH --output=seperate_columns_results.out

for x in 1 2 4 8 16 32 64 128; do 
    ./col_threads tests/earth.grey $x;
done
