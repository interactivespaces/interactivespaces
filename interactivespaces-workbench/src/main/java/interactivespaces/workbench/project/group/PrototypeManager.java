package interactivespaces.workbench.project.group;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import interactivespaces.SimpleInteractiveSpacesException;
import org.jdom.Element;

import java.util.List;
import java.util.Map;

/**
 */
public class PrototypeManager {

  private Map<String, Element> prototypeMap = Maps.newHashMap();

  public static final String INHERITS_FROM_ATTRIBUTE = "inheritsFrom";

  public static final String PROTOTYPE_NAME_ATTRIBUTE = "prototypeName";

  public static final String ELEMENT_NAME = "prototype";

  public static final String GROUP_ELEMENT_NAME = "prototypes";

  public void addPrototypeElement(Element element) {
    Preconditions.checkArgument(ELEMENT_NAME.equals(element.getName()),
        "Invalid prototype element name " + element.getName());
    String name = element.getAttributeValue(PROTOTYPE_NAME_ATTRIBUTE);
    Preconditions.checkNotNull(name, "Missing prototype name attribute from prototype");
    if (prototypeMap.containsKey(name)) {
      throw new SimpleInteractiveSpacesException("Duplicate prototype name " + name);
    }
    prototypeMap.put(name, element);
  }

  public List<Element> getPrototypeChain(Element element) {
    List<Element> prototypeChain = Lists.newArrayList();
    followPrototypeChain(element, prototypeChain);
    return prototypeChain;
  }

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
