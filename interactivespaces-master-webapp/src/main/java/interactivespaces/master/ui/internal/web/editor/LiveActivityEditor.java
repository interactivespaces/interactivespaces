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

package interactivespaces.master.ui.internal.web.editor;

import interactivespaces.domain.basic.LiveActivity;
import interactivespaces.master.server.services.ActivityRepository;

import java.beans.PropertyEditorSupport;

/**
 * A property editor for {@link LiveActivity} instances.
 *
 * @author Keith M. Hughes
 */
public class LiveActivityEditor extends PropertyEditorSupport {

  /**
   * Repository for activities.
   */
  private ActivityRepository activityRepository;

  public LiveActivityEditor() {
  }

  public LiveActivityEditor(ActivityRepository activityRepository) {
    this.activityRepository = activityRepository;
  }

  @Override
  public String getAsText() {
    Object o = getValue();

    if (o != null) {
      return ((LiveActivity) o).getId();
    } else
      return null;
  }

  @Override
  public void setAsText(String text) throws IllegalArgumentException {
    if (text != null && text.trim().length() > 0) {
      LiveActivity activity = activityRepository.getLiveActivityById(text);
      if (activity != null)
        setValue(activity);
      else
        throw new IllegalArgumentException("No live activity with ID " + text);
    } else
      setValue(null);
  }

  /**
   * @param activityRepository
   *          the activityRepository to set
   */
  public void setActivityRepository(ActivityRepository activityRepository) {
    this.activityRepository = activityRepository;
  }
}