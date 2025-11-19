package obp3.modelchecking.tools;

import obp3.modelchecking.EmptinessCheckerAnswer;
import obp3.runtime.IExecutable;

public sealed interface ModelCheckerModel<C> permits
        StatePredicateModelCheckerModel,
        SafetyModelCheckerModel,
        BuchiModelCheckerModel {
    IExecutable<?, EmptinessCheckerAnswer<C>> modelChecker();
}
