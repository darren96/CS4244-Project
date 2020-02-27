import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.io.*;
import java.util.Map;

public class SAT_Solver {
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

    for (Clause clause : clauses) {
      for (Integer variable : clause.literals) {
        if (variable < 0) {
          variable = -1 * variable;
        }

        if (variableMap.containsKey(variable)) {
          variableMap.get(variable).noOfAppearances++;
        } else {
          variableMap.put(variable, new Variable(variable));
        }
      }
    }
    return variableMap;
  }
}
