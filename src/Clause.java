import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Clause {
  List<Integer> literals;

    public Clause(String clauseString) {
        literals = Arrays.stream(clauseString.substring(0, clauseString.length() - 2)
            .trim().split("\\s+"))
            .map(Integer::parseInt)
            .collect(Collectors.toList());
    }
}
