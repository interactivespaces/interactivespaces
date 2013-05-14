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
<title>Interactive Spaces Admin: Live Activities</title>

<#include "/allpages_head.ftl">

<script type="text/javascript" src="/interactivespaces/js/jquery-ispopup.js"></script>

<script type="text/javascript">
function doAjaxCommandByUrl(url) {
  $.ajax({
      url: url,
      success: function(data) {
        $('#commandResult').html(data.result);
      }
  });
}

function shutdownAllActivitiesAllControllers() {
    if (confirm("Are you sure you want to shut down all applications on all controllers?")) {
        window.location='/interactivespaces/spacecontroller/all/activities/shutdown.html';
    }
}

$(document).ready(function() {
<#list liveactivities as liveactivity>
$('${"#liveactivity-info-${liveactivity.activity.uuid}"}')
  .ispopup("#liveactivity-info-${liveactivity.activity.uuid}-popup");
</#list>
});
</script>

</head>

<body>

<#include "/allpages_body_header.ftl">

<h1>Live Activities</h1>

<div class="commandBar"><ul>
<li><button type="button" id="newButton" onclick="window.location='/interactivespaces/liveactivity/new.html?mode=embedded'" title="Create a new live activity">New</button></li>
<li><button type="button" id="nstatusAllButton" onclick="doAjaxCommandByUrl('/interactivespaces/spacecontroller/all/status.json')" title="Get the status of all Live Activities on all Space Controllers">Status All</button></li>
<li><button type="button" id="shutdownActivitiesAllButton" onclick="shutdownAllActivitiesAllControllers();" title="Shutdown all activities on all connected controllers">Shutdown All Activities</button></li>
</ul></div>

<div id="commandResult">
</div>

<table>
<tr><th>Name</th><th>Status</th><th>Up to date?</th></tr>

<#list liveactivities as liveactivity>
<#assign trCss = (liveactivity_index % 2 == 0)?string("even","odd")>
<tr class="${trCss}">
<td><a href="${liveactivity.activity.id}/view.html">${liveactivity.activity.name}</a></td>
<td><#if liveactivity.active?has_content><div id="liveactivity-info-${liveactivity.activity.uuid}">
<span class="liveactivity-status liveactivity-status-${liveactivity.active.runtimeState.name()}"><@spring.message liveactivity.active.runtimeState.description /></span>
 as of 
  <#if liveactivity.active.lastStateUpdate??>
    ${liveactivity.active.lastStateUpdateDate?datetime}
  <#else>
    Unknown
  </#if>
</div>
<div id="liveactivity-info-${liveactivity.activity.uuid}-popup" class="liveactivity-info-popup">
<div><#if liveactivity.active.directRunning>
Directly Running
<#else>
Not directly running
</#if> 
</div><div>
<#if liveactivity.active.directActivated>
Directly activated
<#else>
Not directly activated
</#if> 
</div><div>
Running from ${liveactivity.active.numberLiveActivityGroupRunning} groups
</div><div>
Activated from ${liveactivity.active.numberLiveActivityGroupActivated} groups
</div></div>
<#else>
<span style="color: red;">No controller assigned!</span>
</#if>
</td>
<td>
<#if liveactivity.activity.outOfDate>
<span title="Live Activity is out of date"><img src="/interactivespaces/img/outofdate.png" alt="Live Activity is out of date" /></span>
</#if>
<#if liveactivity.active.deployState != "READY">
<span>
<@spring.message liveactivity.active.deployState.description />
</span>
</#if>
</td>
    </tr>
</#list>
</table>

</body>
<html>