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

<body class="admin-content">

<h2>Activities</h2>

<table class="commandBar">
  <tr>
    <td><button type="button" id="uploadButton" onclick="ugly.changePage('/interactivespaces/activity/upload.html?mode=embedded')" title="Upload an activity">Upload</button></td>
  </tr>
</table>

<div id="commandResult">
</div>

<table class="activity-list">
  <tr>
    <th></th>
    <th>Name</th>
    <th>Identifying Name</th>
    <th>Version</th>
    <th>Last Uploaded</th>
    <th>Last Started</th>
    <th>Bundle Content Hash</th>
  </tr>
<#list activities as activity>
    <#assign trCss = (activity_index % 2 == 0)?string("even","odd")>
    <tr class="${trCss}">
      <td><a class="uglylink" onclick="return ugly.changePage('/interactivespaces/activity/${activity.id}/view.html', event);">View</a></td>
      <td>${activity.name?html}</td>
      <td>${activity.identifyingName?html}</td>
      <td>${activity.version}</td>
      <td>${activity.lastUploadDate?datetime}</td>
      <td><#if activity.lastStartDate??>${activity.lastStartDate?datetime}</#if></td>
      <td class="bundle-content-hash"><#if activity.bundleContentHash??>${activity.bundleContentHash}</#if></td>
    </tr>
</#list>
</table>

</body>
<html>