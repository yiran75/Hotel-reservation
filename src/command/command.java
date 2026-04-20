package command;

/**
 * Command interface for the Command Pattern.
 * Abstracts all UI-triggered actions (Add, Edit, Delete) from Swing event handlers,
 * maintaining a clean separation between UI and business logic.
 */
public interface Command {
    /**
     * Execute this command.
     * @return true if the operation succeeded, false otherwise.
     */
    boolean execute();

    /**
     * A human-readable description of this command (for logging/feedback).
     */
    String getDescription();
}
