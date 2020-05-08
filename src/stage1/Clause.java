package stage1;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Clause {
    List<Integer> literals;
    Satisfiability isSatisfied;
    Set<Integer> assignedLiterals;

    public enum Satisfiability {
        SAT,
        UNSAT,
        UNDECIDE
    }

    public Clause(List<Integer> literals) {
        this.literals = literals;
        isSatisfied = Satisfiability.UNDECIDE;
        assignedLiterals = new HashSet<>();
    }

    public Clause(String clauseString) {
        literals = Arrays.stream(clauseString.substring(0, clauseString.length() - 2)
                .trim().split("\\s+"))
                .map(Integer::parseInt)
                .collect(Collectors.toList());
        isSatisfied = Satisfiability.UNDECIDE;
        assignedLiterals = new HashSet<>();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Clause)) {
            return false;
        }

        for (int literal : ((Clause) obj).literals) {
            if (!this.literals.contains(literal)) {
                return false;
            }
        }

        return true;
    }
}
