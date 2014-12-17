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

package interactivespaces.configuration;

import interactivespaces.evaluation.ExpressionEvaluator;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

/**
 * A {@link Configuration} based on Java properties files.
 *
 * @author Keith M. Hughes
 */
public class PropertiesConfiguration extends BaseConfiguration {

  /**
   * The properties file containing the app properties.
   */
  private Properties values;

  /**
   * @param properties
   *          the properties object to us
   * @param expressionEvaluator
   *          the expression evaluator for this configuration.
   */
  public PropertiesConfiguration(Properties properties, ExpressionEvaluator expressionEvaluator) {
    super(expressionEvaluator);

    this.values = properties;
  }

  @Override
  public boolean containsPropertyLocally(String property) {
    return values.containsKey(property);
  }

  @Override
  public String findValueLocally(String property) {
    return (String) values.get(property);
  }

  @Override
  public void setValue(String property, String value) {
    values.put(property, value);
  }

  @Override
  public void clear() {
    values.clear();
  }

  @Override
  public void addCollapsedEntries(Map<String, String> map) {
    Configuration parent = getParent();
    if (parent != null) {
      parent.addCollapsedEntries(map);
    }

    for (Entry<Object, Object> entry : values.entrySet()) {
      map.put(entry.getKey().toString(), entry.getValue().toString());
    }
  }
}
