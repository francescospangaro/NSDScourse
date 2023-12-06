#include <mpi.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>

/**
 * Group number:
 *
 * Group members
 * Member 1
 * Member 2
 * Member 3
 *
 **/

// Set DEBUG 1 if you want car movement to be deterministic
#define DEBUG 0

const int num_segments = 256;

const int num_iterations = 1000;
const int count_every = 10;

const double alpha = 0.5;
const int max_in_per_sec = 10;

int *segments;

// Returns the number of car that enter the first segment at a given iteration.
int create_random_input()
{
#if DEBUG
  return 1;
#else
  return rand() % max_in_per_sec;
#endif
}

// Returns 1 if a car needs to move to the next segment at a given iteration, 0 otherwise.
int move_next_segment()
{
#if DEBUG
  return 1;
#else
  return (rand() / RAND_MAX) < alpha ? 1 : 0;
#endif
}

int main(int argc, char **argv)
{
  MPI_Init(NULL, NULL);

  int rank;
  int num_procs;
  MPI_Comm_rank(MPI_COMM_WORLD, &rank);
  MPI_Comm_size(MPI_COMM_WORLD, &num_procs);
  srand(time(NULL) + rank);

  // TODO: define and init variables
  segments = calloc(sizeof(int), num_segments / num_procs);

  // Simulate for num_iterations iterations
  for (int it = 0; it < num_iterations; ++it)
  {
    // Move cars across segments
    for (int s = 0; s < (num_segments / num_procs) - 1; s++)
    {
      for (int c = 0; c < segments[s]; c++)
      {
        if (move_next_segment() == 1)
        {
          segments[s]--;
          segments[s + 1]++;
        }
      }
    }

    // New cars may enter in the first segment
    if (rank == 0)
      segments[0] += create_random_input();
    else
    {
      int buff;
      MPI_Recv(&buff, 1, MPI_INT, rank - 1, 0, MPI_COMM_WORLD, NULL);
      segments[0] += buff;
    }

    // Cars may exit from the last segment
    int exiting_cars = 0;
    for (int c = 0; c < segments[(num_segments / num_procs) - 1]; c++)
    {
      if (move_next_segment() == 1)
      {
        segments[(num_segments / num_procs) - 1]--;
        exiting_cars++;
      }
    }
    if (rank != num_procs - 1)
      MPI_Send(&exiting_cars, 1, MPI_INT, rank + 1, 0, MPI_COMM_WORLD);

    // When needed, compute the overall sum
    if (it % count_every == 0)
    {
      int local_sum = 0;
      for (int i = 0; i < num_segments / num_procs; i++)
        local_sum += segments[i];

      int global_sum = 0;
      MPI_Reduce(&local_sum, &global_sum, 1, MPI_INT, MPI_SUM, 0, MPI_COMM_WORLD);

      if (rank == 0)
      {
        printf("Iteration: %d, sum: %d\n", it, global_sum);
      }
    }

    MPI_Barrier(MPI_COMM_WORLD);
  }

  free(segments);
  MPI_Finalize();
}
