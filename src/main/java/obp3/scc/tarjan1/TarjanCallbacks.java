package obp3.scc.tarjan1;

import obp3.scc.TarjanVertexData;
import obp3.traversal.dfs.domain.IDepthFirstTraversalConfiguration;
import obp3.traversal.dfs.model.IDepthFirstTraversalCallbacksModel;

public class TarjanCallbacks<V> implements IDepthFirstTraversalCallbacksModel<V, V> {
    @Override
    public boolean onEntry(V source, V vertex, IDepthFirstTraversalConfiguration<V, V> configuration) {
        TarjanConfigurationSCC<V> config = (TarjanConfigurationSCC<V>) configuration;
        config.time++;
        var vData = config.data.computeIfAbsent(vertex, _ -> TarjanVertexData.DEFAULT());
        vData.low = config.time;
        vData.lead = true;
        return false;
    }

    @Override
    public boolean onKnown(V source, V vertex, IDepthFirstTraversalConfiguration<V, V> configuration) {
        TarjanConfigurationSCC<V> config = (TarjanConfigurationSCC<V>) configuration;
        var vData = config.data.get(source);
        var wData = config.data.get(vertex);
        //retreat on known
        if (vData != null && wData.low < vData.low) {
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
        var data = config.data;
        var vData = data.get(v);
        var wData = data.get(w);
        //retreat
        if (vData != null && wData.low < vData.low) {
            vData.low = wData.low;
            vData.lead = false;
        }
        //postvisit
        if (wData.lead) {
            var followers = config.followers;
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
            config.followers.push(w);
        }
        return false;
    }
}
