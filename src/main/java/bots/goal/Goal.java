package bots.goal;

/**
 * One objective the robot pursues every turn.
 */
public interface Goal {

    void pursue(GoalContext context);
}
