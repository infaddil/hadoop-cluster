/*
 * MeanHouseholdMalaysiaReducerMPI.java (Corrected for MPJ Express + Java 8)
 *
 * Reads "year\tincome" lines from STDIN at rank 0, distributes them,
 * each rank partially accumulates sums by year, then Gatherv merges at rank 0,
 * final average is printed by rank 0 to STDOUT.
 */

import mpi.MPI;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

public class MeanHouseholdMalaysiaReducerMPI {

    public static void main(String[] args) throws Exception {
        MPI.Init(args);
        int rank = MPI.COMM_WORLD.Rank();
        int size = MPI.COMM_WORLD.Size();

        ArrayList<String> lines = new ArrayList<>();

        if (rank == 0) {
            // read from STDIN
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            String ln;
            while ((ln = br.readLine()) != null) {
                lines.add(ln);
            }
        }

        // broadcast line count
        String[] allLines = lines.toArray(new String[0]);
        int total = allLines.length;
        int[] totalArr = new int[1];
        if (rank == 0) {
            totalArr[0] = total;
        }
        MPI.COMM_WORLD.Bcast(totalArr, 0, 1, MPI.INT, 0);
        total = totalArr[0];

        int baseCount = total / size;
        int remainder = total % size;
        int myCount = baseCount + ((rank < remainder) ? 1 : 0);

        ArrayList<String> localLines = new ArrayList<>();
        if (rank == 0) {
            int idx = 0;
            for (int r = 0; r < size; r++) {
                int sendCount = baseCount + ((r < remainder) ? 1 : 0);
                if (r == 0) {
                    for (int i = 0; i < sendCount; i++) {
                        localLines.add(allLines[idx++]);
                    }
                } else {
                    String[] subset = new String[sendCount];
                    for (int i = 0; i < sendCount; i++) {
                        subset[i] = allLines[idx++];
                    }
                    MPI.COMM_WORLD.Send(subset, 0, sendCount, MPI.OBJECT, r, 77);
                }
            }
        } else {
            if (myCount > 0) {
                String[] subset = new String[myCount];
                MPI.COMM_WORLD.Recv(subset, 0, myCount, MPI.OBJECT, 0, 77);
                for (String s : subset) {
                    localLines.add(s);
                }
            }
        }

        // local reduce: sum by year
        // lines are "year\tincome"
        HashMap<String, Double> sumMap = new HashMap<>();
        HashMap<String, Integer> countMap = new HashMap<>();

        for (String ln : localLines) {
            String[] parts = ln.split("\t");
            if (parts.length < 2) continue;
            String year = parts[0];
            try {
                double inc = Double.parseDouble(parts[1]);
                sumMap.put(year, sumMap.getOrDefault(year, 0.0) + inc);
                countMap.put(year, countMap.getOrDefault(year, 0) + 1);
            } catch (NumberFormatException e) {
                // ignore
            }
        }

        // Convert partial results to an array of "year\t sum \t count"
        ArrayList<String> partialList = new ArrayList<>();
        for (String y : sumMap.keySet()) {
            double s = sumMap.get(y);
            int c = countMap.get(y);
            partialList.add(y + "\t" + s + "\t" + c);
        }

        String[] localArray = partialList.toArray(new String[0]);
        int localLen = localArray.length;

        // gather counts
        int[] sendCountArr = new int[1];
        sendCountArr[0] = localLen;
        int[] recvCounts = new int[size];
        MPI.COMM_WORLD.Gather(sendCountArr, 0, 1, MPI.INT,
                              recvCounts, 0, 1, MPI.INT,
                              0);

        int totalGather = 0;
        int[] displs = null;
        if (rank == 0) {
            displs = new int[size];
            for (int r = 0; r < size; r++) {
                displs[r] = totalGather;
                totalGather += recvCounts[r];
            }
        }

        String[] reduceOutputs = null;
        if (rank == 0 && totalGather > 0) {
            reduceOutputs = new String[totalGather];
        }

        MPI.COMM_WORLD.Gatherv(localArray, 0, localLen, MPI.OBJECT,
                               reduceOutputs, 0, recvCounts, displs, MPI.OBJECT,
                               0);

        if (rank == 0 && reduceOutputs != null) {
            // combine final results
            HashMap<String, Double> globalSum = new HashMap<>();
            HashMap<String, Integer> globalCount = new HashMap<>();

            for (String rec : reduceOutputs) {
                if (rec == null) continue;
                String[] sp = rec.split("\t");
                if (sp.length < 3) continue;
                String yy = sp[0];
                double s = Double.parseDouble(sp[1]);
                int c = Integer.parseInt(sp[2]);
                globalSum.put(yy, globalSum.getOrDefault(yy, 0.0) + s);
                globalCount.put(yy, globalCount.getOrDefault(yy, 0) + c);
            }

            // print final average
            for (String year : globalSum.keySet()) {
                double sumVal = globalSum.get(year);
                int cc = globalCount.get(year);
                double avg = sumVal / cc;
                System.out.println(year + "\t" + avg);
            }
        }

        MPI.Finalize();
    }
}
