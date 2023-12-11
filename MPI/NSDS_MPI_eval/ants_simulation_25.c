#include <stdio.h>
#include <stdlib.h>
#include <time.h>
#include <mpi.h>
#include <math.h>

/*
 * Group number: 25
 *
 * Group members
 *  - Francesco Spangaro
 *  - Giacomo Orsenigo
 *  - Federico Saccani
 */

const float min = 0;
const float max = 1000;
const float len = max - min;
const int num_ants = 8 * 1000 * 1000;
const int num_food_sources = 10;
const int num_iterations = 500;

float random_position()
{
  return (float)rand() / (float)(RAND_MAX / (max - min)) + min;
}

/*
 * Process 0 invokes this function to initialize food sources.
 */
void init_food_sources(float *food_sources)
{
  for (int i = 0; i < num_food_sources; i++)
  {
    food_sources[i] = random_position();
  }
}

/*
 * Process 0 invokes this function to initialize the position of ants.
 */
void init_ants(float *ants)
{
  for (int i = 0; i < num_ants; i++)
  {
    ants[i] = random_position();
  }
}

float get_f1(float pos, const float *food_source)
{
  float min = food_source[0] - pos;
  for (int i = 1; i < num_food_sources; i++)
  {
    if (fabs(food_source[i] - pos) < fabs(min))
    {
      min = food_source[i] - pos;
    }
  }
  return min * 0.01;
}

float get_f2(float pos, float center)
{
  return (center - pos) * 0.012;
}

int main()
{
  MPI_Init(NULL, NULL);

  int rank;
  int num_procs;
  MPI_Comm_rank(MPI_COMM_WORLD, &rank);
  MPI_Comm_size(MPI_COMM_WORLD, &num_procs);

  srand(rank);

  // Allocate space in each process for food sources and ants
  float food_sources[num_food_sources];
  float *ants;
  if (rank == 0)
  {
    ants = calloc(sizeof(float), num_ants);
  }

  const int num_local_ants = num_ants / num_procs;
  float local_ants[num_local_ants];

  // Process 0 initializes food sources and ants
  if (rank == 0)
  {
    init_food_sources(food_sources);
    init_ants(ants);
  }

  // Process 0 distributed food sources and ants
  MPI_Bcast(food_sources, num_food_sources, MPI_FLOAT, 0, MPI_COMM_WORLD);
  MPI_Scatter(ants, num_local_ants, MPI_FLOAT, local_ants, num_local_ants, MPI_FLOAT, 0, MPI_COMM_WORLD);

  // Iterative simulation
  float center;
  if (rank == 0)
  {
    double sum = 0;
    for (int i = 0; i < num_ants; i++)
    {
      sum += ants[i];
    }
    center = sum / num_ants;
  }

  for (int iter = 0; iter < num_iterations; iter++)
  {
    // SEND center
    MPI_Bcast(&center, 1, MPI_FLOAT, 0, MPI_COMM_WORLD);

    // ANT moves
    for (int i = 0; i < num_local_ants; i++)
    {
      local_ants[i] += get_f1(local_ants[i], food_sources);
      local_ants[i] += get_f2(local_ants[i], center);
    }

    // Compute local center
    double sum = 0;
    for (int i = 0; i < num_local_ants; i++)
    {
      sum += local_ants[i];
    }
    float local_center = sum / num_local_ants;

    // compute center
    MPI_Reduce(&local_center, &center, 1, MPI_FLOAT, MPI_SUM, 0, MPI_COMM_WORLD);
    center /= num_procs;

    if (rank == 0)
    {
      printf("Iteration: %d - Average position: %f\n", iter, center);
    }
  }

  // Free memory
  if (rank == 0)
    free(ants);

  MPI_Finalize();
  return 0;
}
