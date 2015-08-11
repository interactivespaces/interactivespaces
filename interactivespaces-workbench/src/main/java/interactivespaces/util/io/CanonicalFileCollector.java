package interactivespaces.util.io;

import interactivespaces.SimpleInteractiveSpacesException;

import com.google.common.collect.Maps;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

/**
 * Simple version of a file map that converts everything to a cannonical file as a key, so that
 * duplicate files are not present in the key map.  This can happen, for example, when there is
 * a simple '.' in the file path somewhere, which causes two files to look different when they're
 * really the same.
 */
public class CanonicalFileCollector implements FileCollector {

  /**
   * Internal map for keeping a set of source files.
   */
  private Map<File, File> fileMap = Maps.newHashMap();

  @Override
  public void put(File destination, File source) {
    try {
      fileMap.put(destination.getCanonicalFile(), source);
    } catch (IOException e) {
      throw SimpleInteractiveSpacesException
          .newFormattedException("Error getting canonical version of %s", destination, e);
    }
  }

  @Override
  public Set<Map.Entry<File, File>> entrySet() {
    return fileMap.entrySet();
  }
}
