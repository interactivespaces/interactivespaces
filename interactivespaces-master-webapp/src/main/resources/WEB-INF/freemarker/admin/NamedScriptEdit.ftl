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
<title>Interactive Spaces Admin: Named Scripts</title>

<#include "/allpages_head.ftl" >

<link type="text/css" href="/interactivespaces/css/jquery.ui.all.css" rel="stylesheet" />
<link type="text/css" href="/interactivespaces/css/jquery-gentleSelect.css" rel="stylesheet" />
<link type="text/css" href="/interactivespaces/css/jquery-cron.css" rel="stylesheet" />

<style type="text/css"> 
/* css for timepicker */
.ui-timepicker-div .ui-widget-header { margin-bottom: 8px; }
.ui-timepicker-div dl { text-align: left; }
.ui-timepicker-div dl dt { height: 25px; margin-bottom: -25px; }
.ui-timepicker-div dl dd { margin: 0 10px 10px 65px; }
.ui-timepicker-div td { font-size: 90%; }
.ui-tpicker-grid-label { background: none; border: none; margin: 0; padding: 0; }
</style>
<script type="text/javascript" src="/interactivespaces/js/jquery-ui-1.8.21.custom.min.js"></script>
<script type="text/javascript" src="/interactivespaces/js/jquery-ui-timepicker-addon.js"></script>
<script type="text/javascript" src="/interactivespaces/js/jquery-gentleSelect-min.js"></script>
<script type="text/javascript" src="/interactivespaces/js/jquery-cron.js"></script>

<script type="text/javascript">
function showScheduleNone() {
    $('#schedule-repeat').hide();
    $('#schedule-once').hide();
}

function showScheduleRepeat() {
    $('#schedule-repeat').show();
    $('#schedule-once').hide();
}

function showScheduleOnce() {
    $('#schedule-repeat').hide();
    $('#schedule-once').show();
}

$(document).ready(function() {
    
$('#schedule-repeat').cron().hide();
$('#schedule-once').datetimepicker({
    ampm: true
}).hide();

$('form').submit(function() {
    var type = $('#schedule-type').val();
    var val = "";
    if (type == '1') {
        val = 'once:' + $('#schedule-once').val();
    } else if (type == '2') {
        val = 'repeat:' + $('#schedule-repeat').cron('value');
    }
    
    alert(val);
    $('#schedule').val(val);
    return true;
});

var schedule = $('#schedule').val();
if (schedule.indexOf('repeat:') == 0) {
    $('#schedule-type').val(2);
    var cur = schedule.substring(7);
    if (cur == '') {
        cur = '* * * * *';
    }
    $('#schedule-repeat').cron('value', cur);
    showScheduleRepeat();
} else if (schedule.indexOf('once:') == 0) {
    $('#schedule-type option:eq(1)').attr('selected','selected');
    var cur = schedule.substring(5);
    $('#schedule-once').val(cur);
    showScheduleOnce();
} else {
    $('#schedule-type option:eq(0)').attr('selected','selected');
    showScheduleNone();
}

$('#schedule-type')
    .gentleSelect({
        title: "Select a schedule type",
    })
    .change(function() {
        var type = $('#schedule-type').val();
        if (type == '1') {
            showScheduleOnce();
        } else if (type == '2') {
            showScheduleRepeat();
        } else {
            showScheduleNone();
        }
    });
});

</script>
</head>

<body>

<#include "/allpages_body_header.ftl">

<h1>Edit Named Script: ${script.name}</h1>

<form  method="post">

<table>
<tr>
<td>Name</td>
<td>
<@spring.formInput path="script.name" />
<@spring.showErrors '<br>', 'fieldError' />
 </td>

</tr>
<tr>
<td valign="top">Description</td><td><@spring.formTextarea path="script.description" attributes='rows="5" cols="40"' /></td>
</tr>
<tr>
<td>Language</td>
<td>
<@spring.formInput path="script.language" />
<@spring.showErrors '<br>', 'fieldError' />
 </td>
</tr>
<tr>
<td>Scheduled?</td>
<td>
<@spring.formCheckbox path="script.scheduled" />
<@spring.showErrors '<br>', 'fieldError' />
</td>
</tr>
<tr>
<td>Schedule</td>
<td>
<div>
<select id="schedule-type">
<option value="0">None</option>
<option value="1">Once</option>
<option value="2">Repeating</option>
</select>
<span id='schedule-repeat'></span>
<input type="text" id="schedule-once">
</div>
<@spring.formHiddenInput path="script.schedule" attributes="size='128'" />
<@spring.showErrors '<br>', 'fieldError' />
</td>
</tr>
<tr>
<td valign="top">Content</td>
<td>
<@spring.formTextarea path="script.content" attributes='rows="30" cols="80"' />
<@spring.showErrors '<br>', 'fieldError' />
 </td>
</tr>
<tr>
<th>&nbsp;</th>
<td>
<input type="submit" value="Save" />
<button type="button" id="cancelButton" onclick="window.location='/interactivespaces/admin/namedscript/${id}/view.html'" title="Cancel the edit">Cancel</button>
</td>
</table>

</form>

</body>
<html>