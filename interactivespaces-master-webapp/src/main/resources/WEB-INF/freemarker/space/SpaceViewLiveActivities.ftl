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
<#list liveActivityGroups as liveActivityGroup>
<#list liveActivityGroup.liveActivities as liveactivity>
$('${"#liveactivity-info-${liveactivity.uuid}"}')
  .ispopup("#liveactivity-info-${liveactivity.uuid}-popup");
</#list></#list>
});
</script>
</head>

<body class="admin-content">

<h1>Live Activities for Space: ${space.name?html}</h1>

<p><a class="uglylink" onclick="return ugly.changePage('/interactivespaces/space/${space.id}/view.html', event);">Back to main space page</a></p>

<table>
<tr><th>Live Activity</th><th>Status</th><th>Up to date?</th></td>
<#list liveActivityGroups as liveActivityGroup>
<tr style="background-color: #e0e0e0; font-weight: bold"><td colspan="3">Live Activity Group: <a class="uglylink" onclick="return ugly.changePage('/interactivespaces/liveactivitygroup/${liveActivityGroup.id}/view.html', event);">${liveActivityGroup.name?html}</a></td></tr>
<#list liveActivityGroup.liveActivities as liveactivity>
<#assign trCss = (liveactivity_index % 2 == 0)?string("even","odd")>
<tr class="${trCss}">
<td><a class="uglylink" onclick="return ugly.changePage('/interactivespaces/liveactivity/${liveactivity.id}/view.html', event);">${liveactivity.name?html}</a></td>
<td><#if liveactivity.active?has_content><div id="liveactivity-info-${liveactivity.uuid}">
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
</#list>
</table>

</body>
<html>