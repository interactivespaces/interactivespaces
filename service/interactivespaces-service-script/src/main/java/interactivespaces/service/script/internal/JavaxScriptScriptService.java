/*
 * Copyright (C) 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package interactivespaces.service.script.internal;

import interactivespaces.InteractiveSpacesException;
import interactivespaces.activity.Activity;
import interactivespaces.activity.ActivityFilesystem;
import interactivespaces.configuration.Configuration;
import interactivespaces.service.script.ActivityScriptWrapper;
import interactivespaces.service.script.Script;
import interactivespaces.service.script.ScriptService;
import interactivespaces.service.script.ScriptSource;
import interactivespaces.service.script.StringScriptSource;
import interactivespaces.service.script.internal.javascript.RhinoJavascriptActivityScriptFactory;
import interactivespaces.service.script.internal.python.PythonActivityScriptFactory;
import interactivespaces.system.InteractiveSpacesEnvironment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.script.Compilable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

import org.codehaus.groovy.jsr223.GroovyScriptEngineFactory;
import org.python.jsr223.PyScriptEngineFactory;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * An {@link ScriptService} using {@code javax.script}.
 *
 * @author Keith M. Hughes
 */
public class JavaxScriptScriptService implements ScriptService {

  /**
   * All engines stored in the script engine.
   */
  private List<ScriptLanguage> languages = new ArrayList<JavaxScriptScriptService.ScriptLanguage>();

  /**
   * A mapping from names of languages to the factory.
   */
  private Map<String, ScriptLanguage> nameToLanguage =
      new HashMap<String, JavaxScriptScriptService.ScriptLanguage>();

  /**
   * A mapping from names of extensions to the factory.
   */
  private Map<String, ScriptLanguage> extensionToLanguage =
      new HashMap<String, JavaxScriptScriptService.ScriptLanguage>();

  /**
   * Interactive Spaces environment for scripting engine.
   */
  private InteractiveSpacesEnvironment spaceEnvironment;

  /**
   * The metadata for the service.
   */
  private Map<String, Object> metadata = Maps.newHashMap();

  @Override
  public Map<String, Object> getMetadata() {
    return metadata;
  }

  @Override
  public String getName() {
    return ScriptService.SERVICE_NAME;
  }

  @Override
  public void startup() {
    final ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
    Thread.currentThread().setContextClassLoader(null);
    try {
      // TODO(keith): Create a bundle listener which will scan bundles for
      // registered
      // factories.
      GroovyScriptEngineFactory groovyScriptEngineFactory = new GroovyScriptEngineFactory();
      registerLanguage("groovy", groovyScriptEngineFactory, null);
      registerLanguage("python", new PyScriptEngineFactory(), new PythonActivityScriptFactory(
          spaceEnvironment));
      // registerLanguage("javascript",
      // new RhinoJavascriptScriptEngineFactory(),
      // new RhinoJavascriptActivityScriptFactory());

      // Get any languages, other than Javascript, that
      // might be available.
      ScriptEngineManager mgr = new ScriptEngineManager();
      for (ScriptEngineFactory factory : mgr.getEngineFactories()) {
        if ("ECMAScript".equals(factory.getLanguageName())) {
          registerLanguage("javascript", factory, new RhinoJavascriptActivityScriptFactory());
        } else {
          registerLanguage(factory.getLanguageName(), factory, null);
        }
      }
    } finally {
      Thread.currentThread().setContextClassLoader(oldClassLoader);
    }
  }

  @Override
  public void shutdown() {
    // Nothing to do
  }

  /**
   * Register a new scripting engine factory with the factory.
   *
   * @param languageName
   *          the name of the language
   * @param scriptEngineFactory
   *          the JSR 223 scripting factory
   * @param activityScriptFactory
   *          the factory for making scripted activities
   */
  private void registerLanguage(String languageName, ScriptEngineFactory scriptEngineFactory,
      ActivityScriptFactory activityScriptFactory) {
    ScriptLanguage language = new ScriptLanguage(scriptEngineFactory, activityScriptFactory);
    languages.add(language);

    nameToLanguage.put(scriptEngineFactory.getLanguageName(), language);
    for (String name : scriptEngineFactory.getNames()) {
      nameToLanguage.put(name, language);
    }
    for (String extension : scriptEngineFactory.getExtensions()) {
      extensionToLanguage.put(extension, language);
    }

    if (activityScriptFactory != null) {
      activityScriptFactory.initialize();
    }
  }

  @Override
  public Set<String> getLanguageNames() {
    Set<String> languages = Sets.newHashSet(nameToLanguage.keySet());

    return languages;
  }

  @Override
  public void executeSimpleScript(String languageName, String script) {
    executeScript(languageName, script, EMPTY_BINDINGS);
  }

  @Override
  public void executeScript(String languageName, String script, Map<String, Object> bindings) {
    executeScriptByName(languageName, new StringScriptSource(script), bindings);
  }

  @Override
  public void executeScriptByName(String languageName, ScriptSource scriptSource,
      Map<String, Object> bindings) {
    ScriptLanguage scriptLanguage = nameToLanguage.get(languageName);
    if (scriptLanguage == null) {
      throw new InteractiveSpacesException(String.format(
          "Unable to find a script engine for language %s", languageName));
    }

    executeScript(scriptLanguage, scriptSource, bindings, "language", languageName);
  }

  @Override
  public void executeScriptByExtension(String extension, ScriptSource scriptSource,
      Map<String, Object> bindings) {
    ScriptLanguage scriptLanguage = extensionToLanguage.get(extension);
    if (scriptLanguage == null) {
      throw new InteractiveSpacesException(String.format(
          "Unable to find a script engine for language extension %s", extension));
    }

    executeScript(scriptLanguage, scriptSource, bindings, "extension", extension);
  }

