public class Variable {
    Integer variable;
    Boolean truthValue;
    Integer occurences;
    Integer score;

    public Variable(Integer variable) {
        this.variable = variable;
        this.truthValue = null;
        this.occurences = 1;
        this.score = 1;
    }
}
