package interactivespaces.workbench.confederate;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.jdom.Element;

import java.util.List;
import java.util.Map;

/**
 */
public class PrototypeManager {

  Map<String, Element> prototypeMap = Maps.newHashMap();

  public static final String INHERRITS_FROM_ATTRIBUTE = "inherritsFrom";

  public static final String PROTOTYPE_NAME_ATTRIBUTE = "prototypeName";

  public static final String PROTOTYPE_ELEMENT_NAME = "prototype";

  public void addPrototypeElement(Element element) {
    String name = element.getAttributeValue(PROTOTYPE_NAME_ATTRIBUTE);
    Preconditions.checkNotNull(name, "Missing prototype name attribute from prototype");
    prototypeMap.put(name, element);
  }

  public List<Element> getPrototypeChain(Element element) {
    List<Element> prototypeChain = Lists.newArrayList();

    while (element != null && element.getAttribute(INHERRITS_FROM_ATTRIBUTE) != null) {
      String prototypeName = element.getAttributeValue(INHERRITS_FROM_ATTRIBUTE);
      Element parent = prototypeMap.get(prototypeName);
      Preconditions.checkNotNull(parent, "Could not find prototype named " + prototypeName);
      prototypeChain.add(parent);
      element = parent;
    }
    return prototypeChain;
  }
}
