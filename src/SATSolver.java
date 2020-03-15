import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.io.*;
import java.util.Map;
import java.util.stream.Collectors;

public class SATSolver {
    static List<Variable> variables = new ArrayList<>();
    static List<Clause> clauses = new ArrayList<>();

    public static void main(String[] args) {
        initialise();
        CDCL cdcl = new CDCL(clauses, variables);
        cdcl.checkSAT();
    }

    static void initialise() {
        InputStreamReader inputStreamReader = new InputStreamReader(System.in);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        String line;

        try {
            while ((line = bufferedReader.readLine()) != null) {
                if (line.trim().startsWith("p")) {
                    String[] tokens = line.split(" ");
                    for (int i = 0; i < Integer.parseInt(tokens[2]); i++) {
                        variables.add(new Variable(i));
                    }
                }

                if (line.trim().endsWith("0")) {
                    clauses.add(new Clause(line.trim()));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
