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

package interactivespaces.master.ui.internal.web.liveactivity;

import interactivespaces.domain.basic.LiveActivity;
import interactivespaces.domain.basic.LiveActivityGroup;
import interactivespaces.expression.FilterExpression;
import interactivespaces.master.server.services.ActivityRepository;
import interactivespaces.master.server.services.ControllerRepository;
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
 * Spring MVC controller for working with live activities.
 * 
 * @author Keith M. Hughes
 */
@Controller
public class LiveActivityController extends BaseActiveSpaceMasterController {

	/**
	 * Repository for activity entities.
	 */
	private ActivityRepository activityRepository;

	/**
	 * Repository for controller entities.
	 */
	private ControllerRepository controllerRepository;

	/**
	 * Display a list of all installed activities.
	 * 
	 * @return Model and view for controller list display.
	 */
	@RequestMapping("/liveactivity/all.html")
	public ModelAndView listActivities() {
		List<UiLiveActivity> liveactivities = uiControllerManager
				.getAllUiLiveActivities();
		Collections.sort(liveactivities,
				UiUtilities.UI_LIVE_ACTIVITY_BY_NAME_COMPARATOR);

		ModelAndView mav = getModelAndView();
		mav.setViewName("liveactivity/LiveActivityViewAll");
		mav.addObject("liveactivities", liveactivities);

		return mav;
	}

	@RequestMapping(value = "/liveactivity/{id}/view.html", method = RequestMethod.GET)
	public ModelAndView viewActivity(@PathVariable String id) {
		ModelAndView mav = getModelAndView();

		UiLiveActivity liveactivity = uiControllerManager.getUiLiveActivity(id);
		if (liveactivity != null) {
			mav.setViewName("liveactivity/LiveActivityView");
			mav.addObject("liveactivity", liveactivity);
			mav.addObject("metadata", UiUtilities.getMetadataView(liveactivity
					.getActivity().getMetadata()));

			List<LiveActivityGroup> groups = Lists
					.newArrayList(activityRepository
							.getLiveActivityGroupsByLiveActivity(liveactivity
									.getActivity()));
			Collections.sort(groups,
					UiUtilities.LIVE_ACTIVITY_GROUP_BY_NAME_COMPARATOR);
			mav.addObject("liveactivitygroups", groups);

		} else {
			mav.setViewName("liveactivity/LiveActivityNonexistent");
		}

		return mav;
	}

	@RequestMapping(value = "/liveactivity/all.json", method = RequestMethod.GET)
	public @ResponseBody
	Map<String, ? extends Object> listActivitiesJson(
			@RequestParam(value = "filter", required = false) String filter) {
		List<Map<String, Object>> data = Lists.newArrayList();

		try {
			FilterExpression filterExpression = expressionFactory
					.getFilterExpression(filter);

			for (LiveActivity activity : activityRepository
					.getLiveActivities(filterExpression)) {
				Map<String, Object> activityData = Maps.newHashMap();

				uiActivityManager.getLiveActivityViewJsonData(activity,
						activityData);

				data.add(activityData);
			}

			return JsonSupport.getSuccessJsonResponse(data);
		} catch (Exception e) {
			spacesEnvironment.getLog().error(
					"Attempt to get live activity data failed", e);

			return JsonSupport.getFailureJsonResponse("call failed");
		}
	}

	@RequestMapping(value = "/liveactivity/{id}/view.json", method = RequestMethod.GET)
	public @ResponseBody
	Map<String, ? extends Object> viewLiveActivityJson(@PathVariable String id) {
		LiveActivity liveactivity = activityRepository.getLiveActivityById(id);
		if (liveactivity != null) {
			Map<String, Object> data = Maps.newHashMap();

			uiActivityManager.getLiveActivityViewJsonData(liveactivity, data);

			return JsonSupport.getSuccessJsonResponse(data);
		} else {
			return getNoSuchLiveActivityResult();
		}
	}

	@RequestMapping(value = "/liveactivity/{id}/deploy.json", method = RequestMethod.GET)
	public @ResponseBody
	Map<String, ? extends Object> deployLiveActivity(@PathVariable String id) {
		try {
			uiControllerManager.deployLiveActivity(id);

			return JsonSupport.getSimpleSuccessJsonResponse();
		} catch (EntityNotFoundInteractiveSpacesException e) {
			return getNoSuchLiveActivityResult();
		}
	}

	@RequestMapping(value = "/liveactivity/{id}/configure.json", method = RequestMethod.GET)
	public @ResponseBody
	Map<String, ? extends Object> configureLiveActivity(@PathVariable String id) {
		try {
			uiControllerManager.configureLiveActivity(id);

			return JsonSupport.getSimpleSuccessJsonResponse();
		} catch (EntityNotFoundInteractiveSpacesException e) {
			return getNoSuchLiveActivityResult();
		}
	}

	@RequestMapping(value = "/liveactivity/{id}/configuration.json", method = RequestMethod.GET)
	public @ResponseBody
	Map<String, ? extends Object> getLiveActivityConfiguration(
			@PathVariable String id) {
		try {
			return JsonSupport.getSuccessJsonResponse(uiActivityManager
					.getLiveActivityConfiguration(id));
		} catch (EntityNotFoundInteractiveSpacesException e) {
			return getNoSuchLiveActivityResult();
		}
	}

