#!/bin/bash
#
# SBATCH --job-name=omp_anisotropic
# SBATCH --output=output_%a.txt
# SBATCH --array=1,2,4,8,16

echo "Thread Count: $SLURM_ARRAY_TASK_ID"
echo "Puppy"
OMP_NUM_THREADS=$SLURM_ARRAY_TASK_ID ./omp_anisotropic tests/puppy.grey 0.10
echo "Earth"
OMP_NUM_THREADS=$SLURM_ARRAY_TASK_ID ./omp_anisotropic tests/earth.grey 0.25
