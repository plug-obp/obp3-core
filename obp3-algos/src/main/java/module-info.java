module obp.algos {
    requires obp.sli.runtime;
    exports obp3;
    exports obp3.sli.core;
    exports obp3.sli.core.operators;
    exports obp3.sli.core.operators.product;
    exports obp3.sli.core.operators.product.model;
    exports obp3.sli.core.operators.product.deterministic;
    exports obp3.sli.core.operators.product.deterministic.model;
    exports obp3.traversal.bfs;
    exports obp3.fixer;
    exports obp3.things;
    exports obp3.traversal.dfs;
    exports obp3.traversal.dfs.domain;
    exports obp3.traversal.dfs.model;
    exports obp3.traversal.dfs.semantics;
    exports obp3.traversal.dfs.semantics.relational;
    exports obp3.traversal.dfs.defaults.domain;
    exports obp3.traversal.dfs.semantics.relational.actions;
    exports obp3.traversal.dfs.semantics.relational.callbacks;

    exports obp3.scc;
    exports obp3.scc.tarjan1;
    exports obp3.scc.tarjan2;
    exports obp3.modelchecking;
    exports obp3.modelchecking.buchi.ndfs.gs09.cdlp05.separated;
    exports obp3.modelchecking.safety;
    exports obp3.modelchecking.tools;
}