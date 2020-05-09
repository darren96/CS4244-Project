package stage1;

import java.util.*;

import stage1.Clause.Satisfiability;
import utils.Logger;

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

    // variables used for data collection
    int backtrackingInvocation = 0;

    public CDCL(List<Clause> clauses, List<Variable> variables) {
        this.clauses = clauses;
        this.variables = variables;
        this.decisionLevel = 0;

        for (Clause clause : clauses) {
            for (int literal : clause.literals) {
                variables.get(Math.abs(literal)).score++;
            }
        }
        scoreHeap.addAll(variables);
    }

    public boolean checkSAT() {
        if (unitPropagation() == ClauseSatisfiability.CONFLICT) {
            Logger.printout("UNSAT");
            return false;
        }

        while (!allVarsAssigned()) {
            Logger.printout("--------------------------------");
            decisionLevel++;
            Logger.printout("Decision Level: " + decisionLevel);
            Assignment assignment = pickBranchingVar();
            assignVariable(assignment, -1);
            while (unitPropagation() == ClauseSatisfiability.CONFLICT) {
                conflictCount++;
                if(conflictCount == 256) {
                    for(Variable var : scoreHeap) {
                        var.score /= 2;
                    }
                    conflictCount = 0;
                }
                Integer beta = conflictAnalysis();
                Logger.printout("Beta: " + beta);
                if (beta < 0) {
                    Logger.printout("UNSAT");
                    return false;
                } else {
                    Logger.printout("Backtracking to level: " + beta);
                    backtrack(beta);
                    decisionLevel = beta;
                }
            }
        }

        Logger.printout("SAT");
        return true;
    }

    // iterated application of the unit clause rule
    private ClauseSatisfiability unitPropagation() {
        Logger.printout("\nUnit Propagation");
        List<Integer> propList = new ArrayList<>(findUnitClauses());
        int prop;
        boolean assignValue;
        while (propList.size() > 0) {    // check if a unit clause exists
            Logger.printout("PropList: " + propList);
            for (int i = 0; i < propList.size(); i++) {
                prop = propList.get(i);
                assignValue = prop > 0;

                if (propList.contains(-1 * prop) || (variables.get(Math.abs(prop)).truthValue != null
                        && variables.get(Math.abs(prop)).truthValue != assignValue)) {
                    Logger.printout("Conflict literal: " + prop);
                    Logger.printout("Kappa : " + kappaAntecedant);
                    Logger.printout("End Propagation\n");
                    return ClauseSatisfiability.CONFLICT;
                }
            }
            propList = new ArrayList<>(findUnitClauses());
        }

        for (int i = 0; i < clauses.size(); i++) {
            Clause clause = clauses.get(i);
            if (clause.isSatisfied == Satisfiability.UNSAT) {
                Logger.printout("UNSAT Clause: " + clause.literals);
                kappaAntecedant = i;
                Logger.printout("Kappa: " + kappaAntecedant);
                Logger.printout("End Propagation\n");
                return ClauseSatisfiability.CONFLICT;
            }

        }

        Logger.printout("End Propagation\n");
        kappaAntecedant = -1;
        return ClauseSatisfiability.UNSOLVED;
    }

    private Set<Integer> findUnitClauses() {
        Set<Integer> unitClauses = new HashSet<>();
        int lastUnassignedLiteral = 0;
        boolean assignValue;
        for (int i = 0; i < clauses.size(); i++) {
            Clause clause = clauses.get(i);
            Logger.printout("Clause assessing: " + clause.literals);
            if (clause.isSatisfied == Satisfiability.SAT) {
                Logger.printout("Clause is already satisfied");
                continue;
            }

            for (int literal : clause.literals) {
                if (variables.get(Math.abs(literal)).truthValue == null) {
                    lastUnassignedLiteral = literal;
                }
            }

            // If the clause in a unit clause
            if (clause.literals.size() - clause.assignedLiterals.size() == 1) {
                unitClauses.add(lastUnassignedLiteral);
                Logger.printout("Unit Clause Found: " + clause.literals);

                if (unitClauses.contains(-lastUnassignedLiteral)) {
                    kappaAntecedant = i;
                    break;
                }

                assignValue = lastUnassignedLiteral > 0;
                assignVariable(new Assignment(Math.abs(lastUnassignedLiteral), assignValue, decisionLevel, false), i);
                scoreHeap.remove(variables.get(Math.abs(lastUnassignedLiteral)));
            } else if (clause.literals.size() - clause.assignedLiterals.size() == 0) {
                Logger.printout("No unassigned variables");
            } else {
                Logger.printout("Not a unit clause");
            }
        }
        return unitClauses;
    }

    // tests whether all variables have been assigned excluding dummy variable 0
    private boolean allVarsAssigned() {
        return variables.stream()
                .filter(variable -> variable.variable != 0)
                .allMatch(variable -> variable.truthValue != null);
    }

    // tests whether all variables have been assigned
    private boolean allClausesSatisfied() {
        return clauses.stream()
                .allMatch(clause -> clause.isSatisfied == Satisfiability.SAT);
    }

    // ------------ Branch Picking Related Methods ------------//

    // selects a variable for truth assignment
    private Assignment pickBranchingVar() {
        return linearVarPicker();
        // return randomVarPicker();
        // return VSIDSVarPicker();
    }

    private Assignment randomVarPicker() {
        Random random = new Random();
        int randomInteger = random.nextInt(variables.size() - 1) + 1;

        while (variables.get(randomInteger).truthValue != null || randomInteger == 0) {
            randomInteger = random.nextInt(variables.size() - 1) + 1;
        }

        variables.get(randomInteger).truthValue = true;

        return new Assignment(randomInteger, true, decisionLevel, true);
    }

    // linear var picker with random truth value assignment
    private Assignment linearVarPicker() {
        int var = 0;
        Random random = new Random();
        for (Variable variable : variables) {
            if (variable.variable == 0) {
                continue;
            }
            if (variable.truthValue == null) {
                var = variables.indexOf(variable);
                break;
            }
        }

        variables.get(var).truthValue = true;

        return new Assignment(var, true, decisionLevel, true);
    }

    // picking a branch with VSIDS heuristics
    private Assignment VSIDSVarPicker() {
        Assignment newAssignment = null;
        Variable var = scoreHeap.poll();

        if (var != null) {
            var.truthValue = true;
            var.decidedLevel = decisionLevel;
            newAssignment = new Assignment(var.variable, var.truthValue, decisionLevel, true);
        }

        return newAssignment;
    }

    // ------------ Conflict Analysis Related Methods ------------//

    // analyzes the most recent conflict, learns a new clause from the conflict and
    // decide the backtracking decision level
    private Integer conflictAnalysis() {
        Clause learntClause = clauses.get(kappaAntecedant);
        Clause resolvingClause = null;
        Clause previousLearntClause = null;
        int literalsThatConflictsAtThisLevel;
        int conflictDecisionLevel = decisionLevel;
        int literal;
        Variable var;

        Logger.printout("\nAssignment List : " + assignmentList);
        Logger.printout("Conflict Decision Level: " + conflictDecisionLevel);

        if (conflictDecisionLevel == 0) {
            return -1;
        }

        while (!learntClause.equals(previousLearntClause)) {
            literalsThatConflictsAtThisLevel = 0;
            Logger.printout("Conflict Level Count: " + literalsThatConflictsAtThisLevel);

            resolvingClause = null;

            // For every literal in the conflicting clause recorded at kappa antecedant
            // Count the literal that has conflicts on this level
            for (int i = 0; i < learntClause.literals.size(); i++) {
                literal = learntClause.literals.get(i);

                Logger.printout("Literal: " + (literal));
                Logger.printout("Literal's Antecedant: " + variables.get(Math.abs(literal)).antecedant);
                Logger.printout("Literal's Assignment Level: " + findLiteralAssignmentLevel(literal));

                // if literal assignment level is the same as conflicting level
                if (findLiteralAssignmentLevel(literal) == conflictDecisionLevel) {
                    literalsThatConflictsAtThisLevel++;
                }

                // if literal assignment level is the same as conflicting level
                // and its antecedant clause is assigned
                if (findLiteralAssignmentLevel(literal) == conflictDecisionLevel
                        && variables.get(Math.abs(literal)).antecedant != -1) {
                    resolvingClause = clauses.get(variables.get(Math.abs(literal)).antecedant);
                }
            }

            // if there is only one literal conflict at this level, break the loop
            if (literalsThatConflictsAtThisLevel == 1) {
                break;
            }

            // Resolve the clause with the conflicting clause and resolving literal
            // and add newly learnt clause based on the resolution
            previousLearntClause = learntClause;
            Logger.printout("\nLearnt Clause: " + learntClause.literals);
            if (resolvingClause != null) {
                Logger.printout("Resolving Clause: " + resolvingClause.literals);
                learntClause = new Clause(resolve(learntClause.literals, resolvingClause.literals));
            }
        }

        Logger.printout("\nLearnt Clause: " + learntClause.literals);

        if (!clauses.contains(learntClause)) {
            // add the newly created clause as a new clause
            Logger.printout("New Clause: " + learntClause.literals);
            Clause finalLearntClause = learntClause;
            learntClause.literals.stream()
                    .filter(lit -> assignmentList.stream().anyMatch(assignment -> assignment.variable == Math.abs(lit)))
                    .forEach(lit -> finalLearntClause.assignedLiterals.add(Math.abs(lit)));
            clauses.add(finalLearntClause);
        }

        // update the scores of the lit4erals that is in the new clause
        for (int i = 0; i < learntClause.literals.size(); i++) {
            literal = learntClause.literals.get(i);

            var = variables.get(Math.abs(literal));

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
            int decisionLevelHere = variables.get(Math.abs(literal)).decidedLevel;

            // if the decision level is not the conflicting decision level
            // and it is larger than backtracking decision level, set the beta
            if (decisionLevelHere != conflictDecisionLevel
                    && decisionLevelHere > backtrackingDecisionLevel) {
                backtrackingDecisionLevel = decisionLevelHere;
            }
        }

        return backtrackingDecisionLevel;
    }

    // backtracks to a decision level where it removes assignments on each respective decision level
    private void backtrack(int beta) {
        backtrackingInvocation++;
        Variable var;
        for (int i = assignmentList.size() - 1; i >= 0; i--) {
            if (assignmentList.get(i).decisionLevel <= beta) {
                break;
            }

            Logger.printout("Removing assignment: " + assignmentList.get(i));

            // Add assigned variable back to score heap
            var = variables.get(Math.abs(assignmentList.get(i).variable));
            if(!scoreHeap.contains(var)) {
                scoreHeap.add(var);
            }

            unassignVariable(assignmentList.get(i));
            assignmentList.remove(i);
        }

        kappaAntecedant = -1;
        Logger.printout("Reset kappaAntecedant: " + kappaAntecedant);
    }

    // Resolve the 2 given clause and produce the new clause
    private List<Integer> resolve(List<Integer> firstClauseLiterals, List<Integer> resolvingClause) {
        Set<Integer> literalsSet = new HashSet<>();
        literalsSet.addAll(firstClauseLiterals);
        literalsSet.addAll(resolvingClause);

        for (int resolvingLiteral : resolvingClause) {
            if (literalsSet.contains(-resolvingLiteral)) {
                literalsSet.remove(resolvingLiteral);
                literalsSet.remove((-1) * resolvingLiteral);
            }
        }

        return new ArrayList<>(literalsSet);
    }

    // ------------ Assignment Related Methods ------------//

    // Find the assignment decision level with the given variable
    private int findLiteralAssignmentLevel(int variable) {
        int assignmentLevel = -1;

        for (int i = 0; i < assignmentList.size(); i++) {
            if (assignmentList.get(i).variable == Math.abs(variable)) {
                assignmentLevel = assignmentList.get(i).decisionLevel;
            }
        }

        return assignmentLevel;
    }

    // Assign the variable and update the related clauses' antecedant, satisfiability and assigned literals
    private void assignVariable(Assignment assignment, int antecedant) {
        int literal = assignment.truthValue ? assignment.variable : -1 * assignment.variable;
        clauses.stream()
                .filter(clause -> clause.literals.contains(literal)
                    && clause.isSatisfied == Satisfiability.UNDECIDE)
                .forEach(clause -> clause.isSatisfied = Satisfiability.SAT);

        clauses.stream()
                .filter(clause -> (clause.literals.contains(literal) || clause.literals.contains(-literal)))
                .forEach(clause -> clause.assignedLiterals.add(assignment.variable));

        clauses.stream()
                .filter(clause -> clause.literals.contains(-literal)
                        && clause.literals.size() - clause.assignedLiterals.size() == 0
                        && clause.isSatisfied == Satisfiability.UNDECIDE)
                .forEach(clause -> clause.isSatisfied = Satisfiability.UNSAT);

        variables.get(assignment.variable).antecedant = antecedant;
        variables.get(assignment.variable).truthValue = assignment.truthValue;
        variables.get(assignment.variable).decidedLevel = assignment.decisionLevel;

        assignmentList.add(assignment);

        Logger.printout(String.format("Variables %s assigned to value %b", assignment.variable,
                assignment.truthValue));
        Logger.printout(String.format("Antecedant clause %d assigned to literal %d",
                antecedant, assignment.variable));
    }

    // Unassign the variable and update the related clauses' antecedant, satisfiability and assigned literals
    private void unassignVariable(Assignment assignment) {
        int literal = assignment.truthValue ? assignment.variable : -1 * assignment.variable;
        clauses.stream()
                .filter(clause -> clause.literals.contains(literal) || clause.literals.contains(-literal))
                .forEach(clause -> {
                    clause.isSatisfied = Satisfiability.UNDECIDE;
                    clause.assignedLiterals.remove(assignment.variable);
                });

        variables.get(assignment.variable).antecedant = -1;
        variables.get(assignment.variable).truthValue = null;
        variables.get(assignment.variable).decidedLevel = -1;
    }
}
