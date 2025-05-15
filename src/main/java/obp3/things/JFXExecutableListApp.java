package obp3.things;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import obp3.Execution;
import obp3.IExecutable;
import obp3.sli.core.IRootedGraph;
import obp3.traversal.dfs.DepthFirstTraversalDo;
import obp3.traversal.dfs.DepthFirstTraversalRelational;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Set;
import java.util.function.Function;

public class JFXExecutableListApp extends Application {
    private final ListView<Execution<Parameters, Set<Long>>> listView = new ListView<>();

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Executable Task List");

        VBox root = new VBox(10);
        root.setPadding(new Insets(10));

        populateList();
        listView.setCellFactory(_ -> new ExecutionListCell<>());

        root.getChildren().add(listView);
        root.getChildren().add(new HBox(5));
        Scene scene = new Scene(root, 500, 400);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    private void populateList() {
        var tasks = new ArrayList<Execution<Parameters, Set<Long>>>();
        tasks.add(traversal(1000, DepthFirstTraversalRelational::new));
        tasks.add(traversal(1000, DepthFirstTraversalDo::new));
        tasks.add(traversal(10000, DepthFirstTraversalRelational::new));
        tasks.add(traversal(100000, DepthFirstTraversalRelational::new));
        tasks.add(traversal(1000000, DepthFirstTraversalRelational::new));
        listView.getItems().addAll(tasks);
    }


    private Execution<Parameters, Set<Long>> traversal(long limit, Function<IRootedGraph<Long>, IExecutable<Set<Long>>> constructor) {
        var width = 30;
        var seed = System.nanoTime();

        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator(' ');
        DecimalFormat formater = new DecimalFormat("###,###.##", symbols);

        return new Execution<>(
                "limit: " + limit,
                new Parameters(limit, width, seed, constructor),
                (p) -> {
                    var graph = new LimitedRandomRootedGraph(p.limit, p.width, p.seed);
                    var executable = p.constructor.apply(graph);
                    return executable;
                },
                (r) -> "Explored: " + formater.format(r.size()) + " configurations");
    }

    record Parameters(long limit, int width, long seed, Function<IRootedGraph<Long>, IExecutable<Set<Long>>> constructor){}

    public static void main(String[] args) {
        launch(args);
    }
}

/**
 * Custom ListCell that displays an ExecutableTask with interactive buttons.
 */

class ExecutionListCell<I, R> extends ListCell<Execution<I, R>> {

    @Override
    protected void updateItem(Execution<I, R> task, boolean empty) {
        super.updateItem(task, empty);
        if (empty || task == null) {
            setGraphic(null);
        } else {
            if (getGraphic() == null) {
                setGraphic(new ExecutionView<>(task));
            }
            ((ExecutionView<?,?>)getGraphic()).updateView();
        }
    }
}

