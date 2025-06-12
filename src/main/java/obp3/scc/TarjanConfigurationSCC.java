package obp3.scc;

import obp3.sli.core.IRootedGraph;
import obp3.traversal.dfs.defaults.domain.DFTConfigurationSetDeque;

import java.util.*;
import java.util.stream.Collectors;

public class TarjanConfigurationSCC<V> extends DFTConfigurationSetDeque<V, V> {
    Map<V, TarjanVertexData<V>> data = new IdentityHashMap<>();
    Deque<V> followers = new ArrayDeque<>();
    Integer time = 0;

    public TarjanConfigurationSCC(IRootedGraph<V> graph) {
        this(graph, new HashSet<>(), new ArrayDeque<>());
    }
    public TarjanConfigurationSCC(IRootedGraph<V> graph, Set<V> known, Deque<StackFrame<V>> stack) {
        super(new TarjanParameters<>(graph), known, stack);
    }
    public Map<V, Set<V>> getStronglyConnectedComponents() {
        return data.entrySet().stream().collect(Collectors.groupingBy(
                entry -> entry.getValue().ptr,
                Collectors.mapping(
                        Map.Entry::getKey,
                        Collectors.toSet()
                )
        ));
    }
}
