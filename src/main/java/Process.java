/**
 * Represents a single process in the system.
 * This class stores both the input attributes (read from the file)
 * and the performance metrics (calculated during the simulation).
 *
 * @author Santiago Duque
 * @version 1.0
 */
public class Process
{
    // Input Attributes
    /** Identifier label for the process ("A", "B"). */
    String label;
    /** The total CPU time required by the process (original Burst Time). */
    int burstTime;
    /** The moment the process arrives in the system (Arrival Time). */
    int arrivalTime;
    /** The ID of the multilevel queue this process belongs to (1, 2, or 3). */
    int queueId;
    /** The internal priority of the process within its queue (5 > 1). */
    int priority;

    // State Attributes
    /** The remaining CPU time the process still needs to execute. */
    int remainingBurstTime;
    /** Flag to calculate Response Time (RT) the first time it runs. */
    boolean isFirstTime = true;

    // Output Attributes
    /** The exact moment the process finishes its execution (Completion Time). */
    int completionTime;
    /** Time from arrival (AT) until it runs for the first time (Response Time). */
    int responseTime;
    /** Total time the process spends in the ready queues (Waiting Time). */
    int waitingTime;
    /** Total time from arrival (AT) to completion (CT) (TurnAround Time). */
    int turnAroundTime;

    /**
     * Constructor to create a new Process.
     *
     * @param label       The unique identifier for the process.
     * @param burstTime   The total required CPU time.
     * @param arrivalTime The arrival time into the system.
     * @param queueId     The MLQ queue it is assigned to (1, 2, or 3).
     * @param priority    The internal priority within its queue (5 is highest).
     */
    public Process(String label, int burstTime, int arrivalTime, int queueId, int priority)
    {
        this.label = label;
        this.burstTime = burstTime;
        this.arrivalTime = arrivalTime;
        this.queueId = queueId;
        this.priority = priority;

        this.remainingBurstTime = burstTime;
    }

    /**
     * Simulates the execution of the process for one time unit (a "tick").
     * Decrements the remaining burst time.
     */
    public void runTick()
    {
        if (remainingBurstTime > 0)
        {
            remainingBurstTime--;
        }
    }

    /**
     * Checks if the process has completed all its execution.
     *
     * @return true if remainingBurstTime is 0, false otherwise.
     */
    public boolean itsOver()
    {
        return remainingBurstTime == 0;
    }

    /**
     * Calculates the final performance metrics (CT, TAT, WT) for this process.
     * Response Time (RT) is calculated during the simulation.
     *
     * @param currentCT The value of the global clock (currentTime)
     * when the process finished.
     */
    public void calculateMetrics(int currentCT)
    {
        this.completionTime = currentCT;
        this.turnAroundTime = this.completionTime - this.arrivalTime;
        this.waitingTime = this.turnAroundTime - this.burstTime;
    }

    //Getters and Setters
    /**
     * Gets the internal priority of the process.
     * @return The priority value (5 > 1).
     */
    public int getPriority()
    {
        return priority;
    }

    /**
     * Gets the arrival time of the process.
     * @return The arrival time (AT).
     */
    public int getArrivalTime()
    {
        return arrivalTime;
    }
}
