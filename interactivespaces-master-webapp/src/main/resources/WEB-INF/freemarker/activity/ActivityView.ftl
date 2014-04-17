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
<title>Interactive Spaces Admin: Activities</title>

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
      url: '/interactivespaces/activity/${activity.id}/' + command + '.json',
      success: function(data) {
        $('#commandResult').html(data.result);
      }
  });
}

function deleteActivity() {
    if (confirm("Are you sure you want to delete the activity?")) {
        window.location='/interactivespaces/activity/${activity.id}/delete.html'
    }
}
</script>

<h1>Activity: ${activity.name?html}</h1>

<table class="commandBar">
  <tr>
    <td><button type="button" id="deployButton" onclick="doAjaxCommand('deploy')" title="Deploy all out of date Live Activities based on this Activity">Update Deployments</button></td>
    <td><button type="button" id="editButton" onclick="window.location='/interactivespaces/activity/${activity.id}/edit.html'" title="Edit the activity details">Edit</button></td>
    <td><button type="button" id="editMetadataButton" onclick="window.location='/interactivespaces/activity/${activity.id}/metadata/edit.html'" title="Edit the activity metadata">Metadata</button></td>
    <#if liveactivities?has_content>
      <#assign disabledAttribute = 'disabled'>
      <#assign title = 'Can not delete activity in a group'>
    <#else>
      <#assign disabledAttribute = ''>
      <#assign title = 'Delete activity on master'>
    </#if>
    <td><button type="button" onclick="deleteActivity()" title="${title}" ${disabledAttribute}>Delete</button></td>
  </tr>
</table>

<div id="commandResult">
</div>

<#if activity.description?has_content><p>
${activity.description?html}
</p></#if>

<table class="activity-details">
<tr>
<th>Version</th>
<td>${activity.version}</td>
</tr>
<tr>
<th>Identifying Name</th>
<td>${activity.identifyingName}</td>
</tr>
  <tr>
    <th>Last Uploaded</th>
    <td>${activity.lastUploadDate?datetime}</td>
  </tr>
  <tr>
    <th>Last Started</th>
    <td><#if activity.lastStartDate??>${activity.lastStartDate?datetime}</#if></td>
  </tr>
  <tr>
    <th>Bundle Content Hash</th>
    <td class="bundle-content-hash"><#if activity.bundleContentHash??>${activity.bundleContentHash}</#if></td>
  </tr>
<tr>
<th valign="top">Metadata</th>
<#assign metadataKeys = activity.metadata?keys?sort>
<td><table><#list metadataKeys as metadataKey>
<tr><th>${metadataKey}</th><td>${activity.metadata[metadataKey]?html}</td></tr>
</#list></table></td>
</tr>
</table>

<h2>Containing Live Activities</h2>

<#if liveactivities?has_content>

<table>
<tr><th>Live Activity</th><th>Status</th><th>Up to date?</th></tr>

<#list liveactivities as liveactivity>
<#assign trCss = (liveactivity_index % 2 == 0)?string("even","odd")>
    <tr class="${trCss}">
    <td><a class="uglylink" onclick="return ugly.changePage('/interactivespaces/liveactivity/${liveactivity.id}/view.html', event);">${liveactivity.name?html}</a></td>
<td><#if liveactivity.active?has_content><div id="liveactivity-${liveactivity.uuid}">
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
<#else>
<span style="color: red;">No controller assigned!</span>
</#if>
</td>
<td>
<#if liveactivity.outOfDate>
<span title="Live Activity is out of date" class="out-of-date-indicator"><img src="/interactivespaces/img/outofdate.png" alt="Live Activity is out of date" /></span>
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

<h2>Dependencies</h2>

<#if activity.dependencies?has_content>
<table>
    <tr>
      <th>Name</th>
      <th>Minimum Version</th>
      <th>Maximum Version</th>
      <th>Required?</th>
    </tr>

<#list activity.dependencies as dependency>
    <tr>
      <td>${dependency.name}</td>
      <td>${dependency.minimumVersion}</td>
      <td>${dependency.maximumVersion}</td>
      <td><#if dependency.required>Required<#else>Not Required</#if></td>
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