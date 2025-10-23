package obp3.scc.tarjan2;

import obp3.scc.TarjanVertexData;
import obp3.sli.core.IRootedGraph;

import java.util.*;
import java.util.stream.Collectors;

public class TarjanMemory<V> {
    Map<V, TarjanVertexData<V>> data = new HashMap<>();
    Deque<V> followers = new ArrayDeque<>();
    Integer time = 0;

    public TarjanMemory() {}
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
