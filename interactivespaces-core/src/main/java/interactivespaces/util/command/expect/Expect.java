package interactivespaces.util.command.expect;

import interactivespaces.InteractiveSpacesException;
import interactivespaces.util.resource.ManagedResource;

import com.google.common.collect.Lists;

import org.apache.commons.logging.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.Pipe;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Provides similar functions as the Unix Expect tool.<br>
 * There are two ways to create an Expect object: a constructor that takes an
 * {@link InputStream} handle and {@link OutputStream} handle; or spawning a
 * process by providing a comamnd String. <br>
 * <br>
 * The API is loosely based on Perl Expect library:<br>
 * <a href="http://search.cpan.org/~rgiersig/Expect-1.15/Expect.pod">
 * http://search.cpan.org/~rgiersig/Expect-1.15/Expect.pod</a>
 *
 * <p>
 * If you are not familiar with the Tcl version of Expect, take a look at:<br>
 * <a href="http://oreilly.com/catalog/expect/chapter/ch03.html">
 * http://oreilly.com/catalog/expect/chapter/ch03.html</a> <br>
 * <br>
 * Expect uses a thread to convert InputStream to a SelectableChannel; other
 * than this, no multi-threading is used.<br>
 * A call to expect() will block for at most timeout seconds. Expect is not
 * designed to be thread-safe, in other words, do not call methods of the same
 * Expect object in different threads.
 *
 * <p>
 * Expect return values either give success or say what error condition
 * happened.
 *
 * @author Ronnie Dong
 * @author Trevor Pering -- modified for use with Interactive Spaces
 * @author Keith M. Hughes even more IS changes
 */
public class Expect implements ManagedResource {

  /**
   * Creates an Expect object by spawning a command.<br>
   * To Linux users, perhaps you need to use "bash -i" if you want to spawn
   * Bash.
   *
   * <p>
   * Note: error stream of the process is redirected to output stream.
   *
   * @param command
   *          the command to run in the process
   * @param executorService
   *          the executor service to use
   * @param log
   *          the log to use
   *
   * @return expect object created using the input and output handles from the
   *         spawned process
   *
   * @throws InteractiveSpacesException
   *           the process didn't start up properly
   */
  public static Expect spawn(String command, ScheduledExecutorService executorService, Log log)
      throws InteractiveSpacesException {
    try {
      ProcessBuilder pb = new ProcessBuilder(command.split(" "));
      pb.redirectErrorStream(true);
      Process p;
      p = pb.start();
      Expect expect = new Expect(p.getInputStream(), p.getOutputStream(), executorService, log);
      expect.setProcess(p);

      return expect;
    } catch (Exception e) {
      throw new InteractiveSpacesException("Could not spawn expect process", e);
    }
  }

  /**
   * Successful return.
   */
  public static final int RETV_OK = 0;

  /**
   * Timeout error.
   */
  public static final int RETV_TIMEOUT = -1;

  /**
   * EOF error.
   */
  public static final int RETV_EOF = -2;

  /**
   * IOException error.
   */
  public static final int RETV_IOEXCEPTION = -9;

  /**
   * A stream to duplicate output to.
   */
  private static PrintStream duplicatedTo = null;

  /**
   * The default timeout, in milliseconds.
   */
  private long defaultTimeout = 60 * 1000;

  /**
   * {@code true} if calls should throw an exception on non-successful return
   * values.
   */
  private boolean throwOnError = false;

  /**
   * {@code true} if the timeout should be reset every time new input is
   * received.
   */
  private boolean restartTimeoutUponReceive = false;

  /**
   * Buffer for storing responses.
   */
  private final StringBuffer buffer = new StringBuffer();

  /**
   * {@true} if should not transfer previous content.
   */
  private boolean noTransfer = false;

  /**
   * String before the last match (if there was a match), updated after each
   * expect() call.
   */
  private String before;

  /**
   * String representing the last match (if there was a match), updated after
   * each expect() call.
   */
  private String match;

  /**
   * {@code true} if the last match was successful, updated after each expect()
   * call.
   */
  private boolean success = false;

  /**
   * The input stream for detecting what we expect.
   */
  private final InputStream input;

  /**
   * The input stream for detecting what we expect.
   */
  private final OutputStream output;

  /**
   * A selectable channel, created around the given input stream.
   */
  private Pipe.SourceChannel inputChannel;

