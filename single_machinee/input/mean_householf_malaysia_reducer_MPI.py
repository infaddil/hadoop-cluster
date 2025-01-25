# mean_householf_malaysia_reducer_MPI.py
from mpi4py import MPI

comm = MPI.COMM_WORLD
rank = comm.Get_rank()
size = comm.Get_size()

def reducer(lines):
    results = {}
    for line in lines:
        key, value = line.strip().split("\t")
        value = float(value)
        if key in results:
            results[key] += value
        else:
            results[key] = value
    return results

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

    # Perform reduction for each process's chunk
    partial_results = reducer(chunk)

    # Gather results at rank 0
    gathered_results = comm.gather(partial_results, root=0)

    if rank == 0:
        # Combine all results
        final_results = {}
        for partial in gathered_results:
            for key, value in partial.items():
                if key in final_results:
                    final_results[key] += value
                else:
                    final_results[key] = value

        # Write the final output to a file
        with open("/input/reducer_output.txt", "w") as f:
            for key, value in final_results.items():
                f.write(f"{key}\t{value}\n")
        # Display the results on the terminal
        print("\nReducer Output:")
        for key, value in final_results.items():
            print(f"{key}\t{value}")
