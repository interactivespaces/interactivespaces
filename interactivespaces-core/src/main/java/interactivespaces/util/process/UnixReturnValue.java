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

package interactivespaces.util.process;

import com.google.common.collect.Maps;

import java.util.Map;

/**
 * All Unix return values.
 *
 * @author Keith M. Hughes
 */
public enum UnixReturnValue {

  /**
   * A normal exit.
   */
  EXIT_NORMALLY(0),

  /**
   * Hangup (POSIX).
   * */
  SIGHUP(1 + 128),

  /**
   * Interrupt (ANSI).
   */
  SIGINT(2 + 128),

  /**
   * Quit (POSIX+128).
   */
  SIGQUIT(3 + 128),

  /**
   * Illegal instruction (ANSI).
   */
  SIGILL(4 + 128),

  /**
   * Trace trap (POSIX).
   */
  SIGTRAP(5 + 128),

  /**
   * Abort (ANSI).
   */
  SIGABRT(6 + 128),

  /**
   * IOT trap (4.2 BSD).
   */
  SIGIOT(6 + 128),

  /**
   * BUS error (4.2 BSD).
   */
  SIGBUS(7 + 128),

  /**
   * Floating-point exception (ANSI).
   */
  SIGFPE(8 + 128),

  /**
   * Kill, unblockable (POSIX).
   */
  SIGKILL(9 + 128),

  /**
   * User-defined signal 1 (POSIX).
   */
  SIGUSR1(10 + 128),

  /**
   * Segmentation violation (ANSI).
   */
  SIGSEGV(11 + 128),

  /**
   * User-defined signal 2 (POSIX).
   */
  SIGUSR2(12 + 128),

  /**
   * Broken pipe (POSIX).
   */
  SIGPIPE(13 + 128),

  /**
   * Alarm clock (POSIX).
   */
  SIGALRM(14 + 128),

  /**
   * Termination (ANSI).
   */
  SIGTERM(15 + 128),

  /**
   * Stack fault.
   */
  SIGSTKFLT(16 + 128),

  /**
   * Same as SIGCHLD (System V).
   */
  SIGCLD(17 + 128),

  /**
   * Child status has changed (POSIX).
   */
  SIGCHLD(17 + 128),

  /**
   * Continue (POSIX).
   */
  SIGCONT(18 + 128),

  /**
   * Stop, unblockable (POSIX).
   */
  SIGSTOP(19 + 128),

  /**
   * Keyboard stop (POSIX).
   */
  SIGTSTP(20 + 128),

  /**
   * Background read from tty (POSIX).
   */
  SIGTTIN(21 + 128),

  /**
   * Background write to tty (POSIX).
   */
  SIGTTOU(22 + 128),

  /**
   * Urgent condition on socket (4.2 BSD).
   */
  SIGURG(23 + 128),

  /**
   * CPU limit exceeded (4.2 BSD).
   */
  SIGXCPU(24 + 128),

  /**
   * File size limit exceeded (4.2 BSD).
   */
  SIGXFSZ(25 + 128),

  /**
   * Virtual alarm clock (4.2 BSD).
   */
  SIGVTALRM(26 + 128),

  /**
   * Profiling alarm clock (4.2 BSD).
   */
  SIGPROF(27 + 128),

  /**
   * Window size change (4.3 BSD, Sun).
   */
  SIGWINCH(28 + 128),

  /**
   * Pollable event occurred (System V).
   */
  SIGPOLL(29 + 128),

  /**
   * I/O now possible (4.2 BSD).
   */
  SIGIO(29 + 128),

  /**
   * Power failure restart (System V).
   */
  SIGPWR(30 + 128),

  /**
   * Bad system call.
   */
  SIGSYS(31);

  /**
   * Map from the unix return values to the enum.
   */
  private static final Map<Integer, UnixReturnValue> VALUE_TO_ENUM = Maps.newHashMap();

  static {
    for (UnixReturnValue value : UnixReturnValue.values()) {
      VALUE_TO_ENUM.put(value.getReturnValue(), value);
    }
  }

  /**
   * Look up the associated enum for a given return value.
   *
   * @param retval
   *          the return value from the process
   *
   * @return the proper enum for the return value
   */
  public static UnixReturnValue get(int retval) {
    return VALUE_TO_ENUM.get(retval);
  }

  /**
   * The return value.
   */
  private int returnValue;

  /**
   * Create a new enum.
   *
   * @param returnValue
   *          the integer return value
   */
  UnixReturnValue(int returnValue) {
    this.returnValue = returnValue;
  }

  /**
   * Get the integer return value.
   *
   * @return the return value
   */
  public int getReturnValue() {
    return returnValue;
  }
}
