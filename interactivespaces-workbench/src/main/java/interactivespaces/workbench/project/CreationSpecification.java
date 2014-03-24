package interactivespaces.workbench.project;

import com.google.common.collect.Lists;

import java.io.File;
import java.util.List;

/**
 */
public class CreationSpecification {
  private final List<TemplateVar> templateVars = Lists.newArrayList();
  /**
   * The template for the project.
   */
  private File specification;
  private File baseDirectory;

  /**
   * Get the project template.
   *
   * @return the template
   */
  public File getSpecification() {
    return specification;
  }

  /**
   * Set the project template.
   *
   * @param specification
   *          the template to set
   */
  public void setSpecification(File specification) {
    this.specification = specification;
  }

  public void addAllTemplateVars(List<TemplateVar> addTemplateVars) {
    templateVars.addAll(addTemplateVars);
  }

  public List<TemplateVar> getTemplateVars() {
    return templateVars;
  }

  public File getBaseDirectory() {
    return baseDirectory;
  }

  public void setBaseDirectory(File baseDirectory) {
    this.baseDirectory = baseDirectory;
  }
}
