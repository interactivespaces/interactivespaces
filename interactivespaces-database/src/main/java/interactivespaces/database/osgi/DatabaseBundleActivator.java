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

package interactivespaces.database.osgi;

import java.sql.Connection;
import java.sql.DriverManager;

import org.apache.derby.jdbc.EmbeddedDriver;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * OSGi bundle activator to get the database up and running.
 * 
 * @author Keith M. Hughes
 */
public class DatabaseBundleActivator implements BundleActivator {

	@Override
	public void start(BundleContext context) throws Exception {
		try {
			EmbeddedDriver driver = new EmbeddedDriver();
			Connection conn = DriverManager.getConnection("jdbc:derby:database/interactivespaces;create=true");
			conn.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		try {
			Connection conn = DriverManager.getConnection("jdbc:derby:database/interactivespaces;shutdown=true");
			conn.close();
		} catch (Exception e) {
			// Throws an exception as it shuts down, so just swallow.
		}
	}
}
