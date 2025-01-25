/*
 * MeanHouseholdMalaysiaMapperMPI.java
 *
 * Reads lines from STDIN at rank 0, "distributes" them to each rank,
 * each rank transforms them from "year,income" -> "year\tincome".
 * We gather them back at rank 0 with Gatherv, then rank 0 prints to STDOUT.
 *
 * This version has debug prints to show progress, so you know if it's stuck.
 */

import mpi.MPI;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class MeanHouseholdMalaysiaMapperMPI {

    public static void main(String[] args) throws Exception {
        MPI.Init(args);
        int rank = MPI.COMM_WORLD.Rank();
        int size = MPI.COMM_WORLD.Size();

        if (rank == 0) {
            System.out.println("[Mapper] Rank 0: Starting. Reading lines from STDIN...");
        }

        ArrayList<String> lines = new ArrayList<>();

        // rank 0 reads from STDIN
        if (rank == 0) {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
            System.out.println("[Mapper] Rank 0: Done reading " + lines.size() + " lines.");
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

        if (rank == 0) {
            System.out.println("[Mapper] Rank 0: Broadcasting total lines = " + total);
        }

        MPI.COMM_WORLD.Barrier(); // debug barrier

        // compute how many lines each rank gets
        int baseCount = (total > 0) ? (total / size) : 0;
        int remainder = (total > 0) ? (total % size) : 0;
        int myCount = 0;
        if (total > 0) {
            myCount = baseCount + ((rank < remainder) ? 1 : 0);
        }

        if (rank == 0) {
            System.out.println("[Mapper] Rank 0: baseCount=" + baseCount + ", remainder=" + remainder);
        }

        // rank 0 distribute lines
        ArrayList<String> localLines = new ArrayList<>();
        if (rank == 0) {
            int idx = 0;
            for (int r = 0; r < size; r++) {
                int sendCount = (total > 0) ? (baseCount + ((r < remainder) ? 1 : 0)) : 0;
                if (r == 0) {
                    for (int i = 0; i < sendCount; i++) {
                        localLines.add(allLines[idx++]);
                    }
                    System.out.println("[Mapper] Rank 0: Taking " + sendCount + " lines for myself.");
                } else {
                    String[] subset = new String[sendCount];
                    for (int i = 0; i < sendCount; i++) {
                        subset[i] = allLines[idx++];
                    }
                    if (sendCount > 0) {
                        MPI.COMM_WORLD.Send(subset, 0, sendCount, MPI.OBJECT, r, 99);
                        System.out.println("[Mapper] Rank 0: Sent " + sendCount + " lines to rank " + r);
                    }
                }
            }
        } else {
            if (myCount > 0) {
                String[] subset = new String[myCount];
                MPI.COMM_WORLD.Recv(subset, 0, myCount, MPI.OBJECT, 0, 99);
                for (String s : subset) {
                    localLines.add(s);
                }
                System.out.println("[Mapper] Rank " + rank + ": Received " + myCount + " lines from rank 0.");
            }
        }

        MPI.COMM_WORLD.Barrier(); // debug barrier
        System.out.println("[Mapper] Rank " + rank + ": localLines.size()=" + localLines.size());

        // map step: parse each line, e.g. "year,income" -> "year\tincome"
        ArrayList<String> mappedList = new ArrayList<>();
        for (String ln : localLines) {
            String[] parts = ln.split(",");
            if (parts.length < 2) {
                // skip if not enough columns
                continue;
            }
            String year = parts[0].trim();
            String incomeStr = parts[1].trim();
            mappedList.add(year + "\t" + incomeStr);
        }

        int localLen = mappedList.size();
        String[] localArray = mappedList.toArray(new String[0]);

        // gather counts
        int[] sendCountArr = new int[1];
        sendCountArr[0] = localLen;
        int[] recvCounts = new int[size];
        MPI.COMM_WORLD.Gather(sendCountArr, 0, 1, MPI.INT,
                              recvCounts, 0, 1, MPI.INT, 0);

        int totalGather = 0;
        int[] displs = null;
        if (rank == 0) {
            displs = new int[size];
            for (int r = 0; r < size; r++) {
                displs[r] = totalGather;
                totalGather += recvCounts[r];
            }
            System.out.println("[Mapper] Rank 0: totalGather for final array = " + totalGather);
        }

        String[] mapperOutputs = null;
        if (rank == 0 && totalGather > 0) {
            mapperOutputs = new String[totalGather];
        }

        MPI.COMM_WORLD.Gatherv(localArray, 0, localLen, MPI.OBJECT,
                               mapperOutputs, 0, recvCounts, displs, MPI.OBJECT, 0);

        MPI.COMM_WORLD.Barrier(); // debug barrier
        if (rank == 0 && mapperOutputs != null) {
            System.out.println("[Mapper] Rank 0: Gathered " + mapperOutputs.length + " lines total. Printing...");
            for (String s : mapperOutputs) {
                System.out.println(s);
            }
        }

        MPI.Finalize();
    }
}
