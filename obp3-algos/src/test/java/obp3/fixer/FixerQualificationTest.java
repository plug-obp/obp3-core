package obp3.fixer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/** Finite-domain qualification against oracles that do not use Fixer's scheduler. */
class FixerQualificationTest {
    private record Node(int id) {}

    @Test
    @Timeout(30)
    void allMonotoneBooleanSystemsThroughThreeVariablesMatchTheirLeastFixedPoint() {
        int systemsChecked = 0;
        int sessionsChecked = 0;
        int queriesChecked = 0;

        for (int variableCount = 1; variableCount <= 3; variableCount++) {
            List<BooleanEquation> functions = monotoneBooleanFunctions(variableCount);
            assertEquals(switch (variableCount) {
                case 1 -> 3;
                case 2 -> 6;
                default -> 20;
            }, functions.size());
            int systemCount = (int) Math.pow(functions.size(), variableCount);

            for (int systemIndex = 0; systemIndex < systemCount; systemIndex++) {
                List<BooleanEquation> equations = new ArrayList<>();
                int remaining = systemIndex;
                for (int variable = 0; variable < variableCount; variable++) {
                    equations.add(functions.get(remaining % functions.size()));
                    remaining /= functions.size();
                }
                List<Boolean> expected = leastBooleanFixedPoint(equations);

                for (int[] order : permutations(variableCount)) {
                    for (boolean recognizeTop : List.of(false, true)) {
                        Lattice<Boolean> lattice = recognizeTop
                                ? Lattice.BooleanLattice
                                : new Lattice<>(false, Objects::equals);
                        Fixer<Node, Boolean> solver = new Fixer<>(
                                (node, request) -> equations.get(node.id()).evaluate(request),
                                lattice,
                                HashMap::new);
                        queriesChecked += assertQueries(solver, expected, order,
                                "Boolean system " + equations + ", order=" + Arrays.toString(order)
                                        + ", top=" + recognizeTop);
                        sessionsChecked++;
                    }
                }
                systemsChecked++;
            }
        }

        assertEquals(8_039, systemsChecked);
        assertEquals(96_150, sessionsChecked);
        assertEquals(576_588, queriesChecked);
    }

    @Test
    @Timeout(20)
    void seededPowersetReachabilityMatchesIndependentGraphTraversal() {
        Random graphRandom = new Random(413);
        Random orderRandom = new Random(29);
        int queriesChecked = 0;

        for (int trial = 0; trial < 250; trial++) {
            int nodeCount = 1 + graphRandom.nextInt(10);
            List<List<Node>> graph = new ArrayList<>();
            Set<Node> universe = new HashSet<>();
            for (int source = 0; source < nodeCount; source++) {
                universe.add(new Node(source));
                List<Node> successors = new ArrayList<>();
                for (int target = 0; target < nodeCount; target++) {
                    if (graphRandom.nextInt(4) == 0) {
                        successors.add(new Node(target));
                    }
                }
                graph.add(List.copyOf(successors));
            }

            List<Set<Node>> expected = new ArrayList<>();
            for (int root = 0; root < nodeCount; root++) {
                Set<Node> reachable = new HashSet<>();
                ArrayDeque<Node> pending = new ArrayDeque<>();
                pending.add(new Node(root));
                while (!pending.isEmpty()) {
                    Node current = pending.removeFirst();
                    if (reachable.add(current)) {
                        pending.addAll(graph.get(current.id()));
                    }
                }
                expected.add(Set.copyOf(reachable));
            }

            for (int[] order : threeQueryOrders(nodeCount, orderRandom)) {
                for (boolean recognizeTop : List.of(false, true)) {
                    Lattice<Set<Node>> lattice = new Lattice<>(Set.of(),
                            recognizeTop ? Set.copyOf(universe) : null, Objects::equals);
                    Fixer<Node, Set<Node>> solver = new Fixer<>(
                            (node, request) -> {
                                Set<Node> reachable = new HashSet<>();
                                reachable.add(node);
                                for (Node successor : graph.get(node.id())) {
                                    reachable.addAll(request.apply(successor));
                                }
                                return Set.copyOf(reachable);
                            }, lattice, HashMap::new);
                    queriesChecked += assertQueries(solver, expected, order,
                            "Reachability trial " + trial + ", top=" + recognizeTop);
                }
            }
        }

        assertEquals(16_680, queriesChecked);
    }

