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

<script type="text/javascript" src="/interactivespaces/js/jquery-ispopup.js"></script>

<script type="text/javascript">
function doAjaxCommandByUrl(url) {
  $.ajax({
      url: url,
      success: function(data) {
        $('#commandResult').html(data.result);
      }
  });
}

function shutdownAllActivitiesAllControllers() {
    if (confirm("Are you sure you want to shut down all live activities on all space controllers?")) {
        window.location='/interactivespaces/spacecontroller/all/activities/shutdown.html';
    }
}

$(document).ready(function() {
<#list liveactivities as liveactivity>
$('${"#liveactivity-info-${liveactivity.uuid}"}')
  .ispopup("#liveactivity-info-${liveactivity.uuid}-popup");
</#list>
});
</script>

</head>

<body class="admin-content">

<h1>Live Activities</h1>

<table class="commandBar">
  <tr>
    <#if canCreateLiveActivities>
      <#assign disabledAttribute = ''>
      <#assign title = 'Create a new live activity'>
    <#else>
      <#assign disabledAttribute = 'disabled'>
      <#assign title = 'Live activities cannot be created'>
    </#if>
  
    <td><button type="button" id="newButton" onclick="window.location='/interactivespaces/liveactivity/new.html?mode=embedded'" title="${title}" ${disabledAttribute}>New</button></td>
    <td><button type="button" id="nstatusAllButton" onclick="doAjaxCommandByUrl('/interactivespaces/spacecontroller/all/status.json')" title="Get the status of all Live Activities on all Space Controllers">Status All</button></td>
    <td><button type="button" id="shutdownActivitiesAllButton" onclick="shutdownAllActivitiesAllControllers();" title="Shutdown all activities on all connected space controllers">Shutdown All Activities</button></td>
  </tr>
</table>

<div id="commandResult">
</div>

<table class="liveactivity-list">
<tr><th>Name</th><th>Status</th><th></th><th>Up to date?</th></tr>

<#list liveactivities as liveactivity>
  <#assign trCss = (liveactivity_index % 2 == 0)?string("even","odd")>
  <tr class="${trCss}">
    <td class="liveactivity-name">
      <a class="uglylink" onclick="return ugly.changePage('/interactivespaces/liveactivity/${liveactivity.id}/view.html', event);">${liveactivity.name}</a>
    </td>
    <td>
      <#if liveactivity.active?has_content>
        <div class="status-box" id="liveactivity-info-${liveactivity.uuid}">
          <div class="status-box-inner liveactivity-status liveactivity-status-${liveactivity.active.runtimeState}">
            <@spring.message liveactivity.active.runtimeStateDescription />
          </div>
        </div>
        <div id="liveactivity-info-${liveactivity.uuid}-popup" class="liveactivity-info-popup">
          <div>
            <#if liveactivity.active.directRunning>
              Directly Running
            <#else>
              Not directly running
            </#if>
          </div>
          <div>
            <#if liveactivity.active.directActivated>
              Directly activated
            <#else>
              Not directly activated
            </#if>
          </div>
          <div>
            Running from ${liveactivity.active.numberLiveActivityGroupRunning} groups
          </div>
          <div>
            Activated from ${liveactivity.active.numberLiveActivityGroupActivated} groups
          </div>
        </div>
      <#else>
        <span style="color: red;">No controller assigned!</span>
      </#if>
    </td>
    <td>
      <#if liveactivity.active?has_content>
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
    <td>
      <#if liveactivity.outOfDate>
      <span title="Live Activity is out of date" class="out-of-date-indicator">
        <img src="/interactivespaces/img/outofdate.png" alt="Live Activity is out of date" />
      </span>
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

</body>
<html>