#include <mpi.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

// Distributed count of the number of occurrences of each character in a set of files.
//
// For simplicity, the program considers only the 26 (lower case) letters of the english
// alphabet.
//
// The computation takes place in two phases (inspired by the MapReduce programming model).
//
// In the first phase, each process reads one file and computes the number of occurrences
// of each character in that file.
//
// In the second phase, each process becomes responsible for some of the characters and
// aggregates the partial counts for those characters.
//
// The code exemplifies the use of custom datatypes and the use of asynchronous communication.
//
// Possible extensions/improvements:
// 1. Each process can send (asynchronously) all the data it has for other processes
//    before starting to receive.
// 2. The number of processes could be different than the number of files.
// 3. The final result can be collected in a single node.
 typedef struct pair_char_pair_t {
  char c;
  int count;
} char_int_pair;

int main(int argc, char** argv) {
  const int num_letters = 26;
  const char *files_path = "../files/in";
  
  MPI_Init(NULL, NULL);

  // Create the char_int_pair MPI datatype
  char_int_pair pair;
  MPI_Datatype mpi_char_int_pair;
  int struct_len = 2;
  int block_lens[struct_len];
  MPI_Datatype types[struct_len];
  // We need to compute the displacement to be really portable
  // (different compilers might align structures differently)
  MPI_Aint displacements[struct_len];
  MPI_Aint current_displacement = 0;
  // Add one char
  block_lens[0] = 1;
  types[0] = MPI_CHAR;
  displacements[0] = (size_t) &(pair.c) - (size_t) &pair;
  // Add one int
  block_lens[1] = 1;
  types[1] = MPI_INT;
  displacements[1] = (size_t) &(pair.count) - (size_t) &pair;
  // Create and commit the data structure
  MPI_Type_create_struct(struct_len, block_lens, displacements, types, &mpi_char_int_pair);
  MPI_Type_commit(&mpi_char_int_pair);
  
  int my_rank, world_size;
  MPI_Comm_rank(MPI_COMM_WORLD, &my_rank);
  MPI_Comm_size(MPI_COMM_WORLD, &world_size);

  // Getting the name of my file (supports up to 100 processes)
  int files_path_len = strlen(files_path);
  char my_file[files_path_len + 2];
  strcpy(my_file, files_path);
  sprintf(&my_file[files_path_len], "%d", my_rank);

  // Phase 1 (read input file and compute the number of occurrences of each character)
  char_int_pair *local_count = (char_int_pair *) malloc(sizeof(char_int_pair) * num_letters);
  for (int i=0; i<num_letters; i++) {
    local_count[i].c = i + 'a';
    local_count[i].count = 0;
  }
  FILE *file = fopen(my_file, "r");
  char c;
  while ((c = fgetc(file)) != EOF) {
    int c_index = (int) c - 'a';
    local_count[c_index].count++;
  }
  fclose(file);

  // Sending and receiving each letter
  MPI_Request req;
  for (int i=0; i<num_letters; i++) {
    int receiver = i % world_size;
    // I am the receiver
    if (receiver == my_rank) {
      // I receive one message from any other process
      for (int p=0; p<world_size-1; p++) {
	char_int_pair rec_buffer;
	MPI_Recv(&rec_buffer, 1, mpi_char_int_pair, MPI_ANY_SOURCE, MPI_ANY_TAG, MPI_COMM_WORLD, MPI_STATUS_IGNORE);
	local_count[i].count += rec_buffer.count;
      }
    }
    // I am a sender: I can send asynchronously
    else {
      MPI_Isend(&local_count[i], 1, mpi_char_int_pair, receiver, 0, MPI_COMM_WORLD, &req);
    }
  }
  
  // When done, everybody prints its own results
  for (int i=0; i<num_letters; i++) {
    int owner = i % world_size;
    if (owner == my_rank) {
      printf("%c -> %d\n", local_count[i].c, local_count[i].count);
    }
  }

  free(local_count);
  MPI_Type_free(&mpi_char_int_pair);
  MPI_Finalize();
}
