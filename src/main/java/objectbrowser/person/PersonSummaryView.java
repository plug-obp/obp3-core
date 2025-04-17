package objectbrowser.person;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import objectbrowser.ObjectView;
import objectbrowser.ObjectViewFor;

@ObjectViewFor(Person.class)
public class PersonSummaryView implements ObjectView {
    private Label summaryLabel = new Label();

    @Override
    public Node getView() {
        return summaryLabel;
    }

    @Override
    public void setObject(Object obj) {
        if (!(obj instanceof Person p)) return;
        summaryLabel.setText(p.name() + " " + p.age());
    }
}