#!/usr/bin/python3
from mpi4py import MPI

def mapper(lines):
    # Perform the mapping task
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

# Initialize MPI
comm = MPI.COMM_WORLD
rank = comm.Get_rank()
size = comm.Get_size()

if rank == 0:
    # Master process: Load and distribute the data
    input_file = '/input/input_mean_household_malaysia.csv'  # Ensure path is correct
    with open(input_file, 'r') as f:
        data = f.readlines()
    chunks = [data[i::size] for i in range(size)]  # Distribute data across processes
else:
    chunks = None

# Scatter chunks of data to workers
chunk = comm.scatter(chunks, root=0)

# Map phase: Each worker processes its chunk
mapped_results = mapper(chunk)

# Gather the mapped results at the master
gathered_results = comm.gather(mapped_results, root=0)

if rank == 0:
    # Master process: Combine and save results
    all_results = [item for sublist in gathered_results for item in sublist]
    with open('mapped_output.txt', 'w') as f:
        for key, value in all_results:
            f.write(f"{key}\t{value}\n")
