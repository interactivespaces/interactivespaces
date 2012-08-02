<?xml version="1.0"?>
<activity <#if project.activity.builderType?has_content>builder="${project.activity.builderType}"</#if>>
    <name>${project.activity.name}</name>
    <description>
${project.activity.description}
    </description>
    <version>${project.activity.version}</version>
    <identifyingName>${project.activity.identifyingName}</identifyingName>
</activity>
