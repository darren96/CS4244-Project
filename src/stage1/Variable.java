package stage1;

public class Variable {
    Integer variable;
    Boolean truthValue;
    Integer occurences;
    Integer score;
    Integer decidedLevel;
    Integer antecedant;

    public Variable(Integer variable) {
        this.variable = variable;
        this.truthValue = null;
        this.occurences = 1;
        this.score = 1;
        this.decidedLevel = -1;
        this.antecedant = -1;
    }
}
