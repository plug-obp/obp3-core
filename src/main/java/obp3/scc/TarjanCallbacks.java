package obp3.scc;

import obp3.traversal.dfs.domain.IDepthFirstTraversalConfiguration;
import obp3.traversal.dfs.model.IDepthFirstTraversalCallbacksModel;

public class TarjanCallbacks<V> implements IDepthFirstTraversalCallbacksModel<V, V> {
    @Override
    public boolean onEntry(V source, V vertex, IDepthFirstTraversalConfiguration<V, V> configuration) {
        TarjanConfigurationSCC<V> config = (TarjanConfigurationSCC<V>) configuration;
        config.time++;
        var vData = config.data.computeIfAbsent(vertex, _ -> TarjanVertexData.DEFAULT);
        vData.low = config.time;
        vData.lead = true;
        return false;
    }

    @Override
    public boolean onKnown(V source, V vertex, IDepthFirstTraversalConfiguration<V, V> configuration) {
        TarjanConfigurationSCC<V> config = (TarjanConfigurationSCC<V>) configuration;
        var vData = config.data.computeIfAbsent(source, _ -> TarjanVertexData.DEFAULT);
        var wData = config.data.computeIfAbsent(vertex, _ -> TarjanVertexData.DEFAULT);
        //retreat on known
        if (wData.low < vData.low) {
            vData.low = wData.low;
            vData.lead = false;
        }
        return false;
    }

    @Override
    public boolean onExit(V vertex, IDepthFirstTraversalConfiguration.StackFrame<V> frame, IDepthFirstTraversalConfiguration<V, V> configuration) {
        TarjanConfigurationSCC<V> config = (TarjanConfigurationSCC<V>) configuration;
        var v = config.peek().vertex();
        var w = vertex;
        var vData = config.data.computeIfAbsent(v, _ ->TarjanVertexData.DEFAULT);
        var wData = config.data.computeIfAbsent(w, _ ->TarjanVertexData.DEFAULT);
        //retreat
        if (wData.low < vData.low) {
            vData.low = wData.low;
            vData.lead = false;
        }
        //postvisit
        if (wData.lead) {
            while (!config.followers.isEmpty() && config.data.computeIfAbsent(config.followers.peek(), _ -> TarjanVertexData.DEFAULT).low > wData.low) {
                var x = config.followers.pop();
                var xData = config.data.computeIfAbsent(x, _ -> TarjanVertexData.DEFAULT);
                xData.ptr = w;
                xData.low = Integer.MAX_VALUE;
            }
            wData.ptr = w;
            wData.low = Integer.MAX_VALUE;
        } else {
            config.followers.push(w);
        }
        return false;
    }
}
