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

package interactivespaces.workbench.project.jdom;

import interactivespaces.InteractiveSpacesException;
import interactivespaces.SimpleInteractiveSpacesException;
import interactivespaces.service.web.HttpConstants;
import interactivespaces.util.io.FileSupport;
import interactivespaces.util.io.FileSupportImpl;
import interactivespaces.workbench.InteractiveSpacesWorkbench;
import interactivespaces.workbench.project.Project;
import interactivespaces.workbench.project.constituent.ProjectConstituent;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.apache.commons.logging.Log;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;
import org.jdom2.located.LocatedJDOMFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.ext.EntityResolver2;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.List;
import java.util.Map;

/**
 * Base-class for facilitating reading XML documents using jdom.
 *
 * @author Trevor pering
 */
public class JdomReader {

  /**
   * XML parser feature for enabling xinclude.
   */
  private static final String XML_PARSER_FEATURE_XINCLUDE = "http://apache.org/xml/features/xinclude";

  /**
   * Project definition file element name for templates.
   */
  public static final String PROJECT_ELEMENT_NAME_TEMPLATES = "templateComponents";

  /**
   * Map of resource types to resource builders.
   */
  private final Map<String, ProjectConstituent.ProjectConstituentBuilderFactory> projectConstituentFactoryMap = Maps
      .newHashMap();

  /**
   * {@code true} if read was successful.
   */
  protected boolean failure;

  /**
   * Interactive spaces workbench used by this reader.
   */
  private final InteractiveSpacesWorkbench workbench;

  /**
   * Prototype manager to use when reading/constructing projects, may be {@code null} if none available.
   */
  private JdomPrototypeProcessor jdomPrototypeProcessor;

  /**
   * The file support to use.
   */
  private FileSupport fileSupport = FileSupportImpl.INSTANCE;

  /**
   * Create a new jdom reader.
   *
   * @param workbench
   *          containing workbench instance
   */
  public JdomReader(InteractiveSpacesWorkbench workbench) {
    this.workbench = workbench;
  }

  /**
   * Add a constituent type to the factory.
   *
   * @param constituentFactory
   *          factory to add
   */
  protected void addConstituentType(ProjectConstituent.ProjectConstituentBuilderFactory constituentFactory) {
    projectConstituentFactoryMap.put(constituentFactory.getName(), constituentFactory);
  }

  /**
   * Get the root element for a given input file.
   *
   * @param inputFile
   *          input project file
   *
   * @return top-level element
   */
  Element getRootElement(File inputFile) {
    Document doc;
    try {
      SAXBuilder builder = new SAXBuilder();
      builder.setJDOMFactory(new LocatedJDOMFactory());
      builder.setFeature(XML_PARSER_FEATURE_XINCLUDE, true);
      builder.setEntityResolver(new MyEntityResolver());
      doc = builder.build(inputFile);
    } catch (Exception e) {
      throw new InteractiveSpacesException(
          String.format("Exception while processing %s", inputFile.getAbsolutePath()), e);
    }

    return doc.getRootElement();
  }

  /**
   * Return the trimmed text of a child element.
   *
   * @param element
   *          container element
   * @param namespace
   *          namespace for the key element
   * @param key
   *          variable key
   *
   * @return trimmed element text
   *
   * @throws InteractiveSpacesException
   *           if the child element is not provided
   */
  protected String getChildTextTrimmed(Element element, Namespace namespace, String key)
      throws InteractiveSpacesException {
    try {
      return element.getChildTextTrim(key, namespace);
    } catch (Exception e) {
      throw new SimpleInteractiveSpacesException("Looking for text of child: " + key, e);
    }
  }

