package obp3.des;


public record DESAction<D extends Clonable<D>>(long delta, Event<D> event) {
    DESConfiguration<D> execute(DESConfiguration<D> configuration) {
        //advance time
        configuration.advanceTime(delta);
        //process event
        configuration.processEvent(event);
        return configuration;
    }
}
