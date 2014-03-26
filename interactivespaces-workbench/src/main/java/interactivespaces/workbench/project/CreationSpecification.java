package interactivespaces.workbench.project;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import interactivespaces.workbench.FreemarkerTemplater;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 */
public class CreationSpecification {

  private final Map<String, Object> templateData = Maps.newTreeMap();

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
