#!/bin/bash

#SBATCH -J anisotropic
#SBATCH -o tests.txt

make
echo "Earth File Tests"
echo "=== 1 Thread ==="
./p_anisotropic tests/earth.grey 0.10 1
echo "=== 2 Threads ==="
./p_anisotropic tests/earth.grey 0.10 2
echo "=== 4 Threads ==="
./p_anisotropic tests/earth.grey 0.10 4
echo "=== 8 Threads ==="
./p_anisotropic tests/earth.grey 0.10 8
echo "=== 16 Threads ==="
./p_anisotropic tests/earth.grey 0.10 16
echo "Weak Scaling"
echo "=== 0.25 ==="
./p_anisotropic tests/earth.grey 0.25 16
echo "=== 0.20 ==="
./p_anisotropic tests/earth.grey 0.20 16
echo "=== 0.15 ==="
./p_anisotropic tests/earth.grey 0.15 16
echo "=== 0.10 ==="
./p_anisotropic tests/earth.grey 0.10 16
