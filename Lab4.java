import java.util.ArrayList;
import java.util.HashMap;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Scanner;

public class Lab4 {
   
   int M;
   int P;
   int S;
   int J;
   int N;
   String R;

   int quantum = 3;

   ArrayList<Process> allProcess = new ArrayList<Process> ();

   private Scanner randomScan;
   private String random_filename = "random.txt";

   class Process {
   	   int pNum;
   	   int count;
       double A, B, C;
       int word;
       int noOfFaults;
       int noOfEvictions;
       int residencySum;

       public Process (int a, double b, double c, double d) {
            pNum = a;
            A = b;
            B = c;
            C = d;
            noOfFaults = 0;
            noOfEvictions = 0;
            residencySum = 0;
       }
   }

   class PageInfo {
      int ProcessNum;
      int PageNum;
      int loadTime;

      public PageInfo(int p1, int p2, int lt) {
      	 ProcessNum = p1;
      	 PageNum = p2;
      	 loadTime = lt;
      }
   }

   int noOfFrames;
   ArrayList<PageInfo> FrameTable = new ArrayList<PageInfo>(); 

   public Lab4(int a, int b, int c, int d, int e, String f) {
       M = a;
       P = b;
       S = c;
       J = d;
       N = e;
       R = f;

       noOfFrames = M/P;

       if(J == 1) {
       	  allProcess.add(new Process(0, 1, 0,  0));
       }
       else if(J == 2) {
       	  allProcess.add(new Process(0, 1, 0,  0));
       	  allProcess.add(new Process(1, 1, 0,  0));
       	  allProcess.add(new Process(2, 1, 0,  0));
       	  allProcess.add(new Process(3, 1, 0,  0));
       }
       else if(J == 3) {
       	  allProcess.add(new Process(0, 0, 0,  0));
       	  allProcess.add(new Process(1, 0, 0,  0));
       	  allProcess.add(new Process(2, 0, 0,  0));
       	  allProcess.add(new Process(3, 0, 0,  0));
       }
       else { // j == 4
          allProcess.add(new Process(0, 0.75, 0.25,  0));
       	  allProcess.add(new Process(1, 0.75, 0,  0.25));
       	  allProcess.add(new Process(2, 0.75, 0.125,  0.125));
       	  allProcess.add(new Process(3, 0.5, 0.125,  0.125));
       }
   }

   public static void main(String[] args) throws FileNotFoundException{
       if(args.length != 6) {
       	  System.out.println("invoke as Lab4 M P S J N R");
       	  return;
       }

       Lab4 obj = new Lab4(Integer.parseInt(args[0]), Integer.parseInt(args[1]), Integer.parseInt(args[2]), Integer.parseInt(args[3]), Integer.parseInt(args[4]), args[5]);
        
       System.out.println("The machine size is "+obj.M+".\nThe page size is "+obj.P+".\nThe process size is "+obj.S+".\nThe job mix number is "+obj.J+".\nThe number of references per process is "+obj.N+ ".\nThe replacement algorithm is "+obj.R+".");

       if(obj.R.equals("FIFO") || obj.R.equals("fifo"))
          obj.simulateFIFO();
       else if(obj.R.equals("RANDOM") || obj.R.equals("random"))
       	  obj.simulateRandom();
       else if(obj.R.equals("LRU") || obj.R.equals("lru"))
       	   obj.simulateLRU();
        else System.out.println("Invalid Replacement policy");

       obj.printSummary(); 
       
   }

   public void printSummary() {
       
        System.out.println("");
        double overallResidencySum = 0;
        int overallnoOfEvictions = 0;
        int overallFaults = 0;
        for(Process P : allProcess) {
        	overallFaults += P.noOfFaults; 
        	if(P.noOfEvictions != 0) {
        	  double avgR = (double)P.residencySum/P.noOfEvictions;
        	  overallResidencySum += P.residencySum;
        	  overallnoOfEvictions += P.noOfEvictions;
        	  System.out.println("Process "+(P.pNum+1) +" had "+P.noOfFaults +" and "+ avgR +" average residency.");
        	}  
        	else {
              System.out.println("Process "+(P.pNum+1) +" had "+P.noOfFaults +" and average residency is not defined.");		
        	}  
        } 

        double overallavgR = overallResidencySum/overallnoOfEvictions;
        System.out.println("");
        System.out.println("The total number of faults is "+overallFaults+" and the overall average residency is "+ overallavgR+".");

   }
   
   private int getNextWord(int P, int w) {
        int r = randomScan.nextInt();
        double y = r / (Integer.MAX_VALUE + 1d);
        if(y < allProcess.get(P).A) {
            return ((int)(w+1+S)%S);
        }
        else if(y < allProcess.get(P).A + allProcess.get(P).B) {
        	return ((int)(w-5+S)%S);
        }
        else if(y < allProcess.get(P).A + allProcess.get(P).B + allProcess.get(P).C) {
        	return ((int)(w+4+S)%S);
        }
        else {
            return (randomScan.nextInt() % S);
        }
   }

