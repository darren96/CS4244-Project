import java.util.*;

public class CDCL {

	List<Clause> clauses;
	Map<Integer, Variable> variables;

	public CDCL(List<Clause> clauses, Map<Integer, Variable> variables) {
		this.clauses = clauses;
		this.variables = variables;
	}

    public void checkSAT() {
        System.out.println(unitPropagation());
    }

    // iterated application of the unit clause rule
    private boolean unitPropagation() {
        ArrayList<Integer> propList = findUnitClauses();
        while (propList.size() > 0) {    // check if a unit clause exists
            for (Integer prop : propList) {
                if (propList.contains(-1 * prop)) {
                    return false;
                }

                Iterator<Clause> clausesIterator = clauses.iterator();

                while (clausesIterator.hasNext()) {
                    Clause clause = clausesIterator.next();
                    if (clause.literals.contains(prop)) {
                        clausesIterator.remove();
                    } else if (clause.literals.contains(-1 * prop)) {
                        clause.literals.removeAll(Arrays.asList(-1 * prop));
                    }
                }
            }
            propList = findUnitClauses();
        }

        return true;
    }

    private ArrayList<Integer> findUnitClauses() {
        ArrayList<Integer> unitClauses = new ArrayList<>();

        for (Clause clause : clauses) {
            // If the clause in a unit clause
            if (clause.literals.size() == 1) {
                unitClauses.add(clause.literals.get(0));
            }
        }

        return unitClauses;
    }

    // tests whether all variables have been assigned
    private boolean allVarsAssigned() {
        for (Clause clause : clauses) {
            if (!clause.unassignedLiterals.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    // selects a variable for truth assignment
    private void pickBranchingVar() {

    }

    // analyzes the most recent conflict and learns a new clause from the conflict
    private void conflictAnalysis() {

    }

    // backtracks to a decision level
    private void backtrack() {

    }
}
