package ${project.identifyingName};

import interactivespaces.activity.impl.BaseActivity;

/**
 * A simple Interactive Spaces Android-based activity.
 */
public class SimpleAndroidActivity extends BaseActivity {

    @Override
    public void onActivitySetup() {
        getLog().info("Activity ${project.identifyingName} setup");
    }

    @Override
    public void onActivityStartup() {
        getLog().info("Activity ${project.identifyingName} startup");
    }

    @Override
    public void onActivityActivate() {
        getLog().info("Activity ${project.identifyingName} activate");
    }

    @Override
    public void onActivityDeactivate() {
        getLog().info("Activity ${project.identifyingName} deactivate");
    }

    @Override
    public void onActivityShutdown() {
        getLog().info("Activity ${project.identifyingName} shutdown");
    }

    @Override
    public void onActivityCleanup() {
        getLog().info("Activity ${project.identifyingName} cleanup");
    }
}
