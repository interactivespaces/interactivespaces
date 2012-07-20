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

package interactivespaces.service.scheduler.internal;

import interactivespaces.InteractiveSpacesException;
import interactivespaces.service.scheduler.SchedulerService;
import interactivespaces.system.InteractiveSpacesEnvironment;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.impl.StdSchedulerFactory;

/**
 * A {@link SchedulerService} which uses quartz.
 * 
 * @author Keith M. Hughes
 */
public class QuartzSchedulerService implements SchedulerService {

	/**
	 * JobMap property for the runnable which will run.
	 */
	public static final String JOB_MAP_PROPERTY_RUNNABLE = "runnable";

	/**
	 * JobMap property for the ID of the script which will run.
	 */
	public static final String JOB_MAP_PROPERTY_SCRIPT_ID = "runnable";

	/**
	 * JobMap property for the logger to use.
	 */
	public static final String JOB_MAP_PROPERTY_LOGGER = "logger";

	/**
	 * JobMap property for the logger to use.
	 */
	public static final String JOB_MAP_PROPERTY_SPACE_ENVIRONMENT = "spaceEnvironment";

	/**
	 * The quartz scheduler.
	 */
	private Scheduler scheduler;

	/**
	 * Spaces environment the scheduler is running in.
	 * 
	 * <p>
	 * TODO(keith): Make it so the thread pool is separate.
	 */
	private InteractiveSpacesEnvironment spaceEnvironment;

	@Override
	public void startup() {
		try {
			// TODO(keith): Get Interactive Spaces thread pool in here.
			SchedulerFactory schedulerFactory = new StdSchedulerFactory();
			scheduler = schedulerFactory.getScheduler();

			scheduler.start();

			spaceEnvironment.getServiceRegistry().registerService(SERVICE_NAME,
					this);
		} catch (SchedulerException e) {
			throw new InteractiveSpacesException(
					"Could not start Interactive Spaces scheduler", e);
		}
	}

	@Override
	public void shutdown() {
		try {
			scheduler.shutdown();
		} catch (SchedulerException e) {
			throw new InteractiveSpacesException(
					"Could not shutdown Interactive Spaces scheduler", e);
		}
	}

	@Override
	public void addSchedulingEntities(Map<String, Object> entities) {
		try {
			scheduler.getContext().putAll(entities);
		} catch (SchedulerException e) {
			throw new InteractiveSpacesException(
					"Unable to add map of entities to the scheduler", e);
		}
	}

	@Override
	public void schedule(String jobName, String groupName, Runnable runnable,
			Date when) {
		try {
			JobDetail detail = JobBuilder.newJob(SimpleSchedulerJob.class)
					.withIdentity(jobName, groupName).build();
			detail.getJobDataMap().put(JOB_MAP_PROPERTY_RUNNABLE, runnable);
			detail.getJobDataMap().put(JOB_MAP_PROPERTY_LOGGER,
					spaceEnvironment.getLog());

			Trigger trigger = TriggerBuilder.newTrigger()
					.withIdentity(TriggerKey.triggerKey(jobName, groupName))
					.startAt(when).build();
			scheduler.scheduleJob(detail, trigger);

			spaceEnvironment.getLog().info(
					String.format("Scheduled job %s:%s for %s\n", groupName,
							jobName,
							new SimpleDateFormat("MM/dd/yyyy@HH:mm:ss")
									.format(when)));
		} catch (SchedulerException e) {
			throw new InteractiveSpacesException(String.format(
					"Unable to schedule job %s:%s", groupName, jobName), e);
		}
	}

	@Override
	public void scheduleScriptWithCron(String jobName, String groupName,
			String id, String schedule) {
		try {
			JobDetail detail = JobBuilder
					.newJob(SimpleScriptSchedulerJob.class)
					.withIdentity(jobName, groupName).build();
			JobDataMap jobDataMap = detail.getJobDataMap();
			jobDataMap.put(JOB_MAP_PROPERTY_SCRIPT_ID, id);
			jobDataMap.put(JOB_MAP_PROPERTY_LOGGER, spaceEnvironment.getLog());

			CronTrigger trigger = TriggerBuilder.newTrigger()
					.withIdentity(TriggerKey.triggerKey(jobName, groupName))
					.withSchedule(CronScheduleBuilder.cronSchedule(schedule))
					.build();

			scheduler.scheduleJob(detail, trigger);
		} catch (Exception e) {
			throw new InteractiveSpacesException(String.format(
					"Unable to schedule job %s:%s", groupName, jobName), e);
		}
	}

	/**
	 * Set the Interactive Spaces environment to be used.
	 * 
	 * @param spaceEnvironment
	 *            the spaceEnvironment to set
	 */
	public void setSpaceEnvironment(
			InteractiveSpacesEnvironment spaceEnvironment) {
		this.spaceEnvironment = spaceEnvironment;
	}

	/**
	 * Unset the Interactive Spaces environment being used.
	 * 
	 * @param spaceEnvironment
	 *            the previous environment that was used.
	 */
	public void unsetSpaceEnvironment(
			InteractiveSpacesEnvironment spaceEnvironment) {
		this.spaceEnvironment = spaceEnvironment;
	}

	/**
	 * The job which the scheduler will run.
	 * 
	 * @author Keith M. Hughes
	 */
	public static class SimpleSchedulerJob implements Job {

		@Override
		public void execute(JobExecutionContext context)
				throws JobExecutionException {
			try {
				Runnable runner = (Runnable) context.getJobDetail()
						.getJobDataMap().get(JOB_MAP_PROPERTY_RUNNABLE);
				runner.run();
			} catch (Exception e) {
				((Log) context.getJobDetail().getJobDataMap()
						.get(JOB_MAP_PROPERTY_LOGGER)).error(
						"Could not run scheduled job", e);
			}
		}

	}

	/**
	 * The job which the scheduler will run.
	 * 
	 * @author Keith M. Hughes
	 */
	public static class SimpleScriptSchedulerJob implements Job {

		@Override
		public void execute(JobExecutionContext context)
				throws JobExecutionException {
			try {
				String scriptId = (String) context.getJobDetail()
						.getJobDataMap().get(JOB_MAP_PROPERTY_SCRIPT_ID);
				((Log) context.getJobDetail().getJobDataMap()
						.get(JOB_MAP_PROPERTY_LOGGER)).info(String.format(
						"Running script %s", scriptId));
			} catch (Exception e) {
				((Log) context.getJobDetail().getJobDataMap()
						.get(JOB_MAP_PROPERTY_LOGGER)).error(
						"Could not run scheduled job", e);
			}
		}
	}

}
