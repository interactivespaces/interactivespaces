/**
 *
 */
package interactivespaces.workbench.project.java;

import interactivespaces.workbench.project.ProjectTaskContext;

import java.io.File;
import java.util.List;

/**
 * A Java compiler for Interactive Spaces projects.
 *
 * @author Keith M. Hughes
 */
public interface ProjectJavaCompiler {

  /**
   * Folder where the main classes will be built.
   */
  String BUILD_DIRECTORY_CLASSES_MAIN = "classes/main";

  /**
   * Folder where the main classes will be built.
   */
  String BUILD_DIRECTORY_CLASSES_TESTS = "classes/test";

  /**
   * The default version of Java that items are compiled for.
   */
  String JAVA_VERSION_DEFAULT = "1.6";

  /**
   * configuration property for adding options to the JavaC compiler.
   */
  String CONFIGURATION_BUILDER_JAVA_COMPILEFLAGS =
      "interactivespaces.workbench.builder.java.compileflags";

  /**
   * Compile a set of Java files.
   *
   * @param compilationBuildDirectory
   *          the build folder for compilation artifacts
   * @param classpath
   *          the class path for the compilation
   * @param compilationFiles
   *          the compilation files for the compile
   * @param compilerOptions
   *          any options for the compiler
   *
   * @return {@code true} if the compilation was successful
   */
  boolean compile(File compilationBuildDirectory, List<File> classpath,
      List<File> compilationFiles, List<String> compilerOptions);

  /**
   * Get all compiler options to be used.
   *
   * @param context
   *          the build context
   *
   * @return the complete compiler options
   */
  List<String> getCompilerOptions(ProjectTaskContext context);

  /**
   * get a list of files to compile.
   *
   * @param baseSourceDirectory
   *          the base directory to scan for sources from
   *
   * @return a list of all Java files to be handed to the Java compiler
   */
  List<File> getCompilationFiles(File baseSourceDirectory);

}