	@RequestMapping(value = "/liveactivity/{id}/configuration.json", method = RequestMethod.POST)
	public @ResponseBody
	Map<String, ? extends Object> setLiveActivityConfiguration(
			@PathVariable String id, @RequestBody Object config,
			HttpServletResponse response) {

		try {
			if (Map.class.isAssignableFrom(config.getClass())) {
				@SuppressWarnings("unchecked")
				Map<String, String> map = (Map<String, String>) config;

				uiActivityManager.configureLiveActivity(id, map);

				return JsonSupport.getSimpleSuccessJsonResponse();
			} else {
				return JsonSupport
						.getFailureJsonResponse(JsonSupport.MESSAGE_SPACE_CALL_ARGS_NOMAP);
			}
		} catch (EntityNotFoundInteractiveSpacesException e) {
			return getNoSuchLiveActivityResult();
		}
	}

	@RequestMapping(value = "/liveactivity/{id}/metadata.json", method = RequestMethod.POST)
	public @ResponseBody
	Map<String, ? extends Object> modifyLiveActivityMetadata(
			@PathVariable String id, @RequestBody Object metadataCommandObj,
			HttpServletResponse response) {

		if (Map.class.isAssignableFrom(metadataCommandObj.getClass())) {
			@SuppressWarnings("unchecked")
			Map<String, Object> metadataCommand = (Map<String, Object>) metadataCommandObj;

			return uiActivityManager.updateLiveActivityMetadata(id,
					metadataCommand);
		} else {
			return JsonSupport
					.getFailureJsonResponse(JsonSupport.MESSAGE_SPACE_CALL_ARGS_NOMAP);
		}
	}

	@RequestMapping(value = "/liveactivity/{id}/startup.json", method = RequestMethod.GET)
	public @ResponseBody
	Map<String, ? extends Object> startupActivity(@PathVariable String id) {
		try {
			uiControllerManager.startupLiveActivity(id);

			return JsonSupport.getSimpleSuccessJsonResponse();
		} catch (EntityNotFoundInteractiveSpacesException e) {
			return getNoSuchLiveActivityResult();
		}
	}

	@RequestMapping(value = "/liveactivity/{id}/activate.json", method = RequestMethod.GET)
	public @ResponseBody
	Map<String, ? extends Object> activateActivity(@PathVariable String id) {
		try {
			uiControllerManager.activateLiveActivity(id);

			return JsonSupport.getSimpleSuccessJsonResponse();
		} catch (EntityNotFoundInteractiveSpacesException e) {
			return getNoSuchLiveActivityResult();
		}
	}

	@RequestMapping(value = "/liveactivity/{id}/deactivate.json", method = RequestMethod.GET)
	public @ResponseBody
	Map<String, ? extends Object> deactivateActivity(@PathVariable String id) {
		try {
			uiControllerManager.deactivateLiveActivity(id);

			return JsonSupport.getSimpleSuccessJsonResponse();
		} catch (EntityNotFoundInteractiveSpacesException e) {
			return getNoSuchLiveActivityResult();
		}
	}

	@RequestMapping(value = "/liveactivity/{id}/shutdown.json", method = RequestMethod.GET)
	public @ResponseBody
	Map<String, ? extends Object> shutdownActivity(@PathVariable String id) {
		try {
			uiControllerManager.shutdownLiveActivity(id);

			return JsonSupport.getSimpleSuccessJsonResponse();
		} catch (EntityNotFoundInteractiveSpacesException e) {
			return getNoSuchLiveActivityResult();
		}
	}

	@RequestMapping(value = "/liveactivity/{id}/status.json", method = RequestMethod.GET)
	public @ResponseBody
	Map<String, ? extends Object> statusActivity(@PathVariable String id) {
		LiveActivity activity = activityRepository.getLiveActivityById(id);
		if (activity != null) {
			// Get an update from the controller
			uiControllerManager.statusLiveActivity(id);

			Map<String, Object> statusData = Maps.newHashMap();

			uiActivityManager.getLiveActivityStatusJsonData(activity,
					statusData);

			return JsonSupport.getSuccessJsonResponse(statusData);
		} else {
			return getNoSuchLiveActivityResult();
		}
	}

	@RequestMapping(value = "/liveactivity/{id}/delete.html", method = RequestMethod.GET)
	public ModelAndView deleteActivity(@PathVariable String id) {
		ModelAndView mav = getModelAndView();
		try {
			uiActivityManager.deleteLiveActivity(id);

			mav.clear();
			mav.setViewName("redirect:/liveactivity/all.html");
		} catch (EntityNotFoundInteractiveSpacesException e) {
			mav.setViewName("liveactivity/LiveActivityNonexistent");
		}

		return mav;
	}

	@RequestMapping(value = "/liveactivity/{id}/remotedelete.json", method = RequestMethod.GET)
	public @ResponseBody
	Map<String, ? extends Object> remoteDeleteActivity(@PathVariable String id) {
		try {
			uiControllerManager.deleteLiveActivity(id);

			return JsonSupport.getSimpleSuccessJsonResponse();
		} catch (EntityNotFoundInteractiveSpacesException e) {
			return getNoSuchLiveActivityResult();
		}
	}

	/**
	 * Get a JSON error response for no such live activity.
	 * 
	 * @return the JSON result
	 */
	private Map<String, Object> getNoSuchLiveActivityResult() {
		return JsonSupport
				.getFailureJsonResponse(UiActivityManager.MESSAGE_SPACE_DOMAIN_LIVEACTIVITY_UNKNOWN);
	}

	/**
	 * @param activityRepository
	 *            the activityRepository to set
	 */
	public void setActivityRepository(ActivityRepository activityRepository) {
		this.activityRepository = activityRepository;
	}

	/**
	 * @param controllerRepository
	 *            the controllerRepository to set
	 */
	public void setControllerRepository(
			ControllerRepository controllerRepository) {
		this.controllerRepository = controllerRepository;
	}
}
