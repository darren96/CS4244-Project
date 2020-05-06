package stage1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class EinsteinPuzzleSolver {
    static List<Variable> variables = new ArrayList<>();
    static List<Clause> clauses = new ArrayList<>();
    static List<String> pairs = new ArrayList<>();

    public static void main(String[] args) {
        initialise(resolveFilePath("input" + File.separator + "einstein" + File.separator + "einstein"
                + ".cnf").toString());
        CDCL cdcl = new CDCL(clauses, variables);
        boolean isSatisfiable = cdcl.checkSAT();
        if (isSatisfiable) {
            readEncoding();
            printAssignment(cdcl);
        }
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

    public static void readEncoding() {
        String filePath =
                resolveFilePath("input" + File.separator + "einstein" + File.separator + "einstein_puzzle_encoding"
                        + ".txt").toString();
        FileReader fileReader = null;
        BufferedReader bufferedReader = null;

        try {
            fileReader = new FileReader(filePath);
            bufferedReader = new BufferedReader(fileReader);
            String line;

            while ((line = bufferedReader.readLine()) != null) {
                pairs.add(line);
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

    public static void printAssignment(CDCL cdcl) {
        System.out.println(System.lineSeparator() + "Assignments:");
        for (Assignment assignment : cdcl.assignmentList) {
            if (assignment.truthValue) {
                System.out.println(pairs.get(assignment.variable - 1));
            }
        }
    }
}
