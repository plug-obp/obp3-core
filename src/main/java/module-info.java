module obp3.algos {
    requires javafx.controls;
    requires org.kordamp.ikonli.core;
    requires org.kordamp.ikonli.javafx;
    exports obp3;
    exports obp3.sli.core;
    exports obp3.sli.core.operators;
    exports obp3.sli.core.operators.product;
    exports obp3.traversal.bfs;
    exports obp3.fixer;
    exports obp3.things;
    exports obp3.traversal.dfs.domain;
    exports obp3.traversal.dfs.model;
    exports obp3.traversal.dfs.semantics;
    exports obp3.traversal.dfs.semantics.relational;
    exports obp3.traversal.dfs.defaults.domain;
    exports obp3.traversal.dfs.semantics.relational.actions;
    exports obp3.traversal.dfs.semantics.relational.callbacks;
}