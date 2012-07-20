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

package interactivespaces.master.ui.internal.web.osgi;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.osgi.framework.BundleContext;

/**
 * A servlet context listener to get the bundle context into the servlet context.
 *
 * @author Keith M. Hughes
 */
public class WebappContextListener implements ServletContextListener {
	static final String OSGI_BUNDLE_CONTEXT_ATTRIBUTE = "org.springframework.osgi.web." + BundleContext.class.getName();
	
	@Override
	public void contextInitialized(ServletContextEvent event) {
		BundleContext bundleContext = WebappActivator.getBundleContext();
		event.getServletContext().setAttribute(OSGI_BUNDLE_CONTEXT_ATTRIBUTE, bundleContext);
		event.getServletContext().setAttribute(OsgiBundleXmlWebApplicationContext.BUNDLE_CONTEXT_ATTRIBUTE, bundleContext);
	}

	@Override
	public void contextDestroyed(ServletContextEvent event) {
	}
}
