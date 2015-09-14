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
<#import "/spring.ftl" as spring />
<html>
<head>
<title>Interactive Spaces Admin: Live Activity Groups</title>

<#include "/allpages_head.ftl">
</head>

<body class="admin-content">

<h2>New Live Activity Group</h2>

<form action="${flowExecutionUrl}" method="post">
<table>
<tr>
<td>Name</td>
<td>
<@spring.formInput path="liveActivityGroupForm.liveActivityGroup.name" />
<@spring.showErrors '<br>', 'fieldError' />
</td>
</tr>
<tr>
<td valign="top">Description</td>
<td>
<@spring.formTextarea path="liveActivityGroupForm.liveActivityGroup.description" attributes='rows="5" cols="40"' />
<@spring.showErrors '<br>', 'fieldError' />
</td>
</tr>
<tr>
<td>Live Activities</td>
<td>
<@spring.bind "liveActivityGroupForm.liveActivityIds"/>
<#if spring.status.value??>
<#assign selectedLiveActivities = spring.status.value>
<#else>
<#assign selectedLiveActivities = []>
</#if>
<select multiple="multiple" id="${spring.status.expression}" name="${spring.status.expression}" size='15'>
    <option value="--none--">-- None --</option>
<#list liveactivities?keys as value>
    <option value="${value?html}"<#if selectedLiveActivities?seq_contains(value)> selected="selected"</#if>>${liveactivities[value]?html}</option>
</#list>
</select>
</td>
</tr>
<tr>
<td>&nbsp;</td><td><input type="submit" name="_eventId_save" value="Save" />
<input type="submit" name="_eventId_cancel" value="Cancel" />   </td>
</tr>
</form>


</body>
<html>