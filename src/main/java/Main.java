import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Main (Driver) class for the MLQ simulator.
 * It is responsible for:
 * 1. Reading input files ({@link #readFile(String)}).
 * 2. Instantiating and running the simulator ({@link SchedulerMLQ}).
 * 3. Writing the output files with the results ({@link #writeFile(String, List)}).
 *
 * @author Santiago Duque
 * @version 1.0
 */
public class Main
{

    /**
     * Main entry point for the application.
     *
     * @param args Command-line arguments (not used).
     */
    public static void main(String[] args)
    {
        String inputFile = "mlq001.txt";
        List<Process> Process = readFile(inputFile);

        if (Process != null)
        {
            SchedulerMLQ simulator = new SchedulerMLQ(Process);
            simulator.simulate();

            List<Process> results = simulator.getResults();
            writeFile(inputFile, results);
        }
    }

    /**
     * Reads a text file with process definitions.
     * Ignores lines starting with '#' or empty lines.
     *
     * @param fileName The name (or path) of the input file.
     * @return A list of {@code Process} objects read from the file,
     * or null if an error occurs.
     */
    public static List<Process> readFile(String fileName)
    {
        List<Process> process = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(fileName)))
        {
            String line;
            while ((line = br.readLine()) != null)
            {
                // This condition is used to ignore comments.
                if (line.startsWith("#") || line.trim().isEmpty())
                {
                    continue;
                }

                // Split by ; and trim whitespace
                String[] partes = line.split(";");
                String label = partes[0].trim();

                int bt = Integer.parseInt(partes[1].trim());
                int at = Integer.parseInt(partes[2].trim());
                int q = Integer.parseInt(partes[3].trim());
                int pr = Integer.parseInt(partes[4].trim());

                process.add(new Process(label, bt, at, q, pr));
            }
        }
        catch (Exception e)
        {
            System.err.println("Error leyendo el archivo: " + e.getMessage());
            return null;
        }
        return process;
    }

    /**
     * Writes the simulation results to a text file.
     * The output file will be named "salida_" + originalFile.
     * Includes metrics for each process and the final averages.
     *
     * @param originalFile The name of the input file, used to name the output file.
     * @param results      The list of finished processes with their calculated metrics.
     */
    public static void writeFile(String originalFile, List<Process> results)
    {
        String outputFile = "salida_" + originalFile;
        double totalWT = 0, totalCT = 0, totalRT = 0, totalTAT = 0;

        try (PrintWriter pw = new PrintWriter(new File(outputFile)))
        {
            pw.println("# archivo: " + originalFile);
            pw.println("# label; BT; AT; Q; Pr; WT; CT; RT; TAT");

            for (int i = 0; i < results.size(); i++)
            {
                Process p = results.get(i);
                pw.printf("%s;%d;%d;%d;%d;%d;%d;%d;%d\n",
                        p.label, p.burstTime, p.arrivalTime, p.queueId, p.priority,
                        p.waitingTime, p.completionTime, p.responseTime, p.turnAroundTime);

                totalWT += p.waitingTime;
                totalCT += p.completionTime;
                totalRT += p.responseTime;
                totalTAT += p.turnAroundTime;
            }

            int n = results.size();
            pw.printf("WT=%.1f; CT=%.1f; RT=%.1f; TAT=%.1f;\n",
                    totalWT / n, totalCT / n, totalRT / n, totalTAT / n);

            System.out.println("Simulacion completada. Resultados en: " + outputFile);

        }
        catch (Exception e)
        {
            System.err.println("Error escribiendo el archivo: " + e.getMessage());
        }
    }
}