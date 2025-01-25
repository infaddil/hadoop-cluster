# mean_householf_malaysia_reducer_MPI.py
from mpi4py import MPI

comm = MPI.COMM_WORLD
rank = comm.Get_rank()
size = comm.Get_size()

def calculate_averages(lines):
    results = {}
    for line in lines:
        category, income = line.strip().split("\t")
        income = float(income)
        if category in results:
            results[category].append(income)
        else:
            results[category] = [income]
    averages = {category: sum(values) / len(values) for category, values in results.items()}
    return averages

if __name__ == "__main__":
    input_file = "/input/mapped_output.txt"

    if rank == 0:
        # Master reads the file and splits lines
        try:
            with open(input_file, "r") as f:
                lines = f.readlines()
            chunks = [lines[i::size] for i in range(size)]
        except FileNotFoundError:
            print(f"Error: Input file {input_file} does not exist.")
            comm.Abort()
    else:
        chunks = None

    # Scatter chunks to processes
    chunk = comm.scatter(chunks, root=0)

    # Calculate averages for each process's chunk
    partial_averages = calculate_averages(chunk)

    # Gather results at root process
    gathered_averages = comm.gather(partial_averages, root=0)

    if rank == 0:
        # Combine all averages
        final_results = {}
        for partial in gathered_averages:
            for category, avg in partial.items():
                if category in final_results:
                    final_results[category].append(avg)
                else:
                    final_results[category] = [avg]

        # Write the final averages to output
        with open("/input/reducer_output.txt", "w") as f:
            for category, averages in final_results.items():
                overall_avg = sum(averages) / len(averages)
                f.write(f"{category}\t{overall_avg}\n")