    @Test
    @Timeout(20)
    void seededUnionIntersectionSystemsMatchSynchronousBitsetIteration() {
        int variableCount = 6;
        int factCount = 5;
        int universeMask = (1 << factCount) - 1;
        Random equationRandom = new Random(9_317);
        Random orderRandom = new Random(51);
        int queriesChecked = 0;

        for (int trial = 0; trial < 300; trial++) {
            List<SetEquation> equations = new ArrayList<>();
            for (int variable = 0; variable < variableCount; variable++) {
                int constants = equationRandom.nextBoolean()
                        ? 1 << equationRandom.nextInt(factCount) : 0;
                List<Intersection> terms = new ArrayList<>();
                int termCount = 1 + equationRandom.nextInt(4);
                for (int term = 0; term < termCount; term++) {
                    terms.add(new Intersection(equationRandom.nextInt(variableCount),
                            equationRandom.nextInt(variableCount),
                            1 + equationRandom.nextInt(universeMask)));
                }
                equations.add(new SetEquation(constants, List.copyOf(terms)));
            }
            List<Set<Integer>> expected = leastSetFixedPoint(equations, factCount);

            for (int[] order : threeQueryOrders(variableCount, orderRandom)) {
                for (boolean recognizeTop : List.of(false, true)) {
                    Lattice<Set<Integer>> lattice = new Lattice<>(Set.of(),
                            recognizeTop ? facts(universeMask) : null, Objects::equals);
                    Fixer<Node, Set<Integer>> solver = new Fixer<>(
                            (node, request) -> equations.get(node.id()).evaluate(request),
                            lattice,
                            HashMap::new);
                    queriesChecked += assertQueries(solver, expected, order,
                            "Union/intersection trial " + trial + ": " + equations
                                    + ", top=" + recognizeTop);
                }
            }
        }

        assertEquals(21_600, queriesChecked);
    }

    @Test
    void growingDependenciesRevealAndThenSolveARecursiveComponent() {
        // x0 discovers x2 only after x1 grows, then discovers x3 after x2 grows.
        // The x0/x3 component climbs through three lattice levels once discovered.
        List<Integer> expected = List.of(3, 1, 2, 3);
        for (int[] order : permutations(4)) {
            for (boolean recognizeTop : List.of(false, true)) {
                Lattice<Integer> lattice = new Lattice<>(0, recognizeTop ? 3 : null, Objects::equals);
                Fixer<Node, Integer> solver = new Fixer<>((node, request) -> switch (node.id()) {
                    case 0 -> request.apply(new Node(1)) >= 1 && request.apply(new Node(2)) >= 2
                            ? request.apply(new Node(3)) : 0;
                    case 1 -> 1;
                    case 2 -> Math.min(2, request.apply(new Node(1)) + 1);
                    case 3 -> Math.min(3, request.apply(new Node(0)) + 1);
                    default -> throw new AssertionError("Unexpected node " + node);
                }, lattice, HashMap::new);
                assertQueries(solver, expected, order,
                        "Growing dependencies, order=" + Arrays.toString(order));
            }
        }
    }

    @Test
    void defaultIdentityKeysKeepEqualObjectsDistinct() {
        Node trueNode = new Node(7);
        Node falseNode = new Node(7);
        assertEquals(trueNode, falseNode);
        Fixer<Node, Boolean> solver = new Fixer<>(
                (node, request) -> node == trueNode,
                Lattice.BooleanLattice);

        assertTrue(solver.apply(trueNode));
        assertFalse(solver.apply(falseNode));
        assertFalse(solver.apply(new Node(7)));
        assertTrue(solver.apply(trueNode));
    }

    @Test
    @SuppressWarnings("rawtypes")
    void legacyRawMapFactoryRemainsSourceCompatible() {
        // Deliberately retain the pre-hardening public constructor's factory type.
        Supplier<Map> legacyFactory = HashMap::new;
        Fixer<Node, Boolean> solver = new Fixer<>(
                (node, request) -> node.id() == 1 || request.apply(new Node(1)),
                Lattice.BooleanLattice,
                legacyFactory);

        assertTrue(solver.apply(new Node(0)));
        assertTrue(solver.apply(new Node(1)));
    }

