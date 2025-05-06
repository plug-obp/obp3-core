package obp3.fixer.exemple.cfl.model;

public class Reference extends Composite {
    public final Object name;
    public Language target = Empty.INSTANCE;
    public Reference(Object name) {
        this.name = name;
    }

    @Override
    public <I, O> O accept (FunctionalVisitor<I, O> visitor, I input) {
        return visitor.visit(this, input);
    }
}
