package obp3.modelchecking.tools;

public sealed interface ModelChecker permits
        StatePredicateModelCheckerModel,
        SafetyModelCheckerModel,
        BuchiModelCheckerModel {
}
