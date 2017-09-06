#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include <mpi.h>

/*** Skeleton for Lab 1 ***/
// worked with Neil Kumar and Laura as collaborators

/***** Globals ******/
float **a; /* The coefficients */
float *x;  /* The unknowns */
float *b;  /* The constants */
float err; /* The absolute relative error */
int num = 0;  /* number of unknowns */
int comm_sz;
int rank;


/****** Function declarations */
void check_matrix(); /* Check whether the matrix will converge */
void get_input();  /* Read input from file */

/********************************/

/* Function definitions: functions are ordered alphabetically ****/
/*****************************************************************/
/* 
   Conditions for convergence (diagonal dominance):
   1. diagonal element >= sum of all other elements of the row
   2. At least one diagonal element > sum of all other elements of the row
 */
void check_matrix()
{
  int bigger = 0; /* Set to 1 if at least one diag element > sum  */
  int i, j;
  float sum = 0;
  float aii = 0;
  
  for(i = 0; i < num; i++)
  {
    sum = 0;
    aii = fabs(a[i][i]);
    
    for(j = 0; j < num; j++)
       if( j != i)
	 sum += fabs(a[i][j]);
       
    if( aii < sum)
    {
      printf("The matrix will not converge.\n");
      exit(1);
    }
    
    if(aii > sum)
      bigger++;
    
  }
  
  if( !bigger )
  {
     printf("The matrix will not converge\n");
     exit(1);
  }
}


/******************************************************/
/* Read input from file */
/* After this function returns:
 * a[][] will be filled with coefficients and you can access them using a[i][j] for element (i,j)
 * x[] will contain the initial values of x
 * b[] will contain the constants (i.e. the right-hand-side of the equations
 * num will have number of variables
 * err will have the absolute error that you need to reach
 */
/*void get_num(char filename[])
{
  FILE * fp; 
 
  fp = fopen(filename, "r");
  if(!fp)
  {
    printf("Cannot open file %s\n", filename);
    exit(1);
  }

  fscanf(fp,"%d ",&num);
  fscanf(fp,"%f ",&err);
  fclose(fp); 
}*/

void get_input(char filename[])
{
  FILE * fp; 
  int i,j;
 
  fp = fopen(filename, "r");
  if(!fp)
  {
    printf("Cannot open file %s\n", filename);
    exit(1);
  }

  fscanf(fp,"%d ",&num);
  fscanf(fp,"%f ",&err);

//allocate vectors and matries
  a = (float**)malloc(num * sizeof(float*));
  if( !a)
  {
    printf("Cannot allocate a!\n");
    exit(1);
  }
  for(i = 0; i < num; i++) 
  {
    a[i] = (float *)malloc(num * sizeof(float)); 
    if( !a[i])
    {
    printf("Cannot allocate a[%d]!\n",i);
    exit(1);
    }
  }
 
  x = (float *) malloc(num * sizeof(float));
  if( !x)
  {
    printf("Cannot allocate x!\n");
    exit(1);
  }

  b = (float *) malloc(num * sizeof(float));
  if( !b)
  {
    printf("Cannot allocate b!\n");
    exit(1);
  }
/* Now .. Filling the blanks */ 

//the initial values of the xs
 for(i = 0; i < num; i++)
  fscanf(fp,"%f ", &x[i]);
 
 for(i = 0; i < num; i++)
 {
   for(j = 0; j < num; j++)
     fscanf(fp,"%f ",&a[i][j]);
   
   /* reading the b element */
   fscanf(fp,"%f ",&b[i]);
 }

  fclose(fp); 

}

