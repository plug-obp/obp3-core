package obp3.fixer.exemple.cfl.model;

public class Product extends Composite {
    public Language lhs;
    public Language rhs;
    public Product(Language lhs, Language rhs) {
        this.lhs = lhs;
        this.rhs = rhs;
    }
    @Override
    public <I, O> O accept (FunctionalVisitor<I, O> visitor, I input) {
        return visitor.visit(this, input);
    }
}
