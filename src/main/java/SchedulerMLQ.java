import java.util.List;
import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Comparator;

/**
 * Implements the Multilevel Queue (MLQ) scheduling algorithm simulator.
 * It manages the simulation clock, the ready queues, and the dispatching of processes.
 * This implementation uses 3 queues with preemption between them (Q1 has priority
 * over Q2, and Q2 over Q3).
 *
 * @author Santiago Duque
 * @version 1.0
 */
public class SchedulerMLQ
{
    /** Master list with all processes read from the file. */
    private List<Process> allProcesses;

    /** List to store processes as they finish execution. */
    private List<Process> finishedProcesses;

    /** Priority Queue for Level 1. */
    private PriorityQueue<Process> queue1;
    /** Priority Queue for Level 2. */
    private PriorityQueue<Process> queue2;
    /** Priority Queue for Level 3. */
    private PriorityQueue<Process> queue3;

    /** Global simulation clock. Advances tick by tick. */
    private int actualTime = 0;
    /** The process currently running on the CPU (null if idle). */
    private Process ProcessInCPU = null;


    /** Scheduling policies for each queue ("RR", "SJF"). */
    private String[] policies = {"RR", "RR", "SJF"};
    /** Time quantum for each queue (1, 3, Infinity). */
    private int[] quantum = {1, 3, Integer.MAX_VALUE};
    /** Remaining quantum for the current process on the CPU (relevant for RR). */
    private int remainingQuantum = 0;

    /**
     * Constructor for the MLQ Simulator.
     *
     * @param processes The initial list of all processes loaded from the file.
     */
    public SchedulerMLQ(List<Process> processes)
    {
        this.allProcesses = processes;
        this.finishedProcesses = new ArrayList<>();

        Comparator<Process> comparePriority = (p1, p2) -> p2.getPriority() - p1.getPriority();

        this.queue1 = new PriorityQueue<>(comparePriority);
        this.queue2 = new PriorityQueue<>(comparePriority);
        this.queue3 = new PriorityQueue<>(comparePriority);
    }

    /**
     * Runs the main simulation loop.
     * It continues until all processes from the master list have finished.
     * The loop advances the {@code currentTime} tick by tick and follows 6 steps:
     * 1. ARRIVALS: Moves processes from the master list to the ready queues if {@code arrivalTime == currentTime}.
     * 2. PREEMPTION: Checks if a higher-priority process should preempt the one on the CPU.
     * 3. DISPATCH: If the CPU is idle, dispatches the best available process.
     * 4. EXECUTION: Executes one tick of the process on the CPU.
     * 5. REVIEW: Checks if the CPU process has finished or its quantum expired.
     * 6. ADVANCE: Increments the {@code currentTime}.
     */
    public void simulate()
    {
        while (finishedProcesses.size() < allProcesses.size())
        {

            // 1. Move processes from the total list to queues if they have arrived
            for (int i = 0; i < allProcesses.size(); i++)
            {
                Process p = allProcesses.get(i);
                if (p.getArrivalTime() == actualTime && !finishedProcesses.contains(p))
                {
                    switch (p.queueId)
                    {
                        case 1: queue1.add(p); break;
                        case 2: queue2.add(p); break;
                        case 3: queue3.add(p); break;
                    }
                }
            }

            // 2. Preemption Logic
            Process bestQueuingProcess = getBetterProcess();
            if (ProcessInCPU != null && bestQueuingProcess != null)
            {
                if (bestQueuingProcess.queueId < ProcessInCPU.queueId)
                {
                    returnProcessToQueue(ProcessInCPU);
                    ProcessInCPU = null;
                }
            }

            // 3. If CPU is idle, dispatch the best process
            if (ProcessInCPU == null) 
            {
                Process processToBeDispatched = getBetterProcess();
                if (processToBeDispatched != null)
                {
                    dispatch(processToBeDispatched);
                }
            }

            // 4. Execute one "tick" of the clock
            if (ProcessInCPU != null) 
            {

                if (ProcessInCPU.isFirstTime) 
                {
                    ProcessInCPU.responseTime = actualTime - ProcessInCPU.arrivalTime;
                    ProcessInCPU.isFirstTime = false;
                }

                ProcessInCPU.runTick();
                remainingQuantum--;

                // 5. Check if the process finished or its quantum expired
                if (ProcessInCPU.itsOver())
                {
                    //That "+ 1" is because it ends at the end of the tick
                    ProcessInCPU.calculateMetrics(actualTime + 1);
                    finishedProcesses.add(ProcessInCPU);
                    ProcessInCPU = null;
                }
                else if (remainingQuantum == 0)
                {
                    returnProcessToQueue(ProcessInCPU);
                    ProcessInCPU = null;
                }
            }
            actualTime++;
        }
    }

    /**
     * Gets the best process ready to run, respecting queue priority
     * (Q1 > Q2 > Q3).
     * Uses {@code peek()} to "to obtain without taking out" on the top of the queue without removing the element.
     *
     * @return The highest-priority process in the queues, or null if all are empty.
     */
    private Process getBetterProcess()
    {
        if (!queue1.isEmpty()) return queue1.peek();
        if (!queue2.isEmpty()) return queue2.peek();
        if (!queue3.isEmpty()) return queue3.peek();
        return null;
    }

    /**
     * Moves a process from its ready queue to the CPU.
     * It removes the process from its queue (with {@code poll()}) and assigns the
     * quantum corresponding to its queue level.
     *
     * @param p The process to be dispatched.
     */
    private void dispatch(Process p)
    {
        if (p.queueId == 1) queue1.poll();
        else if (p.queueId == 2) queue2.poll();
        else if (p.queueId == 3) queue3.poll();

        ProcessInCPU = p;
        // The quantum of its queue is assigned
        remainingQuantum = quantum[p.queueId - 1];
    }

    /**
     * Returns a process to its corresponding ready queue.
     * This happens on quantum expiration (for RR) or preemption.
     *
     * @param p The process that was on the CPU and must return to its queue.
     */
    private void returnProcessToQueue(Process p)
    {
        if (p.queueId == 1) queue1.add(p);
        else if (p.queueId == 2) queue2.add(p);
        else if (p.queueId == 3) queue3.add(p);
    }

    /**
     * Gets the list of all processes that have completed their execution.
     * The results are sorted by label (A, B, C...) for clean output.
     *
     * @return A list of {@code Process} objects with all their metrics calculated.
     */
    public List<Process> getResults()
    {
        finishedProcesses.sort(Comparator.comparing(p -> p.label));
        return finishedProcesses;
    }
}
