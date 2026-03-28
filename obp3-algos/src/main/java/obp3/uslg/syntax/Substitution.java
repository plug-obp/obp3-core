package obp3.uslg.syntax;

import obp3.unification.syntax.Term;
import obp3.unification.syntax.Var;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public record Substitution(Map<Var, Term> bindings) {
    public static Substitution empty() {
        return new Substitution(new HashMap<>());
    }
    public boolean contains(Var variable) {
        return bindings.containsKey(variable);
    }
    public Term get(Var variable) {
        Term current = bindings.get(variable);
        if (current == null) return variable;
        while (current instanceof Var v && bindings.containsKey(v)) {
            current = bindings.get(v);
        }
        return current;
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

    public Optional<Substitution> extend(Var variable, Term term) {
        if (variable.occursIn(term, this::get)) {
            return Optional.empty(); // Occurs check failed
        }
        var newBindings = new HashMap<>(bindings);
        newBindings.put(variable, term);
        return Optional.of(new Substitution(newBindings));
    }

    public Substitution project(Set<Var> variables) {
        var projected = new HashMap<Var, Term>();
        for (Var v : variables) {
            if (bindings.containsKey(v)) {
                projected.put(v, bindings.get(v));
            }
        }
        return new Substitution(projected);
    }
}
