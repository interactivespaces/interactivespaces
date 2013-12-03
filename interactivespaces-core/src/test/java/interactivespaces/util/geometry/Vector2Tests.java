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

package interactivespaces.util.geometry;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for the {@link Vector2} class.
 *
 * @author Keith M. Hughes
 */
public class Vector2Tests {

  /**
   * Test finding the nearest point on a line to a target point.
   */
  @Test
  public void testNearestPoint() {
    Vector2 answer1 =
        Vector2.nearestPointOnLine(new Vector2(1.0, 1.0), new Vector2(1.0, -1.0), new Vector2(0.0, 0.0), new Vector2());
    Assert.assertTrue(new Vector2(1.0, 0.0).equal(answer1, 0.001));

    Vector2 answer2 =
        Vector2.nearestPointOnLine(new Vector2(-1.0, 1.0), new Vector2(1.0, 1.0), new Vector2(0.0, 0.0), new Vector2());
    Assert.assertTrue(new Vector2(0.0, 1.0).equal(answer2, 0.001));

    try {
      Vector2 linePoint = new Vector2(1.0, 1.0);
      Vector2.nearestPointOnLine(linePoint, linePoint, new Vector2(0.0, 0.0), new Vector2());
      Assert.fail();
    } catch (Exception e) {
    }
  }

  /**
   * Test calculating the intersection of two lines.
   */
  @Test
  public void testLineIntersection() {
    Vector2 linePoint = new Vector2(1.0, 1.0);
    Vector2 answer1 =
        Vector2.intersectLines(new Vector2(-1.0, -2.0), new Vector2(2.0, 4.0), new Vector2(2.0, 0.0), new Vector2(0.0,
            4.0), new Vector2());

    Assert.assertTrue(new Vector2(1.0, 2.0).equal(answer1, 0.001));

    try {
      Vector2.intersectLines(new Vector2(-1.0, -2.0), new Vector2(2.0, 4.0), new Vector2(-2.0, 0.0), new Vector2(0.0,
          4.0), new Vector2());
      Assert.fail();
    } catch (Exception e) {
    }
  }
}
