package obp3.fixer.exemple.cfl.model;

public abstract class Terminal extends Language {
    @Override
    public <I, O> O accept (FunctionalVisitor<I, O> visitor, I input) {
        return visitor.visit(this, input);
    }
}
