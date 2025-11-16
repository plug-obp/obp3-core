package obp3.unification.syntax;

import obp3.runtime.sli.IRootedGraph;
import obp3.unification.UnificationAnswer;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

public class ToRootedGraph implements IRootedGraph<Term> {
    Term root;
    Function<Var, UnificationAnswer<Term>> mapper;
    public ToRootedGraph(Term root, Function<Var, UnificationAnswer<Term>> mapper) {
        this.root = root;
        this.mapper = mapper;
    }
    @Override
    public Iterator<Term> roots() {
        return List.of(root).iterator();
    }

    @Override
    public Iterator<Term> neighbours(Term term) {
        return switch (term) {
        case Var v -> mapper.apply(v).stream().iterator();
        case App a -> Arrays.stream(a.terms()).iterator();
        };
    }
}
