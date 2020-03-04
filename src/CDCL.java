import java.util.*;

public class CDCL {

	List<Clause> clauses;
	Map<Integer, Variable> variables;
	int decisionLevel;
	List<Integer> decisionList = new ArrayList<>();
	PriorityQueue<Variable> scoreHeap = new PriorityQueue<>((v1, v2) -> v2.score.compareTo(v1.score));

	public CDCL(List<Clause> clauses, Map<Integer, Variable> variables) {
		this.clauses = clauses;
		this.variables = variables;
		this.decisionLevel = 0;
	}

    public void checkSAT() {
        if(!unitPropagation()) {
            System.out.println("UNSAT");
            return;
        }

        int decisionLevel = 0;
        while(!allVarsAssigned()) {
            pickBranchingVar();
            decisionLevel++;
            if(!unitPropagation()) {
               Integer beta = conflictAnalysis();
               if(beta < 0) {
                   System.out.println("UNSAT");
                   return;
               } else {
                   backtrack(decisionLevel);
                   decisionLevel = beta;
               }
            }
        }

        System.out.println("SAT");
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
                .allMatch(variable -> variable.truthValue != null);
    }

    // selects a variable for truth assignment
    private void pickBranchingVar() {

    }

    // analyzes the most recent conflict and learns a new clause from the conflict
    private Integer conflictAnalysis() {
        return 0;
    }

    // backtracks to a decision level
    private void backtrack(int decisionLevel) {
        this.decisionLevel = decisionLevel;
        for (int i = decisionList.size(); i > decisionLevel; i--){
            variables.get(decisionList.get(i)).truthValue = null;
            decisionList.remove(i);
        }
    }

    private void scoreVarPicker() {
	    Variable var = scoreHeap.poll();
	    decisionList.add(var.variable);
        var.truthValue = true;
    }

    private void randomVarPicker() {
	    Random random = new Random();
	    int randomInteger = random.nextInt(variables.size() - 1);

	    while (variables.get(randomInteger).truthValue) {
	        randomInteger = random.nextInt(variables.size() - 1);
        }

	    decisionList.add(randomInteger);
	    variables.get(randomInteger).truthValue = random.nextBoolean();
    }
}
