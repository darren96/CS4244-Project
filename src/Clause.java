import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Clause {
  List<Integer> literals;
  Set<Integer> unassignedLiterals;

    public Clause(String clauseString) {
        literals = Arrays.stream(clauseString.substring(0, clauseString.length() - 2)
            .trim().split("\\s+"))
            .map(Integer::parseInt)
            .collect(Collectors.toList());
        unassignedLiterals = literals.stream().collect(Collectors.toSet());
    }
}
