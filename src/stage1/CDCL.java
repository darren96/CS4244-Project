package stage1;

import java.util.*;
import java.util.logging.Handler;
import java.util.logging.Logger;

import stage1.Clause.Satisfiability;

public class CDCL {

    public enum ClauseSatisfiability {
        UNSOLVED, CONFLICT
    }

    List<Clause> clauses;
    List<Variable> variables;
    int decisionLevel;
    List<Assignment> assignmentList = new ArrayList<>();
    PriorityQueue<Variable> scoreHeap = new PriorityQueue<>((v1, v2) -> v2.score.compareTo(v1.score));
    int kappaAntecedant = -1;
    Logger logger = Logger.getLogger(CDCL.class.getName());

    public CDCL(List<Clause> clauses, List<Variable> variables) {
        this.clauses = clauses;
        this.variables = variables;
        this.decisionLevel = 0;
    }

    public boolean checkSAT() {
        logger.setUseParentHandlers(false);
        if (unitPropagation() == ClauseSatisfiability.CONFLICT) {
            System.out.println("UNSAT");
            return false;
        }

        while (!allVarsAssigned()) {
            logger.info("--------------------------------");
            logger.info("Not All Variables are assigned");
            logger.info("Decision Level: " + decisionLevel);
            Assignment assignment = pickBranchingVar();
            assignLiteral(assignment, -1);
            decisionLevel++;
            if (unitPropagation() == ClauseSatisfiability.CONFLICT) {
                Integer beta = conflictAnalysis();
                logger.info("Beta: " + beta);
                if (beta < 0) {
                    System.out.println("UNSAT");
                    return false;
                } else {
                    logger.info("Backtracking to level: " + beta);
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
        logger.info("\nUnit Propagation");
        List<Integer> propList = new ArrayList<>(findUnitClauses());
        int prop;
        boolean assignValue;
        while (propList.size() > 0) {    // check if a unit clause exists
            logger.info("PropList: " + propList);
            for (int i = 0; i < propList.size(); i++) {
                prop = propList.get(i);
                assignValue = prop > 0;

                if (propList.contains(-1 * prop) || (variables.get(Math.abs(prop) - 1).truthValue != null
                        && variables.get(Math.abs(prop) - 1).truthValue != assignValue)) {
                    logger.info("Conflict literal: " + prop);
                    logger.info("Kappa: " + kappaAntecedant);
                    logger.info("End Propagation\n");
                    return ClauseSatisfiability.CONFLICT;
                }
            }
            propList = new ArrayList<>(findUnitClauses());
        }

        for (int i = 0; i < clauses.size(); i++) {
            Clause clause = clauses.get(i);
            if (clause.isSatisfied == Satisfiability.UNSAT) {
                logger.info("UNSAT Clause: " + clause.literals);
                kappaAntecedant = i;
                logger.info("Kappa: " + kappaAntecedant);
                logger.info("End Propagation\n");
                return ClauseSatisfiability.CONFLICT;
            }
        }

        logger.info("End Propagation\n");
        kappaAntecedant = -1;
        return ClauseSatisfiability.UNSOLVED;
    }

    private Set<Integer> findUnitClauses() {
        Set<Integer> unitClauses = new HashSet<>();
        int lastUnassignedLiteral = 0;
        boolean assignValue;
        for (int i = 0; i < clauses.size(); i++) {
            Clause clause = clauses.get(i);
//            logger.info("Clause assessing: " + clause.literals);
//            logger.info("Unassigned literals: " + (clause.literals.size() - clause.assignedLiterals.size()));
            if (clause.isSatisfied == Satisfiability.SAT) {
//                logger.info("Clause is already satisfied");
                continue;
            }

            for (int literal : clause.literals) {
                if (variables.get(Math.abs(literal) - 1).truthValue == null) {
                    lastUnassignedLiteral = literal;
                }
            }

            // If the clause in a unit clause
            if (clause.literals.size() - clause.assignedLiterals.size() == 1) {
                unitClauses.add(lastUnassignedLiteral);
//                logger.info("Unit Clause Found: " + clause.literals);

                if (unitClauses.contains(-lastUnassignedLiteral)) {
                    kappaAntecedant = i;
                    break;
                }

                assignValue = lastUnassignedLiteral > 0;
                assignLiteral(new Assignment(Math.abs(lastUnassignedLiteral), assignValue, decisionLevel, false),
                        i);
            }
        }
        return unitClauses;
    }

    // tests whether all variables have been assigned
    private boolean allVarsAssigned() {
        return variables.stream()
                .allMatch(variable -> variable.truthValue != null);
    }

    // selects a variable for truth assignment
    private Assignment pickBranchingVar() {
        return randomVarPicker();
    }

    // analyzes the most recent conflict and learns a new clause from the conflict
    private Integer conflictAnalysis() {
        Clause learntClause = clauses.get(kappaAntecedant);
        Clause resolvingClause = null;
        Clause previousLearntClause = null;
        int literalsThatConflictsAtThisLevel;
        int conflictDecisionLevel = decisionLevel;
        int literal;

        logger.info("\nAssignment List : " + assignmentList);
        logger.info("Conflict Decision Level: " + conflictDecisionLevel);

        if (conflictDecisionLevel == 0) {
            return -1;
        }

        while (!learntClause.equals(previousLearntClause)) {
            literalsThatConflictsAtThisLevel = 0;
            logger.info("Conflict Level Count: " + literalsThatConflictsAtThisLevel);

            resolvingClause = null;

            // For every literal in the conflicting clause recorded at kappa antecedant
            // Count the literal that has conflicts on this level
            for (int i = 0; i < learntClause.literals.size(); i++) {
                literal = learntClause.literals.get(i);

//                logger.info("Literal: " + (literal));
//                logger.info("Literal's Antecedant: " + variables.get(Math.abs(literal) - 1).antecedant);
//                logger.info("Literal's Assignment Level: " + findLiteralAssignmentLevel(literal));

                // if literal assignment level is the same as conflicting level
                if (findLiteralAssignmentLevel(literal) == conflictDecisionLevel) {
                    literalsThatConflictsAtThisLevel++;
                }

                // if literal assignment level is the same as conflicting level
                // and its antecedant clause is assigned
                if (findLiteralAssignmentLevel(literal) == conflictDecisionLevel
                        && variables.get(Math.abs(literal) - 1).antecedant != -1) {
                    resolvingClause = clauses.get(variables.get(Math.abs(literal) - 1).antecedant);
                }
            }

            // if there is only one literal conflict at this level, break the loop
            if (literalsThatConflictsAtThisLevel == 1) {
                break;
            }

            // Resolve the clause with the conflicting clause and resolving literal
            // and add newly learnt clause based on the resolution
            logger.info("\nPrevious Learnt Clause: " + learntClause.literals);
            previousLearntClause = learntClause;
            if (resolvingClause != null) {
//                logger.info("Resolving Clause: " + resolvingClause.literals);
                learntClause = new Clause(resolve(learntClause.literals, resolvingClause));
            }
        }

        logger.info("\nLearnt Clause: " + learntClause.literals);

        if (resolvingClause != null) {
            // add the newly created clause as a new clause
            clauses.add(learntClause);
        }

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
        for (int i = assignmentList.size() - 1; i >= 0; i--) {

            if (assignmentList.get(i).decisionLevel < beta) {
                break;
            }

//            logger.info("Removing assignment: " + assignmentList.get(i));
            unassignLiteral(assignmentList.get(i));
            assignmentList.remove(i);
        }
    }

    private void scoreVarPicker() {
        Variable var = scoreHeap.poll();
//	    assignmentList.add(var.variable);
        var.truthValue = true;
    }

    private Assignment randomVarPicker() {
        Random random = new Random();
        int randomInteger = random.nextInt(variables.size() - 1);

        while (variables.get(randomInteger).truthValue != null) {
            randomInteger = random.nextInt(variables.size());
        }

        boolean randomTruthValue = random.nextBoolean();
        variables.get(randomInteger).truthValue = randomTruthValue;

        return new Assignment(randomInteger + 1, randomTruthValue, decisionLevel, true);
    }

    // linear var picker with random truth value assignment
    private Assignment linearVarPicker() {
        int literal = -1;
        Random random = new Random();
        for (Variable variable : variables) {
            if (variable.truthValue == null) {
                literal = variables.indexOf(variable);
                break;
            }
        }

        boolean randomTruthValue = random.nextBoolean();
        variables.get(literal).truthValue = randomTruthValue;

        return new Assignment(literal + 1, randomTruthValue, decisionLevel, true);
    }

    // resolve the clause with the given literal
    private List<Integer> resolve(List<Integer> inputClauseLiterals, Clause resolvingClause) {
        Set<Integer> literalsSet = new HashSet<>(inputClauseLiterals);
        List<Integer> secondClauseLiterals = resolvingClause.literals;
        literalsSet.addAll(secondClauseLiterals);

        Integer currentLiteral = 0;
        ListIterator<Integer> literalIterator = inputClauseLiterals.listIterator();
        while (literalIterator.hasNext()) {
            currentLiteral = literalIterator.next();
            if (literalsSet.contains(currentLiteral) || literalsSet.contains(-currentLiteral)) {
                literalsSet.remove(currentLiteral);
                literalsSet.remove(-currentLiteral);
            }
        }

        return new ArrayList<>(literalsSet);
    }

    private int findLiteralAssignmentLevel(int literal) {
        int assignmentLevel = -1;

        for (int i = 0; i < assignmentList.size(); i++) {
            if (assignmentList.get(i).literal == literal) {
                assignmentLevel = assignmentList.get(i).decisionLevel;
            }
        }

        return assignmentLevel;
    }

    private void assignLiteral(Assignment assignment, int antecedant) {
        int literal = assignment.truthValue ? assignment.literal : -1 * assignment.literal;
        clauses.stream()
                .filter(clause -> clause.literals.contains(literal))
                .forEach(clause -> clause.isSatisfied = Satisfiability.SAT);

        clauses.stream()
                .filter(clause -> (clause.literals.contains(literal) || clause.literals.contains(-literal)))
                .forEach(clause -> clause.assignedLiterals.add(literal));

        clauses.stream()
                .filter(clause -> clause.literals.contains(-literal)
                        && clause.literals.size() - clause.assignedLiterals.size() == 0
                        && clause.isSatisfied == Satisfiability.UNDECIDE)
                .forEach(clause -> clause.isSatisfied = Satisfiability.UNSAT);

        variables.get(assignment.literal - 1).antecedant = antecedant;
        variables.get(assignment.literal - 1).truthValue = assignment.truthValue;

        assignmentList.add(assignment);

//        logger.info(String.format("Variables %s assigned to value %b", assignment.literal,
//                assignment.truthValue));
//        logger.info(String.format("Antecedant clause %d assigned to literal %d",
//                antecedant, assignment.literal));
    }

    private void unassignLiteral(Assignment assignment) {
        int literal = assignment.truthValue ? assignment.literal : -1 * assignment.literal;
        clauses.stream()
                .filter(clause -> clause.literals.contains(literal) || clause.literals.contains(-literal))
                .forEach(clause -> clause.isSatisfied = Satisfiability.UNDECIDE);

        clauses.stream()
                .filter(clause -> (clause.literals.contains(literal) || clause.literals.contains(-literal)))
                .forEach(clause -> clause.assignedLiterals.remove(literal));

        variables.get(assignment.literal - 1).antecedant = -1;
        variables.get(assignment.literal - 1).truthValue = null;
    }
}
