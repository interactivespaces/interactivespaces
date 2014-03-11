package interactivespaces.workbench.project.activity.template;

import interactivespaces.SimpleInteractiveSpacesException;
import interactivespaces.system.InteractiveSpacesEnvironment;
import interactivespaces.workbench.project.ProjectConfigurationProperty;
import interactivespaces.workbench.project.ProjectCreationSpecification;
import interactivespaces.workbench.project.activity.ActivityProject;

import java.util.Map;

/**
 */
public class BaseNativeActivityProjectTemplate extends BaseActivityProjectTemplate {

  /**
   * Construct a new instance.
   */
  public BaseNativeActivityProjectTemplate() {
    super("Generic Base Java Activity Project", "base");
  }

  /**
   * Construct a new instance with the given non-default parameters.
   *
   * @param displayName
   *          ui display name
   * @param language
   *          template language
   */
  public BaseNativeActivityProjectTemplate(String displayName, String language) {
    super(displayName, language);
  }

  @Override
  public void onTemplateSetup(ProjectCreationSpecification spec, ActivityProject activityProject,
      Map<String, Object> fullTemplateData) {

    if (activityProject.getActivityClass() == null) {
      throw new SimpleInteractiveSpacesException("Activity class not set by subclass");
    }
    activityProject.setBuilderType("java");
    activityProject.setActivityType("interactivespaces_native");

    activityProject.addConfigurationProperty(new ProjectConfigurationProperty(
        "space.activity.log.level", InteractiveSpacesEnvironment.LOG_LEVEL_INFO, false, null));

    fullTemplateData.put("activityClassName", activityProject.getActivityClassName());
    fullTemplateData.put("activityPackagePath", activityProject.getActivityPackagePath());
    fullTemplateData.put("activitySourceDirectory", activityProject.getActivitySourceDirectory());
    fullTemplateData.put("activityResourceDirectory", activityProject.getActivityResourceDirectory());
  }

  @Override
  public void onTemplateSetup(ProjectCreationSpecification spec, Map<String, Object> fullTemplateData) {
    super.onTemplateSetup(spec, fullTemplateData);

    if (fullTemplateData.get("activityClassName") == null) {
      throw new SimpleInteractiveSpacesException("Base native onTemplateSetup not called");
    }
  }
}
