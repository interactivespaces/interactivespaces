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
</head>

<body>

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

<#include "/allpages_body_header.ftl">

<h1>Activity: ${activity.name}</h1>

<div class="commandBar"><ul>
<li><button type="button" id="deployButton" onclick="doAjaxCommand('deploy')" title="Deploy all out of date Live Activities based on this Activity">Update Deployments</button></li>
<li><button type="button" id="editButton" onclick="window.location='/interactivespaces/activity/${activity.id}/edit.html'" title="Edit the activity details">Edit</button></li>
<li><button type="button" id="editMetadataButton" 
    onclick="window.location='/interactivespaces/activity/${activity.id}/metadata/edit.html'" title="Edit the activity metadata">Metadata</button></li>
<#if !(liveactivities?has_content)>    
<li><button type="button" onclick="deleteActivity()" title="Delete activity on master">Delete</button></li>
</#if>
</ul></div>

<div id="commandResult">
</div>

<#if activity.description?has_content><p>
${activity.description}
</p></#if>

<table>
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
<th valign="top">Metadata</th>
<td><table><#list metadata as item>
<tr><th>${item.label}</th><td>${item.value}</td></tr>
</#list></table></td>
</tr>
</table>

<h2>Containing Live Activities</h2>

<#if liveactivities?has_content>

<table>
<tr><th>Live Activity</th><th>Status</th><th>Up to date?</th></td>

<#list liveactivities as liveactivity>
<#assign trCss = (liveactivity_index % 2 == 0)?string("even","odd")>
    <tr class="${trCss}">
    <td><a href="/interactivespaces/liveactivity/${liveactivity.activity.id}/view.html">${liveactivity.activity.name}</a></td>
<td><#if liveactivity.active?has_content>
<@spring.message liveactivity.active.state.description />
 as of 
  <#if liveactivity.active.lastStateUpdate??>
    ${liveactivity.active.lastStateUpdateDate?datetime}
  <#else>
    Unknown
  </#if>
<#else>
<span style="color: red;">No controller assigned!</span>
</#if></td>
<td>
<#if liveactivity.activity.outOfDate>
<span title="Live Activity is out of date"><img src="/interactivespaces/img/outofdate.png" alt="Live Activity is out of date" /></span>
</#if>
</td>
    </tr>
<#if liveactivity.active?has_content>
<tr class="${trCss}">
<td>&nbsp;</td>
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
<td>&nbsp;</td>
</tr>
</#if>
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