package interactivespaces.workbench.project;

import com.google.common.collect.Lists;
import com.google.common.io.Closeables;
import interactivespaces.InteractiveSpacesException;
import interactivespaces.SimpleInteractiveSpacesException;
import interactivespaces.workbench.FreemarkerTemplater;

import java.io.File;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

/**
 */
public class BaseTemplate {
  public static final String TEMPLATE_VARIABLES_TMP = "template_variables.tmp";
  /**
   * Map of file/template pairs to add to the created project.
   */
  private final List<TemplateFile> fileTemplates = Lists.newLinkedList();

  private final List<TemplateVar> templateVars = Lists.newArrayList();

  /**
   * The display name for the template.
   */
  protected String displayName;

  public BaseTemplate(String displayName) {
    this.displayName = displayName;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void process(CreationSpecification spec) {
    try {
      onTemplateSetup(spec);
      onTemplateWrite(spec);
    } catch (Exception e) {
      File variableDump = new File(TEMPLATE_VARIABLES_TMP);
      dumpVariables(variableDump, spec.getTemplateData());
      throw new SimpleInteractiveSpacesException(
          "Template variables can be found in " + variableDump.getAbsolutePath(), e);
    }
  }

  /**
   * Template is being set up.
   *
   * @param spec
   *          spec for the project
   *
   */
  protected void onTemplateSetup(CreationSpecification spec) {
    spec.addTemplateDataEntry("baseDirectory", spec.getBaseDirectory());
    FreemarkerTemplater templater = spec.getTemplater();
    for (TemplateVar templateVar : spec.getTemplateVars()) {
      templater.processStringTemplate(spec.getTemplateData(), templateVar.getValue(), templateVar.getName());
    }
  }

  protected void onTemplateWrite(CreationSpecification spec) {
    writeTemplateList(spec);
  }

  private void dumpVariables(File variableDump, Map<String, Object> variables) {
    PrintWriter variableWriter = null;
    try {
      variableWriter = new PrintWriter(variableDump);
      for (Map.Entry<String, Object> entry : variables.entrySet()) {
        variableWriter.println(String.format("%s=%s", entry.getKey(), entry.getValue()));
      }
    } catch (Exception e) {
      throw new SimpleInteractiveSpacesException(
          "Error writing variable dump file " + variableDump.getAbsolutePath(), e);
    } finally {
      Closeables.closeQuietly(variableWriter);
    }
  }

  /**
   * Make a directory, including all needed parent directories.
   *
   * @param directory
   *          the directory to create
   *
   * @throws interactivespaces.InteractiveSpacesException
   *           could not create directory
   */
  public void makeDirectory(File directory) throws InteractiveSpacesException {
    if (!directory.isDirectory() && !directory.mkdirs()) {
      throw new InteractiveSpacesException(String.format("Cannot create directory %s",
          directory.getAbsolutePath()));
    }
  }

  /**
   * Add a file template to the common collection.
   *
   * @param dest
   *          output destination
   * @param source
   *          template source
   */
  public void addFileTemplate(String dest, String source) {
    fileTemplates.add(new TemplateFile(dest, source));
  }

  public void addAllFileTemplate(List<TemplateFile> addFileTemplate) {
    fileTemplates.addAll(addFileTemplate);
  }

  public List<TemplateVar> getTemplateVars() {
    return templateVars;
  }

  public void addTemplateVars(List<TemplateVar> addTemplateVars) {
    templateVars.addAll(addTemplateVars);
  }

  /**
   * Write templates common to all projects of a given type.
   *
   * @param spec
   *          specification for the project
   *
   */
  public void writeTemplateList(CreationSpecification spec) {
    for (TemplateFile template : fileTemplates) {

      FreemarkerTemplater templater = spec.getTemplater();
      Map<String, Object> templateData = spec.getTemplateData();
      String outPath = templater.processStringTemplate(templateData, template.getOutput(), null);
      File outFile = new File(outPath);
      if (!outFile.isAbsolute()) {
        File projectDirectory = getBaseDirectory(spec, templater, templateData);
        outFile = new File(projectDirectory, outFile.getPath());
      }

      String relativeInPath = templater.processStringTemplate(templateData, template.getTemplate(), null);
      String absoluteInPath = new File(spec.getSpecificationBase(), relativeInPath).getAbsolutePath();
      templater.writeTemplate(templateData, outFile, absoluteInPath);
    }
  }

  protected File getBaseDirectory(CreationSpecification spec, FreemarkerTemplater templater,
      Map<String, Object> fullTemplateData) {
    return spec.getBaseDirectory();
  }
}
