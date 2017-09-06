#include<stdio.h>
#include<stdlib.h>
#include<math.h>
#include<string.h>
#include <omp.h>

unsigned int genprimes (unsigned int N, unsigned int t, unsigned int *array);   
void writeToFile (char *filename, unsigned int arraySize, unsigned int *array);
int cmpfunc (const void * a, const void * b); 
 
int main(int argc, char *argv[]) 
{
   unsigned int N, t; 
   unsigned int * array; 
   unsigned int arraySize;
   double tstart = 0.0, ttaken;
   
   if(argc!=3) 
   { 
      fprintf(stderr, "Invoke as : ./genprimes N t");
      fprintf(stderr, "N : number between 2 and 1,000,000");
      fprintf(stderr, "t : number of threads");
      return 0;
   }
   
   N = (unsigned int) atoi(argv[1]);
   t = (unsigned int) atoi(argv[2]);
   array = (unsigned int *)calloc(N-1, sizeof(unsigned int));
   
   if(!array)
   {
      fprintf(stderr, " Cannot allocate the %u x %u array\n", N, N);
      return 0;
   }
   
   int i, n = 2;
   for(i = 0; i < N-1; i++,n++)
   {
      array[i] = n;
   }
   
   tstart = omp_get_wtime();
   //this function call finds the prime nums
   arraySize = genprimes(N,t,array);
   ttaken = omp_get_wtime() - tstart;
   printf("Time take for the main part: %f\n", ttaken);
   
   writeToFile(argv[1], arraySize, array);
   printf("\nNumber of primes generated : %d",arraySize);
   printf("\nCheck file %s to see the prime numbers.\n\n",argv[1]);

   return 0;
}

int cmpfunc (const void * a, const void * b) 
{
   return ( *(int*)a - *(int*)b ); 
}

unsigned int genprimes (unsigned int N, unsigned int t, unsigned int *array) 
{
   unsigned int arraySize = N-1, j, index;
   unsigned int * newarray, *temp;  
   unsigned int next = 2;
   unsigned int nextIndex = 0;
   
   newarray = (unsigned int *)calloc(arraySize, sizeof(unsigned int));  
   unsigned int limit = (unsigned int) floor((N+1)/2);
   
   while(next <= limit) 
   { 
      index = 0;
      //divides array into parallel threads
      #pragma omp parallel for num_threads(t)
      for(j = 0; j < arraySize; j++)
      {
         //condition for prime
         if(array[j] != next && array[j] % next == 0) 
            continue;
         //avoids data erase (find 4 before 6)
         #pragma omp critical 
         {   
           newarray[index] = array[j];
           index ++;
         }
      }
      
      arraySize = index;
      qsort (newarray, arraySize, sizeof(int), cmpfunc);
      
      temp = newarray;
      newarray = array;
      array = temp;
      //changes next (ie number used to divide)
      if(array[nextIndex] == next) {
        nextIndex ++;
        next = array[nextIndex];
      }
   }
   
   return arraySize;
}

void writeToFile (char *filename, unsigned int arraySize, unsigned int *array) 
{
   strcat(filename, ".txt");
   FILE *fp = fopen(filename, "w+");
   int i, diff = 0;
   for(i = 0; i < arraySize; i++)
   {
      fprintf(fp, "%d, %d, %d\n", i+1, array[i], diff);
      diff = array[i+1] - array[i];
   }
   fclose(fp);   
}