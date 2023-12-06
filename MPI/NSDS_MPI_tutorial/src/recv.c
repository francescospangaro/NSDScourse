#include <mpi.h>
#include <stdio.h>

// Every process p>0 waits for an interger from process p=0 that never arrives.
// Try to check the use of resources.
int main(int argc, char** argv) {
  MPI_Init(NULL, NULL);

  int my_rank;
  MPI_Comm_rank(MPI_COMM_WORLD, &my_rank);

  if (my_rank > 0) {
    int buf;
    MPI_Recv(&buf, 1, MPI_INT, 0, MPI_ANY_TAG, MPI_COMM_WORLD, MPI_STATUS_IGNORE);
  }

  MPI_Finalize();
}
