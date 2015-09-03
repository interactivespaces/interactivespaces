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
$('${"#liveactivity-info-${liveactivity.uuid}"}')
  .ispopup("#liveactivity-info-${liveactivity.uuid}-popup");
</#list>
});
</script>
</head>

<body class="admin-content">

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

<h2>Live Activity Group: ${liveactivitygroup.name?html}</h2>

<table class="commandBar">
  <tr>
    <td><button type="button" id="startupButton" onclick="doAjaxCommand('startup')">Startup</button></td>
    <td><button type="button" id="activateButton" onclick="doAjaxCommand('activate')">Activate</button></td>
    <td><button type="button" id="deactivateButton" onclick="doAjaxCommand('deactivate')">Deactivate</button></td>
    <td><button type="button" id="shutdownButton" onclick="doAjaxCommand('shutdown')">Shutdown</button></td>
    <td><button type="button" id="statusButton" onclick="doAjaxCommand('liveactivitystatus')">Status</button></td>
    <td><button type="button" id="configureButton" onclick="doAjaxCommand('configure')">Configure</button></td>
    <td><button type="button" id="deployButton" onclick="doAjaxCommand('deploy')">Deploy</button></td>
  </tr>
  <tr>
    <td><button type="button" id="editButton" onclick="window.location='/interactivespaces/liveactivitygroup/${liveactivitygroup.id}/edit.html'" title="Edit the live activity group details">Edit</button></td>
    <td><button type="button" id="editMetadataButton" onclick="window.location='/interactivespaces/liveactivitygroup/${liveactivitygroup.id}/metadata/edit.html'" title="Edit the live activity group metadata">Metadata</button></td>
   <td><button type="button" id="cloneButton" onclick="window.location='/interactivespaces/liveactivitygroup/${liveactivitygroup.id}/clone.html'" title="Clone the live activity group">Clone</button></td>
     <#if spaces?has_content>
      <#assign disabledAttribute = 'disabled'>
      <#assign title = 'Can not delete activity group in a space'>
    <#else>
      <#assign disabledAttribute = ''>
      <#assign title = 'Delete activity group on master'>
    </#if>
    <td><button type="button" onclick="deleteActivityGroup()" title="${title}" ${disabledAttribute}>Delete</button></td>
    <td><button type="button" onclick="doAjaxCommand('forceshutdownliveactivities')" title="Force all live activities in the group to shut down">Force Shutdown</button></td>
  </tr>
</table>

<div id="commandResult">
</div>

<#if liveactivitygroup.description?has_content><p>
${liveactivitygroup.description?html}
</p></#if>

<table>
<tr>
<th>ID</th>
<td>${liveactivitygroup.id}</td>
</tr>
<tr>
<th valign="top">Metadata</th>
<#assign metadataKeys = liveactivitygroup.metadata?keys?sort>
<td><table><#list metadataKeys as metadataKey>
<tr><th valign="top">${metadataKey}</th><td>${liveactivitygroup.metadata[metadataKey]?html}</td></tr>
</#list></table></td>
</tr>
</table>

<h3>Live Activities</h3>

<#if liveactivities?has_content>
<table>
<tr>
  <th>Live Activity</th>
  <th>Status</th>
  <th>Up To Date?</th>
</tr>
<#list liveactivities as liveactivity>
<#assign trCss = (liveactivity_index % 2 == 0)?string("even","odd")>
<tr class="${trCss}">
<td><a class="uglylink" onclick="return ugly.changePage('/interactivespaces/liveactivity/${liveactivity.id}/view.html', event);">${liveactivity.name?html}</a></td>
<td>
<#if liveactivity.active?has_content><div id="liveactivity-info-${liveactivity.uuid}">
  <span class="status-box status-box-inner liveactivity-status liveactivity-status-${liveactivity.active.runtimeState}"><@spring.message liveactivity.active.runtimeStateDescription /></span>
  <span class="as-of-timestamp">
    as of
    <#if liveactivity.active.lastStateUpdate??>
      ${liveactivity.active.lastStateUpdate}
    <#else>
      Unknown
    </#if>
  </span>
</div>
<div id="liveactivity-info-${liveactivity.uuid}-popup" class="liveactivity-info-popup">
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
<#if liveactivity.outOfDate>
<span title="Live Activity is out of date" class="out-of-date-indicator"><img src="/interactivespaces/static/img/outofdate.png" alt="Live Activity is out of date" /></span>
</#if>
<#if liveactivity.active.deployState != "READY">
<span>
<@spring.message liveactivity.active.deployStateDescription />
<#if liveactivity.active.deployStateDetail?has_content>: ${liveactivity.active.deployStateDetail}</#if>
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
<li><a class="uglylink" onclick="return ugly.changePage('/interactivespaces/space/${space.id}/view.html', event);">${space.name?html}</a></li>
</#list>
</ul>
<#else>
<p>
None
</p>
</#if>

</body>
<html>