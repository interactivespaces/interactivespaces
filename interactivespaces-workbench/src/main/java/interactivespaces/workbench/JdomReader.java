package interactivespaces.workbench;

import org.jdom.Element;

/**
 */
public interface JdomReader<T> {

  T processElement(Element rootElement);

  void handleResult(T result);
}
