package obp3.des.exemples.pingpong;

import obp3.des.DESConfiguration;
import obp3.des.Event;

import java.util.ArrayList;
import java.util.List;

public class PingPong {
   static void run(DESConfiguration<PingPongConfiguration> sim) {
        //sim.schedule(new Event<>("time", sim.currentTime + 1, PingPong::run));
        if (!sim.data.ping().isEmpty()) {
            sim.schedule(
                    new Event<>("ping",
                            sim.currentTime + sim.data.ping().getFirst(),
                            c -> {
                                if (c.data.ping().isEmpty()) return;
                                var d = c.data.ping().removeFirst();
                                c.data.pong().addLast(d);
                                run(c);
                            } ));
        }
        if (!sim.data.pong().isEmpty()) {
            sim.schedule(
                    new Event<>("pong",
                            sim.currentTime + sim.data.pong().getFirst(),
                            c -> {
                                if (c.data.pong().isEmpty()) return;
                                var d = c.data.pong().removeFirst();
                                c.data.ping().addLast(d);
                                run(c);
                            } ));
        }
    }

    public static DESConfiguration<PingPongConfiguration> initial(long end, Integer... pingDelays){
        return new DESConfiguration<>(
                new PingPongConfiguration(new ArrayList<>(List.of(pingDelays)), new ArrayList<>()),
                end,
                new Event<>("start", 0, PingPong::run));
    }
}
