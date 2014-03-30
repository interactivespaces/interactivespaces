package interactivespaces.workbench.project;

import interactivespaces.workbench.InteractiveSpacesWorkbench;
import interactivespaces.workbench.project.activity.type.ProjectType;

import java.io.File;
import java.util.Map;

/**
 * Interface for managing a project context, used for creating or building.
 *
 * @author Trevor Pering
 */
public interface ProjectContext {

  /**
   * Get the project being built.
   *
   * @return the project being built
   */
  Project getProject();

  /**
   * Get the workbench the project is being built under.
   *
   * @return the workbench
   */
  InteractiveSpacesWorkbench getWorkbench();

  /**
   * Get the project type for the project.
   * @param <T>
   *          project type class
   *
   * @return project type for the context
   */
  <T extends ProjectType> T getProjectType();

  /**
   * Get a target file for this context.
   *
   * @param rootDirectory
   *          root directory for calculating project target
   * @param target
   *          the target file name to access
   *
   * @return correct (absolute) file to use
   */
  File getProjectTarget(File rootDirectory, String target);

  /**
   * Get the (mutable) resource source map for the current context.
   *
   * @return resource source map
   */
  Map<File, File> getResourceSourceMap();
}
