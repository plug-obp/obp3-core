package obp3.uslg.syntax;

import obp3.fixer.Lattice;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class AnswerSet {
    private final Set<Substitution> answers;
    public AnswerSet() {
        this.answers = new HashSet<>();
    }
    public AnswerSet(Collection<Substitution> answers) {
        this.answers = new HashSet<>(answers);
    }
    public AnswerSet add(Substitution answer) {
        var newAnswers = new HashSet<>(answers);
        newAnswers.add(answer);
        return new AnswerSet(newAnswers);
    }
    public AnswerSet reverseAll(Substitution substitution) {
        var answers = this.answers.stream().map(subst -> subst.mapVars(substitution)).toList();
        return new AnswerSet(new HashSet<>(answers));
    }

    public AnswerSet union(AnswerSet other) {
        var newAnswers = new HashSet<>(answers);
        newAnswers.addAll(other.answers);
        return new AnswerSet(newAnswers);
    }

    public boolean contains(Substitution answer) {
        return answers.contains(answer);
    }

    public Set<Substitution> answers() {
        return Collections.unmodifiableSet(answers);
    }

    public boolean isEmpty() {
        return answers.isEmpty();
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!( o instanceof AnswerSet other)) {
            return false;
        }
        return answers.equals(other.answers);
    }

    @Override
    public int hashCode() {
        return answers.hashCode();
    }
    @Override
    public String toString() {
        if (answers.isEmpty()) return "{}";
        return answers.stream()
                .map(Substitution::toString)
                .reduce((a,b) -> a + ",\n " + b)
                .map(s -> "{" + s + "}")
                .orElse("{}");
    }

    public static Lattice<AnswerSet> toLattice() {
        return new Lattice<>(new AnswerSet(), null, AnswerSet::equals);
    }
}
