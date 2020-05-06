package stage1;

import java.util.*;

import stage1.Clause.Satisfiability;

public class CDCL {

    public enum ClauseSatisfiability {
        UNSOLVED, CONFLICT
    }

    List<Clause> clauses;
    List<Variable> variables;
    int decisionLevel;
    List<Assignment> assignmentList = new ArrayList<>();
    int kappaAntecedant = -1;

    PriorityQueue<Variable> scoreHeap = new PriorityQueue<>((v1, v2) ->
            (!v2.score.equals(v1.score) ? v2.score.compareTo(v1.score) : v2.variable.compareTo(v1.variable)));
    int conflictCount = 0;

    public CDCL(List<Clause> clauses, List<Variable> variables) {
        this.clauses = clauses;
        this.variables = variables;
        this.decisionLevel = 0;

        for (Clause clause : clauses) {
            for (int literal : clause.literals) {
                variables.get(Math.abs(literal)-1).score++;
            }
        }
        scoreHeap.addAll(variables);
    }

    public boolean checkSAT() {
        if (unitPropagation() == ClauseSatisfiability.CONFLICT) {
            System.out.println("UNSAT");
            return false;
        }

        while (!allVarsAssigned() && !allClausesSatisfied()) {
            System.out.println("--------------------------------");
            System.out.println("Not All Variables are assigned");
            System.out.println("Decision Level: " + decisionLevel);
            decisionLevel++;
            Assignment assignment = pickBranchingVar();
            assignLiteral(assignment, -1);
            while (unitPropagation() == ClauseSatisfiability.CONFLICT) {
                conflictCount++;
                if(conflictCount == 256) {
                    for(Variable var : scoreHeap) {
                        var.score /= 2;
                    }
                    conflictCount = 0;
                }
                Integer beta = conflictAnalysis();
                System.out.println("Beta: " + beta);
                if (beta < 0) {
                    System.out.println("UNSAT");
                    return false;
                } else {
                    System.out.println("Backtracking to level: " + beta);
                    backtrack(beta);
                    decisionLevel = beta;
                }
            }
        }

        System.out.println("SAT");
        return true;
    }

    // iterated application of the unit clause rule
    private ClauseSatisfiability unitPropagation() {
        System.out.println("\nUnit Propagation");
        List<Integer> propList = new ArrayList<>(findUnitClauses());
        int prop;
        boolean assignValue;
        while (propList.size() > 0) {    // check if a unit clause exists
            System.out.println("PropList: " + propList);
            for (int i = 0; i < propList.size(); i++) {
                prop = propList.get(i);
                assignValue = prop > 0;

                if (propList.contains(-1 * prop) || (variables.get(Math.abs(prop) - 1).truthValue != null
                        && variables.get(Math.abs(prop) - 1).truthValue != assignValue)) {
                    System.out.println("Conflict literal: " + prop);
                    System.out.println("Kappa : " + kappaAntecedant);
                    System.out.println("End Propagation\n");
                    return ClauseSatisfiability.CONFLICT;
                }

                for (int j = 0; j < clauses.size(); j++) {
                    Clause clause = clauses.get(j);
                    if (clause.isSatisfied == Satisfiability.UNDECIDE && clause.assignedLiterals == clause.literals.size()) {
                        System.out.println("UNSAT Conflicting Clause: " + clause.literals);
                        clause.isSatisfied = Satisfiability.UNSAT;
                        kappaAntecedant = j;
                        System.out.println("Kappa : " + kappaAntecedant);
                        System.out.println("End Propagation\n");
                        return ClauseSatisfiability.CONFLICT;
                    }
                }
            }
            propList = new ArrayList<>(findUnitClauses());
        }

        if (kappaAntecedant != -1) {
            System.out.println("Kappa : " + kappaAntecedant);
            System.out.println("End Propagation\n");
            return ClauseSatisfiability.CONFLICT;
        }

        System.out.println("End Propagation\n");
        kappaAntecedant = -1;
        return ClauseSatisfiability.UNSOLVED;
    }

