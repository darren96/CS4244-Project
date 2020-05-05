package stage1;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Clause {
    List<Integer> literals;
    Satisfiability isSatisfied;
    int assignedLiterals;

    public enum Satisfiability {
        SAT,
        UNSAT,
        UNDECIDE
    }

    public Clause(List<Integer> literals) {
        this.literals = literals;
        isSatisfied = Satisfiability.UNDECIDE;
        assignedLiterals = 0;
    }

    public Clause(String clauseString) {
        literals = Arrays.stream(clauseString.substring(0, clauseString.length() - 2)
                .trim().split("\\s+"))
                .map(Integer::parseInt)
                .collect(Collectors.toList());
        isSatisfied = Satisfiability.UNDECIDE;
        assignedLiterals = 0;
    }
}
