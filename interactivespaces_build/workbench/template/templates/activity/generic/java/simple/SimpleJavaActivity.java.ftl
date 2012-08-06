package ${project.activity.identifyingName};

import interactivespaces.activity.impl.BaseActivity;

/**
 * A simple Interactive Spaces Java-based activity.
 */
public class SimpleJavaActivity extends BaseActivity {

    @Override
    public void onActivitySetup() {
        getLog().info("Activity ${project.activity.identifyingName} setup");
    }

	@Override
	public void onActivityStartup() {
		getLog().info("Activity ${project.activity.identifyingName} startup");
	}

	@Override
	public void onActivityActivate() {
		getLog().info("Activity ${project.activity.identifyingName} activate");
	}

	@Override
	public void onActivityDeactivate() {
		getLog().info("Activity ${project.activity.identifyingName} deactivate");
	}

	@Override
	public void onActivityShutdown() {
		getLog().info("Activity ${project.activity.identifyingName} shutdown");
	}

    @Override
    public void onActivityCleanup() {
        getLog().info("Activity ${project.activity.identifyingName} cleanup");
    }
}
