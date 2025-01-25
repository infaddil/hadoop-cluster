# MPI Reducer Code
# Save this as `mean_household_malaysia_reducer_MPI.py`

from mpi4py import MPI
import sys

comm = MPI.COMM_WORLD
rank = comm.Get_rank()
size = comm.Get_size()


def reducer(input_file):
    results = {}
    with open(input_file, 'r') as f:
        for line in f:
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
    input_file = 'mapper_output.txt'

    if rank == 0:
        with open(input_file, 'r') as f:
            lines = f.readlines()

        # Split the lines among all ranks
        chunks = [lines[i::size] for i in range(size)]
    else:
        chunks = None

    # Scatter chunks to all processes
    chunk = comm.scatter(chunks, root=0)

    # Write the chunk to a temporary file
    temp_file = f'temp_reducer_file_{rank}.txt'
    with open(temp_file, 'w') as f:
        f.writelines(chunk)

    # Perform reduction
    partial_results = reducer(temp_file)

    # Gather results at rank 0
    gathered_results = comm.gather(partial_results, root=0)

    if rank == 0:
        final_results = {}
        for result in gathered_results:
            for category, avg in result.items():
                if category in final_results:
                    final_results[category].append(avg)
                else:
                    final_results[category] = [avg]

        # Final averages
        with open('reducer_output.txt', 'w') as f:
            for category, averages in final_results.items():
                overall_avg = sum(averages) / len(averages)
                f.write(f"{category}\t{overall_avg}\n")
