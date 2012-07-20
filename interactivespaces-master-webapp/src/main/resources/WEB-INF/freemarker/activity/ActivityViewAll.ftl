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
<title>Interactive Spaces Admin: Activities</title>

<#include "/allpages_head.ftl">

</head>

<body>

<#include "/allpages_body_header.ftl">

<h2>Activities</h2>

<div class="commandBar"><ul>
<li><button type="button" id="uploadButton" onclick="window.location='upload.html?mode=embedded'" title="Upload an activity">Upload</button></li>
</ul></div>

<div id="commandResult">
</div>

<table>
<#list activities as activity>
    <tr>
      <td><a href="${activity.id}/view.html">View</a></td>
      <td>${activity.name}</td>
      <td>${activity.version}</td>
</#list>
</table>

</body>
<html>