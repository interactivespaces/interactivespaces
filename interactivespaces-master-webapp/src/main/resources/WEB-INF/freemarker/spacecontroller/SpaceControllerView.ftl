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
<title>Interactive Spaces Admin: Controllers</title>

<#include "/allpages_head.ftl">

<script type="text/javascript" src="/interactivespaces/js/jquery-ispopup.js"></script>

<script type="text/javascript">
$(document).ready(function() {
<#list liveactivities as liveactivity>
$('${"#liveactivity-info-${liveactivity.activity.uuid}"}')
  .ispopup("#liveactivity-info-${liveactivity.activity.uuid}-popup");
</#list>
});
</script>
</head>

<body>

<script type="text/javascript">
function doAjaxCommand(command) {
  $.ajax({
      url: '/interactivespaces/spacecontroller/${spacecontroller.id}/' + command + '.json',
      success: function(data) {
        $('#commandResult').html(data.result);
      }
  });
}

function deleteController() {
    if (confirm("Are you sure you want to delete the controller?")) {
        window.location='/interactivespaces/spacecontroller/${spacecontroller.id}/delete.html'
    }
}
function shutdownController() {
    if (confirm("Are you sure you want to shutdown the controller?")) {
       doAjaxCommand('shutdown');
    }
}

function shutdownActivities() {
    if (confirm("Are you sure you want to shut down all applications on the controller?")) {
        doAjaxCommand('activities/shutdown');
    }
}
</script>

<#include "/allpages_body_header.ftl">

<h1>Space Controller: ${spacecontroller.name}</h1>

<div class="commandBar"><ul>
<li><button type="button" onclick="doAjaxCommand('connect')">Connect</button></li>
<li><button type="button" onclick="doAjaxCommand('disconnect')">Disconnect</button></li>
<li><button type="button" onclick="doAjaxCommand('status')">Status</button></li>
<li><button type="button" id="editButton" onclick="window.location='/interactivespaces/spacecontroller/${spacecontroller.id}/edit.html'" title="Edit the application details">Edit</button></li>
<li><button type="button" id="editMetadataButton" 
    onclick="window.location='/interactivespaces/spacecontroller/${spacecontroller.id}/metadata/edit.html'" title="Edit the space controller metadata">Metadata</button></li>
<li><button type="button" onclick="shutdownController()" title="Shutdown the controller">Shutdown</button></li>
<li><button type="button" onclick="shutdownActivities()" title="Shutdown all activities on the controller">Shutdown All Activities</button></li>
<li><button type="button" id="deployButton" onclick="doAjaxCommand('deploy')" title="Deploy all Live Activities on this Controller">Deploy</button></li>
<#if !(liveactivities?has_content)>
<li><button type="button" onclick="deleteController()" title="Delete space controller on master">Delete</button></li>
</#if>
</ul></div>

<div id="commandResult">
</div>

<#if spacecontroller.description?has_content><p>
${spacecontroller.description}
</p></#if>

<table>
<tr>
<th>ID</th>
<td>${spacecontroller.id}</td>
</tr>
<tr>
<th>UUID</th>
<td>${spacecontroller.uuid}</td>
</tr>
<tr>
<th>Host ID</th>
<td>${spacecontroller.hostId}</td>
</tr>
<tr>
<th>Status</th>
<td><#if lspacecontroller.lastStateUpdate??><#assign t  = lspacecontroller.lastStateUpdateDate?datetime><#else><#assign t = 'Unknown'></#if>
<@spring.message lspacecontroller.state.description /> as of ${t}</td>
</tr>
<tr>
<th valign="top">Metadata</th>
<td><table><#list metadata as item>
<tr><th>${item.label}</th><td>${item.value}</td></tr>
</#list></table></td>
</tr>
</table>

<h2>Live Activities</h2>

<#if liveactivities?has_content>

<table>
<tr><th>Live Activity</th><th>Status</th><th>Up to date?</th></td>

<#list liveactivities as liveactivity>
<#assign trCss = (liveactivity_index % 2 == 0)?string("even","odd")>
    <tr class="${trCss}">
    <td><a href="/interactivespaces/liveactivity/${liveactivity.activity.id}/view.html">${liveactivity.activity.name}</a></td>
<td>
<div id="liveactivity-info-${liveactivity.activity.uuid}">
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
</td>
<td>
<#if liveactivity.activity.outOfDate>
<span title="Live Activity is out of date"><img src="/interactivespaces/img/outofdate.png" alt="Live Activity is out of date" /></span>
</#if>
</td>
    </tr>
</#list>
</table>
<#else>
<p>
None
</p>
</#if>

</body>
<html>