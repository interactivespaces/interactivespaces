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

package interactivespaces.service.scheduler;

import interactivespaces.service.SupportedService;

import java.util.Date;
import java.util.Map;

/**
 * A scheduler
 * 
 * @author Keith M. Hughes
 */
public interface SchedulerService extends SupportedService {

	/**
	 * The name of the service.
	 */
	public static final String SERVICE_NAME = "scheduler";

	/**
	 * Schedule a runnable for the future.
	 * 
	 * @param jobName
	 *            The name of the job.
	 * 
	 * @param groupName
	 *            The name of the group the job will run in. Can be {@code null}
	 *            to be in the default group.
	 * 
	 * @param runnable
	 *            The runnable to run in the future.
	 * 
	 * @param when
	 *            The date when the job should fire.
	 */
	void schedule(String jobName, String groupName, Runnable runnable, Date when);

	/**
	 * Schedule a runnable for the future.
	 * 
	 * @param jobName
	 *            the name of the job
	 * 
	 * @param groupName
	 *            the name of the group the job will run in, can be {@code null}
	 *            to be in the default group
	 * 
	 * @param id
	 *            id of the script
	 * 
	 * @param schedule
	 *            The date when the job should fire.
	 */
	void scheduleScriptWithCron(String jobName, String groupName, String id, String schedule);

	/**
	 * Add entities to the scheduler that can be used for scheduled jobs.
	 * 
	 * <p>
	 * Entities already registered with a given name will be replaced by new
	 * entities with the same name.
	 * 
	 * @param entities
	 *            map of entity names to entities
	 */
	void addSchedulingEntities(Map<String, Object> entities);
}
