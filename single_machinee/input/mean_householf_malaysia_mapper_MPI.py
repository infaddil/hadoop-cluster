# MPI Mapper Code
# Save this as `mean_household_malaysia_mapper_MPI.py`

from mpi4py import MPI
import csv

comm = MPI.COMM_WORLD
rank = comm.Get_rank()
size = comm.Get_size()

def mapper(input_file):
    results = {}
    with open(input_file, 'r') as f:
        reader = csv.DictReader(f)
        for row in reader:
            category = row['Category']
            income = float(row['Mean Monthly Household Gross Income'])

            if category in results:
                results[category].append(income)
            else:
                results[category] = [income]
    return results

if __name__ == "__main__":
    input_file = 'input_mean_household_malaysia.csv'

    # Each process gets part of the file to process
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
    temp_file = f'temp_file_{rank}.csv'
    with open(temp_file, 'w') as f:
        f.writelines(chunk)

    # Perform mapping
    partial_results = mapper(temp_file)

    # Gather results at rank 0
    gathered_results = comm.gather(partial_results, root=0)

    if rank == 0:
        final_results = {}
        for result in gathered_results:
            for category, incomes in result.items():
                if category in final_results:
                    final_results[category].extend(incomes)
                else:
                    final_results[category] = incomes

        # Save intermediate results for the reducer
        with open('mapper_output.txt', 'w') as f:
            for category, incomes in final_results.items():
                for income in incomes:
                    f.write(f"{category}\t{income}\n")
