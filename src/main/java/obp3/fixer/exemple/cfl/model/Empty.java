package obp3.fixer.exemple.cfl.model;

public class Empty extends Terminal {
    public static final Empty INSTANCE = new Empty();

    @Override
    public <I, O> O accept (FunctionalVisitor<I, O> visitor, I input) {
        return visitor.visit(this, input);
    }
}
