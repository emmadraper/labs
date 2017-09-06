import java.util.ArrayList;
import java.util.Collections;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Scanner;

public class Lab2 {

    private String input_filename;
    private String random_filename;
    private String scheduling;
    private int GlobalTime;
    private boolean running = false;
    private int RunEnd = 0;
    private Scanner randomScan;

    float trackCPURunTime=0;
    float trackIORunTime=0;
    
     ArrayList<Process> allProcess = new ArrayList<Process>();
     ArrayList<IOTracker> IOtrack = new ArrayList<IOTracker>();

    public Lab2(String a, String b, String c) {
          input_filename = a;
          random_filename = b;
          scheduling = c;
          GlobalTime = 0;
    }

    public static void main(String[] args) throws FileNotFoundException {
		 if(args.length != 3) {
		 	System.out.println("Function takes input filename, random filename & scheduling type as argument");
		 	return;
		 }
         Lab2 myScheduler = new Lab2(args[0], args[1], args[2]);
         myScheduler.readprocess();
         myScheduler.Schedule();
	}

    private int randomOS(int U) {
        int X = randomScan.nextInt();
        //System.out.print("Random Picked :"+X);
    	return (1 + (X % U));
    }

    public void readprocess() throws FileNotFoundException {
        File f = new File(input_filename);
        if(!f.exists()){
            throw new FileNotFoundException ("Did not find "+input_filename);
        }
        
        File r = new File(random_filename);
        if(!r.exists()){
            throw new FileNotFoundException ("Did not find "+random_filename);
        }
        
        randomScan = new Scanner(r);    
        Scanner scan = new Scanner(f);

        int NumberOfProcesses = scan.nextInt();
        
         for (int i = 0; i < NumberOfProcesses; i++) {
        	int arrival = scan.nextInt();
            int burst = scan.nextInt();
            int CTime = scan.nextInt();
            int IOTime = scan.nextInt();

            Process P = new Process(arrival, burst, CTime, IOTime);
            allProcess.add(P);
        } 
        
        System.out.print("\nThe original input was: ");
        int count=1;
        for(Process X: allProcess) {
          System.out.print(count+"\t"+X.A+" "+X.B+" "+X.C+" "+X.I);
          count++;
        }  
        System.out.println("");
        
        Collections.sort(allProcess);
        
        System.out.print("The (sorted) input is: ");
         count=1;
        for(Process X: allProcess) {
          System.out.print(count+"\t"+X.A+" "+X.B+" "+X.C+" "+X.I);
          count++;
        }  
        System.out.println("\n");
    }
    
    
    public void Schedule() throws FileNotFoundException {
        ArrayList<Process> ready = new ArrayList<Process>();
        ArrayList<Process> terminated = new ArrayList<Process>();
        ArrayList<Process> blocked = new ArrayList<Process>();
        
        for (int i = 0; i < allProcess.size(); i++) {
            Process P = allProcess.get(i);
            P.PNum = i;
            ready.add(P);
        }
        
        //System.out.println("Ready : "+ ready);
        
        while(!ready.isEmpty() || !blocked.isEmpty()) {

            if(RunEnd <= GlobalTime) running = false;
            
            while(!blocked.isEmpty() && blocked.get(0).timeStamp <= GlobalTime) {
               Process P = blocked.remove(0);
               if(P.currentState == 2) { // Process in block state
                   int currentIO_burst = randomOS(P.I); 
                   //System.out.println(" for IO burst, currentIO_burst="+currentIO_burst+"for "+P.PNum);
                   P.IOT += currentIO_burst;
                   trackIORunTime += currentIO_burst;
                   IOtrack.add(new IOTracker(P.timeStamp, P.timeStamp + currentIO_burst));
                   P.timeStamp = P.timeStamp + currentIO_burst;
                   P.currentState = 0;
                   ready = addTo(ready, P);
               } 
            }
            
            while(!ready.isEmpty() && ready.get(0).timeStamp <= GlobalTime && !running) {
            	Process P; // = ready.remove(0);
            	if(scheduling.equals("LCFS")){
            	   int maxIndex=0, currMaxTime =0;
            	   for(int i=0; i<ready.size(); i++) {
            	      if(ready.get(i).timeStamp <= GlobalTime && ready.get(i).timeStamp > currMaxTime) {
            	         maxIndex = i;
            	         currMaxTime = ready.get(i).timeStamp;
            	      }
            	   } 
            	   P = ready.remove(maxIndex);
            	}
            	else if(scheduling.equals("PSJF")){
            	   /*for(Process X: ready) {
            	      System.out.println("PNum: "+X.PNum+" TimeStamp: "+X.timeStamp +" RT: "+X.R);
            	   }*/
            	   int pickIndex=0, minRT = 9999999;
            	   for(int i=0; i<ready.size(); i++) {
            	      if(ready.get(i).timeStamp <= GlobalTime && ready.get(i).R < minRT) {
            	           pickIndex = i;
            	           minRT = ready.get(i).R;
            	      }
            	   } 
            	   P = ready.remove(pickIndex);
            	}
            	else {
            	  P = ready.remove(0);
            	}
                
                if(P.currentState == 0) {// Process in Ready State
                	if(scheduling.equals("RR")) {
                        P = RunRR(P); // Put in run and Run this process 
                	}
                	else {
                		P = RunFCFS(P);
                	}

                   //System.out.println("GlobalTime : "+GlobalTime);
                   //System.out.println("After Run: PNum="+ P.PNum +" ,timeStamp="+P.timeStamp+" ,currentState="+P.currentState+" ,R="+P.R);
                   
                   if(P.currentState == 2) {
                   	  blocked.add(P); 
                   }
                   else if(P.currentState == 3){
                   	 terminated.add(P);
                   }
                   else {
                     ready = addTo(ready, P);
                     //Collections.sort(ready);
                   }	 
                }
                
                if(RunEnd <= GlobalTime) running = false;
                
                if(!running){
                  Collections.sort(blocked);
                  while(!blocked.isEmpty() && blocked.get(0).timeStamp <= GlobalTime) {
                    Process P1 = blocked.remove(0);
                    if(P1.currentState == 2) { // Process in block state
                      int currentIO_burst = randomOS(P1.I); 
                      //System.out.println(" for IO burst, currentIO_burst="+currentIO_burst+"for "+P.PNum);
                      P1.IOT += currentIO_burst;
                      trackIORunTime += currentIO_burst;
                      IOtrack.add(new IOTracker(P1.timeStamp, P1.timeStamp + currentIO_burst));
                      P1.timeStamp = P1.timeStamp + currentIO_burst;
                      P1.currentState = 0;
                      ready = addTo(ready, P);
                    } 
                  }
                  //Collections.sort(ready);
                }
            } 
           
            Collections.sort(blocked); 
            GlobalTime++;
        }

        System.out.println("The scheduling algorithm used was "+scheduling);
        Collections.sort(terminated);
        int FinishingTime = 0;
        int avgTAT = 0;
        int avgWT = 0;
        int counter=0;
        for(Process X : terminated) {
           System.out.print("\nProcess "+X.PNum+":");
           System.out.println(X);
           if(X.FT > FinishingTime) FinishingTime = X.FT;
           avgTAT += X.TAT;
           avgWT += X.WT;
           counter++;
        }

        // print Summary 
        System.out.println("\nSummary Data:");
        System.out.println("\tFinishing Time: "+FinishingTime);
        System.out.println("\tCPU Utilization: "+(trackCPURunTime/FinishingTime));
        
        trackIORunTime = 0;
        int currmin = IOtrack.get(0).start, currmax=IOtrack.get(0).end;
        for(int i=1; i<IOtrack.size();i++){
            IOTracker I = IOtrack.get(i); 
            if(currmax < I.start) {
             trackIORunTime += (currmax - currmin);
             currmin = I.start;
             currmax = I.end;
            } 
            if(currmax >= I.start && I.end > currmax)
            { 
                currmax = I.end;
            } 
        }
        trackIORunTime += (currmax - currmin);
        
        System.out.println("\tI/O Utilization: "+(trackIORunTime/FinishingTime));
        System.out.println("\tThroughput: "+((float)terminated.size()/FinishingTime)*100 + " processes per hundred cycles");
        System.out.println("\tAverage turnaround time: "+((float)avgTAT/terminated.size()));
        System.out.println("\tAverage wait time: "+((float)avgWT/terminated.size()));
    }
    
