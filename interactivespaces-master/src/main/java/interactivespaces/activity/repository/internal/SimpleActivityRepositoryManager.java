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

package interactivespaces.activity.repository.internal;

import interactivespaces.activity.repository.ActivityRepositoryManager;
import interactivespaces.activity.repository.ActivityRepositoryStorageManager;
import interactivespaces.domain.basic.Activity;
import interactivespaces.domain.basic.ActivityDependency;
import interactivespaces.domain.basic.pojo.SimpleActivity;
import interactivespaces.domain.support.ActivityDescription;
import interactivespaces.domain.support.ActivityDescriptionReader;
import interactivespaces.domain.support.ActivityUtils;
import interactivespaces.domain.support.JdomActivityDescriptionReader;
import interactivespaces.master.server.services.ActivityRepository;

import java.io.InputStream;
import java.util.Date;

/**
 * A simple implementation of the {@link ActivityRepositoryManager}.
 * 
 * @author Keith M. Hughes
 */
public class SimpleActivityRepositoryManager implements
		ActivityRepositoryManager {
	/**
	 * Repository for activities.
	 */
	private ActivityRepository activityRepository;

	/**
	 * Storage manager for the repository.
	 */
	private ActivityRepositoryStorageManager repositoryStorageManager;

	@Override
	public Activity addActivity(InputStream activityStream) {
		String stageHandle = repositoryStorageManager
				.stageActivity(activityStream);
		try {
			InputStream activityDescriptionStream = repositoryStorageManager
					.getStagedActivityDescription(stageHandle);
			ActivityDescriptionReader reader = new JdomActivityDescriptionReader();
			ActivityDescription activityDescription = reader
					.readDescription(activityDescriptionStream);

			repositoryStorageManager.addActivity(activityDescription,
					stageHandle);

			// TODO(keith): Might want to edit what it gives to the import so
			// this may need
			// to move.
			Activity finalActivity = activityRepository
					.getActivityByNameAndVersion(
							activityDescription.getIdentifyingName(),
							activityDescription.getVersion());
			if (finalActivity == null) {
				finalActivity = activityRepository.newActivity();
				finalActivity.setIdentifyingName(activityDescription
						.getIdentifyingName());
				finalActivity
						.setVersion(activityDescription.getVersion());
			}
			
			ActivityUtils.copy(activityDescription, finalActivity);
			finalActivity.setLastUploadDate(new Date());
			
			copyDependencies(activityDescription, finalActivity);

			activityRepository.saveActivity(finalActivity);
			
			return finalActivity;
		} finally {
			repositoryStorageManager.removeStagedActivity(stageHandle);
		}

	}

	/**
	 * Copy all activity dependencies into the final activity.
	 * 
	 * @param activityDescription
	 * @param finalActivity
	 */
	private void copyDependencies(SimpleActivity activityDescription,
			Activity finalActivity) {
		for (ActivityDependency dependency : activityDescription.getDependencies()) {
			ActivityDependency newDependency = activityRepository.newActivityDependency();
			
			newDependency.setName(dependency.getName());
			newDependency.setMinimumVersion(dependency.getMinimumVersion());
			newDependency.setMaximumVersion(dependency.getMaximumVersion());
			newDependency.setRequired(dependency.isRequired());
			
			finalActivity.addDependency(newDependency);
		}
	}

	/**
	 * @param activityRepository
	 *            the activityRepository to set
	 */
	public void setActivityRepository(
			ActivityRepository activityRepository) {
		this.activityRepository = activityRepository;
	}

	/**
	 * @param repositoryStorageManager
	 *            the repositoryStorageManager to set
	 */
	public void setRepositoryStorageManager(
			ActivityRepositoryStorageManager repositoryStorageManager) {
		this.repositoryStorageManager = repositoryStorageManager;
	}

}
