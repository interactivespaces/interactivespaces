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

package interactivespaces.master.ui.internal.web.spacecontroller;

import interactivespaces.domain.basic.SpaceController;
import interactivespaces.master.server.services.ActiveControllerManager;
import interactivespaces.master.server.services.ActiveSpaceController;
import interactivespaces.master.server.services.ActivityRepository;
import interactivespaces.master.server.services.ControllerRepository;
import interactivespaces.master.server.services.EntityNotFoundInteractiveSpacesException;
import interactivespaces.master.server.ui.JsonSupport;
import interactivespaces.master.server.ui.UiLiveActivity;
import interactivespaces.master.server.ui.UiSpaceManager;
import interactivespaces.master.ui.internal.web.BaseActiveSpaceMasterController;
import interactivespaces.master.ui.internal.web.UiUtilities;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * A controller for Interactive Spaces space controller operations.
 * 
 * @author Keith M. Hughes
 */
@Controller
public class SpaceControllerController extends BaseActiveSpaceMasterController {

	/**
	 * Repository for activity entities.
	 */
	private ActivityRepository activityRepository;

	/**
	 * Repository for controller entities.
	 */
	private ControllerRepository controllerRepository;

	/**
	 * Manager for controller operations.
	 */
	private ActiveControllerManager activeControllerManager;

	/**
	 * Display a list of all controllers.
	 * 
	 * @return Model and view for controller list display.
	 */
	@RequestMapping("/spacecontroller/all.html")
	public ModelAndView listControllers() {
		List<ActiveSpaceController> controllers = activeControllerManager
				.getActiveSpaceControllers(controllerRepository
						.getAllSpaceControllers());
		Collections.sort(controllers,
				UiUtilities.ACTIVE_CONTROLLER_BY_NAME_COMPARATOR);

		ModelAndView mav = getModelAndView();
		mav.setViewName("spacecontroller/SpaceControllerViewAll");
		mav.addObject("spacecontrollers", controllers);

		return mav;
	}

	@RequestMapping(value = "/spacecontroller/{id}/view.html", method = RequestMethod.GET)
	public ModelAndView viewController(@PathVariable String id) {
		ModelAndView mav = getModelAndView();

		SpaceController controller = controllerRepository
				.getSpaceControllerById(id);
		if (controller != null) {
			ActiveSpaceController lcontroller = activeControllerManager
					.getActiveSpaceController(controller);

			List<UiLiveActivity> liveactivities = uiControllerManager
					.getAllUiLiveActivitiesByController(controller);
			Collections.sort(liveactivities,
					UiUtilities.UI_LIVE_ACTIVITY_BY_NAME_COMPARATOR);
			mav.addObject("metadata",
					UiUtilities.getMetadataView(controller.getMetadata()));

			mav.setViewName("spacecontroller/SpaceControllerView");

			mav.addObject("spacecontroller", controller);
			mav.addObject("lspacecontroller", lcontroller);
			mav.addObject("liveactivities", liveactivities);
		} else {
			mav.setViewName("spacecontroller/SpaceControllerNonexistent");
		}

		return mav;
	}

	@RequestMapping(value = "/spacecontroller/{id}/delete.html", method = RequestMethod.GET)
	public ModelAndView deleteController(@PathVariable String id) {
		ModelAndView mav = getModelAndView();
		try {
			uiControllerManager.deleteController(id);

			mav.clear();
			mav.setViewName("redirect:/spacecontroller/all.html");
		} catch (EntityNotFoundInteractiveSpacesException e) {
			mav.setViewName("spacecontroller/SpaceControllerNonexistent");
		}

		return mav;
	}

	@RequestMapping(value = "/spacecontroller/all.json", method = RequestMethod.GET)
	public @ResponseBody
	Map<String, ? extends Object> listAllControllersJson() {
		List<Map<String, Object>> data = Lists.newArrayList();

		for (ActiveSpaceController acontroller : activeControllerManager
				.getActiveSpaceControllers(controllerRepository
						.getAllSpaceControllers())) {
			Map<String, Object> controllerData = Maps.newHashMap();

			SpaceController controller = acontroller.getController();
			getSpaceControllerData(controller, controllerData);

			data.add(controllerData);
		}

		return JsonSupport.getSuccessJsonResponse(data);
	}

	@RequestMapping(value = "/spacecontroller/{id}/view.json", method = RequestMethod.GET)
	public @ResponseBody
	Map<String, ? extends Object> viewControllerJson(@PathVariable String id) {
		SpaceController controller = controllerRepository
				.getSpaceControllerById(id);
		if (controller != null) {
			Map<String, Object> controllerData = Maps.newHashMap();

			getSpaceControllerData(controller, controllerData);

			return JsonSupport.getSuccessJsonResponse(controllerData);
		} else {
			return getNoSuchSpaceControllerResult();
		}
	}

	@RequestMapping(value = "/spacecontroller/{id}/connect.json", method = RequestMethod.GET)
	public @ResponseBody
	Map<String, ? extends Object> connectController(@PathVariable String id) {
		try {
			uiControllerManager.connectToControllers(Collections
					.singletonList(id));

			return JsonSupport.getSimpleSuccessJsonResponse();
		} catch (EntityNotFoundInteractiveSpacesException e) {
			return getNoSuchSpaceControllerResult();
		}
	}

