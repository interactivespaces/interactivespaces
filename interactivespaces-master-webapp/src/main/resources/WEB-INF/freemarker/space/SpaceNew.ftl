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
<title>Interactive Spaces Admin: Spaces</title>

<#include "/allpages_head.ftl">
</head>

<body class="admin-content">

<h2>New Space</h2>

<form action="${flowExecutionUrl}" method="post">
<table>
<tr>
<td>Name</td><td><@spring.formInput path="form.space.name" /></td>
</tr>
<tr>
<td valign="top">Description</td><td><@spring.formTextarea path="form.space.description" attributes='rows="5" cols="40"' /></td>
</tr>
<tr>
<td>Live Activity Groups</td><td><@spring.formMultiSelect "form.liveActivityGroupIds", liveactivitygroups, "size='15'" /></td>
</tr>
<#if spaces?has_content><tr>
<td>Spaces</td><td><@spring.formMultiSelect "form.spaceIds", spaces, "size='15'" /></td>
</tr></#if>
<tr>
<td>&nbsp;</td><td><input type="submit" name="_eventId_save" value="Save" />
<input type="submit" name="_eventId_cancel" value="Cancel" />   </td>
</tr>
</form>


</body>
<html>