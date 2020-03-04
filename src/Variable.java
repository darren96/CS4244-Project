public class Variable {
    Integer variable;
    Boolean assigned;
    Integer noOfAppearances;

    public Variable(Integer variable) {
        this.variable = variable;
        this.assigned = null;
        this.noOfAppearances = 1;
    }
}
