package obp3.scc.tarjan2;

import obp3.scc.TarjanVertexData;
import obp3.traversal.dfs.domain.IDepthFirstTraversalConfiguration;
import obp3.traversal.dfs.model.IDepthFirstTraversalCallbacksModel;

public class TarjanCallbacks<V> implements IDepthFirstTraversalCallbacksModel<V, V> {
    public TarjanMemory<V> memory = new TarjanMemory<>();

    @Override
    public boolean onEntry(V source, V vertex, IDepthFirstTraversalConfiguration<V, V> configuration) {
        memory.time++;
        var vData = memory.data.computeIfAbsent(vertex, _ -> TarjanVertexData.DEFAULT());
        vData.low = memory.time;
        vData.lead = true;
        return false;
    }

    @Override
    public boolean onKnown(V source, V vertex, IDepthFirstTraversalConfiguration<V, V> configuration) {
        var vData = memory.data.get(source);
        var wData = memory.data.get(vertex);
        //retreat on known
        if (vData != null && wData.low < vData.low) {
            vData.low = wData.low;
            vData.lead = false;
        }
        return false;
    }

    @Override
    public boolean onExit(V vertex, IDepthFirstTraversalConfiguration.StackFrame<V> frame, IDepthFirstTraversalConfiguration<V, V> configuration) {
        var v = configuration.peek().vertex();
        var w = vertex;
        var data = memory.data;
        var vData = data.get(v);
        var wData = data.get(w);
        //retreat
        if (vData != null && wData.low < vData.low) {
            vData.low = wData.low;
            vData.lead = false;
        }
        //postvisit
        if (wData.lead) {
            var followers = memory.followers;
            while (!followers.isEmpty()) {
                var x = followers.peek();
                var xData = data.get(x);
                if (xData.low < wData.low) break;
                //remove only after confirming
                followers.pop();
                xData.ptr = w;
                xData.low = Integer.MAX_VALUE;
            }
            wData.ptr = w;
            wData.low = Integer.MAX_VALUE;
        } else {
            memory.followers.push(w);
        }
        return false;
    }
}