	@RequestMapping(value = "/spacecontroller/{id}/disconnect.json", method = RequestMethod.GET)
	public @ResponseBody
	Map<String, ? extends Object> disconnectController(@PathVariable String id) {
		try {
			uiControllerManager.disconnectFromControllers(Collections
					.singletonList(id));

			return JsonSupport.getSimpleSuccessJsonResponse();
		} catch (EntityNotFoundInteractiveSpacesException e) {
			return getNoSuchSpaceControllerResult();
		}
	}

	@RequestMapping(value = "/spacecontroller/{id}/shutdown.json", method = RequestMethod.GET)
	public @ResponseBody
	Map<String, ? extends Object> shutdownController(@PathVariable String id) {
		try {
			uiControllerManager.shutdownControllers(Lists.newArrayList(id));

			return JsonSupport.getSimpleSuccessJsonResponse();
		} catch (EntityNotFoundInteractiveSpacesException e) {
			return getNoSuchSpaceControllerResult();
		}
	}

	@RequestMapping(value = "/spacecontroller/{id}/activities/shutdown.json", method = RequestMethod.GET)
	public @ResponseBody
	Map<String, ? extends Object> shutdownAllAppsController(
			@PathVariable String id) {
		try {
			uiControllerManager.shutdownAllActivities(id);

			return JsonSupport.getSimpleSuccessJsonResponse();
		} catch (EntityNotFoundInteractiveSpacesException e) {
			return getNoSuchSpaceControllerResult();
		}
	}

	@RequestMapping(value = "/spacecontroller/{id}/status.json", method = RequestMethod.GET)
	public @ResponseBody
	Map<String, ? extends Object> statusController(@PathVariable String id) {
		try {
			uiControllerManager
					.statusControllers(Collections.singletonList(id));

			return JsonSupport.getSimpleSuccessJsonResponse();
		} catch (EntityNotFoundInteractiveSpacesException e) {
			return getNoSuchSpaceControllerResult();
		}
	}

	@RequestMapping(value = "/spacecontroller/{id}/deploy.json", method = RequestMethod.GET)
	public @ResponseBody
	Map<String, ? extends Object> deployLiveActivities(@PathVariable String id) {
		try {
			uiControllerManager.deployAllControllerActivityInstances(id);

			return JsonSupport.getSimpleSuccessJsonResponse();
		} catch (EntityNotFoundInteractiveSpacesException e) {
			return getNoSuchSpaceControllerResult();
		}
	}

	@RequestMapping(value = "/spacecontroller/all/connect.html", method = RequestMethod.GET)
	public String connectAllControllers() {
		uiControllerManager.connectToAllControllers();

		return "redirect:/spacecontroller/all.html";
	}

	@RequestMapping(value = "/spacecontroller/all/disconnect.html", method = RequestMethod.GET)
	public String disconnectAllControllers() {
		uiControllerManager.disconnectFromAllControllers();

		return "redirect:/spacecontroller/all.html";
	}

	@RequestMapping(value = "/spacecontroller/all/shutdown.html", method = RequestMethod.GET)
	public String shutdownAllControllers() {
		uiControllerManager.shutdownAllControllers();

		return "redirect:/spacecontroller/all.html";
	}

	@RequestMapping(value = "/spacecontroller/all/status.json", method = RequestMethod.GET)
	public @ResponseBody
	Map<String, ? extends Object> statusAllControllers() {
		uiControllerManager.statusFromAllControllers();

		return JsonSupport.getSimpleSuccessJsonResponse();
	}

	@RequestMapping(value = "/spacecontroller/all/forcestatus.json", method = RequestMethod.GET)
	public @ResponseBody
	Map<String, ? extends Object> forceStatusAllControllers() {
		uiControllerManager.forceStatusFromAllControllers();

		return JsonSupport.getSimpleSuccessJsonResponse();
	}

	@RequestMapping(value = "/spacecontroller/all/activities/shutdown.html", method = RequestMethod.GET)
	public String shutdownAllActivitiesAllControllers() {
		uiControllerManager.shutdownAllActivitiesAllControllers();

		return "redirect:/spacecontroller/all.html";
	}

	/**
	 * Get the JSON data for a controller.
	 * 
	 * @param controller
	 *            the space controller
	 * @param controllerData
	 *            where the data should be stored
	 */
	private void getSpaceControllerData(SpaceController controller,
			Map<String, Object> controllerData) {
		controllerData.put("id", controller.getId());
		controllerData.put("uuid", controller.getUuid());
		controllerData.put("name", controller.getName());
		controllerData.put("description", controller.getDescription());
		controllerData.put("metadata", controller.getMetadata());
	}

	/**
	 * Get a result for no such space controller for JSON results.
	 * 
	 * @return
	 */
	private Map<String, Object> getNoSuchSpaceControllerResult() {
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
	 * @param controllerRepository
	 *            the controllerRepository to set
	 */
	public void setControllerRepository(
			ControllerRepository controllerRepository) {
		this.controllerRepository = controllerRepository;
	}

	/**
	 * @param activeControllerManager
	 *            the activeControllerManager to set
	 */
	public void setActiveControllerManager(
			ActiveControllerManager activeControllerManager) {
		this.activeControllerManager = activeControllerManager;
	}
}
