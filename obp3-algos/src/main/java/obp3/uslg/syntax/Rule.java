package obp3.uslg.syntax;

import obp3.unification.syntax.Term;
import obp3.unification.syntax.Var;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public record Rule(Term head, List<Term> body) {
    public Rule(Term head, Term... body) {
        this(head, Arrays.asList(body));
    }
    public Rule substitute(Function<Var, Term> mapper) {
        var newHead = head.substitute(mapper);
        var newBody = body.stream().map(term -> term.substitute(mapper)).toList();
        return new Rule(newHead, newBody);
    }
}
