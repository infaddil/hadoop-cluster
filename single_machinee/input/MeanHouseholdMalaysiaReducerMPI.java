/*
 * MeanHouseholdMalaysiaReducerMPI.java
 *
 * Another contrived MPI program that reads key-value lines
 * like "year\tincome", distributed among ranks, then each rank partially
 * accumulates sums, then rank 0 merges them and prints final <year\tavg>
 *
 * This is HPC code, not typical Hadoop approach. We do it because you demanded
 * "mapper" and "reducer" in Java for MPI.
 */

import mpi.MPI;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.ArrayList;

public class MeanHouseholdMalaysiaReducerMPI {

    public static void main(String[] args) throws Exception {
        MPI.Init(args);
        int rank = MPI.COMM_WORLD.Rank();
        int size = MPI.COMM_WORLD.Size();

        ArrayList<String> lines = new ArrayList<>();

        if (rank == 0) {
            // read lines from STDIN
            // e.g. "year\tincome"
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            String ln;
            while ((ln = br.readLine()) != null) {
                lines.add(ln);
            }
        }

        // Distribute lines to all ranks (round-robin or the same approach as mapper)
        String[] allLines = lines.toArray(new String[0]);
        int total = allLines.length;
        int[] totalCount = new int[1];
        if (rank == 0) {
            totalCount[0] = total;
        }
        MPI.COMM_WORLD.Bcast(totalCount, 0, 1, MPI.INT, 0);

        int localCount = totalCount[0] / size;
        int remainder = totalCount[0] % size;
        ArrayList<String> localLines = new ArrayList<>();
        if (rank == 0) {
            int idx = 0;
            for (int r = 0; r < size; r++) {
                int ctsend = localCount + ((r < remainder) ? 1 : 0);
                if (r == 0) {
                    for (int c = 0; c < ctsend; c++) {
                        localLines.add(allLines[idx++]);
                    }
                } else {
                    String[] subset = new String[ctsend];
                    for (int c = 0; c < ctsend; c++) {
                        subset[c] = allLines[idx++];
                    }
                    MPI.COMM_WORLD.Send(subset, 0, ctsend, MPI.OBJECT, r, 77);
                }
            }
        } else {
            int ctrecv = localCount + ((rank < remainder) ? 1 : 0);
            if (ctrecv > 0) {
                String[] subset = new String[ctrecv];
                MPI.COMM_WORLD.Recv(subset, 0, ctrecv, MPI.OBJECT, 0, 77);
                for (String s : subset) {
                    localLines.add(s);
                }
            }
        }

        // local reduce
        // We'll accumulate sums by year
        HashMap<String, Double> sumMap = new HashMap<>();
        HashMap<String, Integer> countMap = new HashMap<>();

        for (String ln : localLines) {
            String[] parts = ln.split("\t");
            if (parts.length < 2) continue;
            String year = parts[0];
            String incStr = parts[1];
            try {
                double inc = Double.parseDouble(incStr);
                sumMap.put(year, sumMap.getOrDefault(year, 0.0) + inc);
                countMap.put(year, countMap.getOrDefault(year, 0) + 1);
            } catch (NumberFormatException e) {
                // ignore
            }
        }

        // convert partial results into an array of "year sum count"
        ArrayList<String> partialList = new ArrayList<>();
        for (String y : sumMap.keySet()) {
            double s = sumMap.get(y);
            int c = countMap.get(y);
            partialList.add(y + "\t" + s + "\t" + c);
        }
        String[] partialArr = partialList.toArray(new String[0]);

        // gather them at rank 0
        Object[] gatherArray = MPI.COMM_WORLD.Gather(partialArr, 0, partialArr.length, MPI.OBJECT, 0, MPI.OBJECT, 0);

        if (rank == 0) {
            // combine
            HashMap<String, Double> globalSum = new HashMap<>();
            HashMap<String, Integer> globalCount = new HashMap<>();

            for (Object o : gatherArray) {
                if (o instanceof String[]) {
                    String[] arr = (String[]) o;
                    for (String rec : arr) {
                        String[] sp = rec.split("\t");
                        if (sp.length < 3) continue;
                        String yy = sp[0];
                        double ss = Double.parseDouble(sp[1]);
                        int cc = Integer.parseInt(sp[2]);
                        globalSum.put(yy, globalSum.getOrDefault(yy, 0.0) + ss);
                        globalCount.put(yy, globalCount.getOrDefault(yy, 0) + cc);
                    }
                }
            }

            // output final average
            for (String year : globalSum.keySet()) {
                double sumVal = globalSum.get(year);
                int cVal = globalCount.get(year);
                double avg = sumVal / cVal;
                // Print to STDOUT: year \t avg
                System.out.println(year + "\t" + avg);
            }
        }

        MPI.Finalize();
    }
}