    private ArrayList<Process> addTo(ArrayList<Process> ready, Process P){
        int i;
        for(i=0;i<ready.size();i++){
           if(ready.get(i).timeStamp > P.timeStamp) {
              break;
           }
           else if(ready.get(i).timeStamp == P.timeStamp && ready.get(i).PNum > P.PNum){
              break;
           }
        }
        ready.add(i,P);
        return ready;
    }

    private Process RunFCFS(Process P){
    	 // calculate CPU burst
        int currentCPU_burst = randomOS(P.B);
        //System.out.println(" for CPU burst");
        //System.out.println("CPU_burst:"+currentCPU_burst);

        if(currentCPU_burst > P.R) {
        	currentCPU_burst = P.R;
        }	

        //System.out.println("At GloabalTime "+GlobalTime +" Running PNo: "+P.PNum+" for currentCPU_burst:"+currentCPU_burst);

    	 // run for CPU burst
        P.WT += (GlobalTime - P.timeStamp);
        P.timeStamp = GlobalTime + currentCPU_burst;

        running = true;
        RunEnd = P.timeStamp;

        trackCPURunTime += currentCPU_burst;

    	 // check if process terminates or goes to blocked
        if((P.R - currentCPU_burst) == 0) { // terminate
           P.currentState = 3;
           P.FT = P.timeStamp;
           P.TAT = P.FT - P.A;
        }   
        else { 
           P.currentState = 2;  
        }  

        P.R = P.R - currentCPU_burst;  
    	//System.out.println("R:"+P.R);
    	return P;
    }

