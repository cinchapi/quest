package org.cinchapi.quest;

/**
 * A {@link QuestAction} is a runnable that contains an implementation for a
 * Quest CLI action in its {@link #run()} method. Any subclass that is named
 * appropriately will automatically be registered in the Quest CLI's list of
 * runnable actions.
 * 
 * <p>
 * Each action can access user arguments that are passed into the program
 * via the {@link #inputArgs} variable.
 * </p>
 * 
 * @author jeffnelson
 */
abstract class QuestAction implements Runnable {

    /**
     * Return a description that summarizes the action, its inputs and
     * output, etc.
     * 
     * @return the description of the action
     */
    protected abstract String getDescription();
}