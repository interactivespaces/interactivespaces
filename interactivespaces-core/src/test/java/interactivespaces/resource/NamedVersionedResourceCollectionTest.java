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

package interactivespaces.resource;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test {@link NamedVersionedResourceCollection}.
 *
 * @author Keith M. Hughes
 */
public class NamedVersionedResourceCollectionTest {
  private NamedVersionedResourceCollection<Integer> collection;

  @Before
  public void setup() {
    collection = NamedVersionedResourceCollection.newNamedVersionedResourceCollection();
  }

  /**
   * Check accessing an empty collection
   */
  @Test
  public void testWhenEmpty() {
    Assert.assertNull(collection.getHighestResource("foo"));
    Assert.assertNull(collection.getResource("foo", new Version(1, 2, 3)));
    Assert.assertNull(collection.getResource("foo", VersionRange.parseVersionRange("[1.2,1.3)")));
  }

  /**
   * Check finding a resource by various methods
   */
  @Test
  public void testFindingResource() {
    Version v0 = new Version(2, 1, 0);
    Integer v0Resource = 12;
    collection.addResource("foo", v0, v0Resource);

    Assert.assertEquals(v0Resource, collection.getHighestResource("foo"));
    Assert.assertEquals(v0Resource, collection.getResource("foo", v0));
    Assert.assertEquals(v0Resource, collection.getResource("foo", new VersionRange(v0, v0.incrementMajor(), true)));

    Assert.assertNull(collection.getResource("foo", new VersionRange(v0.incrementMinor(), v0.incrementMajor(), true)));
  }

  /**
   * Check finding a resource by range when it is in the middle
   */
  @Test
  public void testFindingResourceWithMultiple() {
    Version v0 = new Version(2, 1, 0);
    Integer v0Resource = 12;
    collection.addResource("foo", v0, v0Resource);

    Version v1 = new Version(3, 0, 0);
    Integer v1Resource = 75;
    collection.addResource("foo", v1, v1Resource);

    Assert.assertEquals(v1Resource, collection.getHighestResource("foo"));
    Assert.assertEquals(v0Resource, collection.getResource("foo", v0));
    Assert.assertEquals(v1Resource, collection.getResource("foo", v1));
    Assert.assertEquals(v0Resource, collection.getResource("foo", new VersionRange(v0, v0.incrementMinor(), true)));
  }

}
