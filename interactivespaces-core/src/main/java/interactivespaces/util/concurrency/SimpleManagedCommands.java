/**
 *
 */
package interactivespaces.util.concurrency;

import interactivespaces.util.events.EventFrequency;

import com.google.common.collect.Sets;

import org.apache.commons.logging.Log;

import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author Keith M. Hughes
 */
public class SimpleManagedCommands implements ManagedCommands {

  /**
   * All managed commands in collection.
   */
  private final Set<ManagedCommand> managedCommands = Sets.newHashSet();

  /**
   * The executor service for this collection.
   */
  private final ScheduledExecutorService executorService;

  /**
   * The logger for the collection.
   */
  private final Log log;

  /**
   * Construct a managed command collection.
   *
   * @param executorService
   *          the executor service for the command
   * @param log
   *          the logger for the command
   */
  public SimpleManagedCommands(ScheduledExecutorService executorService, Log log) {
    this.executorService = executorService;
    this.log = log;
  }

  @Override
  public synchronized ManagedCommand submit(Runnable command) {
    SimpleManagedCommand managedCommand = new SimpleManagedCommand(command, this, false, false, log);
    managedCommand.setFuture(executorService.submit(managedCommand.getTask()));
    managedCommands.add(managedCommand);

    return managedCommand;
  }

  @Override
  public synchronized ManagedCommand schedule(Runnable command, long delay, TimeUnit unit) {
    SimpleManagedCommand managedCommand = new SimpleManagedCommand(command, this, false, false, log);
    managedCommand.setFuture(executorService.schedule(managedCommand.getTask(), delay, unit));
    managedCommands.add(managedCommand);

    return managedCommand;
  }

  @Override
  public ManagedCommand scheduleAtFixedRate(Runnable command, EventFrequency commandFrequency) {
    return scheduleAtFixedRate(command, commandFrequency, true);
  }

  @Override
  public synchronized ManagedCommand scheduleAtFixedRate(Runnable command, EventFrequency commandFrequency,
      boolean allowTerminate) {
    SimpleManagedCommand managedCommand = new SimpleManagedCommand(command, this, true, allowTerminate, log);
    managedCommand.setFuture(executorService.scheduleAtFixedRate(managedCommand.getTask(), 0,
        commandFrequency.getPeriod(), commandFrequency.getUnit()));

    managedCommands.add(managedCommand);

    return managedCommand;
  }

  @Override
  public ManagedCommand scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
    return scheduleAtFixedRate(command, initialDelay, period, unit, true);
  }

  @Override
  public synchronized ManagedCommand scheduleAtFixedRate(Runnable command, long initialDelay, long period,
      TimeUnit unit, boolean allowTerminate) {
    SimpleManagedCommand managedCommand = new SimpleManagedCommand(command, this, true, allowTerminate, log);
    managedCommand.setFuture(executorService.scheduleAtFixedRate(managedCommand.getTask(), initialDelay, period, unit));

    managedCommands.add(managedCommand);

    return managedCommand;
  }

  @Override
  public ManagedCommand scheduleWithFixedDelay(Runnable command, EventFrequency commandFrequency) {
    return scheduleWithFixedDelay(command, commandFrequency, true);
  }

  @Override
  public synchronized ManagedCommand scheduleWithFixedDelay(Runnable command, EventFrequency commandFrequency,
      boolean allowTerminate) {
    SimpleManagedCommand managedCommand = new SimpleManagedCommand(command, this, true, allowTerminate, log);
    managedCommand.setFuture(executorService.scheduleWithFixedDelay(managedCommand.getTask(), 0,
        commandFrequency.getPeriod(), commandFrequency.getUnit()));

    managedCommands.add(managedCommand);

    return managedCommand;
  }

  @Override
  public ManagedCommand scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
    return scheduleWithFixedDelay(command, initialDelay, delay, unit, true);
  }

  @Override
  public synchronized ManagedCommand scheduleWithFixedDelay(Runnable command, long initialDelay, long delay,
      TimeUnit unit, boolean allowTerminate) {
    SimpleManagedCommand managedCommand = new SimpleManagedCommand(command, this, true, allowTerminate, log);
    managedCommand
        .setFuture(executorService.scheduleWithFixedDelay(managedCommand.getTask(), initialDelay, delay, unit));

    managedCommands.add(managedCommand);

    return managedCommand;
  }

  /**
   * Shut down all executing commands or commands which haven't started yet.
   */
  public synchronized void shutdownAll() {
    for (ManagedCommand managedCommand : managedCommands) {
      managedCommand.cancel();
    }
    managedCommands.clear();
  }

  /**
   * Remove a managed command from the collection.
   *
   * @param managedCommand
   *          the command to remove
   */
  synchronized void removeManagedCommand(ManagedCommand managedCommand) {
    managedCommands.remove(managedCommand);
  }
}
