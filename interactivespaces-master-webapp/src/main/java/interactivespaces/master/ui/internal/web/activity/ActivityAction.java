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

import interactivespaces.InteractiveSpacesException;
import interactivespaces.domain.basic.Activity;
import interactivespaces.domain.basic.pojo.SimpleActivity;
import interactivespaces.master.api.MasterApiActivityManager;
import interactivespaces.master.ui.internal.web.BaseSpaceMasterController;

import org.springframework.web.multipart.MultipartFile;
import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.execution.RequestContext;

import java.io.IOException;
import java.io.Serializable;

/**
 * The webflow action for activity upload.
 *
 * @author Keith M. Hughes
 */
public class ActivityAction extends BaseSpaceMasterController {

  /**
   * Manager for UI operations on activities.
   */
  private MasterApiActivityManager masterApiActivityManager;

  /**
   * Get a new activity model.
   *
   * @return
   */
  public ActivityForm newActivity() {
    return new ActivityForm();
  }

  /**
   * Add entities to the flow context needed by the new entity page.
   *
   * @param context
   *          The Webflow context.
   */
  public void addNeededEntities(RequestContext context) {
    MutableAttributeMap viewScope = context.getViewScope();
    addGlobalModelItems(viewScope);
  }

  /**
   * Save the new activity.
   *
   * @param activity
   */
  public void saveActivity(ActivityForm form) {
    try {
      Activity activity =
          masterApiActivityManager.saveActivity(form.getActivity(), form.getActivityFile()
              .getInputStream());

      // So the ID gets copied out of the flow.
      form.getActivity().setId(activity.getId());
    } catch (IOException e) {
      throw new InteractiveSpacesException("Could not get uploaded activity file", e);
    }
  }

  /**
   * @param masterApiActivityManager
   *          the masterApiActivityManager to set
   */
  public void setMasterApiActivityManager(MasterApiActivityManager masterApiActivityManager) {
    this.masterApiActivityManager = masterApiActivityManager;
  }

  /**
   * Form bean for activity objects.
   *
   * @author Keith M. Hughes
   */
  public static class ActivityForm implements Serializable {

    /**
     * Form data for activity.
     */
    private SimpleActivity activity = new SimpleActivity();

    /**
     * The activity file.
     */
    private MultipartFile activityFile;

    /**
     * @return the activity
     */
    public SimpleActivity getActivity() {
      return activity;
    }

    /**
     * @param activity
     *          the activity to set
     */
    public void setActivity(SimpleActivity activity) {
      this.activity = activity;
    }

    /**
     * Get the uploaded activity file.
     *
     * @return the uploaded file
     */
    public MultipartFile getActivityFile() {
      return activityFile;
    }

    /**
     * Set the uploaded activity file.
     *
     * @param activityFile
     *          the uploaded file
     */
    public void setActivityFile(MultipartFile activityFile) {
      this.activityFile = activityFile;
    }
  }
}
