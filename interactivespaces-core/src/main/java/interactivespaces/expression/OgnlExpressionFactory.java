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

package interactivespaces.expression;

import java.util.Map;

import org.apache.commons.ognl.ClassResolver;
import org.apache.commons.ognl.OgnlContext;

/**
 * An {@link Expression Factory} which uses OGNL.
 *
 * @author Keith M. Hughes
 */
public class OgnlExpressionFactory implements ExpressionFactory {

  // This needs to go away, but is needed right now for classloader
  // until I remind myself of OSGi headers
  javassist.ClassPool foo;

  @Override
  public FilterExpression getFilterExpression(String expression) {
    if (expression == null || expression.trim().isEmpty()) {
      return new AlwaysPassFilterExpression();
    } else {

      return new OgnlFilterExpression(newOgnlContext(), expression);
    }
  }

  /**
   * Get a new OGNL context.
   *
   * @return
   */
  private OgnlContext newOgnlContext() {
    return new OgnlContext(new MyClassResolver(), OgnlContext.DEFAULT_TYPE_CONVERTER,
        OgnlContext.DEFAULT_MEMBER_ACCESS);

  }

  /**
   * A class resolver happy to work with OSGi
   *
   * @author Keith M. Hughes
   */
  private static class MyClassResolver implements ClassResolver {
    @Override
    public Class<?> classForName(String className, Map<String, Object> context)
        throws ClassNotFoundException {
      return OgnlExpressionFactory.class.getClassLoader().loadClass(className);
    }
  }
}
