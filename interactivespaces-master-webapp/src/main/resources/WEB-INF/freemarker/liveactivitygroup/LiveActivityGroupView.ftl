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
<title>Interactive Spaces Admin: Live Activity Groups</title>

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
      url: '/interactivespaces/liveactivitygroup/${liveactivitygroup.id}/' + command + '.json',
      success: function(data) {
        $('#commandResult').html(data.result);
      }
  });
}

function deleteActivityGroup() {
    if (confirm("Are you sure you want to delete the live activity group?")) {
        window.location='/interactivespaces/liveactivitygroup/${liveactivitygroup.id}/delete.html'
    }
}
</script>

<#include "/allpages_body_header.ftl">

<h2>Live Activity Group: ${liveactivitygroup.name}</h2>

<div class="commandBar"><ul>
<li><button type="button" id="startupButton" onclick="doAjaxCommand('startup')">Startup</button></li>
<li><button type="button" id="activateButton" onclick="doAjaxCommand('activate')">Activate</button></li>
<li><button type="button" id="deactivateButton" onclick="doAjaxCommand('deactivate')">Deactivate</button></li>
<li><button type="button" id="shutdownButton" onclick="doAjaxCommand('shutdown')">Shutdown</button></li>
<li><button type="button" id="statusButton" onclick="doAjaxCommand('liveactivitystatus')">Status</button></li>
<li><button type="button" id="configureButton" onclick="doAjaxCommand('configure')">Configure</button></li>
<li><button type="button" id="deployButton" onclick="doAjaxCommand('deploy')">Deploy</button></li>
<li><button type="button" id="editButton" 
    onclick="window.location='/interactivespaces/liveactivitygroup/${liveactivitygroup.id}/edit.html'" title="Edit the live activity group details">Edit</button></li>
<li><button type="button" id="editMetadataButton" 
    onclick="window.location='/interactivespaces/liveactivitygroup/${liveactivitygroup.id}/metadata/edit.html'" title="Edit the live activity group metadata">Metadata</button></li>
<#if !(spaces?has_content)>
<li><button type="button" onclick="deleteActivityGroup()" title="Delete activity group on master">Delete</button></li>
</#if>
<li><button type="button" onclick="doAjaxCommand('forceshutdownliveactivities')" title="Force all live activities in the group to shut down">Force Shutdown</button></li>
</ul></div>

<div id="commandResult">
</div>

<#if liveactivitygroup.description?has_content><p>
${liveactivitygroup.description}
</p></#if>

<table>
<tr>
<th>ID</th>
<td>${liveactivitygroup.id}</td>
</tr>
<tr>
<th valign="top">Metadata</th>
<td><table><#list metadata as item>
<tr><th valign="top">${item.label}</th><td>${item.value}</td></tr>
</#list></table></td>
</tr>
</table>

<h3>Live Activities</h3>

<#if liveactivities?has_content>
<table>
<tr><th>Live Activity</th><th>Status</th><th>Up To Date?</th></tr>
<#list liveactivities as liveactivity>
<#assign trCss = (liveactivity_index % 2 == 0)?string("even","odd")>
<tr class="${trCss}">
<td><a href="/interactivespaces/liveactivity/${liveactivity.activity.id}/view.html">${liveactivity.activity.name}</a></td>
<td>
<#if liveactivity.active?has_content><div id="liveactivity-info-${liveactivity.activity.uuid}">
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
<#else>
<p>
None
</p>
</#if>

<h2>Containing Spaces</h2>

<#if spaces?has_content>

<ul>
<#list spaces as space>
<li><a href="/interactivespaces/space/${space.id}/view.html">${space.name}</a></li>
</#list>
</ul>
<#else>
<p>
None
</p>
</#if>

</body>
<html>