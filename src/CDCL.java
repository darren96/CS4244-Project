import java.util.*;

public class CDCL {

	List<Clause> clauses;
	Map<Integer, Variable> variables;
	int decisionLevel;
	List<Integer> decisionList = new ArrayList<>();

	public CDCL(List<Clause> clauses, Map<Integer, Variable> variables) {
		this.clauses = clauses;
		this.variables = variables;
		this.decisionLevel = 0;
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
        return variables.values().stream()
                .allMatch(variable -> variable.assigned != null);
    }

    // selects a variable for truth assignment
    private void pickBranchingVar() {

    }

    // analyzes the most recent conflict and learns a new clause from the conflict
    private void conflictAnalysis() {

    }

    // backtracks to a decision level
    private void backtrack(int decisionLevel) {
        this.decisionLevel = decisionLevel;
        for (int i = decisionList.size(); i > decisionLevel; i--){
            variables.get(decisionList.get(i)).assigned = null;
            decisionList.remove(i);
        }
    }

    private void randomVarPicker() {
	    Random random = new Random();
	    int randomInteger = random.nextInt(variables.size() - 1);

	    while (variables.get(randomInteger).assigned) {
	        randomInteger = random.nextInt(variables.size() - 1);
        }

	    decisionList.add(randomInteger);
	    variables.get(randomInteger).assigned = random.nextBoolean();
    }
}
