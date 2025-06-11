package z2mc.traversal.dft;

import obp3.sli.core.IRootedGraph;
import obp3.sli.core.RootedGraphFunctional;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class RootedGraphExamples {
    public static IRootedGraph<Integer> emptyGraph =
            new RootedGraphFunctional<>(Collections::emptyIterator, (_) -> Collections.emptyIterator());

    public static IRootedGraph<Integer> emptyRootGraph =
            new RootedGraphFunctional<>(
                    Collections::emptyIterator,
                    (_) -> List.of(1, 2, 3).iterator());

    public static IRootedGraph<Integer> oneRootEmptyNeighboursGraph =
            new RootedGraphFunctional<>(
                    () -> List.of(1).iterator(),
                    (_) -> Collections.emptyIterator()
            );

    public static IRootedGraph<Integer> twoRootsEmptyNeighboursGraph =
            new RootedGraphFunctional<>(
                    () -> List.of(1, 2).iterator(),
                    (_) -> Collections.emptyIterator()
            );

    public static IRootedGraph<Integer> twoIdenticalRoots =
            new RootedGraphFunctional<>(
                    () -> List.of(1, 1).iterator(),
                    (_) -> Collections.emptyIterator()
            );

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

    public static IRootedGraph<Integer> rootCycle =
            new RootedGraphFunctional<>(
                    () -> List.of(1).iterator(),
                    (v) ->  Map.of(
                                    1, List.of(1, 2),
                                    2, List.<Integer>of())
                            .get(v).iterator()
            );

    public static IRootedGraph<Integer> rootCycle3 =
            new RootedGraphFunctional<>(
                    () -> List.of(1).iterator(),
                    (v) ->  Map.of(
                                    1, List.of(2),
                                    2, List.of(3),
                                    3, List.of(1))
                            .get(v).iterator()
            );

    public static IRootedGraph<Integer> simpleCycle2 =
            new RootedGraphFunctional<>(
                    () -> List.of(1).iterator(),
                    (v) -> Map.of(
                            1, List.of(2),
                            2, List.of(1))
                            .get(v).iterator()
            );

    public static IRootedGraph<Integer> lasso_1_1 =
            new RootedGraphFunctional<>(
                    () -> List.of(1).iterator(),
                    (v) ->  Map.of(
                                    1, List.of(2),
                                    2, List.of(2))
                            .get(v).iterator()
            );

    public static IRootedGraph<Integer> lasso_2_1 =
            new RootedGraphFunctional<>(
                    () -> List.of(1).iterator(),
                    (v) ->  Map.of(
                                    1, List.of(2),
                                    2, List.of(3),
                                    3, List.of(3))
                            .get(v).iterator()
            );

    public static IRootedGraph<Integer> lasso_1_2 =
            new RootedGraphFunctional<>(
                    () -> List.of(1).iterator(),
                    (v) ->  Map.of(
                                    1, List.of(2),
                                    2, List.of(3),
                                    3, List.of(2))
                            .get(v).iterator()
            );
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
}
