/*
 * Copyright (C) 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package interactivespaces.util.concurrency;

import interactivespaces.InteractiveSpacesException;
import interactivespaces.system.InteractiveSpacesEnvironment;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicReference;

/**
 * An interruptable loop.
 *
 * This should be run with the {@link ExecutorService} obtained from
 * {@link InteractiveSpacesEnvironment#getExecutorService()}.
 *
 * @author Keith M. Hughes
 */
public abstract class CancellableLoop implements Runnable {

  /**
   * The {@link Thread} the code will be running in.
   */
  private Thread thread;

  /**
   * Any exception which may have happened during execution.
   */
  private AtomicReference<Exception> exception = new AtomicReference<Exception>();

  /**
   * Runtime state of the loop.
   */
  private AtomicReference<RuntimeState> runtimeState = new AtomicReference<RuntimeState>(
      RuntimeState.READY);

  @Override
  public void run() {
    synchronized (this) {
      if (thread != null) {
        throw new InteractiveSpacesException("Loop already running");
      }

      thread = Thread.currentThread();
      runtimeState.set(RuntimeState.READY);
    }
    try {
      setup();
      while (!thread.isInterrupted()) {
        loop();
      }

      runtimeState.set(RuntimeState.SHUTDOWN);
    } catch (InterruptedException e) {
      runtimeState.set(RuntimeState.SHUTDOWN);
      handleInterruptedException(e);
    } catch (Exception e) {
      exception.set(e);
      runtimeState.set(RuntimeState.CRASH);
      handleException(e);
    } finally {
      thread = null;

      cleanup();
    }
  }

  /**
   * The setup block for the loop. This will be called exactly once before the
   * first call to {@link #loop()}.
   */
  protected void setup() {
    // Do nothing by default.
  }

  /**
   * The cleanup block for the loop. This will be called exactly once after the
   * loop has exited for any reason.
   */
  protected void cleanup() {
    // Do nothing by default.
  }

  /**
   * The body of the loop. This will run continuously until the
   * {@link CancellableLoop} has been interrupted externally or by calling
   * {@link #cancel()}.
   */
  protected abstract void loop() throws InterruptedException;

  /**
   * An {@link InterruptedException} was thrown.
   */
  protected void handleInterruptedException(InterruptedException e) {
    // Ignore InterruptedExceptions by default.
  }

  /**
   * An {@link Exception} other than an {@link InterruptedException} was thrown.
   */
  protected void handleException(Exception e) {
    // Ignore Exceptions by default.
  }

  /**
   * Interrupts the loop.
   */
  public void cancel() {
    if (thread != null) {
      thread.interrupt();
    }
  }

  /**
   * @return {@code true} if the loop is running
   */
  public synchronized boolean isRunning() {
    return thread != null && !thread.isInterrupted();
  }

  /**
   * Get the exception that was thrown during the loop running.
   *
   * @return the exception thrown, can be {@code null} if no exception has yet
   *         been thrown
   */
  public Exception getException() {
    return exception.get();
  }
}
