package objectbrowser;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import objectbrowser.person.Person;
import objectbrowser.person.PersonView;
import objectbrowser.person.Point;

public class ObjectBrowser extends Application {
    private BorderPane root = new BorderPane();

    @Override
    public void start(Stage primaryStage) {
        Scene scene = new Scene(root, 600, 400);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Object Browser");

        Object sample = new Person("Alice", 30);
//        ObjectViewFactory.register(Person.class, PersonView.class);
        ObjectView view = ObjectViewFactory.instance.createView(sample);
        root.setCenter(new ObjectViewContainer( sample ).getView());
        root.setBottom(new ObjectViewContainer( new Point(2, 3)).getView());

        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}