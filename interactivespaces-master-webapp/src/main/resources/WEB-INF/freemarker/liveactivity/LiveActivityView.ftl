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
  ugly.executeApi("/liveactivity/" + command, {'id':'${liveActivity.id}'});
}

function doAjaxCommand(command) {
  $.ajax({
      url: '/interactivespaces/liveactivity/${liveActivity.id}/' + command + '.json',
      success: function(data) {
        $('#commandResult').html(data.result);
      }
  });
}

function cleanTempDataLiveActivity() {
    if (confirm("Are you sure you want to clean the tmp data for the activity?")) {
        doAjaxCommand('cleantmpdata');
    }
}

function cleanPermanentDataLiveActivity() {
    if (confirm("Are you sure you want to clean the permanent data for the activity?")) {
        doAjaxCommand('cleanpermanentdata');
    }
}

function deleteLiveActivity() {
    if (confirm("Are you sure you want to delete the activity?")) {
        window.location='/interactivespaces/liveactivity/${liveActivity.id}/delete.html'
    }
}

function remoteDeleteLiveActivity() {
    if (confirm("Are you sure you want to delete the activity from its controller?")) {
        doAjaxCommand('remotedelete');
    }
}
</script>

<h1>Live Activity: ${liveActivity.name}</h1>

<table class="commandBar">
  <tr>
    <td><button type="button" id="startupButton" onclick="doApiCommand('startup')">Startup</button></td>
    <td><button type="button" id="activateButton" onclick="doApiCommand('activate')">Activate</button></td>
    <td><button type="button" id="deactivateButton" onclick="doApiCommand('deactivate')">Deactivate</button></td>
    <td><button type="button" id="shutdownButton" onclick="doApiCommand('shutdown')">Shutdown</button></td>
    <td><button type="button" id="statusButton" onclick="doApiCommand('status')">Status</button></td>
    <td><button type="button" id="configureButton" onclick="doApiCommand('configure')" title="Configure activity on the controller">Configure</button></td>
    <td><button type="button" id="deployButton" onclick="doApiCommand('deploy')" title="Deploy activity to the controller">Deploy</button></td>
  </tr>
  <tr>
    <td><button type="button" id="editButton" onclick="ugly.changePage('/interactivespaces/liveactivity/${liveActivity.id}/edit.html')" title="Edit the activity details">Edit</button></td>
    <td><button type="button" id="editConfigButton" onclick="ugly.changePage('/interactivespaces/liveactivity/${liveActivity.id}/config/edit.html')" title="Edit the activity configuration">Edit Config</button></td>
    <td><button type="button" id="editMetadataButton" onclick="ugly.changePage('/interactivespaces/liveactivity/${liveActivity.id}/metadata/edit.html')" title="Edit the live activity metadata">Metadata</button></td>
    <td><button type="button" onclick="cleanTempDataLiveActivity()" title="Delete temp data for live activity">Clean Tmp</button></td>
    <td><button type="button" onclick="cleanPermanentDataLiveActivity()" title="Delete permanent data for live activity">Clean Permanent</button></td>
    <td><button type="button" onclick="remoteDeleteLiveActivity()" title="Delete the live activity on its controller">Remote Delete</button></td>
    <#if liveActivityGroups?has_content>
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

<#if liveActivity.description?has_content><p>
${liveActivity.description}
</p></#if>

<table class="liveactivity-details">
<tr>
<th>ID</th>
<td>${liveActivity.id}</td>
</tr>
<tr>
<th>UUID</th>
<td>${liveActivity.uuid}</td>
</tr>
<tr>
<th>Activity</th>
<td><a class="uglylink" onclick="ugly.changePage('/interactivespaces/activity/${liveActivity.activity.id}/view.html')">${liveActivity.activity.name} - ${liveActivity.activity.version}</a></td>
</tr>
<th>Controller</th>
<td><#if liveActivity.controller?has_content>
<a class="uglylink" onclick="ugly.changePage('/interactivespaces/spacecontroller/${liveActivity.controller.id}/view.html')">${liveActivity.controller.name}</a>
<#else>
<span style="color: red;">No controller assigned!</span>
</#if>
</td>
</tr>
<tr>
<th>Deployment</th>
<td>
Last: <#if liveActivity.lastDeployDate??>
      ${liveActivity.lastDeployDate}
    <#else>
      Unknown
    </#if>
<#if liveActivity.outOfDate>
<span title="Live Activity is out of date" class="out-of-date-indicator"><img src="/interactivespaces/img/outofdate.png" alt="Live Activity is out of date" /></span>
</#if>
<#if liveActivity.active.deployState != "READY">
&bull;
<span>
<@spring.message liveActivity.active.deployStateDescription />
</span>
</#if>

</td>
</tr>
<tr>
<th>Status</th>
<td>
<#if liveActivity.active?has_content>
  <span class="status-box status-box-inner liveactivity-status liveactivity-status-${liveActivity.active.runtimeState}"><@spring.message liveActivity.active.runtimeStateDescription /></span>
  <span class="as-of-timestamp">
    as of
    <#if liveActivity.active.lastStateUpdate??>
      ${liveActivity.active.lastStateUpdate}
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
<#if liveActivity.active.directRunning>
Directly Running
<#else>
Not Directly Running
</#if>
&bull;
<#if liveActivity.active.directActivated>
Directly Activated
<#else>
Not Directly Activated
</#if>
&bull;
running from ${liveActivity.active.numberLiveActivityGroupRunning} groups
&bull;
activated from ${liveActivity.active.numberLiveActivityGroupActivated} groups
</td>
</tr>
<tr>
<th valign="top">Metadata</th>
<#assign metadataKeys = liveActivity.metadata?keys?sort>
<td><table><#list metadataKeys as metadataKey>
<tr><th>${metadataKey}</th><td>${liveActivity.metadata[metadataKey]}</td></tr>
</#list></table></td>
</tr>
</table>

<h2>Containing Live Activity Groups</h2>

<#if liveActivityGroups?has_content>

<ul>
<#list liveActivityGroups as liveActivityGroup>
    <li><a class="uglylink" onclick="ugly.changePage('/interactivespaces/liveactivitygroup/${liveActivityGroup.id}/view.html')">${liveActivityGroup.name}</a></li>
</#list>
</ul>
<#else>
<p>
None
</p>
</#if>

<#if liveActivity.active.runtimeStateDetail?has_content>
<h2>Runtime State Details</h2>

<pre>
${liveActivity.active.runtimeStateDetail}
</pre>

</#if>


</body>
<html>