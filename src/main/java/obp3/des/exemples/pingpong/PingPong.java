package obp3.des.exemples.pingpong;

import obp3.des.DESConfiguration;
import obp3.des.Event;

import java.util.List;

record PingPongConfiguration(List<Integer> ping, List<Integer> pong) {}
public class PingPong {

    PingPongConfiguration start(PingPongConfiguration p) {
        return null;
    }

    DESConfiguration<PingPongConfiguration> initial(){
        return new DESConfiguration<>(
                new PingPongConfiguration(List.of(0), List.of(0)),
                0,
                new Event<>("start", 0, this::start));
    }
}
