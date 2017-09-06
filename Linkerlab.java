import java.util.ArrayList;
import java.util.Hashtable;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Scanner;

class Linker {

    private String filename;
    private Hashtable<String,Integer> SymTab = new Hashtable<String,Integer>();
    private Hashtable<String,ArrayList<Integer>> UseList =  new Hashtable<String,ArrayList<Integer>>();
    private Hashtable<String,Hashtable<Integer,ArrayList<Integer>>> ErrorCheckUseList =  new Hashtable<String,Hashtable<Integer,ArrayList<Integer>>>();
    private ArrayList<Integer> BaseAddr = new ArrayList<Integer>();
    private Hashtable<Integer,ArrayList<String>> Program = new Hashtable<Integer,ArrayList<String>>(); 
    private int NumberOfModules;
    private Hashtable<String,String> SymDefError = new Hashtable<String,String> ();
    private Hashtable<String,Integer> SymModule = new Hashtable<String,Integer> ();

    public Linker (String fname) {
        filename = fname;
    }

	public static void main(String[] args) throws FileNotFoundException {
		 if(args.length != 1) {
		 	System.out.println("Function takes filename as an argument");
		 	return;
		 }
         Linker myLinker = new Linker(args[0]);
         myLinker.Pass1();
         myLinker.Pass2();
	}

	public void Pass1() throws FileNotFoundException {
        File f = new File(filename);
        if(!f.exists()){
            throw new FileNotFoundException ("Did not find "+filename);
        }

        Scanner scan = new Scanner(f);

        
        //BufferedReader br = new BufferedReader(new FileReader(f));
        //String line = null;

        NumberOfModules = scan.nextInt();
       
        BaseAddr.add(0);
        int moduleNo = 0;

        while(scan.hasNext()){
            
            //def list
            int numOfSymbols = scan.nextInt();
            for(int i=0; i < numOfSymbols; i++)
            {
                String symName = scan.next();
                int symVal = scan.nextInt();
                if(!SymTab.containsKey(symName)) {
                   SymTab.put(symName,symVal+BaseAddr.get(moduleNo));
                   SymModule.put(symName,moduleNo);
                }
                else
                   SymDefError.put(symName, "Error: This variable is multiply defined; first value used.");  
            }    
            
            // use list
            int numOfUseCases = scan.nextInt();

            for(int i=0; i < numOfUseCases; i++)
            {
               String symName = scan.next();
               ArrayList<Integer> newList;
               ArrayList<Integer> ErrorCheck_newList = new ArrayList<Integer>();
               if(UseList.containsKey(symName)){
                  newList =  UseList.get(symName);
               }
               else {
                  newList = new ArrayList<Integer>();
               }

               int val;
               while((val = scan.nextInt()) != -1) {
                  newList.add(val+BaseAddr.get(moduleNo)); 
                  ErrorCheck_newList.add(val);
               }
               UseList.put(symName,newList);
            
               Hashtable<Integer,ArrayList<Integer>> currentTab = new Hashtable<Integer,ArrayList<Integer>>(); 
               if(ErrorCheckUseList.containsKey(symName)){
                 currentTab = ErrorCheckUseList.get(symName);
               }
               currentTab.put(moduleNo,ErrorCheck_newList);
               ErrorCheckUseList.put(symName,currentTab);
            } 

            // program text
            int noOfLines = scan.nextInt();
            BaseAddr.add(BaseAddr.get(moduleNo) + noOfLines);
            ArrayList<String> currentProgText = new ArrayList<String>();
            for(int i=0; i < noOfLines; i++) {
                currentProgText.add(scan.next() + " " +scan.nextInt());
            }
            Program.put(moduleNo,currentProgText);

            moduleNo++;
        }        
	}

