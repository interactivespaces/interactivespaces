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
<title>Interactive Spaces Admin: Controllers</title>

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
      url: '/interactivespaces/spacecontroller/${spacecontroller.id}/' + command + '.json',
      success: function(data) {
        $('#commandResult').html(data.result);
      }
  });
}

function cleanActivitiesTempDataSpaceController() {
    if (confirm("Are you sure you want to clean the tmp data for all live activities on the space controller?")) {
        doAjaxCommand('cleanactivitiestmpdata');
    }
}

function cleanActivitiesPermanentDataSpaceController() {
    if (confirm("Are you sure you want to clean the permanent data for all live activities on the space controller?")) {
        doAjaxCommand('cleanactivitiespermanentdata');
    }
}

function cleanTempDataSpaceController() {
    if (confirm("Are you sure you want to clean the tmp data for the space controller?")) {
        doAjaxCommand('cleantmpdata');
    }
}

function cleanPermanentDataSpaceController() {
    if (confirm("Are you sure you want to clean the permanent data for the space controller?")) {
        doAjaxCommand('cleanpermanentdata');
    }
}

function deleteController() {
    if (confirm("Are you sure you want to delete the controller?")) {
        window.location='/interactivespaces/spacecontroller/${spacecontroller.id}/delete.html'
    }
}
function shutdownController() {
    if (confirm("Are you sure you want to shutdown the controller?")) {
       doAjaxCommand('shutdown');
    }
}

function captureData() {
  doAjaxCommand('capturedata');
}

function restoreData() {
  doAjaxCommand('restoredata');
}

function shutdownActivities() {
    if (confirm("Are you sure you want to shut down all applications on the controller?")) {
        doAjaxCommand('activities/shutdown');
    }
}
</script>

<h1>Space Controller: ${spacecontroller.name?html}</h1>

<table class="commandBar">
  <tr>
    <td><button type="button" onclick="doAjaxCommand('connect')">Connect</button></td>
    <td><button type="button" onclick="doAjaxCommand('disconnect')">Disconnect</button></td>
    <td><button type="button" onclick="doAjaxCommand('status')">Status</button></td>
    <td><button type="button" id="editButton" onclick="window.location='/interactivespaces/spacecontroller/${spacecontroller.id}/edit.html'" title="Edit the application details">Edit</button></td>
    <td><button type="button" id="editMetadataButton" onclick="window.location='/interactivespaces/spacecontroller/${spacecontroller.id}/metadata/edit.html'" title="Edit the space controller metadata">Metadata</button></td>
    <td><button type="button" id="deployButton" onclick="doAjaxCommand('deploy')" title="Deploy all Live Activities on this Controller">Deploy</button></td>
    <td><button type="button" onclick="captureData()" title="Capture controller data">Capture Data</button></td>
  </tr>
  <tr>
    <td><button type="button" onclick="cleanActivitiesTempDataSpaceController()" title="Clean the tmp data for all live activities on the controller">Clean Activities Tmp</button></td>
    <td><button type="button" onclick="cleanActivitiesPermanentDataSpaceController()" title="Clean the permanent data for all live activities on the controller">Clean Activities Permanent</button></td>
    <td><button type="button" onclick="cleanTempDataSpaceController()" title="Clean the controller's tmp data">Clean Tmp</button></td>
    <td><button type="button" onclick="cleanPermanentDataSpaceController()" title="Clean the controller's permanent data">Clean Permanent</button></td>
    <td><button type="button" onclick="shutdownController()" title="Shutdown the controller">Shutdown</button></td>
    <td><button type="button" onclick="shutdownActivities()" title="Shutdown all activities on the controller">Shutdown All Activities</button></td>
    <td><button type="button" onclick="restoreData()" title="Restore controller data">Restore Data</button></td>
    <#if liveactivities?has_content>
      <#assign disabledAttribute = 'disabled'>
      <#assign title = 'Can not delete controller containing live activities'>
    <#else>
      <#assign disabledAttribute = ''>
      <#assign title = 'Delete space controller on master'>
    </#if>
    <td><button type="button" onclick="deleteController()" title="${title}" ${disabledAttribute}>Delete</button></td>
  </tr>
</table>

<div id="commandResult">
</div>

<#if spacecontroller.description?has_content><p>
${spacecontroller.description?html}
</p></#if>

<table class="controller-details">
<tr>
<th>ID</th>
<td>${spacecontroller.id}</td>
</tr>
<tr>
<th>UUID</th>
<td>${spacecontroller.uuid}</td>
</tr>
<tr>
<th>Host ID</th>
<td>${spacecontroller.hostId}</td>
</tr>
<tr>
  <th>Mode</th>
  <td>
    <#if spacecontroller.mode??>
      <span class="status-box status-box-inner spacecontroller-mode spacecontroller-mode-${spacecontroller.mode}">
        <@spring.message spacecontroller.modeDescription />
      </span>
    </#if>
  </td>
</tr>
<tr>
  <th>Status</th>
  <td>
    <#if spacecontroller.lastStateUpdate??>
      <#assign t  = spacecontroller.lastStateUpdate>
    <#else>
      <#assign t = 'Unknown'>
    </#if>
    <span class="status-box status-box-inner spacecontroller-status spacecontroller-status-${spacecontroller.state}">
      <@spring.message spacecontroller.stateDescription />
    </span>
    <span class="as-of-timestamp">as of ${t}</span>
  </td>
</tr>
<tr>
<th valign="top">Metadata</th>
<td><table><#assign metadataKeys = spacecontroller.metadata?keys?sort><#list metadataKeys as metadataKey>
<tr><th>${metadataKey}</th><td>${spacecontroller.metadata[metadataKey]?html}</td></tr>
</#list></table></td>
</tr>
</table>

<h2>Live Activities</h2>

<#if liveactivities?has_content>

  <table class="liveactivity-list">
  <tr><th>Name</th><th>Status</th><th></th><th>Up to date?</th></tr>

  <#list liveactivities as liveactivity>
    <#assign trCss = (liveactivity_index % 2 == 0)?string("even","odd")>
    <tr class="${trCss}">
      <td class="liveactivity-name">
        <a class="uglylink" onclick="return ugly.changePage('/interactivespaces/liveactivity/${liveactivity.id}/view.html', event);">${liveactivity.name?html}</a>
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
<#else>
  <p>
  None
  </p>
</#if>
<h2>Advanced Properties</h2>
<table class="advanced-properties">
  <tr>
    <td class="databundle-header">Data Bundle State</td>
    <td class="databundle-value">
      <#if spacecontroller.lastDataBundleStateUpdate??>
        <#assign t  = spacecontroller.lastDataBundleStateUpdateDate?datetime>
      <#else>
        <#assign t = 'Unknown'>
      </#if>
      <span class="as-of-timestamp">
        <@spring.message spacecontroller.dataBundleStateDescription /> as of ${t}
      </span>
    </td>
  </tr>
</table>

</body>
<html>