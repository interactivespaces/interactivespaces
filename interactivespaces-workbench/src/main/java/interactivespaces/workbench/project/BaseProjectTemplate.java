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
public class BaseProjectTemplate implements ProjectTemplate {

  public static final String TEMPLATE_VARIABLES_TMP = "template_variables.tmp";

  public static final String BASE_DIRECTORY_VARIABLE = "baseDirectory";
  /**
   * Map of file/template pairs to add to the created project.
   */
  private final List<TemplateFile> fileTemplates = Lists.newLinkedList();

  private final List<TemplateVar> templateVars = Lists.newArrayList();

  private FreemarkerTemplater templater;

  public void process(ProjectCreationSpecification spec) {
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
  protected void onTemplateSetup(ProjectCreationSpecification spec) {
    Project project = spec.getProject();

    spec.addTemplateDataEntry("baseDirectory", spec.getBaseDirectory().getAbsolutePath());
    spec.addTemplateDataEntry("internalTemplates", FreemarkerTemplater.TEMPLATE_LOCATION.getAbsoluteFile());
    spec.addTemplateDataEntry("spec", spec);
    spec.addTemplateDataEntry("project", project);

    FreemarkerTemplater templater = getTemplater();
    for (TemplateVar templateVar : project.getTemplateVars()) {
      templater.processStringTemplate(spec.getTemplateData(), templateVar.getValue(), templateVar.getName());
    }
  }

  protected void onTemplateWrite(ProjectCreationSpecification spec) {
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
   * @throws InteractiveSpacesException
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

  public void addAllFileTemplates(List<TemplateFile> addFileTemplate) {
    fileTemplates.addAll(addFileTemplate);
  }

  public List<TemplateVar> getTemplateVars() {
    return templateVars;
  }

  public void addAllTemplateVars(List<TemplateVar> addTemplateVars) {
    templateVars.addAll(addTemplateVars);
  }

  /**
   * Write templates common to all projects of a given type.
   *
   * @param spec
   *          specification for the project
   *
   */
  public void writeTemplateList(ProjectCreationSpecification spec) {
    for (TemplateFile template : spec.getProject().getTemplates()) {

      FreemarkerTemplater templater = getTemplater();
      Map<String, Object> templateData = spec.getTemplateData();

      String outPath = templater.processStringTemplate(templateData, template.getOutput(), null);
      File outFile = new File(outPath);
      if (!outFile.isAbsolute()) {
        String newBasePath = (String) templateData.get(BASE_DIRECTORY_VARIABLE);
        outFile = new File(newBasePath, outFile.getPath());
      }

      String inPath = templater.processStringTemplate(templateData, template.getTemplate(), null);
      File inFile = new File(inPath);
      if (!inFile.isAbsolute()) {
        inPath = new File(spec.getSpecificationBase(), inPath).getAbsolutePath();
      }

      templater.writeTemplate(templateData, outFile, inPath);
    }
  }

  public FreemarkerTemplater getTemplater() {
    return templater;
  }

  public void setTemplater(FreemarkerTemplater templater) {
    this.templater = templater;
  }
}