  /**
   * A selector used for detecting input on the input channel.
   */
  private Selector selector;

  /**
   * The potential process being read to get the expect responses.
   */
  private Process process = null;

  /**
   * The executor service to use.
   */
  private final ScheduledExecutorService executorService;

  /**
   * Logging class for managing log output.
   */
  private final Log log;

  /**
   * If an IOException is thrown during an expt call this will be that
   * exception.
   */
  private IOException thrownIOException;

  /**
   * The future for controlling the thread which is reading information from the
   * input.
   */
  private Future<?> pipingFuture;

  /**
   * Construct an Expect object that can be used to manage communication to/from
   * the given streams.
   *
   * @param input
   *          input stream for what to expect
   * @param output
   *          output stream for sending commands
   * @param executorService
   *          the executor service to use
   * @param log
   *          logger used for logging
   */
  public Expect(InputStream input, OutputStream output, ScheduledExecutorService executorService, Log log) {
    this.executorService = executorService;
    this.input = input;
    this.output = output;
    this.log = log;
  }

  @Override
  public void startup() {
    Pipe.SourceChannel inputChannel = null;
    Selector selector = null;
    try {
      inputChannel = inputStreamToSelectableChannel(input);
      selector = Selector.open();
      inputChannel.register(selector, SelectionKey.OP_READ);
    } catch (IOException e) {
      close();
      throw new InteractiveSpacesException("Fatal error when initializing pipe or selector", e);
    }
    this.inputChannel = inputChannel;
    this.selector = selector;
  }

  @Override
  public void shutdown() {
    if (pipingFuture != null) {
      pipingFuture.cancel(true);
      pipingFuture = null;
      close();
    }
  }

  /**
   * Get the string before the current match.
   *
   * @return the string before a match
   */
  public String getBefore() {
    return before;
  }

  /**
   * Get the current match string.
   *
   * @return the current match string
   */
  public String getMatch() {
    return match;
  }

  /**
   * Is the current match a success?
   *
   * @return {@code true} if a success
   */
  public boolean isSuccess() {
    return success;
  }

  /**
   * Converts an {@link InputStream} to a {@link SelectableChannel}. A thread is
   * created to read from the InputStream, and write to a pipe. The source of
   * the pipe is returned as an input handle from which you can perform
   * unblocking read. The thread will terminate when reading EOF from
   * InputStream, or when InputStream is closed, or when the returned Channel is
   * closed(pipe broken).
   *
   * @param input
   *          the input stream to be read from
   *
   * @return a non-blocking channel to be read from
   *
   * @throws IOException
   *           most unlikely
   */
  private Pipe.SourceChannel inputStreamToSelectableChannel(final InputStream input) throws IOException {
    Pipe pipe = Pipe.open();
    pipe.source().configureBlocking(false);
    final OutputStream out = Channels.newOutputStream(pipe.sink());
    Runnable piping = new Runnable() {
      @Override
      public void run() {
        byte[] buffer = new byte[1024];
        try {
          for (int n = 0; n != -1; n = input.read(buffer)) {
            out.write(buffer, 0, n);
            if (duplicatedTo != null) {
              String toWrite = new String(buffer, 0, n);

              // no exception will be thrown
              duplicatedTo.append(toWrite);
            }
          }
          log.debug("EOF from InputStream");

          // now that input has EOF, close it. other than this, do not close
          // input
          input.close();
        } catch (IOException e) {
          log.warn("IOException when piping from InputStream, " + "now the piping thread will end", e);
        } finally {
          log.debug("closing sink of the pipe");
          try {
            output.close();
          } catch (IOException e) {
            log.warn("Trouble closing Expect output", e);
          }
        }
      }
    };

    pipingFuture = executorService.submit(piping);

    return pipe.source();
  }

  /**
   * @return the spawned process, if this {@link Expect} object is created by
   *         spawning a process
   */
  public Process getProcess() {
    return process;
  }

  /**
   * Set the process that is being read.
   *
   * @param process
   *          the spawned process, if this {@link Expect} object is created by
   *          spawning a process
   */
  void setProcess(Process process) {
    this.process = process;
  }

  /**
   * Send the string.
   *
   * @param str
   *          the string to send
   */
  public void send(String str) {
    this.send(str.getBytes());
  }

  /**
   * Send the string with a new line on the end.
   *
   * @param str
   *          the string to send
   */
  public void sendLn(String str) {
    String strLn = str + "\r";
    this.send(strLn.getBytes());
  }

