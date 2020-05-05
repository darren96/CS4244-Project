public class Assignment {
    int variable;
    boolean truthValue;
    int decisionLevel;
    boolean pickBranch;

    public Assignment(int variable, boolean truthValue, int decisionLevel, boolean pickBranch) {
        this.variable = variable;
        this.truthValue = truthValue;
        this.decisionLevel = decisionLevel;
        this.pickBranch = pickBranch;
    }

    @Override
    public String toString() {
        return String.format("{%d=%b @Level%d, PickBranch=%b}", variable, truthValue, decisionLevel, pickBranch);
    }
}
