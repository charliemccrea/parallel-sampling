#!/bin/bash
# SBATCH -j omp-anisotropic
# SBATCH --output=output_%a.txt
# SBATCH --array=1,2,4,8,16

OMP_NUM_THREADS=$SLURM_ARRAY_TASK_ID
./omp_anisotropic tests/earth.grey 0.25
