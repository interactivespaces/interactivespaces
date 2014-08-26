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

package interactivespaces.workbench.project.library;

import interactivespaces.InteractiveSpacesException;
import interactivespaces.resource.VersionRange;
import interactivespaces.workbench.project.Project;
import interactivespaces.workbench.project.ProjectContext;
import interactivespaces.workbench.project.constituent.BaseProjectConstituentBuilder;
import interactivespaces.workbench.project.constituent.ProjectConstituent;
import interactivespaces.workbench.project.java.OsgiInfo;
import interactivespaces.workbench.project.java.OsgiInfo.ImportPackage;

import org.jdom.Element;
import org.jdom.Namespace;

import java.io.File;
import java.util.List;

/**
 * The project file constituent for library projects.
 *
 * @author Keith M. Hughes
 */
public class LibraryProjectConstituent implements ProjectConstituent {

  /**
   * Element type for a resource.
   */
  public static final String TYPE_NAME = "library";

  /**
   * XML entity name for an activity.
   */
  public static final String LIBRARY_ELEMENT = "library";

  /**
   * XML entity name giving the configuration for the activity.
   */
  public static final String OSGI_ELEMENT = "osgi";

  /**
   * XML entity name for the OSGi bundle activator.
   */
  public static final String OSGI_ACTIVATOR_ELEMENT = "activator";

  /**
   * XML entity name for the OSGi private packages.
   */
  public static final String OSGI_PRIVATE_PACKAGES_ELEMENT = "privatePackages";

  /**
   * XML entity name for the OSGi import packages.
   */
  public static final String OSGI_IMPORT_PACKAGES_ELEMENT = "importPackages";

  /**
   * XML entity name for the OSGi package elements in package collection
   * elements.
   */
  public static final String ELEMENT_NAME_OSGI_PACKAGES_PACKAGE = "package";

  /**
   * XML attribute name for whether an OSGi package is required or not.
   */
  public static final String ATTRIBUTE_NAME_OSGI_PACKAGES_PACKAGE_REQUIRED = "required";

  /**
   * XML attribute value for an OSGi package being required.
   */
  public static final String ATTRIBUTE_VALUE_OSGI_PACKAGES_PACKAGE_REQUIRED_VALUE_TRUE = "true";

  /**
   * XML attribute value for an OSGi package not being required.
   */
  public static final String ATTRIBUTE_VALUE_OSGI_PACKAGES_PACKAGE_REQUIRED_VALUE_FALSE = "false";

  /**
   * XML attribute name for the version of the required package.
   */
  public static final String ATTRIBUTE_NAME_OSGI_PACKAGES_PACKAGE_VERSION = "version";

  @Override
  public void processConstituent(Project project, File stagingDirectory, ProjectContext context) {
    // Nothing to do
  }

  @Override
  public String getSourceDirectory() throws InteractiveSpacesException {
    return null;
  }

  /**
   * Factory for building the constituent builder.
   *
   * @author Keith M. Hughes
   */
  public static class LibraryProjectBuilderFactory implements ProjectConstituentFactory {
    @Override
    public String getName() {
      return TYPE_NAME;
    }

    @Override
    public ProjectConstituentBuilder newBuilder() {
      return new LibraryProjectBuilder();
    }
  }

  /**
   * Builder class for creating new activity instances.
   */
  private static class LibraryProjectBuilder extends BaseProjectConstituentBuilder {

    @Override
    public ProjectConstituent buildConstituentFromElement(Namespace namespace, Element rootElement, Project project) {
      LibraryProject lproject = (LibraryProject) project;

      getOsgiInfo(namespace, rootElement, lproject.getOsgiInfo());

      return null;
    }

    /**
     * Get all OSGi info for an OSGi element if there is one.
     *
     * @param namespace
     *          XML namespace for elements
     * @param rootElement
     *          the root element for the library
     * @param osgiInfo
     *          the OSGi Info object to populate
     */
    @SuppressWarnings("unchecked")
    private void getOsgiInfo(Namespace namespace, Element rootElement, OsgiInfo osgiInfo) {

      Element osgiElement = rootElement.getChild(OSGI_ELEMENT, namespace);
      if (osgiElement != null) {
        osgiInfo.setActivatorClassname(osgiElement.getChildText(OSGI_ACTIVATOR_ELEMENT, namespace));

        extractPrivatePackages(namespace, osgiElement, osgiInfo);
        extractImportPackages(namespace, osgiElement, osgiInfo);
      }
    }

    /**
     * Get private package data.
     *
     * @param namespace
     *          XML namespace for elements
     * @param rootElement
     *          root element for the OSGi
     * @param osgiInfo
     *          the OSGi info object to store the packages in
     */
    private void extractPrivatePackages(Namespace namespace, Element rootElement, OsgiInfo osgiInfo) {
      List<String> packages = osgiInfo.getPrivatePackages();

      Element packagesElement = rootElement.getChild(OSGI_PRIVATE_PACKAGES_ELEMENT, namespace);
      if (packagesElement != null) {
        @SuppressWarnings("unchecked")
        List<Element> packageElements = packagesElement.getChildren(ELEMENT_NAME_OSGI_PACKAGES_PACKAGE, namespace);
        for (Element packageElement : packageElements) {
          String packageName = packageElement.getTextTrim();
          if (packageName != null && !packageName.isEmpty()) {
            packages.add(packageName);
          }
        }
      }
    }

    /**
     * Get import package data.
     *
     * the element name for the package section desired
     *
     * @param namespace
     *          XML namespace for elements
     * @param rootElement
     *          root element for the OSGi
     * @param osgiInfo
     *          the OSGi info object to store the packages in
     */
    private void extractImportPackages(Namespace namespace, Element rootElement, OsgiInfo osgiInfo) {
      List<ImportPackage> packages = osgiInfo.getImportPackages();

      Element packagesElement = rootElement.getChild(OSGI_IMPORT_PACKAGES_ELEMENT, namespace);
      if (packagesElement != null) {
        @SuppressWarnings("unchecked")
        List<Element> packageElements = packagesElement.getChildren(ELEMENT_NAME_OSGI_PACKAGES_PACKAGE, namespace);
        for (Element packageElement : packageElements) {
          String packageName = packageElement.getTextTrim();
          if (packageName != null && !packageName.isEmpty()) {
            String requiredAttribute =
                packageElement.getAttributeValue(ATTRIBUTE_NAME_OSGI_PACKAGES_PACKAGE_REQUIRED,
                    ATTRIBUTE_VALUE_OSGI_PACKAGES_PACKAGE_REQUIRED_VALUE_TRUE);

            VersionRange versionRange = null;
            String versionAttribute = packageElement.getAttributeValue(ATTRIBUTE_NAME_OSGI_PACKAGES_PACKAGE_VERSION);
            if (versionAttribute != null) {
              versionRange = VersionRange.parseVersionRange(versionAttribute);
            }

            packages.add(new ImportPackage(packageName, ATTRIBUTE_VALUE_OSGI_PACKAGES_PACKAGE_REQUIRED_VALUE_TRUE
                .equals(requiredAttribute), versionRange));
          }
        }
      }
    }
  }
}
