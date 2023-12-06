#include <mpi.h>
#include <stdio.h>

// This program avoids deadlocking by using a non-blocking send
int main(int argc, char** argv) {
  MPI_Init(NULL, NULL);

  int my_rank;
  MPI_Comm_rank(MPI_COMM_WORLD, &my_rank);
  int other_rank = 1 - my_rank;

  int msg_to_send = 1;
  int msg_to_recv;
  MPI_Request req;
  MPI_Isend(&msg_to_send, 1, MPI_INT, other_rank, 0, MPI_COMM_WORLD, &req);
  MPI_Recv(&msg_to_recv, 1, MPI_INT, other_rank, MPI_ANY_TAG, MPI_COMM_WORLD, MPI_STATUS_IGNORE);
  MPI_Wait(&req, MPI_STATUS_IGNORE);

  printf("Process %d received message %d\n", my_rank, msg_to_recv);
  
  MPI_Finalize();
}
