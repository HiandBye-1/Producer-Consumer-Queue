package checker;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import src.FileValidator;
import java.util.NoSuchElementException;
import java.util.HashMap;
import java.lang.IllegalArgumentException;
import java.util.HashSet;
import java.util.ArrayDeque;

public class TraceChecker {
    public static void main(String[] args) throws Exception {
        if (args.length != 2) throw new Exception("Wrong number of arguments");

        HashMap<String, Integer> map;
        try{//the config
            map = FileValidator.FileInput(args[0]);
        }catch(FileNotFoundException e){
            throw new FileNotFoundException("File does not exist");
        }

        try(Scanner in1 = new Scanner(new File(args[1]))){//the trace files
            if(!in1.hasNextLine()) {
                throw new IllegalArgumentException("Trace file is empty");
            }
            if(in1.nextLine().equals("seq,actor,event,job_id,occupancy,result")){
                System.out.println("Header is valid");
            }else{
                throw new Exception("File at path "+args[1]+" is invalid(due to file content)");
            }

            
            int line = 1;
            HashSet<Integer> dupeID= new HashSet<>();
            HashSet<Integer> deqID= new HashSet<>();
            HashSet<Integer> doneID = new HashSet<>();
            ArrayDeque<String> fifoQueue = new ArrayDeque<>();
            HashMap<String, Integer> consumersJob= new HashMap<>();
            HashSet<String> stopID = new HashSet<>();
            HashSet<String> stopRetrieve= new HashSet<>();
            HashSet<String> stoppedConsumers = new HashSet<>();

            int currentOccu = 0;
            int maxOccu = 0;
            long resultSum = 0;
            boolean stopStarted = false;
            long expectedSum = 0;

            for (int jobId = 1; jobId <= map.get("jobs"); jobId++) {
                int payload = (37 * jobId + 11 * map.get("seed")) % 1000;
                int units = 1 + ((jobId + map.get("seed")) % 5);
                expectedSum += (long) payload * units + jobId;
            }

            while(in1.hasNextLine()){
                String nextLine = in1.nextLine();

                if(nextLine.isBlank()) continue;

                String[] row = nextLine.split(",", -1);
                if(!validRow(row, line, map)) {
                    System.out.println("FAIL");
                    System.out.println("event=" + line + ", reason=Invalid row format or field value");
                    System.exit(1);
                }

                try{
                if(row[1].startsWith("C") && stoppedConsumers.contains(row[1])) {
                    throw new IllegalArgumentException("Consumer acted after receiving STOP at line " + line);
                }
                if(row[2].equals("ENQ")){
                    try{
                        if(stopStarted) throw new IllegalArgumentException("Job enqueue after STOP began at line "+line);
                        if(dupeID.add(Integer.parseInt(row[3]))){}
                        else throw new IllegalArgumentException("Duplicate ENQ job ID");
                        fifoQueue.add(row[3]);
                        currentOccu++;
                        maxOccu = Math.max(maxOccu, currentOccu);
                        if(currentOccu != Integer.parseInt(row[4])) throw new IllegalArgumentException("File at path"+args[1]+" at Line "+line+" is invalid");
                        if((Integer.parseInt(row[3])-1)% map.get("producers") != Integer.parseInt(row[1].substring(1, row[1].length()))) throw new IllegalArgumentException("File at path"+args[1]+" at Line "+line+" is invalid");
                    }catch(NumberFormatException e){
                        throw new IllegalArgumentException("File at path"+args[1]+" at Line "+line+" is invalid");
                    }
                }

                if(row[2].equals("DEQ")){
                    try{
                        int num = Integer.parseInt(row[3]);
                        if(consumersJob.getOrDefault(row[1], -1) != -1){
                            throw new IllegalArgumentException("File at path"+args[1]+" at Line "+line+" is invalid");
                        }else consumersJob.put(row[1], num);
                        if(deqID.contains(num)) throw new IllegalArgumentException("File at path"+args[1]+" at Line "+line+" is invalid");
                        if(!(dupeID.contains(num)&& deqID.add(num))) throw new IllegalArgumentException("File at path"+args[1]+" at Line "+line+" is invalid");
                        if(!fifoQueue.pop().equals(row[3])) throw new NoSuchElementException("File at path"+args[1]+" at Line "+line+" is invalid");
                        currentOccu--;
                        if(currentOccu != Integer.parseInt(row[4])) throw new IllegalArgumentException("File at path"+args[1]+" at Line "+line+" is invalid");
                    }catch(NumberFormatException e){
                        throw new IllegalArgumentException("File at path"+args[1]+" at Line "+line+" is invalid");
                    }
                }

                if(row[2].equals("DONE")){
                    try{
                        int num = Integer.parseInt(row[3]);
                        int currentJob = consumersJob.getOrDefault(row[1], -1);
                        if(currentJob == -1){//doesn't exists
                            throw new IllegalArgumentException("File at path"+args[1]+" at Line "+line+" is invalid");
                        }else{
                            if(currentJob != num) throw new IllegalArgumentException("File at path"+args[1]+" at Line "+line+" is invalid");
                            else consumersJob.remove(row[1]);//remove
                        }
                        if(!(deqID.contains(num) && doneID.add(num))) throw new IllegalArgumentException("File at path"+args[1]+" at Line "+line+" is invalid");
                        resultSum += Long.parseLong(row[5]);
                        if(currentOccu != Integer.parseInt(row[4])) throw new IllegalArgumentException("File at path"+args[1]+" at Line "+line+" is invalid");
                    }catch(NumberFormatException e){
                        throw new IllegalArgumentException("File at path"+args[1]+" at Line "+line+" is invalid");
                    }

                }

                if(row[2].equals("STOP_ENQ")){
                    stopStarted = true;
                    if(!stopID.add(row[3])){
                        throw new IllegalArgumentException("Duplicate stop enqueue at line " + line);
                    }

                    currentOccu++;
                    maxOccu = Math.max(maxOccu, currentOccu);
                    fifoQueue.add(row[3]);
                    if(currentOccu != Integer.parseInt(row[4])) throw new IllegalArgumentException("File at path"+args[1]+" at Line "+line+" is invalid");
                }

                if(row[2].equals("STOP_GET")){
                    if(!stopRetrieve.add(row[3])){
                        throw new IllegalArgumentException("Duplicate STOP retrieve at line " + line);
                    }
                    if(!stopID.contains(row[3])){
                        throw new IllegalArgumentException("File at path"+args[1]+" at Line "+line+" is invalid");
                    
                    }
                    if(!fifoQueue.pop().equals(row[3])) throw new NoSuchElementException("STOP marker out of FIFO order at Line "+line);
                    if(consumersJob.containsKey(row[1])) {
                        throw new IllegalArgumentException("Consumer retrieved STOP with an unfinished job at line " + line);
                    }
                    if(!stoppedConsumers.add(row[1])) {
                        throw new IllegalArgumentException("Consumer retrieved more than one STOP marker at line " + line);
                    }
                    currentOccu--;
                    if(currentOccu != Integer.parseInt(row[4])) throw new IllegalArgumentException("File at path"+args[1]+" at Line "+line+" is invalid");
                }
            }catch(IllegalArgumentException | NoSuchElementException e){
                System.out.println("FAIL");
                System.out.println("event=" + line + ", reason=" + e.getMessage());
                System.exit(1);
            }
                line++;
            }

            try{
                
            if(dupeID.size() != map.get("jobs") ||
               deqID.size() != map.get("jobs") ||
               doneID.size() != map.get("jobs") ||
               stopID.size() != map.get("consumers") ||
               stopRetrieve.size() != map.get("consumers")|| 
               stoppedConsumers.size() != map.get("consumers")){
                throw new IllegalArgumentException("File at path"+args[1]+" is invalid(due to file content)");
                
                }
            if(!fifoQueue.isEmpty()) {
                throw new IllegalArgumentException("FIFO queue is not empty at EOF");
            }

            if(currentOccu != 0) {
                throw new IllegalArgumentException("Occupancy is not zero at EOF");
            }

            if(!consumersJob.isEmpty()) {
                throw new IllegalArgumentException("A consumer still has an unfinished job at EOF");
            }

            if(resultSum != expectedSum) {
                throw new IllegalArgumentException("Result sum mismatch at EOF");
            }
            }catch(IllegalArgumentException e){
                    System.out.println("FAIL");
                    System.out.println("event=EOF, reason=" + e.getMessage());
                    System.exit(1);
            }


            
            //proof
            System.out.println("PASS");
            System.out.println("jobs=" + map.get("jobs"));
            System.out.println("produced=" + dupeID.size());
            System.out.println("dequeued=" + deqID.size());
            System.out.println("completed=" + doneID.size());
            System.out.println("result_sum=" + resultSum);
            System.out.println("max_occupancy=" + maxOccu);

        }catch(FileNotFoundException e){
            throw new FileNotFoundException("File does not exist");
        }catch(NoSuchElementException e){
            throw new NoSuchElementException("File at path"+args[1]+" is invalid(due to file content)");
        }

        


    }

