package obp3.modelchecking.buchi.ndfs.gs09.cdlp05;

import obp3.modelchecking.buchi.ndfs.gs09.VertexColor;

public class WeightedColor {
    public VertexColor color;
    public int weight;
    public WeightedColor(VertexColor color, int weight) {
        this.color = color;
        this.weight = weight;
    }
}