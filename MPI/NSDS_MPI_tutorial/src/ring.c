#include <mpi.h>
#include <stdio.h>
#include <stdlib.h>

// Send a message in a ring starting from process 0 and increment by one at each hop.
// When the message gets back to process 0, print the number of hops.
int main(int argc, char** argv) {
  MPI_Init(NULL, NULL);

  int my_rank, world_size;
  MPI_Comm_rank(MPI_COMM_WORLD, &my_rank);
  MPI_Comm_size(MPI_COMM_WORLD, &world_size);
  int other_rank = 1 - my_rank;

  int num_hops = 0;
  for (int i=0; i<world_size; i++) {
    int sender = i;
    int receiver = (i+1) % world_size;

    if (my_rank == sender) {
      num_hops++;
      MPI_Send(&num_hops, 1, MPI_INT, receiver, 0, MPI_COMM_WORLD);
      printf("Process %d sent msg with num hops = %d\n", my_rank, num_hops);
    } else if (my_rank == receiver) {
      MPI_Recv(&num_hops, 1, MPI_INT, sender, 0, MPI_COMM_WORLD, MPI_STATUS_IGNORE);
      printf("Process %d received msg with num hops = %d\n", my_rank, num_hops);
    }
  }

  if (my_rank == 0) {
    printf("Final number of hops in process %d = %d\n", my_rank, num_hops);
  }
  
  MPI_Finalize();
}
