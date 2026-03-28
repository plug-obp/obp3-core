package obp3.uslg.syntax;

import obp3.unification.syntax.App;
import obp3.unification.syntax.Term;
import obp3.unification.syntax.Var;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

public class Unifier {
    public static Optional<Substitution> unify(Term lhs, Term rhs, Substitution substitution) {
        var t1 = lhs.substitute(substitution::get);
        var t2 = rhs.substitute(substitution::get);
        //if they are the same
        if (t1.equals(t2)) {
            return Optional.of(substitution);
        }
        //if one is a var
        if (t1 instanceof Var v) {
            return substitution.extend(v, t2);
        }
        //if the other is a var
        if (t2 instanceof Var v) {
            return unify(t2, t1, substitution);
        }
        if (       t1 instanceof App(String name1, List<Term> args1)
                && t2 instanceof App(String name2, List<Term> args2))
        {
            if (!name1.equals(name2) || args1.size() != args2.size()) return Optional.empty();
            return IntStream.range(0, args1.size())
                    .boxed().
                    reduce(
                            Optional.of(substitution),
                            (ons, i) -> ons.flatMap( ns -> unify(args1.get(i), args2.get(i), ns)),
                            (os1, os2) -> os1.isPresent() ? os1 : os2
                    );
        }
        return Optional.empty();
    }
}
