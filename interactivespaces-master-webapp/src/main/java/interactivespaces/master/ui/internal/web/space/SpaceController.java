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

package interactivespaces.master.ui.internal.web.space;

import interactivespaces.domain.basic.GroupLiveActivity;
import interactivespaces.domain.basic.LiveActivity;
import interactivespaces.domain.basic.LiveActivityGroup;
import interactivespaces.domain.space.Space;
import interactivespaces.expression.FilterExpression;
import interactivespaces.master.server.services.ActiveSpaceManager;
import interactivespaces.master.server.services.ActivityRepository;
import interactivespaces.master.server.services.EntityNotFoundInteractiveSpacesException;
import interactivespaces.master.server.ui.JsonSupport;
import interactivespaces.master.server.ui.UiLiveActivity;
import interactivespaces.master.server.ui.UiSpaceManager;
import interactivespaces.master.ui.internal.web.BaseActiveSpaceMasterController;
import interactivespaces.master.ui.internal.web.UiSpaceLiveActivityGroup;
import interactivespaces.master.ui.internal.web.UiUtilities;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import com.google.common.collect.Sets;

/**
 * A Spring MVC controller for spaces.
 * 
 * @author Keith M. Hughes
 */
@Controller
public class SpaceController extends BaseActiveSpaceMasterController {

	/**
	 * Repository for space entities.
	 */
	private ActivityRepository activityRepository;

	/**
	 * Manager for space operations.
	 */
	private ActiveSpaceManager activeSpaceManager;

	/**
	 * Display a list of all spaces.
	 * 
	 * @return Model and view for space list display.
	 */
	@RequestMapping("/space/all.html")
	public ModelAndView listActivities() {
		ModelAndView mav = getModelAndView();
		mav.setViewName("space/SpaceViewAll");
		List<Space> spaces = Lists.newArrayList(activityRepository.getAllSpaces());
		Collections.sort(spaces, UiUtilities.SPACE_BY_NAME_COMPARATOR);
		mav.addObject("spaces", spaces);

		return mav;
	}

	@RequestMapping(value = "/space/{id}/view.html", method = RequestMethod.GET)
	public ModelAndView viewSpace(@PathVariable String id) {
		ModelAndView mav = getModelAndView();
		Space space = activityRepository.getSpaceById(id);
		if (space != null) {
			mav.setViewName("space/SpaceView");
			mav.addObject("space", space);
			mav.addObject("metadata",
					UiUtilities.getMetadataView(space.getMetadata()));

			List<? extends LiveActivityGroup> liveActivityGroups = space
					.getActivityGroups();
			Collections.sort(liveActivityGroups,
					UiUtilities.LIVE_ACTIVITY_GROUP_BY_NAME_COMPARATOR);
			mav.addObject("liveActivityGroups", liveActivityGroups);

			List<? extends Space> subspaces = space.getSpaces();
			Collections.sort(subspaces, UiUtilities.SPACE_BY_NAME_COMPARATOR);
			mav.addObject("subspaces", subspaces);

			List<Space> cspaces = Lists.newArrayList(activityRepository
					.getSpacesBySubspace(space));
			Collections.sort(cspaces, UiUtilities.SPACE_BY_NAME_COMPARATOR);
			mav.addObject("cspaces", cspaces);
		} else {
			mav.setViewName("space/SpaceNonexistent");
		}

		return mav;
	}

	@RequestMapping(value = "/space/{id}/delete.html", method = RequestMethod.GET)
	public String deleteSpace(@PathVariable String id) {
		uiSpaceManager.deleteSpace(id);

		return "redirect:/space/all.html";
	}

