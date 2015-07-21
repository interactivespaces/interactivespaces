/*
 * Copyright (C) 2013 Google Inc.
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

package interactivespaces.resource;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test {@link Version} and {@link VersionRange}.
 *
 * @author Keith M. Hughes
 */
public class VersionTest {

  @Test
  public void testEquals() {
    Version v1 = new Version(1, 2, 3);
    Version v2 = new Version(1, 2, 3);

    Assert.assertTrue(v1.equals(v1));
    Assert.assertTrue(v1.equals(v2));
    Assert.assertTrue(v1.lessThanOrEqual(v2));
    Assert.assertTrue(v1.greaterThanOrEqual(v2));
    Assert.assertTrue(v2.lessThanOrEqual(v1));
    Assert.assertTrue(v2.greaterThanOrEqual(v1));
    Assert.assertTrue(v1.lessThanOrEqual(v1));
    Assert.assertTrue(v1.greaterThanOrEqual(v1));
    Assert.assertTrue(v1.compareTo(v2) == 0);
  }

  @Test
  public void testCompares() {
    Version v1 = new Version(1, 2, 3);
    Version v2 = new Version(1, 2, 4);

    Assert.assertTrue(v1.lessThan(v2));
    Assert.assertTrue(v1.lessThanOrEqual(v2));
    Assert.assertTrue(v2.greaterThan(v1));
    Assert.assertTrue(v2.greaterThanOrEqual(v1));
    Assert.assertTrue(v1.compareTo(v2) < 0);
  }

  @Test
  public void testQualifierComparisons() {
    Version v1 = new Version(1, 2, 3);
    Version v2 = new Version(1, 2, 3, "foo");
    Version v3 = new Version(1, 2, 3, "yada");

    Assert.assertFalse(v1.equals(v2));
    Assert.assertFalse(v2.equals(v3));
    Assert.assertTrue(v1.lessThan(v2));
    Assert.assertTrue(v2.lessThan(v3));
  }

  @Test
  public void testIncrements() {
    Version v0 = new Version(1, 2, 3);

    Version v1 = v0.incrementMajor();
    Assert.assertEquals(v0.getMajor() + 1, v1.getMajor());
    Assert.assertEquals(0, v1.getMinor());
    Assert.assertEquals(0, v1.getMicro());

    Version v2 = v0.incrementMinor();
    Assert.assertEquals(v0.getMajor(), v2.getMajor());
    Assert.assertEquals(0, v2.getMicro());

    Version v3 = v0.incrementMicro();
    Assert.assertEquals(v0.getMajor(), v3.getMajor());
    Assert.assertEquals(v0.getMinor(), v3.getMinor());
    Assert.assertEquals(v0.getMicro() + 1, v3.getMicro());
  }

  /**
   * Test a variety of version parses
   */
  @Test
  public void testVersionParses() {
    Assert.assertEquals(new Version(1, 2, 3), Version.parseVersion("1.2.3"));
    Assert.assertEquals(new Version(1, 2, 3, "foo"), Version.parseVersion("1.2.3.foo"));

    Assert.assertEquals(new Version(1, 2, 0), Version.parseVersion("1.2"));
    Assert.assertEquals(new Version(1, 2, 0, "foo"), Version.parseVersion("1.2.foo"));

    Assert.assertEquals(new Version(1, 0, 0), Version.parseVersion("1"));
    Assert.assertEquals(new Version(1, 0, 0, "foo"), Version.parseVersion("1.foo"));

    tryBadVersionParse("qwerty");
    tryBadVersionParse("qwerty.foo");
    tryBadVersionParse("1.qwerty.foo");
    tryBadVersionParse("1.2.qwerty.foo");
  }

  /**
   * Try a bad version parse.
   *
   * @param version
   *          the bad version string
   */
  private void tryBadVersionParse(String version) {
    try {
      Version.parseVersion(version);

      Assert.fail(String.format("Properly parsed bad version string %s", version));
    } catch (Exception e) {
    }
  }

  @Test
  public void testRange() {
    Version version = new Version(1, 2, 3);

    // Test infinity match.
    Assert.assertTrue(new VersionRange(new Version(1, 2, 3)).contains(version));
    Assert.assertTrue(new VersionRange(new Version(1, 2, 0)).contains(version));
    Assert.assertFalse(new VersionRange(new Version(1, 2, 4)).contains(version));

    // In range
    Assert.assertTrue(new VersionRange(new Version(1, 2, 3), new Version(1, 2, 4), true).contains(version));
    Assert.assertTrue(new VersionRange(new Version(1, 2, 3), new Version(1, 2, 4), false).contains(version));

    // Below range, both inclusive and exclusive.
    Assert.assertFalse(new VersionRange(new Version(1, 3, 3), new Version(1, 3, 4), true).contains(version));

    // Above range
    Assert.assertFalse(new VersionRange(new Version(0, 3, 3), new Version(0, 3, 4), true).contains(version));

    // Bump against max, inclusive
    Assert.assertTrue(new VersionRange(new Version(1, 2, 1), new Version(1, 2, 3), true).contains(version));

    // Bump against max, exclusive
    Assert.assertFalse(new VersionRange(new Version(1, 2, 1), new Version(1, 2, 3), false).contains(version));
  }

  /**
   * Test converting version ranges to strings.
   */
  @Test
  public void testVersionRangeToString() {
    Assert.assertEquals("1.8.3", new VersionRange(new Version(1, 8, 3)).toString());
    Assert.assertEquals("[1.8.3, 1.8.4]",
        new VersionRange(new Version(1, 8, 3), new Version(1, 8, 4), true).toString());
    Assert.assertEquals("[1.8.3, 1.8.4)",
        new VersionRange(new Version(1, 8, 3), new Version(1, 8, 4), false).toString());
  }

  /**
   * Test a variety of version parses
   */
  @Test
  public void testVersionRangeParses() {
    Assert.assertEquals(new VersionRange(new Version(1, 2, 3)), VersionRange.parseVersionRange("1.2.3"));

    Assert.assertEquals(new VersionRange(new Version(1, 2, 0), new Version(2, 3, 0), false),
        VersionRange.parseVersionRange("[1.2, 2.3)"));
    Assert.assertEquals(new VersionRange(new Version(1, 2, 0), new Version(2, 3, 0), true),
        VersionRange.parseVersionRange("[1.2, 2.3]"));
    Assert.assertEquals(new VersionRange(new Version(1, 2, 0), new Version(1, 2, 1), false),
        VersionRange.parseVersionRange("=1.2"));

    tryBadVersionRangeParse("qwerty");
    tryBadVersionRangeParse("[1]");
    tryBadVersionRangeParse("[1)");
    tryBadVersionRangeParse("1,2)");
    tryBadVersionRangeParse("1,2]");
    tryBadVersionRangeParse("[1 2)");
    tryBadVersionRangeParse("[1 2]");
    tryBadVersionRangeParse("[1 2");
    tryBadVersionRangeParse("(1, 2)");
  }

  /**
   * Try a bad version range parse.
   *
   * @param range
   *          the bad version range string
   */
  private void tryBadVersionRangeParse(String range) {
    try {
      VersionRange.parseVersionRange(range);

      Assert.fail();
    } catch (Exception e) {
    }
  }

}
