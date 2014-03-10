package interactivespaces.workbench.project;

import interactivespaces.SimpleInteractiveSpacesException;

import java.util.Map;

/**
 */
public class ProjectFileTemplate {

  private final String templatePath;
  private final String outputPath;

  public ProjectFileTemplate(String templatePath, String outputPath) {
    this.templatePath = templatePath;
    this.outputPath = outputPath;
  }

  public String getTemplatePath() {
    return templatePath;
  }

  public String getOutputPath() {
    return outputPath;
  }

  public static ProjectFileTemplate fromString(String input) {
    String[] parts = input.split(",");
    if (parts.length > 2) {
      throw new SimpleInteractiveSpacesException("Too many parts in template spec " + input);
    }
    return new ProjectFileTemplate(parts[0], parts[1]);
  }
}
