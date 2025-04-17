package objectbrowser;

import javafx.scene.Node;
import javafx.scene.control.Label;

public class NullObjectView implements ObjectView {
    @Override
    public Node getView() {
        return new Label("null");
    }

    @Override
    public void setObject(Object object) {

    }
}
