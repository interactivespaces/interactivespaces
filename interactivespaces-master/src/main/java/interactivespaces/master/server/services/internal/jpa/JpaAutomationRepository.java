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

package interactivespaces.master.server.services.internal.jpa;

import interactivespaces.domain.system.NamedScript;
import interactivespaces.master.server.services.AutomationRepository;
import interactivespaces.master.server.services.internal.jpa.domain.JpaNamedScript;

import org.springframework.orm.jpa.JpaTemplate;

import java.util.List;

/**
 * A JPA implementation of {@link AutomationRepository}.
 *
 * @author Keith M. Hughes
 */
public class JpaAutomationRepository implements AutomationRepository {

  /**
   * The Spring JPA template.
   */
  private JpaTemplate template;

  @Override
  public NamedScript newNamedScript() {
    return new JpaNamedScript();
  }

  @Override
  public NamedScript newNamedScript(NamedScript template) {
    return copyNamedScriptTemplate(template);
  }

  /**
   * Create a new script and fill in the script from the template.
   *
   * @param template
   *          the template with the values for the new script
   *
   * @return a new script with the specified values
   */
  private NamedScript copyNamedScriptTemplate(NamedScript template) {
    NamedScript script = new JpaNamedScript();
    script.setName(template.getName());
    script.setDescription(template.getDescription());
    script.setContent(template.getContent());
    script.setLanguage(template.getLanguage());
    script.setSchedule(template.getSchedule());
    script.setScheduled(template.getScheduled());

    return script;
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<NamedScript> getAllNamedScripts() {
    return template.findByNamedQuery("namedScriptAll");
  }

  @Override
  public NamedScript getNamedScriptById(String id) {
    return template.find(JpaNamedScript.class, id);
  }

  @Override
  public NamedScript saveNamedScript(NamedScript script) {
    if (script.getId() != null) {
      return template.merge(script);
    } else {
      template.persist(script);

      return script;
    }
  }

  @Override
  public void deleteNamedScript(NamedScript script) {
    template.remove(script);
  }

  /**
   * @param template
   *          the template to set
   */
  public void setTemplate(JpaTemplate template) {
    this.template = template;
  }
}