    private Set<Integer> findUnitClauses() {
        Set<Integer> unitClauses = new HashSet<>();
        int unassignedCount = 0;
        int lastUnassignedLiteral = 0;
        boolean assignValue;
        for (int i = 0; i < clauses.size(); i++) {
            Clause clause = clauses.get(i);
            unassignedCount = 0;
            System.out.println("Clause assessing: " + clause.literals);
            if (clause.isSatisfied == Satisfiability.SAT) {
                System.out.println("Clause is already satisfied");
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

                assignValue = lastUnassignedLiteral > 0;
                assignLiteral(new Assignment(Math.abs(lastUnassignedLiteral), assignValue, decisionLevel, false), i);
                scoreHeap.remove(variables.get(Math.abs(lastUnassignedLiteral)-1));
            } else if (unassignedCount == 0) {
                System.out.println("No unassigned variables");
            } else {
                System.out.println("Not a unit clause");
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

    // tests whether all variables have been assigned
    private boolean allClausesSatisfied() {
        return clauses.stream()
                .allMatch(clause -> clause.isSatisfied == Satisfiability.SAT);
    }

    // selects a variable for truth assignment
    private Assignment pickBranchingVar() {
//        return randomVarPicker();
        return VSIDSVarPicker();
    }

    private Assignment randomVarPicker() {
        Random random = new Random();
        int randomInteger = random.nextInt(variables.size()-1);

        while (variables.get(randomInteger).truthValue != null) {
            randomInteger = random.nextInt(variables.size());
        }

        boolean randomTruthValue = random.nextBoolean();
        randomTruthValue = false;
        variables.get(randomInteger).truthValue = randomTruthValue;

        return new Assignment(randomInteger + 1, randomTruthValue, decisionLevel, true);
    }

    // linear var picker with random truth value assignment
    private Assignment linearVarPicker() {
        int literalIndex = -1;
        Random random = new Random();
        for (Variable variable : variables) {
            if (variable.truthValue == null) {
                literalIndex = variables.indexOf(variable);
                break;
            }
        }

        boolean randomTruthValue = random.nextBoolean();
        variables.get(literalIndex).truthValue = randomTruthValue;

        return new Assignment(literalIndex + 1, randomTruthValue, decisionLevel, true);
    }

    private Assignment VSIDSVarPicker() {
        //TODO: how to choose which truth value to assign

        Assignment newAssignment = null;
        Variable var = scoreHeap.poll();

        if (var != null) {
            var.truthValue = true;
            var.decidedLevel = decisionLevel;
            newAssignment = new Assignment(var.variable, var.truthValue, decisionLevel, true);
        }

        return newAssignment;
    }

    // analyzes the most recent conflict and learns a new clause from the conflict
    private Integer conflictAnalysis() {
        Clause learntClause = clauses.get(kappaAntecedant);
        int literalsThatConflictsAtThisLevel;
        int conflictDecisionLevel = decisionLevel;
        int resolvingLiteral = -1;
        int literal;
        Variable var;
        List<Assignment> tempAssignmentList = new ArrayList<>(assignmentList);

        System.out.println("\nAssignment List : " + assignmentList);
        System.out.println("Conflict Decision Level: " + conflictDecisionLevel);

        if (conflictDecisionLevel == -1) {
            return -1;
        }

        while (true) {
            literalsThatConflictsAtThisLevel = 0;
            System.out.println("Conflict Level Count: " + literalsThatConflictsAtThisLevel);

            // For every literal in the conflicting clause recorded at kappa antecedant
            // Count the literal that has conflicts on this level
            for (int i = 0; i < learntClause.literals.size(); i++) {
                literal = learntClause.literals.get(i);

                System.out.println("Literal: " + (literal));
                System.out.println("Literal's Antecedant: " + variables.get(Math.abs(literal) - 1).antecedant);
                System.out.println("Literal's Assignment Level: " + findLiteralAssignmentLevel(literal));

                // if literal assignment level is the same as conflicting level
                if (findLiteralAssignmentLevel(literal) == conflictDecisionLevel) {
                    literalsThatConflictsAtThisLevel++;
                }
            }

            // if there is only one literal conflict at this level, break the loop
            if (literalsThatConflictsAtThisLevel == 1) {
                break;
            }

            // Get last assigned variable at the conflict level
            resolvingLiteral = tempAssignmentList.remove(tempAssignmentList.size()-1).variable;

            // Resolve the clause with the conflicting clause and resolving literal
            // and add newly learnt clause based on the resolution
            System.out.println("\nResolving Clause: " + learntClause.literals);
            System.out.println("Resolving Literals: " + resolvingLiteral);
            learntClause = new Clause(resolve(learntClause.literals, resolvingLiteral));
        }

        System.out.println("\nLearnt Clause: " + learntClause.literals);

        // add the newly created clause as a new clause
        clauses.add(learntClause);

        // update the scores of the literals that is in the new clause
        for (int i = 0; i < learntClause.literals.size(); i++) {
            literal = learntClause.literals.get(i);
            // Polarity update (reserved)
            // boolean update = (literal > 0) ? true : false;
            // variables.get(Math.abs(literal)).polarity += update;

            var = variables.get(Math.abs(literal)-1);

            // Update score heap
            if(scoreHeap.contains(var)) {
                scoreHeap.remove(var);
                var.score++;
                scoreHeap.add(var);
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
        Variable var;
        for (int i = assignmentList.size() - 1; i >= 0; i--) {
            if (assignmentList.get(i).decisionLevel <= beta) {
                break;
            }

            System.out.println("Removing assignment: " + assignmentList.get(i));

            // Add assigned variable back to score heap
            var = variables.get(Math.abs(assignmentList.get(i).variable)-1);
            if(!scoreHeap.contains(var)) {
                scoreHeap.add(var);
            }

            unassignLiteral(assignmentList.get(i));
            assignmentList.remove(i);
        }

        kappaAntecedant = -1;
        System.out.println("Reset kappaAntecedant: " + kappaAntecedant);
    }

    // resolve the clause with the given literal
//    private List<Integer> resolve(List<Integer> inputClauseLiterals, int resolvingLiteral) {
//        Set<Integer> literalsSet = new HashSet<>(inputClauseLiterals);
//        List<Integer> secondClauseLiterals = clauses
//                .get(variables.get(resolvingLiteral-1).antecedant).literals;
//        literalsSet.addAll(secondClauseLiterals);
//
//        Integer currentLiteral = 0;
//        ListIterator<Integer> literalIterator = inputClauseLiterals.listIterator();
//        while (literalIterator.hasNext()) {
//            currentLiteral = literalIterator.next();
//            if (Math.abs(currentLiteral) == resolvingLiteral) {
//                literalIterator.remove();
//            }
//            if (literalsSet.contains(currentLiteral) || literalsSet.contains(-currentLiteral)) {
//                literalsSet.remove(currentLiteral);
//                literalsSet.remove(-currentLiteral);
//            }
//        }
//
//        return new ArrayList<>(literalsSet);
//    }

    private List<Integer> resolve(List<Integer> firstClauseLiterals, int resolvingLiteral) {
        List<Integer> secondClauseLiterals = clauses.get(variables.get(Math.abs(resolvingLiteral)-1).antecedant).literals;
        Set<Integer> literalsSet = new HashSet<>();
        literalsSet.addAll(firstClauseLiterals);
        literalsSet.addAll(secondClauseLiterals);

        while(literalsSet.contains(resolvingLiteral)) {
            literalsSet.remove(resolvingLiteral);
        }
        while(literalsSet.contains((-1)*resolvingLiteral)) {
            literalsSet.remove((-1)*resolvingLiteral);
        }

        return new ArrayList<>(literalsSet);
    }

    private int findLiteralAssignmentLevel(int literal) {
        int assignmentLevel = -1;

        for (int i = 0; i < assignmentList.size(); i++) {
            if (assignmentList.get(i).variable == Math.abs(literal)) {
                assignmentLevel = assignmentList.get(i).decisionLevel;
            }
        }

        return assignmentLevel;
    }

    private void assignLiteral(Assignment assignment, int antecedant) {
        int literal = assignment.truthValue ? assignment.variable : -1 * assignment.variable;
        clauses.stream()
                .filter(clause -> clause.literals.contains(literal))
                .forEach(clause -> {
                    clause.isSatisfied = Satisfiability.SAT;
                    clause.assignedLiterals++;
                });

        variables.get(assignment.variable - 1).antecedant = antecedant;
        variables.get(assignment.variable - 1).truthValue = assignment.truthValue;

        assignmentList.add(assignment);

        System.out.println(String.format("Variables %s assigned to value %b", assignment.variable,
                assignment.truthValue));
        System.out.println(String.format("Antecedant clause %d assigned to literal %d",
                antecedant, assignment.variable));
    }

    private void unassignLiteral(Assignment assignment) {
        int literal = assignment.truthValue ? assignment.variable : -1 * assignment.variable;

        clauses.stream()
                .filter(clause -> clause.literals.contains(literal))
                .forEach(clause -> {
                    clause.isSatisfied = Satisfiability.UNDECIDE;
                    clause.assignedLiterals--;
                });

        variables.get(assignment.variable - 1).antecedant = -1;
        variables.get(assignment.variable - 1).truthValue = null;
    }
}
