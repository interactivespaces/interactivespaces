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

<body class="admin-content">

<script type="text/javascript">
function doApiCommand(command) {
  ugly.executeApi("/liveactivity/" + command, {'id':'${liveactivity.id}'});
}

function doAjaxCommand(command) {
  $.ajax({
      url: '/interactivespaces/liveactivity/${liveactivity.id}/' + command + '.json',
      success: function(data) {
        $('#commandResult').html(data.result);
      }
  });
}

function cleanTempDataLiveActivity() {
    if (confirm("Are you sure you want to clean the tmp data for the live activity?")) {
        doAjaxCommand('cleantmpdata');
    }
}

function cleanPermanentDataLiveActivity() {
    if (confirm("Are you sure you want to clean the permanent data for the live activity?")) {
        doAjaxCommand('cleanpermanentdata');
    }
}

function deleteLiveActivity() {
    if (confirm("Are you sure you want to delete the live activity?")) {
        window.location='/interactivespaces/liveactivity/${liveactivity.id}/delete.html'
    }
}

function remoteDeleteLiveActivity() {
    if (confirm("Are you sure you want to delete the live activity from its space controller?")) {
        doAjaxCommand('remotedelete');
    }
}
</script>

<h1>Live Activity: ${liveactivity.name?html}</h1>

<table class="commandBar">
  <tr>
    <td><button type="button" id="startupButton" onclick="doAjaxCommand('startup')">Startup</button></td>
    <td><button type="button" id="activateButton" onclick="doAjaxCommand('activate')">Activate</button></td>
    <td><button type="button" id="deactivateButton" onclick="doAjaxCommand('deactivate')">Deactivate</button></td>
    <td><button type="button" id="shutdownButton" onclick="doAjaxCommand('shutdown')">Shutdown</button></td>
    <td><button type="button" id="statusButton" onclick="doAjaxCommand('status')">Status</button></td>
    <td><button type="button" id="configureButton" onclick="doAjaxCommand('configure')" title="Configure activity on the controller">Configure</button></td>
    <td><button type="button" id="deployButton" onclick="doAjaxCommand('deploy')" title="Deploy activity to the controller">Deploy</button></td>
  </tr>
  <tr>
    <td><button type="button" id="editButton" onclick="ugly.changePage('/interactivespaces/liveactivity/${liveactivity.id}/edit.html')" title="Edit the activity details">Edit</button></td>
    <td><button type="button" id="editConfigButton" onclick="ugly.changePage('/interactivespaces/liveactivity/${liveactivity.id}/config/edit.html')" title="Edit the activity configuration">Edit Config</button></td>
    <td><button type="button" id="editMetadataButton" onclick="ugly.changePage('/interactivespaces/liveactivity/${liveactivity.id}/metadata/edit.html')" title="Edit the live activity metadata">Metadata</button></td>
    <td><button type="button" onclick="cleanTempDataLiveActivity()" title="Delete temp data for live activity">Clean Tmp</button></td>
    <td><button type="button" onclick="cleanPermanentDataLiveActivity()" title="Delete permanent data for live activity">Clean Permanent</button></td>
    <td><button type="button" onclick="remoteDeleteLiveActivity()" title="Delete the live activity on its controller">Remote Delete</button></td>
    <#if liveactivityGroups?has_content>
      <#assign disabledAttribute = 'disabled'>
      <#assign title = 'Can not delete a live activity contained in a group'>
    <#else>
      <#assign disabledAttribute = ''>
      <#assign title = 'Delete activity on master'>
    </#if>
    <td><button type="button" onclick="deleteLiveActivity()" title="${title}" ${disabledAttribute}>Delete</button></td>
  </tr>
</table>

<div id="commandResult">
</div>

<#if liveactivity.description?has_content><p>
${liveactivity.description?html}
</p></#if>

<table class="liveactivity-details">
<tr>
<th>ID</th>
<td>${liveactivity.id}</td>
</tr>
<tr>
<th>UUID</th>
<td>${liveactivity.uuid}</td>
</tr>
<tr>
<th>Activity</th>
<td><a class="uglylink" onclick="return ugly.changePage('/interactivespaces/activity/${liveactivity.activity.id}/view.html', event)">${liveactivity.activity.name?html} - ${liveactivity.activity.version}</a></td>
</tr>
<th>Controller</th>
<td><#if liveactivity.controller?has_content>
<a class="uglylink" onclick="return ugly.changePage('/interactivespaces/spacecontroller/${liveactivity.controller.id}/view.html', event);">${liveactivity.controller.name?html}</a>
<#else>
<span style="color: red;">No controller assigned!</span>
</#if>
</td>
</tr>
<tr>
<th>Deployment</th>
<td>
Last: <#if liveactivity.lastDeployDate??>
      ${liveactivity.lastDeployDate}
    <#else>
      Unknown
    </#if>
<#if liveactivity.outOfDate>
<span title="Live Activity is out of date" class="out-of-date-indicator"><img src="/interactivespaces/img/outofdate.png" alt="Live Activity is out of date" /></span>
</#if>
<#if liveactivity.active.deployState != "READY">
&bull;
<span>
<@spring.message liveactivity.active.deployStateDescription />
<#if liveactivity.active.deployStateDetail?has_content>: ${liveactivity.active.deployStateDetail}</#if>
</span>
</#if>

</td>
</tr>
<tr>
<th>Status</th>
<td>
<#if liveactivity.active?has_content>
  <span class="status-box status-box-inner liveactivity-status liveactivity-status-${liveactivity.active.runtimeState}"><@spring.message liveactivity.active.runtimeStateDescription /></span>
  <span class="as-of-timestamp">
    as of
    <#if liveactivity.active.lastStateUpdate??>
      ${liveactivity.active.lastStateUpdate}
    <#else>
      Unknown
    </#if>
  </span>
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
<#assign metadataKeys = liveactivity.metadata?keys?sort>
<td><table><#list metadataKeys as metadataKey>
<tr><th>${metadataKey}</th><td>${liveactivity.metadata[metadataKey]?html}</td></tr>
</#list></table></td>
</tr>
</table>

<h2>Containing Live Activity Groups</h2>

<#if liveActivityGroups?has_content>

<ul>
<#list liveActivityGroups as liveActivityGroup>
    <li><a class="uglylink" onclick="return ugly.changePage('/interactivespaces/liveactivitygroup/${liveActivityGroup.id}/view.html', event);">${liveActivityGroup.name?html}</a></li>
</#list>
</ul>
<#else>
<p>
None
</p>
</#if>

<#if liveactivity.active.runtimeStateDetail?has_content>
<h2>Runtime State Details</h2>

<pre>
${liveactivity.active.runtimeStateDetail}
</pre>

</#if>


</body>
<html>