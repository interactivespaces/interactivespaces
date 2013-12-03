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
 * Tests for the {@link Transform2} class.
 *
 * @author Keith M. Hughes
 */
public class Transform2Tests {
  @Test
  public void testTranslate() {
    Transform2 transform = new Transform2();

    double tx = 10.0;
    double ty = -12.5;
    transform.translate(tx, ty);
    transform.rotate(0.0);

    Vector2 transformPoint = new Vector2(23.6, 27.2);
    Vector2 expected = new Vector2(transformPoint.getV0() + tx, transformPoint.getV1() + ty);

    transform.transformSelf(transformPoint);
    Assert.assertTrue(expected.equal(transformPoint, 0.001));
  }
}
