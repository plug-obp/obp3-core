package objectbrowser;

import javafx.scene.Node;

public interface ObjectView {
    Node getView();
    void setObject(Object object);
}
