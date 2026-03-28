package obp3.uslg.syntax;

import obp3.unification.syntax.App;
import obp3.unification.syntax.Term;
import obp3.unification.syntax.Var;

import java.util.HashMap;
import java.util.Map;

public class AlphaEquivalence {
    public static Term toCanonical(Term term, Substitution substitution) {
        return toCanonical(term, substitution, new NameGenerator());
    }

    public static boolean areVariants(Term t1, Term t2, Substitution substitution) {
        return toCanonical(t1, substitution).equals(toCanonical(t2, substitution));
    }

    private static Term toCanonical(Term term, Substitution substitution, NameGenerator nameGen) {
        return switch (term) {
            case Var v -> nameGen.get(v, substitution);
            case App a -> new App(a.name(), a.terms().stream().map(t -> toCanonical(t, substitution, nameGen)).toList());
        };
    }

    private static class NameGenerator {
        int counter = 0;
        private Map<Var, Integer> mapping = new HashMap<>();
        public Var get(Var v, Substitution substitution) {
            var idx = mapping.get(v);
            if (idx != null) { return new Var("V" + idx); }
            idx = counter++;
            var nv = new Var("V" + idx);
            mapping.put(v, idx);
            return nv;

        }
    }
}
