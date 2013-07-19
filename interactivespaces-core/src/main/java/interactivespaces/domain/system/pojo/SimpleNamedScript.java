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

package interactivespaces.domain.system.pojo;

import interactivespaces.domain.pojo.SimpleObject;
import interactivespaces.domain.system.NamedScript;

/**
 * A POJO {@link NamedScript}
 *
 * @author Keith M. Hughes
 */
public class SimpleNamedScript extends SimpleObject implements NamedScript {

  private static final long serialVersionUID = -8506899266993754281L;

  /**
   * The name of the script.
   */
  private String name;

  /**
   * The description of the script.
   */
  private String description;

  /**
   * The language of the script.
   */
  private String language;

  /**
   * The content of the script.
   */
  private String content;

  /**
   * The schedule of the script.
   */
  private String schedule;

  /**
   * {@code true} if the script is scheduled.
   */
  private boolean scheduled;

  @Override
  public String getName() {
    return name;
  }

  @Override
  public void setName(String name) {
    this.name = name;
  }

  @Override
  public String getDescription() {
    return description;
  }

  @Override
  public void setDescription(String description) {
    this.description = description;
  }

  @Override
  public String getContent() {
    return content;
  }

  @Override
  public void setContent(String content) {
    this.content = content;
  }

  @Override
  public String getLanguage() {
    return language;
  }

  @Override
  public void setLanguage(String language) {
    this.language = language;
  }

  @Override
  public String getSchedule() {
    return schedule;
  }

  @Override
  public void setSchedule(String schedule) {
    this.schedule = schedule;
  }

  @Override
  public boolean getScheduled() {
    return scheduled;
  }

  @Override
  public void setScheduled(boolean scheduled) {
    this.scheduled = scheduled;
  }
}
