make parallel_anisotropic
srun ./p_anisotropic tests/earth.grey 1 > 001.txt &
srun ./p_anisotropic tests/earth.grey 2 > 002.txt &
srun ./p_anisotropic tests/earth.grey 4 > 004.txt &
srun ./p_anisotropic tests/earth.grey 8 > 008.txt &
srun ./p_anisotropic tests/earth.grey 16 > 016.txt &
srun ./p_anisotropic tests/earth.grey 32 > 032.txt &
srun ./p_anisotropic tests/earth.grey 64 > 064.txt &
srun ./p_anisotropic tests/earth.grey 128 > 128.txt &
