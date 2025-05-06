package obp3.fixer.exemple.cfl.model;

public class Delta extends Composite {
    public final Language operand;
    public Delta(Language operand) {
        this.operand = operand;
    }
    @Override
    public <I, O> O accept (FunctionalVisitor<I, O> visitor, I input) {
        return visitor.visit(this, input);
    }
}