  /**
   * Send the byte array on the expect output.
   *
   * @param toWrite
   *          Write a byte array to the output handle, notice flush()
   */
  public void send(byte[] toWrite) {
    log.info("sending: " + bytesToPrintableString(toWrite));
    try {
      output.write(toWrite);
      output.flush();
    } catch (IOException e) {
      log.error("Error when sending bytes to output", e);
    }
  }

  /**
   * match a sequence of patterns.
   *
   * <p>
   * Use the default timeout.
   *
   * @param patterns
   *          the patterns in the order to match, can be strings of regex
   *          patterns or actual {@code java.util.regex.Pattern} instances.
   *
   * @return the expect return value
   *
   * @throws Exception
   *           exception if return values should throw exceptions
   */
  public int expect(Object... patterns) throws Exception {
    return expect(defaultTimeout, patterns);
  }

  /**
   * Match a series of patterns.
   *
   * @param timeout
   *          timeout for input in milliseconds
   * @param patterns
   *          the patterns in the order to match, can be strings of regex
   *          patterns or actual {@code java.util.regex.Pattern} instances.
   *
   * @return the expect return value
   *
   * @throws Exception
   *           exception if return values should throw exceptions
   */
  public int expect(long timeout, Object... patterns) throws Exception {
    List<Pattern> list = Lists.newArrayList();
    for (Object o : patterns) {
      if (o instanceof String) {
        list.add(Pattern.compile(Pattern.quote((String) o)));
      } else if (o instanceof Pattern) {
        list.add((Pattern) o);
      } else {
        log.warn("Object " + o.toString() + " (class: " + o.getClass().getName() + ") is neither a String nor "
            + "a java.util.regex.Pattern, using as a literal String");
        list.add(Pattern.compile(Pattern.quote(o.toString())));
      }
    }
    return expect(timeout, list);
  }

  /**
   * Expect will wait for the input handle to produce one of the patterns in the
   * list. If a match is found, this method returns immediately; otherwise, the
   * methods waits for up to timeout seconds, then returns. If timeout is less
   * than or equal to 0 Expect will check one time to see if the internal buffer
   * contains the pattern.
   *
   * @param timeout
   *          timeout in seconds
   * @param list
   *          List of Java {@link Pattern}s used for matching the input stream
   *
   * @return position of the matched pattern within the list (starting from 0);
   *         or a negative number if there is an IOException, EOF or timeout
   *
   * @throws Exception
   *           exception if return values should throw exceptions
   */
  public int expect(long timeout, List<Pattern> list) throws Exception {
    return maybeThrow(expectInternal(timeout, list));
  }

  /**
   * Match a collection of regex patterns.
   *
   * @param timeout
   *          the timeout to wait for responses
   * @param list
   *          the commands
   *
   * @return the return value for the result
   */
  private int expectInternal(long timeout, List<Pattern> list) {
    log.info("Expecting " + list);

    clearGlobalVariables();
    long endTime = System.currentTimeMillis() + timeout;

    try {
      ByteBuffer bytes = ByteBuffer.allocate(1024);
      int n;
      while (true) {
        for (int i = 0; i < list.size(); i++) {
          log.trace("trying to match " + list.get(i) + " against buffer \"" + buffer + "\"");
          Matcher m = list.get(i).matcher(buffer);
          if (m.find()) {
            log.trace("success!");
            int matchStart = m.start(), matchEnd = m.end();
            this.before = buffer.substring(0, matchStart);
            this.match = m.group();
            this.success = true;
            if (!noTransfer) {
              buffer.delete(0, matchEnd);
            }
            return i;
          }
        }

        long waitTime = endTime - System.currentTimeMillis();
        if (restartTimeoutUponReceive) {
          waitTime = timeout;
        }
        if (waitTime <= 0) {
          log.debug("Timeout when expecting " + list);
          return RETV_TIMEOUT;
        }

        selector.select(waitTime);
        if (selector.selectedKeys().size() == 0) {
          log.debug("Timeout when expecting " + list);
          return RETV_TIMEOUT;
        }
        selector.selectedKeys().clear();
        if ((n = inputChannel.read(bytes)) == -1) {
          log.debug("EOF when expecting " + list);
          return RETV_EOF;
        }
        StringBuilder tmp = new StringBuilder();
        for (int i = 0; i < n; i++) {
          buffer.append((char) bytes.get(i));
          byteToPrintableString(tmp, bytes.get(i));
        }
        log.debug("Obtained following from InputStream: " + tmp);
        bytes.clear();
      }
    } catch (IOException e) {
      log.error("IOException when selecting or reading", e);
      thrownIOException = e;

      return RETV_IOEXCEPTION;
    }

  }

