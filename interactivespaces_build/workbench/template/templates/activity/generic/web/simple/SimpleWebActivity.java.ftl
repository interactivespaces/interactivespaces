package ${project.identifyingName};

import interactivespaces.activity.impl.BaseActivity;
import interactivespaces.activity.component.web.WebServerActivityComponent;

/**
  * A simple Interactive Spaces Java-based activity for running a web server.
  */
public class SimpleWebActivity extends BaseActivity {
  /**
   * The web server component.
   */
  private WebServerActivityComponent webServerComponent;

  @Override
  public void onActivitySetup() {
    getLog().info("Activity ${project.identifyingName} setup");
    webServerComponent = addActivityComponent(WebServerActivityComponent.COMPONENT_NAME);
  }

  @Override
  public void onActivityStartup() {
    getLog().info("Activity ${project.identifyingName} startup");
  }
}
