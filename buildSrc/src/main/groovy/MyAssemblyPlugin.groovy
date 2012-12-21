
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.Plugin
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.file.FileCollection
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.tasks.Copy
 
class MyAssemblyPlugin implements Plugin<Project>  {
    public static final String RUNTIME_CONFIGURATION_NAME = "runtime"
    public static final String BUILD_DEPENDENTS_TASK_NAME = "buildDependents"
    
    def void apply(Project project) {
      project.getPlugins().apply(BasePlugin.class)
      configureConfigurations(project)
      configureBuildDependents(project)

      def assembleComponentsTask = project.task("assembleComponents")
        .dependsOn(BUILD_DEPENDENTS_TASK_NAME) << {
          assembleProject(project)
      }

      project.afterEvaluate {
        setBuildDependencies(project)
      }
    }

    void configureConfigurations(final Project project) {
        ConfigurationContainer configurations = project.getConfigurations()
 
        Configuration runtimeConfiguration = configurations.add(RUNTIME_CONFIGURATION_NAME).setVisible(true).setTransitive(false)
                .setDescription("Classpath for running the specified dependencies.")
  
        configurations.getByName(Dependency.DEFAULT_CONFIGURATION).extendsFrom(runtimeConfiguration)
    }

    void configureBuildDependents(Project project) {
        DefaultTask buildDependentsTask = project.getTasks().add(BUILD_DEPENDENTS_TASK_NAME, DefaultTask.class)
        buildDependentsTask.setDescription("Assembles this project and all projects that depend on it.")
        buildDependentsTask.setGroup(BasePlugin.BUILD_GROUP)
    }

    void setBuildDependencies(Project project) {
        DefaultTask buildDependentsTask = project.tasks.getByName(BUILD_DEPENDENTS_TASK_NAME)

        buildDependentsTask.dependsOn project.configurations.runtime.allDependencies.collect {
          it.dependencyProject.tasks.getByName('buildNeeded') 
        }
    }

    void assembleProject(Project project) {
      def files = new java.util.HashSet()

      project.configurations.runtime.allDependencies.each { collectProjectJars(it, project, files) }

      FileCollection assembledFiles = project.files(files)

      project.ext.assembledFiles = assembledFiles
      processFiles(project)
    }

    void collectProjectJars(ProjectDependency projectDependency, project, files) {
        println projectDependency.dependencyProject
        def runtimeConfiguration = projectDependency.dependencyProject.configurations.runtime

        files.addAll runtimeConfiguration.allArtifacts*.file

        def runtimeFiles = runtimeConfiguration.resolvedConfiguration.resolvedArtifacts*.file
        files.addAll runtimeFiles
    }

    void processFiles(project) {
      File libsAssets = project.file("libs")
      libsAssets.mkdirs()
      project.copy {
        from project.ext.assembledFiles
        into libsAssets
      }

      //libsAssets.eachFile { processFile(it, project) }
    }

    void processFile(File jar, project) {
      def archiveDir = jar.parent

      def dexFile = new File("classes.dex")
	  
      def dex = "$project.ext.androidPlatformTools/dx --dex --output=$dexFile $jar".execute()
      dex.waitFor()
	  
      if (dex.exitValue() == 0) {
        def aapt = "$project.ext.androidPlatformTools/aapt add $jar $dexFile".execute()
        aapt.waitFor()
        if (aapt.exitValue() != 0) {
          println "aapt failed"
        }
      } else {
        println "dex failed"
      }

      dexFile.delete()
    }
}

