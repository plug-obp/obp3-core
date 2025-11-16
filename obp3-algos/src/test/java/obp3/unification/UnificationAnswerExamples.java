package obp3.unification;

/**
 * Examples demonstrating the UnificationAnswer monad usage patterns.
 */
public class UnificationAnswerExamples {
    
    /**
     * Example 1: Basic usage with successful unification
     */
    public static void example1_BasicSuccess() {
        System.out.println("=== Example 1: Basic Success ===");
        
        var answer = UnificationAnswer.of("x = 42")
            .map(solution -> "Unified: " + solution)
            .map(String::toUpperCase);
        
        System.out.println("Has solution: " + answer.hasSolution());
        System.out.println("Solution: " + answer.solution());
        System.out.println();
    }
    
    /**
     * Example 2: Handling failure with recovery
     */
    public static void example2_FailureRecovery() {
        System.out.println("=== Example 2: Failure Recovery ===");
        
        var answer = UnificationAnswer.<String>failure("Type mismatch: expected Int, got String")
            .recover(reason -> "Recovered from: " + reason)
            .map(s -> "Final result: " + s);
        
        System.out.println("Has solution: " + answer.hasSolution());
        System.out.println("Solution: " + answer.solution());
        System.out.println();
    }
    
    /**
     * Example 3: Unknown state with default value
     */
    public static void example3_UnknownWithDefault() {
        System.out.println("=== Example 3: Unknown with Default ===");
        
        var answer = UnificationAnswer.<String>unknown()
            .recoverUnknown(() -> "default unification")
            .map(s -> s + " was applied");
        
        System.out.println("Has solution: " + answer.hasSolution());
        System.out.println("Solution: " + answer.solution());
        System.out.println();
    }
    
    /**
     * Example 4: Chaining unification steps
     */
    public static void example4_ChainingUnifications() {
        System.out.println("=== Example 4: Chaining Unifications ===");
        
        var result = UnificationAnswer.of("X")
            .flatMap(var -> unify(var, "Int"))
            .flatMap(type -> checkConstraints(type))
            .flatMap(validated -> generateCode(validated));
        
        System.out.println("Result: " + result.solutionOrElse("Failed to unify"));
        System.out.println();
    }
    
    /**
     * Example 5: Failure propagation through chain
     */
    public static void example5_FailurePropagation() {
        System.out.println("=== Example 5: Failure Propagation ===");
        
        var result = UnificationAnswer.of("X")
            .flatMap(var -> unify(var, "String"))
            .flatMap(type -> checkConstraints(type))  // This will fail
            .flatMap(validated -> generateCode(validated))  // Never executed
            .recover(reason -> "Error: " + reason);
        
        System.out.println("Result: " + result.solution());
        System.out.println();
    }
    
    /**
     * Example 6: Complex pipeline with multiple recovery points
     */
    public static void example6_ComplexPipeline() {
        System.out.println("=== Example 6: Complex Pipeline ===");
        
        var result = UnificationAnswer.of("Y")
            .flatMap(var -> unify(var, "Boolean"))
            .recover(reason -> {
                System.out.println("  First recovery: " + reason);
                return "recovered: Boolean";
            })
            .flatMap(type -> checkConstraints(type))
            .recover(reason -> {
                System.out.println("  Second recovery: " + reason);
                return "validated: Boolean";
            })
            .flatMap(validated -> generateCode(validated));
        
        System.out.println("Final result: " + result.solutionOrElse("Complete failure"));
        System.out.println();
    }
    
    /**
     * Example 7: Using toOptional for interop
     */
    public static void example7_OptionalInterop() {
        System.out.println("=== Example 7: Optional Interop ===");
        
        var answer1 = UnificationAnswer.of("success");
        var answer2 = UnificationAnswer.<String>failure("error");
        var answer3 = UnificationAnswer.<String>unknown();
        
        System.out.println("Solution to Optional: " + answer1.toOptional());
        System.out.println("Failure to Optional: " + answer2.toOptional());
        System.out.println("Unknown to Optional: " + answer3.toOptional());
        System.out.println();
    }
    
