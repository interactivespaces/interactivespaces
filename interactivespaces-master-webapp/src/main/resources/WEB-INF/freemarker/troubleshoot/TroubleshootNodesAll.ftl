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
<title>Interactive Spaces Admin: Troubleshoot Nodes</title>

<#include "/allpages_head.ftl">
</head>

<body class="admin-content">

<h1>Troubleshoot Nodes</h1>

<ul>
<#list nodes as node>
    <li>${node.nodeName}</li>
    <ul>
        <li>Published Topics</li>
        <ul>
<#list node.publisherTopics as publisher>
            <li>${publisher}</li>
</#list>
        </ul>
        <li>Subscribed Topics</li>
        <ul>
<#list node.subscriberTopics as subscriber>
            <li>${subscriber}</li>
</#list>
        </ul>
    </ul>
</#list>
</ul>

</body>
<html>