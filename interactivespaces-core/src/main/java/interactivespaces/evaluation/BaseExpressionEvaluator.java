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

package interactivespaces.evaluation;

/**
 * An {@link ExpressionEvaluator} that does simple evaluations of strings.
 *
 * @author Keith M. Hughes
 */
public abstract class BaseExpressionEvaluator implements ExpressionEvaluator {

  /**
   * The environment for evaluating the expressions in.
   */
  protected EvaluationEnvironment environment;

  @Override
  public String evaluateStringExpression(String initial) {
    // I don't know if the short-circuit is needed, but will leave for now
    // and check by profiling
    // later.
    int exprPos = initial.indexOf("${");
    if (exprPos == -1) {
      return initial;
    } else {
      // Store the first part of the string that has no variables.
      StringBuffer buffer = new StringBuffer();

      // For now there will never be a ${ or } in the middle of an
      // expression.
      int endExpr = 0;
      do {
        buffer.append(initial.substring(endExpr, exprPos));
        exprPos += 2;

        endExpr = initial.indexOf("}", endExpr);
        if (endExpr == -1) {
          throw new EvaluationInteractiveSpacesException(String.format(
              "Expression in string doesn't end with }: %s", initial.substring(exprPos)));
        }

        String expression = initial.substring(exprPos, endExpr);
        Object value = evaluateExpression(expression);
        if (value == null || value.equals(expression))
          buffer.append("${").append(expression).append("}");
        else
          buffer.append(value.toString());

        endExpr++;
        exprPos = initial.indexOf("${", endExpr);
      } while (exprPos != -1);

      buffer.append(initial.substring(endExpr));

      return buffer.toString();
    }
  }

  @Override
  public void setEvaluationEnvironment(EvaluationEnvironment environment) {
    this.environment = environment;
  }

}