	@RequestMapping(value = "/space/{id}/liveactivities.html", method = RequestMethod.GET)
	public ModelAndView viewSpaceLiveActivities(@PathVariable String id) {
		ModelAndView mav = getModelAndView();
		Space space = activityRepository.getSpaceById(id);
		if (space != null) {
			mav.setViewName("space/SpaceViewLiveActivities");
			mav.addObject("space", space);

			Set<LiveActivityGroup> liveActivityGroups = Sets.newHashSet();
			collectLiveActivityGroups(space, liveActivityGroups);

			List<UiSpaceLiveActivityGroup> uiLiveActivityGroups = Lists.newArrayList();
			for (LiveActivityGroup liveActivityGroup : liveActivityGroups) {
				List<LiveActivity> liveActivities = Lists.newArrayList();
				for (GroupLiveActivity gla : liveActivityGroup.getActivities()) {
					liveActivities.add(gla.getActivity());
				}

				List<UiLiveActivity> liveactivities = uiControllerManager
						.getUiLiveActivities(liveActivities);
				Collections.sort(liveactivities,
						UiUtilities.UI_LIVE_ACTIVITY_BY_NAME_COMPARATOR);
				
				uiLiveActivityGroups.add(new UiSpaceLiveActivityGroup(liveActivityGroup, liveactivities));
			}
			Collections.sort(uiLiveActivityGroups,
					UiUtilities.UI_SPACE_LIVE_ACTIVITY_GROUP_BY_NAME_COMPARATOR);

			mav.addObject("liveactivitygroups", uiLiveActivityGroups);
		} else {
			mav.setViewName("space/SpaceNonexistent");
		}

		return mav;
	}

	/**
	 * Collect the live activity groups from this space and all subspaces.
	 * 
	 * @param space
	 *            the root space
	 * @param liveActivityGroups
	 *            the set of all groups seen
	 */
	private void collectLiveActivityGroups(Space space,
			Set<LiveActivityGroup> liveActivityGroups) {
		liveActivityGroups.addAll(space.getActivityGroups());

		for (Space subspace : space.getSpaces()) {
			collectLiveActivityGroups(subspace, liveActivityGroups);
		}
	}

	@RequestMapping(value = "/space/all.json", method = RequestMethod.GET)
	public @ResponseBody
	Map<String, ? extends Object> listAllSpacesJson(
			@RequestParam(value = "filter", required = false) String filter) {
		List<Map<String, Object>> data = Lists.newArrayList();

		try {
			FilterExpression filterExpression = expressionFactory
					.getFilterExpression(filter);

			for (Space space : activityRepository.getSpaces(filterExpression)) {
				data.add(uiSpaceManager.getBasicSpaceViewJsonData(space));
			}

			return JsonSupport.getSuccessJsonResponse(data);
		} catch (Exception e) {
			spacesEnvironment.getLog().error(
					"Attempt to get live activity group data failed", e);

			return JsonSupport.getFailureJsonResponse("call failed");
		}
	}

	@RequestMapping(value = "/space/{id}/view.json", method = RequestMethod.GET)
	public @ResponseBody
	Map<String, ? extends Object> viewSpaceJson(@PathVariable String id) {
		Space space = activityRepository.getSpaceById(id);
		if (space != null) {
			return JsonSupport.getSuccessJsonResponse(uiSpaceManager
					.getSpaceViewJsonData(space));
		} else {
			return getNoSuchSpaceResult();
		}
	}

	@RequestMapping(value = "/space/{id}/status.json", method = RequestMethod.GET)
	public @ResponseBody
	Map<String, ? extends Object> statusSpaceJson(@PathVariable String id) {
		return uiSpaceManager.getJsonSpaceStatus(id);
	}

	@RequestMapping(value = "/space/{id}/liveactivitystatus.json", method = RequestMethod.GET)
	public @ResponseBody
	Map<String, ? extends Object> statusSpaceLiveActivities(
			@PathVariable String id) {
		try {
			uiControllerManager.liveActivityStatusSpace(id);

			return JsonSupport.getSimpleSuccessJsonResponse();
		} catch (EntityNotFoundInteractiveSpacesException e) {
			return getNoSuchSpaceResult();
		}
	}

