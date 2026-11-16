package src;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.ArrayDeque;
import java.util.ArrayList;


public class FIFO {
    //lock() unlock()
    //await() not full producer
    //await() not empty consumer

    private final ReentrantLock lock = new ReentrantLock();
    private final int capacity;
    private final Condition notFull = lock.newCondition();
    private final Condition notEmpty = lock.newCondition();
    private int maxOccupancy=0;
    private int occupancy=0;
    private final ArrayDeque<Job> queue = new ArrayDeque<>();//shared queue
    private final ArrayList<Event> events = new ArrayList<>();
    private int seq = 1;


    public FIFO(int capacity) {
        this.capacity = capacity;
    }

    public void put(Job job, String ID) throws InterruptedException{//for producer
        lock.lock();
        try{
            while(occupancy == capacity){
                notFull.await();
            }

            queue.addLast(job);
            occupancy++;
            
            if(job.isStop()) logEvent("STOP_ENQ", ID, job.getStopId(), null);
            else logEvent("ENQ", ID, job.getJobId()+"", job.getResult());
            if(occupancy > maxOccupancy) maxOccupancy = occupancy;

            notEmpty.signal();

        }finally{
            lock.unlock();
        }
    }

    public Job get(String ID) throws InterruptedException{
        lock.lock();
        try{
            while(occupancy == 0) notEmpty.await();

            Job job = queue.removeFirst();
            occupancy--;

            if(job.isStop()) logEvent("STOP_GET", ID, job.getStopId(), null);
            else logEvent("DEQ", ID, job.getJobId()+"", job.getResult());
            
            notFull.signal();
            return job;

        }finally{
            lock.unlock();
        }
    }

    public int getMaxOccupancy(){
        lock.lock();
        try{
            return maxOccupancy;
        }finally{
            lock.unlock();
        }
    }

    public int getOccupancy(){
        lock.lock();
        try{
            return occupancy;
        }finally{
            lock.unlock();
        }
    }

    public void logDONE(String consumerActorId, String jobId, int result ){
        lock.lock();
        try{
            logEvent("DONE", consumerActorId, jobId, result);
        }finally{
            lock.unlock();
        }
    }
    
    private void logEvent(String event, String actor, String job_id, Integer result) {
        events.add(new Event(seq++, event, actor, job_id, occupancy, result));
    }

    public ArrayList<Event> getEvents() {
        lock.lock();
        try{
            return new ArrayList<>(events);//prevent modify orignal
        }finally{
            lock.unlock();
        }
    }

    public void logSTOPGET(String event, String actor, String job_id, int result ){
        
    }



}
