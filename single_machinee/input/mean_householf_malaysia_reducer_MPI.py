#!/usr/bin/python3
from mpi4py import MPI
from collections import defaultdict

def reducer(mapped_data):
    sums = defaultdict(float)
    counts = defaultdict(int)
    for category, income in mapped_data:
        sums[category] += income
        counts[category] += 1
    return {category: sums[category] / counts[category] for category in sums}

comm = MPI.COMM_WORLD
rank = comm.Get_rank()
size = comm.Get_size()

if rank == 0:
    # Master reads and distributes mapped data
    with open('mapped_output.txt', 'r') as f:
        data = [line.strip().split('\t') for line in f]
        mapped_data = [(key, float(value)) for key, value in data]
    chunks = [mapped_data[i::size] for i in range(size)]
else:
    chunks = None

chunk = comm.scatter(chunks, root=0)
reduced_result = reducer(chunk)
gathered_results = comm.gather(reduced_result, root=0)

if rank == 0:
    # Combine results from all workers
    final_results = defaultdict(float)
    counts = defaultdict(int)
    for partial_result in gathered_results:
        for key, value in partial_result.items():
            final_results[key] += value
            counts[key] += 1
    
    # Print the results to the terminal
    for key in final_results:
        avg = final_results[key] / counts[key]
        print(f"{key}\t{avg}")
