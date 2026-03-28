package obp3.uslg.syntax;

import obp3.fixer.Fixer;
import obp3.unification.syntax.App;
import obp3.unification.syntax.Term;
import obp3.unification.syntax.Var;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SLGSolver {
    Fixer<Term, AnswerSet> fixer = new Fixer<>(
            this::equations,
            AnswerSet.toLattice(),
            HashMap::new
    );
    Map<String, List<Rule>> rules;
    public SLGSolver(List<Rule> rules) {
        this.rules = rules.stream().collect(
                Collectors.groupingBy(r -> r.head().name())
        );
    }
    public AnswerSet solve(Term term) {
        var canonicalTerm = AlphaEquivalence.toCanonical(term, Substitution.empty());
        var as = fixer.apply(canonicalTerm);
        return as;
    }

    AnswerSet equations(Term call, Function<Term, AnswerSet> request) {
        AnswerSet result = new AnswerSet();
        for (Rule rule : applicable(call)) {
            Optional<Substitution> mgu = Unifier.unify(rule.head(), call, Substitution.empty());
            if (mgu.isEmpty()) continue;
            Substitution substitution = mgu.get();
            var goals = rule.body().stream().map(term -> term.substitute(substitution::get)).toList();
            //solve the conjunction of goals
            AnswerSet solutions = solveConjunction(goals, substitution, request);
            result = result.union(solutions);
        }
        return result;
    }

    List<Rule> applicable(Term goal) {
        return rules.get(goal.name());
    }

    AnswerSet solveConjunction(List<Term> goals, Substitution substitution, Function<Term, AnswerSet> request) {
        if (goals.isEmpty()) {
            //no more goals, return the current substitution as a solution
            return new AnswerSet(List.of(substitution));
        }
        Term first = goals.getFirst();
        Term canonicalFirst = AlphaEquivalence.toCanonical(first, Substitution.empty());
        List<Term> rest = goals.subList(1, goals.size());
        //get the solutions for the first goal
        AnswerSet firstSolutions = request.apply(canonicalFirst);
        AnswerSet result = new AnswerSet();
        //for each solution of the first goal, solve the rest of the goals with the new substitution
        for (Substitution s : firstSolutions.answers()) {
            Substitution newSubstitution = substitution.compose(s);
            var newGoals = rest.stream().map(term -> term.substitute(newSubstitution::get)).toList();
            AnswerSet restSolutions = solveConjunction(newGoals, newSubstitution, request);
            result = result.union(restSolutions);
        }
        return result;
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
