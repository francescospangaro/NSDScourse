#include <mpi.h>
#include <stdio.h>
#include <stdlib.h>

// Simple ping pong program to exemplify MPI_Send and MPI_Recs
// Assume only two processes
int main(int argc, char** argv) {
  const int tot_msgs = 100;
  
  MPI_Init(NULL, NULL);

  int my_rank;
  MPI_Comm_rank(MPI_COMM_WORLD, &my_rank);
  int other_rank = 1 - my_rank;

  int num_msgs = 0;
  while (num_msgs < tot_msgs) {
    if (num_msgs % 2 == my_rank) {
      num_msgs++;
      MPI_Send(&num_msgs, 1, MPI_INT, other_rank, 0, MPI_COMM_WORLD);
      printf("Process %d sent message %d\n", my_rank, num_msgs);
    } else {
      MPI_Recv(&num_msgs, 1, MPI_INT, other_rank, 0, MPI_COMM_WORLD, MPI_STATUS_IGNORE);
      printf("Process %d received message %d\n", my_rank, num_msgs);
    }
  }

  MPI_Finalize();
}
