package obp3.fx;

import javafx.application.Platform;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import obp3.Execution;
import org.kordamp.ikonli.javafx.FontIcon;


public class ExecutionView<I, ExeState, R> extends GridPane {
    private final String playIconName = "gmi-play-arrow:30";
    private final String pauseIconName = "gmi-pause:30";
    private final String resumeIconName = "gmi-play-circle-filled:30";
    private final String stopIconName = "gmi-stop:20";
    private final String clearIconName = "gmi-delete:10";
    private final String detailsIconName = "gmi-directions-run:20";

    private final FontIcon executionIcon = new FontIcon(playIconName);
    private final FontIcon stopIcon = new FontIcon(stopIconName);
    private final Label nameLabel = new Label("Name");
    private final Label detailsLabel = new Label("details");
    private final FontIcon detailsIcon = new FontIcon(detailsIconName);
    private final FontIcon clearIcon = new FontIcon(clearIconName);

    private final Execution<I, ExeState, R> execution;

    public ExecutionView(Execution<I, ExeState, R> execution) {
        super();
        this.execution = execution;
        add(nameLabel, 1, 0);
        detailsLabel.setFont(new Font(10));
        add(detailsLabel, 1, 1);
        add(executionIcon, 0, 0, 1, 2);
        add(detailsIcon, 3, 0, 1, 2);

        addButtonEffects(executionIcon);
        addButtonEffects(stopIcon);
        addButtonEffects(clearIcon);
        addButtonEffects(detailsIcon);

        getColumnConstraints().add(new ColumnConstraints(60));
        getColumnConstraints().add(new ColumnConstraints(40, 60, Double.MAX_VALUE, Priority.ALWAYS, HPos.CENTER, true));
        getColumnConstraints().add(new ColumnConstraints(10));
        getRowConstraints().add(new RowConstraints(20, 20, 20, Priority.NEVER, VPos.CENTER, false));
        getRowConstraints().add(new RowConstraints(20, 20, 20, Priority.NEVER, VPos.CENTER, false));
    }

    void updateView() {
        nameLabel.setText(execution.name);
        detailsLabel.setText("details");
        execution.resultConsumer = (o) -> Platform.runLater(() -> updateView());

        stopIcon.setOnMouseClicked(e -> { execution.stop(); updateView(); });

        if (!execution.running.get()) {
            executionIcon.setIconLiteral(playIconName);
            executionIcon.setOnMouseClicked(e -> { execution.start(); updateView(); });
            if (execution.result != null) {
                detailsLabel.setGraphic(clearIcon);
                clearIcon.setOnMouseClicked(e -> { execution.result = null; updateView(); });
                detailsLabel.setText(execution.resultToString());
            } else {
                detailsLabel.setGraphic(null);
            }
            nameLabel.setGraphic(null);
        } else {
            if (execution.paused.get()) {
                executionIcon.setIconLiteral(resumeIconName);
                executionIcon.setOnMouseClicked(e -> { execution.resume(); updateView(); });
            } else {
                executionIcon.setIconLiteral(pauseIconName);
                executionIcon.setOnMouseClicked(e -> { execution.pause(); updateView(); });
            }
            nameLabel.setGraphic(stopIcon);
            stopIcon.setOnMouseClicked(e -> { execution.stop(); updateView(); });
        }
    }

    public static void addButtonEffects(Node btn) {
        //btn.setBackground(Background.EMPTY);

        DropShadow normalShadow = new DropShadow(3, Color.DARKGRAY);
        normalShadow.setBlurType(BlurType.GAUSSIAN);
        btn.setEffect(normalShadow);

        //set the default node shadow
        DropShadow hoverShadow = new DropShadow(3, Color.BLACK);
        hoverShadow.setBlurType(BlurType.GAUSSIAN);

        //install the node hover effect
        DropShadow clickShadow = new DropShadow(5, Color.GREEN);
        clickShadow.setBlurType(BlurType.GAUSSIAN);

        btn.addEventHandler(MouseEvent.MOUSE_ENTERED, e-> btn.setEffect(hoverShadow));
        btn.addEventHandler(MouseEvent.MOUSE_EXITED, e-> btn.setEffect(normalShadow));
        btn.addEventHandler(MouseEvent.MOUSE_PRESSED, e-> btn.setEffect(clickShadow));
        btn.addEventHandler(MouseEvent.MOUSE_RELEASED, e-> btn.setEffect(hoverShadow));
    }
}
