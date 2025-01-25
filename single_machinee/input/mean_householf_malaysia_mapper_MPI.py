# mean_householf_malaysia_mapper_MPI.py
from mpi4py import MPI

comm = MPI.COMM_WORLD
rank = comm.Get_rank()
size = comm.Get_size()

def process_lines(lines):
    results = []
    for line in lines:
        if line.startswith("Strata"):
            continue
        fields = line.strip().split(",")
        if len(fields) != 4:
            continue
        _, category, _, income = fields
        try:
            income = float(income)
            results.append(f"{category}\t{income}")
        except ValueError:
            continue
    return results

if __name__ == "__main__":
    input_file = "/input/input_mean_household_malaysia.csv"

    if rank == 0:
        # Master reads the file and splits lines
        with open(input_file, "r") as f:
            lines = f.readlines()
        chunks = [lines[i::size] for i in range(size)]
    else:
        chunks = None

    # Scatter chunks to processes
    chunk = comm.scatter(chunks, root=0)

    # Process lines in each process
    local_results = process_lines(chunk)

    # Gather results from all processes
    gathered_results = comm.gather(local_results, root=0)

    if rank == 0:
        # Combine results and write to output
        with open("/input/mapped_output.txt", "w") as f:
            for sublist in gathered_results:
                for result in sublist:
                    f.write(result + "\n")