    /**
     * Example 8: Pattern matching with switch expressions
     */
    public static void example8_PatternMatching() {
        System.out.println("=== Example 8: Pattern Matching ===");
        
        var answer = UnificationAnswer.of("x = 10");
        
        String result = switch (answer) {
            case UnificationAnswer.Solution<String> sol -> 
                "Got solution: " + sol.solution();
            case UnificationAnswer.Failure<String> fail -> 
                "Got failure: " + fail.reason();
            case UnificationAnswer.Unknown<String> _ -> 
                "Unknown state";
        };
        
        System.out.println(result);
        System.out.println();
    }
    
    /**
     * Example 9: Meet semi-lattice operations
     */
    public static void example9_MeetLattice() {
        System.out.println("=== Example 9: Meet Semi-Lattice ===");
        
        var sol1 = UnificationAnswer.of("x: Int");
        var sol2 = UnificationAnswer.of("x: Int");
        var sol3 = UnificationAnswer.of("x: String");
        var fail1 = UnificationAnswer.<String>failure("type error");
        var fail2 = UnificationAnswer.<String>failure("type error");
        var unknown = UnificationAnswer.<String>unknown();
        
        // Meet of equal solutions
        System.out.println("sol1 ⊓ sol2 = " + sol1.meet(sol2).solution());
        
        // Meet of different solutions -> Unknown
        System.out.println("sol1 ⊓ sol3 = " + (sol1.meet(sol3).isUnknown() ? "Unknown" : "Not Unknown"));
        
        // Meet of solution and failure -> Unknown (incomparable)
        System.out.println("sol1 ⊓ fail1 = " + (sol1.meet(fail1).isUnknown() ? "Unknown" : "Not Unknown"));
        
        // Meet of equal failures
        System.out.println("fail1 ⊓ fail2 = " + fail1.meet(fail2).failureReason());
        
        // Meet with Unknown (bottom absorbs)
        System.out.println("sol1 ⊓ Unknown = " + (sol1.meet(unknown).isUnknown() ? "Unknown" : "Not Unknown"));
        System.out.println("Unknown ⊓ fail1 = " + (unknown.meet(fail1).isUnknown() ? "Unknown" : "Not Unknown"));
        
        System.out.println();
    }
    
    /**
     * Example 10: Partial order relationships
     */
    public static void example10_PartialOrder() {
        System.out.println("=== Example 10: Partial Order (⊑) ===");
        
        var solution = UnificationAnswer.of("x: Int");
        var failure = UnificationAnswer.<String>failure("error");
        var unknown = UnificationAnswer.<String>unknown();
        
        // Unknown is bottom (⊥)
        System.out.println("Unknown ⊑ Solution: " + unknown.lessOrEqual(solution));
        System.out.println("Unknown ⊑ Failure: " + unknown.lessOrEqual(failure));
        System.out.println("Unknown ⊑ Unknown: " + unknown.lessOrEqual(unknown));
        
        // Solution and Failure are incomparable
        System.out.println("Solution ⊑ Failure: " + solution.lessOrEqual(failure));
        System.out.println("Failure ⊑ Solution: " + failure.lessOrEqual(solution));
        
        // Reflexivity
        System.out.println("Solution ⊑ Solution: " + solution.lessOrEqual(solution));
        System.out.println("Failure ⊑ Failure: " + failure.lessOrEqual(failure));
        
        System.out.println();
    }
    
    /**
     * Example 11: Bottom element
     */
    public static void example11_BottomElement() {
        System.out.println("=== Example 11: Bottom Element (⊥) ===");
        
        var bottom = UnificationAnswer.<String>bottom();
        System.out.println("bottom() is Unknown: " + bottom.isUnknown());
        
        var solution = UnificationAnswer.of("result");
        System.out.println("bottom ⊓ solution = " + (bottom.meet(solution).isUnknown() ? "Unknown (⊥)" : "Not bottom"));
        System.out.println("solution ⊓ bottom = " + (solution.meet(bottom).isUnknown() ? "Unknown (⊥)" : "Not bottom"));
        
        System.out.println();
    }
    
