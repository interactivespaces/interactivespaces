package interactivespaces.workbench.project.group;

import interactivespaces.SimpleInteractiveSpacesException;
import interactivespaces.workbench.project.builder.BaseProjectBuilder;
import interactivespaces.workbench.project.builder.ProjectBuildContext;

/**
 */
public class BlankProjectBuilder extends BaseProjectBuilder<BlankProject> {
  @Override
  public boolean build(BlankProject project, ProjectBuildContext context) {
    throw new SimpleInteractiveSpacesException("Can't build blank projects");
  }
}
