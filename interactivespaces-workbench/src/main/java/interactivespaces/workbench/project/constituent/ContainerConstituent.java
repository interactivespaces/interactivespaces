package interactivespaces.workbench.project.constituent;

import interactivespaces.SimpleInteractiveSpacesException;
import interactivespaces.util.io.FileSupport;
import interactivespaces.util.io.FileSupportImpl;

import java.util.Map;

/**
 * Abstract class for all constituents than can be used in project containers.
 */
public abstract class ContainerConstituent implements ProjectConstituent {
  /**
   * File support instance for file operations.
   */
  protected final FileSupport fileSupport = FileSupportImpl.INSTANCE;

  @Override
  public String getSourceDirectory() throws SimpleInteractiveSpacesException {
    return null;
  }

  /**
   * @return attributes that can be used for reconstructing the constituent
   */
  public Map<String, String> getAttributeMap() {
    return null;
  }
}
