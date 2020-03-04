import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.io.*;
import java.util.Map;
import java.util.stream.Collectors;

public class SATSolver {
  public static void main(String[] args) {
    List<Clause> clauses = getAllClausesFromFile();
    Map<Integer, Variable> variables = getAllVariables(clauses);
    CDCL cdcl = new CDCL(clauses, variables);
    cdcl.checkSAT();
  }

  static List<Clause> getAllClausesFromFile() {
    InputStreamReader inputStreamReader = new InputStreamReader(System.in);
    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
    String line;
    List<Clause> clauses = new ArrayList<>();

    try {
      while ((line = bufferedReader.readLine()) != null) {
        if (line.trim().endsWith("0")) {
          clauses.add(new Clause(line.trim()));
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    return clauses;
  }

  static Map<Integer, Variable> getAllVariables(List<Clause> clauses) {
    Map<Integer, Variable> variableMap = new HashMap<>();

    List<Integer> listOfLiteralsInClauses = clauses.stream()
            .flatMap(clause -> clause.literals.stream())
            .collect(Collectors.toList());

    listOfLiteralsInClauses.stream().forEach(variable -> {
      if (variableMap.containsKey(Math.abs(variable))) {
        variableMap.get(Math.abs(variable)).noOfAppearances++;
      } else {
        variableMap.put(Math.abs(variable), new Variable(Math.abs(variable)));
      }
    });
    return variableMap;
  }
}
