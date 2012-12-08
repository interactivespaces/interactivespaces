package ${project.activityDescription.identifyingName};

import interactivespaces.activity.impl.BaseActivity;

/**
 * A simple Interactive Spaces Android-based activity.
 */
public class SimpleAndroidActivity extends BaseActivity {

    @Override
    public void onActivitySetup() {
        getLog().info("Activity ${project.activityDescription.identifyingName} setup");
    }

	@Override
	public void onActivityStartup() {
		getLog().info("Activity ${project.activityDescription.identifyingName} startup");
	}

	@Override
	public void onActivityActivate() {
		getLog().info("Activity ${project.activityDescription.identifyingName} activate");
	}

	@Override
	public void onActivityDeactivate() {
		getLog().info("Activity ${project.activityDescription.identifyingName} deactivate");
	}

	@Override
	public void onActivityShutdown() {
		getLog().info("Activity ${project.activityDescription.identifyingName} shutdown");
	}

    @Override
    public void onActivityCleanup() {
        getLog().info("Activity ${project.activityDescription.identifyingName} cleanup");
    }
}
