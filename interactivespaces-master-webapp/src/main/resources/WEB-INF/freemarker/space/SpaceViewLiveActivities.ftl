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
<title>Interactive Spaces Admin: Spaces</title>

<#include "/allpages_head.ftl">

<script type="text/javascript" src="/interactivespaces/js/jquery-ispopup.js"></script>

<script type="text/javascript">
$(document).ready(function() {
<#list liveactivitygroups as liveactivitygroup>
<#list liveactivitygroup.liveActivities as liveactivity>
$('${"#liveactivity-info-${liveactivity.activity.uuid}"}')
  .ispopup("#liveactivity-info-${liveactivity.activity.uuid}-popup");
</#list></#list>
});
</script>
</head>

<body>

<#include "/allpages_body_header.ftl">

<h1>Live Activities for Space: ${space.name}</h1>

<p><a href="view.html">Back to main space page</a></p>

<table>
<tr><th>Live Activity</th><th>Status</th><th>Up to date?</th></td>
<#list liveactivitygroups as liveactivitygroup>
<tr style="background-color: #e0e0e0; font-weight: bold"><td colspan="3">Live Activity Group: <a href="/interactivespaces/liveactivitygroup/${liveactivitygroup.liveActivityGroup.id}/view.html">${liveactivitygroup.liveActivityGroup.name}</a></td></tr>
<#list liveactivitygroup.liveActivities as liveactivity>
<#assign trCss = (liveactivity_index % 2 == 0)?string("even","odd")>
<tr class="${trCss}">
<td><a href="/interactivespaces/liveactivity/${liveactivity.activity.id}/view.html">${liveactivity.activity.name}</a></td>
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
</#list>
</table>

</body>
<html>