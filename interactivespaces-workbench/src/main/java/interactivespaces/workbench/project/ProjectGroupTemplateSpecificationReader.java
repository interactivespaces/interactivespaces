package interactivespaces.workbench.project;

import interactivespaces.workbench.project.group.GroupProjectTemplateSpecification;

import java.io.File;

/**
 * Group specification to read.
 *
 * @author Trevor Pering
 */
public interface ProjectGroupTemplateSpecificationReader {

  /**
   * Read a group project template specification from a file.
   *
   * @param specFile
   *          file to read
   *
   * @return specificaiton
   */
  GroupProjectTemplateSpecification readProject(File specFile);
}
