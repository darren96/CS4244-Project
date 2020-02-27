import java.util.ArrayList;
import java.util.List;
import java.io.*;

public class SAT_Solver {
  public static void main(String[] args) {
    List<Clause> clauses = getAllClausesFromFile();
    
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
}
