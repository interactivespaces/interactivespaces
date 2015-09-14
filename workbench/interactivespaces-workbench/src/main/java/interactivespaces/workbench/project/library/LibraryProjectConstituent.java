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

import interactivespaces.resource.VersionRange;
import interactivespaces.workbench.project.Project;
import interactivespaces.workbench.project.constituent.BaseProjectConstituent;
import interactivespaces.workbench.project.constituent.BaseProjectConstituentBuilder;
import interactivespaces.workbench.project.constituent.ProjectConstituent;
import interactivespaces.workbench.project.java.ContainerInfo;
import interactivespaces.workbench.project.java.ContainerInfo.ImportPackage;

import org.jdom2.Element;
import org.jdom2.Namespace;

import java.util.List;

/**
 * The project file constituent for library projects.
 *
 * @author Keith M. Hughes
 */
public class LibraryProjectConstituent extends BaseProjectConstituent {

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
  public static final String CONTAINER_ELEMENT = "container";

  /**
   * XML entity name for the container bundle activator.
   */
  public static final String CONTAINER_ACTIVATOR_ELEMENT = "activator";

  /**
   * XML entity name for the container private packages.
   */
  public static final String CONTAINER_PRIVATE_PACKAGES_ELEMENT = "privatePackages";

  /**
   * XML entity name for the container import packages.
   */
  public static final String CONTAINER_IMPORT_PACKAGES_ELEMENT = "importPackages";

  /**
   * XML entity name for the container package elements in package collection elements.
   */
  public static final String ELEMENT_NAME_CONTAINER_PACKAGES_PACKAGE = "package";

  /**
   * XML attribute name for whether an container package is required or not.
   */
  public static final String ATTRIBUTE_NAME_CONTAINER_PACKAGES_PACKAGE_REQUIRED = "required";

  /**
   * XML attribute value for an container package being required.
   */
  public static final String ATTRIBUTE_VALUE_CONTAINER_PACKAGES_PACKAGE_REQUIRED_VALUE_TRUE = "true";

  /**
   * XML attribute value for an container package not being required.
   */
  public static final String ATTRIBUTE_VALUE_CONTAINER_PACKAGES_PACKAGE_REQUIRED_VALUE_FALSE = "false";

  /**
   * XML attribute name for the version of the required package.
   */
  public static final String ATTRIBUTE_NAME_CONTAINER_PACKAGES_PACKAGE_VERSION = "version";

  /**
   * Factory for building the constituent builder.
   *
   * @author Keith M. Hughes
   */
  public static class LibraryProjectBuilderFactory implements ProjectConstituentBuilderFactory {
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

      getOsgiInfo(namespace, rootElement, lproject.getContainerInfo());

      return null;
    }

    /**
     * Get all container info for an container element if there is one.
     *
     * @param namespace
     *          XML namespace for elements
     * @param rootElement
     *          the root element for the library
     * @param containerInfo
     *          the container Info object to populate
     */
    @SuppressWarnings("unchecked")
    private void getOsgiInfo(Namespace namespace, Element rootElement, ContainerInfo containerInfo) {

      Element osgiElement = rootElement.getChild(CONTAINER_ELEMENT, namespace);
      if (osgiElement != null) {
        containerInfo.setActivatorClassname(osgiElement.getChildText(CONTAINER_ACTIVATOR_ELEMENT, namespace));

        extractPrivatePackages(namespace, osgiElement, containerInfo);
        extractImportPackages(namespace, osgiElement, containerInfo);
      }
    }

    /**
     * Get private package data.
     *
     * @param namespace
     *          XML namespace for elements
     * @param rootElement
     *          root element for the container
     * @param containerInfo
     *          the container info object to store the packages in
     */
    private void extractPrivatePackages(Namespace namespace, Element rootElement, ContainerInfo containerInfo) {
      Element packagesElement = rootElement.getChild(CONTAINER_PRIVATE_PACKAGES_ELEMENT, namespace);
      if (packagesElement != null) {
        @SuppressWarnings("unchecked")
        List<Element> packageElements = packagesElement.getChildren(ELEMENT_NAME_CONTAINER_PACKAGES_PACKAGE, namespace);
        for (Element packageElement : packageElements) {
          String packageName = packageElement.getTextTrim();
          if (packageName != null && !packageName.isEmpty()) {
            containerInfo.addPrivatePackages(packageName);
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
     *          root element for the container
     * @param containerInfo
     *          the container info object to store the packages in
     */
    private void extractImportPackages(Namespace namespace, Element rootElement, ContainerInfo containerInfo) {
      List<ImportPackage> packages = containerInfo.getImportPackages();

      Element packagesElement = rootElement.getChild(CONTAINER_IMPORT_PACKAGES_ELEMENT, namespace);
      if (packagesElement != null) {
        @SuppressWarnings("unchecked")
        List<Element> packageElements = packagesElement.getChildren(ELEMENT_NAME_CONTAINER_PACKAGES_PACKAGE, namespace);
        for (Element packageElement : packageElements) {
          String packageName = packageElement.getTextTrim();
          if (packageName != null && !packageName.isEmpty()) {
            String requiredAttribute =
                packageElement.getAttributeValue(ATTRIBUTE_NAME_CONTAINER_PACKAGES_PACKAGE_REQUIRED,
                    ATTRIBUTE_VALUE_CONTAINER_PACKAGES_PACKAGE_REQUIRED_VALUE_TRUE);

            VersionRange versionRange = null;
            String versionAttribute =
                packageElement.getAttributeValue(ATTRIBUTE_NAME_CONTAINER_PACKAGES_PACKAGE_VERSION);
            if (versionAttribute != null) {
              versionRange = VersionRange.parseVersionRange(versionAttribute);
            }

            packages.add(new ImportPackage(packageName, ATTRIBUTE_VALUE_CONTAINER_PACKAGES_PACKAGE_REQUIRED_VALUE_TRUE
                .equals(requiredAttribute), versionRange));
          }
        }
      }
    }
  }
}
