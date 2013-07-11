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

package interactivespaces.resource.repository.internal;

import com.google.common.collect.Lists;

import interactivespaces.domain.basic.Activity;
import interactivespaces.domain.basic.ActivityDependency;
import interactivespaces.domain.basic.pojo.SimpleActivity;
import interactivespaces.domain.support.ActivityDescription;
import interactivespaces.domain.support.ActivityDescriptionReader;
import interactivespaces.domain.support.ActivityUtils;
import interactivespaces.domain.support.JdomActivityDescriptionReader;
import interactivespaces.master.server.services.ActivityRepository;
import interactivespaces.resource.repository.ActivityRepositoryManager;
import interactivespaces.resource.repository.ResourceRepositoryStorageManager;

import java.io.InputStream;
import java.util.Date;
import java.util.List;

/**
 * A simple implementation of the {@link ActivityRepositoryManager}.
 *
 * @author Keith M. Hughes
 */
public class SimpleActivityRepositoryManager implements ActivityRepositoryManager {
  /**
   * Repository for activities.
   */
  private ActivityRepository activityRepository;

  /**
   * Storage manager for the repository.
   */
  private ResourceRepositoryStorageManager repositoryStorageManager;

  @Override
  public Activity addActivity(InputStream activityStream) {
    String stageHandle = repositoryStorageManager.stageResource(activityStream);
    try {
      InputStream activityDescriptionStream =
          repositoryStorageManager.getStagedResourceDescription("activity.xml", stageHandle);
      ActivityDescriptionReader reader = new JdomActivityDescriptionReader();
      ActivityDescription activityDescription = reader.readDescription(activityDescriptionStream);

      repositoryStorageManager.addResource(activityDescription.getIdentifyingName(),
          activityDescription.getVersion(), stageHandle);

      // TODO(keith): Might want to edit what it gives to the import so
      // this may need to move.
      Activity finalActivity =
          activityRepository.getActivityByNameAndVersion(activityDescription.getIdentifyingName(),
              activityDescription.getVersion());
      if (finalActivity == null) {
        finalActivity = activityRepository.newActivity();
        finalActivity.setIdentifyingName(activityDescription.getIdentifyingName());
        finalActivity.setVersion(activityDescription.getVersion());
      }

      ActivityUtils.copy(activityDescription, finalActivity);
      finalActivity.setLastUploadDate(new Date());

      copyDependencies(activityDescription, finalActivity);

      activityRepository.saveActivity(finalActivity);

      return finalActivity;
    } finally {
      repositoryStorageManager.removeStagedReource(stageHandle);
    }

  }

  /**
   * Copy all activity dependencies into the final activity.
   *
   * @param activityDescription
   *          the activity which is being brought in
   * @param finalActivity
   *          the activity stored in the database
   */
  private void copyDependencies(SimpleActivity activityDescription, Activity finalActivity) {
    List<ActivityDependency> finalDependencies = Lists.newArrayList();
    for (ActivityDependency dependency : activityDescription.getDependencies()) {
      ActivityDependency newDependency = activityRepository.newActivityDependency();

      newDependency.setName(dependency.getName());
      newDependency.setMinimumVersion(dependency.getMinimumVersion());
      newDependency.setMaximumVersion(dependency.getMaximumVersion());
      newDependency.setRequired(dependency.isRequired());

      finalDependencies.add(newDependency);
    }

    finalActivity.setDependencies(finalDependencies);
  }

  /**
   * @param activityRepository
   *          the activityRepository to set
   */
  public void setActivityRepository(ActivityRepository activityRepository) {
    this.activityRepository = activityRepository;
  }

  /**
   * @param repositoryStorageManager
   *          the repositoryStorageManager to set
   */
  public void
      setRepositoryStorageManager(ResourceRepositoryStorageManager repositoryStorageManager) {
    this.repositoryStorageManager = repositoryStorageManager;
  }

}
