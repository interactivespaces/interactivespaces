/*
 * Copyright (C) 2015 Google Inc.
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

package interactivespaces.example.activity.image.gesture;

import java.util.Map;

import interactivespaces.activity.impl.BaseActivity;
import interactivespaces.service.image.gesture.Gesture;
import interactivespaces.service.image.gesture.Gesture.GestureState;
import interactivespaces.service.image.gesture.GestureEndpoint;
import interactivespaces.service.image.gesture.GestureHand;
import interactivespaces.service.image.gesture.GestureHandListener;
import interactivespaces.service.image.gesture.GestureListener;
import interactivespaces.service.image.gesture.GesturePointable;
import interactivespaces.service.image.gesture.GesturePointableListener;
import interactivespaces.service.image.gesture.GestureService;

/**
 * An activity that uses the gesture service to detect gestures, the user's hand
 * being palm up, and the number of fingers pointing upward, and logging the
 * events.
 *
 * @author Keith M. Hughes
 */
public class GestureExampleActivity extends BaseActivity {

  /**
   * The Y axis direction threshold for considering the palm to be facing up.
   */
  private static final double PALM_UP_THRESHOLD = 0.5;

  /**
   * The Y axis direction threshold for considering the pointable to be facing
   * up.
   */
  private static final double POINTABLE_UP_THRESHOLD = 0.8;

  /**
   * The number of pointables passing the test criteria required for a pointable
   * event.
   */
  private static final int POINTABLE_EVENT_COUNT_THRESHOLD = 2;

  @Override
  public void onActivitySetup() {
    getLog().info("Gestural Example activity starting!");

    GestureService gestureService =
        getSpaceEnvironment().getServiceRegistry().getRequiredService(GestureService.SERVICE_NAME);
    GestureEndpoint gestureEndpoint = gestureService.newGestureEndpoint(getLog());
    gestureEndpoint.addGestureListener(new GestureListener() {
      @Override
      public void onGestures(Map<String, Gesture> gestures) {
        handleGestures(gestures);
      }
    });
    gestureEndpoint.addHandListener(new GestureHandListener() {
      @Override
      public void onGestureHands(Map<String, GestureHand> hands) {
        handleHands(hands);
      }
    });
    gestureEndpoint.addPointableListener(new GesturePointableListener() {
      @Override
      public void onGesturePointables(Map<String, GesturePointable> pointables) {
        handlePointables(pointables);
      }
    });

    addManagedResource(gestureEndpoint);
  }

  /**
   * Handle any gestures that are recognized.
   * 
   * @param gestures
   *          the map of gesture ID to gestures
   */
  private void handleGestures(Map<String, Gesture> gestures) {
    if (isActivated()) {
      for (Gesture gesture : gestures.values()) {
        if (gesture.getState() == GestureState.STOP) {
          getLog().info("Detected gesture " + gesture.getType());
        }
      }
    }
  }

  /**
   * Handle any hand data that is recognized.
   * 
   * @param hands
   *          the map of hand ID to hand data
   */
  private void handleHands(Map<String, GestureHand> hands) {
    if (isActivated()) {
      for (GestureHand hand : hands.values()) {
        if (isPalmUp(hand)) {
          getLog().info("User hand is palm up");
        }
      }
    }
  }

  /**
   * Is the palm of the hand up or down?
   * 
   * @param hand
   *          the hand being checked
   * 
   * @return {@code true} if the hand is considered to be up
   */
  private boolean isPalmUp(GestureHand hand) {
    return hand.getPalmNormal().getV1() > PALM_UP_THRESHOLD;
  }

  /**
   * Handle any pointable data that is recognized.
   * 
   * @param pointables
   *          the map of pointable ID to pointable data
   */
  private void handlePointables(Map<String, GesturePointable> pointables) {
    if (isActivated()) {
      int numberUpPointables = countNumberUpPointables(pointables);

      if (numberUpPointables > POINTABLE_EVENT_COUNT_THRESHOLD) {
        getLog().info("The number of fingers pointing up is " + numberUpPointables);
      }
    }
  }

  /**
   * Get the number of pointables that have the tip pointing up.
   * 
   * @param pointables
   *          the pointable map
   * 
   * @return the number of pointables detected to be up
   */
  private int countNumberUpPointables(Map<String, GesturePointable> pointables) {
    int numberUpPointables = 0;
    for (GesturePointable pointable : pointables.values()) {
      if (isPointableUp(pointable)) {
        numberUpPointables++;
      }
    }
    return numberUpPointables;
  }

  /**
   * Is the pointable tip pointing up?
   * 
   * @param pointable
   *          the pointable to check
   * 
   * @return {@code true} if the pointable is up
   */
  private boolean isPointableUp(GesturePointable pointable) {
    return pointable.getDirection().getV1() > POINTABLE_UP_THRESHOLD;
  }
}
