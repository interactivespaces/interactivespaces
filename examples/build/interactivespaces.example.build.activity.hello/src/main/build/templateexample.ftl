#!/bin/bash

GREETING="Hello from templateexample"
echo ${"$"}{GREETING}, my project home is ${"$"}{project.home}

echo ${project.identifyingName} version ${project.version}

<#list project.configurationProperties as property>
echo      property ${property.name}
</#list>
