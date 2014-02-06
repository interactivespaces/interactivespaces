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

package interactivespaces.master.api.internal;

import interactivespaces.expression.ExpressionFactory;
import interactivespaces.system.InteractiveSpacesEnvironment;

/**
 * A base manager with support for Master API managers.
 *
 * @author Keith M. Hughes
 */
public class BaseMasterApiManager {

  /**
   * A factory for expressions.
   */
  protected ExpressionFactory expressionFactory;

  /**
   * The space environment to use.
   */
  protected InteractiveSpacesEnvironment spaceEnvironment;

  /**
   * Set the expression factory for this manager.
   *
   * @param expressionFactory
   *          the factory to use
   */
  public void setExpressionFactory(ExpressionFactory expressionFactory) {
    this.expressionFactory = expressionFactory;
  }

  /**
   * Set the space environment to use.
   *
   * @param spaceEnvironment
   *          the space environment to use
   */
  public void setSpaceEnvironment(InteractiveSpacesEnvironment spaceEnvironment) {
    this.spaceEnvironment = spaceEnvironment;
  }
}