int computations(int num_iterations){
  // determine the number of elements each process will work with (need to add its share of remainder)
  // figure out a starting and ending point

  int *counts = (int*)malloc(num * sizeof(int));
  int *displs = (int*)malloc(num * sizeof(int));

  int remainder;

  if(num < comm_sz){
     remainder = comm_sz;
  }
  else{
    remainder = num % comm_sz;
  }

//ie dividing up the tasks to decide the distribution of tasks per processor 
int distribution = num / comm_sz;
int beginning;
//if 7 processes and 5 left over
if(rank < remainder){
  beginning = rank * distribution + rank;
//no need to go to 7 just go to offset 5
}else{
  beginning = rank * distribution + remainder;
}
int end = beginning + distribution - 1;
if(rank < remainder){
  end += 1;
}

int local_counter = end - beginning + 1;
//printf("%d\n", local_counter);

float* newX = (float*)malloc(local_counter*sizeof(float)); // keep track of newly calculated x values
// use this variable in call to all_gather to create new x

// a and b remain unchanged because they are global variables 
// therefore when we use them we will not have to distribute them
// each process has an array x

// create a new array to put new x's in 
// do the error calculation in each process
// if the percentage is over, set a flag that indicates we need to loop again
// use reduce to find the maximum of those flags (1 means loop again)
// create a method called all_gather that puts the new x's into the old
// all_gather automatically synchronizes everything!

int i;
int j;
float sumX;
float maximumError = 0.0;
int repeat = 1;

while(repeat){
  maximumError = 0.0;
  if(rank == 0){
    num_iterations++;
  }

  //calculate the new x values using the old ones
  for (i = 0; i < local_counter; i++){
    //subtract all of the x's: ((except for the x at j) * their corresponding a[j])/ by a[i]
    sumX = 0;
    for(j = 0; j < num; j++){
        if(j != i+beginning){
          sumX -= x[j] * a[i+beginning][j]; //follow the directions in step 1 in lab1 instructions
        }
    }
    newX[i] = (b[i+beginning]+ sumX) / a[i+beginning][i+beginning];
    //printf("%d: %f\n", rank, newX[i]);
  }

  // calculate percent error for each x in localX, while keeping track of the max
  // use this max to set the flag indicating whether or not to continue the while loop
  float currentError;
    for(i = 0; i < local_counter; i++){
      currentError = ((newX[i] - x[i+beginning])/newX[i]);
    }
    if(currentError < 0) {
      currentError *= -1.0;
    }
    if(currentError > maximumError) {
      maximumError = currentError;
    }

    //printf("error: %f\n", maximumError);

    int flag = 0;
    // if the largest percent error is greater than error then the flag gets set so the program knows to continue looping
    if (maximumError >= err) flag = 1;

  // based on whether or not maximumError is within the bounds of the allowed error
  // we set error equal to the result of reducing the flags
  // use the flag from each process and multiply and distribute the result to each process
    MPI_Allreduce(&flag, &repeat, 1, MPI_INT, MPI_MAX, MPI_COMM_WORLD); 

  // if any of the processes returns 1 for the flag, repeat will be set to 1 and the loop will continue
  // else: repeat will be set equal to 0 and the loop will break
    
    MPI_Allgather(&local_counter, 1, MPI_INT, counts, 1, MPI_INT, MPI_COMM_WORLD);
    MPI_Allgather(&beginning, 1, MPI_INT, displs, 1, MPI_INT, MPI_COMM_WORLD);
    MPI_Allgatherv(newX, local_counter, MPI_FLOAT, x, counts, displs, MPI_FLOAT, MPI_COMM_WORLD);

  //include MPI_ALLgatherv
  }

  // at this point the values from newX have been saved in x so it is safe to free it
  free(newX);
  
  // Now x should hold all values for x within the allowed percent error
  // we can now output these values to stdout in main method
  return num_iterations; 
}

/************************************************************/

int main(int argc, char *argv[])
{

 int i;
 int num_iterations = 0; /* number of iterations */

  
 if(argc != 2)
 {
   printf("Usage: gsref filename\n");
   exit(1);
 }
 
 // Read the input file and fill the global data structure above 
 //if(my_rank == 0){
    get_input(argv[1]);  

 // Check for convergence condition 
 // This function will exit the program if the coffeicient will never converge to the needed absolute error. 
 // This is not expected to happen for this programming assignment.
  
    check_matrix();
 //}
/*
 MPI_Bcast(a, sizeof(a), MPI_FLOAT, 0, MPI_COMM_WORLD);
 MPI_Bcast(b, sizeof(b), MPI_FLOAT, 0, MPI_COMM_WORLD);
 MPI_Bcast(x, sizeof(x), MPI_FLOAT, 0, MPI_COMM_WORLD);
 */
 
 MPI_Init(&argc, &argv);
 MPI_Comm_size(MPI_COMM_WORLD, &comm_sz);
 MPI_Comm_rank(MPI_COMM_WORLD, &rank);
 double local_begin, local_end, local_elapsed, elapsed;

MPI_Barrier(MPI_COMM_WORLD);
local_begin = MPI_Wtime();
 
if(num < comm_sz){
  printf("There are only %d unkowns, you don't need %d processes to solve this. \n", num, comm_sz);
}
else{
  num_iterations = computations(num_iterations);

  local_end = MPI_Wtime();
  local_elapsed = local_end-local_begin;
  MPI_Reduce(&local_elapsed, &elapsed, 1, MPI_DOUBLE, MPI_MAX, 0, MPI_COMM_WORLD);

/* Writing to the stdout */
/* Keep that same format */
if(rank ==0){  
 for(i =0; i < num; i++)
    printf("%f\n", x[i]);

  printf("total number of iterations: %d\n", num_iterations);
 }
}
 MPI_Finalize();

 exit(0);

}
