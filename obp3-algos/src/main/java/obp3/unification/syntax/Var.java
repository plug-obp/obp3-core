package obp3.unification.syntax;

import obp3.modelchecking.safety.SafetyDepthFirstTraversal;
import obp3.traversal.dfs.DepthFirstTraversal;
import obp3.unification.UnificationAnswer;

import java.util.function.Function;

public record Var(String name) implements Term {
    @Override
    public <I, O> O accept(Visitor<I, O> visitor, I input) {
        return visitor.visit(this, input);
    }

    @Override
    public String toString() {
        return "?"+name;
    }

    @Override
    public Term substitute(Function<Var, Term> mapper) {
        return mapper.apply(this);
    }

    public boolean occursIn(Term term, Function<Var, Term> substitution) {
        Function<Var, UnificationAnswer<Term>> mapper = (Var v) -> UnificationAnswer.of(substitution.apply(v));
        var rr = new ToRootedGraph(term, mapper);
        var traversal = new SafetyDepthFirstTraversal<>(
                DepthFirstTraversal.Algorithm.WHILE,
                rr, -1, Function.identity(), (node) -> node instanceof Var(var x) && x.equals(this.name()));
        return !traversal.runAlone().holds;
    }
}