  /**
   * Check for an expected EOF.
   *
   * @param timeout
   *          the timeout to wait for the EOF, in milliseconds
   *
   * @return the expect return value
   *
   * @throws Exception
   *           exception if return values should throw exceptions
   */
  public int expectEOF(long timeout) throws Exception {
    int retv = RETV_OK;
    try {
      retv = expect(timeout, new ArrayList<Pattern>());
    } catch (EOFException e) {
      // This is actually what we expect!
    }
    if (retv == RETV_EOF) {
      success = true;
      before = this.buffer.toString();
      buffer.delete(0, buffer.length());
    }
    try {
      return maybeThrow(retv);
    } catch (EOFException e) {
      throw new RuntimeException("Shouldn't throw EOF here", e);
    }
  }

  /**
   * Check for an expected EOF within the default timeout.
   *
   * @return the expect return value
   *
   * @throws Exception
   *           exception if return values should throw exceptions
   */
  public int expectEOF() throws Exception {
    return expectEOF(defaultTimeout);
  }

  /**
   * Throws checked exceptions when expectEOF was not successful.
   *
   * @param timeout
   *          the timeout to wait for the EOF
   *
   * @return a successful return value
   *
   * @throws Exception
   *           exception if something bad happened
   */
  public int expectEOFOrThrow(long timeout) throws Exception {
    int retv = expectEOF(timeout);
    if (retv == RETV_TIMEOUT) {
      throw new TimeoutException();
    } else if (retv == RETV_IOEXCEPTION) {
      throw thrownIOException;
    }

    return retv;
  }

  /**
   * Expect an EOF. The default timeout is used.
   *
   * @return the expect return value
   *
   * @throws Exception
   *           exception if return values should throw exceptions
   */
  public int expectEOFOrThrow() throws Exception {
    return expectEOFOrThrow(defaultTimeout);
  }

  /**
   * Throw an error based on the return value if throw on error was requested.
   *
   * @param retValue
   *          the return value
   *
   * @return the return value if a throw was not requested
   *
   * @throws Exception
   *           the exception thrown
   */
  private int maybeThrow(int retValue) throws Exception {
    if (!success && throwOnError) {
      switch (retValue) {
        case RETV_TIMEOUT:
          throw new TimeoutException();
        case RETV_EOF:
          throw new EOFException();
        case RETV_IOEXCEPTION:
          throw thrownIOException;
        default:
          throw new RuntimeException("Unknown error code " + retValue);
      }
    }
    return retValue;
  }

  /**
   * This method calls {@link #expect(int, Object...) expect(timeout, patterns)}
   * , and throws checked exceptions when expect was not successful. Useful when
   * you want to simplify error handling: for example, when you send a series of
   * commands to an SSH server, you expect a prompt after each send, however the
   * server may die or the prompt may take forever to appear, you would want to
   * skip the following commands if those occurred. In such a case this method
   * will be handy.
   *
   * @param timeout
   *          the time, in milliseconds, to wait for responses
   * @param patterns
   *          the patterns in the order to match, can be strings of regex
   *          patterns or actual {@code java.util.regex.Pattern} instances.
   *
   * @return the return code for the match
   *
   * @throws Exception
   *           exception if return values should throw exceptions
   */
  public int expectOrThrow(long timeout, Object... patterns) throws Exception {
    int retv = expect(timeout, patterns);
    switch (retv) {
      case RETV_TIMEOUT:
        throw new TimeoutException();
      case RETV_EOF:
        throw new EOFException();
      case RETV_IOEXCEPTION:
        throw thrownIOException;
      default:
        return retv;
    }
  }

  /**
   * Expect a series of patterns. Use the default timeout.
   *
   * @param patterns
   *          the patterns in the order to match, can be strings of regex
   *          patterns or actual {@code java.util.regex.Pattern} instances.
   *
   * @return the return code
   *
   * @throws Exception
   *           exception if return values should throw exceptions
   */
  public int expectOrThrow(Object... patterns) throws Exception {
    return expectOrThrow(defaultTimeout, patterns);
  }

