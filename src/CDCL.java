import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class CDCL {

	public void checkSAT(List<Clause> clauses) {
    System.out.println(unitPropagation(clauses));
    clauses.stream().forEach(x -> System.out.println(x.literals.toString()));
	}

	// iterated application of the unit clause rule
	private boolean unitPropagation(List<Clause> clauses) {
		ArrayList<Integer> propList = findUnitClauses(clauses);
		while(propList.size() > 0) {    // check if a unit clause exists
			for(Integer prop : propList) {
				if(propList.contains(-1*prop)) {
					return false;
				}

				Iterator<Clause> clausesIterator = clauses.iterator();

				while (clausesIterator.hasNext()) {
					Clause clause = clausesIterator.next();
					System.out.println(clause.literals.toString());
					if (clause.literals.contains(prop)) {
						clausesIterator.remove();
					} else if (clause.literals.contains(-1*prop)) {
						clause.literals.removeAll(Arrays.asList(-1*prop));
					}
				}
			}
			propList = findUnitClauses(clauses);
		}

		return true;
	}

	private ArrayList<Integer> findUnitClauses(List<Clause> clauses) {
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
