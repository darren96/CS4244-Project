import java.util.ArrayList;

public class CDCL {

	public void checkSAT() {

	}

	// iterated application of the unit clause rule
	private boolean unitPropagation(ArrayList<Clause> clauses) {
		ArrayList<Integer> propList = findUnitClauses(clauses);
		while(propList.size() > 0) {    // check if a unit clause exists
			for(Integer prop : propList) {
				if(propList.contains(-1*prop)) {
					return false;
				}

				for(Clause clause : clauses) {
					if(clause.literals.contains(prop)) {
						clauses.remove(clause);
						continue;
					}

					if(clause.literals.contains(-1*prop)) {
						clause.literals.remove(-1*prop);
					}
				}
			}

			propList = new ArrayList<>();
			propList.addAll(findUnitClauses(clauses));
		}

		return true;
	}

	private ArrayList<Integer> findUnitClauses(ArrayList<Clause> clauses) {
		ArrayList<Integer> unitClauses = new ArrayList<>();

		for(Clause clause : clauses) {
			// If the clause in a unit clause
			if(clause.literals.size() == 1) {
				unitClauses.add(clause.literals.get(0));
			}
		}

		return unitClauses;
	}

	// tests whether all variables have been assigned
	private void allVarsAssigned() {

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
