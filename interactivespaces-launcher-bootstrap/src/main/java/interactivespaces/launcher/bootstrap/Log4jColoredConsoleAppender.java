/*
 * Copyright 2002-2009 Daniel W. Dyer
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


package interactivespaces.launcher.bootstrap;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Priority;
import org.apache.log4j.spi.LoggingEvent;

/**
 * Color-coded console appender for Log4J.
 *
 * @author Paul Hounshell
 */
public class Log4jColoredConsoleAppender extends ConsoleAppender {
  /**
   * ANSI code for normal text.
   */
  private static final int NORMAL = 0;

  /**
   * ANSI code for bright text.
   */
  private static final int BRIGHT = 1;

  /**
   * ANSI code for black text.
   */
  private static final int FOREGROUND_BLACK = 30;

  /**
   * ANSI code for red text.
   */
  private static final int FOREGROUND_RED = 31;

  /**
   * ANSI code for green text.
   */
  private static final int FOREGROUND_GREEN = 32;

  /**
   * ANSI code for yellow text.
   */
  private static final int FOREGROUND_YELLOW = 33;

  /**
   * ANSI code for blue text.
   */
  private static final int FOREGROUND_BLUE = 34;

  /**
   * ANSI code for magenta text.
   */
  private static final int FOREGROUND_MAGENTA = 35;

  /**
   * ANSI code for cyan text.
   */
  private static final int FOREGROUND_CYAN = 36;

  /**
   * ANSI code for white text.
   */
  private static final int FOREGROUND_WHITE = 37;

  /**
   * ANSI code for using the default console colors.
   */
  private static final String CONSOLE_DEFAULT = "";

  /**
   * ANSI prefix for escape codes.
   */
  private static final String PREFIX = "\u001b[";

  /**
   * ANSI suffix for escape codes.
   */
  private static final String SUFFIX = "m";

  /**
   * ANSI code separator.
   */
  private static final char SEPARATOR = ';';

  /**
   * ANSI code to end an escape sequence and return to normal text.
   */
  private static final String END_COLOUR = PREFIX + SUFFIX;

  /**
   * ANSI code sequence for FATAL log messages.
   */
  private static final String FATAL_COLOUR = PREFIX + BRIGHT + SEPARATOR + FOREGROUND_RED + SUFFIX;

  /**
   * ANSI code sequence for ERROR log messages.
   */
  private static final String ERROR_COLOUR = PREFIX + NORMAL + SEPARATOR + FOREGROUND_RED + SUFFIX;

  /**
   * ANSI code sequence for WARN log messages.
   */
  private static final String WARN_COLOUR  = PREFIX + NORMAL + SEPARATOR + FOREGROUND_YELLOW + SUFFIX;

  /**
   * ANSI code sequence for INFO log messages.
   */
  private static final String INFO_COLOUR  = CONSOLE_DEFAULT;

  /**
   * ANSI code sequence for DEBUG log messages.
   */
  private static final String DEBUG_COLOUR = PREFIX + BRIGHT + SEPARATOR + FOREGROUND_BLACK + SUFFIX;

  /**
   * ANSI code sequence for TRACE log messages.
   */
  private static final String TRACE_COLOUR = PREFIX + BRIGHT + SEPARATOR + FOREGROUND_BLACK + SUFFIX;

  /**
   * Wraps the ANSI control characters around the
   * output from the super-class Appender.
   *
   * @param event
   *          The logging event
   */
  protected void subAppend(LoggingEvent event) {
    // qw is the console output stream from base class.

    qw.write(getColour(event.getLevel()));
    super.subAppend(event);
    qw.write(END_COLOUR);

    if (this.immediateFlush) {
      qw.flush();
    }
  }

  /**
   * Get the appropriate control characters to change
   * the colour for the specified logging level.
   *
   * @param level
   *          The logging level
   *
   * @return the ANSI color sequence for this level of logging message
   */
  private String getColour(Level level) {
    switch (level.toInt()) {
      case Priority.FATAL_INT: return FATAL_COLOUR;
      case Priority.ERROR_INT: return ERROR_COLOUR;
      case Priority.WARN_INT: return WARN_COLOUR;
      case Priority.INFO_INT: return INFO_COLOUR;
      case Priority.DEBUG_INT: return DEBUG_COLOUR;
      default: return TRACE_COLOUR;
    }
  }
}
