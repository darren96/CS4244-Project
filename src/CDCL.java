import javafx.util.Pair;

import java.util.*;
import java.util.stream.Collectors;

public class CDCL {

    public enum ClauseSatisfiability {
        UNSOLVED, CONFLICT
    }

	List<Clause> clauses;
	List<Variable> variables;
	int decisionLevel;
	List<Pair<Integer, Boolean>> assignmentList = new ArrayList<>();
	PriorityQueue<Variable> scoreHeap = new PriorityQueue<>((v1, v2) -> v2.score.compareTo(v1.score));
	int kappaAntecedant = -1;

	public CDCL(List<Clause> clauses, List<Variable> variables) {
		this.clauses = clauses;
		this.variables = variables;
		this.decisionLevel = 0;
	}

    public void checkSAT() {
        if(unitPropagation() == ClauseSatisfiability.CONFLICT) {
            System.out.println("UNSAT");
            return;
        }

        while(!allVarsAssigned()) {
            System.out.println("--------------------------------");
            System.out.println("Not All Variables are assigned");
            System.out.println("Decision Level: " + decisionLevel);
            Pair<Integer, Boolean> assignment = pickBranchingVar();
            System.out.println("Assignment: " + assignment);
            assignmentList.add(assignment);
            decisionLevel++;
            if(unitPropagation() == ClauseSatisfiability.CONFLICT) {
               Integer beta = conflictAnalysis();
               System.out.println("Beta: " + beta);
               if(beta < 0) {
                   System.out.println("UNSAT");
                   return;
               } else {
                   System.out.println("Backtracking to level: " + beta);
                   backtrack(beta);
                   decisionLevel = beta;
               }
            }
        }

        System.out.println("SAT");
    }

    // iterated application of the unit clause rule
    private ClauseSatisfiability unitPropagation() {
        ArrayList<Integer> propList = findUnitClauses();
        int prop;
        while (propList.size() > 0) {    // check if a unit clause exists
            System.out.println("PropList: " + propList);
            for (int i = 0; i < propList.size(); i++) {
                prop = propList.get(i);
                if (propList.contains(-1 * prop)) {
                    System.out.println("Conflict literal: " + prop);
                    System.out.println("Kappa : " + kappaAntecedant);
                    return ClauseSatisfiability.CONFLICT;
                }

                for (Clause clause : clauses) {
                    if (clause.literals.contains(prop)) {
                        // Clause which is sat shall be persist
                        clause.isSatisfied = true;
                        System.out.println("Satisfying Clause: " + clause.literals);
                    }
                }
            }
            propList = findUnitClauses();
        }

        kappaAntecedant = -1;
        return ClauseSatisfiability.UNSOLVED;
    }

    private ArrayList<Integer> findUnitClauses() {
        ArrayList<Integer> unitClauses = new ArrayList<>();
        int unassignedCount = 0;
        int lastUnassignedLiteral = 0;
        for (int i = 0; i < clauses.size(); i++) {
            Clause clause = clauses.get(i);
            unassignedCount = 0;

            if (clause.isSatisfied) {
                continue;
            }

            for (int literal : clause.literals) {
                if (variables.get(Math.abs(literal) - 1).truthValue == null) {
                    unassignedCount++;
                    lastUnassignedLiteral = literal;
                }
            }

            // If the clause in a unit clause
            if (unassignedCount == 1) {
                unitClauses.add(lastUnassignedLiteral);
                System.out.println("Unit Clause Found: " + clause.literals);

                if (unitClauses.contains(-lastUnassignedLiteral)) {
                    kappaAntecedant = i;
                    break;
                }

                variables.get(Math.abs(lastUnassignedLiteral) - 1).antecedant = i;
                System.out.println("Antecedant clause " + i + " assigned to literal "
                        + Math.abs(lastUnassignedLiteral));
            }
        }
        System.out.println();
        return unitClauses;
    }

    // tests whether all variables have been assigned
    private boolean allVarsAssigned() {
        return variables.stream()
                .allMatch(variable -> variable.truthValue != null);
    }

    // selects a variable for truth assignment
    private Pair<Integer, Boolean> pickBranchingVar() {
        return randomVarPicker();
    }