   public int findPageNumInFrameTable(int pageNum, int processNum) {

   	   for(int j=noOfFrames-1; j>= 0; j--) {
            PageInfo pi = FrameTable.get(j);
            if (pi.ProcessNum == processNum && pi.PageNum == pageNum) {
                return j;
            }
        }
        return -1;
   }

   public void simulateFIFO() throws FileNotFoundException {

   	    File r = new File(random_filename);
        if(!r.exists()){
            throw new FileNotFoundException ("Did not find "+random_filename);
        }
        
        randomScan = new Scanner(r); 

        ArrayList<Integer> Done = new ArrayList<Integer>();

        for(Process P : allProcess) {
            P.count = N;
            P.word = (111* (P.pNum+1)) % S;
        }
       
        int time = 1;

        // Frame Size = M/P;
        for(int i=0; i<noOfFrames; i++) {
        	FrameTable.add(new PageInfo(-1, -1, -1));
        }

        int FIFOreplacementIndex = noOfFrames - 1;
        // get page from word = word / P

        while(Done.size() != allProcess.size()) {
        	for(int n = 0; n < allProcess.size(); n++) {
        	  Process pro = allProcess.get(n);
        	  for(int i=0; i < quantum; i++) {
        	  	   // simulate this reference for this process
                  
                   int accessPageNum = pro.word / P; 

                   int frameNum = findPageNumInFrameTable(accessPageNum, pro.pNum);
                   //System.out.print("For Process "+ (pro.pNum+1) + " accessing word "+pro.word +" (Page "+accessPageNum+ ") at time " +time +": ");


                   if( frameNum == -1 )  { // Page Not found == MISS 
                   	   pro.noOfFaults ++;
                       // if any frame free, use that 
                   	   boolean foundFree = false; int index = -1;
                       for(int j=noOfFrames-1; j>= 0; j--) {
                       	     PageInfo pi = FrameTable.get(j);
                       	     if (pi.ProcessNum == -1 && pi.PageNum == -1) {
                       	     	foundFree = true; index = j;
                       	     	break;
                       	     }
                       }
                       
                       if(foundFree) {
                       	   FrameTable.remove(index);
                       	   FrameTable.add(index, new PageInfo(pro.pNum, accessPageNum, time));
                       	   //System.out.println("Fault, using free frame "+ index);
                       }

                       // else replace some frame
                       else { // replace using FIFO
                       	   //System.out.println("Fault, evicting page "+ FrameTable.get(FIFOreplacementIndex).PageNum + " of " + (FrameTable.get(FIFOreplacementIndex).ProcessNum+1) + " from frame " + FIFOreplacementIndex);
                           
                           Process evictP = allProcess.get(FrameTable.get(FIFOreplacementIndex).ProcessNum);
                           evictP.noOfEvictions ++;
                           evictP.residencySum += (time - FrameTable.get(FIFOreplacementIndex).loadTime);


                           FrameTable.remove(FIFOreplacementIndex);
                           FrameTable.add(FIFOreplacementIndex, new PageInfo(pro.pNum, accessPageNum, time));
                           

                           if(FIFOreplacementIndex == 0) {
                           	   FIFOreplacementIndex = noOfFrames - 1;
                           }
                           else {
                           	   FIFOreplacementIndex --;
                           }
                       }  
                   }	
                   else { // HIT
                        //System.out.println("Hit in frame "+ frameNum);
                   }
                   
                   time ++; 
                   // calculate the next reference for this process
                   pro.word = getNextWord(pro.pNum, pro.word);

                   pro.count --;
                   if(pro.count == 0) {
                   	  Done.add(pro.pNum);
                   	  break;
                   }
              }
            }
        }

    }

