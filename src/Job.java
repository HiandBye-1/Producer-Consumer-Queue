package src;

public class Job {
    private final int job_id;
    private final int payload;
    private final int units;
    private final int result;
    private final boolean stop;
    private final String stopId;

    public Job(int job_id, int payload, int units, int result, boolean stop, String stopId) {
        this.job_id = job_id;
        this.payload = payload;
        this.units = units;
        this.result = result;
        this.stop = stop;
        this.stopId = stopId;
    }

    public int getJobId() {
        return job_id;
    }

    public int getPayload() {
        return payload;
    }

    public int getUnits() {
        return units;
    }

    public int getResult() {
        return result;
    }

    public boolean isStop() {
        return stop;
    }

    public String getStopId() {
        return stopId;
    }

}