    // analyzes the most recent conflict and learns a new clause from the conflict
    private Integer conflictAnalysis() {
        Clause learntClause = clauses.get(kappaAntecedant);
        int literalsThatConflictsAtThisLevel;
        int conflictDecisionLevel = decisionLevel;
        int resolvingLiteral = -1;
        int literal;

        System.out.println("\nAssignment List: " + assignmentList);
        System.out.println("Conflict Decision Level: " + conflictDecisionLevel);


        while (true) {
            literalsThatConflictsAtThisLevel = 0;
            System.out.println("Conflict Level Count: " + literalsThatConflictsAtThisLevel);

            // For every literal in the conflicting clause recorded at kappa antecedant
            // Count the literal that has conflicts on this level
            for (int i = 0; i < learntClause.literals.size(); i++) {
                literal = Math.abs(learntClause.literals.get(i)) - 1;

                System.out.println("Literal: " + (literal));
                System.out.println("Literal's Antecedant: " + variables.get(literal).antecedant);
                System.out.println("Literal's Assignment Level: " + findLiteralAssignmentLevel(literal));

                // if literal assignment level is the same as conflicting level
                if (findLiteralAssignmentLevel(literal) == conflictDecisionLevel) {
                    literalsThatConflictsAtThisLevel++;
                }

                // if literal assignment level is the same as conflicting level
                // and its antecedant clause is assigned
                if (findLiteralAssignmentLevel(literal) == conflictDecisionLevel
                        && variables.get(literal).antecedant != -1) {
                    resolvingLiteral = literal;
                }
            }

            // if there is only one literal conflict at this level, break the loop
            if (literalsThatConflictsAtThisLevel == 1) {
                break;
            }

            // Resolve the clause with the conflicting clause and resolving literal
            // and add newly learnt clause based on the resolution
            System.out.println("\nResolving Clause: " + learntClause.literals);
            System.out.println("Resolving Literals: " + resolvingLiteral);
            learntClause = new Clause(resolve(learntClause.literals, resolvingLiteral));
        }

        System.out.println("\nLearnt Clause: " + learntClause.literals);

        // add the newly created clause as a new clause
        clauses.add(learntClause);

        // update the occurences of the literals that is in the new clause
        for (int i = 0; i < learntClause.literals.size(); i++) {
            literal = learntClause.literals.get(i);
            // Polarity update (reserved)
            // boolean update = (literal > 0) ? true : false;
            // variables.get(Math.abs(literal)).polarity += update;

            if (variables.get(Math.abs(literal) - 1).occurences != -1) {
                variables.get(Math.abs(literal) - 1).occurences++;
            }
        }


        int backtrackingDecisionLevel = 0;
        // for every literals in the new clause
        for (int i = 0; i < learntClause.literals.size(); i++) {
            literal = learntClause.literals.get(i);
            int decisionLevelHere = variables.get(Math.abs(literal) - 1).decidedLevel;

            // if the decision level is not the conflicting decision level
            // and it is larger than backtracking decision level, set the beta
            if (decisionLevelHere != conflictDecisionLevel
                    && decisionLevelHere > backtrackingDecisionLevel) {
                backtrackingDecisionLevel = decisionLevelHere;
            }
        }

        return backtrackingDecisionLevel;
    }

    // backtracks to a decision level
    private void backtrack(int beta) {
        for (int i = assignmentList.size() - 1; i >= beta; i--){
            System.out.println("Removing assignment: " + assignmentList.get(i));
            variables.get(assignmentList.get(i).getKey()).truthValue = null;
            assignmentList.remove(i);
        }
    }

    private void scoreVarPicker() {
	    Variable var = scoreHeap.poll();
//	    assignmentList.add(var.variable);
        var.truthValue = true;
    }

    private Pair<Integer, Boolean> randomVarPicker() {
	    Random random = new Random();
	    int randomInteger = random.nextInt(variables.size() - 1);

	    while (variables.get(randomInteger).truthValue != null) {
	        randomInteger = random.nextInt(variables.size());
        }

	    Boolean randomTruthValue = random.nextBoolean();
	    variables.get(randomInteger).truthValue = randomTruthValue;

	    return new Pair<>(randomInteger, randomTruthValue);
    }

    // linear var picker with random truth value assignment
    private Pair<Integer, Boolean> linearVarPicker() {
        int literal = -1;
        Random random = new Random();
        for (Variable variable : variables) {
            if (variable.truthValue == null) {
                literal = variables.indexOf(variable);
                break;
            }
        }

        Boolean randomTruthValue = random.nextBoolean();
        variables.get(literal).truthValue = randomTruthValue;

        return new Pair<>(literal, randomTruthValue);
    }

    // resolve the clause with the given literal
    private List<Integer> resolve(List<Integer> inputClauseLiterals, int resolvingLiteral) {
	    Set<Integer> literalsSet = new HashSet<>();
	    List<Integer> secondClauseLiterals = clauses
                .get(variables.get(resolvingLiteral).antecedant).literals;
	    literalsSet.addAll(secondClauseLiterals);

	    Integer currentLiteral = 0;
	    ListIterator<Integer> literalIterator = inputClauseLiterals.listIterator();
	    while (literalIterator.hasNext()) {
            currentLiteral = literalIterator.next();
	        if (Math.abs(currentLiteral) == resolvingLiteral) {
                literalIterator.remove();
            }
        }

        return literalsSet.stream().collect(Collectors.toList());
    }

    private int findLiteralAssignmentLevel(int literal) {
	    int assingmentLevel = -1;

	    for (int i = 0; i < assignmentList.size(); i++) {
	        if (assignmentList.get(i).getKey() == literal) {
	            assingmentLevel = i + 1;
            }
        }

	    return assingmentLevel;
    }
}
