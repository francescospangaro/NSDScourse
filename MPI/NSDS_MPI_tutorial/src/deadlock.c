#include <mpi.h>
#include <stdio.h>

// Run with two processes.
// Process 0 sends an integer to process 1 and vice-versa.
// Try to run the system: what goes wrong?
int main(int argc, char** argv) {
  MPI_Init(NULL, NULL);

  int my_rank;
  MPI_Comm_rank(MPI_COMM_WORLD, &my_rank);
  int other_rank = 1 - my_rank;

  int msg_to_send = 1;
  int msg_to_recv;
  MPI_Ssend(&msg_to_send, 1, MPI_INT, other_rank, 0, MPI_COMM_WORLD);
  MPI_Recv(&msg_to_recv, 1, MPI_INT, other_rank, MPI_ANY_TAG, MPI_COMM_WORLD, MPI_STATUS_IGNORE);
  
  MPI_Finalize();
}
