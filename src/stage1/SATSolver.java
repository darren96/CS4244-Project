package stage1;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.io.*;

public class SATSolver {
    static List<Variable> variables = new ArrayList<>();
    static List<Clause> clauses = new ArrayList<>();

    public static void main(String[] args) {
        System.out.print("Please key in the cnf input file path here: ");
        InputStreamReader inputStreamReader = new InputStreamReader(System.in);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        String file = "";
        try {
            file = bufferedReader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                bufferedReader.close();
                inputStreamReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        initialise(file);
        CDCL cdcl = new CDCL(clauses, variables);
        long startTime = System.nanoTime();
        boolean isSatisfiable = cdcl.checkSAT();
        long endTime = System.nanoTime();

        if (isSatisfiable) {
            printAssignment(cdcl);
        } else {
            System.out.println("UNSAT");
        }

        long timeTaken = (endTime - startTime) / 1000000;
        System.out.println("Backtracking Invocation: " + cdcl.backtrackingInvocation);
        System.out.println(String.format("Time Taken: %d ms", timeTaken));
    }

    static void initialise(String file) {
        String filePath = resolveFilePath(file).toString();
        FileReader fileReader = null;
        BufferedReader bufferedReader = null;

        try {
            fileReader = new FileReader(filePath);
            bufferedReader = new BufferedReader(fileReader);
            String line;

            while ((line = bufferedReader.readLine()) != null) {
                if (line.trim().startsWith("c")) {
                    continue;
                }
                if (line.trim().startsWith("p")) {
                    String[] tokens = line.split(" ");
                    int numOfVariables = Integer.parseInt(tokens[2]);
                    // add dummy variable for easy access
                    variables.add(new Variable(0));
                    for (int i = 1; i <= numOfVariables; i++) {
                        variables.add(new Variable(i));
                    }
                    continue;
                }
                if (line.trim().endsWith("0")) {
                    clauses.add(new Clause(line.trim()));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bufferedReader != null)
                    bufferedReader.close();
                if (fileReader != null)
                    fileReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static Path resolveFilePath(String fileName) throws InvalidPathException {
        Path currentDirectory = Paths.get(System.getProperty("user.dir"));
        return currentDirectory.resolve(fileName);
    }

    public static void printAssignment(CDCL cdcl) {
        System.out.println(System.lineSeparator() + "Assignments:");
        for (Assignment assignment : cdcl.assignmentList) {
            System.out.println(assignment.variable + ": " + assignment.truthValue);
        }
    }
}