    @Test
    void structuralKeysConvergeWhenEveryRecursiveRequestUsesAFreshEqualObject() {
        Fixer<Node, Set<Integer>> solver = new Fixer<>((node, request) -> {
            Set<Integer> result = new HashSet<>();
            result.add(node.id());
            result.addAll(request.apply(new Node((node.id() + 1) % 3)));
            return Set.copyOf(result);
        }, new Lattice<>(Set.of(), Objects::equals), HashMap::new);

        for (int repetition = 0; repetition < 3; repetition++) {
            for (int node = 0; node < 3; node++) {
                assertEquals(Set.of(0, 1, 2), solver.apply(new Node(node)));
            }
        }
    }

    @Test
    @Timeout(10)
    void deepDependencyChainDoesNotConsumeTheJavaCallStack() {
        int depth = 20_000;
        Fixer<Node, Boolean> solver = new Fixer<>(
                (node, request) -> node.id() == depth || request.apply(new Node(node.id() + 1)),
                Lattice.BooleanLattice,
                HashMap::new);

        assertTrue(solver.apply(new Node(0)));
        assertTrue(solver.apply(new Node(depth / 2)));
        assertTrue(solver.apply(new Node(depth)));
    }

    @Test
    @Timeout(10)
    void largeRecursiveComponentPropagatesItsSeedWithoutRecursiveEvaluation() {
        int size = 10_000;
        Fixer<Node, Boolean> solver = new Fixer<>((node, request) -> {
            // Read even at the seed so the dependency graph contains the entire cycle.
            boolean successor = request.apply(new Node((node.id() + 1) % size));
            return node.id() == size / 2 || successor;
        }, new Lattice<>(false, Objects::equals), HashMap::new);

        assertTrue(solver.apply(new Node(0)));
        assertTrue(solver.apply(new Node(size - 1)));
        assertTrue(solver.apply(new Node(size / 2)));
    }

    private record BooleanEquation(int truthTable, List<Integer> minimalTerms) {
        boolean evaluate(Function<Node, Boolean> request) {
            // Short-circuit DNF changes observed dependencies as approximations grow.
            for (int term : minimalTerms) {
                boolean satisfied = true;
                for (int bits = term; bits != 0; bits &= bits - 1) {
                    if (!request.apply(new Node(Integer.numberOfTrailingZeros(bits)))) {
                        satisfied = false;
                        break;
                    }
                }
                if (satisfied) {
                    return true;
                }
            }
            return false;
        }
    }

    private static List<BooleanEquation> monotoneBooleanFunctions(int variableCount) {
        int assignmentCount = 1 << variableCount;
        List<BooleanEquation> functions = new ArrayList<>();
        for (int table = 0; table < (1 << assignmentCount); table++) {
            boolean monotone = true;
            for (int assignment = 0; assignment < assignmentCount; assignment++) {
                if ((table & (1 << assignment)) == 0) {
                    continue;
                }
                for (int variable = 0; variable < variableCount; variable++) {
                    if ((table & (1 << (assignment | (1 << variable)))) == 0) {
                        monotone = false;
                    }
                }
            }
            if (!monotone) {
                continue;
            }
            List<Integer> minimalTerms = new ArrayList<>();
            for (int assignment = 0; assignment < assignmentCount; assignment++) {
                if ((table & (1 << assignment)) == 0) {
                    continue;
                }
                boolean minimal = true;
                for (int bits = assignment; bits != 0; bits &= bits - 1) {
                    int predecessor = assignment & ~Integer.lowestOneBit(bits);
                    if ((table & (1 << predecessor)) != 0) {
                        minimal = false;
                    }
                }
                if (minimal) {
                    minimalTerms.add(assignment);
                }
            }
            functions.add(new BooleanEquation(table, List.copyOf(minimalTerms)));
        }
        return functions;
    }