    public static boolean validRow(String[] row, int line, HashMap<String, Integer> map) throws Exception{
        if(row.length != 6) return false;

        //check seq
        try{
            int seq = Integer.parseInt(row[0]);
            if(!(seq == line)) return false;
        }catch(NumberFormatException e){
            return false;
        }

        
        //check actor
        if(!row[1].equals("MAIN")){
            if(row[1].length() < 2) return false;
            if(!row[1].substring(0,1).equals("P") && !row[1].substring(0,1).equals("C")) return false;//if producer or consumer
            int id=0;

            try{
               id = Integer.parseInt(row[1].substring(1));//for p and c
            }catch(NumberFormatException e){
                return false;
            }

            if(row[1].substring(0,1).equals("P")){//if producer
                if(id < 0 ||id >= map.get("producers")) return false;
            }else if(row[1].substring(0,1).equals("C")){//if consumer
                if(id < 0 || id >= map.get("consumers")) return false;
            }
        }

        
        //check event
        if(!row[2].equals("ENQ") && !row[2].equals("DEQ") && 
           !row[2].equals("DONE")&& !row[2].equals("STOP_ENQ") && 
           !row[2].equals("STOP_GET")) return false;
        
        //check job_id
        boolean isStopId = row[3].startsWith("STOP");
        boolean isStopEvent = row[2].equals("STOP_ENQ") || row[2].equals("STOP_GET");
        
        if(isStopId != isStopEvent) return false;
        if(row[3].startsWith("STOP")){
            try{
                int check = Integer.parseInt(row[3].substring(4));
                if(check <= 0 || check > map.get("consumers")) return false;
            }catch(NumberFormatException e){
                return false;
            }
        }else{
            try{
                int check = Integer.parseInt(row[3]);
                if(check <= 0 || check > map.get("jobs")) return false;
            }catch(NumberFormatException e){
                return false;
            }
        }

        //check occupancy
        try{
            int check = Integer.parseInt(row[4]);
            if(check < 0 || check > map.get("capacity")) return false;
        }catch(NumberFormatException e){
            return false;
        }

        //check result

        if(row[2].equals("STOP_ENQ") || row[2].equals("STOP_GET")){
            if(!row[5].equals("null")) return false;
        }else{
            try{
            int actualResult = Integer.parseInt(row[5]);

            int jobId = Integer.parseInt(row[3]);
            int seed = map.get("seed");

            int payload = (37 * jobId + 11 * seed) % 1000;
            int units = 1 + ((jobId + seed) % 5);
            int expectedResult = payload * units + jobId;

            if(actualResult != expectedResult) return false;
            }catch(NumberFormatException e){
                return false;
            }
        }

        //validte ending
        if(row[2].equals("ENQ") && !row[1].substring(0,1).equals("P")) return false;
        if(row[2].equals("DEQ") && !row[1].substring(0,1).equals("C")) return false;
        if(row[2].equals("DONE") && !row[1].substring(0,1).equals("C")) return false;
        if(row[2].equals("STOP_ENQ") && !row[1].equals("MAIN")) return false;
        if(row[2].equals("STOP_GET") && !row[1].substring(0,1).equals("C")) return false;

        return true;
    }

}

