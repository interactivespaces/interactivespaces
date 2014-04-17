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

<#include "/allpages_head.ftl" >
</head>

<body class="admin-content">

<h1>Edit Space: ${form.space.name?html}</h1>

<form  method="post">

<table>
<tr>
<td>Name</td>
<td>
<@spring.formInput path="form.space.name" />
<@spring.showErrors '<br>', 'fieldError' />
 </td>

</tr>
<tr>
<td valign="top">Description</td><td><@spring.formTextarea path="form.space.description" attributes='rows="5" cols="40"' /></td>
</tr>
<tr>
<td>Live Activity Groups</td>
<td>
<@spring.bind "form.liveActivityGroupIds"/>
<#if spring.status.value??>
<#assign selectedGroups = spring.status.value?split(",")>
<#else>
<#assign selectedGroups = []>
</#if>
<select multiple="multiple" id="${spring.status.expression}" name="${spring.status.expression}" size='15'>
    <option value="--none--">-- None --</option>
    <#list liveactivitygroups?keys as value>
    <option value="${value?html}"<#if selectedGroups?seq_contains(value)> selected="selected"</#if>>${liveactivitygroups[value]?html}</option>
    </#list>
</select>
</td>
</tr>
<tr>
<td>Subspaces</td>
<td>
<@spring.bind "form.spaceIds"/>
<#if spring.status.value??>
<#assign selectedSpaces = spring.status.value?split(",")>
<#else>
<#assign selectedSpaces = []>
</#if>
<select multiple="multiple" id="${spring.status.expression}" name="${spring.status.expression}" size='15'>
    <option value="--none--">-- None --</option>
    <#list spaces?keys as value>
    <option value="${value?html}"<#if selectedSpaces?seq_contains(value)> selected="selected"</#if>>${spaces[value]?html}</option>
    </#list>
</select>
</td>
</tr>

<tr>
<th>&nbsp;</th>
<td>
<input type="submit" value="Save" />
<button type="button" id="cancelButton" onclick="window.location='/interactivespaces/space/${id}/view.html'" title="Cancel the edit">Cancel</button>
</td>
</table>

</form>

</body>
<html>