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

package interactivespaces.event.trigger;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A simple trigger which watches for the change of a value and will signal
 * rising or falling states.
 * 
 * <p>
 * The trigger supports hysteresis.
 * 
 * @author Keith M. Hughes
 */
public class SimpleTriggerPoint implements Trigger {
	/**
	 * The current value of the trigger.
	 */
	private int value;

	/**
	 * The threshold when the trigger will trigger.
	 */
	private int threshold;

	/**
	 * The hysteresis of the trigger.
	 */
	private int hysteresis;

	/**
	 * The current state of the trigger.
	 */
	private TriggerState state;

	/**
	 * The previous state of the trigger.
	 */
	private TriggerState previousState;

	/**
	 * Collection of listeners for trigger point events.
	 */
	private List<TriggerListener> listeners = new CopyOnWriteArrayList<TriggerListener>();

	public SimpleTriggerPoint() {
		threshold = 0;
		hysteresis = 0;
		value = 0;
		state = previousState = TriggerState.NOT_TRIGGERED;
	}

	/**
	 * Set the threshold parameter.
	 * 
	 * @param threshold
	 *            The new value of the triggering threshold.
	 * @param hysteresis
	 *            The new value of the hysteresis.
	 */
	public SimpleTriggerPoint setThreshold(int threshold) {
		this.threshold = threshold;
		
		return this;
	}

	/**
	 * Set the triggering hysteresis parameter.
	 * 
	 * @param hysteresis
	 *            The new value of the hysteresis.
	 */
	public SimpleTriggerPoint setHysteresis(int hysteresis) {
		this.hysteresis = hysteresis;
		
		return this;
	}

	/**
	 * Update the value.
	 * 
	 * @param newValue
	 *            The new value.
	 */
	public void update(int newValue) {
		value = newValue;

		state = previousState;

		if (value < (threshold - hysteresis)) {
			state = TriggerState.NOT_TRIGGERED;
		} else if (value > (threshold + hysteresis)) {
			state = TriggerState.TRIGGERED;
		}

		if ((previousState == TriggerState.NOT_TRIGGERED)
				&& (state == TriggerState.TRIGGERED)) {
			for (TriggerListener listener : listeners) {
				listener.onTrigger(this, state, TriggerEventType.RISING);
			}
		} else if ((previousState == TriggerState.TRIGGERED)
				&& (state == TriggerState.NOT_TRIGGERED)) {
			for (TriggerListener listener : listeners) {
				listener.onTrigger(this, state, TriggerEventType.FALLING);
			}
		}

		previousState = state;
	}

	/**
	 * Get the current value of the trigger.
	 * 
	 * @return
	 */
	public int getValue() {
		return value;
	}

	@Override
	public TriggerState getState() {
		return state;
	}

	/**
	 * Get the previous state of the trigger.
	 * 
	 * @return
	 */
	public TriggerState getPreviousState() {
		return previousState;
	}

	@Override
	public void addListener(TriggerListener listener) {
		listeners.add(listener);
	}

	@Override
	public void removeListener(TriggerListener listener) {
		listeners.remove(listener);
	}

}
