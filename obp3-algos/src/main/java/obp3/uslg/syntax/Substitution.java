package obp3.uslg.syntax;

import obp3.unification.syntax.Term;
import obp3.unification.syntax.Var;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public record Substitution(Map<Var, Term> bindings) {
    public static Substitution empty() {
        return new Substitution(new HashMap<>());
    }
    public boolean contains(Var variable) {
        return bindings.containsKey(variable);
    }
    public Term get(Var variable) {
        return bindings.getOrDefault(variable, variable);
    }

    public Substitution compose(Substitution other) {
        // Apply this substitution to the terms in the other substitution
        Map<Var, Term> newBindings = other.bindings.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().substitute(this::get)
                ));
        // Add the bindings from this substitution
        newBindings.putAll(this.bindings);
        return new Substitution(newBindings);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Substitution(Map<Var, Term> otherBindings))) return false;
        return bindings.equals(otherBindings);
    }

    @Override
    public int hashCode() {
        return bindings.hashCode();
    }

    public boolean extend(Var variable, Term term) {
        if (variable.occursIn(term, this::get)) {
            return false; // Occurs check failed
        }
        bindings.put(variable, term);
        return true;
    }
}
