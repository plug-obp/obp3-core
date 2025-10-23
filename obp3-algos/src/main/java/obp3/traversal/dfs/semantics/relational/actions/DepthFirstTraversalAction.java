package obp3.traversal.dfs.semantics.relational.actions;

public sealed interface DepthFirstTraversalAction<V, A>
        permits
            BacktrackAction,
            KnownConfigurationAction,
            UnknownConfigurationAction {
}


