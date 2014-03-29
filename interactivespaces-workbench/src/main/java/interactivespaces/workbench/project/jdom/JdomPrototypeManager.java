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

package interactivespaces.workbench.project.jdom;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import interactivespaces.SimpleInteractiveSpacesException;
import org.jdom.Element;

import java.util.List;
import java.util.Map;

/**
 * Manager for the project prototypes in a project group.
 *
 * @author Trevor Pering
 */
class JdomPrototypeManager {

  /**
   * Attribute name for inhterriting-from.
   */
  public static final String INHERITS_FROM_ATTRIBUTE = "inheritsFrom";

  /**
   * Attribute name for name of this prototype.
   */
  public static final String PROTOTYPE_NAME_ATTRIBUTE = "prototypeName";

  /**
   * Element name for a prototype element.
   */
  public static final String ELEMENT_NAME = "prototype";

  /**
   * Element name for a gorup of prototypes.
   */
  public static final String GROUP_ELEMENT_NAME = "prototypes";

  /**
   * Map of prototype names to generating elements.
   */
  private Map<String, Element> prototypeMap = Maps.newHashMap();

  /**
   * Add a prototype record for the given element.
   *
   * @param element
   *          element to add a prototype for
   */
  public void addPrototypeElement(Element element) {
    Preconditions.checkArgument(ELEMENT_NAME.equals(element.getName()),
        "Invalid prototype element name " + element.getName());
    String name = element.getAttributeValue(PROTOTYPE_NAME_ATTRIBUTE);
    Preconditions.checkNotNull(name, "Missing prototype name attribute from prototype");
    if (prototypeMap.put(name, element) != null) {
      throw new SimpleInteractiveSpacesException("Duplicate prototype name " + name);
    }
  }

  /**
   * Get a prototype chain for the given element.
   *
   * @param element
   *          element to generate a prototype chain for.
   *
   * @return prototype chain (ordered list of elements to process)
   */
  public List<Element> getPrototypeChain(Element element) {
    List<Element> prototypeChain = Lists.newArrayList();
    followPrototypeChain(element, prototypeChain);
    return prototypeChain;
  }

  /**
   * Recursively construct the prototype chain for the given element.
   *
   * @param element
   *          root element
   * @param prototypeChain
   *          prototype chain to augment
   */
  private void followPrototypeChain(Element element, List<Element> prototypeChain) {
    if (element == null) {
      return;
    }

    String prototypeNameAttributeValue = element.getAttributeValue(INHERITS_FROM_ATTRIBUTE);
    if (prototypeNameAttributeValue == null) {
      return;
    }

    String[] prototypeNameList = prototypeNameAttributeValue.split(",");
    for (String prototypeName : prototypeNameList) {
      Element parent = prototypeMap.get(prototypeName);
      Preconditions.checkNotNull(parent, "Could not find prototype named " + prototypeName);
      followPrototypeChain(parent, prototypeChain);
      prototypeChain.add(parent);
    }
  }
}