	@RequestMapping(value = "/space/{id}/metadata.json", method = RequestMethod.POST)
	public @ResponseBody
	Map<String, ? extends Object> modifySpaceGroupMetadata(
			@PathVariable String id, @RequestBody Object metadataCommandObj,
			HttpServletResponse response) {

		if (Map.class.isAssignableFrom(metadataCommandObj.getClass())) {
			@SuppressWarnings("unchecked")
			Map<String, Object> metadataCommand = (Map<String, Object>) metadataCommandObj;

			return uiSpaceManager.updateSpaceMetadata(id, metadataCommand);
		} else {
			return JsonSupport
					.getFailureJsonResponse(JsonSupport.MESSAGE_SPACE_CALL_ARGS_NOMAP);
		}
	}

	@RequestMapping(value = "/space/{id}/load.json", method = RequestMethod.GET)
	public @ResponseBody
	Map<String, ? extends Object> loadSpace(@PathVariable String id) {
		return JsonSupport.getSimpleSuccessJsonResponse();
	}

	@RequestMapping(value = "/space/{id}/deploy.json", method = RequestMethod.GET)
	public @ResponseBody
	Map<String, ? extends Object> deploySpace(@PathVariable String id) {
		try {
			uiSpaceManager.deploySpace(id);

			return JsonSupport.getSimpleSuccessJsonResponse();
		} catch (EntityNotFoundInteractiveSpacesException e) {
			return getNoSuchSpaceResult();
		}
	}

	@RequestMapping(value = "/space/{id}/configure.json", method = RequestMethod.GET)
	public @ResponseBody
	Map<String, ? extends Object> configureSpace(@PathVariable String id) {
		try {
			uiSpaceManager.configureSpace(id);

			return JsonSupport.getSimpleSuccessJsonResponse();
		} catch (EntityNotFoundInteractiveSpacesException e) {
			return getNoSuchSpaceResult();
		}
	}

	@RequestMapping(value = "/space/{id}/startup.json", method = RequestMethod.GET)
	public @ResponseBody
	Map<String, ? extends Object> startupSpace(@PathVariable String id) {
		try {
			uiSpaceManager.startupSpace(id);

			return JsonSupport.getSimpleSuccessJsonResponse();
		} catch (EntityNotFoundInteractiveSpacesException e) {
			return getNoSuchSpaceResult();
		}
	}

	@RequestMapping(value = "/space/{id}/activate.json", method = RequestMethod.GET)
	public @ResponseBody
	Map<String, ? extends Object> activateSpace(@PathVariable String id) {
		try {
			uiSpaceManager.activateSpace(id);

			return JsonSupport.getSimpleSuccessJsonResponse();
		} catch (EntityNotFoundInteractiveSpacesException e) {
			return getNoSuchSpaceResult();
		}
	}

	@RequestMapping(value = "/space/{id}/deactivate.json", method = RequestMethod.GET)
	public @ResponseBody
	Map<String, ? extends Object> deactivateSpace(@PathVariable String id) {
		try {
			uiSpaceManager.deactivateSpace(id);

			return JsonSupport.getSimpleSuccessJsonResponse();
		} catch (EntityNotFoundInteractiveSpacesException e) {
			return getNoSuchSpaceResult();
		}
	}

	@RequestMapping(value = "/space/{id}/shutdown.json", method = RequestMethod.GET)
	public @ResponseBody
	Map<String, ? extends Object> shutdownSpace(@PathVariable String id) {
		try {
			uiSpaceManager.shutdownSpace(id);

			return JsonSupport.getSimpleSuccessJsonResponse();
		} catch (EntityNotFoundInteractiveSpacesException e) {
			return getNoSuchSpaceResult();
		}
	}

	/**
	 * Get a JSON error response for no such space.
	 * 
	 * @return the JSON result
	 */
	private Map<String, Object> getNoSuchSpaceResult() {
		return JsonSupport
				.getFailureJsonResponse(UiSpaceManager.MESSAGE_SPACE_DOMAIN_SPACE_UNKNOWN);
	}

	/**
	 * @param activityRepository
	 *            the activityRepository to set
	 */
	public void setActivityRepository(ActivityRepository activityRepository) {
		this.activityRepository = activityRepository;
	}

	/**
	 * @param activeSpaceManager
	 *            the activeSpaceManager to set
	 */
	public void setActiveSpaceManager(ActiveSpaceManager spaceManager) {
		this.activeSpaceManager = spaceManager;
	}
}
