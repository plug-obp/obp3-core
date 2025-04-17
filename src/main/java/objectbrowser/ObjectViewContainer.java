package objectbrowser;

import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.VBox;

import java.util.List;

public class ObjectViewContainer {
    private VBox root = new VBox();
    private ComboBox<String> viewSelector = new ComboBox<>();
    private ObjectView currentView;
    private Object obj;

    public ObjectViewContainer(Object obj) {
        this.obj = obj;
        initializeViewSelector();
        showAvailableViews();
        setInitialView();
    }

    public Node getView() {
        return root;
    }

    private void initializeViewSelector() {
        viewSelector.setOnAction(event -> updateView());
        root.getChildren().add(viewSelector);
    }

    private void showAvailableViews() {
        List<Class<? extends ObjectView>> list = ObjectViewFactory.instance.getViews(obj);
        for (Class<? extends ObjectView> viewClass : list) {
            viewSelector.getItems().add(viewClass.getSimpleName());
        }
    }

    private void setInitialView() {
        // Set the initial view (first view in the list)
        if (viewSelector.getItems().isEmpty()) return;

        String firstViewName = viewSelector.getItems().get(0);
        updateView(firstViewName);
    }

    private void updateView(String viewName) {
        // Find the corresponding view class
        List<Class<? extends ObjectView>> list = ObjectViewFactory.instance.getViews(obj);
        for (Class<? extends ObjectView> viewClass : list) {
            if (viewClass.getSimpleName().equals(viewName)) {
                try {
                    ObjectView view = viewClass.getDeclaredConstructor().newInstance();
                    view.setObject(obj);
                    if (currentView != null) {
                        // Remove old view from the container
                        root.getChildren().remove(currentView.getView());
                    }
                    // Add new view to the container
                    root.getChildren().add(view.getView());
                    currentView = view;
                    break;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void updateView() {
        // Update view based on user selection
        updateView(viewSelector.getValue());
    }
}