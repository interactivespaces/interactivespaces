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

package interactivespaces.activity;

import interactivespaces.InteractiveSpacesException;
import interactivespaces.SimpleInteractiveSpacesException;

/**
 * Status of an Interactive Spaces activity.
 *
 * @author Keith M. Hughes
 */
public class ActivityStatus {

  /**
   * Status of the activity.
   */
  private final ActivityState state;

  /**
   * Description of the state.
   */
  private final String description;

  /**
   * Exception, if any.
   *
   * <p>
   * Can be {@code null}.
   */
  private final Throwable exception;

  /**
   * Construct a new status.
   *
   * <p>
   * The exception will be {@code null}.
   *
   * @param state
   *          the activity state
   * @param description
   *          the description
   */
  public ActivityStatus(ActivityState state, String description) {
    this(state, description, null);
  }

  /**
   * Construct a new status.
   *
   * @param state
   *          the activity state
   * @param description
   *          the description
   * @param exception
   *          the exception, can be {@code null}
   */
  public ActivityStatus(ActivityState state, String description, Throwable exception) {
    this.state = state;
    this.description = description;
    this.exception = exception;
  }

  /**
   * Get the activity state.
   *
   * @return the state
   */
  public ActivityState getState() {
    return state;
  }

  /**
   * Get the status description.
   *
   * @return the description
   */
  public String getDescription() {
    return description;
  }

  /**
   * Get the status exception.
   *
   * @return the exception, can be {@code null}
   */
  public Throwable getException() {
    return exception;
  }

  /**
   * Get the exception as a string.
   *
   * @return the exception string, or {@code null} if there is no exception
   */
  public String getExceptionAsString() {
    if (exception != null) {
      if (exception instanceof SimpleInteractiveSpacesException) {
        return String.format("Exception message: %s",
            ((SimpleInteractiveSpacesException) exception).getCompoundMessage());
      } else {
        return InteractiveSpacesException.getStackTrace(exception);
      }
    } else {
      return null;
    }
  }

  /**
   * Get the combined detail of the description, if any, and the exception as a string, if any.
   *
   * @return the combined detail, if none then the empty string
   */
  public String getCombinedDetail() {
    StringBuilder detail = new StringBuilder();

    if (description != null) {
      detail.append(description);
    }

    if (exception != null) {
      if (detail.length() != 0) {
        detail.append("\n\n");
      }
    }
    detail.append(getExceptionAsString());

    return detail.toString();
  }

  @Override
  public String toString() {
    return "ActivityState [state=" + state + ", description=" + description + ", exception=" + exception + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((description == null) ? 0 : description.hashCode());
    result = prime * result + state.hashCode();
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    ActivityStatus other = (ActivityStatus) obj;
    if (state != other.state) {
      return false;
    }
    if (description == null) {
      if (other.description != null) {
        return false;
      }
    } else if (!description.equals(other.description)) {
      return false;
    }

    return true;
  }
}
