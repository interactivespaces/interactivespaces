package interactivespace.test.activity.routable.RouteScale.Master;

import com.google.common.collect.Maps;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import interactivespaces.activity.impl.ros.BaseRoutableRosActivity;


/**
 * A simple Interactive Spaces Java-based activity.
 */
public class RouteMasterTest extends BaseRoutableRosActivity {

    private Map<String, Object> data;
  
    @Override
    public void onActivitySetup() {
        getLog().info("Activity interactivespace.test.activity.routable.RouteScale.Master setup");
    }

	@Override
	public void onActivityStartup() {
	    data = Maps.newHashMap();
	    int dataSize = getConfiguration().getPropertyInteger("space.activity.dataSize", 1000);
	    for (int i = 0; i < dataSize; i++) {
	      data.put(String.valueOf(i), "test");
	    }
		getLog().info("Activity interactivespace.test.activity.routable.RouteScale.Master startup");
	}

	@Override
	public void onActivityActivate() {
		getLog().info("Activity interactivespace.test.activity.routable.RouteScale.Master activate");
		int delay = getConfiguration().getPropertyInteger("space.activity.dataRate", 1000);
		getLog().info(delay);
		getManagedCommands().scheduleAtFixedRate(new Runnable() {
		  @Override
		  public void run() {
		    sendOnRoutes();
		  }
		}, 1000, delay, TimeUnit.MILLISECONDS);
		// On activiation, we will schedule a command to send on all our routes
	}
	
	private void sendOnRoutes() {
	    getLog().info("sending data on routes");
		int numRoutes = getConfiguration().getPropertyInteger("space.activity.numRoutes", 19);
		for (int i = 0; i < numRoutes; i++) {
		  getLog().info("sending on " + String.valueOf(i+1));
		  sendOutputJson("out" + String.valueOf(i+1), data);
		}
		getLog().info("done sending on routes");
		
		
	}

	@Override
	public void onActivityDeactivate() {
		getLog().info("Activity interactivespace.test.activity.routable.RouteScale.Master deactivate");
	}

	@Override
	public void onActivityShutdown() {
		getLog().info("Activity interactivespace.test.activity.routable.RouteScale.Master shutdown");
	}

    @Override
    public void onActivityCleanup() {
        getLog().info("Activity interactivespace.test.activity.routable.RouteScale.Master cleanup");
    }
}