  /**
   * Clear all variables that the expect keeps.
   */
  private void clearGlobalVariables() {
    success = false;
    match = null;
    before = null;
  }

  /**
   * The OutputStream passed to Expect constructor is closed; the InputStream is
   * not closed (there is no need to close the InputStream).<br>
   * It is suggested that this method be called after the InputStream has come
   * to EOF. For example, when you connect through SSH, send an "exit" command
   * first, and then call this method.<br>
   * <br>
   *
   * When this method is called, the thread which write to the sink of the pipe
   * will end.
   */
  public void close() {
    try {
      output.close();
    } catch (IOException e) {
      log.warn("Exception when closing OutputStream", e);
    }
    try {
      inputChannel.close();
    } catch (IOException e) {
      log.warn("Exception when closing input Channel", e);
    }
  }

  /**
   * Get the default timeout for responses.
   *
   * @return the timeout, in milliseconds
   */
  public long getDefaultTimeout() {
    return defaultTimeout;
  }

  /**
   * Set the default timeout for responses.
   *
   * @param defaultTimeout
   *          the timeout, in milliseconds
   */
  public void setDefaultTimeout(long defaultTimeout) {
    this.defaultTimeout = defaultTimeout;
  }

  /**
   * Set if the expect should throw exceptions on errors.
   *
   * @param throwOnError
   *          {@code true} if exceptions will be thrown
   */
  public void setThrowOnError(boolean throwOnError) {
    this.throwOnError = throwOnError;
  }

  /**
   * Is the expect throwing exceptions on errors?
   *
   * @return {@code true} if exceptions will be thrown
   */
  public boolean isThrowOnError() {
    return throwOnError;
  }

  /**
   * Should the timeout be reset when new input is received?
   *
   * @return {@code true} if the timeout should be reset
   */
  public boolean isRestartTimeoutUponReceive() {
    return restartTimeoutUponReceive;
  }

  /**
   * Set whether the timeout should be reset when new input is received?
   *
   * @param restartTimeoutUponReceive
   *          {@code true} if the timeout should be reset
   */
  public void setRestartTimeoutUponReceive(boolean restartTimeoutUponReceive) {
    this.restartTimeoutUponReceive = restartTimeoutUponReceive;
  }

  /**
   * Set whether previous content should not be transfered.
   *
   * @param noTransfer
   *          {@true} if should not transfer previous content
   */
  public void setNoTransfer(boolean noTransfer) {
    this.noTransfer = noTransfer;
  }

  /**
   * Should previous content not be transfered?
   *
   * @return {@true} if should not transfer previous content
   */
  public boolean isNoTransfer() {
    return noTransfer;
  }

  /**
   * Convert a byte array to a string, each byte is converted to an ASCII
   * character, if the byte represents a control character, it is replaced by a
   * printable caret notation <a href="http://en.wikipedia.org/wiki/ASCII">
   * http://en.wikipedia.org/wiki/ASCII </a>, or an escape code if possible.
   *
   * @param bytes
   *          bytes to be printed
   *
   * @return string representation of the byte array
   */
  private String bytesToPrintableString(byte[] bytes) {
    StringBuilder sb = new StringBuilder();
    for (byte b : bytes) {
      byteToPrintableString(sb, b);
    }
    return sb.toString();
  }

  /**
   * Add the byte to the builder as part of a string.
   *
   * <p>
   * Special characters are escaped.
   *
   * @param buffer
   *          the builder being added to
   * @param b
   *          the byte to change
   */
  private void byteToPrintableString(StringBuilder buffer, byte b) {
    String s = new String(new byte[] { b });

    if (b >= 0 && b < 32) {
      buffer.append("^").append((char) (b + 64));
    } else if (b == 127) {
      buffer.append("^?");
    } else if (b == 9) {
      buffer.append("\\t");
    } else if (b == 10) {
      buffer.append("\\n");
    } else if (b == 13) {
      buffer.append("\\r");
    } else {
      buffer.append((char) b);
    }
  }

  /**
   * The exception if a timeout occurred while waiting for input.
   */
  @SuppressWarnings("serial")
  public static class TimeoutException extends Exception {
  }

  /**
   * The exception if EOF was reached before the match.
   */
  @SuppressWarnings("serial")
  public static class EOFException extends Exception {
  }
}
