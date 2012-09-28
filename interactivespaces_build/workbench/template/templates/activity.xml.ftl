<?xml version="1.0"?>
<activity <#if project.activityDescription.builderType?has_content>builder="${project.activityDescription.builderType}"</#if>>
    <name>${project.activityDescription.name}</name>
    <description>
${project.activityDescription.description}
    </description>
    <version>${project.activityDescription.version}</version>
    <identifyingName>${project.activityDescription.identifyingName}</identifyingName>
</activity>
