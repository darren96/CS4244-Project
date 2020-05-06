package stage1;

public class Variable {
    Integer variable;
    Boolean truthValue;
    Integer score;
    Integer decidedLevel;
    Integer antecedant;

    public Variable(Integer variable) {
        this.variable = variable;
        this.truthValue = null;
        this.score = 0;
        this.decidedLevel = -1;
        this.antecedant = -1;
    }
}