    public void simulateRandom() throws FileNotFoundException {

   	    File r = new File(random_filename);
        if(!r.exists()){
            throw new FileNotFoundException ("Did not find "+random_filename);
        }
        
        randomScan = new Scanner(r); 

        ArrayList<Integer> Done = new ArrayList<Integer>();

        for(Process P : allProcess) {
            P.count = N;
            P.word = (111* (P.pNum+1)) % S;
        }
       
        int time = 1;

        // Frame Size = M/P;
        for(int i=0; i<noOfFrames; i++) {
        	FrameTable.add(new PageInfo(-1, -1, -1));
        }

        // get page from word = word / P

        while(Done.size() != allProcess.size()) {
        	for(int n = 0; n < allProcess.size(); n++) {
        	  Process pro = allProcess.get(n);
        	  for(int i=0; i < quantum; i++) {
        	  	   // simulate this reference for this process
                  
                   int accessPageNum = pro.word / P; 

                   int frameNum = findPageNumInFrameTable(accessPageNum, pro.pNum);
                   //System.out.print("For Process "+ (pro.pNum+1) + " accessing word "+pro.word +" (Page "+accessPageNum+ ") at time " +time +": ");


                   if( frameNum == -1 )  { // Page Not found == MISS 
                   	   pro.noOfFaults ++;
                       // if any frame free, use that 
                   	   boolean foundFree = false; int index = -1;
                       for(int j=noOfFrames-1; j>= 0; j--) {
                       	     PageInfo pi = FrameTable.get(j);
                       	     if (pi.ProcessNum == -1 && pi.PageNum == -1) {
                       	     	foundFree = true; index = j;
                       	     	break;
                       	     }
                       }
                       
                       if(foundFree) {
                       	   FrameTable.remove(index);
                       	   FrameTable.add(index, new PageInfo(pro.pNum, accessPageNum, time));
                       	   //System.out.println("Fault, using free frame "+ index);
                       }

                       // else replace some frame
                       else { // replace using Random
                       	   int randomIndex = randomScan.nextInt() % noOfFrames;
                       	   //System.out.println("Fault, evicting page "+ FrameTable.get(randomIndex).PageNum + " of " + (FrameTable.get(randomIndex).ProcessNum+1) + " from frame " + randomIndex);
                           
                           Process evictP = allProcess.get(FrameTable.get(randomIndex).ProcessNum);
                           evictP.noOfEvictions ++;
                           evictP.residencySum += (time - FrameTable.get(randomIndex).loadTime);

                           FrameTable.remove(randomIndex);
                           FrameTable.add(randomIndex, new PageInfo(pro.pNum, accessPageNum, time));       
                       }  
                   }	
                   else { // HIT
                        //System.out.println("Hit in frame "+ frameNum);
                   }
                   
                   time ++; 
                   // calculate the next reference for this process
                   pro.word = getNextWord(pro.pNum, pro.word);

                   pro.count --;
                   if(pro.count == 0) {
                   	  Done.add(pro.pNum);
                   	  break;
                   }
              }
            }
        }

    }

     public void simulateLRU() throws FileNotFoundException {

   	    File r = new File(random_filename);
        if(!r.exists()){
            throw new FileNotFoundException ("Did not find "+random_filename);
        }
        
        randomScan = new Scanner(r); 

        ArrayList<Integer> Done = new ArrayList<Integer>();

        for(Process P : allProcess) {
            P.count = N;
            P.word = (111* (P.pNum+1)) % S;
        }
       
        int time = 1;

        // Frame Size = M/P;
        for(int i=0; i<noOfFrames; i++) {
        	FrameTable.add(new PageInfo(-1, -1, -1));
        }

        ArrayList<Integer> LRU = new ArrayList<Integer>();
        // get page from word = word / P

        while(Done.size() != allProcess.size()) {
        	for(int n = 0; n < allProcess.size(); n++) {
        	  Process pro = allProcess.get(n);
        	  for(int i=0; i < quantum; i++) {
        	  	   // simulate this reference for this process
                  
                   int accessPageNum = pro.word / P; 

                   int frameNum = findPageNumInFrameTable(accessPageNum, pro.pNum);
                   //System.out.print("For Process "+ (pro.pNum+1) + " accessing word "+pro.word +" (Page "+accessPageNum+ ") at time " +time +": ");


                   if( frameNum == -1 )  { // Page Not found == MISS 
                   	   pro.noOfFaults ++;
                       // if any frame free, use that 
                   	   boolean foundFree = false; int index = -1;
                       for(int j=noOfFrames-1; j>= 0; j--) {
                       	     PageInfo pi = FrameTable.get(j);
                       	     if (pi.ProcessNum == -1 && pi.PageNum == -1) {
                       	     	foundFree = true; index = j;
                       	     	break;
                       	     }
                       }
                       
                       if(foundFree) {
                       	   LRU.add(index);
                       	   FrameTable.remove(index);
                       	   FrameTable.add(index, new PageInfo(pro.pNum, accessPageNum, time));
                       	   //System.out.println("Fault, using free frame "+ index);
                       }

                       // else replace some frame
                       else { // replace using FIFO
                       	   int LRUreplacementIndex = LRU.remove(0);
                       	   //System.out.println("Fault, evicting page "+ FrameTable.get(LRUreplacementIndex).PageNum + " of " + (FrameTable.get(LRUreplacementIndex).ProcessNum+1) + " from frame " + LRUreplacementIndex);
                           
                           Process evictP = allProcess.get(FrameTable.get(LRUreplacementIndex).ProcessNum);
                           evictP.noOfEvictions ++;
                           evictP.residencySum += (time - FrameTable.get(LRUreplacementIndex).loadTime);

                           LRU.add(LRUreplacementIndex);
                           FrameTable.remove(LRUreplacementIndex);
                           FrameTable.add(LRUreplacementIndex, new PageInfo(pro.pNum, accessPageNum, time));
                       }  
                   }	
                   else { // HIT
                   	    LRU.remove(new Integer(frameNum));
                        LRU.add(frameNum);
                        //System.out.println("Hit in frame "+ frameNum);
                   }
                   
                   time ++; 
                   // calculate the next reference for this process
                   pro.word = getNextWord(pro.pNum, pro.word);

                   pro.count --;
                   if(pro.count == 0) {
                   	  Done.add(pro.pNum);
                   	  break;
                   }
              }
            }
        }

    }        
        
}