    // === Helper methods to simulate unification operations ===
    
    private static UnificationAnswer<String> unify(String var, String type) {
        if ("String".equals(type)) {
            return UnificationAnswer.failure("Cannot unify " + var + " with " + type);
        }
        return UnificationAnswer.of(var + ": " + type);
    }
    
    private static UnificationAnswer<String> checkConstraints(String typeBinding) {
        if (typeBinding.contains("String")) {
            return UnificationAnswer.failure("String type violates constraint");
        }
        return UnificationAnswer.of("validated " + typeBinding);
    }
    
    private static UnificationAnswer<String> generateCode(String validated) {
        return UnificationAnswer.of("code(" + validated + ")");
    }

    /**
     * Example 12: Using fold for concise state handling
     */
    public static void example12_FoldPattern() {
        System.out.println("=== Example 12: Fold Pattern ===");

        var solution = UnificationAnswer.of("x: Int");
        var failure = UnificationAnswer.<String>failure("type error");
        var unknown = UnificationAnswer.<String>unknown();

        // Fold solution
        String result1 = solution.fold(
                sol -> "✓ Unified: " + sol,
                fail -> "✗ Error: " + fail,
                () -> "? Unknown"
        );
        System.out.println(result1);

        // Fold failure
        String result2 = failure.fold(
                sol -> "✓ Unified: " + sol,
                fail -> "✗ Error: " + fail,
                () -> "? Unknown"
        );
        System.out.println(result2);

        // Fold unknown
        String result3 = unknown.fold(
                sol -> "✓ Unified: " + sol,
                fail -> "✗ Error: " + fail,
                () -> "? Unknown"
        );
        System.out.println(result3);

        System.out.println();
    }

    /**
     * Example 13: Using stream() for Stream API integration
     */
    public static void example13_StreamIntegration() {
        System.out.println("=== Example 13: Stream Integration ===");

        // Create a list of unification answers
        var answers = java.util.List.of(
            UnificationAnswer.of("x: Int"),
            UnificationAnswer.<String>failure("type mismatch"),
            UnificationAnswer.of("y: String"),
            UnificationAnswer.<String>unknown(),
            UnificationAnswer.of("z: Bool")
        );

        // Extract all successful solutions using stream()
        System.out.println("All successful solutions:");
        answers.stream()
            .flatMap(UnificationAnswer::stream)
            .forEach(sol -> System.out.println("  - " + sol));

        // Transform solutions
        System.out.println("\nTransformed solutions:");
        var transformed = answers.stream()
            .flatMap(UnificationAnswer::stream)
            .map(String::toUpperCase)
            .toList();
        transformed.forEach(s -> System.out.println("  - " + s));

        // Count solutions
        long count = answers.stream()
            .flatMap(UnificationAnswer::stream)
            .count();
        System.out.println("\nTotal solutions: " + count);

        // Filter and collect
        System.out.println("\nFiltered (containing 'Int' or 'Bool'):");
        answers.stream()
            .flatMap(UnificationAnswer::stream)
            .filter(s -> s.contains("Int") || s.contains("Bool"))
            .forEach(s -> System.out.println("  - " + s));

        // Convert to iterator
        System.out.println("\nUsing iterator:");
        var iterator = UnificationAnswer.of("test: Data").stream().iterator();
        while (iterator.hasNext()) {
            System.out.println("  Iterator value: " + iterator.next());
        }

        System.out.println();
    }

    
    // === Main method to run all examples ===
    
    public static void main(String[] args) {
        example1_BasicSuccess();
        example2_FailureRecovery();
        example3_UnknownWithDefault();
        example4_ChainingUnifications();
        example5_FailurePropagation();
        example6_ComplexPipeline();
        example7_OptionalInterop();
        example8_PatternMatching();
        example9_MeetLattice();
        example10_PartialOrder();
        example11_BottomElement();
        example12_FoldPattern();
        example13_StreamIntegration();
    }
}