	public void Pass2() {
        System.out.println("Symbol Table");
        for(String key : SymTab.keySet())
        {
            if(!(SymTab.get(key) >= BaseAddr.get(SymModule.get(key)) && SymTab.get(key) < BaseAddr.get(SymModule.get(key) +1))){
                SymTab.put(key,BaseAddr.get(SymModule.get(key)));
                System.out.print(key + "=" +SymTab.get(key));
                System.out.println(" Error: Definition exceeds module size; first word in module used.");
            }
            else{
              System.out.print(key + "=" +SymTab.get(key));
              if(SymDefError.containsKey(key)) {
                  System.out.println(" "+SymDefError.get(key));
              }
              else System.out.println("");
            }  
        }    
        
        System.out.println("\nMemory Map");
        int lineNo = 0;
        for(int i=0; i<NumberOfModules; i++) {
           ArrayList<String> currentProgText = Program.get(i);
           for(String line : currentProgText) {
               String[] contents = line.split(" ");
               System.out.print(lineNo + ":\t");
               if(contents[0].equals("I")) {
                    System.out.println(contents[1]);
               }
               else if(contents[0].equals("A")) {
                    int last3digits = Integer.parseInt(contents[1]) % 1000;  
                    if(last3digits >= 200) {
                        int newAddr = (Integer.parseInt(contents[1])/1000) *1000;  
                        System.out.print(newAddr);
                        System.out.println(" Error: Absolute address exceeds machine size; zero used.");
                    }
                    else {
                        System.out.println(contents[1]);
                    }
               }     
               else if(contents[0].equals("R")) {
                    int val = Integer.parseInt(contents[1]) + BaseAddr.get(i);
                    int last3digits = val % 1000;  
                    if(last3digits >= BaseAddr.get(i+1)) {
                        int newAddr = (val/1000) *1000;  
                        System.out.print(newAddr); 
                        System.out.println(" Error: Relative address exceeds module size; zero used.");
                    }
                    else {
                        System.out.println(val);
                    }
               }
               else
               { 
                   boolean MultipleDefinedflag = false; 
                   int newAddr = (Integer.parseInt(contents[1])/1000) *1000;  
                   String error= null;
                   for(String key : UseList.keySet())  {
                      //System.out.println("Checking in "+key+"line Number "+lineNo);
                      ArrayList<Integer> currentList = UseList.get(key);
                      if(currentList.contains(lineNo)) {
                         //System.out.println("Here "+key);
                         if(!MultipleDefinedflag){
                           MultipleDefinedflag = true;
                           if(SymTab.containsKey(key)) {
                             int val = SymTab.get(key);
                             //System.out.println("val="+val);
                             newAddr = newAddr + val;
                           } 
                           else {
                             error = "Error: "+ key +" is not defined; zero used."; 
                           }
                         }
                         else { //Multiply defined
                             error = "Error: Multiple variables used in instruction; all but first ignored.";
                         }
                      }  
                   } 

                   System.out.print(newAddr);
                   if(error!= null) {
                      System.out.println(" "+error);
                   }
                   else { System.out.println(""); }
               } 
               lineNo++;
           }
        }
        
        System.out.println("");
        for(String key : SymTab.keySet()) {
            if(!UseList.containsKey(key)) {
                System.out.println("Warning: "+key+" was defined in module "+ (SymModule.get(key)+1) +" but never used.");
            }
        }
        
        for(String key : SymTab.keySet()) {
           if(!ErrorCheckUseList.containsKey(key)) continue;
           Hashtable<Integer,ArrayList<Integer>> currentTab = ErrorCheckUseList.get(key);
           for(int i=0; i<NumberOfModules; i++) {
              if(!currentTab.containsKey(i)) continue;
              ArrayList<Integer> currentList = currentTab.get(i);
              for(Integer val : currentList){
                int currAddr = val + BaseAddr.get(i);
                if(!(currAddr >= BaseAddr.get(i) && currAddr < BaseAddr.get(i+1)))
                  System.out.println("Error: Use of "+ key +" in module "+ (i+1) +" exceeds module size; use ignored.");
              }   
           }
        } 
	}
}