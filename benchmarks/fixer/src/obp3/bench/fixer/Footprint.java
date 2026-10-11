package obp3.bench.fixer;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

/** Reachable shallow-size sum, not a dominator-tree retained-heap or process RSS measurement. */
public final class Footprint {
    static Set<Object> identitySet() { return Collections.newSetFromMap(new IdentityHashMap<>()); }

    static Set<Object> reachable(Object root, Set<Object> excluded) throws ReflectiveOperationException {
        Set<Object> seen = identitySet();
        ArrayDeque<Object> todo = new ArrayDeque<>();
        todo.add(root);
        while (!todo.isEmpty()) {
            Object object = todo.removeFirst();
            if (excluded.contains(object) || !seen.add(object)) continue;
            Class<?> type = object.getClass();
            if (object instanceof Class<?> || object instanceof ClassLoader || object instanceof Thread)
                throw new IllegalStateException("Unexpected JVM infrastructure in measured graph: " + type);
            if (type.isArray()) {
                if (!type.componentType().isPrimitive()) {
                    for (int i = 0; i < Array.getLength(object); i++) {
                        Object value = Array.get(object, i);
                        if (value != null) todo.add(value);
                    }
                }
            } else {
                for (Class<?> c = type; c != null; c = c.getSuperclass()) {
                    for (Field field : c.getDeclaredFields()) {
                        if (Modifier.isStatic(field.getModifiers()) || field.getType().isPrimitive()) continue;
                        if (!field.trySetAccessible()) throw new IllegalStateException("Cannot inspect " + field);
                        Object value = field.get(object);
                        if (value != null) todo.add(value);
                    }
                }
            }
        }
        return seen;
    }

    public static void main(String[] args) throws ReflectiveOperationException {
        if (FootprintAgent.instrumentation == null) throw new IllegalStateException("Use -javaagent");
        System.out.println("mapMode,workload,phase,objects,bytes");
        for (String mode : new String[]{"identity", "structural"}) {
            for (String workload : new String[]{"chain", "booleanCycle", "powersetRing", "independent", "topFanout"}) {
                FixerBenchmark.Fixture fixture = new FixerBenchmark.Fixture(workload, mode);
                Set<Object> excluded = reachable(fixture, identitySet());
                var solver = fixture.newSolver();
                print(mode, workload, "empty", solver, excluded);
                fixture.verify(solver);
                print(mode, workload, "solved", solver, excluded);
            }
        }
    }

    static void print(String mode, String workload, String phase, Object solver, Set<Object> excluded)
            throws ReflectiveOperationException {
        Set<Object> graph = reachable(solver, excluded);
        long size = graph.stream().mapToLong(FootprintAgent.instrumentation::getObjectSize).sum();
        System.out.println(mode + "," + workload + "," + phase + "," + graph.size() + "," + size);
    }
}
