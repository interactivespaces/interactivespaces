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

<body>

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

<#include "/allpages_body_header.ftl">

<h1>Space: ${space.name}</h1>

<div class="commandBar"><ul>
<li><button type="button" id="startupButton" onclick="doAjaxCommand('startup')">Startup</button></li>
<li><button type="button" id="activateButton" onclick="doAjaxCommand('activate')">Activate</button></li>
<li><button type="button" d="deactivateButton" onclick="doAjaxCommand('deactivate')"">Deactivate</button></li>
<li><button type="button" id="shutdownButton" onclick="doAjaxCommand('shutdown')">Shutdown</button></li>
<li><button type="button" id="statusButton" onclick="doAjaxCommand('liveactivitystatus')">Status</button></li>
<li><button type="button" id="configureButton" onclick="doAjaxCommand('configure')">Configure</button></li>
<li><button type="button" id="deployButton" onclick="doAjaxCommand('deploy')">Deploy</button></li>
<li><button type="button" id="liveActivitiesButton" 
    onclick="window.location='/interactivespaces/space/${space.id}/liveactivities.html'" title="Get all live activities that are part of the space and its subspaces">Live Activities</button></li>
<li><button type="button" id="editButton" 
    onclick="window.location='/interactivespaces/space/${space.id}/edit.html'" title="Edit the space details">Edit</button></li>
<li><button type="button" id="editMetadataButton" 
    onclick="window.location='/interactivespaces/space/${space.id}/metadata/edit.html'" title="Edit the space metadata">Metadata</button></li>
<#if !(cspaces?has_content)>
<li><button type="button" onclick="deleteSpace()" title="Delete space on master">Delete</button></li>
</#if>
</ul></div>

<div id="commandResult">
</div>

<p>
${space.description}
</p>

<table>
<tr>
<th>ID</th>
<td>${space.id}</td>
</tr>
<tr>
<th valign="top">Metadata</th>
<td><table><#list metadata as item>
<tr><th valign="top">${item.label}</th><td>${item.value}</td></tr>
</#list></table></td>
</tr>
</table>

<h3>Live Activity Groups</h3>

<#if liveActivityGroups?has_content>
<ul>
<#list liveActivityGroups as group>
    <li><a href="/interactivespaces/liveactivitygroup/${group.id}/view.html">${group.name}</a></li>
</#list>
</ul>
<#else>
None
</#if>

<h3>Subspaces</h3>

<#if subspaces?has_content>
<ul>
<#list subspaces as subspace>
    <li><a href="/interactivespaces/space/${subspace.id}/view.html">${subspace.name}</a></li>
</#list>
</ul>
<#else>
None
</#if>


<h3>Containing Spaces</h3>

<#if cspaces?has_content>
<ul>
<#list cspaces as cspace>
    <li><a href="/interactivespaces/space/${cspace.id}/view.html">${cspace.name}</a></li>
</#list>
</ul>
<#else>
None
</#if>

</body>
<html>