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

package interactivespaces.workbench.activity.project.type.android;

import interactivespaces.workbench.activity.project.builder.ActivityBuilder;
import interactivespaces.workbench.activity.project.builder.java.JavaActivityBuilder;
import interactivespaces.workbench.activity.project.creator.ActivityProjectTemplate;
import interactivespaces.workbench.activity.project.ide.EclipseIdeProjectCreatorSpecification;
import interactivespaces.workbench.activity.project.ide.JavaEclipseIdeProjectCreatorSpecification;
import interactivespaces.workbench.activity.project.type.ActivityProjectType;

/**
 * An Android activity project type.
 *
 * @author Keith M. Hughes
 */
public class AndroidActivityProjectType implements ActivityProjectType {

	/**
	 * Name for the type.
	 */
	public static final String NAME = "android";
	
	/**
	 * The extension for android projects.
	 */
	private AndroidJavaActivityExtension extension = new AndroidJavaActivityExtension();

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public ActivityBuilder newBuilder() {
		return new JavaActivityBuilder(extension);
	}

	@Override
	public ActivityProjectTemplate newActivityProjectTemplate() {
		return new GenericAndroidActivityProjectTemplate();
	}

	@Override
	public EclipseIdeProjectCreatorSpecification getEclipseIdeProjectCreatorSpecification() {
		return new JavaEclipseIdeProjectCreatorSpecification(extension);
	}
}
