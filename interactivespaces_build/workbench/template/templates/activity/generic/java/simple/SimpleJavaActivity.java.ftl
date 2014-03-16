package ${activityClassPackage};

import interactivespaces.activity.impl.BaseActivity;

/**
 * A simple Interactive Spaces Java-based activity.
 */
public class SimpleJavaActivity extends BaseActivity {

    @Override
    public void onActivitySetup() {
        getLog().info("Activity ${project.identifyingName} setup");
    }

    @Override
    public void onActivityStartup() {
        getLog().info("Activity ${project.identifyingName} startup");
    }

    @Override
    public void onActivityPostStartup() {
        getLog().info("Activity ${project.identifyingName} post startup");
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
    public void onActivityPreShutdown() {
        getLog().info("Activity ${project.identifyingName} pre shutdown");
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
