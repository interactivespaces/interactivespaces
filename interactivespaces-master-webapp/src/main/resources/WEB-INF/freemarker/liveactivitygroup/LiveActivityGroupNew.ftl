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

<body>

<#include "/allpages_body_header.ftl">

<h2>New Live Activity Group</h2>

<form action="${flowExecutionUrl}" method="post">
<table>
<tr>
<td>Name</td><td><@spring.formInput path="form.liveActivityGroup.name" /></td>
</tr>
<tr>
<td valign="top">Description</td><td><@spring.formTextarea path="form.liveActivityGroup.description" attributes='rows="5" cols="40"' /></td>
</tr>
<tr>
<td>Live Activities</td><td><@spring.formMultiSelect "form.liveActivityIds", liveactivities, "size='15'" /></td>
</tr>
<tr>
<td>&nbsp;</td><td><input type="submit" name="_eventId_save" value="Save" />
<input type="submit" name="_eventId_cancel" value="Cancel" />   </td>
</tr>
</form>


</body>
<html>