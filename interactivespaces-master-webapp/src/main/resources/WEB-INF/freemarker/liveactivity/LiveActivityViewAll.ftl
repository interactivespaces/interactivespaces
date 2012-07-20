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
<title>Interactive Spaces Admin: Live Activities</title>

<#include "/allpages_head.ftl">
</head>

<body>

<#include "/allpages_body_header.ftl">

<h1>Live Activities</h1>

<div class="commandBar"><ul>
<li><button type="button" id="newButton" onclick="window.location='/interactivespaces/liveactivity/new.html?mode=embedded'" title="Create a new live activity">New</button></li>
</ul></div>

<div id="commandResult">
</div>

<table>
<tr><th>Name</th><th>Status</th><th>Up to date?</th></tr>

<#list liveactivities as liveactivity>
<#assign trCss = (liveactivity_index % 2 == 0)?string("even","odd")>
<tr class="${trCss}">
<td><a href="${liveactivity.activity.id}/view.html">${liveactivity.activity.name}</a></td>
<td><#if liveactivity.active?has_content>
<span class="liveactivity-status liveactivity-status-${liveactivity.active.state.name()}"><@spring.message liveactivity.active.state.description /></span>
 as of 
  <#if liveactivity.active.lastStateUpdate??>
    ${liveactivity.active.lastStateUpdateDate?datetime}
  <#else>
    Unknown
  </#if>
<#else>
<span style="color: red;">No controller assigned!</span>
</#if>
</td>
<td>
<#if liveactivity.activity.outOfDate>
<span title="Live Activity is out of date"><img src="/interactivespaces/img/outofdate.png" alt="Live Activity is out of date" /></span>
</#if>
</td>
    </tr>
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
</#list>
</table>

</body>
<html>