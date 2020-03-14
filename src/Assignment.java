public class Assignment {
    int literal;
    boolean truthValue;
    int decisionLevel;
    boolean pickBranch;

    public Assignment(int literal, boolean truthValue, int decisionLevel, boolean pickBranch) {
        this.literal = literal;
        this.truthValue = truthValue;
        this.decisionLevel = decisionLevel;
        this.pickBranch = pickBranch;
    }
}