    private static List<Boolean> leastBooleanFixedPoint(List<BooleanEquation> equations) {
        int current = 0;
        // Synchronous truth-table evaluation is independent of DNF and demand ordering.
        for (int iteration = 0; iteration <= equations.size(); iteration++) {
            int next = 0;
            for (int variable = 0; variable < equations.size(); variable++) {
                if ((equations.get(variable).truthTable() & (1 << current)) != 0) {
                    next |= 1 << variable;
                }
            }
            if (next == current) {
                List<Boolean> result = new ArrayList<>();
                for (int variable = 0; variable < equations.size(); variable++) {
                    result.add((current & (1 << variable)) != 0);
                }
                return result;
            }
            assertEquals(current, current & next, "Generated Boolean system must be monotone");
            current = next;
        }
        return fail("Finite Boolean oracle exceeded its lattice height");
    }

    private record Intersection(int left, int right, int mask) {}

    private record SetEquation(int constants, List<Intersection> terms) {
        Set<Integer> evaluate(Function<Node, Set<Integer>> request) {
            Set<Integer> result = new HashSet<>(facts(constants));
            for (Intersection term : terms) {
                Set<Integer> contribution = new HashSet<>(facts(term.mask()));
                contribution.retainAll(request.apply(new Node(term.left())));
                if (!contribution.isEmpty()) {
                    contribution.retainAll(request.apply(new Node(term.right())));
                    result.addAll(contribution);
                }
            }
            return Set.copyOf(result);
        }
    }

    private static List<Set<Integer>> leastSetFixedPoint(List<SetEquation> equations, int factCount) {
        int[] current = new int[equations.size()];
        // Evaluate all coordinates from the old vector, using bit operations rather than sets.
        for (int iteration = 0; iteration <= equations.size() * factCount; iteration++) {
            int[] next = new int[equations.size()];
            for (int variable = 0; variable < equations.size(); variable++) {
                SetEquation equation = equations.get(variable);
                int value = equation.constants();
                for (Intersection term : equation.terms()) {
                    value |= term.mask() & current[term.left()] & current[term.right()];
                }
                next[variable] = value;
                assertEquals(current[variable], current[variable] & value,
                        "Generated powerset system must be monotone");
            }
            if (Arrays.equals(current, next)) {
                return Arrays.stream(current).mapToObj(FixerQualificationTest::facts).toList();
            }
            current = next;
        }
        return fail("Finite powerset oracle exceeded its lattice height");
    }

    private static Set<Integer> facts(int mask) {
        Set<Integer> values = new HashSet<>();
        for (int bits = mask; bits != 0; bits &= bits - 1) {
            values.add(Integer.numberOfTrailingZeros(bits));
        }
        return Set.copyOf(values);
    }

    private static <T> int assertQueries(Fixer<Node, T> solver, List<T> expected,
                                         int[] order, String description) {
        for (int pass = 0; pass < 2; pass++) {
            for (int index = 0; index < order.length; index++) {
                int variable = order[pass == 0 ? index : order.length - index - 1];
                assertEquals(expected.get(variable), solver.apply(new Node(variable)),
                        description + ", variable=" + variable + ", pass=" + pass);
            }
        }
        return order.length * 2;
    }

    private static List<int[]> threeQueryOrders(int size, Random random) {
        List<Integer> shuffled = new ArrayList<>();
        int[] forward = new int[size];
        int[] reverse = new int[size];
        for (int index = 0; index < size; index++) {
            forward[index] = index;
            reverse[index] = size - index - 1;
            shuffled.add(index);
        }
        Collections.shuffle(shuffled, random);
        return List.of(forward, reverse, shuffled.stream().mapToInt(Integer::intValue).toArray());
    }

    private static List<int[]> permutations(int size) {
        List<int[]> result = new ArrayList<>();
        enumeratePermutations(new int[size], new boolean[size], 0, result);
        return result;
    }

    private static void enumeratePermutations(int[] order, boolean[] used, int offset,
                                              List<int[]> result) {
        if (offset == order.length) {
            result.add(order.clone());
            return;
        }
        for (int variable = 0; variable < order.length; variable++) {
            if (!used[variable]) {
                used[variable] = true;
                order[offset] = variable;
                enumeratePermutations(order, used, offset + 1, result);
                used[variable] = false;
            }
        }
    }
}
