package obp3.des;

import obp3.sli.core.support.Clonable;

public record DESAction<D extends Clonable<D>>(long delta, Event<D> event) {
    DESConfiguration<D> execute(DESConfiguration<D> configuration) {
        //advance time
        configuration.advanceTime(delta);
        //process event
        configuration.processEvent(event);
        return configuration;
    }
}
