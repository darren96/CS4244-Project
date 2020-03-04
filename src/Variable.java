public class Variable {
    Integer variable;
    Boolean assigned;
    Integer noOfAppearances;

    public Variable(Integer variable) {
        this.variable = variable;
        this.assigned = false;
        this.noOfAppearances = 1;
    }
}
