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
</head>

<body class="admin-content">

<script type="text/javascript">
function doAjaxCommand(command) {
  $.ajax({
      url: '/interactivespaces/space/${space.id}/' + command + '.json',
      success: function(data) {
        $('#commandResult').html(data.result);
      }
  });
}

function deleteSpace() {
    if (confirm("Are you sure you want to delete the space?")) {
        window.location='/interactivespaces/space/${space.id}/delete.html'
    }
}
</script>

<h1>Space: ${space.name?html}</h1>

<table class="commandBar">
  <tr>
    <td><button type="button" id="startupButton" onclick="doAjaxCommand('startup')">Startup</button></td>
    <td><button type="button" id="activateButton" onclick="doAjaxCommand('activate')">Activate</button></td>
    <td><button type="button" id="deactivateButton" onclick="doAjaxCommand('deactivate')"">Deactivate</button></td>
    <td><button type="button" id="shutdownButton" onclick="doAjaxCommand('shutdown')">Shutdown</button></td>
    <td><button type="button" id="statusButton" onclick="doAjaxCommand('liveactivitystatus')">Status</button></td>
    <td><button type="button" id="configureButton" onclick="doAjaxCommand('configure')">Configure</button></td>
    <td><button type="button" id="deployButton" onclick="doAjaxCommand('deploy')">Deploy</button></td>
  </tr>
  <tr>
    <td><button type="button" id="liveActivitiesButton" onclick="window.location='/interactivespaces/space/${space.id}/liveactivities.html'" title="Get all live activities that are part of the space and its subspaces">Live Activities</button></td>
    <td><button type="button" id="editButton" onclick="window.location='/interactivespaces/space/${space.id}/edit.html'" title="Edit the space details">Edit</button></td>
    <td><button type="button" id="editMetadataButton" onclick="window.location='/interactivespaces/space/${space.id}/metadata/edit.html'" title="Edit the space metadata">Metadata</button></td>
    <td><button type="button" id="cloneButton" onclick="window.location='/interactivespaces/space/${space.id}/clone.html'" title="Clone the space">Clone</button></td>
    <#if containingSpaces?has_content>
      <#assign disabledAttribute = 'disabled'>
      <#assign title = 'Can not delete a space contained in other spaces'>
    <#else>
      <#assign disabledAttribute = ''>
      <#assign title = 'Delete space on master'>
    </#if>
    <td><button type="button" onclick="deleteSpace()" title="${title}" ${disabledAttribute}>Delete</button></td>
  </tr>
</table>

<div id="commandResult">
</div>

<p>
${space.description?html}
</p>

<table>
<tr>
<th>ID</th>
<td>${space.id}</td>
</tr>
<tr>
<th valign="top">Metadata</th>
<td><table><#assign metadataKeys = space.metadata?keys?sort><#list metadataKeys as metadataKey>
<tr><th valign="top">${metadataKey}</th><td>${space.metadata[metadataKey]?html}</td></tr>
</#list></table></td>
</tr>
</table>

<h3>Live Activity Groups</h3>

<#if liveActivityGroups?has_content>
<ul>
<#list liveActivityGroups as group>
    <li><a class="uglylink" onclick="return ugly.changePage('/interactivespaces/liveactivitygroup/${group.id}/view.html', event);">${group.name?html}</a></li>
</#list>
</ul>
<#else>
None
</#if>

<h3>Subspaces</h3>

<#if subspaces?has_content>
<ul>
<#list subspaces as subspace>
    <li><a class="uglylink" onclick="ugly.changePage('/interactivespaces/space/${subspace.id}/view.html')">${subspace.name}</a></li>
</#list>
</ul>
<#else>
None
</#if>


<h3>Containing Spaces</h3>

<#if containingSpaces?has_content>
<ul>
<#list containingSpaces as containingSpace>
    <li><a class="uglylink" onclick="return ugly.changePage('/interactivespaces/space/${containingSpace.id}/view.html', event);">${containingSpace.name?html}</a></li>
</#list>
</ul>
<#else>
None
</#if>

</body>
<html>