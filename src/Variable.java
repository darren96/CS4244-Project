public class Variable {
    Integer variable;
    Boolean truthValue;
    Integer occurences;
    Integer score;
    Boolean dead;

    public Variable(Integer variable) {
        this.variable = variable;
        this.truthValue = null;
        this.occurences = 1;
        this.score = 1;
        this.dead = false;
    }
}