  /**
   * Return the trimmed text of a child element.
   *
   * @param element
   *          container element
   * @param namespace
   *          namespace for the key element
   * @param key
   *          variable key
   * @param fallback
   *          fallback when not defined
   *
   * @return trimmed element text
   */
  protected String getChildTextTrimmed(Element element, Namespace namespace, String key, String fallback) {
    String value = getChildTextTrimmed(element, namespace, key);
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
      getLog().error("Missing required attribute " + key);
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
   * An error has occurred.
   *
   * @param error
   *          text of the error message
   */
  protected void addError(String error) {
    getLog().error(error);
    failure = true;
  }

  /**
   * Logger to use while reading.
   *
   * @return logger
   */
  public Log getLog() {
    return workbench.getLog();
  }

  /**
   * @return the prototype processor
   */
  public JdomPrototypeProcessor getJdomPrototypeProcessor() {
    return jdomPrototypeProcessor;
  }

  /**
   * Set the prototype processor.
   *
   * @param jdomPrototypeProcessor
   *          prototype processor
   */
  public void setJdomPrototypeProcessor(JdomPrototypeProcessor jdomPrototypeProcessor) {
    this.jdomPrototypeProcessor = jdomPrototypeProcessor;
  }

  /**
   * Get the map of project constituent factories.
   *
   * @return the constituent factory map
   */
  public Map<String, ProjectConstituent.ProjectConstituentBuilderFactory> getProjectConstituentFactoryMap() {
    return projectConstituentFactoryMap;
  }

  /**
   * Add the constituents from a container in the document the document.
   *
   * @param namespace
   *          XML namespace for elements
   * @param containerElement
   *          root element of the XML DOM containing the project data
   * @param project
   *          the project being read
   *
   * @return the constituents for the project
   */
  protected List<ProjectConstituent> getContainerConstituents(Namespace namespace, Element containerElement,
      Project project) {
    if (containerElement == null) {
      return null;
    }

    List<ProjectConstituent> constituents = Lists.newArrayList();
    List<Element> childElements = getChildren(containerElement);

    for (Element childElement : childElements) {
      getConstituent(namespace, childElement, project, constituents);
    }

    return constituents;
  }

  /**
   * Add the constituents from a container in the document the document.
   *
   * @param namespace
   *          XML namespace for elements
   * @param constituentElement
   *          XML element containing the constituent data
   * @param project
   *          the project being read
   *
   * @return the constituents for the element
   */
  protected List<ProjectConstituent> getIndividualConstituent(Namespace namespace, Element constituentElement,
      Project project) {
    if (constituentElement == null) {
      return null;
    }

    List<ProjectConstituent> constituents = Lists.newArrayList();

    getConstituent(namespace, constituentElement, project, constituents);

    return constituents;
  }

  /**
   * Get the constituent from the element which describes it..
   *
   * @param namespace
   *          XML namespace for elements
   * @param constituentElement
   *          the element containing the constituent
   * @param project
   *          the project being built
   * @param constituents
   *          the list of constituents currently being extracted
   */
  private void getConstituent(Namespace namespace, Element constituentElement, Project project,
      List<ProjectConstituent> constituents) {
    String type = constituentElement.getName();
    ProjectConstituent.ProjectConstituentBuilderFactory factory = getProjectConstituentFactoryMap().get(type);
    if (factory != null) {
      ProjectConstituent.ProjectConstituentBuilder projectConstituentBuilder = factory.newBuilder();
      projectConstituentBuilder.setLog(getLog());
      ProjectConstituent constituent =
          projectConstituentBuilder.buildConstituentFromElement(namespace, constituentElement, project);
      if (constituent != null) {
        constituents.add(constituent);
      }
    } else {
      addError(String.format("Unknown resource type '%s'", type));
    }
  }

  /**
   * An EntityResolver that evaluates system IDs using an Interactive Spaces configuration evaluation.
   *
   * @author Keith M. Hughes
   */
  private class MyEntityResolver implements EntityResolver2 {

    @Override
    public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
      // JDOM2 uses a SAX2 parser so this method should never be called.
      throw SimpleInteractiveSpacesException.newFormattedException("Unsupported call to %s.resolveEntity(%s, %s)",
          getClass().getCanonicalName(), publicId, systemId);
    }

    @Override
    public InputSource getExternalSubset(String name, String baseUri) throws SAXException, IOException {
      throw SimpleInteractiveSpacesException.newFormattedException("Unsupported call to %s.getExternalSubset(%s, %s)",
          getClass().getCanonicalName(), name, baseUri);
    }

    @Override
    public InputSource resolveEntity(String name, String publicId, String baseUri, String systemId)
        throws SAXException, IOException {
      try {
        String decodedSystemId =
            workbench.getSpaceEnvironment().getSystemConfiguration()
                .evaluate(URLDecoder.decode(systemId, Charsets.UTF_8.name()));

        File resolvedFile = null;
        if (!decodedSystemId.startsWith(HttpConstants.URL_PATH_COMPONENT_SEPARATOR)) {
          resolvedFile =
              fileSupport.newFile(fileSupport.getParentFile(fileSupport.newFile(new URI(baseUri))), decodedSystemId);
        } else {
          resolvedFile = fileSupport.newFile(decodedSystemId);
        }

        if (!decodedSystemId.equals(systemId)) {
          getLog().info(String.format("XML entity %s (possibly xinclude) resolved to %s",
              systemId, resolvedFile.getAbsolutePath()));
        }
        return new InputSource(resolvedFile.toURI().toString());
      } catch (URISyntaxException e) {
        throw InteractiveSpacesException.newFormattedException(e, "Could not resolve XML entity");
      }
    }
  }
}
