package interactivespaces.workbench.project;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Closeables;
import interactivespaces.InteractiveSpacesException;
import interactivespaces.SimpleInteractiveSpacesException;
import interactivespaces.workbench.FreemarkerTemplater;
import interactivespaces.workbench.InteractiveSpacesWorkbench;

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

  public void process(CreationSpecification spec, InteractiveSpacesWorkbench workbench,
      FreemarkerTemplater templater, Map<String, Object> templateData) {

    Map<String, Object> fullTemplateData = Maps.newTreeMap();
    fullTemplateData.putAll(templateData);

    onTemplateSetup(spec, templater, fullTemplateData);

    try {

      onTemplateWrite(spec, workbench, templater, fullTemplateData);
    } catch (Exception e) {
      File variableDump = new File(TEMPLATE_VARIABLES_TMP);
      dumpVariables(fullTemplateData, variableDump);
      throw new SimpleInteractiveSpacesException("Template variables are in " + variableDump.getAbsolutePath(), e);
    }
  }

  /**
   * Template is being set up.
   *
   * @param spec
   *          spec for the project
   * @param templater
   *          the templater to use for setup
   * @param fullTemplateData
   *          template data to setup
   */
  protected void onTemplateSetup(CreationSpecification spec,
      FreemarkerTemplater templater, Map<String, Object> fullTemplateData) {
    fullTemplateData.put("baseDirectory", spec.getBaseDirectory());
    for (TemplateVar templateVar : spec.getTemplateVars()) {
      templater.processStringTemplate(fullTemplateData, templateVar.getValue(), templateVar.getName());
    }
  }

  protected void onTemplateWrite(CreationSpecification spec, InteractiveSpacesWorkbench workbench,
      FreemarkerTemplater templater, Map<String, Object> fullTemplateData) {
    writeTemplateList(spec, workbench, templater, fullTemplateData);
  }

  private void dumpVariables(Map<String, Object> fullTemplateData, File variableDump) {
    PrintWriter variableWriter = null;
    try {
      variableWriter = new PrintWriter(variableDump);
      for (Map.Entry<String, Object> entry : fullTemplateData.entrySet()) {
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

  /**
   * Write templates common to all projects of a given type.
   *
   * @param spec
   *          specification for the project
   * @param workbench
   *          the workbench the project is being built under
   * @param templater
   *          the templater to use
   * @param fullTemplateData
   *          the full data to be used for the template
   */
  public void writeTemplateList(CreationSpecification spec,
      InteractiveSpacesWorkbench workbench, FreemarkerTemplater templater,
      Map<String, Object> fullTemplateData) {
    for (TemplateFile template : fileTemplates) {

      File outFile = new File(templater.processStringTemplate(fullTemplateData, template.getOutput(), null));
      if (!outFile.isAbsolute()) {
        File projectDirectory = getBaseDirectory(spec, templater, fullTemplateData);
        outFile = new File(projectDirectory, outFile.getPath());
      }

      String relativeInPath = templater.processStringTemplate(fullTemplateData, template.getTemplate(), null);
      File specificationSource = spec.getSpecification();
      String absoluteInPath = new File(specificationSource.getParentFile(), relativeInPath).getAbsolutePath();
      templater.writeTemplate(fullTemplateData, outFile, absoluteInPath);
    }
  }

  protected File getBaseDirectory(CreationSpecification spec, FreemarkerTemplater templater,
      Map<String, Object> fullTemplateData) {
    return spec.getBaseDirectory();
  }
}
