package obp3.des.exemples.pingpong;

import obp3.sli.core.support.Clonable;

import java.util.ArrayList;
import java.util.List;

public record PingPongConfiguration(List<Integer> ping, List<Integer> pong) implements Clonable<PingPongConfiguration> {

    @Override
    public PingPongConfiguration clone() {
        return new PingPongConfiguration(new ArrayList<>(ping), new ArrayList<>(pong));
    }
}
