package interactivespaces.workbench;

import com.google.common.io.Closeables;
import interactivespaces.InteractiveSpacesException;
import interactivespaces.SimpleInteractiveSpacesException;
import interactivespaces.workbench.project.TemplateVar;
import org.apache.commons.logging.Log;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;

/**
 */
public abstract class JdomReader {

  /**
   * Log for errors.
   */
  protected final Log log;

  /**
   * {@code true} if read was successful.
   */
  protected boolean failure;

  /**
   * Interactive spaces workbench used by this reader.
   */
  private InteractiveSpacesWorkbench workbench;

  /**
   * Create a new jdom reader.
   *
   * @param log
   *          logger for error messages
   */
  public JdomReader(Log log) {
    this.log = log;
  }

  /**
   * Get the root element for a given input file.
   *
   * @param projectFile
   *          input project file
   *
   * @return top-level element
   */
  protected static Element getRootElement(File projectFile) {
    Document doc;
    FileInputStream inputStream = null;
    try {
      inputStream = new FileInputStream(projectFile);
      SAXBuilder builder = new SAXBuilder();
      doc = builder.build(inputStream);
    } catch (Exception e) {
      throw new InteractiveSpacesException(String.format("Exception while processing project file %s",
          projectFile.getAbsolutePath()), e);
    } finally {
      Closeables.closeQuietly(inputStream);
    }
    return doc.getRootElement();
  }

  /**
   * Get a template variable from a given element.
   *
   * @param element
   *          element to parse
   *
   * @return template variable for the element
   */
  protected TemplateVar getTemplateVarFromElement(Element element) {
    String name = getRequiredAttributeValue(element, TemplateVar.NAME_ATTRIBUTE_NAME);
    String value = getRequiredAttributeValue(element, TemplateVar.VALUE_ATTRIBUTE_NAME);
    return new TemplateVar(name, value);
  }

  /**
   * Return the trimmed text of a child element.
   *
   * @param element
   *          container element
   * @param key
   *          variable key
   *
   * @return trimmed element text
   *
   * @throws SimpleInteractiveSpacesException
   *           if the child element is not provided
   */
  protected String getChildTextTrimmed(Element element, String key) throws SimpleInteractiveSpacesException {
    try {
      return element.getChildTextTrim(key);
    } catch (Exception e) {
      throw new SimpleInteractiveSpacesException("Looking for text of child: " + key, e);
    }
  }

  /**
   * Return the trimmed text of a child element.
   *
   * @param element
   *          container element
   * @param key
   *          variable key
   * @param fallback
   *          fallback when not defined
   *
   * @return trimmed element text
   */
  protected String getChildTextTrimmed(Element element, String key, String fallback) {
    String value = getChildTextTrimmed(element, key);
    return (value == null || (!value.isEmpty() && fallback != null)) ? fallback : value;
  }

  /**
   * Return a required element attribute.
   *
   * @param element
   *          container element
   * @param key
   *          attribute key
   *
   * @return attribute value or {@code null} if none found
   */
  protected String getRequiredAttributeValue(Element element, String key) {
    String value = getAttributeValue(element, key);
    if (value == null) {
      log.error("Missing required attribute " + key);
    }
    return value;
  }

  /**
   * Return a given element attribute, using the default value if not found.
   *
   * @param element
   *          container element
   * @param key
   *          attribute key
   * @param fallback
   *          default attribute value
   *
   * @return attribute value
   */
  protected String getAttributeValue(Element element, String key, String fallback) {
    return element.getAttributeValue(key, fallback);
  }

  /**
   * Return a given element attribute.
   *
   * @param element
   *          container element
   * @param key
   *          attribute key
   *
   * @return attribute value, or {@code null} if not found.
   */
  protected String getAttributeValue(Element element, String key) {
    return getAttributeValue(element, key, null);
  }

  /**
   * Get the children of the given element.
   *
   * @param element
   *          element to return the children of
   *
   * @return element children
   */
  @SuppressWarnings("unchecked")
  protected List<Element> getChildren(Element element) {
    return element.getChildren();
  }

  /**
   * @return workbench used by this reader
   */
  public InteractiveSpacesWorkbench getWorkbench() {
    return workbench;
  }

  /**
   * Set the workbench in use.
   *
   * @param workbench
   *          workbench to use
   */
  public void setWorkbench(InteractiveSpacesWorkbench workbench) {
    this.workbench = workbench;
  }

  /**
   * An error has occurred.
   *
   * @param error
   *          text of the error message
   */
  protected void addError(String error) {
    log.error(error);
    failure = true;
  }
}
