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

package interactivespaces.master.ui.internal.web.activity;

import interactivespaces.domain.basic.Activity;
import interactivespaces.expression.FilterExpression;
import interactivespaces.master.server.services.ActivityRepository;
import interactivespaces.master.server.services.EntityNotFoundInteractiveSpacesException;
import interactivespaces.master.server.ui.JsonSupport;
import interactivespaces.master.server.ui.UiActivityManager;
import interactivespaces.master.server.ui.UiLiveActivity;
import interactivespaces.master.ui.internal.web.BaseActiveSpaceMasterController;
import interactivespaces.master.ui.internal.web.UiUtilities;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Spring MVC controller for activity operations.
 * 
 * @author Keith M. Hughes
 */
@Controller
public class ActivityController extends BaseActiveSpaceMasterController {

	/**
	 * Repository for activity entities.
	 */
	private ActivityRepository activityRepository;

	/**
	 * Display a list of all activities.
	 * 
	 * @return Model and view for controller list display.
	 */
	@RequestMapping("/activity/all.html")
	public ModelAndView listActivities() {
		List<Activity> activities = Lists.newArrayList(activityRepository
				.getAllActivities());
		Collections.sort(activities, UiUtilities.ACTIVITY_BY_NAME_COMPARATOR);

		ModelAndView mav = getModelAndView();

		mav.setViewName("activity/ActivityViewAll");
		mav.addObject("activities", activities);

		return mav;
	}

	@RequestMapping(value = "/activity/{id}/view.html", method = RequestMethod.GET)
	public ModelAndView viewActivity(@PathVariable String id) {
		Activity activity = activityRepository.getActivityById(id);
		if (activity != null) {
			List<UiLiveActivity> liveactivities = uiControllerManager
					.getUiLiveActivities(activityRepository
							.getLiveActivitiesByActivity(activity));
			Collections.sort(liveactivities,
					UiUtilities.UI_LIVE_ACTIVITY_BY_NAME_COMPARATOR);

			ModelAndView mav = getModelAndView();
			mav.setViewName("activity/ActivityView");
			mav.addObject("activity", activity);
			mav.addObject("liveactivities", liveactivities);

			mav.addObject("metadata",
					UiUtilities.getMetadataView(activity.getMetadata()));

			return mav;
		} else {
			ModelAndView mav = getModelAndView();
			mav.setViewName("activity/ActivityNonexistent");

			return mav;
		}
	}

	@RequestMapping(value = "/activity/{id}/delete.html", method = RequestMethod.GET)
	public ModelAndView deleteActivity(@PathVariable String id) {
		ModelAndView mav = getModelAndView();
		try {
			uiActivityManager.deleteActivity(id);

			mav.clear();
			mav.setViewName("redirect:/activity/all.html");
		} catch (EntityNotFoundInteractiveSpacesException e) {
			mav.setViewName("activity/ActivityNonexistent");
		}

		return mav;
	}

	@RequestMapping(value = "/activity/all.json", method = RequestMethod.GET)
	public @ResponseBody
	Map<String, ? extends Object> getAllActivities(
			@RequestParam(value = "filter", required = false) String filter) {
		List<Map<String, Object>> data = Lists.newArrayList();

		try {
			FilterExpression filterExpression = expressionFactory
					.getFilterExpression(filter);

			for (Activity activity : activityRepository
					.getActivities(filterExpression)) {
				Map<String, Object> d = Maps.newHashMap();
				data.add(d);

				d.put("id", activity.getId());
				d.put("identifyingName", activity.getIdentifyingName());
				d.put("version", activity.getVersion());
				d.put("name", activity.getName());
				d.put("description", activity.getDescription());
				d.put("metadata", activity.getMetadata());
				d.put("lastUploadDate", activity.getLastUploadDate());
			}

			return JsonSupport.getSuccessJsonResponse(data);
		} catch (Exception e) {
			spacesEnvironment.getLog().error(
					"Attempt to get activity data failed", e);

			return JsonSupport.getFailureJsonResponse("call failed");
		}
	}

	@RequestMapping(value = "/activity/{id}/deploy.json", method = RequestMethod.GET)
	public @ResponseBody
	Map<String, ? extends Object> deployActivities(@PathVariable String id) {
		try {
			uiControllerManager.deployAllActivityInstances(id);

			return JsonSupport.getSimpleSuccessJsonResponse();
		} catch (EntityNotFoundInteractiveSpacesException e) {
			return getNoSuchActivityResult();
		}
	}

	@RequestMapping(value = "/activity/{id}/metadata.json", method = RequestMethod.POST)
	public @ResponseBody
	Map<String, ? extends Object> modifyLiveActivityMetadata(
			@PathVariable String id, @RequestBody Object metadataCommandObj,
			HttpServletResponse response) {

		if (Map.class.isAssignableFrom(metadataCommandObj.getClass())) {
			@SuppressWarnings("unchecked")
			Map<String, Object> metadataCommand = (Map<String, Object>) metadataCommandObj;

			return uiActivityManager.updateActivityMetadata(id,
					metadataCommand);
		} else {
			return JsonSupport
					.getFailureJsonResponse(JsonSupport.MESSAGE_SPACE_CALL_ARGS_NOMAP);
		}
	}

	/**
	 * Prepare a JSON result for there not being a particular activity.
	 * 
	 * @return
	 */
	private Map<String, Object> getNoSuchActivityResult() {
		return JsonSupport
				.getFailureJsonResponse(UiActivityManager.MESSAGE_SPACE_DOMAIN_ACTIVITY_UNKNOWN);
	}

	/**
	 * @param activityRepository
	 *            the activityRepository to set
	 */
	public void setActivityRepository(ActivityRepository activityRepository) {
		this.activityRepository = activityRepository;
	}
}
