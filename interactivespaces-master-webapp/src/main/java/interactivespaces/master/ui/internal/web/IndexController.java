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

package interactivespaces.master.ui.internal.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * A controller for the index page.
 *
 * @author Keith M. Hughes
 */
@Controller
public class IndexController extends BaseSpaceMasterController {
	
	/**
	 * Front page of the app.
	 * 
	 * @return Model and view for app front page.
	 */
	@RequestMapping("/index.html")
	public ModelAndView frontPage() {
		ModelAndView mav = getModelAndView();
		mav.setViewName("index");
		
		return mav;
	}
	
	@RequestMapping("/")
	public String listControllers() {
		return "redirect:/index.html";
	}

}
