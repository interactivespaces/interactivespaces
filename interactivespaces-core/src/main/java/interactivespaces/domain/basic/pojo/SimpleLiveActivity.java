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

package interactivespaces.domain.basic.pojo;

import interactivespaces.domain.basic.Activity;
import interactivespaces.domain.basic.ActivityConfiguration;
import interactivespaces.domain.basic.LiveActivity;
import interactivespaces.domain.basic.SpaceController;
import interactivespaces.domain.pojo.SimpleObject;

import java.util.Date;
import java.util.Map;

import com.google.common.collect.Maps;

/**
 * A POJO implementation of a {@link LiveActivity}.
 *
 * @author Keith M. Hughes
 */
public class SimpleLiveActivity extends SimpleObject implements LiveActivity {

	/**
	 * For serialization.
	 */
	//private static final long serialVersionUID = 1490104161418795601L;
	private static final long serialVersionUID = -6129826497811473032L;

	/**
	 * The activity this is an instance of.
	 */
	private Activity activity;
	
	/**
	 * The UUID of the activity.
	 */
	private String uuid;
	
	/**
	 * The controller this activity is installed on.
	 */
	private SpaceController controller;
	
	/**
	 * A name for this installed activity.
	 */
	private String name;
	
	/**
	 * A description of this installed activity.
	 */
	private String description;
	
	/**
	 * The configuration for the installed activity.
	 */
	private ActivityConfiguration configuration;
	
	/**
	 * The last date the activity was deployed to the controller.
	 * 
	 * <p>
	 * {@code null} if never deployed
	 */
	private Date lastDeployDate;
	
	/**
	 * The meta data for this live activity.
	 */
	private Map<String, Object> metadata = Maps.newHashMap();

	@Override
	public Activity getActivity() {
		return activity;
	}

	@Override
	public LiveActivity setActivity(Activity activity) {
		this.activity = activity;
		
		return this;
	}

	@Override
	public String getUuid() {
		return uuid;
	}

	@Override
	public LiveActivity setUuid(String uuid) {
		this.uuid = uuid;
		
		return this;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public LiveActivity setName(String name) {
		this.name = name;
		
		return this;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public LiveActivity setDescription(String description) {
		this.description = description;
		
		return this;
	}

	@Override
	public SpaceController getController() {
		return controller;
	}

	@Override
	public LiveActivity setController(SpaceController controller) {
		this.controller = controller;
		
		return this;
	}

	@Override
	public ActivityConfiguration getConfiguration() {
		return configuration;
	}

	@Override
	public void setConfiguration(ActivityConfiguration configuration) {
		this.configuration = configuration;
	}

	@Override
	public Date getLastDeployDate() {
		return lastDeployDate;
	}

	@Override
	public void setLastDeployDate(Date lastDeployDate) {
		this.lastDeployDate = lastDeployDate;
	}

	@Override
	public boolean isOutOfDate() {
		Date date = getLastDeployDate();
		if (date != null) {
			return activity.getLastUploadDate().after(date);
		} else {
			// Never been deployed.
			return false;
		}
	}

	@Override
	public void setMetadata(Map<String, Object> metadata) {
		this.metadata = Maps.newHashMap(metadata);
	}

	@Override
	public Map<String, Object> getMetadata() {
		return Maps.newHashMap(metadata);
	}
}
