package obp3.uslg.syntax;

import obp3.fixer.Fixer;
import obp3.unification.syntax.App;
import obp3.unification.syntax.Term;
import obp3.unification.syntax.Var;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SLGSolver {
    Fixer<Term, AnswerSet> fixer = new Fixer<>(
            this::equations,
            AnswerSet.toLattice(),
            HashMap::new
    );
    Map<String, List<Rule>> rules;
    IdentityHashMap<Rule, Rule> renamedRuleCache = new IdentityHashMap<>();

    public SLGSolver(List<Rule> rules) {
        this.rules = rules.stream().collect(
                Collectors.groupingBy(r -> r.head().name())
        );
    }

    public AnswerSet solve(Term term) {
        var canonicalTerm = AlphaEquivalence.toCanonical(term, Substitution.empty());
        var as = fixer.apply(canonicalTerm);
        // Map canonical answers back to user variables
        return mapBackToUserVars(term, canonicalTerm, as);
    }

    AnswerSet mapBackToUserVars(Term userTerm, Term canonicalTerm, AnswerSet canonicalAnswers) {
        AnswerSet result = new AnswerSet();
        Set<Var> userVars = collectVars(userTerm);
        for (Substitution s : canonicalAnswers.answers()) {
            // Unify the resolved canonical answer with the original user term
            Term resolved = canonicalTerm.substitute(s::get);
            Optional<Substitution> mapping = Unifier.unify(resolved, userTerm, Substitution.empty());
            if (mapping.isPresent()) {
                result = result.add(mapping.get().project(userVars));
            }
        }
        return result;
    }

    Rule renameRule(Rule rule) {
        Map<Var, Var> renaming = new HashMap<>();
        int id = System.identityHashCode(rule);
        int[] localCounter = {0};
        Function<Var, Term> mapper = v -> renaming.computeIfAbsent(v, _ -> new Var("_R" + id + "_" + localCounter[0]++));
        return rule.substitute(mapper);
    }

    Rule renamedRule(Rule rule) {
        return renamedRuleCache.computeIfAbsent(rule, this::renameRule);
    }

    AnswerSet equations(Term call, Function<Term, AnswerSet> request) {
        Set<Var> callVars = collectVars(call);
        AnswerSet result = new AnswerSet();
        for (Rule rule : applicable(call)) {
            Rule fresh = renamedRule(rule);
            Optional<Substitution> mgu = Unifier.unify(fresh.head(), call, Substitution.empty());
            if (mgu.isEmpty()) continue;
            Substitution substitution = mgu.get();
            var goals = fresh.body().stream().map(term -> term.substitute(substitution::get)).toList();
            AnswerSet solutions = solveConjunction(goals, substitution, request);
            // Project answers to only the call's variables
            for (Substitution s : solutions.answers()) {
                Substitution projected = projectResolved(s, callVars);
                result = result.add(projected);
            }
        }
        return result;
    }

    List<Rule> applicable(Term goal) {
        return rules.getOrDefault(goal.name(), List.of());
    }

    AnswerSet solveConjunction(List<Term> goals, Substitution substitution, Function<Term, AnswerSet> request) {
        if (goals.isEmpty()) {
            return new AnswerSet(List.of(substitution));
        }
        Term first = goals.getFirst();
        Term canonicalFirst = AlphaEquivalence.toCanonical(first, substitution);
        List<Term> rest = goals.subList(1, goals.size());
        AnswerSet firstSolutions = request.apply(canonicalFirst);
        AnswerSet result = new AnswerSet();
        for (Substitution s : firstSolutions.answers()) {
            // Apply the answer back: resolve canonical form with answer, then unify with original goal
            Term resolved = canonicalFirst.substitute(s::get);
            Optional<Substitution> applied = Unifier.unify(first, resolved, substitution);
            if (applied.isEmpty()) continue;
            Substitution newSubstitution = applied.get();
            var newGoals = rest.stream().map(term -> term.substitute(newSubstitution::get)).toList();
            AnswerSet restSolutions = solveConjunction(newGoals, newSubstitution, request);
            result = result.union(restSolutions);
        }
        return result;
    }

    static Set<Var> collectVars(Term term) {
        Set<Var> vars = new HashSet<>();
        collectVars(term, vars);
        return vars;
    }

    private static void collectVars(Term term, Set<Var> vars) {
        switch (term) {
            case Var v -> vars.add(v);
            case App a -> a.terms().forEach(t -> collectVars(t, vars));
        }
    }

    Substitution projectResolved(Substitution s, Set<Var> vars) {
        var projected = new HashMap<Var, Term>();
        for (Var v : vars) {
            Term resolved = s.get(v);
            if (!resolved.equals(v)) {
                projected.put(v, resolved);
            }
        }
        return new Substitution(projected);
    }

    public static void main(String[] args) {

        var x1 = new App("connection",new App("Amsterdam"), new Var("X"));
        var x2 = new App("connection",new App("Amsterdam"), new Var("Y"));

        var ea = AlphaEquivalence.areVariants(x1, x2, Substitution.empty());

        var x = new Var("X");
        var y = new Var("Y");
        var z = new Var("Z");
        List<Rule> rules = List.of(
                new Rule(new App("connection", x, y),
                        new App("connection", x, z),
                        new App("connection", z, y)),
//                new Rule(new App("connection", x, y),
//                        new App("connection", y, x)),
                new Rule(new App("connection", new App("a"), new App("b"))),
                new Rule(new App("connection", new App("b"), new App("c"))),
                new Rule(new App("connection", new App("c"), new App("a")))
//                new Rule(new App("connection",new App("Amsterdam"), new App("Schiphol"))),
//                new Rule(new App("connection", new App("Amsterdam"), new App("Haarlem"))),
//                new Rule(new App("connection", new App("Schiphol"), new App("Leiden"))),
//                new Rule(new App("connection", new App("Haarlem"), new App("Leiden")))
        );
        SLGSolver solver = new SLGSolver(rules);
        var result = solver.solve(new App("connection", new App("a"), new Var("T")));
        System.out.println(result);
    }
}
