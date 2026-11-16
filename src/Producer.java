package src;

public class Producer implements Runnable{
    private final FIFO fifo;
    private final int producers;
    private final SemaphoreFIFO semaphoreFIFO;
    private final int jobs;
    private final int seed;
    private final int id; 

    public Producer(FIFO fifo, int producers, int jobs, int seed, int id) {
        this.fifo = fifo;
        this.semaphoreFIFO = null;
        this.producers = producers;
        this.jobs = jobs;
        this.seed = seed;
        this.id = id;
    }

    public Producer(SemaphoreFIFO semaphoreFIFO, int producers, int jobs, int seed, int id) {
        this.semaphoreFIFO = semaphoreFIFO;
        this.fifo = null;
        this.producers = producers;
        this.jobs = jobs;
        this.seed = seed;
        this.id = id;
    }



    public void run(){
        String actorId = "P"+id;
        int jobId = id+1;

        while(jobId <= jobs){
            int payload = (37*jobId + 11 * seed) % 1000;
            int units = 1 +((jobId + seed)%5);
            int result = payload * units + jobId;
            Job job = new Job (jobId, payload, units, result, false, null);
            try{
                if(semaphoreFIFO != null) semaphoreFIFO.put(job, actorId);//put job into semaphore
                else fifo.put(job, actorId);//put job into fifo
            }catch(InterruptedException e){
                Thread.currentThread().interrupt();
                return;
            }
            jobId += producers;//update id
        }
    }//need to implement


}
