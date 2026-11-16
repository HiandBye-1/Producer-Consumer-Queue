package src;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantLock;


public class SemaphoreFIFO {
    private final Semaphore empty;
    private final Semaphore full;
    private final ReentrantLock lock;
    private final ArrayDeque<Job> queue;
    private final ArrayList<Event> events;
    private int occupancy=0;
    private int maxOccupancy=0;
    private int seq=1;

    public SemaphoreFIFO(int capacity) {
        empty = new Semaphore(capacity);
        full = new Semaphore(0);
        lock = new ReentrantLock();
        queue = new ArrayDeque<>();
        events = new ArrayList<>();
    }

    public void put(Job job, String ID) throws InterruptedException{
        empty.acquire();
        lock.lock();
        try{
            queue.addLast(job);
            occupancy++;
            if(job.isStop()) logEvent("STOP_ENQ", ID, job.getStopId(), null);
            else logEvent("ENQ", ID, job.getJobId()+"", job.getResult());
            if(occupancy > maxOccupancy) maxOccupancy = occupancy;
        }finally{
            lock.unlock();
        }
        full.release();
    }

    public Job get(String ID) throws InterruptedException{
        full.acquire();
        lock.lock();
        Job job = null;
        try{
            job = queue.removeFirst();
            occupancy--;
            if(job.isStop()) logEvent("STOP_GET", ID, job.getStopId(), null);
            else logEvent("DEQ", ID, job.getJobId()+"", job.getResult());
        }finally{
            lock.unlock();
        }
        empty.release();
        return job;
    }

    public void logEvent(String event, String actor, String job_id, Integer result ){
        lock.lock();
        try{
            events.add(new Event(seq++, event, actor, job_id, occupancy, result));
        }finally{
            lock.unlock();
        }
    }

    public ArrayList<Event> getEvents() {
        lock.lock();
        try{
            return new ArrayList<>(events);//prevent modify orignal
        }finally{
            lock.unlock();
        }
    }

    public int getMaxOccupancy() {
        lock.lock();
        try{
            return maxOccupancy;
        }finally{
            lock.unlock();
        }
    }

    public int getOccupancy() {
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
}
