package objectbrowser;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.lang.reflect.Field;

public class GenericObjectView implements ObjectView {
    private VBox root = new VBox();

    @Override
    public Node getView() {
        return root;
    }

    @Override
    public void setObject(Object obj) {
        root.getChildren().clear();
        if (obj == null) {
            root.getChildren().add(new Label("null"));
            return;
        }

        Class<?> cls = obj.getClass();
        root.getChildren().add(new Label("Type: " + cls.getName()));

        for (Field field : cls.getDeclaredFields()) {
            field.setAccessible(true);
            try {
                Object value = field.get(obj);
                Label label = new Label(field.getName() + " = " + value);
                root.getChildren().add(label);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }
}