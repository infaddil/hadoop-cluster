/*
 * MeanHouseholdMalaysiaMapperMPI.java (Corrected for MPJ Express + Java 8)
 *
 * We read lines from STDIN at rank 0, distribute them, each rank processes
 * lines, then gather them into rank 0's "mapperOutputs" array, and rank 0 prints.
 *
 * This compiles under Java 8 with MPJ Express.
 */

import mpi.MPI;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;

public class MeanHouseholdMalaysiaMapperMPI {

    public static void main(String[] args) throws Exception {
        MPI.Init(args);
        int rank = MPI.COMM_WORLD.Rank();
        int size = MPI.COMM_WORLD.Size();

        ArrayList<String> lines = new ArrayList<>();

        // rank 0 reads from STDIN
        if (rank == 0) {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
        }

        // convert to array
        String[] allLines = lines.toArray(new String[0]);
        int total = allLines.length;

        // broadcast total
        int[] totalArr = new int[1];
        if (rank == 0) {
            totalArr[0] = total;
        }
        MPI.COMM_WORLD.Bcast(totalArr, 0, 1, MPI.INT, 0);
        total = totalArr[0];

        // compute how many lines each rank gets
        int baseCount = total / size;
        int remainder = total % size;
        int myCount = baseCount + ((rank < remainder) ? 1 : 0);

        // rank 0 distribute lines via Send/Recv
        ArrayList<String> localLines = new ArrayList<>();
        if (rank == 0) {
            int idx = 0;
            for (int r = 0; r < size; r++) {
                int sendCount = baseCount + ((r < remainder) ? 1 : 0);
                if (r == 0) {
                    // keep for me
                    for (int i = 0; i < sendCount; i++) {
                        localLines.add(allLines[idx++]);
                    }
                } else {
                    // send a string array
                    String[] subset = new String[sendCount];
                    for (int i = 0; i < sendCount; i++) {
                        subset[i] = allLines[idx++];
                    }
                    MPI.COMM_WORLD.Send(subset, 0, sendCount, MPI.OBJECT, r, 99);
                }
            }
        } else {
            if (myCount > 0) {
                String[] subset = new String[myCount];
                MPI.COMM_WORLD.Recv(subset, 0, myCount, MPI.OBJECT, 0, 99);
                for (String s : subset) {
                    localLines.add(s);
                }
            }
        }

        // map step: parse each line, if it's "year,income" etc. -> output "year\tincome"
        ArrayList<String> mappedList = new ArrayList<>();
        for (String ln : localLines) {
            // Suppose lines have: year,income
            String[] parts = ln.split(",");
            if (parts.length < 2) continue;
            String year = parts[0].trim();
            String incomeStr = parts[1].trim();
            // produce "year\tincome"
            mappedList.add(year + "\t" + incomeStr);
        }

        // now gather these mapped lines at rank 0
        // we have local mappedList -> localArray
        String[] localArray = mappedList.toArray(new String[0]);
        int localLen = localArray.length;

        // gather counts
        int[] sendCountArr = new int[1];
        sendCountArr[0] = localLen;
        int[] recvCounts = new int[size];

        MPI.COMM_WORLD.Gather(sendCountArr, 0, 1, MPI.INT,
                              recvCounts, 0, 1, MPI.INT,
                              0);

        // rank 0 knows how many total to expect
        int totalGather = 0;
        int[] displs = null;
        if (rank == 0) {
            displs = new int[size];
            for (int r = 0; r < size; r++) {
                displs[r] = totalGather;
                totalGather += recvCounts[r];
            }
        }

        // create a large buffer on rank 0 to receive everything
        String[] mapperOutputs = null;
        if (rank == 0 && totalGather > 0) {
            mapperOutputs = new String[totalGather];
        }

        // gather
        MPI.COMM_WORLD.Gatherv(localArray, 0, localLen, MPI.OBJECT,
                               mapperOutputs, 0, recvCounts, displs, MPI.OBJECT,
                               0);

        // rank 0 prints them
        if (rank == 0 && mapperOutputs != null) {
            for (String s : mapperOutputs) {
                System.out.println(s);
            }
        }

        MPI.Finalize();
    }
}
