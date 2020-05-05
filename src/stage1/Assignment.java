package stage1;

public class Assignment {
    int literal;
    Boolean truthValue;
    int decisionLevel;
    boolean pickBranch;

    public Assignment(int literal, Boolean truthValue, int decisionLevel, boolean pickBranch) {
        this.literal = literal;
        this.truthValue = truthValue;
        this.decisionLevel = decisionLevel;
        this.pickBranch = pickBranch;
    }

    @Override
    public String toString() {
        return String.format("{%d=%b @Level%d, PickBranch=%b}", literal, truthValue, decisionLevel, pickBranch);
    }
}
