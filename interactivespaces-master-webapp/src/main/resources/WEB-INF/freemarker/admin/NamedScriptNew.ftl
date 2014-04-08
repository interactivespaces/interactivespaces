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
<title>Interactive Spaces Admin: Named Scripts</title>

<#include "/allpages_head.ftl">

</head>

<body class="admin-content">

<h2>New Named Script</h2>

<form action="${flowExecutionUrl}" method="post">
<table>
<tr>
<td>Name</td>
<td>
<@spring.formInput path="namedScriptForm.name" />
<@spring.showErrors '<br>', 'fieldError' />
</td>
</tr>
<tr>
<td valign="top">Description</td>
<td>
<@spring.formTextarea path="namedScriptForm.description" attributes='rows="5" cols="40"' />
<@spring.showErrors '<br>', 'fieldError' />
</td>
</tr>
<tr>
<td>Language</td>
<td>
<@spring.formInput path="namedScriptForm.language" />
<@spring.showErrors '<br>', 'fieldError' />
</td>
</tr>
<tr>
<td>Scheduled?</td><td><@spring.formCheckbox path="namedScriptForm.scheduled" attributes="size='128' disabled" /></td>
</tr>
<tr>
<td>Schedule</td>
<td>
<div>
<select id="schedule-type" disabled>
<option value="0">None</option>
</select>
</div>
</td>
</tr>
<tr>
<td valign="top">Content</td>
<td>
<@spring.formTextarea path="namedScriptForm.content" attributes='rows="30" cols="80"'/>
<@spring.showErrors '<br>', 'fieldError' />
</td>
</tr>
<tr>
<td>&nbsp;</td><td><input type="submit" name="_eventId_save" value="Save" />
<input type="submit" name="_eventId_cancel" value="Cancel" />   </td>
</tr>
</form>


</body>
<html>