  /**
   * Execute a script.
   *
   * @param scriptLanguage
   *          the scripting language that will run the script
   * @param scriptSource
   *          the source of the script
   * @param bindings
   *          the extra bindings for the script
   * @param idType
   *          the type of language ID which was used
   * @param languageId
   *          the ID used to get the language
   *
   */
  private void executeScript(ScriptLanguage scriptLanguage, ScriptSource scriptSource,
      Map<String, Object> bindings, String idType, String languageId) {

    ScriptEngineFactory factory = scriptLanguage.getScriptEngineFactory();
    if (factory == null) {
      throw new InteractiveSpacesException(String.format(
          "Unable to find a script engine for %s %s", idType, languageId));
    }

    final ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
    Thread.currentThread().setContextClassLoader(JavaxScriptScriptService.class.getClassLoader());
    try {
      ScriptEngine engine = factory.getScriptEngine();
      if (engine != null) {
        engine.setBindings(new SimpleBindings(bindings), ScriptContext.GLOBAL_SCOPE);

        engine.eval(scriptSource.getScriptContents());
      }
    } catch (ScriptException ex) {
      ex.printStackTrace();
    } finally {
      Thread.currentThread().setContextClassLoader(oldClassLoader);
    }
  }

  @Override
  public Script newSimpleScript(String languageName, String script) {
    return newScriptByName(languageName, new StringScriptSource(script));
  }

  @Override
  public Script newScriptByName(String languageName, ScriptSource scriptSource) {
    ScriptLanguage scriptLanguage = nameToLanguage.get(languageName);
    if (scriptLanguage == null) {
      throw new InteractiveSpacesException(String.format(
          "Unable to find a script engine for language %s", languageName));
    }

    return newScript(scriptLanguage, scriptSource);
  }

  @Override
  public Script newScriptByExtension(String extension, ScriptSource scriptSource) {
    ScriptLanguage scriptLanguage = extensionToLanguage.get(extension);
    if (scriptLanguage == null) {
      throw new InteractiveSpacesException(String.format(
          "Unable to find a script engine for language extension %s", extension));
    }

    return newScript(scriptLanguage, scriptSource);
  }

  /**
   * Create the new script object.
   *
   * @param scriptLanguage
   *          the script language
   * @param scriptSource
   *          source of the script
   *
   * @return the script
   */
  private Script newScript(ScriptLanguage scriptLanguage, ScriptSource scriptSource) {
    ScriptEngine engine = scriptLanguage.getScriptEngineFactory().getScriptEngine();
    if (engine instanceof Compilable) {
      return new CompiledScriptScript((Compilable) engine, scriptSource);
    } else {
      return new ScriptEngineScript(engine, scriptSource);
    }
  }

  @Override
  public ActivityScriptWrapper getActivityByName(String languageName, String objectName,
      ScriptSource script, ActivityFilesystem activityFilesystem, Configuration configuration) {
    ScriptLanguage language = nameToLanguage.get(languageName);
    if (language == null) {
      throw new InteractiveSpacesException(String.format(
          "Unable to find a script engine for language %s", languageName));
    }

    return getActivity(languageName, objectName, script, activityFilesystem, configuration,
        language, "name");
  }

  @Override
  public ActivityScriptWrapper getActivityByExtension(String extension, String objectName,
      ScriptSource script, ActivityFilesystem activityFilesystem, Configuration configuration) {
    ScriptLanguage language = extensionToLanguage.get(extension);
    if (language == null) {
      throw new InteractiveSpacesException(String.format(
          "Unable to find a script engine for language %s", extension));
    }

    return getActivity(extension, objectName, script, activityFilesystem, configuration, language,
        "extension");
  }

  /**
   * Get the activity, if possible, from the {@link ActivityScriptFactory}.
   *
   * @param languageId
   * @param objectName
   * @param script
   * @param language
   * @return
   */
  private ActivityScriptWrapper getActivity(String languageId, String objectName,
      ScriptSource script, ActivityFilesystem activityFilesystem, Configuration configuration,
      ScriptLanguage language, String nameType) {
    ActivityScriptFactory factory = language.getActivityScriptFactory();
    if (factory == null) {
      throw new InteractiveSpacesException(String.format(
          "Unable to find an activity script factory for %s %s", nameType, languageId));
    }

    return factory.getActivity(objectName, script, activityFilesystem, configuration);
  }

  @Override
  public void setSpaceEnvironment(InteractiveSpacesEnvironment spaceEnvironment) {
    this.spaceEnvironment = spaceEnvironment;
  }

  /**
   * Everything that can be done with a scripting language.
   *
   * @author Keith M. Hughes
   */
  private static class ScriptLanguage {
    /**
     * A script language factory, if any, for the language.
     *
     * <p>
     * Can be {@code null}.
     */
    private ScriptEngineFactory scriptEngineFactory;

    /**
     * The factory for creating {@link Activity} instances.
     *
     * <p>
     * Can be {@code null}.
     */
    private ActivityScriptFactory activityScriptFactory;

    public ScriptLanguage(ScriptEngineFactory scriptEngineFactory,
        ActivityScriptFactory activityScriptFactory) {
      this.scriptEngineFactory = scriptEngineFactory;
      this.activityScriptFactory = activityScriptFactory;
    }

    /**
     * @return the scriptEngineFactory
     */
    public ScriptEngineFactory getScriptEngineFactory() {
      return scriptEngineFactory;
    }

    /**
     * @return the activityScriptFactory
     */
    public ActivityScriptFactory getActivityScriptFactory() {
      return activityScriptFactory;
    }
  }
}
