import java.util.ArrayList;
import java.util.HashMap;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Scanner;

public class Lab3 {

	class Activity{
		String type;
		int task_num;
		int delay;
		int resource_type;
		int initial_claim;

		public Activity(String a, int b, int c, int d, int e) {
            type = a;
			task_num = b;
			delay = c;
			resource_type = d;
			initial_claim = e;
		}

		public String toString() {
			return type +" " + task_num + " " +delay +" "+ resource_type +" " + initial_claim;
		}
	}

	class TaskInfo{
		int numberOfActivities;
		int WT;
		int RT;
		int counter;
		boolean waiting;
		boolean aborted; 
		ArrayList<Integer> resourceInUse;

		public TaskInfo(int x, int max) {
			WT = 0;
			RT = 0;
			counter = 0;
			numberOfActivities = x;
			waiting = false;
			aborted = false;
			resourceInUse = new ArrayList<Integer>();
			for(int i=0; i < max; i++) {
               resourceInUse.add(0);
            }
		}

		public String toString() {
			return  numberOfActivities+" WT: " + WT+ " RT: " +RT +" "+ counter;
		}
	}

	int NumberOfTasks;
    int NumberOfResources;
    ArrayList<Integer> maxResourceAvailable = new ArrayList<Integer>();
	HashMap<Integer,ArrayList<Activity>> input = new HashMap<Integer,ArrayList<Activity>>();
	HashMap<Integer,TaskInfo> taskInfo = new HashMap<Integer,TaskInfo>();
	HashMap<Integer,Boolean> aborted = new HashMap<Integer,Boolean>();
   
   public static void main(String[] args) throws FileNotFoundException{
       if(args.length != 1) {
       	  System.out.println("invoke as Lab3 filename");
       }

       Lab3 obj = new Lab3();
       obj.readFile(args[0]);
       obj.FIFO();

       Lab3 obj1 = new Lab3();
       obj1.readFile(args[0]);
       obj1.Bankers();

   }

   public void readFile(String input_filename) throws FileNotFoundException{
        File f = new File(input_filename);
        if(!f.exists()){
            throw new FileNotFoundException ("Did not find "+input_filename);
        }
        Scanner scan = new Scanner(f);

        NumberOfTasks = scan.nextInt();
        NumberOfResources = scan.nextInt();

        for(int i=0; i < NumberOfResources; i++) {
        	int val = scan.nextInt();
            maxResourceAvailable.add(val);
        }

        //System.out.println("NumberOfTasks: "+NumberOfTasks);
        //System.out.println("NumberOfResources: "+NumberOfResources);
        //System.out.println("maxResourceAvailable = "+ maxResourceAvailable);


        while(scan.hasNext()) {
        	String type = scan.next();
		    int task_num = scan.nextInt();
		    int delay = scan.nextInt();
		    int resource_type = scan.nextInt();
		    int initial_claim = scan.nextInt();
            Activity activity = new Activity(type, task_num, delay, resource_type, initial_claim);
            
            ArrayList<Activity> xyz;
            if(input.containsKey(task_num)) {
               xyz = input.get(task_num);
            }  
            else {
               xyz = new ArrayList<Activity>();
            }

            xyz.add(activity);
            input.put(task_num, xyz);
        }

        //System.out.println("input = " + input);

        for(int i = 0; i<NumberOfTasks; i++) {
        	 int task_num = i+1;
             taskInfo.put(task_num, new TaskInfo(input.get(task_num).size(), NumberOfResources));
        }
       
        //System.out.println("taskInfo = " +  taskInfo);

   }

