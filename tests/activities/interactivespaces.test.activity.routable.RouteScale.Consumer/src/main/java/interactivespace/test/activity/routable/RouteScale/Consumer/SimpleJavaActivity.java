package interactivespace.test.activity.routable.RouteScale.Consumer;

import java.util.Map;

import interactivespaces.activity.impl.ros.BaseRoutableRosActivity;


/**
 * A simple Interactive Spaces Java-based activity.
 */
public class SimpleJavaActivity extends BaseRoutableRosActivity {

    @Override
    public void onNewInputJson(String route, Map<String, Object> data) {
      getLog().info("got data on route:" + route);
    }
  
    @Override
    public void onActivitySetup() {
        getLog().info("Activity interactivespace.test.activity.routable.RouteScale.Consumer setup");
    }

	@Override
	public void onActivityStartup() {
		getLog().info("Activity interactivespace.test.activity.routable.RouteScale.Consumer startup");
	}

	@Override
	public void onActivityActivate() {
		getLog().info("Activity interactivespace.test.activity.routable.RouteScale.Consumer activate");
	}

	@Override
	public void onActivityDeactivate() {
		getLog().info("Activity interactivespace.test.activity.routable.RouteScale.Consumer deactivate");
	}

	@Override
	public void onActivityShutdown() {
		getLog().info("Activity interactivespace.test.activity.routable.RouteScale.Consumer shutdown");
	}

    @Override
    public void onActivityCleanup() {
        getLog().info("Activity interactivespace.test.activity.routable.RouteScale.Consumer cleanup");
    }
}
