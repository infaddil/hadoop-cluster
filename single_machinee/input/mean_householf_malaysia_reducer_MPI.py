from mpi4py import MPI
import os

comm = MPI.COMM_WORLD
rank = comm.Get_rank()
size = comm.Get_size()

# Ensure proper input path
input_file = "/input/mapped_output.txt"

if not os.path.exists(input_file):
    if rank == 0:
        print(f"Error: Input file {input_file} does not exist.")
    # Exit gracefully
    MPI.Finalize()
    exit(1)


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
    # Read the file at rank 0
    if rank == 0:
        with open(input_file, "r") as f:
            lines = f.readlines()

        # Scatter chunks of lines
        chunks = [lines[i::size] for i in range(size)]
    else:
        chunks = None

    chunk = comm.scatter(chunks, root=0)

    # Process the chunk using the reducer function
    partial_results = reducer(chunk)

    # Gather partial results at rank 0
    gathered_results = comm.gather(partial_results, root=0)

    if rank == 0:
    # Combine results from all workers
    all_results = [item for sublist in gathered_results for item in sublist]
    with open('/input/mapped_output.txt', 'w') as f:
        for key, value in all_results:
            f.write(f"{key}\t{value}\n")

        # Compute overall averages
        with open("/input/reducer_output.txt", "w") as f:
            for category, averages in final_results.items():
                overall_avg = sum(averages) / len(averages)
                f.write(f"{category}\t{overall_avg}\n")
        print("Reducer completed. Output saved to /input/reducer_output.txt")
