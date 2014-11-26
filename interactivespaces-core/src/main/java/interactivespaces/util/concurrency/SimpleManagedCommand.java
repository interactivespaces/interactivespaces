/**
 *
 */
package interactivespaces.util.concurrency;

import org.apache.commons.logging.Log;

import java.util.concurrent.Future;

/**
 * @author Keith M. Hughes
 */
public class SimpleManagedCommand implements ManagedCommand {

  /**
   * The command to be run.
   */
  private final Runnable command;

  /**
   * The task of the command.
   */
  private final WrappedTask task;

  /**
   * The managed commands this command is part of.
   */
  private final SimpleManagedCommands managedCommands;

  /**
   * The future associated with this task.
   */
  private Future<?> future;

  /**
   * The logger to use.
   */
  private final Log log;

  /**
   * Construct a managed command for the given wrapped task.
   *
   * @param command
   *          the command to run
   * @param managedCommands
   *          the managed commands that the command is part of
   * @param repeating
   *          {@code true} if this command will be repeating
   * @param allowTerminate
   *          {@code true} if the repeating command should be allowed to terminate if an exception is thrown
   * @param log
   *          the log to use
   */
  SimpleManagedCommand(Runnable command, SimpleManagedCommands managedCommands, boolean repeating,
      boolean allowTerminate, Log log) {
    this.command = command;
    this.task = new WrappedTask(command, repeating, allowTerminate);
    this.managedCommands = managedCommands;
    this.log = log;
  }

  /**
   * Set the future for this task.
   *
   * @param future
   *          the future
   */
  void setFuture(Future<?> future) {
    this.future = future;
  }

  /**
   * Cancel the task whether or not it was running.
   */
  public void cancel() {
    synchronized (managedCommands) {
      future.cancel(true);
      managedCommands.removeManagedCommand(this);
    }
  }

  /**
   * Has the command been cancelled?
   *
   * @return {@code true} if cancelled
   */
  public boolean isCancelled() {
    return future.isCancelled();
  }

  /**
   * Is the command done?
   *
   * @return {@code true} if done
   */
  public boolean isDone() {
    return future.isDone();
  }

  /**
   * Get the task for the command.
   *
   * @return the wrapped task
   */
  WrappedTask getTask() {
    return task;
  }

  /**
   * A runnable that logs exceptions.
   *
   * @author Keith M. Hughes
   */
  class WrappedTask implements Runnable {

    /**
     * The actual runnable to be run.
     */
    private final Runnable delegate;

    /**
     * {@code true} if this task is a repeating task.
     */
    private final boolean repeating;

    /**
     * {@code true} if this task should terminate if an exception happens.
     */
    private final boolean allowTerminate;

    /**
     * Construct a new wrapped task.
     *
     * @param delegate
     *          the actual runnable to run
     * @param repeating
     *          {@code true} if this delegate will be repeating
     * @param allowTerminate
     *          {@code true} if the potentially periodic running of the delegate can stop the periodic running
     */
    WrappedTask(Runnable delegate, boolean repeating, boolean allowTerminate) {
      this.delegate = delegate;
      this.repeating = repeating;
      this.allowTerminate = allowTerminate;
    }

    @Override
    public void run() {
      try {
        delegate.run();
      } catch (Throwable e) {
        log.error("Exception caught during Managed Command", e);

        if (allowTerminate) {
          // This guarantees the future will stop immediately
          throw new RuntimeException();
        }
      } finally {
        if (!repeating) {
          managedCommands.removeManagedCommand(SimpleManagedCommand.this);
        }
      }
    }
  }
}
