package ${activityClassPackage};

import interactivespaces.activity.impl.BaseActivity;
import interactivespaces.activity.component.web.WebServerActivityComponent;

/**
  * A simple Interactive Spaces Java-based activity for running a web server.
  */
public class ${activityClassName} extends BaseActivity {
  /**
   * The web server component.
   */
  private WebServerActivityComponent webServerComponent;

  @Override
  public void onActivitySetup() {
    getLog().info("Activity ${project.identifyingName} setup");
    webServerComponent = addActivityComponent(WebServerActivityComponent.COMPONENT_NAME);
  }
}
