package interactivespaces.workbench.project;

import interactivespaces.SimpleInteractiveSpacesException;

/**
 * A simple file template specification.
 */
public class ProjectFileTemplate {

  /**
   * The input template path.
   */
  private final String templatePath;

  /**
   * Output path.
   */
  private final String outputPath;

  /**
   * Create a new file template.
   *
   * @param templatePath
   *          input path
   * @param outputPath
   *          output path
   */
  public ProjectFileTemplate(String templatePath, String outputPath) {
    this.templatePath = templatePath;
    this.outputPath = outputPath;
  }

  /**
   * @return file input template path
   */
  public String getTemplatePath() {
    return templatePath;
  }

  /**
   * @return output path
   */
  public String getOutputPath() {
    return outputPath;
  }

  /**
   * Create a new instance from an input string specification.
   *
   * @param input
   *          input string
   *
   * @return instance constructed from input string
   */
  public static ProjectFileTemplate fromString(String input) {
    String[] parts = input.split(",");
    if (parts.length > 2) {
      throw new SimpleInteractiveSpacesException("Too many parts in template spec " + input);
    }
    return new ProjectFileTemplate(parts[0], parts[1]);
  }
}
