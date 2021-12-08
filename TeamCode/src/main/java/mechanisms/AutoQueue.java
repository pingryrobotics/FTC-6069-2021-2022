package mechanisms;

import java.util.LinkedList;
import java.util.Queue;

/**
 * A class for a queue of autonomous actions to be executed
 */
public class AutoQueue {
    private final Queue<AutoAction> actionQueue;
    private AutoAction currentAction;

    /**
     * Initialize the auto queue
     */
    public AutoQueue() {
        actionQueue = new LinkedList<>();
    }

    /**
     * Add an auto action to the queue
     * @param autoAction the auto action to add
     */
    public void addAutoAction(AutoAction autoAction) {
        actionQueue.add(autoAction);
        if (currentAction == null) {
            shiftQueue();
        }
    }

    /**
     * Update the action queue. This must be called each loop
     * @return false if the queue is empty and not running, true if its still running
     */
    public boolean updateQueue() {
        // if theres no current action or the current action finishes, shift the queue
        if (currentAction == null || currentAction.updateAutoAction()) {
            shiftQueue();
            // if the current action is null, the queue isnt running, otherwise, its still running
            return (currentAction != null);
        }
        // if the action isn't finished, return false because its still running
        return true;
    }

    /**
     * Shift the queue forward and begin an action if theres a new action to perform
     */
    private void shiftQueue() {
        currentAction = actionQueue.poll();
        if (currentAction != null) {
            currentAction.beginAutoAction();
        }
    }


    /**
     * An class for autonomous actions which can be extended by action classes within mechanisms
     */
    public static abstract class AutoAction {

        public abstract void beginAutoAction();
        public abstract boolean updateAutoAction();
    }
}