   public void Bankers() {
 
      ArrayList<Integer> terminate = new  ArrayList<Integer>();
      ArrayList<Integer> orderOfBlocked = new  ArrayList<Integer>();
      HashMap<Integer,Boolean> keepTrackOfBlocked = new HashMap<Integer,Boolean>();

      int[][] maxClaim = new int[NumberOfTasks][NumberOfResources];
      int[][] currAllocated = new int[NumberOfTasks][NumberOfResources];

       for(int i = 0; i<NumberOfTasks; i++) {
       	   for(int j = 0; j<NumberOfResources; j++){
       	   	   maxClaim[i][j] = 0;
       	   	   currAllocated[i][j] = 0;
       	   } 
       }


      int globaltime = 0;
      while(terminate.size() <  NumberOfTasks) {
         //System.out.println("cycle = " +  globaltime + "-" + (globaltime+1));
      	 globaltime ++ ;

      	 ArrayList<Integer> releasedResources = new ArrayList<Integer>();
      	 for(int i=0; i < NumberOfResources; i++) {
              releasedResources.add(0);
         }

         //first serve blocked
         for(int j = 0; j< orderOfBlocked.size(); j++) { // for
         	int task_num = orderOfBlocked.get(j);
      	 	TaskInfo TI = taskInfo.get(task_num);
      	 	boolean flag = false;

      	 	//System.out.println("\nCurrent Task Info = "+ TI);

            if(TI.counter < TI.numberOfActivities && !TI.aborted) { // if
                 // Execute 
            	 Activity current = input.get(task_num).get(TI.counter);

                 //System.out.println("executing Activity = " +  current);
            	 if(current.type.equals("initiate")) {
            	 	 //System.out.println("In initiate");
            	 	TI.waiting = false;
                    TI.RT ++;
                    TI.counter ++;
                    taskInfo.put(task_num, TI); 
                    flag = true;

                    if(current.initial_claim > maxResourceAvailable.get(current.resource_type-1)){
                         TI.aborted = true;
                         aborted.put(task_num,new Boolean(true));
                         terminate.add(task_num);
                         //System.out.println("Aborted " + task_num);
                    }
                    else {
                    	 maxClaim[task_num-1][current.resource_type-1] = current.initial_claim;
                    }
                    

            	 }
            	 else if(current.type.equals("request")) {
            	 	//System.out.println("In request");
            	 	if(current.delay != 0){
                        TI.RT ++;
                        current.delay --;
            	 		taskInfo.put(task_num, TI);
	                    flag = true;
	                    //System.out.println("compute, cycles left = " + current.delay);
            	 	}
            	 	else{
            	 		ArrayList<Integer> maxResourceAvailableCopy = new ArrayList<Integer>();
            	 		maxResourceAvailableCopy.addAll(maxResourceAvailable);
            	 		int[][] currAllocated1 = new int[NumberOfTasks][NumberOfResources];

					       for(int p = 0; p<NumberOfTasks; p++) {
					       	   for(int q = 0; q<NumberOfResources; q++) {
					       	   	   currAllocated1[p][q] = currAllocated[p][q];
					       	   
					       	   } 
					       }
					       
					    int claim = currAllocated[task_num-1][current.resource_type-1] + current.initial_claim;
					     
					    if(claim > maxClaim[task_num-1][current.resource_type-1]){
                           TI.aborted = true;
                           aborted.put(task_num,new Boolean(true));
                           terminate.add(task_num);
                           TI.waiting = false;
	                   
	                       TI.counter ++; 
	                       for(int i=0; i < NumberOfResources; i++) {
	                          int resource = TI.resourceInUse.get(i);
	                          TI.resourceInUse.remove(i);
	                          TI.resourceInUse.add(i, 0);

	                          currAllocated[task_num-1][i] = 0;

	                          int remaining = releasedResources.get(i) + resource;
	                          releasedResources.remove(i);
	                          releasedResources.add(i, remaining);
	                       }
	                       taskInfo.put(task_num, TI);
	                       
                           //System.out.println("Aborted " + task_num);
                        }   
                        
	            	 	else if(isSafe(current, maxClaim, currAllocated1, maxResourceAvailableCopy)) {
	            	 		//System.out.println("Safe to grant request");
	                    	TI.waiting = false;
	                    	TI.RT += (current.delay + 1);
	                    	TI.counter ++; 
	                        int remaining = maxResourceAvailable.get(current.resource_type-1) - current.initial_claim;
	                        maxResourceAvailable.remove(current.resource_type-1);
	                    	maxResourceAvailable.add(current.resource_type-1, remaining);

	                    	int resource = current.initial_claim + TI.resourceInUse.get(current.resource_type-1);
	                    	TI.resourceInUse.remove(current.resource_type-1);
	                    	TI.resourceInUse.add(current.resource_type-1, resource);
	                    	flag = true;

	                    	currAllocated[task_num-1][current.resource_type-1] = resource;
	                    } 
	                    else {
	                    	//System.out.println("Unsafe : can't execute Activity currently, will try later");
	                    	TI.waiting = true;
	                    	TI.WT ++;
	                    	flag = false;
	                    }
	                    taskInfo.put(task_num, TI);

	            	}
	                    
            	 }
            	 else if(current.type.equals("release")) {
            	 	//System.out.println("In release");
            	 	if(current.delay != 0){
                        TI.RT ++;
                        current.delay --;
            	 		taskInfo.put(task_num, TI);
	                    flag = true;
	                    //System.out.println("compute, cycles left = " + current.delay);
            	 	}
            	 	else {
	            	 	TI.waiting = false;
	                    TI.RT ++;
	                    TI.counter ++; 
	                    int remaining = releasedResources.get(current.resource_type-1) + current.initial_claim;
	                    releasedResources.remove(current.resource_type-1);
	                    releasedResources.add(current.resource_type-1, remaining);

	                    int resource = TI.resourceInUse.get(current.resource_type-1) - current.initial_claim;
	                    TI.resourceInUse.remove(current.resource_type-1);
	                    TI.resourceInUse.add(current.resource_type-1, resource);
	                    taskInfo.put(task_num, TI);

	                    flag = true;

	                    currAllocated[task_num-1][current.resource_type-1] = resource;
                    }
            	 }
            	 else { // terminate
            	 	//System.out.println("In terminate");

            	 	if(current.delay != 0){
                        TI.RT ++;
                        current.delay --;
            	 		taskInfo.put(task_num, TI);
	                    flag = true;
	                    //System.out.println("compute, cycles left = " + current.delay);
            	 	}
            	 	else{
            	 		TI.waiting = false;
	                    terminate.add(task_num);
	                    TI.counter ++; 
	                    for(int i=0; i < NumberOfResources; i++) {
	                       int resource = TI.resourceInUse.get(i);
	                       TI.resourceInUse.remove(i);
	                       TI.resourceInUse.add(i, 0);

	                       currAllocated[task_num-1][i] = 0;

	                       int remaining = releasedResources.get(i) + resource;
	                       releasedResources.remove(i);
	                       releasedResources.add(i, remaining);
	                    }
	                    taskInfo.put(task_num, TI);

	                    flag = true;

            	 	}	
            	 }
            } // end if

            keepTrackOfBlocked.put(task_num, new Boolean(flag)); 
              
         } // end for

         //System.out.println("Execute the rest ");

      	 for(int j = 0; j<NumberOfTasks; j++) { // for 

      	 	int task_num = j+1;	
      	 	if(keepTrackOfBlocked.containsKey(task_num)) {
      	 		continue;
      	 	}
      	 
      	 	TaskInfo TI = taskInfo.get(task_num);

      	 	//System.out.println("\nCurrent Task Info = "+ TI);

            if(TI.counter < TI.numberOfActivities && !TI.aborted) { // if
                 // Execute 
            	 Activity current = input.get(task_num).get(TI.counter);

                 //System.out.println("executing Activity = " +  current);
            	 if(current.type.equals("initiate")) {
            	 	 //System.out.println("In initiate");
            	 	TI.waiting = false;
                    TI.RT ++;
                    TI.counter ++;
                    taskInfo.put(task_num, TI); 

                     if(current.initial_claim > maxResourceAvailable.get(current.resource_type-1)){
                         TI.aborted = true;
                         aborted.put(task_num,new Boolean(true));
                         terminate.add(task_num);
                         //System.out.println("Aborted " + task_num);
                    }
                    else {
                    	 maxClaim[task_num-1][current.resource_type-1] = current.initial_claim;
                    }
            	 }
            	 else if(current.type.equals("request")) {
            	 	//System.out.println("In request");
            	 	if(current.delay != 0){
                        TI.RT ++;
                        current.delay --;
            	 		taskInfo.put(task_num, TI);
	                    //System.out.println("compute, cycles left = " + current.delay);
            	 	}
            	 	else{
	                    ArrayList<Integer> maxResourceAvailableCopy = new ArrayList<Integer>();
            	 		maxResourceAvailableCopy.addAll(maxResourceAvailable);
						int[][] currAllocated1 = new int[NumberOfTasks][NumberOfResources];

					       for(int p = 0; p<NumberOfTasks; p++) {
					       	   for(int q = 0; q<NumberOfResources; q++) {
					       	   	   currAllocated1[p][q] = currAllocated[p][q];
					       	   
					       	   } 
					       }
					     
					    int claim = currAllocated[task_num-1][current.resource_type-1] + current.initial_claim;
					     
					    if(claim > maxClaim[task_num-1][current.resource_type-1]){
                           TI.aborted = true;
                           aborted.put(task_num,new Boolean(true));
                           terminate.add(task_num);
                           TI.waiting = false;
	                   
	                       TI.counter ++; 
	                       for(int i=0; i < NumberOfResources; i++) {
	                          int resource = TI.resourceInUse.get(i);
	                          TI.resourceInUse.remove(i);
	                          TI.resourceInUse.add(i, 0);

	                          currAllocated[task_num-1][i] = 0;

	                          int remaining = releasedResources.get(i) + resource;
	                          releasedResources.remove(i);
	                          releasedResources.add(i, remaining);
	                       }
	                       taskInfo.put(task_num, TI);
	                       
                           //System.out.println("Aborted " + task_num);
                        }   
                        
	            	 	else if(isSafe(current, maxClaim, currAllocated1, maxResourceAvailableCopy)) {
	                    	//System.out.println("Safe to grant request");
	                    	TI.waiting = false;
	                    	TI.RT ++;
	                    	TI.counter ++; 
	                        int remaining = maxResourceAvailable.get(current.resource_type-1) - current.initial_claim;
	                        maxResourceAvailable.remove(current.resource_type-1);
	                    	maxResourceAvailable.add(current.resource_type-1, remaining);

	                    	int resource = current.initial_claim + TI.resourceInUse.get(current.resource_type-1);
	                    	TI.resourceInUse.remove(current.resource_type-1);
	                    	TI.resourceInUse.add(current.resource_type-1, resource);

	                    	currAllocated[task_num-1][current.resource_type-1] = resource;
	                    } 
	                    else {
	                    	//System.out.println("Unsafe : can't execute Activity currently, will try later");
	                    	TI.waiting = true;
	                    	TI.WT ++;
	                    	keepTrackOfBlocked.put(task_num, new Boolean(false));
	                    	orderOfBlocked.add(task_num);
	                    }
	                    taskInfo.put(task_num, TI);
                    }
            	 }
            	 else if(current.type.equals("release")) {
            	 	//System.out.println("In release");
            	 	if(current.delay != 0){
                        TI.RT ++;
                        current.delay --;
            	 		taskInfo.put(task_num, TI);
	                    
	                    //System.out.println("compute, cycles left = " + current.delay);
            	 	}
            	 	else {
	            	 	TI.waiting = false;
	                    TI.RT ++;
	                    TI.counter ++; 
	                    int remaining = releasedResources.get(current.resource_type-1) + current.initial_claim;
	                    releasedResources.remove(current.resource_type-1);
	                    releasedResources.add(current.resource_type-1, remaining);

	                    int resource = TI.resourceInUse.get(current.resource_type-1) - current.initial_claim;
	                    TI.resourceInUse.remove(current.resource_type-1);
	                    TI.resourceInUse.add(current.resource_type-1, resource);
	                    taskInfo.put(task_num, TI);

	                    currAllocated[task_num-1][current.resource_type-1] = resource;
                    }
            	 }
            	 else { // terminate
            	 	//System.out.println("In terminate");
            	 	if(current.delay != 0){
                        TI.RT ++;
                        current.delay --;
            	 		taskInfo.put(task_num, TI);
	                   
	                    //System.out.println("compute, cycles left = " + current.delay);
            	 	}
            	 	else{
	            	 	TI.waiting = false;
	                    terminate.add(task_num);
	                    TI.counter ++; 
	                    for(int i=0; i < NumberOfResources; i++) {
	                       int resource = TI.resourceInUse.get(i);
	                       TI.resourceInUse.remove(i);
	                       TI.resourceInUse.add(i, 0);

	                       currAllocated[task_num-1][i] = 0;

	                       int remaining = releasedResources.get(i) + resource;
	                       releasedResources.remove(i);
	                       releasedResources.add(i, remaining);
	                    }
	                    taskInfo.put(task_num, TI);
                    }
            	 }

            } // end if
            
      	 }	// end for 

        // finally release resources
        for(int i=0; i < NumberOfResources; i++) {
              int remaining = maxResourceAvailable.get(i) + releasedResources.get(i);
              maxResourceAvailable.remove(i);
              maxResourceAvailable.add(i, remaining);
        }

        
        // release blocked
        for(int i =0; i<NumberOfTasks; i++) { // for
        	int task_num = i+1;
        	if(keepTrackOfBlocked.containsKey(task_num)) {
        		boolean val = keepTrackOfBlocked.get(task_num);
	            if(val == true) {
	            	keepTrackOfBlocked.remove(task_num);
	            	orderOfBlocked.remove(new Integer(task_num));
	            } 
        	}
        }	

      }

         System.out.println("\n\t\tBANKER'S");
         int totalRT = 0;
         int totalWT = 0;
         for(int j = 0; j<NumberOfTasks; j++) {
      	 	int task_num = j+1;
      	 	TaskInfo TI = taskInfo.get(task_num);
      	 	if(!TI.aborted) {
	      	 	totalRT += TI.RT;
	      	 	totalWT += TI.WT;
	      	 	int total = TI.WT + TI.RT;
	      	 	float waitpercent = TI.WT; 
	      	 	waitpercent = waitpercent / total; 
	      	 	waitpercent = waitpercent * 100;
	      	 	System.out.println("Task "+ task_num +"\t" + total +"\t" + TI.WT +"\t" + waitpercent+"%");
      	    }
      	    else {
      	    	System.out.println("Task "+ task_num +"\taborted");
      	    }
      	 }	
         
         float totalwaitpercent = 0;
         int totalTime = totalRT+totalWT;
         if(totalWT != 0) {
            totalwaitpercent = totalWT;
	      	totalwaitpercent = totalwaitpercent / totalTime; 
	      	totalwaitpercent = totalwaitpercent * 100;
         }
         
      	 System.out.println("total\t" + totalTime +"\t" + totalWT +"\t" + totalwaitpercent + "%");


     
   } // Banker


