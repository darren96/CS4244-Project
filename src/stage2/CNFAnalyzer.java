package stage2;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.sat4j.minisat.SolverFactory;
import org.sat4j.reader.DimacsReader;
import org.sat4j.specs.IProblem;
import org.sat4j.specs.ISolver;

// To generate an analytical data for the Random Generated CNF
public class CNFAnalyzer {

    static List<String> satFiles = new ArrayList<>();
    static List<String> unsatFiles = new ArrayList<>();
    static List<String> errorFiles = new ArrayList<>();

    private static final String STATS_DIR = "stats" + File.separator;

    public static void main(String[] args) {
        System.out.print("Please key in the folder path that consist of cnf files: ");
        InputStreamReader inputStreamReader = new InputStreamReader(System.in);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        try {
            String directory = bufferedReader.readLine();
            analyse(directory);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                bufferedReader.close();
                inputStreamReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Analyse each and every cnf files given the file structure in input/generated/k_
     * @param directory The given directory eg: input/generated/k3
     * @throws Exception
     */
    private static void analyse(String directory) throws Exception {
        String results = "";
        Path directoryPath = Paths.get(directory);
        if (!Files.exists(directoryPath)) {
            throw new Exception("Folder not found");
        }
        if (!Files.isDirectory(directoryPath)) {
            throw new Exception("This is not a directory");
        }

        ISolver solver = SolverFactory.newDefault();
        solver.setTimeout(60);
        DimacsReader reader = new DimacsReader(solver);

        List<Path> rFoldersInFolder =
                Files.list(directoryPath).filter(Files::isDirectory).sorted().collect(Collectors.toList());

        for (Path folder : rFoldersInFolder) {
            List<Path> filesInFolder =
                    Files.list(folder).filter(x -> x.getFileName().toString().endsWith(".cnf")).sorted().collect(Collectors.toList());

            for (Path file : filesInFolder) {
                try {
                    IProblem problem = reader.parseInstance(file.toString());
                    long startTime = System.nanoTime();
                    boolean satisfiable = problem.isSatisfiable();
                    long endTime = System.nanoTime();

                    if (satisfiable) {
                        satFiles.add(file.getFileName().toString());
                    } else {
                        unsatFiles.add(file.getFileName().toString());
                    }

                    long timeTaken = (endTime - startTime) / 1000000;
                    results += folder.getFileName().toString() + "," + file.getFileName().toString() + "," + timeTaken +
                            "," + satisfiable + System.lineSeparator();
                } catch (Exception e) {
                    errorFiles.add(file.getFileName().toString());
                }
            }
        }

        writeResults(results, STATS_DIR + directoryPath.getFileName() + ".csv");
    }

    /**
     * Write the results to respective k files
     * @param results The results generated after analysing the cnfs
     * @param outputFile The output file name
     * @throws IOException
     */
    private static void writeResults(String results, String outputFile) throws IOException {
        if (!Files.exists(Paths.get(outputFile))) {
            Files.createFile(Paths.get(outputFile));
        }

        FileWriter fileWriter = new FileWriter(outputFile);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        bufferedWriter.write(Paths.get(outputFile).getFileName() + System.lineSeparator());
        bufferedWriter.write("r,filename,time taken to solve (ms),satisfiability" + System.lineSeparator());

        bufferedWriter.write(results + System.lineSeparator());
        bufferedWriter.write(System.lineSeparator());

        bufferedWriter.write("SAT Files" + System.lineSeparator());
        bufferedWriter.write(String.join(System.lineSeparator(), satFiles));
        bufferedWriter.write(System.lineSeparator());

        bufferedWriter.write("UNSAT Files" + System.lineSeparator());
        bufferedWriter.write(String.join(System.lineSeparator(), unsatFiles));
        bufferedWriter.write(System.lineSeparator());

        bufferedWriter.write("Error Files" + System.lineSeparator());
        bufferedWriter.write(String.join(System.lineSeparator(), errorFiles));
        bufferedWriter.write(System.lineSeparator());

        bufferedWriter.close();
    }
}
