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
<html>
<head>
<title>Interactive Spaces Admin: Live Activities</title>

<#include "/allpages_head.ftl" >
</head>

<body>

<script type="text/javascript">
function doAjaxCommand(command) {
  $.ajax({
      url: '/interactivespaces/liveactivity/${liveactivity.activity.id}/' + command + '.json',
      success: function(data) {
        $('#commandResult').html(data.result);
      }
  });
}

function deleteLiveActivity() {
    if (confirm("Are you sure you want to delete the activity?")) {
        window.location='/interactivespaces/liveactivity/${liveactivity.activity.id}/delete.html'
    }
}
</script>


<#include "/allpages_body_header.ftl">

<h1>Live Activity: ${liveactivity.activity.name}</h1>

<div class="commandBar"><ul>
<li><button type="button" id="startupButton" onclick="doAjaxCommand('startup')">Startup</button></li>
<li><button type="button" id="activateButton" onclick="doAjaxCommand('activate')">Activate</button></li>
<li><button type="button" id="deactivateButton" onclick="doAjaxCommand('deactivate')">Deactivate</button></li>
<li><button type="button" id="shutdownButton" onclick="doAjaxCommand('shutdown')">Shutdown</button></li>
<li><button type="button" id="statusButton" onclick="doAjaxCommand('status')">Status</button></li>
<li><button type="button" id="configureButton" onclick="doAjaxCommand('configure')" title="Configure activity on the controller">Configure</button></li>
<li><button type="button" id="deployButton" onclick="doAjaxCommand('deploy')" title="Deploy activity to the controller">Deploy</button></li>
<li><button type="button" id="editButton" onclick="window.location='/interactivespaces/liveactivity/${liveactivity.activity.id}/edit.html'" title="Edit the activity details">Edit</button></li>
<li><button type="button" id="editConfigButton" onclick="window.location='/interactivespaces/liveactivity/${liveactivity.activity.id}/config/edit.html'" title="Edit the activity configuration">Edit Config</button></li>
<li><button type="button" id="editMetadataButton" 
    onclick="window.location='/interactivespaces/liveactivity/${liveactivity.activity.id}/metadata/edit.html'" title="Edit the live activity metadata">Metadata</button></li>
<#if !(liveactivitygroups?has_content)>
<li><button type="button" onclick="deleteLiveActivity()" title="Delete activity on master">Delete</button></li>
</#if>

</ul></div>

<div id="commandResult">
</div>

<#if liveactivity.activity.description?has_content><p>
${liveactivity.activity.description}
</p></#if>

<table>
<tr>
<th>ID</th>
<td>${liveactivity.activity.id}</td>
</tr>
<tr>
<th>UUID</th>
<td>${liveactivity.activity.uuid}</td>
</tr>
<tr>
<th>Activity</th>
<td><a href="/interactivespaces/activity/${liveactivity.activity.activity.id}/view.html">${liveactivity.activity.activity.name}</a></td>
</tr>
<th>Controller</th>
<td><#if liveactivity.activity.controller?has_content>
<a href="/interactivespaces/spacecontroller/${liveactivity.activity.controller.id}/view.html">${liveactivity.activity.controller.name}</a>
<#else>
<span style="color: red;">No controller assigned!</span>
</#if>
</td>
</tr>
<tr>
<th>Deployement</th>
<td>
Last: <#if liveactivity.activity.lastDeployDate??>
      ${liveactivity.activity.lastDeployDate?datetime}
    <#else>
      Unknown
    </#if>
<#if liveactivity.activity.outOfDate>
<span title="Live Activity is out of date"><img src="/interactivespaces/img/outofdate.png" alt="Live Activity is out of date" /></span>
</#if>
<#if liveactivity.active.deployState != "READY">
&bull;
<span>
<@spring.message liveactivity.active.deployState.description />
</span>
</#if>

</td>
</tr>
<tr>
<th>Status</th>
<td>
<#if liveactivity.active?has_content>
  <span class="liveactivity-status liveactivity-status-${liveactivity.active.runtimeState.name()}"><@spring.message liveactivity.active.runtimeState.description /></span>
   as of 
  <#if liveactivity.active.lastStateUpdate??>
    ${liveactivity.active.lastStateUpdateDate?datetime}
  <#else>
    Unknown
  </#if>
</#if>
</td>
</tr>
<tr>
<th>&nbsp;</th>
<td>
<#if liveactivity.active.directRunning>
Directly Running
<#else>
Not Directly Running
</#if> 
&bull;
<#if liveactivity.active.directActivated>
Directly Activated
<#else>
Not Directly Activated
</#if> 
&bull;
running from ${liveactivity.active.numberLiveActivityGroupRunning} groups
&bull;
activated from ${liveactivity.active.numberLiveActivityGroupActivated} groups
</td>
</tr>
<tr>
<th valign="top">Metadata</th>
<td><table><#list metadata as item>
<tr><th>${item.label}</th><td>${item.value}</td></tr>
</#list></table></td>
</tr>
</table>

<h2>Containing Live Activity Groups</h2>

<#if liveactivitygroups?has_content>

<ul>
<#list liveactivitygroups as liveactivitygroup>
    <li><a href="/interactivespaces/liveactivitygroup/${liveactivitygroup.id}/view.html">${liveactivitygroup.name}</a></li>
</#list>
</ul>
<#else>
<p>
None
</p>
</#if>


</body>
<html>