<#--
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
 -->
<!DOCTYPE html>
<html>
<head>
<title>Interactive Spaces Admin: Space Controllers</title>

<#include "/allpages_head.ftl">
</head>

<body>
<script type="text/javascript">
function doAjaxCommand(command) {
  $.ajax({
      url: '/interactivespaces/spacecontroller/all/' + command + '.json',
      success: function(data) {
        $('#commandResult').html(data.result);
      }
  });
}

function shutdownAllControllers() {
    if (confirm("Are you sure you want to shutdown all controllers?")) {
        window.location='/interactivespaces/spacecontroller/all/shutdown.html';
    }
}

function shutdownAllActivitiesAllControllers() {
    if (confirm("Are you sure you want to shut down all applications on all controllers?")) {
        window.location='/interactivespaces/spacecontroller/all/activities/shutdown.html';
    }
}
</script>

<#include "/allpages_body_header.ftl">

<h1>Space Controllers</h1>

<div class="commandBar"><ul>
<li><button type="button" id="connectAllButton" onclick="window.location='/interactivespaces/spacecontroller/all/connect.html'" title="Connect to all known controllers">Connect All</button></li>
<li><button type="button" id="disconnectAllButton" onclick="window.location='/interactivespaces/spacecontroller/all/disconnect.html'" title="Disconnect from all known controllers">Disconnect All</button></li>
<li><button type="button" id="statusAllButton" onclick="doAjaxCommand('status')" title="Status from all controllers that claim they are in some sort of connection">Status All</button></li>
<li><button type="button" id="forceStatusAllButton" onclick="doAjaxCommand('forcestatus')" title="Status from all controllers, whether or not they have been connected">Force Status All</button></li>
<li><button type="button" id="shutdownActivitiesAllButton" onclick="shutdownAllActivitiesAllControllers();" title="Shutdown all activities on all connected controllers">Shutdown All Activities</button></li>
<li><button type="button" id="shutdownAllButton" onclick="shutdownAllControllers();" title="Shut down all connected controllers">Shutdown All</button></li>
<li><button type="button" id="newButton" onclick="window.location='/interactivespaces/spacecontroller/new.html?mode=embedded'" title="Create a new controller">New</button></li>
</ul></div>

<div id="commandResult">
</div>

<table>
<tr><th>Name</th><th>Status</th></tr>

<#list spacecontrollers as spacecontroller>
<tr>
<td><a href="${spacecontroller.controller.id}/view.html">${spacecontroller.controller.name}</a></td>
<td><@spring.message spacecontroller.state.description /></td>
</#list>
</ul>


</body>
<html>