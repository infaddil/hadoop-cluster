import mpi.*;

public class HelloWorld {
    public static void main(String[] args) throws Exception {
        MPI.Init(args);
        int rank = MPI.COMM_WORLD.Rank();
        int size = MPI.COMM_WORLD.Size();
        System.out.println("Hello from rank " + rank + " of " + size);
        MPI.Finalize();
    }
}
