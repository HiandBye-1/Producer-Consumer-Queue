package src;
import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;


public class Main {
    public static void main(String[] args) throws Exception {
        
        if(args.length != 2 ) throw new Exception("Wrong number of arguments");
        if(!args[1].equals("cv") && !args[1].equals("sem")) throw new Exception("Invalid Mode");
        

        HashMap<String, Integer> map = FileValidator.FileInput("config/"+args[0]+".txt");

        FIFO fifo = null;
        SemaphoreFIFO semaphoreFIFO = null;

        if(args[1].equals("cv")){
            fifo = new FIFO(map.get("capacity"));
        }else{
            semaphoreFIFO = new SemaphoreFIFO(map.get("capacity"));
        }

       
        Thread[] array = new Thread[map.get("consumers")];

        long time = System.currentTimeMillis();
        for(int i = 0 ; i < array.length; i++){//consumers
            if(fifo == null) array[i] = new Thread(new Consumer(semaphoreFIFO, i, map.get("delay_ms")));
            else array[i] = new Thread(new Consumer(fifo, i, map.get("delay_ms")));

            array[i].start();
        }

        Thread[] array2 = new Thread[map.get("producers")];
        for(int i = 0 ; i < array2.length; i++){//producers
            if (fifo == null) array2[i] = new Thread(new Producer(semaphoreFIFO, map.get("producers"), map.get("jobs"), map.get("seed"), i));
            else array2[i] = new Thread(new Producer(fifo, map.get("producers"), map.get("jobs"), map.get("seed"), i));
            array2[i].start();
        }

        for(int i = 0 ; i < array2.length; i++){//join producers
            array2[i].join();
        }

        int stop = 1;
        for(int i = 0 ; i < map.get("consumers"); i++){//join consumers
            if(fifo == null) semaphoreFIFO.put(new Job(0, 0, 0, 0, true, "STOP"+stop), "MAIN");
            else fifo.put(new Job(0, 0, 0, 0, true, "STOP"+stop), "MAIN");
            stop++;
        }

        for(int i = 0 ; i < array.length; i++){//join consumers
            array[i].join();
        }

        
        int jobs = map.get("jobs");
        int produced = 0;
        int dequeued = 0;
        int completed = 0;
        int result_sum = 0;


        System.out.printf("%-5s | %-5s | %-10s | %-7s | %-9s | %-6s%n",
                          "seq", "actor", "event", "job_id", "occupancy", "result");
        System.out.println("-----------------------------------------------");
        

        ArrayList<Event> log;
        if(fifo == null) log = semaphoreFIFO.getEvents();
        else log = fifo.getEvents();

        for(int i = 0; i < log.size(); i++){
            Event event = log.get(i);
            System.out.printf("%-5d | %-5s | %-10s | %-7s | %-9d | %-6s%n",
            event.getSeq(),
            event.getActor(),
            event.getEvent(),
            event.getJobId(),
            event.getOccupancy(),
            event.getResult());
            if(event.getEvent().equals("ENQ")) produced++;
            if(event.getEvent().equals("DEQ")) dequeued++;
            if(event.getEvent().equals("DONE")) completed++;
            if(event.getResult() != null && event.getEvent().equals("DONE")) result_sum += event.getResult();
        }

        //statistics
        System.out.println("-----------------------------------------------");
        System.out.println("Elapsed_ms: "+(System.currentTimeMillis() - time)+" ms");
        System.out.println("Jobs: "+jobs);
        System.out.println("Produced: "+produced);
        System.out.println("Dequeued: "+dequeued);
        System.out.println("Completed: "+completed);
        System.out.println("Result_sum: "+result_sum);
        if(fifo == null) System.out.println("Max_occupancy: "+ semaphoreFIFO.getMaxOccupancy());
        else System.out.println("Max_occupancy: "+ fifo.getMaxOccupancy());

        //separate sem and cv mode
        try(BufferedWriter bw = Files.newBufferedWriter(Paths.get("traces/"+args[0]+"-"+args[1]+".csv"))){
            for(int i = 0; i < log.size(); i++){
                if(i == 0){bw.write("seq,actor,event,job_id,occupancy,result\n");}
                Event event = log.get(i);
                bw.write(event.getSeq()+","+event.getActor()+","+event.getEvent()+","+event.getJobId()+","+event.getOccupancy()+","+event.getResult()+"\n");
            }
        }




    }


}
