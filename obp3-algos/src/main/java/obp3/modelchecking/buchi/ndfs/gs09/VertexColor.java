package obp3.modelchecking.buchi.ndfs.gs09;

/// possible transitions: (white ⟶ cyan), (cyan ⟶ blue), (blue ⟶ red), (cyan ⟶ red)
public enum VertexColor {
    ///never touched by dfs_blue
    WHITE,
    /// if its invocation of dfs_blue is still running (in on the stack_blue) and every cyan
    /// config can reach s, for the currently active instance of dfs_blue(s)
    CYAN,
    /// it is non-accepting and its invocation of dfs_blue has terminated (it was popped from the stack_blue)
    BLUE,
    /// its invocation of dfs_blue has terminated (it was popped from the stack_blue, and is not part of any counterexample)
    RED,
}
