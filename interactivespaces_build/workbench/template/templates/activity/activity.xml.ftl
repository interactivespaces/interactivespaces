<?xml version="1.0"?>
<activity <#if project.builderType?has_content>builder="${project.builderType}"</#if>>
    <name>${project.name}</name>
    <description>
${project.description}
    </description>

    <identifyingName>${project.identifyingName}</identifyingName>
    <version>${project.version}</version>
</activity>
