package src;
public class Event {
    private final int seq;
    private final String event;
    private final String actor;
    private final String job_id;
    private final int occupancy;
    private final Integer result;

    public Event(int seq, String event, String actor, String job_id, int occupancy, Integer result) {
        this.seq = seq;
        this.event = event;
        this.actor = actor;
        this.job_id = job_id;
        this.occupancy = occupancy;
        this.result = result;
    }

    public int getSeq() {
        return seq;
    }

    public String getEvent() {
        return event;
    }

    public String getActor() {
        return actor;
    }

    public String getJobId() {
        return job_id;
    }

    public int getOccupancy() {
        return occupancy;
    }

    public Integer getResult() {
        return result;
    }

}