package interactivespaces.liveactivity.runtime.monitor.internal;

import interactivespaces.liveactivity.runtime.monitor.LiveActivityRuntimeMonitorPlugin;
import interactivespaces.liveactivity.runtime.monitor.PluginFunctionalityDescriptor;
import interactivespaces.service.web.server.HttpRequest;
import interactivespaces.service.web.server.HttpResponse;

import com.google.common.collect.Lists;

import java.io.OutputStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * The live activity monitor plugin that provides a web index for all the monitoring plugins.
 *
 * @author Keith M. Hughes
 */
public class IndexLiveActivityRuntimeMonitorPlugin extends BaseLiveActivityRuntimeMonitorPlugin {

  /**
   * The URL prefix for this plugin.
   */
  public static final String URL_PREFIX = "/";

  /**
   * A comparator for sorting plugins in alphabetically increasing order.
   */
  public static final Comparator<PluginFunctionalityDescriptor> FUNCTIONALITY_DESCRIPTOR_COMPARATOR =
      new Comparator<PluginFunctionalityDescriptor>() {

        @Override
        public int compare(PluginFunctionalityDescriptor o1, PluginFunctionalityDescriptor o2) {
          return o1.getDisplayName().compareToIgnoreCase(o2.getDisplayName());
        }
      };

  /**
   * The functionality descriptors for this plugin.
   */
  private List<PluginFunctionalityDescriptor> functionalityDescriptors = Collections.unmodifiableList(Lists
      .newArrayList(new PluginFunctionalityDescriptor(URL_PREFIX, "Index")));

  @Override
  public String getUrlPrefix() {
    return URL_PREFIX;
  }

  @Override
  public List<PluginFunctionalityDescriptor> getFunctionalityDescriptors() {
    return functionalityDescriptors;
  }

  @Override
  protected void onHandleRequest(HttpRequest request, HttpResponse response, String fullRequestPath) throws Throwable {
    OutputStream outputStream = startWebResponse(response, false);
    addCommonPageHeader(outputStream, "Debugging");

    List<PluginFunctionalityDescriptor> descriptors = Lists.newArrayList();
    for (LiveActivityRuntimeMonitorPlugin plugin : getMonitorService().getPlugins()) {
      descriptors.addAll(plugin.getFunctionalityDescriptors());
    }
    Collections.sort(descriptors, FUNCTIONALITY_DESCRIPTOR_COMPARATOR);

    StringBuilder links = new StringBuilder();
    links.append("<ul>");
    for (PluginFunctionalityDescriptor descriptor : descriptors) {
      links.append("<li>");
      addLink(links, descriptor.getUrl(), descriptor.getDisplayName());
      links.append("</li>");
    }
    links.append("</ul>");
    outputStream.write(links.toString().getBytes());

    endWebResponse(outputStream, false);
  }

}
