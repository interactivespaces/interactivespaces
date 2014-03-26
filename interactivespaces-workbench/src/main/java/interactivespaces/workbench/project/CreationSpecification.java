package interactivespaces.workbench.project;

import com.google.common.collect.Maps;
import interactivespaces.workbench.FreemarkerTemplater;

import java.io.File;
import java.util.Map;

/**
 */
public class CreationSpecification {

  private final Map<String, Object> templateData = Maps.newTreeMap();

  /**
   * The project itself.
   */
  private Project project;

  private FreemarkerTemplater templater;
  /**
   * The template for the project.
   */
  private File specificationBase;
  /**
   * Base directory where output files should be rooted.
   */
  private File baseDirectory;

  /**
   * Get the project description associated with the spec.
   *
   * @param <T>
   *          the project type
   *
   * @return the project
   */
  @SuppressWarnings("unchecked")
  public <T extends Project> T getProject() {
    return (T) project;
  }

  /**
   * Set the project description for the spec.
   *
   * @param project
   *          the project to set
   */
  public void setProject(Project project) {
    this.project = project;
  }

  /**
   * Get the project template.
   *
   * @return the template
   */
  public File getSpecificationBase() {
    return specificationBase;
  }

  /**
   * Set the project template.
   *
   * @param specification
   *          the template to set
   */
  public void setSpecificationBase(File specification) {
    this.specificationBase = specification;
  }

  public File getBaseDirectory() {
    return baseDirectory;
  }

  public void setBaseDirectory(File baseDirectory) {
    this.baseDirectory = baseDirectory;
  }

  public Map<String, Object> getTemplateData() {
    return templateData;
  }

  public void addTemplateData(Map<String, Object> addData) {
    templateData.putAll(addData);
  }

  public void addTemplateDataEntry(String key, Object value) {
    templateData.put(key, value);
  }

  public FreemarkerTemplater getTemplater() {
    return templater;
  }

  public void setTemplater(FreemarkerTemplater templater) {
    this.templater = templater;
  }

}
