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

package interactivespaces.activity.binary;

import java.util.Map;

import com.google.common.collect.Maps;

/**
 * All Unix return values.
 * 
 * @author Keith M. Hughes
 */
public enum UnixReturnValue {
	EXIT_NORMALLY(0), 
	SIGHUP(1 + 128), /* Hangup (POSIX). */
	SIGINT(2 + 128), /* Interrupt (ANSI). */
	SIGQUIT(3 + 128), /* Quit (POSIX+128). */
	SIGILL(4 + 128), /* Illegal instruction (ANSI). */
	SIGTRAP(5 + 128), /* Trace trap (POSIX). */
	SIGABRT(6 + 128), /* Abort (ANSI). */
	SIGIOT(6 + 128), /* IOT trap (4.2 BSD). */
	SIGBUS(7 + 128), /* BUS error (4.2 BSD). */
	SIGFPE(8 + 128), /* Floating-point exception (ANSI). */
	SIGKILL(9 + 128), /* Kill, unblockable (POSIX). */
	SIGUSR1(10 + 128), /* User-defined signal 1 (POSIX). */
	SIGSEGV(11 + 128), /* Segmentation violation (ANSI). */
	SIGUSR2(12 + 128), /* User-defined signal 2 (POSIX). */
	SIGPIPE(13 + 128), /* Broken pipe (POSIX). */
	SIGALRM(14 + 128), /* Alarm clock (POSIX). */
	SIGTERM(15 + 128), /* Termination (ANSI). */
	SIGSTKFLT(16 + 128), /* Stack fault. */
	SIGCLD(17 + 128), /* Same as SIGCHLD (System V). */
	SIGCHLD(17 + 128), /* Child status has changed (POSIX). */
	SIGCONT(18 + 128), /* Continue (POSIX). */
	SIGSTOP(19 + 128), /* Stop, unblockable (POSIX). */
	SIGTSTP(20 + 128), /* Keyboard stop (POSIX). */
	SIGTTIN(21 + 128), /* Background read from tty (POSIX). */
	SIGTTOU(22 + 128), /* Background write to tty (POSIX). */
	SIGURG(23 + 128), /* Urgent condition on socket (4.2 BSD). */
	SIGXCPU(24 + 128), /* CPU limit exceeded (4.2 BSD). */
	SIGXFSZ(25 + 128), /* File size limit exceeded (4.2 BSD). */
	SIGVTALRM(26 + 128), /* Virtual alarm clock (4.2 BSD). */
	SIGPROF(27 + 128), /* Profiling alarm clock (4.2 BSD). */
	SIGWINCH(28 + 128), /* Window size change (4.3 BSD, Sun). */
	SIGPOLL(29 + 128), /* Pollable event occurred (System V). */
	SIGIO(29 + 128), /* I/O now possible (4.2 BSD). */
	SIGPWR(30 + 128), /* Power failure restart (System V). */
	SIGSYS(31); /* Bad system call. */

	/**
	 * Map from the unix return values to the enum.
	 */
	static Map<Integer, UnixReturnValue> valToEnum = Maps.newHashMap();

	static {
		for (UnixReturnValue value : UnixReturnValue.values()) {
			valToEnum.put(value.getReturnValue(), value);
		}
	}

	/**
	 * Look up the associated enum for a given return value.
	 * 
	 * @param retval
	 * @return
	 */
	static public UnixReturnValue get(int retval) {
		return valToEnum.get(retval);
	}

	/**
	 * The return value.
	 */
	private int returnValue;

	UnixReturnValue(int returnValue) {
		this.returnValue = returnValue;
	}

	/**
	 * @return the returnValue
	 */
	public int getReturnValue() {
		return returnValue;
	}
}
