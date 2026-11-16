package src;

public class Consumer implements Runnable {
    private final FIFO fifo;
    private final int id;
    private final int delayMs;
    private final SemaphoreFIFO semaphoreFIFO;

    public Consumer(FIFO fifo, int id, int delayMs) {
        this.fifo = fifo;
        this.semaphoreFIFO = null;
        this.id = id;
        this.delayMs = delayMs;
    }

    public Consumer(SemaphoreFIFO semaphoreFIFO, int id, int delayMs) {
        this.fifo = null;
        this.semaphoreFIFO = semaphoreFIFO;
        this.id = id;
        this.delayMs = delayMs;
    }

    public void run(){
        String actorId = "C"+id;

        while(true){
            try{
                Job job;
                if(semaphoreFIFO == null) job = fifo.get(actorId);
                else job = semaphoreFIFO.get(actorId);//get job
                if(job.isStop()){
                    return;
                }

                if(delayMs > 0) Thread.sleep(job.getUnits() * delayMs);//sleep for delayMs
                int result = job.getPayload() * job.getUnits() + job.getJobId();//calculate 
                if (semaphoreFIFO != null) semaphoreFIFO.logDONE(actorId, job.getJobId()+"", result);//log
                else fifo.logDONE(actorId, job.getJobId()+"", result);//log

            }catch(InterruptedException e){
                Thread.currentThread().interrupt();
                return;
            }

        }
    }

}
