package obp3.scc.tarjan1;

import obp3.scc.TarjanVertexData;
import obp3.sli.core.IRootedGraph;
import obp3.traversal.dfs.defaults.domain.DFTConfigurationSetDeque;
import obp3.traversal.dfs.model.DepthFirstTraversalParameters;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TarjanConfigurationSCC<V> extends DFTConfigurationSetDeque<V, V> {
    Map<V, TarjanVertexData<V>> data = new HashMap<>();
    Deque<V> followers = new ArrayDeque<>();
    Integer time = 0;

    public TarjanConfigurationSCC(IRootedGraph<V> graph) {
        this(graph, new HashSet<>(), new ArrayDeque<>());
    }
    public TarjanConfigurationSCC(IRootedGraph<V> graph, Set<V> known, Deque<StackFrame<V>> stack) {
        super(new DepthFirstTraversalParameters<>(graph, Function.identity(), new TarjanCallbacks<>()), known, stack);
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
