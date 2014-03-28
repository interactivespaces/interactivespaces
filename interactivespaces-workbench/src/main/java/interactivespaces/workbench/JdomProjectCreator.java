package interactivespaces.workbench;

import interactivespaces.SimpleInteractiveSpacesException;
import interactivespaces.workbench.project.JdomProjectReader;
import interactivespaces.workbench.project.Project;
import interactivespaces.workbench.project.ProjectCreationSpecification;
import interactivespaces.workbench.project.activity.ActivityProject;
import interactivespaces.workbench.project.group.JdomProjectGroupReader;
import interactivespaces.workbench.project.group.ProjectGroup;
import interactivespaces.workbench.project.group.ProjectGroupCreator;
import org.jdom.Element;

import java.io.File;
import java.util.List;

/**
 * Create projects given input specifications in XML.
 *
 * @author Trevor Pering
 */
public class JdomProjectCreator {

  /**
   * Containing workbench.
   */
  private final InteractiveSpacesWorkbench interactiveSpacesWorkbench;

  /**
   * Creator to use for project groups.
   */
  private final ProjectGroupCreator projectGroupCreator;

  /**
   * Create a new instance.
   *
   * @param interactiveSpacesWorkbench
   *          containing workbench instance
   */
  public JdomProjectCreator(InteractiveSpacesWorkbench interactiveSpacesWorkbench) {
    this.interactiveSpacesWorkbench = interactiveSpacesWorkbench;
    projectGroupCreator = new ProjectGroupCreator(interactiveSpacesWorkbench);
  }

  /**
   * Create projects from a specification, could be a project or a group of projects.
   *
   * @param commands
   *          specific creation commands
   */
  void createProjectsFromSpecification(List<String> commands, File specFile, File baseDirectory) {
    try {
      Element rootElement = JdomReader.getRootElement(specFile);
      String type = rootElement.getName();
      if (JdomProjectGroupReader.PROJECT_GROUP_ELEMENT_NAME.equals(type)) {
        createProjectGroupFromElement(rootElement, specFile, baseDirectory);
      } else if (JdomProjectReader.ELEMENT_NAME.equals(type)) {
        createProjectFromElement(rootElement, baseDirectory);
      } else {
        throw new SimpleInteractiveSpacesException("Unknown root element type " + type);
      }
    } catch (Exception e) {
      throw new SimpleInteractiveSpacesException(
          "While processing specification file " + specFile.getAbsolutePath(), e);
    }
  }

  /**
   * Create a project group from a given element.
   *
   * @param rootElement
   *          input element
   * @param specFile
   *          specification file
   * @param baseDirectory
   */
  private void createProjectGroupFromElement(Element rootElement, File specFile, File baseDirectory) {
    ProjectGroup projectGroup = new ProjectGroup();
    projectGroup.setSpecificationSource(specFile);
    projectGroup.setBaseDirectory(baseDirectory);

    JdomProjectGroupReader projectGroupReader = new JdomProjectGroupReader(interactiveSpacesWorkbench.getLog());
    projectGroupReader.setWorkbench(interactiveSpacesWorkbench);
    projectGroupReader.processSpecification(projectGroup, rootElement);

    projectGroupCreator.create(projectGroup);
  }

  /**
   * Create an output project from a project specification element.
   *
   * @param rootElement
   *          input root element
   * @param baseDirectory
   */
  private void createProjectFromElement(Element rootElement, File baseDirectory) {
    JdomProjectReader projectReader = new JdomProjectReader(interactiveSpacesWorkbench.getLog());
    projectReader.setWorkbench(interactiveSpacesWorkbench);

    Project project = projectReader.processSpecification(rootElement);
    project.setType(ActivityProject.PROJECT_TYPE_NAME);
    project.setBaseDirectory(baseDirectory);

    ProjectCreationSpecification spec = new ProjectCreationSpecification();
    spec.setProject(project);

    interactiveSpacesWorkbench.getActivityProjectCreator().create(spec);
  }
}
