/*
 * Copyright (C) 2013 Google Inc.
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

package interactivespaces.hardware.driver.gaming.wilddivine;

/**
 * A listener for events from an {@link IomDriver}.
 *
 * @author Keith M. Hughes
 */
public interface IomDeviceListener {

  /**
   * The minimum value of the heart rate value.
   */
  double HEART_RATE_VALUE_MINIMUM = 1.6;

  /**
   * The maximum value of the heart rate value.
   */
  double HEART_RATE_VALUE_MAXIMUM = 2.5;

  /**
   * The minimum value of the skin conductivity level.
   */
  double SKIN_CONDUCTIVITY_LEVEL_MINIMUM = 3.0;

  /**
   * The maximum value of the skin conductivity level.
   */
  double SKIN_CONDUCTIVITY_LEVEL_MAXIMUM = 3.0;

  /**
   * A sensor reading has taken place.
   *
   * @param driver
   *          the driver the reading is from
   * @param heartRateValue
   *          the heart rate value (between {@link #HEART_RATE_VALUE_MINIMUM}
   *          and {@link #HEART_RATE_VALUE_MAXIMUM})
   * @param skinConductivityLevel
   *          the skin conductivity level (between
   *          {@link #SKIN_CONDUCTIVITY_LEVEL_MINIMUM} and
   *          {@link #SKIN_CONDUCTIVITY_LEVEL_MAXIMUM})
   */
  void onIomMeasurement(IomDriver driver, double heartRateValue, double skinConductivityLevel);
}
