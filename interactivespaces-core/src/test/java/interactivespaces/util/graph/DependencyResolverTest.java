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

package interactivespaces.util.graph;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

/**
 * Test the {@link DependencyResolver} class.
 *
 * @author Keith M. Hughes
 */
public class DependencyResolverTest {
  private DependencyResolver<String,String> resolver;

  @Before
  public void setup() {
    resolver = new DependencyResolver<String,String>();
  }

  @Test
  public void testTwoNodes() {
    resolver.addNode("a", "a-data");
    resolver.addNode("b", "b-data");
    resolver.addNodeDependencies("a", "b");
    resolver.resolve();

    assertComesAfter(resolver.getOrdering(), "a-data", "b-data");
  }

  @Test
  public void testThreeNodesLinear() {
    resolver.addNode("a", "a-data");
    resolver.addNodeDependencies("a", "b");
    resolver.addNode("b", "b-data");
    resolver.addNodeDependencies("b", "c");
    resolver.addNode("c", "c-data");
    resolver.resolve();

    List<String> ordering = resolver.getOrdering();
    assertComesAfter(ordering, "b-data", "c-data");
    assertComesAfter(ordering, "a-data", "b-data");
  }

  /**
   * a depends on c b depends on c
   *
   * No specification between a and b
   */
  @Test
  public void testTwoNodesDependOnOne() {
    resolver.addNode("a", "a-data");
    resolver.addNodeDependencies("a", "c");
    resolver.addNode("b", "b-data");
    resolver.addNodeDependencies("b", "c");
    resolver.addNode("c", "c-data");

    resolver.resolve();

    List<String> ordering = resolver.getOrdering();
    assertComesAfter(ordering, "b-data", "c-data");
    assertComesAfter(ordering, "a-data", "c-data");
  }

  /**
   * a depends on b a depends on c c depends on d, e, and f
   *
   * No specification between a and b
   */
  @Test
  public void testComplicated() {
    resolver.addNode("a", "a-data");
    resolver.addNodeDependencies("a", "b", "c");
    resolver.addNode("b", "b-data");
    resolver.addNode("c", "c-data");
    resolver.addNodeDependencies("c", "d", "e", "f");
    resolver.addNode("d", "d-data");
    resolver.addNode("e", "e-data");
    resolver.addNode("f", "f-data");

    resolver.resolve();

    List<String> ordering = resolver.getOrdering();
    assertComesAfter(ordering, "a-data", "b-data", "c-data", "d-data", "e-data", "f-data");
    assertComesAfter(ordering, "c-data", "d-data", "e-data", "f-data");
  }

  /**
   * a depends on b b depends on a
   */
  @Test
  public void testTwoNodesCyclic() {
    resolver.addNode("a", "a-data");
    resolver.addNodeDependencies("a", "b");
    resolver.addNode("b", "b-data");
    resolver.addNodeDependencies("b", "a");

    try {
      resolver.resolve();

      fail();
    } catch (Exception e) {
      // Exception thrown on cycle detection.
    }
  }

  /**
   * Check that the before item comes before the after item in the ordering.
   *
   * @param ordering
   *          the ordering of elements
   * @param after
   *          this item should come after everything in the befores list
   * @param befores
   *          all of these items should appear before the after item
   *
   */
  private void assertComesAfter(List<String> ordering, String after, String... befores) {
    int posAfter = ordering.indexOf(after);
    assertFalse(-1 == posAfter);
    assertTrue(posAfter == ordering.lastIndexOf(after));

    for (String before : befores) {
      int posBefore = ordering.indexOf(before);
      assertFalse(-1 == posBefore);
      assertTrue(posBefore == ordering.lastIndexOf(before));

      assertTrue(
          String.format("%s(%d) should be after %s(%d)", after, posAfter, before, posBefore),
          posBefore < posAfter);
    }
  }
}
