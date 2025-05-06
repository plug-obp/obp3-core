package obp3.fixer.exemple.cfl.model;

public class Token<T> extends Terminal {
    public final T token;
    public Token(T token) {
        this.token = token;
    }
    @Override
    public <I, O> O accept (FunctionalVisitor<I, O> visitor, I input) {
        return visitor.visit(this, input);
    }
}
