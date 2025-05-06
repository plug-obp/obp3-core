package obp3.fixer.exemple.cfl;

import obp3.fixer.exemple.cfl.model.*;

import java.util.function.Function;

public class ToString extends FunctionalVisitor.Base<Void, String> implements Function<Language, String> {
    public static ToString INSTANCE = new ToString();
    @Override
    public String apply(Language node) {
        return node.accept(INSTANCE, null);
    }

    @Override
    public String visit(Empty node, Void input) {
        return "∅";
    }

    @Override
    public String visit(Epsilon node, Void input) {
        return "ε";
    }

    @Override
    public <T> String visit(Token<T> node, Void input) {
        return "(τ " +node.token+ ")";
    }

    @Override
    public String visit(Sum node, Void input) {
        return "(" + node.lhs.accept(this, input) + " | " + node.rhs.accept(this, input) + ")";
    }

    @Override
    public String visit(Product node, Void input) {
        return "(" + node.lhs.accept(this, input) + " ∘ " + node.rhs.accept(this, input) + ")";
    }

    @Override
    public String visit(Delta node, Void input) {
        return "(δ " + node.operand.accept(this, input) + ")";
    }

    @Override
    public String visit(Reference node, Void input) {
        return node.name.toString();// + " = " + node.target.accept(this, input);
    }


}
