#!/usr/bin/python3
from mpi4py import MPI

def mapper(lines):
    results = []
    for line in lines:
        if line.startswith('Strata'):
            continue
        fields = line.strip().split(',')
        if len(fields) != 4:
            continue
        _, category, _, income = fields
        try:
            income = float(income)
            results.append((category, income))
        except ValueError:
            continue
    return results

comm = MPI.COMM_WORLD
rank = comm.Get_rank()
size = comm.Get_size()

if rank == 0:
    # Master process: Split data and distribute
    with open('/input/input_mean_household_malaysia.csv', 'r') as f:
        data = f.readlines()
    chunks = [data[i::size] for i in range(size)]
else:
    chunks = None

chunk = comm.scatter(chunks, root=0)
mapped_results = mapper(chunk)
gathered_results = comm.gather(mapped_results, root=0)

if rank == 0:
    # Combine results from all workers
    all_results = [item for sublist in gathered_results for item in sublist]
    with open('mapped_output.txt', 'w') as f:
        for key, value in all_results:
            f.write(f"{key}\t{value}\n")
