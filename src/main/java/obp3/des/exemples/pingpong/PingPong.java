package obp3.des.exemples.pingpong;

import obp3.des.DESConfiguration;
import obp3.des.Event;

import java.util.List;

record PingPongConfiguration(List<Integer> ping, List<Integer> pong) {}
public class PingPong {

    PingPongConfiguration run(DESConfiguration<PingPongConfiguration> sim) {
        sim.schedule(new Event<>("time", sim.currentTime + 1, this::run));
        if (!sim.data.ping().isEmpty()) {
            var d = sim.data.ping().removeFirst();
            sim.schedule(
                    new Event<>("ping",
                            sim.currentTime + d,
                            c -> {
                                c.data.pong().addLast(d);
                                run(c);
                            } ));
        }
        return null;
    }

    DESConfiguration<PingPongConfiguration> initial(){
        return new DESConfiguration<>(
                new PingPongConfiguration(List.of(0), List.of(0)),
                0,
                new Event<>("start", 0, this::run));
    }
}
