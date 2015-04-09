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

import junit.framework.Assert;
import org.junit.Test;

/**
 * Test the {@link VersionedResourceCollection} class.
 *
 * @author Keith M. Hughes
 */
public class VersionedResourceCollectionTest {

  /**
   * Try getting items just by version.
   */
  @Test
  public void testVersion() {
    VersionedResourceCollection<Integer> resources = VersionedResourceCollection.newVersionedResourceCollection();

    Version v0 = new Version(1, 0, 0);
    Integer v0Value = 12;

    Version v1 = new Version(1, 1, 0);
    Integer v1Value = 75;

    Version v2 = new Version(2, 0, 0);
    Integer v2Value = 13;

    resources.addResource(v0, v0Value);
    resources.addResource(v1, v1Value);
    resources.addResource(v2, v2Value);

    Assert.assertEquals(v0Value, resources.getResource(v0));
    Assert.assertEquals(v1Value, resources.getResource(v1));
    Assert.assertEquals(v2Value, resources.getResource(v2));

    Assert.assertNull(resources.getResource(v2.incrementMajor()));
  }

  /**
   * Test when resources available in range.
   */
  @Test
  public void testInRange() {
    VersionedResourceCollection<Integer> resources = VersionedResourceCollection.newVersionedResourceCollection();

    Version v0 = new Version(1, 0, 0);
    Integer v0Value = 12;

    Version v1 = new Version(1, 1, 0);
    Integer v1Value = 75;

    Version v2 = new Version(2, 0, 0);
    Integer v2Value = 13;

    resources.addResource(v0, v0Value);
    resources.addResource(v1, v1Value);
    resources.addResource(v2, v2Value);

    // make sure we get the entry with the largest version
    Assert.assertEquals(v2Value, resources.getHighestEntry());

    // Should get highest
    VersionRange range0 = new VersionRange(v0, v2, true);
    Assert.assertEquals(v2Value, resources.getResource(range0));

    // Should get second highest
    VersionRange range1 = new VersionRange(v0, v2, false);
    Assert.assertEquals(v1Value, resources.getResource(range1));

    // Should get second highest
    VersionRange range2 = new VersionRange(v0, v1, true);
    Assert.assertEquals(v1Value, resources.getResource(range2));

    // Should get lowest
    VersionRange range3 = new VersionRange(v0, v1, false);
    Assert.assertEquals(v0Value, resources.getResource(range3));

    // Should get lowest
    VersionRange range4 = new VersionRange(v0, v0.incrementMicro(), false);
    Assert.assertEquals(v0Value, resources.getResource(range4));

    // Should get middle
    VersionRange range5 = new VersionRange(v1, v1.incrementMicro(), false);
    Assert.assertEquals(v1Value, resources.getResource(range5));

    // Should get highest
    VersionRange range6 = new VersionRange(v2, v2.incrementMicro(), false);
    Assert.assertEquals(v2Value, resources.getResource(range6));

    // Should get highest from an infinite upper limit
    VersionRange range7 = new VersionRange(v2);
    Assert.assertEquals(v2Value, resources.getResource(range7));
    VersionRange range8 = new VersionRange(v1);
    Assert.assertEquals(v2Value, resources.getResource(range8));
    VersionRange range9 = new VersionRange(v0);
    Assert.assertEquals(v2Value, resources.getResource(range9));
 }

  @Test
  public void testOutsideRange() {
    VersionedResourceCollection<Integer> resources = VersionedResourceCollection.newVersionedResourceCollection();

    Version v0 = new Version(1, 0, 0);
    Integer v0Value = 12;

    Version v1 = new Version(1, 1, 0);
    Integer v1Value = 75;

    Version v2 = new Version(2, 0, 0);
    Integer v2Value = 13;

    resources.addResource(v0, v0Value);
    resources.addResource(v1, v1Value);
    resources.addResource(v2, v2Value);

    Version below = new Version(0, 1, 2);

    // Range totally outside below
    Assert.assertNull(resources.getResource(new VersionRange(below, below.incrementMinor(), true)));

    // Bump up against lowest in set
    Assert.assertNull(resources.getResource(new VersionRange(below, v0, false)));

    // Range totally outside above
    Version above = v2.incrementMinor();
    Assert.assertNull(resources.getResource(new VersionRange(above, above.incrementMinor(), true)));
  }
}
