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
<title>Interactive Spaces Admin: Space Controllers</title>

<#include "/allpages_head.ftl" >
</head>

<body class="admin-content">

<h1>Edit Space Controller: ${spacecontroller.name?html}</h1>

<form  method="post">

<table>
  <tr>
    <th>UUID</th>
    <td>${spacecontroller.uuid}</td>
  </tr>
  <tr>
    <th>Name</th>
    <td>
      <@spring.formInput path="spacecontroller.name" />
      <@spring.showErrors '<br>', 'fieldError' />
    </td>
  </tr>

  <tr>
    <th>Host ID</th>
    <td>
      <@spring.formInput path="spacecontroller.hostId" />
      <@spring.showErrors '<br>', 'fieldError' />
    </td>
  </tr>

  <tr>
    <th>Mode</th>
    <td>
      <@spring.formSingleSelect "spacecontroller.mode", modes, "" />
      <@spring.showErrors '<br>', 'fieldError' />
    </td>
  </tr>

  <tr>
    <th valign="top">Description</th>
    <td>
      <@spring.formTextarea path="spacecontroller.description" attributes='rows="5" cols="40"' />
    </td>
  </tr>

  <tr>
    <th>&nbsp;</th>
    <td>
      <input type="submit" value="Save" />
      <button type="button" id="cancelButton" onclick="window.location='/interactivespaces/spacecontroller/${id}/view.html'" title="Cancel the edit">Cancel</button>
    </td>
  </tr>
</table>

</form>

</body>
<html>