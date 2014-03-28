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
<title>Interactive Spaces Admin: Scripts</title>

<#include "/allpages_head.ftl">

</head>

<body class="admin-content">

<h2>Scripts</h2>

<table class="commandBar">
  <tr>
    <td><button type="button" id="newScriptButton" onclick="window.location='new.html?mode=embedded'" title="Create a new script">New</button></td>
  </tr>
</table>

<div id="commandResult">
</div>

<table>
<#list scripts as script>
    <tr>
      <td><a class="uglylink" onclick="return ugly.changePage('/interactivespaces/admin/namedscript/${script.id}/view.html', event);">View</a></td>
      <td>${script.name}</td>
</#list>
</table>

</body>
<html>