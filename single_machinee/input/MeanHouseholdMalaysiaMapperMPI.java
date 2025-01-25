/*
 * MeanHouseholdMalaysiaMapperMPI.java
 *
 * A contrived example of using MPI in a "Mapper" style.
 * We read from STDIN: lines like "year,income"
 * We parse them, then distribute lines among ranks for partial processing,
 * then each rank prints out (key \t value) lines to STDOUT in rank 0 only.
 *
 * To compile and run with MPJ Express:
 *   mpjrun.sh -np 4 MeanHouseholdMalaysiaMapperMPI
 * (But note, this won't integrate with Hadoop streaming. It's purely HPC.)
 *
 */

import mpi.MPI;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class MeanHouseholdMalaysiaMapperMPI {

    public static void main(String[] args) throws Exception {
        // Initialize MPI
        MPI.Init(args);
        int rank = MPI.COMM_WORLD.Rank();
        int size = MPI.COMM_WORLD.Size();

        ArrayList<String> lines = new ArrayList<>();

        if (rank == 0) {
            // Rank 0 reads from STDIN
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
        }

        // Distribute lines to all ranks
        // We'll do a simple round-robin distribution
        // Convert lines to array
        String[] allLines = lines.toArray(new String[0]);
        int total = allLines.length;
        // We'll send total lines count first
        int[] totalCount = new int[1];
        if (rank == 0) {
            totalCount[0] = total;
        }
        MPI.COMM_WORLD.Bcast(totalCount, 0, 1, MPI.INT, 0);

        int localCount = totalCount[0] / size;
        int remainder = totalCount[0] % size;
        // we gather local lines in a list
        ArrayList<String> localLines = new ArrayList<>();
        if (rank == 0) {
            int idx = 0;
            for (int r = 0; r < size; r++) {
                int countToSend = localCount + ((r < remainder) ? 1 : 0);
                if (r == 0) {
                    // keep them for me
                    for (int c = 0; c < countToSend; c++) {
                        localLines.add(allLines[idx++]);
                    }
                } else {
                    // send them to rank r
                    String[] subset = new String[countToSend];
                    for (int c = 0; c < countToSend; c++) {
                        subset[c] = allLines[idx++];
                    }
                    // send subset
                    MPI.COMM_WORLD.Send(subset, 0, countToSend, MPI.OBJECT, r, 99);
                }
            }
        } else {
            // non-zero ranks receive
            int countToRecv = localCount + ((rank < remainder) ? 1 : 0);
            if (countToRecv > 0) {
                String[] subset = new String[countToRecv];
                MPI.COMM_WORLD.Recv(subset, 0, countToRecv, MPI.OBJECT, 0, 99);
                for (String s : subset) {
                    localLines.add(s);
                }
            }
        }

        // Process localLines
        // parse them: "year,income"
        // then print <year>\t<income>
        // but only rank 0 prints (to STDOUT) for a typical streaming approach
        // though this is weird for HPC
        ArrayList<String> mappedOutput = new ArrayList<>();
        for (String ln : localLines) {
            String[] parts = ln.split(",");
            if (parts.length < 2) {
                continue;
            }
            String year = parts[0].trim();
            String incomeStr = parts[1].trim();
            // We'll just emit "year\tincome"
            mappedOutput.add(year + "\t" + incomeStr);
        }

        // gather mappedOutput on rank 0
        // let's gather the arrays
        String[] localArray = mappedOutput.toArray(new String[0]);
        Object[] gatherArray = MPI.COMM_WORLD.Gather(localArray, 0, localArray.length, MPI.OBJECT, 0, MPI.OBJECT, 0);

        if (rank == 0) {
            // gatherArray is an array of arrays
            // we combine them
            for (Object o : gatherArray) {
                if (o instanceof String[]) {
                    String[] arr = (String[]) o;
                    for (String s : arr) {
                        System.out.println(s);
                    }
                }
            }
        }

        // Finalize
        MPI.Finalize();
    }
}
