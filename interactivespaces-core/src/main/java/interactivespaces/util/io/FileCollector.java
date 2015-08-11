package interactivespaces.util.io;

import java.io.File;
import java.util.Map;
import java.util.Set;

/**
 * Interface used to collect file operations for creating output maps.
 */
public interface FileCollector {

  /**
   * Put an entry into the collection. Designed to be compatible with {@link Map.put}
   *
   * @param dest
   *          destination file
   * @param src
   *          source file
   */
  void put(File dest, File src);

  /**
   * Get the entry set of collected files.
   *
   * @return collected files
   */
  Set<Map.Entry<File, File>> entrySet();
}