   public boolean isSafe(Activity current, int[][] maxClaim, int[][] currAllocated, ArrayList<Integer> maxResourceAvailable) {

   	  boolean safe = true;

   	  if(!current.type.equals("request")) return true;

   	  // 1 : alter currAlocated based on current
   	  // hypthetically allocating resources to check if its safe or not
      currAllocated[current.task_num-1][current.resource_type-1] = currAllocated[current.task_num-1][current.resource_type-1] + current.initial_claim;

      int left = maxResourceAvailable.get(current.resource_type-1) - current.initial_claim;
	  maxResourceAvailable.remove(current.resource_type-1);
      maxResourceAvailable.add(current.resource_type-1, left);


   	  // 2 : find need = maxClaim - currAllocated
   	   int[][] need = new int[NumberOfTasks][NumberOfResources];
	
       for(int i = 0; i<NumberOfTasks; i++) {
       	   for(int j = 0; j<NumberOfResources; j++){
       	   	   need[i][j] = maxClaim[i][j] - currAllocated[i][j];
       	   }  
       }

   	  // 3 : 

      ArrayList<Integer> terminate = new  ArrayList<Integer>(); 
   	  while(terminate.size() <  NumberOfTasks) {
   	  	   // a: find a task (Index of Row in need) that can be serviced
   	  	   int R = -1;
   	  	   for(int i = 0; i<NumberOfTasks; i++) {
   	  	       
   	  	       if(aborted.containsKey(i+1)) {
   	  	          // if this task has been aborted forget about this task
   	  	          terminate.add(i);
   	  	          continue;
   	  	       }
   	  	       
   	  	   	   if(terminate.contains(new Integer(i))) {
   	  	   	   	   // this task has already terminated we can not pick it for servicing
   	  	   	   	   continue;
   	  	   	   }
               
               boolean canBeServiced = true;
               for(int j = 0; j<NumberOfResources; j++){
       	   	       if (need[i][j] > maxResourceAvailable.get(j)) {
       	   	       	   canBeServiced = false;
       	   	       }
       	       } 

       	       if(!canBeServiced) {
       	       	  continue;
       	       }	

       	       R = i;
       	       break;
   	  	   }	

           if (R == -1) {
           	  // could not find a task that can be finished 
           	  // therefore this state is unsafe
           	  safe = false;
           	  return false; 
           } 

   	  	   // b: finish it and release resources held by that task
            
	        
	        for(int i=0; i < NumberOfResources; i++) {
	        	  int remaining = maxResourceAvailable.get(i) + currAllocated[R][i];
	        	  maxResourceAvailable.remove(i);
                  maxResourceAvailable.add(i, remaining);
	              
	              currAllocated[R][i] = 0;
	              terminate.add(R);
	        }
	                
   	  }

   	  return true;
   }
  