    private Process RunRR(Process P){
    	 // calculate CPU burst
    	 
        int currentCPU_burst;
        int balance_burst = 0;
         
        if(P.BB == 0){
           currentCPU_burst = randomOS(P.B);
           //System.out.println(" for CPU burst");
           //System.out.println("CPU_burst:"+currentCPU_burst);
        }
        else {
           currentCPU_burst = P.BB;
        }
       
        if(currentCPU_burst > P.R) {
        	currentCPU_burst = P.R;
        }
        
        if(currentCPU_burst > 2) {
            balance_burst = currentCPU_burst - 2;
            //System.out.println("balance_burst:"+balance_burst);
            currentCPU_burst = 2;
        }	
        
        //System.out.println("At GloabalTime "+GlobalTime +" Running PNo: "+P.PNum+" for currentCPU_burst:"+currentCPU_burst);

    	// run for CPU burst
        P.WT += (GlobalTime - P.timeStamp);
        P.timeStamp = GlobalTime + currentCPU_burst;

        running = true;
        RunEnd = P.timeStamp;

        trackCPURunTime += currentCPU_burst;

    	 // check if process terminates or goes to blocked
        if((P.R - currentCPU_burst) == 0) { // terminate
           P.currentState = 3;
           P.FT = P.timeStamp;
           P.TAT = P.FT - P.A;
           P.BB = 0;
        }   
        else if(balance_burst == 0){ 
           P.currentState = 2;  
           P.BB = 0;
        }  
        else {
           P.BB = balance_burst;
           P.currentState = 0;
        }

        P.R = P.R - currentCPU_burst;  
    	//System.out.println("R:"+P.R);
    	return P;
    }

    private class Process implements Comparable<Process> {
    	int PNum;
		int A; // arrival Time
		int B; // CPU burst Time
		int C; // total CPU time needed
		int I; // IO burst Time

		int R; // total CPU time remaining
		int timeStamp; 
		int currentState; // 0-Ready, 1-Run, 2-IO/block, 3-Terminate 

		int FT, TAT, IOT, WT, BB;

		public Process(int a, int b, int c, int i) {
			A = a;
			B = b;
			C = c;
			I = i;
			R = c;
			timeStamp = a;
			currentState = 0;
			FT = 0; TAT = 0; IOT = 0; WT = 0;
			BB = 0;
		}

		public int compareTo(Process X) {
			if(this.timeStamp < X.timeStamp)
				return -1;
			if(this.timeStamp == X.timeStamp) {
				if(this.A < X.A)
					return -1;
				else if(this.A > X.A)
					return 1;
				return 0;
			}
			return 1;
		}

		public String toString() {
			return "\n\t(A,B,C,IO) = (" +A +"," + B + "," + C + "," + I + ")\n\tFinishing time: " + FT + "\n\tTurnaround time: " + TAT + "\n\tI/O time: " + IOT + "\n\tWaiting Time: "+WT;
		}
    } 
    
    private class IOTracker{
        int start;
        int end;
        public IOTracker(int a, int b) {
			start = a;
			end = b;
		}
		public String toString(){
		    return "{"+start +", "+end+"}";
		}
    }

}

    
   


