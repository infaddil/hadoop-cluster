# mean_householf_malaysia_reducer_MPI.py
from mpi4py import MPI
import os

comm = MPI.COMM_WORLD
rank = comm.Get_rank()
size = comm.Get_size()

def reducer(lines):
    results = {}
    for line in lines:
        category, income = line.strip().split("\t")
        income = float(income)
        if category in results:
            results[category].append(income)
        else:
            results[category] = [income]

    # Calculate averages
    averages = {category: sum(incomes) / len(incomes) for category, incomes in results.items()}
    return averages

if __name__ == "__main__":
    input_file = '/input/mapped_output.txt'  # Ensure path is consistent

    if not os.path.exists(input_file):
        if rank == 0:
            print(f"Error: Input file {input_file} does not exist.")
        sys.exit(1)

    if rank == 0:
        # Read all lines from the input file
        with open(input_file, 'r') as f:
            lines = f.readlines()

        # Split lines evenly among all processes
        chunks = [lines[i::size] for i in range(size)]
    else:
        chunks = None

    # Scatter chunks to all processes
    chunk = comm.scatter(chunks, root=0)

    # Perform reduction on the received chunk
    partial_results = reducer(chunk)

    # Gather partial results from all processes at rank 0
    gathered_results = comm.gather(partial_results, root=0)

    if rank == 0:
        # Combine results from all processes
        final_results = {}
        for result in gathered_results:
            for category, incomes in result.items():
                if category in final_results:
                    final_results[category].extend(incomes)
                else:
                    final_results[category] = incomes

        # Calculate overall averages
        with open('/input/reducer_output.txt', 'w') as f:
            for category, incomes in final_results.items():
                overall_avg = sum(incomes) / len(incomes)
                f.write(f"{category}\t{overall_avg}\n")

        print("Reducer operation completed. Output saved to reducer_output.txt.")
