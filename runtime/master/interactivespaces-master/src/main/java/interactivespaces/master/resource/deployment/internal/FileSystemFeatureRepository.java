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

package interactivespaces.master.resource.deployment.internal;

import interactivespaces.master.resource.deployment.Feature;
import interactivespaces.master.resource.deployment.FeatureRepository;

import org.ros.exception.RosRuntimeException;
import org.ros.osgi.common.RosEnvironment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A repository where everything is stored in the file system.
 *
 * @author Keith M. Hughes
 */
public class FileSystemFeatureRepository implements FeatureRepository {

  /**
   * ROS Environment the repository is running in.
   */
  private RosEnvironment rosEnvironment;

  /**
   * A mapping of ID to feature.
   */
  private Map<String, SimpleFeature> featureById = new HashMap<String, SimpleFeature>();

  /**
   * A mapping of bundle name to file.
   */
  private Map<String, File> bundlesByName = new HashMap<String, File>();

  /**
   * The root pathname for the repository.
   */
  private String repositoryRootPathname;

  @Override
  public void startup() {
    repositoryRootPathname = rosEnvironment.getProperty("org.ros.deployment.repository.root");
    File repositoryRoot = new File(repositoryRootPathname);
    if (!repositoryRoot.isDirectory())
      throw new RosRuntimeException(String.format("Feature repository root %s is not a directory",
          repositoryRootPathname));

    for (File repositoryFile : repositoryRoot.listFiles(new FilenameFilter() {
      @Override
      public boolean accept(File dir, String name) {
        // For now only read Jars.
        return name.endsWith(".jar");
      }
    })) {
      addFeature(repositoryFile);
    }
  }

  @Override
  public void shutdown() {
    // Nothing to do right now.
  }

  @Override
  public List<Feature> getAllFeatures() {
    return new ArrayList<Feature>(featureById.values());
  }

  @Override
  public Feature getFeature(String id) {
    return featureById.get(id);
  }

  @Override
  public InputStream getFeatureBundleStream(String bundleName) {
    File bundleFile = bundlesByName.get(bundleName);

    if (bundleFile != null) {
      try {
        return new FileInputStream(bundleFile);
      } catch (FileNotFoundException e) {
        // Need ROS exception
        throw new RosRuntimeException("Could not load ROS feature bundle", e);
      }
    } else {
      return null;
    }
  }

  @Override
  public File getFeatureFile(String bundleName) {
    return bundlesByName.get(bundleName);
  }

  /**
   * Add a new feature to the repository.
   *
   * @param feature
   *          The new feature.
   */
  private void addFeature(File featureFile) {
    String name = featureFile.getName();
    String id = new String(name.substring(0, name.lastIndexOf("-")));

    SimpleFeature feature = new SimpleFeature(id);

    // For now only 1 bundle per feature. This will all change when there is a
    // feature which has a collection of bundle files.
    feature.addRootBundle(name);
    bundlesByName.put(name, featureFile);

    featureById.put(feature.getId(), feature);
  }

  /**
   * Set the Ros Environment the server should run in.
   *
   * @param rosEnvironment
   */
  public void setRosEnvironment(RosEnvironment rosEnvironment) {
    this.rosEnvironment = rosEnvironment;
  }

  /**
   * Remove the Ros Environment the server should run in.
   *
   * @param rosEnvironment
   */
  public void unsetRosEnvironment(RosEnvironment rosEnvironment) {
    this.rosEnvironment = null;
  }
}
