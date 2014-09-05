/*
 * Copyright (C) 2014 Google Inc.
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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

/**
 * Tests for the {@link BaseNativeApplicationRunner}.
 *
 * @author Keith M. Hughes
 */
public class BaseNativeApplicationRunnerTest {
  private BaseNativeApplicationRunner runner;

  @Before
  public void setup() {
    runner = new BaseNativeApplicationRunner(null, null) {
      @Override
      public boolean handleProcessExit(int exitValue, String[] commands) {
        return false;
      }
    };
  }

  /**
   * Test the parsing of flags.
   */
  @Test
  public void testFlagsParsing() {
    Map<String, Object> config = Maps.newHashMap();
    config.put(NativeApplicationRunner.EXECUTABLE_PATHNAME, "foo/bar");
    config.put(NativeApplicationRunner.EXECUTABLE_FLAGS, " --a -b -c");

    runner.configure(config);

    Assert.assertArrayEquals(new String[] { "foo/bar", "--a", "-b", "-c"}, runner.getCommandLine());
  }

  /**
   * Test the parsing of environment variables without nulls.
   */
  @Test
  public void testEnvironmentParsing() {
    Map<String, Object> config = Maps.newHashMap();
    config.put(NativeApplicationRunner.EXECUTABLE_PATHNAME, "foo/bar");
    config.put(NativeApplicationRunner.EXECUTABLE_FLAGS, " --a -b -c");
    config.put(NativeApplicationRunner.EXECUTABLE_ENVIRONMENT, "     foo=bar bar=bletch");

    runner.configure(config);

    Map<String,String> env = runner.getEnvironment();
    Assert.assertEquals("bar", env.get("foo"));
    Assert.assertEquals("bletch", env.get("bar"));

    Assert.assertArrayEquals(new String[] { "foo/bar", "--a", "-b", "-c"}, runner.getCommandLine());
  }

  /**
   * Test the parsing of environment variables with nulls.
   */
  @Test
  public void testEnvironmentParsingNulls() {
    Map<String, Object> config = Maps.newHashMap();
    config.put(NativeApplicationRunner.EXECUTABLE_PATHNAME, "foo/bar");
    config.put(NativeApplicationRunner.EXECUTABLE_ENVIRONMENT, "foo bar");

    runner.configure(config);

    Map<String,String> env = runner.getEnvironment();
    Assert.assertTrue(env.containsKey("foo"));
    Assert.assertNull(env.get("foo"));
    Assert.assertTrue(env.containsKey("bar"));
    Assert.assertNull(env.get("bar"));
  }

  /**
   * Test modifying an environment
   */
  @Test
  public void testEnvironmentModification() {
    Map<String, String> processEnvironment = Maps.newHashMap();
    processEnvironment.put("foo", "bar");
    processEnvironment.put("bletch", "spam");
    processEnvironment.put("spam", "blorg");

    Map<String, String> modificationEnvironment = Maps.newHashMap();
    modificationEnvironment.put("foo", "yowza");
    modificationEnvironment.put("hiya", "there");
    modificationEnvironment.put("spam", null);

    runner.modifyEnvironment(processEnvironment, modificationEnvironment);

    Assert.assertEquals("yowza", processEnvironment.get("foo"));
    Assert.assertEquals("spam", processEnvironment.get("bletch"));
    Assert.assertEquals("there", processEnvironment.get("hiya"));
    Assert.assertFalse(processEnvironment.containsKey("spam"));
  }
}
