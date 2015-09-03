<#--
 * Copyright (C) 2015 Google Inc.
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
<title>Interactive Spaces Admin: Live Activity Groups</title>

<#include "/allpages_head.ftl" >
</head>

<body class="admin-content">

<h1>Clone Live Activity Group: ${liveactivitygroupname?html}</h1>

<form  method="post">

<table>
  <tr>
    <td>Name Prefix</td>
    <td>
      <@spring.formInput path="form.namePrefix" />
      <@spring.showErrors '<br>', 'fieldError' />
    </td>
  </tr>

  <tr>
    <th>&nbsp;</th>
    <td>
      <input type="submit" value="Clone" />
      <button type="button" id="cancelButton" onclick="window.location='/interactivespaces/liveactivitygroup/${id}/view.html'" title="Cancel the edit">Cancel</button>
    </td>
  </tr>
</table>

</form>

</body>
<html>
