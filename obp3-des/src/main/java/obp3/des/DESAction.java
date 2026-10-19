package obp3.des;


public record DESAction<D extends Clonable<D>>(long delta, Event<D> event) {
    DESConfiguration<D> execute(DESConfiguration<D> configuration) {
        if (event != null) {
            configuration.events.remove(event);
        }
        //advance time
        configuration.advanceTime(delta);
        //process event
        configuration.processEvent(event);
        return configuration;
    }
}