   public void FIFO() {
 
      ArrayList<Integer> terminate = new  ArrayList<Integer>();
      ArrayList<Integer> orderOfBlocked = new  ArrayList<Integer>();
      HashMap<Integer,Boolean> keepTrackOfBlocked = new HashMap<Integer,Boolean>();

      int globaltime = 0;
      while(terminate.size() <  NumberOfTasks) {
         //System.out.println("cycle = " +  globaltime + "-" + (globaltime+1));
      	 globaltime ++ ;

      	 boolean deadlock = true;

      	 ArrayList<Integer> releasedResources = new ArrayList<Integer>();
      	 for(int i=0; i < NumberOfResources; i++) {
              releasedResources.add(0);
         }

         //first service blocked
        
         for(int j = 0; j< orderOfBlocked.size(); j++) { // for
         	int task_num = orderOfBlocked.get(j);
      	 	TaskInfo TI = taskInfo.get(task_num);
      	 	boolean flag = false;

      	 	//System.out.println("\nCurrent Task Info = "+ TI);

            if(TI.counter < TI.numberOfActivities && !TI.aborted) { // if
                 // Execute 
            	 Activity current = input.get(task_num).get(TI.counter);

                 //System.out.println("executing Activity = " +  current);
            	 if(current.type.equals("initiate")) {
            	 	 //System.out.println("In initiate");
            	 	 TI.waiting = false;
                    TI.RT ++;
                    TI.counter ++;
                    taskInfo.put(task_num, TI); 
                    flag = true;
            	 }
            	 else if(current.type.equals("request")) {
            	 	//System.out.println("In request");
            	 	if(current.delay != 0){
                        TI.RT ++;
                        current.delay --;
            	 		taskInfo.put(task_num, TI);
	                    flag = true;
	                    //System.out.println("compute, cycles left = " + current.delay);
            	 	}
            	 	else{
	            	 	if(maxResourceAvailable.get(current.resource_type-1) >= current.initial_claim) {
	                    	TI.waiting = false;
	                    	TI.RT += (current.delay + 1);
	                    	TI.counter ++; 
	                        int remaining = maxResourceAvailable.get(current.resource_type-1) - current.initial_claim;
	                        maxResourceAvailable.remove(current.resource_type-1);
	                    	maxResourceAvailable.add(current.resource_type-1, remaining);

	                    	int resource = current.initial_claim + TI.resourceInUse.get(current.resource_type-1);
	                    	TI.resourceInUse.remove(current.resource_type-1);
	                    	TI.resourceInUse.add(current.resource_type-1, resource);
	                    	flag = true;
	                    } 
	                    else {
	                    	//System.out.println("can't execute Activity currently, will try later");
	                    	TI.waiting = true;
	                    	TI.WT ++;
	                    	flag = false;
	                    }
	                    taskInfo.put(task_num, TI);

	            	}
	                    
            	 }
            	 else if(current.type.equals("release")) {
            	 	//System.out.println("In release");
            	 	if(current.delay != 0){
                        TI.RT ++;
                        current.delay --;
            	 		taskInfo.put(task_num, TI);
	                    flag = true;
	                    //System.out.println("compute, cycles left = " + current.delay);
            	 	}
            	 	else {
	            	 	TI.waiting = false;
	                    TI.RT ++;
	                    TI.counter ++; 
	                    int remaining = releasedResources.get(current.resource_type-1) + current.initial_claim;
	                    releasedResources.remove(current.resource_type-1);
	                    releasedResources.add(current.resource_type-1, remaining);

	                    int resource = TI.resourceInUse.get(current.resource_type-1) - current.initial_claim;
	                    TI.resourceInUse.remove(current.resource_type-1);
	                    TI.resourceInUse.add(current.resource_type-1, resource);
	                    taskInfo.put(task_num, TI);

	                    flag = true;
                    }
            	 }
            	 else { // terminate
            	 	//System.out.println("In terminate");

            	 	if(current.delay != 0){
                        TI.RT ++;
                        current.delay --;
            	 		taskInfo.put(task_num, TI);
	                    flag = true;
	                    //System.out.println("compute, cycles left = " + current.delay);
            	 	}
            	 	else{
            	 		TI.waiting = false;
	                    terminate.add(task_num);
	                    TI.counter ++; 
	                    for(int i=0; i < NumberOfResources; i++) {
	                       int resource = TI.resourceInUse.get(i);
	                       TI.resourceInUse.remove(i);
	                       TI.resourceInUse.add(i, 0);

	                       int remaining = releasedResources.get(i) + resource;
	                       releasedResources.remove(i);
	                       releasedResources.add(i, remaining);
	                    }
	                    taskInfo.put(task_num, TI);

	                    flag = true;

            	 	}	
            	 }

            	 if(!TI.waiting) { deadlock = false; }
            } // end if

            keepTrackOfBlocked.put(task_num, new Boolean(flag)); 
              
         } // end for

         //System.out.println("Execute the rest ");

      	 for(int j = 0; j<NumberOfTasks; j++) { // for 

      	 	int task_num = j+1;	
      	 	if(keepTrackOfBlocked.containsKey(task_num)) {
      	 		continue;
      	 	}
      	 
      	 	TaskInfo TI = taskInfo.get(task_num);

      	 	//System.out.println("\nCurrent Task Info = "+ TI);

            if(TI.counter < TI.numberOfActivities && !TI.aborted) { // if
                 // Execute 
            	 Activity current = input.get(task_num).get(TI.counter);

                 //System.out.println("executing Activity = " +  current);
            	 if(current.type.equals("initiate")) {
            	 	 //System.out.println("In initiate");
            	 	TI.waiting = false;
                    TI.RT ++;
                    TI.counter ++;
                    taskInfo.put(task_num, TI); 
            	 }
            	 else if(current.type.equals("request")) {
            	 	//System.out.println("In request");
            	 	if(current.delay != 0){
                        TI.RT ++;
                        current.delay --;
            	 		taskInfo.put(task_num, TI);
	                    //System.out.println("compute, cycles left = " + current.delay);
            	 	}
            	 	else{
	                    if(maxResourceAvailable.get(current.resource_type-1) >= current.initial_claim) {
	                    	TI.waiting = false;
	                    	TI.RT ++;
	                    	TI.counter ++; 
	                        int remaining = maxResourceAvailable.get(current.resource_type-1) - current.initial_claim;
	                        maxResourceAvailable.remove(current.resource_type-1);
	                    	maxResourceAvailable.add(current.resource_type-1, remaining);

	                    	int resource = current.initial_claim + TI.resourceInUse.get(current.resource_type-1);
	                    	TI.resourceInUse.remove(current.resource_type-1);
	                    	TI.resourceInUse.add(current.resource_type-1, resource);
	                    } 
	                    else {
	                    	//System.out.println("can't execute Activity currently, will try later");
	                    	TI.waiting = true;
	                    	TI.WT ++;
	                    	keepTrackOfBlocked.put(task_num, new Boolean(false));
	                    	orderOfBlocked.add(task_num);
	                    }
	                    taskInfo.put(task_num, TI);
                    }
            	 }
            	 else if(current.type.equals("release")) {
            	 	//System.out.println("In release");
            	 	if(current.delay != 0){
                        TI.RT ++;
                        current.delay --;
            	 		taskInfo.put(task_num, TI);
	                    
	                    //System.out.println("compute, cycles left = " + current.delay);
            	 	}
            	 	else {
	            	 	TI.waiting = false;
	                    TI.RT ++;
	                    TI.counter ++; 
	                    int remaining = releasedResources.get(current.resource_type-1) + current.initial_claim;
	                    releasedResources.remove(current.resource_type-1);
	                    releasedResources.add(current.resource_type-1, remaining);

	                    int resource = TI.resourceInUse.get(current.resource_type-1) - current.initial_claim;
	                    TI.resourceInUse.remove(current.resource_type-1);
	                    TI.resourceInUse.add(current.resource_type-1, resource);
	                    taskInfo.put(task_num, TI);
                    }
            	 }
            	 else { // terminate
            	 	//System.out.println("In terminate");
            	 	if(current.delay != 0){
                        TI.RT ++;
                        current.delay --;
            	 		taskInfo.put(task_num, TI);
	                   
	                    //System.out.println("compute, cycles left = " + current.delay);
            	 	}
            	 	else{
	            	 	TI.waiting = false;
	                    terminate.add(task_num);
	                    TI.counter ++; 
	                    for(int i=0; i < NumberOfResources; i++) {
	                       int resource = TI.resourceInUse.get(i);
	                       TI.resourceInUse.remove(i);
	                       TI.resourceInUse.add(i, 0);

	                       int remaining = releasedResources.get(i) + resource;
	                       releasedResources.remove(i);
	                       releasedResources.add(i, remaining);
	                    }
	                    taskInfo.put(task_num, TI);
                    }
            	 }

            	 if(!TI.waiting) { deadlock = false; }
            } // end if
            
      	 }	// end for 

      	 while(deadlock) { //while
      	 	// abort lowest waiting process
      	 	for(int j = 0; j<NumberOfTasks; j++) {
      	 	   int task_num = j+1;
      	 	   TaskInfo TI = taskInfo.get(task_num);
      	 	   if(!TI.aborted && TI.waiting) {
      	 	   	   //System.out.println("aborting "+task_num);
      	 	   	   for(int i=0; i < NumberOfResources; i++) {
                       int resource = TI.resourceInUse.get(i);
                       TI.resourceInUse.remove(i);
                       TI.resourceInUse.add(i, 0);
                       int remaining = maxResourceAvailable.get(i) + resource;
                       maxResourceAvailable.remove(i);
                       maxResourceAvailable.add(i, remaining);
                   }
      	 	   	   TI.waiting = false;
      	 	   	   TI.aborted = true;
      	 	   	   terminate.add(task_num);
                   break;
      	 	   }
      	 	}  
      	 	// check if deadlock still exists 
      	 	for(int j = 0; j<NumberOfTasks; j++) {
	      	 	int task_num = j+1;
	      	 	TaskInfo TI = taskInfo.get(task_num);
	            if(TI.counter < TI.numberOfActivities && !TI.aborted) {
	            	 Activity current = input.get(task_num).get(TI.counter);
	            	 if(current.type.equals("request") && (maxResourceAvailable.get(current.resource_type-1) < current.initial_claim)) {
	                    	deadlock = true;
	            	 }
	            	 else {
	            	 	    deadlock = false;
	            	 	    break;
	            	 }
	        
	            } 
      	    }
        } // end while

        // finally release resources
        for(int i=0; i < NumberOfResources; i++) {
              int remaining = maxResourceAvailable.get(i) + releasedResources.get(i);
              maxResourceAvailable.remove(i);
              maxResourceAvailable.add(i, remaining);
        }

        
        // release blocked
        for(int i =0; i<NumberOfTasks; i++) { // for
        	int task_num = i+1;
        	if(keepTrackOfBlocked.containsKey(task_num)) {
        		boolean val = keepTrackOfBlocked.get(task_num);
	            if(val == true) {
	            	keepTrackOfBlocked.remove(task_num);
	            	orderOfBlocked.remove(new Integer(task_num));
	            } 
        	}
        }	

      }

       
        //System.out.println("taskInfo = " +  taskInfo);

         System.out.println("\n\t\tFIFO");
         int totalRT = 0;
         int totalWT = 0;
         for(int j = 0; j<NumberOfTasks; j++) {
      	 	int task_num = j+1;
      	 	TaskInfo TI = taskInfo.get(task_num);
      	 	if(!TI.aborted) {
	      	 	totalRT += TI.RT;
	      	 	totalWT += TI.WT;
	      	 	int total = TI.WT + TI.RT;
	      	 	float waitpercent = TI.WT; 
	      	 	waitpercent = waitpercent / total; 
	      	 	waitpercent = waitpercent * 100;
	      	 	System.out.println("Task "+ task_num +"\t" + total +"\t" + TI.WT +"\t" + waitpercent+"%");
      	    }
      	    else {
      	    	System.out.println("Task "+ task_num +"\taborted");
      	    }
      	 }	
         
         float totalwaitpercent = 0;
         int totalTime = totalRT+totalWT;
         if(totalWT != 0) {
            totalwaitpercent = totalWT;
	      	totalwaitpercent = totalwaitpercent / totalTime; 
	      	totalwaitpercent = totalwaitpercent * 100;
         }
         
      	 System.out.println("total\t" + totalTime +"\t" + totalWT +"\t" + totalwaitpercent + "%");


     
   } // FIFO

} // Lab 3

