/**
 *
 */
package interactivespaces.workbench.project.java;

import interactivespaces.InteractiveSpacesException;
import interactivespaces.workbench.project.builder.ProjectBuildContext;

import com.google.common.collect.Lists;
import com.google.common.io.Closeables;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;

/**
 * A project java compiler which uses the javax compiler.
 *
 * @author Keith M. Hughes
 */
public class JavaxProjectJavaCompiler implements ProjectJavaCompiler {

  /**
   * The version of Java being compiled for.
   */
  private final String javaVersion = JAVA_VERSION_DEFAULT;

  /**
   * Filename postfix to indicate an editor backup file.
   */
  private static final String EDITOR_BACKUP_FILE_POSTFIX = "~";

  @Override
  public boolean compile(File compilationBuildDirectory, List<File> classpath, List<File> compilationFiles,
      List<String> compilerOptions) {

    StandardJavaFileManager fileManager = null;
    try {
      JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
      if (compiler == null) {
        throw new InteractiveSpacesException("Could not find Java compiler.  Verify java is being run "
            + "from the JDK and not JRE.");
      }
      fileManager = compiler.getStandardFileManager(null, null, null);
      fileManager.setLocation(StandardLocation.CLASS_PATH, classpath);
      fileManager.setLocation(StandardLocation.CLASS_OUTPUT, Lists.newArrayList(compilationBuildDirectory));

      Iterable<? extends JavaFileObject> compilationUnits1 = fileManager.getJavaFileObjectsFromFiles(compilationFiles);

      Boolean success = compiler.getTask(null, fileManager, null, compilerOptions, null, compilationUnits1).call();

      return success;
    } catch (IOException e) {
      throw new InteractiveSpacesException("Error while compiling Java files", e);
    } finally {
      Closeables.closeQuietly(fileManager);
    }
  }

  @Override
  public List<String> getCompilerOptions(ProjectBuildContext context) {
    List<String> options = Lists.newArrayList();
    options.add("-source");
    options.add(javaVersion);
    options.add("-target");
    options.add(javaVersion);

    String extraOptions =
        context.getProject().getConfiguration().getPropertyString(CONFIGURATION_BUILDER_JAVA_COMPILEFLAGS);
    if (extraOptions != null) {
      String[] optionComponents = extraOptions.split("\\s+");
      for (String optionComponent : optionComponents) {
        options.add(optionComponent);
      }
    }
    return options;
  }

  /**
   * get a list of files to compile.
   *
   * @param baseSourceDirectory
   *          the base directory to scan for sources from
   *
   * @return a list of all Java files to be handed to the Java compiler
   */
  @Override
  public List<File> getCompilationFiles(File baseSourceDirectory) {
    List<File> files = Lists.newArrayList();

    scanDirectory(baseSourceDirectory, files);

    return files;
  }

  /**
   * Scan the given directory for files to add.
   *
   * <p>
   * This method will recurse into subdirectories.
   *
   * @param directory
   *          the directory to scan
   * @param files
   *          collection to add found files in
   */
  private void scanDirectory(File directory, List<File> files) {
    File[] directoryListing = directory.listFiles();
    if (directoryListing != null) {
      for (File file : directoryListing) {
        // Check for hidden/backup files/directories, we don't want those.
        boolean shouldIgnore = file.isHidden() || file.getName().endsWith(EDITOR_BACKUP_FILE_POSTFIX);
        if (!shouldIgnore) {
          if (file.isDirectory()) {
            scanDirectory(file, files);
          } else {
            files.add(file);
          }
        }
      }
    }
  }

}
