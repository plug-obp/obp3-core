package z2mc.traversal.dft;

import obp3.runtime.sli.IRootedGraph;
import obp3.sli.core.RootedGraphFunctional;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class RootedGraphExamples {
    public static IRootedGraph<Integer> emptyGraph =
            new RootedGraphFunctional<>(Collections::emptyIterator, (_) -> Collections.emptyIterator());

    /// /// <img src="../../../../../doc-files/emptyRootGraph.png" width="300">
    public static IRootedGraph<Integer> emptyRootGraph =
            new RootedGraphFunctional<>(
                    Collections::emptyIterator,
                    (_) -> List.of(1, 2, 3).iterator());

    /// <img src="../../../../../doc-files/oneRootEmptyNeighboursGraph.png" width="200">
    public static IRootedGraph<Integer> oneRootEmptyNeighboursGraph =
            new RootedGraphFunctional<>(
                    () -> List.of(1).iterator(),
                    (_) -> Collections.emptyIterator()
            );

    /// <img src="../../../../../doc-files/twoRootsEmptyNeighboursGraph.png" width="300">
    public static IRootedGraph<Integer> twoRootsEmptyNeighboursGraph =
            new RootedGraphFunctional<>(
                    () -> List.of(1, 2).iterator(),
                    (_) -> Collections.emptyIterator()
            );

    /// <img src="../../../../../doc-files/twoIdenticalRoots.png" width="300">
    public static IRootedGraph<Integer> twoIdenticalRoots =
            new RootedGraphFunctional<>(
                    () -> List.of(1, 1).iterator(),
                    (_) -> Collections.emptyIterator()
            );

    /// <img src="../../../../../doc-files/line2.png" width="300">
    public static IRootedGraph<Integer> line2 =
            new RootedGraphFunctional<>(
                    () -> List.of(1).iterator(),
                    (v) -> Map.of(
                                1, List.of(2),
                                2, List.<Integer>of()
                            ).get(v).iterator()
            );

    /// <img src="../../../../../doc-files/disconnectedGraph1.png" width="300">
    public static IRootedGraph<Integer> disconnectedGraph1 =
            new RootedGraphFunctional<>(
                    () -> List.of(1).iterator(),
                    (v) ->  Map.of(
                                        1, List.of(2, 3),
                                        2, List.<Integer>of(),
                                        3, List.<Integer>of(),
                                        4, List.of(5),
                                        5, List.of(5))
                            .get(v).iterator()
            );

    /// <img src="../../../../../doc-files/disconnectedGraph2.png" width="300">
    public static IRootedGraph<Integer> disconnectedGraph2 =
            new RootedGraphFunctional<>(
                    () -> List.of(4).iterator(),
                    (v) ->  Map.of(
                                    1, List.of(2, 3),
                                    2, List.<Integer>of(),
                                    3, List.<Integer>of(),
                                    4, List.of(5),
                                    5, List.of(5))
                            .get(v).iterator()
            );

    /// <img src="../../../../../doc-files/twoRootsTwoGraphs.png" width="300">
    public static IRootedGraph<Integer> twoRootsTwoGraphs =
            new RootedGraphFunctional<>(
                    () -> List.of(1, 4).iterator(),
                    (v) ->  Map.of(
                                    1, List.of(2, 3),
                                    2, List.<Integer>of(),
                                    3, List.<Integer>of(),
                                    4, List.of(5),
                                    5, List.of(5))
                            .get(v).iterator()
            );

    /// <img src="../../../../../doc-files/rootCycle.png" width="200">
    public static IRootedGraph<Integer> rootCycle =
            new RootedGraphFunctional<>(
                    () -> List.of(1).iterator(),
                    (v) ->  Map.of(
                                    1, List.of(1, 2),
                                    2, List.<Integer>of())
                            .get(v).iterator()
            );

    /// <img src="../../../../../doc-files/rootCycle3.png" width="200">
    public static IRootedGraph<Integer> rootCycle3 =
            new RootedGraphFunctional<>(
                    () -> List.of(1).iterator(),
                    (v) ->  Map.of(
                                    1, List.of(2),
                                    2, List.of(3),
                                    3, List.of(1))
                            .get(v).iterator()
            );

    /// <img src="../../../../../doc-files/simpleCycle2.png" width="200">
    public static IRootedGraph<Integer> simpleCycle2 =
            new RootedGraphFunctional<>(
                    () -> List.of(1).iterator(),
                    (v) -> Map.of(
                            1, List.of(2),
                            2, List.of(1))
                            .get(v).iterator()
            );

    /// <img src="../../../../../doc-files/lasso_1_1.png" width="300">
    public static IRootedGraph<Integer> lasso_1_1 =
            new RootedGraphFunctional<>(
                    () -> List.of(1).iterator(),
                    (v) ->  Map.of(
                                    1, List.of(2),
                                    2, List.of(2))
                            .get(v).iterator()
            );

    /// <img src="../../../../../doc-files/lasso_2_1.png" width="300">
    public static IRootedGraph<Integer> lasso_2_1 =
            new RootedGraphFunctional<>(
                    () -> List.of(1).iterator(),
                    (v) ->  Map.of(
                                    1, List.of(2),
                                    2, List.of(3),
                                    3, List.of(3))
                            .get(v).iterator()
            );

    /// <img src="../../../../../doc-files/lasso_1_2.png" width="300">
    public static IRootedGraph<Integer> lasso_1_2 =
            new RootedGraphFunctional<>(
                    () -> List.of(1).iterator(),
                    (v) ->  Map.of(
                                    1, List.of(2),
                                    2, List.of(3),
                                    3, List.of(2))
                            .get(v).iterator()
            );

    /// <img src="../../../../../doc-files/lasso_1_3.png" width="300">
    public static IRootedGraph<Integer> lasso_1_3 =
            new RootedGraphFunctional<>(
                    () -> List.of(1).iterator(),
                    (v) ->  Map.of(
                                    1, List.of(2),
                                    2, List.of(3),
                                    3, List.of(4),
                                    4, List.of(2))
                            .get(v).iterator()
            );

    /// <img src="../../../../../doc-files/sharing_2.png" width="300">
    public static IRootedGraph<Integer> sharing_2 =
            new RootedGraphFunctional<>(
                    () -> List.of(1).iterator(),
                    (v) -> Map.of(
                            1, List.of(2, 4),
                            2, List.of(3),
                            3, List.of(1),
                            4, List.of(5),
                            5, List.of(2)
                    ).get(v).iterator()
            );
    /// <img src="../../../../../doc-files/sharing3.png" width="300">
    public static IRootedGraph<Integer> sharing_3 =
            new RootedGraphFunctional<>(
                    () -> List.of(1).iterator(),
                    (v) -> Map.of(
                            1, List.of(2, 4),
                            2, List.of(3),
                            3, List.of(1),
                            4, List.of(5),
                            5, List.of(3)
                    ).get(v).iterator()
            );

    public static IRootedGraph<Integer> tree4 =
            new RootedGraphFunctional<>(
                    () -> List.of(1).iterator(),
                    (v) -> Map.of(
                            1, List.of(2, 3),
                            2, List.<Integer>of(),
                            3, List.<Integer>of(4),
                            4, List.<Integer>of()
                    ).get(v).iterator(),
                    false,
                    false
            );
}
